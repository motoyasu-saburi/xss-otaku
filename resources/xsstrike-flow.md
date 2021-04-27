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
1. `<script>...</script>` 系統のタグを全て取得
2. 1.で取得した <script> タグを使ってループ
    1. <script>...</script> を \n で split し、行単位になった JS と思われる行でループ
        1. parts なる部品にするため、行を `var ` で splitする
        2. parts が存在する場合に以下のループを行う　（が、何度読んでもデッドコードに見える・・・）
           (過去ログ見ると前までグローバル変数だったのでバグ。コントリビュートチャンスですよ、誰か）
           https://github.com/s0md3v/XSStrike/commit/3723a95db48b6cb25f098db2c4c16aa52c488236#diff-8ba4e7bf4b3f2db95f21f25a97061568e527589b36ec6d2d692a5d2c42c5c4f7L8
        
           > controlledVariables = set()   
           > allControlledVariables = set()  # ここで後続のループに使う配列を初期化して
           >  if len(parts) > 1:             # ここが 2. の「parts の有無の確認箇所」で、
           >    for part in parts:           # part =  ["var ", "aa=123"] 形式
           >      for controlledVariable in allControlledVariables:  # ここ、３行上で（毎ループ）初期化してるから、デッドコードでは・・・  
           >        if controlledVariable in part:
           >          controlledVariables.add(re.search(r'[a-zA-Z$_][a-zA-Z0-9$_]+', part).group().replace('$', '\$'))
           
            # TODO あとでここがグローバル変数になった前提で見直す
            1. （毎ループ / 初期化したばかりの）配列を使ってループする
              1. もし controlledVariable が part の中に入っている場合
                1. {英字, $, _} から始まって {英""数字"", $, _} が連続する文字列から $ -> \$ に置換する
                2. それを controlledVariable に追加する
        
        3. script の行に対し `source` ( document.location, location.href など)でサーチする
        4. 見つかった興味深い JS の行でループする
            1. js 列から空白スペースを消す
            2. 事前に見つけておいた `var ...` の箇所について、存在した様であれば次
                1. その `var ` でループする
                2. `source` があったということで `sourceFound` フラグを trueにしつつ
                   controlledVariables に `source` を追加する
                   
        5. これまで見つかった `controlledVariables` を保持するために
            1. `allControlledVariables` に追加する。
        6. （一つ上で追加した） `allControlledVariables` でループ
            1. matches = list(filter(None, re.findall(r'\b%s\b' % controlledVariable, line)))
               controlledVariables から、 <script>タグの中の行（各行）を「\b」(スペースなどで区切られた箇所にマッチ)を抽出する
            　　(どうでもいいけど list(filter(None ...)) は初めて見た)
            
                1. もしマッチした行があればその部分を抽出する
                    ... のだが、なんか見つかった一個目しか line に入れてなくね？ 
                    line = re.sub(r'\b%s\b' % controlledVariable, controlledVariable, line)
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
       
