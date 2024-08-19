package me.leon

import me.leon.support.urlDecode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NetworkSubTest {
    @Test
    fun subParse() {

        val e =
            "https://raw.githubusercontent.com/itsyebekhe/HiN-VPN/main/subscription/normal/mix"

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
    fun parseHysteria2() {
        val uri =
            "hysteria2://HowdyHysteria2023w0W@hysteria.udpgw.com:8443?insecure=1&sni=sni-here.com&obfs=salamander&obfs-password=HysteriaHowdy#\uD83C\uDD94oneclickvpnkeys%20\uD83D\uDD12%20HY2-UDP-N/A%20\uD83C\uDDA5\uD83C\uDDA5%20%20117ms"
       println(Parser.parse(uri))
       Parser.parseHysteria2(uri).also {
            println(it.toUri().urlDecode())
            Assertions.assertEquals(uri.substringBefore("#"), it.toUri().substringBefore("#"))
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
