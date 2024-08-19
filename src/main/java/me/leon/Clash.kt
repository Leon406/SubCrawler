package me.leon

/** Clash完整配置 https://github.com/Dreamacro/clash/wiki/configuration */
@Suppress("ConstructorParameterNaming")
data class Clash(
    var proxies: List<Node> = mutableListOf(),
)

@Suppress("ConstructorParameterNaming")
data class Node(
    var name: String = "",
    var type: String = "",
    var cipher: String = "",
    var country: String = "",
    var obfs: String = "",
    var password: String = "",
    var port: Int = 0,
    var protocol: String = "",
    var uuid: String = "",
    var alterId: String = "",
    var network: String = "",
    var `protocol-param`: String = "",
    var server: String = "",
    var servername: String = ""
) {
    var `ws-headers`: LinkedHashMap<String, String> = linkedMapOf()
    var `ws-path`: String = ""
    var `ws-opts`: VmessWsOpts = VmessWsOpts()

    var `obfs-param`: String = ""
    var obfs_param: String = ""
    var sni: String = ""
    var tls: Any = Any()

    var `skip-cert-verify`: Boolean = false
    var `protocol_param`: String = ""
    var protocolparam: String = ""
    var protoparam: String = ""
    var obfsparam: String = ""

    data class VmessWsOpts(
        var path: String = "",
        var headers: LinkedHashMap<String, String> = linkedMapOf()
    )

    private fun properPath() = if (network == "ws") `ws-path`.ifEmpty { `ws-opts`.path } else ""

    private fun properHost() =
        if (network == "ws") {
            `ws-headers`["Host"]
                ?: `ws-headers`["host"] ?: `ws-opts`.headers["Host"]
                ?: `ws-opts`.headers["host"].orEmpty()
        } else {
            ""
        }

    fun toNode(): Sub {
        // 兼容某些异常节点池
        if (server == "NULL") return NoSub
        return when (type) {
            "ss" -> toSs()
            "ssr" -> toSsr()
            "vmess" -> if (network == "grpc") NoSub else toVmess()
            "trojan" -> toTrojan()
            else -> NoSub
        }
    }

    private fun toTrojan() =
        Trojan(password, server, port.toString()).apply {
            this.remark = this@Node.name
            query = "allowInsecure=${if (`skip-cert-verify`) 1 else 0}&sni=$sni"
            nation = country
        }

    private fun toVmess() =
        V2ray(
            aid = alterId,
            add = server,
            port = port.toString(),
            id = uuid,
            net = network,
        )
            .apply {
                tls =
                    when (this@Node.tls) {
                        is Boolean -> if (this@Node.tls as Boolean) "true" else ""
                        is Int -> if (this@Node.tls as Int == 1) "true" else ""
                        else -> ""
                    }
                path = properPath()
                host = properHost()
                ps = this@Node.name
                nation = country
            }

    private fun toSsr() =
        SSR(
            server,
            port.toString(),
            protocol,
            cipher,
            obfs,
            password,
            if (obfs == "plain") "" else `obfs-param` + obfs_param + obfsparam,
            `protocol-param` + `protocol_param` + protocolparam + protoparam
        )
            .apply {
                remarks = this@Node.name
                nation = country
            }

    private fun toSs() =
        SS(cipher, password, server, port.toString()).apply {
            remark = this@Node.name
            nation = country
        }
}
