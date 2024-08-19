package me.leon

import me.leon.support.b64Encode
import me.leon.support.b64EncodeNoEqual
import me.leon.support.toJson
import me.leon.support.urlEncode

interface Uri {

    var name: String
    var nation: String
    val SERVER: String
    val serverPort: Int
    fun toUri(): String
    fun info(): String
}

sealed class Sub : Uri

object NoSub : Sub() {
    override var name: String
        get() = "nosub"
        set(value) {}
    override var nation: String
        get() = "N/A"
        set(value) {}
    override var serverPort = 0
    override val SERVER = "nosub"

    override fun toUri() = "nosub"
    override fun info() = "nosub"
}

data class V2ray(
    /** address 服务器 */
    var add: String = "",
    var port: String = "",
    /** uuid */
    var id: String = "",
    /** alertId */
    var aid: String = "0",
    var scy: String = "auto",
    /** network */
    var net: String = "tcp",
) : Sub() {
    var v: String = "2"
    var ps: String = ""

    /** 伪装域名 */
    var host: String = ""

    /** 伪装路径 */
    var path: String = ""

    /** 默认false,空串即可 */
    var tls: String = ""
        get() = if (field == "tls") "true" else field
    var sni: String = ""

    /** 伪装类型 tcp/kcp/QUIC 默认none */
    var type: String = "none"

    override var name: String
        get() = ps.ifEmpty { "$SERVER:$serverPort-V2-${hashCode()}" }
        set(value) {
            ps = value
        }
    override var serverPort: Int = 0
        get() = runCatching { port.toInt() }.getOrDefault(-1)
    override val SERVER
        get() = add
    override var nation: String = ""

    override fun toUri() = "vmess://${this.toJson().b64Encode()}"
    override fun info() = "$nation $name vmess $add:$port"
}

data class SS(
    val method: String = "",
    val pwd: String = "",
    val server: String = "",
    val port: String = "",
) : Sub() {
    var remark: String = ""
    override var nation: String = ""

    override var name: String
        get() = remark.ifEmpty { "$SERVER:$serverPort-SS-${hashCode()}" }
        set(value) {
            remark = value
        }
    override val serverPort
        get() = runCatching { port.toInt() }.getOrDefault(-1)
    override val SERVER
        get() = server

    override fun toUri() = "ss://${"$method:$pwd@$server:$port".b64Encode()}#${name.urlEncode()}"

    override fun info() = "$nation $remark ss $server:$port"
}

@Suppress("ConstructorParameterNaming")
data class SSR(
    val server: String = "",
    val port: String = "",
    val protocol: String = "",
    val method: String = "",
    val obfs: String = "",
    val password: String = "",
    val obfs_param: String = "",
    val protocol_param: String = "",
) : Sub() {
    var remarks: String = ""
    var group: String = ""

    override var name: String
        get() = remarks.ifEmpty { "$SERVER:$serverPort-SSR-${hashCode()}" }
        set(value) {
            remarks = value
        }
    override val serverPort
        get() = runCatching { port.toInt() }.getOrDefault(-1)
    override val SERVER
        get() = server
    override var nation: String = ""

    @Suppress("TrimMultilineRawString")
    override fun toUri() =
        "ssr://${
            ("$server:$port:$protocol:$method:$obfs:${password.b64Encode()}" +
                    "/?obfsparam=${obfs_param.b64EncodeNoEqual()}" +
                    "&protoparam=${protocol_param.b64EncodeNoEqual()}" +
                    "&remarks=${name.b64EncodeNoEqual()}" +
                    "&group=${group.b64EncodeNoEqual()}")
                .b64Encode()
        }"

    override fun info() = "$nation $remarks ssr $server:$port"
}

data class Trojan(val password: String = "", val server: String = "", val port: String = "") :
    Sub() {
    var remark: String = ""
    var query: String = ""
    override var name: String
        get() = remark.ifEmpty { "$SERVER:$serverPort-TR-${hashCode()}" }
        set(value) {
            remark = value
        }
    private val params
        get() = if (query.isEmpty()) "" else "?$query"
    override val serverPort
        get() = runCatching { port.toInt() }.getOrDefault(-1)
    override val SERVER
        get() = server
    override var nation: String = ""

    override fun toUri() = "trojan://${"$password@$server:$port$params"}#${name.urlEncode()}"
    override fun info() =
        if (query.isEmpty()) {
            "$nation $name trojan $server:$port"
        } else {
            "$nation $remark trojan $server:$port?$query"
        }
}

/** refer https://github.com/XTLS/Xray-core/issues/91 */
data class Vless(val uuid: String = "", val server: String = "", val port: String = "") : Sub() {
    var remark: String = ""
    var query: String = ""
    override var name: String
        get() = remark.ifEmpty { "$SERVER:$serverPort-VL-${hashCode()}" }
        set(value) {
            remark = value
        }
    private val params
        get() = if (query.isEmpty()) "" else "?$query"
    override val serverPort
        get() = runCatching { port.toInt() }.getOrDefault(-1)
    override val SERVER
        get() = server
    override var nation: String = ""

    override fun toUri() =
        "vless://${"${uuid.urlEncode()}@$server:$port$params"}#${name.urlEncode()}"

    override fun info() =
        if (query.isEmpty()) {
            "$nation $name vless $server:$port"
        } else {
            "$nation $remark vless $server:$port?$query"
        }
}

data class Hysteria2(val uuid: String = "", val server: String = "", val port: String = "") : Sub() {
    var remark: String = ""
    var query: String = ""
    override var name: String
        get() = remark.ifEmpty { "$SERVER:$serverPort-hys2-${hashCode()}" }
        set(value) {
            remark = value
        }
    private val params
        get() = if (query.isEmpty()) "" else "?$query"
    override val serverPort
        get() = runCatching { port.toInt() }.getOrDefault(-1)
    override val SERVER
        get() = server
    override var nation: String = ""

    override fun toUri() =
        "hysteria2://${"${uuid.urlEncode()}@$server:$port$params"}#${name.urlEncode()}"

    override fun info() =
        if (query.isEmpty()) {
            "$nation $name vless $server:$port"
        } else {
            "$nation $remark vless $server:$port?$query"
        }
}

fun Sub.methodUnSupported() =
    this is SSR && (method in SSR_unSupportMethod || protocol in SSR_unSupportProtocol) ||
            this is SS && method in SS_unSupportCipher ||
            this is V2ray && net in VMESS_unSupportProtocol

val SSR_unSupportMethod = arrayOf("none", "rc4", "rc4-md5")
val SSR_unSupportProtocol = arrayOf("auth_chain_a")
val SS_unSupportCipher = arrayOf("rc4-md5", "aes-128-cfb", "aes-256-cfb", "none")
val VMESS_unSupportProtocol = arrayOf("none", "grpc", "h2", "auto")
