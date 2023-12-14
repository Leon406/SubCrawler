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
val NODE_ALL = "$SHARE/all4"
val NODE_TR = "$SHARE/tr"
val NODE_VLESS = "$SHARE/vless"
val FAIL_IPS = "$ROOT/socketfail"
