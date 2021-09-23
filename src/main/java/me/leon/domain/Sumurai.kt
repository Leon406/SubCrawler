package me.leon.domain

data class Sumurai(val code: Int = 0, val `data`: Data = Data(), val msg: String = "")

data class Data(val items: List<Item> = listOf())

data class Item(
    val free: Boolean = false,
    val id: Int = 0,
    val items: List<ItemX> = listOf(),
    val name: String = "",
    val tag: Int = 0
)

data class ItemX(
    val city: String = "",
    val free: Boolean = false,
    val icon: String = "",
    val ovpn: String = ""
)
