package me.leon.domain

@Suppress("ConstructorParameterNaming", "NonBooleanPropertyPrefixedWithIs")
data class Quark(
    val area: String = "",
    val country: String = "",
    val `data`: List<Data> = emptyList(),
    val port: Port = Port(),
    val status: Int? = 0
) {
    data class Data(
        val area: String = "",
        val country: String = "",
        val host: String = "",
        val id: String = "",
        val is_full: Int = 0,
        val is_proxy: String = "",
        val is_vip: Int = 0,
        val name: String = "",
        val rate: Double = 0.0,
        val weight: Int = 0
    )

    data class Port(val method: String = "", val password: String = "", val port: String = "")
}
