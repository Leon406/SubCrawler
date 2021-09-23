package me.leon.ip

import me.leon.SHARE
import me.leon.domain.Host
import me.leon.support.readFromNet
import me.leon.support.writeLine
import org.junit.jupiter.api.Test

class HostsTest {

    // https://raw.fastgit.org/VeleSila/yhosts/master/hosts
    // https://raw.fastgit.org/jdlingyu/ad-wars/master/hosts
    // https://raw.fastgit.org/Goooler/1024_hosts/master/hosts
    // https://winhelp2002.mvps.org/hosts.txt
    // https://raw.fastgit.org/StevenBlack/hosts/master/hosts
    @Test
    fun blacklist() {
        listOf(
            "https://raw.fastgit.org/VeleSila/yhosts/master/hosts",
            "https://raw.fastgit.org/jdlingyu/ad-wars/master/hosts",
            "https://raw.fastgit.org/Goooler/1024_hosts/master/hosts",
            "https://winhelp2002.mvps.org/hosts.txt",
            "https://raw.fastgit.org/StevenBlack/hosts/master/hosts",
        )
            .flatMap {
                it
                    .readFromNet()
                    .split("\n|\r\n".toRegex())
                    .map { it.trim() }
                    .filterNot { it.isEmpty() || it.startsWith("#") }
                    .map {
                        it.split("\\s+".toRegex()).run {
                            Host(this[1]).apply {
                                ip =
                                    if (this@run[0] == "127.0.0.1" && domain != "localhost")
                                        "0.0.0.0"
                                    else this@run[0]
                            }
                        }
                    }
                    .filterNot { it.domain.contains("#") || it.domain == "0.0.0.0" }
                    .also { println(it.size) }
            }
            .distinct()
            .sortedBy { it.domain }
            .also {
                println(it.size)
                "$SHARE/blackhosts".writeLine()
                "$SHARE/blackhosts".writeLine(it.joinToString("\n"))
            }
    }

    // https://raw.fastgit.org/googlehosts/hosts/master/hosts-files/hosts
    // 需要单独走无污染 dns,获取最新的ip
    @Test
    fun whitelist() {
        listOf(
            "https://raw.fastgit.org/googlehosts/hosts/master/hosts-files/hosts",
        )
            .flatMap {
                it
                    .readFromNet()
                    .split("\n|\r\n".toRegex())
                    .map { it.trim() }
                    .filterNot { it.isEmpty() || it.startsWith("#") }
                    .map {
                        it.split("\\s+".toRegex()).run { Host(this[1]).apply { ip = this@run[0] } }
                    }
                    .filterNot { it.domain.contains("#") }
                    .also { println(it.size) }
            }
            .distinct()
            .sortedBy { it.domain }
            .also {
                println(it.size)
                "$SHARE/whitehost".writeLine()
                "$SHARE/whitehost".writeLine(it.joinToString("\n"))
            }
    }
}
