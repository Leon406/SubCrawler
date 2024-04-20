package me.leon

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NetworkSubTest {
    @Test
    fun subParse() {

        val e =
            "https://gitlab.com/univstar1/v2ray/-/raw/main/data/clash/general.yaml"

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

    @Test
    fun parseVless() {
        val uri =
            "vless://21f181f3-2f66-47a8-b4d5-7aef046cc087@104.19.146.137:443?encryption=none&security=tls&sni=ap.utopub.com&type=ws&host=ap.utopub.com&path=%2futopub-vless#NY%40vless"
        Parser.parseVless(uri).also {
            Assertions.assertEquals(uri, it.toUri())
            println(it.info())
        }
    }
  @Test
    fun parseSs() {
        val uri =
            "ss://YWVzLTI1Ni1jZmI6OWQ2Y2NlYWEzNzNiZjJjOGFjYjIyZTYwYjZhNThiZTZANDUuNzkuMTExLjIxNDo0NDM=#%E7%BE%8E%E5%9B%BD"
        Parser.parseSs(uri).also {
            Assertions.assertEquals(uri, it.toUri())
            println(it.info())
        }
    }

    @Test
    fun parseText() {

        Parser.parseFromSub(
                "https://raw.iqiq.io/caijh/FreeProxiesScraper/2951ca40e0b93dc37be07a46ecc528bf245b6be8/README.md"
            )
            .also { println(it.size) }
    }
}
