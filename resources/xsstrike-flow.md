# config.py

* delay などのプロパティ値
* 特別な { 属性, タグ }
* 普通の { タグ } 
* js, tag, handler, space のセパレーターとなる文字列
* event handler

* POP UP を出現させる関数のペイロードパターン (confirm, prompt 各３パターン)
* Filter / WAF を回避する 20パターン
* Fuzz strings to test WAFs 
* WAF をテストするためのPayloads 30種

* default header （UA, Upgrade-Insecure-Request, Accept など）
* パラメータ発見のために利用される総当たり用の一般的なパラメータ名 95種類


# Fuzzingの入り口

Main

大きく以下の４つの分岐
 --> singleFuzz()
 --> bruteforcer()
 --> scan()
 --> photon() + crawl()

# scan()
```
1. Parameter（GET URL以外のパラメータ）の有無で POST, GETを決定
2. protocol(http, https) の決定。
   target URLが https だった場合、httpsのリクエストを送って対応しているか確認。
   https がだめだったら以後のリクエストは http ベースになる。 
   
3. if not skipDOM:
   (skipDOM : クロール中にDOMXSSスキャンをスキップするフラグ）
   ここでは、脆弱性のポテンシャルがあるDOM脆弱性をチェックしている。（時間がかかる処理のためON/OFFできる）
   
   1. highlighted = dom(response)  
      
 
```

## dom()

### dom() の概要
（textベースの）responseを解析するメソッドで、DOMベースの潜在的な脆弱性を持つDOMを `highligted` の配列で返す。

メソッドの冒頭で3つの正規表現を変数 `sources`, `sinks`, `scripts` にてそれぞれ宣言しており、
`scripts` ( `<script>...</script>` )にマッチした箇所を深堀して脆弱性の有無を確認するというのが
全体の処理（ `sources`, `sinks` はその中で利用している）

* sources: よく使われる脆弱性が入り込みやすい JS のメソッド類の検知 (e.g. `location.href` など) 
* sinks: 
  - eval, Function, setTimeout などの Function型を引っ張って来れそうなメソッド類
  - document.innerHtml, document.location などのよくあるケース  
* scripts: `<script>...</script> のケース

sources:
```
r'''document\.(URL|documentURI|URLUnencoded|baseURI|cookie|referrer)|location\.(href|search|hash|pathname)|window\.name|history\.(pushState|replaceState)(local|session)Storage'''

マッチ例：
document.URL
location.href
window.name
history.pushState
localStrage
```

sinks:
```
sinks = r'''eval|evaluate|execCommand|assign|navigate|getResponseHeaderopen|showModalDialog|Function|set(Timeout|Interval|Immediate)|execScript|crypto.generateCRMFRequest|ScriptElement\.(src|text|textContent|innerText)|.*?\.onEventName|document\.(write|writeln)|.*?\.innerHTML|Range\.createContextualFragment|(document|window)\.location'''
```

scripts:
```
scripts = re.findall(r'(?i)(?s)<script[^>]*>(.*?)</script>', response)

つまり
<script> or <script{なんかの文字の連続...}> でスタートし、
間が任意の文字列の連続（の最短マッチ）で
</script> で終わる 

(?i) = 大文字小文字を区別しない
(?s) = Regexの . (なんでも１文字にマッチ）で、改行にもマッチする様にする
```

### dom() のフロー
```
Main

大きく以下の４つの分岐
 --> singleFuzz()
 --> bruteforcer()
 --> scan()
 --> photon() + crawl()
```

今回の `dom()`  を call しているのは `scan()` の内部です。
[https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/modes/scan.py#L37:title]


馬鹿正直に読んだせいでアホみたいに疲れました。
では見ていきましょう。

# dom() の概要

メソッドに引数とか description とか一切ないので読んだ限りのやつを記載しておきます。
具体的なフローは次の節にて。


`dom()` の概要は、
XSSで悪用が可能な要素を見つけて、それに関わる行を返すというのが全体の処理になります。
（この `XSSで悪用が可能` な要素のことを Injectable っぽそうな値　と勝手に読んでます）

つまり、 `dom()` では、 Injectable っぽそうな関数・型・変数などを見つける処理を実施します。

これは
* sources と呼称される `document.location` , `location.href`, `history.pushState` などの要素
* sinks と呼称される `eval`, `document.innerHTML`, `Function` などの要素
* 及び `sources`, `sinks` で見つかった関数・変数の結果を入れている変数  (e.g. `var xxx = eval("...")` の `xxx`)
  を検出し、リストで返すという感じです。


## 全体フロー

では、全体のフローを見てみましょう。

今回も自作XSSスキャナーのためにかなり丁寧に読んでるので、分量が多いです。

前回の記載を踏まえて以下の様な書き方をしています。

* （ある程度）元のコードのインデントを尊重した形式で記載
*  処理のまとまりには `#`  を使って勝手にコメント

```md
1. HTMLデータから全ての `<script>...</script>` を取得し、タグの中身(文字列）を抽出 (以下、抽出した部分を "JSコード_FULL" と呼称）
   (つまり <script> 部分は除いた `console.log(0)` みたいな場所だけ抽出）
   
2. 取得した JSコード_FULL(複数） を使ってループ
    1. JSコード（単体） を \n で split し、行単位になったものを使ってループ（以下、 `script` と呼称 )
        
        #######################################
        #  JSコードから、 "var " で split する。
        #  おそらく JSコード から意味のある場所（特に XSS に悪用できそうな Injectable な可能性が高い場所）を抽出している
        #  (この Injectable な可能性が高い箇所、というのは、次に出てくる大きな一塊の処理部分にて (`source` 変数に定義された regexを使って) 検出している） 
        #  このひとまとまりの処理の後方にて（バグってるから動かない気がするが...）変数っぽいパラメータがあったら
        #  それを controlledVariables にいれている。
        #  
        #  まとめると、この処理部分では
        #     1. 後述する Injectable っぽそうな処理を行っている行 
        #     2. その処理が入っている変数（ 要するに、 Injectable っぽそうな変数 ）
        #     を検出している。
        #
        #  この処理の塊の当該行数:
        #  https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/dom.py#L18-L25
        #######################################
        1. parts なる部品にするため、 `script` を `var ` で splitする (JSコードの構成要素として分離している。この行以降、配列の状態であれば `parts`, 個別の場合は `part` と記載する。)
        2. parts が存在する場合に以下のループを行う　（が、現行では多分デッドコード）
           (原因は、途中で出てくる `controlledVariables`, `allControlledVariables` の変数が、ループの都度初期化されてるため。
            過去ログ見ると前までグローバル変数だったのでおそらくバグ。コントリビュートチャンスですよ、誰か。
            過去ログ: https://github.com/s0md3v/XSStrike/commit/3723a95db48b6cb25f098db2c4c16aa52c488236#diff-8ba4e7bf4b3f2db95f21f25a97061568e527589b36ec6d2d692a5d2c42c5c4f7L8）
            
           # 現行コード抜粋
           > for newLine in script:
           > ...
           >     controlledVariables = set()          # ループ内で毎回初期化（コード的にこっちは正常っぽそう）
           >     allControlledVariables = set()       # ループ内で毎回初期化（こっちはバグっぽい）
           >         if len(parts) > 1:               # "var " が存在する行か（つまり、 Injectable っぽそうな行かの判別用？）
           >             for part in parts:           # part =  ["var ", "aa=123"] 形式
           >                 for controlledVariable in allControlledVariables:  # （問題の箇所) 3行上で（毎ループ）初期化してるから、デッドコード  
           >                     if controlledVariable in part:
           >                         controlledVariables.add(re.search(r'[a-zA-Z$_][a-zA-Z0-9$_]+', part).group().replace('$', '\$'))
           
           
           # 以下、上記のバグがない（ Global 変数前提）で推測まじりに書く
           1. `allControlledVariables` を使ってループを行う。
           　　この `allControlledVariables` は、後ほど出てくる `source` の regex で発見された箇所が入ってくる。
              ( `document.location`, `history.localStrage` などの検知 regex )
              
                1. allControlledVariables の中身のどれかが、現在処理中の part に部分的にでも含まれている場合、次の処理を行う
                    1. regex `[a-zA-Z$_][a-zA-Z0-9$_]+` で文字を抽出し、 `controlledVariable` に保管
                    
                      regex 部分は `$abc`, `_abc`, `abc` などにマッチ。
                      controlledVariables は、 (先述した) `allControlledVariables` に値をあとで移し替える用の（一時的？）な配列っぽそう。
                      
        #######################################
        #  行に `var xxx = document.location` などが含まれている場合 ( Injectable な可能性がある場合）
        #  `document.location` の部分を取得する
        #  その処理( document.location など）が、 parts の中に存在する場合、
        #  その変数名を抽出して `controlledVariables` に追記しておく。
        #  ついでに `sourceFound` Flagを True にしておく。
        # 
        #  この処理の塊の当該行数:
        #  https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/dom.py#L26-L35
        #######################################
        
        3. script の行に対し `source` ( document.location, location.href など)でサーチする
        4. 見つかった Injectable っぽそうな JS の行でループする
            1. `var xxx = location.href` などの見つけた箇所から、 `location.href` などの部分を抽出
            2. parts 配列の中に `location.href` (このループで見つかった Injectable っぽそうな処理を含む行） はあるか？
                1. 見つかった Injectable っぽそうな処理を含む変数を抽出し、 `controlledVariables` に追加
                2. `sourceFound` フラグを true にする
           
        ####################################### 
        #  これまで見つかった `controlledVariables` を、（バグって初期化しまくっちゃう）`allControlledVariables` に追加する 
        #  その後、追加を行った `allControlledVariables` の各変数名が、現在ループ中のJSコードの行に含まれているかを確認する。
        #  存在した場合は `line = ["tmp_dir"]` みたいな感じで、その要素を line 変数にいれる（かなり謎。 append ではなく上書きだし、バグでは？）
        #
        #  この処理の当該行数:
        #  https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/dom.py#L37-L42
        #######################################          
        5. これまで見つかった `controlledVariables` を保持するために、 
        　　現行ループ内で見つかった Injectable っぽそうな変数名の一覧 `controlledVariables` の各要素を `allControlledVariables` に  add する
        6. （一つ上で追加した）これまでの全ての Injectable っぽそうな変数名の一覧 `allControlledVariables` でループ
              
            1. 現在のJSコードの行に、これまでの Injectable っぽそうな変数名があるかチェック
            
                1. もしマッチした行があればその変数名を抽出する
                    ... のだが、なんか見つかったやつを毎度 line 変数に上書きしているので一個しか検出しなさそう。
                         
        #######################################
        #  JSコードの行部分から、Injectable っぽそうなメソッド部分(など）を抽出する。
        #  例えば、行が ` eval("alert(0)") ` だったら `eval` のみを抽出する
        #
        #  この処理の当該行数:
        #  https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/dom.py#L43-L49
        #######################################
        7. JSコードの現在の行部分から、 `eval`, `Function` などのコードを Injection できる型や関数として抽出する
            1. 対象の関数などがあれば、その要素だけを抽出する。 (つまり `eval` 部分のみを抽出）
            2. sinkFound フラグを True にする
            
        #######################################
        # これまでの結果をまとめる（返り値となる配列に要素を追加）
        #
        # この処理の当該行数:
        # https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/dom.py#L50-L51
        #######################################
        8. これまでの処理の中で、 
           * sinkFound のフラグが立った行
           * sourceFound のフラグが立った行 
           * Injectableっぽそうな変数が含まれていた行
         　の場合は、その行を `highligted` 配列に追加する。
           この highlited 配列が、 `dom()` メソッドの返り値になる（ならないこともある。それは後ほど）



################
# 当該処理: https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/dom.py#L55-L56
################
3. これまでの処理で、 `sinkFound` と `sourceFound` が見つかった場合、
   `highligted` 配列を返す。
   なければ空配列を返す
```



# bruteforcer()

# photon()  +crawl()


# singleFuzz()

ファザーは、フィルターとWebアプリケーションファイアウォールをテストするためのものです。ランダムに*遅延要求を送信し、遅延は最大30秒になる可能性があるため、非常に低速です。遅延を最小限に抑えるには、-dオプションを使用して遅延を1秒に設定します。

1. paramData ( e.g. `--data q=123` などのパラメータ) が存在するかで GET, POST どちらかを選択する
2. protocol(http, https) の決定。
   target URLが https だった場合、httpsのリクエストを送って対応しているか確認。
   https がだめだったら以後のリクエストは http ベースになる。 
3. wafDetector()
   1. /db/wafSignatures.json を読み込む。
      これはレスポンスの Status, header, page(htmlなど？) から何製のWAFかを判断するためのデータ
   2. `?xss=<script>alert("XSS)</script>` を送る
   3. response の status code が `400 <= status` 場合、
      WAF の可能性があると考え、更に評価
   4. WAF Signature からステータスコード、ページデータなどとの一致度でScoreを＋していく。
   5. WAFで一番マッチ度が高いやつを検知したWAFだとし、返す
4. parameter（複数）でループ
   ループ時に、その index 番目のパラメータの値を `v3dm0s` に変更（あとでペイロードに置換するための足跡）
   https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/config.py#L6
   
    https://github.com/s0md3v/XSStrike/blob/0ecedc1bba149931e3b32e53422d5b7c089ba9dc/core/config.py#L66
    にあるペイロード（複数）でループ
       
        1. ペイロードの文字列数・処理の中で変動する数値をベースに sleep をかける
        2. ペイロードをデコードしたあとにエンコードする
        3. 前もって置換しておいた `v3dm0s` （ペイロードの置換用の足跡）をペイロードに置換
        4. リクエストを送る
            1. エラーが起きた時はしばらくsleepしてから再送。
            2. 再送で失敗した場合は IP がブロックされたと判断して停止
            3. 再送が成功した場合は、sleepが功を奏したとして、 `5.` (次の行）に移動
        5. レスポンスにペイロードがないかをレスポンス, ペイロード共に lower してマッチング
        6. あった場合は [pass],  
           status code が 2xx 系以外の場合は [block],
           その他の場合は [filtered] というログレベルで吐き出して終わり。
       
