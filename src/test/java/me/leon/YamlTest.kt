package me.leon

import me.leon.support.readFromNet
import me.leon.support.readText
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

class YamlTest {

    fun yamlLocalTest() {

        with(Yaml(Constructor(Clash::class.java)).load(BIHAI.readText()) as Clash) {
            println(
                this.proxies
                    .map(Node::toNode)
                    //                    .filterIsInstance<V2ray>()
                    .joinToString("|") { sub -> sub.toUri() }
            )
        }
    }

    @Test
    fun yaml() {

        val list = listOf(
            "https://raw.iqiq.io/vpei/Free-Node-Merge/main/out/clash.yaml",
            "https://ghproxy.com/https://raw.githubusercontent.com/mahdibland/get_v2/main/pub/changfengoss10.yaml",
            "https://ghproxy.com/https://raw.githubusercontent.com/mahdibland/get_v2/main/pub/mattkaydiary.yaml",
        )

        for (url in list) {
            println(url)
            val data = url.readFromNet()
            if (data.isNotEmpty()) {
                with(Yaml(Constructor(Clash::class.java)).load(data
                    .replace("!<[^>]+>".toRegex(), "")
                    .replace("  password: \n", "  password: xxxxx\n")
                    .replace("server: $*@", "server: ")
                    .also { println(it) }
                ) as Clash) {

                    println(this.proxies)
//                    println(
//                        this.proxies
//                            .map(Node::toNode)
//                            .filterIsInstance<V2ray>()
////                        .filter { it.add =="in04.my1188.org" }
//                            .joinToString("|") { sub -> sub.also { println(it) }.toUri() }
//                    )
                }
            } else {
                println("no content")
            }
        }
    }

    @Test
    fun str() {
        val raw = """
- cipher: aes-128-cfb
  name: '[10-12]-🇦🇶-本机地址-964-${'$'}*@14.29.124.174'
  password: 
  server: ${'$'}*@14.29.124.174
  port: 11049
  type: ss
        """.trimIndent()

        println(raw)
        raw.replace("!<[^>]+>".toRegex(), "")
            .replace("  password: \n", "")
            .replace("server: $*@", "server: ")
            .also { println(it) }
    }

}
