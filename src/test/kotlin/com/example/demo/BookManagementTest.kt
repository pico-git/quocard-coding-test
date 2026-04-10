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
import org.springframework.dao.DataAccessException

@SpringBootTest
@Transactional
class BookManagementTest {

    @Autowired
    lateinit var bookService: BookService

    @Autowired
    lateinit var authorService: AuthorService

    @Autowired
    lateinit var dsl: DSLContext


    // --- 著者作成　異常系 ---

    @Test
    fun `著者登録時に名前が空文字だとエラーになること`() {
        assertThrows(IllegalArgumentException::class.java) {
            authorService.registerAuthor("", LocalDate.of(1990, 1, 1))
        }
    }

    @Test
    fun `著者登録時に名前が255文字を超えるとエラーになること`() {
        val longName = "a".repeat(256)

        assertThrows(IllegalArgumentException::class.java) {
            authorService.registerAuthor(longName, LocalDate.of(1990, 1, 1))
        }
    }

    @Test
    fun `著者登録時に未来日の生年月日を指定するとエラーになること`() {
        val future = LocalDate.now().plusDays(1)

        assertThrows(IllegalArgumentException::class.java) {
            authorService.registerAuthor("未来人", future)
        }
    }

    // --- 著者更新　異常系 ---
    @Test
    fun `著者更新時に名前が空文字だとエラーになること`() {
        val authorId = authorService.registerAuthor("名前テスト", LocalDate.of(1990, 1, 1))

        assertThrows(IllegalArgumentException::class.java) {
            authorService.updateAuthor(
                id = authorId,
                name = "",
                birthDate = LocalDate.of(1990, 1, 1)
            )
        }
    }

    @Test
    fun `著者更新時に名前が255文字を超えるとエラーになること`() {
        val authorId = authorService.registerAuthor("正常著者", LocalDate.of(1990, 1, 1))
        val longName = "a".repeat(256)

        assertThrows(IllegalArgumentException::class.java) {
            authorService.updateAuthor(
                id = authorId,
                name = longName,
                birthDate = LocalDate.of(1990, 1, 1)
            )
        }
    }

    @Test
    fun `著者更新時に未来日の生年月日を指定するとエラーになること`() {
        val authorId = authorService.registerAuthor("未来更新テスト", LocalDate.of(1990, 1, 1))
        val future = LocalDate.now().plusDays(1)

        assertThrows(IllegalArgumentException::class.java) {
            authorService.updateAuthor(
                id = authorId,
                name = "未来人",
                birthDate = future
            )
        }
    }

    @Test
    fun `存在しない著者IDを更新しようとすると例外が発生すること`() {
        assertThrows(IllegalArgumentException::class.java) {
            authorService.updateAuthor(
                id = 99999,
                name = "更新名",
                birthDate = LocalDate.of(1990, 1, 1)
            )
        }
    }

    // --- 書籍の登録　異常系 ---
    @Test
    fun `書籍登録時にタイトルが空文字だとエラーになること`() {
        val authorId = authorService.registerAuthor("著者C", LocalDate.of(1990, 1, 1))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook("", 1500, listOf(authorId))
        }
    }

    @Test
    fun `書籍登録時にタイトルが空白のみだとエラーになること`() {
        val authorId = authorService.registerAuthor("著者D", LocalDate.of(1990, 1, 1))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook("   ", 1500, listOf(authorId))
        }
    }

    @Test
    fun `書籍登録時にタイトルが255文字を超えるとエラーになること`() {
        val authorId = authorService.registerAuthor("著者E", LocalDate.of(1990, 1, 1))
        val longTitle = "a".repeat(256)

        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook(longTitle, 1500, listOf(authorId))
        }
    }

    @Test
    fun `書籍登録時に価格が0未満だとエラーになること`() {
        val authorId = authorService.registerAuthor("著者A", LocalDate.of(1990, 1, 1))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook("不正価格本", -100, listOf(authorId))
        }
    }

    @Test
    fun `書籍登録時に著者が0人だとエラーになること`() {
        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook("著者なし本", 1000, emptyList())
        }
    }

    @Test
    fun `書籍登録時に存在しない著者IDが含まれているとエラーになること`() {
        val validAuthor = authorService.registerAuthor("著者B", LocalDate.of(1980, 1, 1))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.registerBook("不正著者本", 1200, listOf(validAuthor, 99999))
        }
    }

    // --- 書籍の更新　異常系 ---
    @Test
    fun `書籍更新時にタイトルが空文字だとエラーになること`() {
        val authorId = authorService.registerAuthor("著者E", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("タイトルテスト", 1000, listOf(authorId))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(bookId, "", 1500, listOf(authorId))
        }
    }

    @Test
    fun `書籍更新時にタイトルが空白のみだとエラーになること`() {
        val authorId = authorService.registerAuthor("著者F", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("空白タイトル", 1000, listOf(authorId))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(bookId, "   ", 1500, listOf(authorId))
        }
    }

    @Test
    fun `書籍更新時にタイトルが255文字を超えるとエラーになること`() {
        val authorId = authorService.registerAuthor("著者G", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("長タイトル本", 1000, listOf(authorId))

        val longTitle = "a".repeat(256)

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(bookId, longTitle, 1500, listOf(authorId))
        }
    }

    @Test
    fun `書籍更新時に価格が0未満だとエラーになること`() {
        val authorId = authorService.registerAuthor("著者B", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("価格テスト", 1000, listOf(authorId))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(bookId, "新タイトル", -500, listOf(authorId))
        }
    }

    @Test
    fun `書籍更新時に著者が0人だとエラーになること`() {
        val authorId = authorService.registerAuthor("著者C", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("著者0テスト", 1000, listOf(authorId))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(bookId, "更新本", 1500, emptyList())
        }
    }

    @Test
    fun `書籍更新時に存在しない著者IDが含まれているとエラーになること`() {
        val a1 = authorService.registerAuthor("著者D", LocalDate.of(1980, 1, 1))
        val bookId = bookService.registerBook("不正著者更新", 1000, listOf(a1))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(bookId, "更新本", 1500, listOf(a1, 99999))
        }
    }

    @Test
    fun `書籍更新時に存在しない書籍IDを更新しようとすると例外が発生すること`() {
        val authorId = authorService.registerAuthor("著者A", LocalDate.of(1990, 1, 1))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBook(
                bookId = 99999,
                title = "更新本",
                price = 1200,
                authorIds = listOf(authorId)
            )
        }
    }

    // --- 書籍の出版状況更新　異常系 ---
    @Test
    fun `存在しない書籍IDのステータス更新は例外が発生すること`() {
        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBookStatus(99999, 1)
        }
    }

    @Test
    fun `ステータスが0と1以外の場合はIllegalArgumentExceptionが発生すること`() {
        val authorId = authorService.registerAuthor("著者X", LocalDate.of(1990, 1, 1))
        val bookId = bookService.registerBook("ステータス不正本", 1000, listOf(authorId))

        assertThrows(IllegalArgumentException::class.java) {
            bookService.updateBookStatus(bookId, 999)
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

    @Test
    fun `書籍更新時に著者を入れ替えられること`() {
        val a1 = authorService.registerAuthor("著者1", LocalDate.of(1970, 1, 1))
        val a2 = authorService.registerAuthor("著者2", LocalDate.of(1980, 1, 1))
        val a3 = authorService.registerAuthor("著者3", LocalDate.of(1990, 1, 1))

        val bookId = bookService.registerBook("入れ替え本", 1500, listOf(a1, a2))

        bookService.updateBook(bookId, "入れ替え本", 1500, listOf(a3))

        val authors = dsl.select(BOOK_AUTHOR.AUTHOR_ID)
            .from(BOOK_AUTHOR)
            .where(BOOK_AUTHOR.BOOK_ID.eq(bookId))
            .fetch()
            .map { it.value1() }

        assertEquals(setOf(a3), authors.toSet())
    }

    @Test
    fun `書籍が存在しない著者を検索した場合は空リストが返ること`() {
        val authorId = authorService.registerAuthor("空著者", LocalDate.of(1990, 1, 1))

        val results = bookService.findBooksByAuthor(authorId)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `存在しない著者IDで検索した場合は空リストが返ること`() {
        val results = bookService.findBooksByAuthor(99999)
        assertTrue(results.isEmpty())
    }
}
