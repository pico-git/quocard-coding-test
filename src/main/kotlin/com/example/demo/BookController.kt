package com.example.demo

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(private val bookService: BookService) {

    // 著者に紐づく本を取得
    @GetMapping("/by-author/{authorId}")
    fun getByAuthor(@PathVariable authorId: Int) =
        bookService.findBooksByAuthor(authorId)

    // 書籍の登録
    @PostMapping
    fun add(@RequestBody req: CreateBookRequest): Int =
        bookService.registerBook(req.title, req.price, req.authorIds)

    // 書籍の更新
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody req: UpdateBookRequest
    ) {
        bookService.updateBook(id, req.title, req.price, req.authorIds)
    }

    // ステータス更新
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Int,
        @RequestParam status: Int
    ) {
        bookService.updateBookStatus(id, status)
    }
}

data class CreateBookRequest(
    val title: String,
    val price: Int,
    val authorIds: List<Int>
)

data class UpdateBookRequest(
    val title: String,
    val price: Int,
    val authorIds: List<Int>
)
