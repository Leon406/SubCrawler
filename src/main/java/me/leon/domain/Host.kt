package me.leon.domain

data class Host(val domain: String) {
    var ip: String = ""
    override fun toString(): String {
        return "$ip $domain"
    }
}
