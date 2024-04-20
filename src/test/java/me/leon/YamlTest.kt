package me.leon

import me.leon.support.*
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

class YamlTest {

    @Test
    fun yaml() {
        val list =
            listOf(
                "https://ghproxy.com/https://raw.githubusercontent.com/clashconfig/online/main/SurfShark(34687).yml",
                //
                // "https://ghproxy.com/https://raw.githubusercontent.com/mahdibland/SSAggregator/master/sub/sub_merge_yaml.yml",
                "https://ghproxy.com/https://raw.githubusercontent.com/vveg26/get_proxy/main/dist/clash.config.yaml",
            )

        for (url in list) {
            println(url)
            val data = url.readFromNet()
            if (data.isNotEmpty()) {
                with(Yaml(Constructor(Clash::class.java, LoaderOptions())).load(data.fixYaml()) as Clash) {
                    println(
                        this.proxies.map(Node::toNode).filterIsInstance<V2ray>().joinToString(
                            "|"
                        ) { sub ->
                            sub.also { println(it) }.toUri()
                        }
                    )
                }
            } else {
                println("no content")
            }
        }
    }

    @Test
    fun str() {
        val raw =
            """
- cipher: aes-128-cfb
  name: '[10-12]-🇦🇶-本机地址-964-${'$'}*@14.29.124.174'
  password: 
  server: ${'$'}*@14.29.124.174
  port: 11049
  type: ss
        """
                .trimIndent()

        println(raw)
        raw.replace("!<[^>]+>".toRegex(), "")
            .replace("  password: \n", "")
            .replace("server: $*@", "server: ")
            .also { println(it) }
    }
}
