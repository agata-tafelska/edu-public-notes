package com.atafelska.service.notes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@ComponentScan(
    basePackages = ["com.atafelska.service.notes.core"]
)
@PropertySource(
    "classpath:service-notes.properties"
)
class NotesServiceApplication

fun main(args: Array<String>) {
    runApplication<NotesServiceApplication>(*args)
}
