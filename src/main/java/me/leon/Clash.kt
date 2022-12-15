package me.leon

/** Clash完整配置 https://github.com/Dreamacro/clash/wiki/configuration */
@Suppress("ConstructorParameterNaming")
data class Clash(
    var port: Int = 7890,
    var `socks-port`: Int = 7891,
    var `redir-port`: Int = 0,
    var `allow-lan`: Boolean = false,
    var `log-level`: String = "info",
    var secret: String = "",
    var `external-controller`: String = "info",
    var mode: String = "Rule",
    var `proxy-groups`: List<LinkedHashMap<String, Any>> = mutableListOf(),
    var dns: LinkedHashMap<String, Any> = linkedMapOf(),
    var proxies: List<Node> = mutableListOf(),
    var rules: List<String> = mutableListOf(),
) {
    var `tproxy-port`: Int = 0
    var `mixed-port`: Int = 0
    var `bind-address`: String = ""
    var `interface-name`: String = ""
    var `external-ui`: String = ""
    var authentication: List<String> = mutableListOf()
    var `rule-providers`: LinkedHashMap<String, Any> = linkedMapOf()
    var tun: LinkedHashMap<String, Any> = linkedMapOf()
    var profile: LinkedHashMap<String, Any> = linkedMapOf()
    var hosts: Any = Any()
    var ipv6: Boolean = false
    var `cfw-bypass`: List<String> = mutableListOf()
    var `cfw-latency-timeout`: Int = 0
    var `experimental`: Any = Any()
    var rule: List<String> = mutableListOf()
    var `proxy-providers`: LinkedHashMap<String, Any> = linkedMapOf()
}

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
    var `h2-opts`: LinkedHashMap<String, String> = linkedMapOf()
    var `plugin-opts`: LinkedHashMap<String, String> = linkedMapOf()
    var `ws-path`: String = ""
    var `ws-opts`: VmessWsOpts = VmessWsOpts()

    var `obfs-param`: String = ""
    var obfs_param: String = ""
    var plugin: String = ""
    var sni: String = ""
    var udp: Boolean = false
    var ipv6: Boolean = false
    var tls: Any = Any()
    var `skip-cert-verify`: Boolean = false
    var `protocol_param`: String = ""
    var protocolparam: String = ""
    var protoparam: String = ""
    var obfsparam: String = ""
    var username: String = ""

    // hysteria
    var auth_str: String = ""
    var alpn: String = ""
    var disable_mtu_discovery: Boolean = false

    // http协议
    var `http-opts`: LinkedHashMap<String, Any> = linkedMapOf()

    var `grpc-opts`: LinkedHashMap<String, Any> = linkedMapOf()

    var group: String = ""
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
