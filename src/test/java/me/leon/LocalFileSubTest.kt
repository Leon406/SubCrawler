package me.leon

import me.leon.support.removeFlags
import org.junit.jupiter.api.Test

class LocalFileSubTest {
    @Test
    fun readLocal() {
        Parser.parseFromSub("$ROOT/V2RayN.txt").joinToString("|") { it.toUri() }.also {
            println(it)
        }
    }

    @Test
    fun readLocal2() {
        Parser.parseFromSub("$ROOT/subs.txt").joinToString("|") { it.toUri() }.also { println(it) }
    }

    @Test
    fun readLocal3() {
        Parser.parseFromSub("$ROOT/bihai.yaml").joinToString("\n") { it.info() }.also {
            println(it)
        }
    }

    @Test
    fun readLocal4() {
        Parser.parseFromSub(NODE_OK)
            .filterNot { it.methodUnSupported().apply { if (this) println("____$it") } }
            .joinToString("\n") {
                it.name
                    .removeFlags()
                    .replace(NodeCrawler.REG_AD, "")
                    .replace(NodeCrawler.REG_AD_REPLACE, NodeCrawler.customInfo)
            }
            .also { println(it) }
    }

    @Test
    fun readLocalSSR() {
        Parser.parseFromSub(NODE_SSR)
            .also { println(it.size) }
            .filterNot { it.methodUnSupported() }
            .filterIsInstance<SSR>()
            //            .joinToString("\n") { it.name }
            .also { println(it.map { it.method }.groupBy { it }) }
    }
}
