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
                this.proxies.map(Node::toNode)
                    //                    .filterIsInstance<V2ray>()
                    .joinToString("|") { sub -> sub.toUri() }
            )
        }
    }

    @Test
    fun yaml() {

        var url = "https://raw.fastgit.org/AzadNetCH/Clash/main/AzadNet.yml".readFromNet()
        if (url.isNotEmpty()) {
            with(Yaml(Constructor(Clash::class.java)).load(url) as Clash) {
                println(
                    this.proxies.map(Node::toNode)
                        //                    .filterIsInstance<V2ray>()
                        .joinToString("|") { sub -> sub.also { println(it.info()) }.toUri() }
                )
            }
        } else {
            println("no content")
        }
    }
}
