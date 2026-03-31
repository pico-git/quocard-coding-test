package com.example.demo

import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/authors")
class AuthorController(private val authorService: AuthorService) {

    @PostMapping
    fun add(@RequestBody req: CreateAuthorRequest): Int =
        authorService.registerAuthor(req.name, req.birthDate)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody req: UpdateAuthorRequest
    ) {
        authorService.updateAuthor(id, req.name, req.birthDate)
    }
}

data class CreateAuthorRequest(
    val name: String,
    val birthDate: LocalDate
)

data class UpdateAuthorRequest(
    val name: String,
    val birthDate: LocalDate
)
