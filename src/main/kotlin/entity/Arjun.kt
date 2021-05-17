package entity

class Arjun {

    // TODO split to config file (enum file?)
    private val blindParams = listOf("redirect", "redir", "url", "link", "goto", "debug", "_debug", "test", "get", "index", "src", "source", "file",
        "frame", "config", "new", "old", "var", "rurl", "return_to", "_return", "returl", "last", "text", "load", "email",
        "mail", "user", "username", "password", "pass", "passwd", "first_name", "last_name", "back", "href", "ref", "data", "input",
        "out", "net", "host", "address", "code", "auth", "userid", "auth_token", "token", "error", "keyword", "key", "q", "query", "aid",
        "bid", "cid", "did", "eid", "fid", "gid", "hid", "iid", "jid", "kid", "lid", "mid", "nid", "oid", "pid", "qid", "rid", "sid",
        "tid", "uid", "vid", "wid", "xid", "yid", "zid", "cal", "country", "x", "y", "topic", "title", "head", "higher", "lower", "width",
        "height", "add", "result", "log", "demo", "example", "message")

    fun arjun() {
        val response = "" // TODO Normal Request を送り、 Response の Text を受け取る

        val regex = Regex("<input.*?name=\'(.*?)\'.*?>|<input.*?name=\"(.*?)\".*?>'")
        val matches = regex.findAll(response)
        matches.forEach {
            try {
                val matchValue = it.groupValues
                val singleQuoteMatch = matchValue[0]
                val foundParam = matchValue[1]  // TODO change "foundParam" --> "foundParam"
                // logger.good('Heuristics found a potentially valid parameter: %s%s%s. Priortizing it.' % (green, foundParam, end))

                if (!blindParams.any { blindParam -> foundParam == blindParam }) {
                    blindParams.insert(0, foundParam)
                }
            } //catch {

            //}
        }
//        threadpool = concurrent.futures.ThreadPoolExecutor(max_workers=threadCount)
//        futures = (threadpool.submit(checky, param, paraNames, url, headers, GET, delay, timeout) for param in blindParams)
//        for i, _ in enumerate(concurrent.futures.as_completed(futures)):
//        if (i + 1 == len(blindParams) or (i + 1) % threadCount == 0) {
//            logger.info('Progress: %i/%i\r' % (i + 1, len(blindParams)))
//        }
//    return paraNames
    }
}