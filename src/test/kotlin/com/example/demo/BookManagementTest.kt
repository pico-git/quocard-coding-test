package com.example.demo

import com.example.demo.jooq.tables.references.BOOK
import com.example.demo.jooq.tables.references.BOOK_AUTHOR
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class BookManagementTest {

    @Autowired
    lateinit var bookService: BookService

    @Autowired
    lateinit var authorService: AuthorService

    @Autowired
    lateinit var dsl: DSLContext

    // --- 書籍の属性・制約テスト ---

    @Test
    fun `価格が0未満の場合はIllegalArgumentExceptionが発生すること`() {
        val authorId = authorService.registerAuthor("テスト著者", LocalDate.of(1990, 1, 1))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook("マイナス価格の本", -1, listOf(authorId))
        }
    }

    @Test
    fun `著者が0人の場合はIllegalArgumentExceptionが発生すること`() {
        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook("著者なし本", 1000, emptyList())
        }
    }

    @Test
    fun `出版済みから未出版に戻そうとするとIllegalStateExceptionが発生すること`() {
        val authorId = authorService.registerAuthor("ステータス著者", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("ステータス本", 1300, listOf(authorId))

        // DB を直接更新して出版済みにする
        dsl.update(BOOK)
            .set(BOOK.STATUS, 1)
            .where(BOOK.ID.eq(bookId))
            .execute()

        assertThrows(IllegalStateException::class.java) {
            bookService.updateBookStatus(bookId, 0)
        }
    }

    // --- 著者の属性・制約テスト ---

    @Test
    fun `未来の生年月日の著者は登録できないこと`() {
        val futureDate = LocalDate.now().plusDays(1)
        assertThrows(IllegalArgumentException::class.java) {
            authorService.registerAuthor("未来人", futureDate)
        }
    }

    // --- 正常系動作確認 ---

    @Test
    fun `正常系：正しいデータで書籍を登録し、著者から検索できること`() {
        val authorId = authorService.registerAuthor("検索テスト著者", LocalDate.of(1980, 5, 9))
        val title = "Kotlinテスト"

        bookService.registerBook(title, 2500, listOf(authorId))

        val results = bookService.findBooksByAuthor(authorId)

        assertTrue(results.any { it.title == title })
    }

    // --- 書籍更新（updateBook）テスト ---

    @Test
    fun `書籍のタイトルと価格を更新できること`() {
        val authorId = authorService.registerAuthor("更新著者", LocalDate.of(1985, 3, 10))
        val bookId = bookService.registerBook("旧タイトル", 1000, listOf(authorId))

        // updateBook を実行
        bookService.updateBook(bookId, "新タイトル", 2000, listOf(authorId))

        val updated = bookService.findBooksByAuthor(authorId).first()

        assertEquals("新タイトル", updated.title)
        assertEquals(2000, updated.price)
    }

    @Test
    fun `書籍更新時に著者が0人だとエラーになること`() {
        val authorId = authorService.registerAuthor("著者A", LocalDate.of(1980, 1, 1))
        val bookId = bookService.registerBook("テスト本", 1200, listOf(authorId))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(bookId, "更新本", 1500, emptyList())
        }
    }

    @Test
    fun `書籍に複数の著者を登録できること`() {
        val a1 = authorService.registerAuthor("著者1", LocalDate.of(1970, 1, 1))
        val a2 = authorService.registerAuthor("著者2", LocalDate.of(1980, 1, 1))

        val bookId = bookService.registerBook("共著本", 1800, listOf(a1, a2))

        val authors = dsl.select(BOOK_AUTHOR.AUTHOR_ID)
            .from(BOOK_AUTHOR)
            .where(BOOK_AUTHOR.BOOK_ID.eq(bookId))
            .fetch()
            .map { it.value1() }

        assertEquals(setOf(a1, a2), authors.toSet())
    }

    @Test
    fun `著者が複数の書籍を持てること`() {
        val authorId = authorService.registerAuthor("多作著者", LocalDate.of(1975, 2, 2))

        val b1 = bookService.registerBook("本1", 1000, listOf(authorId))
        val b2 = bookService.registerBook("本2", 1500, listOf(authorId))

        val books = bookService.findBooksByAuthor(authorId).mapNotNull { it.id }.toSet()

        assertEquals(setOf(b1, b2), books)
    }

    @Test
    fun `未出版から出版済みに正常に変更できること`() {
        val authorId = authorService.registerAuthor("出版著者", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("出版テスト本", 2000, listOf(authorId))

        bookService.updateBookStatus(bookId, 1)

        val status = dsl.select(BOOK.STATUS)
            .from(BOOK)
            .where(BOOK.ID.eq(bookId))
            .fetchOne(BOOK.STATUS)

        assertEquals(1, status)
    }
}
