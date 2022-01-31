package com.atafelska.service.notes.core

import com.atafelska.service.notes.generated.Note
import com.atafelska.service.notes.generated.UpsertNoteRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Repository
class NotesRepository(val database: NotesDatabaseConfiguration.NotesDatabase) : Logging {

    companion object {
        fun ResultRow.toNote() = Note(
                id = this[Notes.id].toString(),
                text = this[Notes.text],
                createdAt = OffsetDateTime.ofInstant(this[Notes.createdAt], ZoneOffset.UTC),
                updatedAt = this[Notes.updatedAt]?.let { updatedAtInstant ->
                    OffsetDateTime.ofInstant(updatedAtInstant, ZoneOffset.UTC)
                }
        )
    }

    fun getNotes(): List<Note> =
            transaction(Database.connect(database.dataSource)) {
                logger().debug("getNotes")
                Notes.selectAll().map { it.toNote() }
            }

    fun insertOrUpdatePost(upsertNoteRequest: UpsertNoteRequest): Note = transaction(Database.connect(database.dataSource)) {
        val currentNote = upsertNoteRequest.noteId?.let {
            Notes.select { Notes.id eq UUID.fromString(it) }
        }
                ?.firstOrNull()
                ?.toNote()
        if (currentNote != null) {
            logger().info("Updating note: $upsertNoteRequest")
            val editedNote = Note(
                    id = upsertNoteRequest.noteId,
                    text = upsertNoteRequest.text,
                    createdAt = currentNote.createdAt,
                    updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
            )
            Notes.update({ Notes.id eq UUID.fromString(currentNote.id) }) {
                it[text] = editedNote.text
                it[updatedAt] = editedNote.updatedAt!!.toInstant()
            }
            editedNote
        } else {
            logger().info("Inserting note: $upsertNoteRequest")
            val createdNote = Note(
                    id = UUID.randomUUID().toString(),
                    text = upsertNoteRequest.text,
                    createdAt = OffsetDateTime.now(ZoneOffset.UTC)
            )
            Notes.insert {
                it[id] = UUID.fromString(createdNote.id)
                it[text] = createdNote.text
                it[createdAt] = createdNote.createdAt.toInstant()
            }
            createdNote
        }
    }

    fun deleteNote(noteId: String) = transaction(Database.connect(database.dataSource)) {
        Notes.deleteWhere { Notes.id eq UUID.fromString(noteId) }
    }
}
