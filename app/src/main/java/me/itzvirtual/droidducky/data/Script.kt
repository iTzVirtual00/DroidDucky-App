package me.itzvirtual.droidducky.data

import java.util.UUID

data class Script(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val content: String = ""
)
