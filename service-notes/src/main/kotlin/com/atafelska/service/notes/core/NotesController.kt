package com.atafelska.service.notes.core

import com.atafelska.service.notes.generated.Note
import com.atafelska.service.notes.generated.NotesResponse
import com.atafelska.service.notes.generated.UpsertNoteRequest
import com.atafelska.service.notes.generated.apis.NotesApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class NotesController(
    val notesRepository: NotesRepository,
) : NotesApi, Logging {

    @Autowired
    lateinit var request: HttpServletRequest

    override fun deleteNote(noteId: String): ResponseEntity<Unit> {
        logger().info("deleteNote(): NoteId: $noteId")
        notesRepository.deleteNote(noteId)
        return ResponseEntity.noContent().build()
    }

    override fun getNotes(): ResponseEntity<NotesResponse> =
            ResponseEntity.ok(NotesResponse(notes = notesRepository.getNotes()))

    override fun upsertNote(upsertNoteRequest: UpsertNoteRequest): ResponseEntity<Note> {
        logger().info("upsertNote(): $upsertNoteRequest")
        val upsertedNote = notesRepository.insertOrUpdatePost(upsertNoteRequest)
        return ResponseEntity.ok(upsertedNote)
    }
}
