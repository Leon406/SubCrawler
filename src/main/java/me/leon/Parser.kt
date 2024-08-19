package me.leon

import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*
import me.leon.support.*

private const val UUID_LENGTH = 36

object Parser {
    private val REG_SCHEMA_HASH = "(\\w+)://([^ #]+)(?:#([^#]+)?)?".toRegex()
    private val REG_SS = "([^:]+):([^@]+)@([^:]+):(\\d{1,5})/?".toRegex()
    private val REG_SSR_PARAM = "([^/]+)/\\?(.+)".toRegex()
    private val REG_TROJAN = "([^@]+)@([^:]+):(\\d{1,5})/?(?:\\?(.+))?".toRegex()

    init {
        // 信任过期证书
        val trustAllCerts: Array<TrustManager> =
            arrayOf(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                        // if needed
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                        // if needed
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return emptyArray()
                    }
                }
            )
        // Install the all-trusting trust manager
        runCatching {
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            val sslsc = sc.serverSessionContext
            sslsc.sessionTimeout = 0
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        }
            .getOrElse {
                // if needed
            }
    }

    var debug = false

    fun parse(uri: String) =
        runCatching {
            when (uri.substringBefore(':')) {
                "vmess" -> parseV2ray(uri.trim())
                "ss" -> parseSs(uri.trim())
                "ssr" -> parseSsr(uri.trim())
                "trojan" -> parseTrojan(uri.trim())
                "vless" -> parseVless(uri.trim())
                "hysteria2" -> parseHysteria2(uri.trim())
                else -> NoSub
            }
        }
            .getOrDefault(NoSub)

    fun parseV2ray(uri: String): Sub =
        runCatching {
            "parseV2ray ".debug(uri)
            REG_SCHEMA_HASH.matchEntire(uri)?.run {
                groupValues[2]
                    .b64SafeDecode()
                    .also { "parseV2ray base64 decode: ".debug(it) }
                    .fromJson<V2ray>()
                    .takeIf { it.id.length == UUID_LENGTH && !it.add.contains("baidu.com") }
            }
                ?: NoSub
        }
            .getOrElse {
                "parseV2ray err".debug(uri)
                println("parseV2ray error ${it.stackTrace}")
                NoSub
            }

    fun parseSs(uri: String): Sub {
        "parseSs ".debug(uri)
        REG_SCHEMA_HASH.matchEntire(uri)?.run {
            val remark = groupValues[3].urlDecode()
            "parseSs match".debug(remark)
            "parseSs match".debug(groupValues[2])
            val decoded =
                groupValues[2].takeUnless { it.contains("@") }?.b64Decode()
                // 兼容异常
                    ?: with(groupValues[2]) {
                        "${substringBefore('@').b64Decode()}${substring(indexOf('@'))}".also {
                            "parseSs b64 format correct".debug("___$it")
                        }
                    }
            decoded.also {
                "parseSs b64 decode".debug(it)
                REG_SS.matchEntire(it)?.run {
                    "parseSs ss match".debug(this.groupValues.toString())
                    return SS(groupValues[1], groupValues[2], groupValues[3], groupValues[4])
                        .apply { this.remark = remark }
                }
            }
        }
        "parseSs failed".debug(uri)
        return NoSub
    }

    fun parseSsr(uri: String): Sub {
        "parseSsr ".debug(uri)
        return runCatching {
            REG_SCHEMA_HASH.matchEntire(uri)?.run {
                groupValues[2].b64SafeDecode().split(":").run {
                    "parseSsr query".debug(this[5])
                    REG_SSR_PARAM.matchEntire(this[5])?.let {
                        "parseSsr query match".debug(it.groupValues[2])
                        val q = it.groupValues[2].queryParamMapB64()
                        "parseSsr query maps".debug(q.toString())
                        return SSR(
                            this[0],
                            this[1],
                            this[2],
                            this[3],
                            this[4],
                            it.groupValues[1].b64SafeDecode(),
                            q["obfsparam"].orEmpty(),
                            q["protoparam"].orEmpty(),
                        )
                            .apply {
                                remarks = q["remarks"].orEmpty()
                                group = q["group"].orEmpty()
                            }
                    }
                }
            }
                ?: NoSub
        }
            .getOrElse {
                "parseSsr err not match".debug(uri)
                NoSub
            }
    }

    fun parseTrojan(uri: String): Sub {
        "parseTrojan".debug(uri)
        REG_SCHEMA_HASH.matchEntire(uri)?.run {
            val remark = groupValues[3].urlDecode()
            "parseTrojan remark".debug(remark)
            groupValues[2].also {
                "parseTrojan data".debug(it)
                REG_TROJAN.matchEntire(it)?.run {
                    return Trojan(groupValues[1], groupValues[2], groupValues[3]).apply {
                        this.remark = remark
                        query = groupValues[4]
                    }
                }
            }
        }
        "parseTrojan ".debug("failed")
        return NoSub
    }

    fun parseVless(uri: String): Sub {
        "parseVless".debug(uri)
        REG_SCHEMA_HASH.matchEntire(uri)?.run {
            val remark = groupValues[3].urlDecode()
            "parseVless remark".debug(remark)
            groupValues[2].also {
                "parseVless data".debug(it)
                REG_TROJAN.matchEntire(it)?.run {
                    return Vless(groupValues[1], groupValues[2], groupValues[3]).apply {
                        this.remark = remark
                        query = groupValues[4]
                    }
                }
            }
        }
        "parseVless ".debug("failed")
        return NoSub
    }

    fun parseHysteria2(uri: String): Sub {
        "parseHysteria2".debug(uri)
        REG_SCHEMA_HASH.matchEntire(uri)?.run {
            val remark = groupValues[3].urlDecode()
            "parseHysteria2 remark".debug(remark)
            groupValues[2].also {
                "parseHysteria2 data".debug(it)
                REG_TROJAN.matchEntire(it)?.run {
                    return Hysteria2(groupValues[1], groupValues[2], groupValues[3]).apply {
                        this.remark = remark
                        query = groupValues[4]
                    }
                }
            }
        }
        "parseHysteria2 ".debug("failed")
        return NoSub
    }

    private fun parseFromFileSub(path: String): LinkedHashSet<Sub> {
        "parseFromSub Local".debug(path)
        val data = path.readText().b64SafeDecode()
        return if (data.contains("proxies:")) {
            data.parseYaml<Clash>().proxies.asSequence().map(Node::toNode).fold(linkedSetOf()) { acc,
                                                                                                 sub ->
                acc.also { acc.add(sub) }
            }
        } else {
            data
                //                .also { println(it) }
                .split("\r\n|\n".toRegex())
                .asSequence()
                .filter { it.isNotEmpty() }
                .map { it to parse(it) }
                .filterNot { it.second is NoSub }
                .fold(linkedSetOf()) { acc, sub ->
                    acc.add(sub.second)
                    acc
                }
        }
    }

    private fun parseFromNetwork(url: String): LinkedHashSet<Sub> {
        "parseFromNetwork".debug(url)
        println(url)
        val data = url.readFromNet().b64SafeDecode()
        return runCatching {
            if (data.contains("proxies:"))
            // 移除yaml中的标签
            {
                data
                    .replace("\\[[^\"]+?]".toRegex(), "")
                    .parseYaml<Clash>()
                    .proxies
                    .asSequence()
                    .map(Node::toNode)
                    .filterNot { it is NoSub }
                    .fold(linkedSetOf<Sub>()) { acc, sub -> acc.also { acc.add(sub) } }
            } else {
                data
                    .also { "parseFromNetwork".debug(it) }
                    .split("\r\n|\n".toRegex())
                    .asSequence()
                    .filter { it.isNotEmpty() && it.contains("://") }
                    .map {
                        println(it)
                        parse(it.replace("/#", "#").trim())
                    }
                    .filterNot { it is NoSub }
                    .fold(linkedSetOf()) { acc, sub -> acc.also { acc.add(sub) } }
            }
        }
            .getOrElse {
                it.printStackTrace()
                println("failed______ $url ${it.message}")
                linkedSetOf()
            }
    }

    fun parseFromSub(uri: String) =
        when {
            uri.startsWith("http") -> parseFromNetwork(uri)
            uri.startsWith("/") -> parseFromFileSub(uri)
            "^[A-Za-z]:".toRegex().find(uri) != null -> parseFromFileSub(uri)
            else -> linkedSetOf()
        }

    fun String.debug(extra: String = "") {
        if (debug) println("$this $extra")
    }
}
