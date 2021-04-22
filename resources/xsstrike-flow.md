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
 --> 一番下の else: 
    --> crawlingResult = photon(target, headers, level, threadCount, delay, timeout, skipDOM)
    --> // 上のやつで forms, domURLs, が取得できるっぽい
　　 -->    --> crawl をFutureで並列実行 



# singleFuzz()
1. paramData が存在するかで GET, POST どちらかを選択する
2. http, https それぞれ送って Protocol を調べる。
3. WAF detector 
   1. ?xss=<script>alert("XSS)</script> を送る
   2. 400 >= のエラーコードが帰ってきたらWAF の可能性があると考え、更に評価
   3. WAF Signature からステータスコード、ページデータなどとの一致度でScoreを＋していく。
   4. WAFで一番マッチ度が高いやつを返す
4. param copy 
   (maybe) もう１回リクエストを送って？パラメータを取得する
   // TODO ちゃんと読む
   
5. fuzzer(url, 4.のパラメータ, header, GET, delay, timeout, WAF(使ってない）, encoding)
   //TODO
