package me.leon

/** Clash完整配置 https://github.com/Dreamacro/clash/wiki/configuration */
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
    var hosts: List<LinkedHashMap<String, String>> = mutableListOf()
    var ipv6: Boolean = false
    var `cfw-bypass`: List<String> = mutableListOf()
    var rule: List<String> = mutableListOf()
}

data class DNS(
    var enable: Boolean = false,
    var ipv6: Boolean = false,
    var listen: String = "",
    var `enhanced-mode`: String = "",
    var `fake-ip-range`: String = "",
    var nameserver: List<String> = mutableListOf(),
    var `default-nameserver`: List<String> = mutableListOf(),
    var fallback: List<String> = mutableListOf(),
    var `fallback-filter`: LinkedHashMap<String, String> = linkedMapOf()
)

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
    var `http-opts`: LinkedHashMap<String, String> = linkedMapOf()
    var `h2-opts`: LinkedHashMap<String, String> = linkedMapOf()
    var `plugin-opts`: LinkedHashMap<String, String> = linkedMapOf()
    var `ws-path`: String = ""
    var `obfs-param`: String = ""
    var obfs_param: String = ""
    var plugin: String = ""
    var sni: String = ""
    var udp: Boolean = false
    var tls: Boolean = false
    var _index: Int = 0
    var `skip-cert-verify`: Boolean = false
    var `protocol_param`: String = ""
    var protocolparam: String = ""
    var obfsparam: String = ""

    fun toNode(): Sub {
        // 兼容某些异常节点池
        if (server == "NULL") return NoSub
        return when (type) {
            "ss" -> toSs()
            "ssr" -> toSsr()
            "vmess" -> toVmess()
            "trojan" -> toTrojan()
            else -> NoSub
        }
    }

    private fun toTrojan() =
        Trojan(password, server, port.toString()).apply {
            this.remark = this@Node.name
            nation = country
        }

    private fun toVmess() =
        V2ray(
                aid = alterId,
                add = server,
                port = port.toString(),
                id = uuid,
                net = network,
                tls = if (tls) "true" else ""
            )
            .apply {
                path = if (network == "ws") `ws-path` else ""
                host = if (network == "ws") `ws-headers`["Host"] ?: "" else ""
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
            `protocol-param` + `protocol_param` + protocolparam
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

data class Group(
    var name: String = "",
    var type: String = "",
    var url: String = "",
    var interval: Int = 0,
    var tolerance: Int = 0,
    var proxies: List<String> = mutableListOf()
)
