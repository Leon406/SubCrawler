package me.leon.support

import java.net.InetAddress
import kotlin.jvm.Throws

@Throws fun String.toInetAddress() = InetAddress.getByName(this)
