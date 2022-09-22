package me.leon.domain

@Suppress("ConstructorParameterNaming")
data class DnsResolve(
    val Status: Int?,
    val TC: Boolean?,
    val RD: Boolean?,
    val RA: Boolean?,
    val AD: Boolean?,
    val CD: Boolean?,
    val Question: List<Question>?,
    val Answer: List<Answer>?
)

data class Question(val name: String?, val type: Int?)

@Suppress("ConstructorParameterNaming")
data class Answer(val name: String?, val type: Int?, val TTL: Int?, val `data`: String?)
