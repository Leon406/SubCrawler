package me.leon

import java.io.File
import java.text.NumberFormat
import java.util.*
import me.leon.domain.Lanzou
import me.leon.support.*
import org.junit.jupiter.api.Test

class ExtTest {

    @Test
    fun encode() {
        println("https://suo.yt/WtbjDPJ".readFromNet())
        println("你好Leon".b64Encode())

        println("5L2g5aW9TGVvbg==".b64Decode())
        println("你好Leon".urlEncode())
        println("%E4%BD%A0%E5%A5%BDLeon".urlDecode())
    }

    @Test
    fun v2rayTest() {
        val url =
            "vmess://ew0KICAidiI6ICIyIiwNCiAgInBzIjogIvCfh7rwn4e4576O5Zu9IOKYhuKYhiAgMDEg4piGTlRU4piGICAgMS4y5YCN" +
                "546HIiwNCiAgImFkZCI6ICJiai5rZWFpeXVuLnh5eiIsDQogICJwb3J0IjogIjMxMTAzIiwNCiAgImlkIjogIjQ0MTg5Mz" +
                "QxLTJjYzktM2JlOS1iYjEwLWMxMzVlOThjZDhlYiIsDQogICJhaWQiOiAiMiIsDQogICJzY3kiOiAiYXV0byIsDQogICJu" +
                "ZXQiOiAid3MiLA0KICAidHlwZSI6ICJub25lIiwNCiAgImhvc3QiOiAid3d3LmJhaWR1LmNvbSIsDQogICJwYXRoIjogIi9" +
                "2MnJheSIsDQogICJ0bHMiOiAiIiwNCiAgInNuaSI6ICIiDQp9"
        println(Parser.parseV2ray(url).toUri())
    }

    @Test
    fun ssTest() {
        val url =
            "ss://YWVzLTI1Ni1nY206bjh3NFN0bmJWRDlkbVhZbjRBanQ4N0VBQDE1NC4xMjcuNTAuMTM4OjMxNTcy#(%e5%b7%b2%e5%9d%9" +
                "a%e6%8c%ba5%e5%a4%a9)%e5%8d%97%e9%9d%9e%e3%80%90%e5%88%86%e4%ba%ab%e6%9d%a5%e8%87%aaYoutube%e" +
                "4%b8%8d%e8%89%af%e6%9e%97%e3%80%91"
        println(Parser.parseSs(url).toUri())

        val url2 =
            "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTpyNFRlRXQ1YkswVURAc3MuY2Euc3NobWF4Lm5ldDoxNDQz"
        println(Parser.parseSs(url2).toUri())
    }

    @Test
    fun ssrTest() {
        val url =
            "ssr://bnRlbXAxNi5ib29tLnBhcnR5OjIxMDAwOmF1dGhfYWVzMTI4X3NoYTE6YWVzLTI1Ni1jZmI6aHR0cF9zaW1wbGU6VldzNU1" +
                "rTlQvP29iZnNwYXJhbT1aRzkzYm14dllXUXVkMmx1Wkc5M2MzVndaR0YwWlM1amIyMCZwcm90b3BhcmFtPU1UUXpNRGN6T" +
                "2tONk9HRlBhUSZyZW1hcmtzPTZhYVo1cml2TFVJJmdyb3VwPU1R"
        println(Parser.parseSsr(url).toUri())

        val url2 =
            "ssr://bjU3LmJvb20ucGFydHk6MjUwMDA6YXV0aF9hZXMxMjhfc2hhMTphZXMtMjU2LWNmYjpodHRwX3NpbXBsZTpWV3M1TWtOVC" +
                "8_b2Jmc3BhcmFtPVpHOTNibXh2WVdRdWQybHVaRzkzYzNWd1pHRjBaUzVqYjIwJnByb3RvcGFyYW09TVRRek1EY3pPa042" +
                "T0dGUGFRJnJlbWFya3M9NmFhWjVyaXZMVVEmZ3JvdXA9TVE"
        println(Parser.parseSsr(url2).toUri())
    }

    @Test
    fun trojanTest() {
        val url3 = "trojan://N8l9RGMa@t2.ssrsub.one:8443?sni=t2.ssrsub.one"
        println(Parser.parse(url3).toUri())
    }

    @Test
    fun queryParse() {
        val q =
            "obfsparam=ZG93bmxvYWQud2luZG93c3VwZGF0ZS5jb20&protoparam=" +
                "MTQzMDczOkN6OGFPaQ&remarks=6aaZ5rivLUI&group=MQ"
        println(q.queryParamMap())

        val q2 =
            "obfsparam=&protoparam=" +
                "dC5tZS9TU1JTVUI&remarks=UmVsYXlf8J+HqPCfh6ZDQS3wn4eo8J+HpkNBXzQxOSB8IDMuNTNNYg&group="
        println(q2.queryParamMapB64())
    }

    @Test
    fun fileTest() {
        println(File("./").canonicalPath)
        println(this.javaClass.getResource(""))
        println(this.javaClass.getResource("/"))
        println(this.javaClass.classLoader.getResource(""))
        println(this.javaClass.classLoader.getResource("/"))
    }

    @Test
    fun sliceTest() {
        println(7.slice(3))
        println(9.slice(3))
    }

    @Test
    fun pingTest() {
        println("wwws.baidu.com".quickPing())
        println("wwws.baidu.com".quickPing())
        println("www.baidu.com".quickPing())
    }

    @Test
    fun socketTest() {
        println("wwws.baidu.com".quickConnect(50))
        println("www.baidu.com".quickConnect(80))
        println("www.baidu.com".quickConnect(443))
    }

    @Test
    fun lanzouDirectLink() {
        val url = "https://leon.lanzoub.com/icjqqmk38xg"

        url.readFromNet()
            .run { "(/fn\\?\\w{6,})\" frameborder".toRegex().find(this)!!.groupValues[1] }
            .also {
                println(it)
                "https://www.lanzouw.com/$it".readFromNet().also {
                    println(it)
                    val sign = "(?:vsign = +|'sign':)'(\\w+)'".toRegex().find(it)!!.groupValues[1]
                    "https://www.lanzouw.com/ajaxm.php"
                        .post(
                            mutableMapOf(
                                "action" to "downprocess",
                                "signs" to "?ctdf",
                                "sign" to sign,
                                "ves" to "1"
                            )
                        )
                        .fromJson<Lanzou>()
                        .run { println("$dom/file/${this.url}") }
                }
            }
    }

    @Test
    fun timeZone() {
        System.setProperty("user.timezone", "GMT +04")
        println(timeStamp())
        println(timeStamp("GMT-3"))
        println(timeStamp("UTC"))
        println(timeStamp("America/New_York"))
    }

    @Test
    fun dd() {
        val d1 = 2.147483647E9
        val d = 2_147_483_647.toDouble()
        println(d.toString())
        val instance = NumberFormat.getInstance()
        instance.isGroupingUsed = false // 设置不使用科学计数器
        instance.maximumFractionDigits = 2 // 小数点最大位数
        println(instance.format(d1))
        "\uD83C\uDDFA\uD83C\uDDF8 美国(欢迎订阅YouTube：8度科技%"
            .replace("[【（(].+[)）%】]?".toRegex(), "")
            .also { println(it) }

        "（欢迎订阅youtube：8度科技".replace("[【（(].+[)）%】]?".toRegex(), "").also { println(it) }
    }
}
