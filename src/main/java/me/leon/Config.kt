package me.leon

import java.io.File

val ROOT: String = File("sub").absolutePath
val SHARE = "$ROOT/share"
val HOST = "$SHARE/host"

//        本地节点池
val POOL = "$ROOT/pools"

val NODE_OK = "$SHARE/available"
val NODE_SS = "$SHARE/ss"
val NODE_SSR = "$SHARE/ssr"
val NODE_V2 = "$SHARE/v2"
val NODE_ALL = "$SHARE/a11"
val NODE_TR = "$SHARE/tr"
val NODE_VLESS = "$SHARE/vless"
val NODE_HYS2 = "$SHARE/hysteria2"
val FAIL_IPS = "$ROOT/socketfail"
val IP_SCORE = "$ROOT/ipScore"
