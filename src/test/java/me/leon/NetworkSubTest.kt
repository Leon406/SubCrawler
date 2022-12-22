package me.leon

import org.junit.jupiter.api.Test

class NetworkSubTest {
    @Test
    fun subParse() {
        /**
         * https://qiaomenzhuanfx.netlify.app/
         * https://ghproxy.net/https://raw.githubusercontent.com/learnhard-cn/free_proxy_ss/main/clash/providers/provider_home_netflix.yaml
         * https://ghproxy.com/https://raw.githubusercontent.com/Jsnzkpg/Jsnzkpg/Jsnzkpg/Jsnzkpg
         * https://ghproxy.net/https://raw.githubusercontent.com/git-yusteven/openit/main/long
         * https://ghproxy.net/https://raw.githubusercontent.com/webdao/v2ray/master/nodes.txt
         * https://ghproxy.com/https://raw.githubusercontent.com/mahdibland/get_v2/main/pub/changfengoss13.yaml
         * https://ghproxy.com/https://raw.githubusercontent.com/mahdibland/get_v2/main/pub/changfengoss1.yaml
         * https://ghproxy.com/https://raw.githubusercontent.com/eycorsican/rule-sets/master/kitsunebi_sub
         * https://ghproxy.net/https://raw.githubusercontent.com/alanbobs999/TopFreeProxies/master/sub/sub_merge_base64.txt
         * https://ghproxy.net/https://raw.githubusercontent.com/JasonZhao2k08/fly/main/index.html
         */
        // https://ghproxy.com/https://raw.githubusercontent.com/samjoeyang/subscribe/main/fly
        val e =
            "https://ghproxy.net/https://raw.githubusercontent.com/alanbobs999/TopFreeProxies/master/sub/sub_merge_base64.txt"
        //        runBlocking {
        //            Parser.parseFromSub(e)
        //                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort,
        // 1000) } }
        //                .filter { it.second.await() > -1 }
        //                .also { println(it.size) }
        //                .forEach { println(it.first.info() + ":" + it.second) }
        //        }

        listOf(
                e,
            )
            .forEach {
                kotlin
                    .runCatching {
                        Parser.parseFromSub(it)
                            .also { println(it.size) }
                            .joinToString(
                                //                        "|",
                                "\r\n",
                                transform = Sub::toUri
                            )
                            .also {
                                println("___________")
                                println(it)
                            }
                    }
                    .onFailure { it.printStackTrace() }
            }
    }

    @Test
    fun sub() {
        val l1 = Parser.parseFromSub("https://etproxypool.ga/clash/proxies")
        val l2 = Parser.parseFromSub("https://suo.yt/v9UsfNr")
        val combine = l1 + l2
        val l1Only = combine - l2
        val l2Only = combine - l1
        val share = l1 - l1Only
        println("共享 ${share.size}")
        println("l1 ${l1.size} 独有 ${l1Only.size}")
        println("l2 ${l2.size} 独有 ${l2Only.size}")
    }
}
