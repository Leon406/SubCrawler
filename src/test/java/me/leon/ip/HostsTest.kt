package me.leon.ip

import kotlin.streams.toList
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.HOST
import me.leon.SHARE
import me.leon.domain.DnsResolve
import me.leon.domain.Host
import me.leon.support.*
import org.junit.jupiter.api.Test

class HostsTest {

    private val reg = "https://www\\.ipaddress\\.com/ipv4/(\\d+.\\d+.\\d+.\\d+)".toRegex()

    // https://raw.fastgit.org/VeleSila/yhosts/master/hosts
    // https://raw.fastgit.org/jdlingyu/ad-wars/master/hosts
    // https://raw.fastgit.org/Goooler/1024_hosts/master/hosts
    // https://winhelp2002.mvps.org/hosts.txt
    // https://raw.fastgit.org/StevenBlack/hosts/master/hosts
    // https://github.com/E7KMbb/AD-hosts
    @Test
    fun blacklist() {
        listOf(
                "https://raw.fastgit.org/VeleSila/yhosts/master/hosts",
                "https://raw.fastgit.org/jdlingyu/ad-wars/master/hosts",
                "https://raw.fastgit.org/Goooler/1024_hosts/master/hosts",
                "https://winhelp2002.mvps.org/hosts.txt",
                "https://raw.fastgit.org/StevenBlack/hosts/master/hosts",
                "https://raw.fastgit.org/E7KMbb/AD-hosts/master/system/etc/hosts",
                "https://raw.fastgit.org/ilpl/ad-hosts/master/hosts",
                "https://raw.fastgit.org/rentianyu/Ad-set-hosts/master/hosts"
            )
            .flatMap {
                it.readFromNet()
                    .split("\n|\r\n".toRegex())
                    .map(String::trim)
                    .filterNot fn@{ it.isEmpty() || it.startsWith("#") }
                    .map {
                        it.split("\\s+".toRegex()).run {
                            Host(this[1]).apply {
                                ip =
                                    if (this@run[0] == "127.0.0.1" && domain != "localhost") {
                                        "0.0.0.0"
                                    } else {
                                        this@run[0]
                                    }
                            }
                        }
                    }
                    .filterNot { it.domain.contains("#") || it.domain == "0.0.0.0" }
                    .also { println(it.size) }
            }
            .distinct()
            .sortedBy(Host::domain)
            .also {
                println(it.size)
                "$SHARE/blackhosts".writeLine(it.joinToString("\n"), false)
            }
    }

    // https://raw.fastgit.org/googlehosts/hosts/master/hosts-files/hosts
    // 需要单独走无污染 dns,获取最新的ip
    @Test
    fun whitelist() {
        runBlocking {
            listOf(
                    "https://raw.fastgit.org/googlehosts/hosts/master/hosts-files/hosts",
                    "https://raw.fastgit.org/Leon406/pyutil/master/github/hosts",
                    "https://raw.fastgit.org/Leon406/jsdelivr/master/hosts/whitelist",
                )
                .flatMap {
                    it.readFromNet()
                        .split("\n|\r\n".toRegex())
                        .map(String::trim)
                        .filterNot { it.isEmpty() || it.startsWith("#") }
                        .map {
                            it.split("\\s+".toRegex()).run {
                                Host(this[1]).apply { ip = this@run[0] }
                            }
                        }
                        .filterNot { it.domain.contains("#") }
                        .also { println(it.size) }
                }
                .distinct()
                //                .sortedBy (Host::domain)
                .map { it to async(DISPATCHER) { it.ip.quickPing(1000) } }
                .map { it.second.await() to it.first }
                .forEach {
                    if (it.first > -1) {
                        println("ok ip ${it.second}")
                        "$SHARE/whitehost".writeLine(it.second.toString(), true)
                    } else {
                        println("err ip ${it.second}")
                    }
                }
        }
    }

    @Test
    fun dns() {

        val unReachableDomains = mutableListOf<String>()
        HostsTest::class
            .java
            .getResourceAsStream("/domains")!!
            .bufferedReader()
            .use { it.lines().toList() }
            .filterNot { it.isEmpty() || it.startsWith("#") }
            .map { dnsResolve(it) + "\t" + it }
            .filter {
                val isEmp = it.startsWith("\t")
                if (isEmp) unReachableDomains.add(it.substring(1))
                !isEmp
            }
            .also {
                HOST.toFile().writeText("##### 更新时间${timeStamp()} #####\n" + it.joinToString("\n"))
            }
        println(unReachableDomains)
        unReachableDomains
            .map { ipApiResolve(it) + "\t" + it }
            .filter {
                val isEmp = it.startsWith("\t") || it.contains(",")
                if (isEmp) println(it.substring(1))
                !isEmp
            }
            .also { HOST.toFile().appendText("\n" + it.joinToString("\n")) }
    }

    @Test
    fun ipAddress() {
        ipApiResolve("baidu.com").also { println(it) }
    }

    private fun dnsResolve(url: String): String =
        runCatching {
                "https://1.1.1.1/dns-query?name=$url&type=1"
                    .readBytesFromNet(headers = mutableMapOf("accept" to "application/dns-json"))
                    .decodeToString()
                    .fromJson<DnsResolve>()
                    .Answer
                    ?.find { it.type == 1 && it.data!!.quickPing() > 0 }
                    ?.data
                    .orEmpty()
            }
            .getOrDefault("")

    private fun ipApiResolve(url: String): String =
        runCatching {
                "https://ipaddress.com/website/$url"
                    .readBytesFromNet(
                        headers = mutableMapOf("referer" to "https://www.ipaddress.com/")
                    )
                    .decodeToString()
                    .run { reg.find(this)?.groupValues?.get(1).orEmpty() }
            }
            .getOrDefault("")
}
