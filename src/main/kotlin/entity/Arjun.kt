package entity

class Arjun {
    fun arjun() {

        val response = "" // TODO Normal Request を送り、 Response の Text を受け取る

        val regex = Regex("<input.*?name=\'(.*?)\'.*?>|<input.*?name=\"(.*?)\".*?>'")
        val matches = regex.findAll(response)
        matches.forEach {
//            try:
//                foundParam = it[1]
//                except UnicodeDecodeError:
//                    continue
//                logger.good('Heuristics found a potentially valid parameter: %s%s%s. Priortizing it.' % (green, foundParam, end))
//
//                if foundParam not in blindParams:
//                blindParams.insert(0, foundParam)
//                threadpool = concurrent.futures.ThreadPoolExecutor(max_workers=threadCount)
//                futures = (threadpool.submit(checky, param, paraNames, url,
//                    headers, GET, delay, timeout) for param in blindParams)
//                for i, _ in enumerate(concurrent.futures.as_completed(futures)):
//                if i + 1 == len(blindParams) or (i + 1) % threadCount == 0:
//                logger.info('Progress: %i/%i\r' % (i + 1, len(blindParams)))
//                return paraNames
//            }
        }

}