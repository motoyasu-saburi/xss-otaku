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
       
