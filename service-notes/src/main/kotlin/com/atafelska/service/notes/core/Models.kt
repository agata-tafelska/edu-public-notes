package com.atafelska.service.notes.core

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Notes : UUIDTable(name = "notes", columnName = "id") {
    val text = text("text")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
}
