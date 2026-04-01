package com.example.demo

import com.example.demo.jooq.tables.references.BOOK
import com.example.demo.jooq.tables.references.BOOK_AUTHOR
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(private val dsl: DSLContext) {

    /**
     * 書籍の新規登録
     * @return 登録された書籍のID
     */
    @Transactional
    fun registerBook(title: String, price: Int, authorIds: List<Int>): Int {
        if (price < 0) throw IllegalArgumentException("価格がマイナスです")
        if (authorIds.isEmpty()) throw IllegalArgumentException("著者が指定されていません")

        // 1. 本の登録
        val bookId = dsl.insertInto(BOOK)
            .set(BOOK.TITLE, title)
            .set(BOOK.PRICE, price)
            .set(BOOK.STATUS, BookStatus.UNPUBLISHED.code) // 未出版
            .returning(BOOK.ID)
            .fetchOne()?.id ?: throw IllegalStateException("登録に失敗しました")

        // 2. 著者との紐付け（ここが重要！）
        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHOR)
                .set(BOOK_AUTHOR.BOOK_ID, bookId)
                .set(BOOK_AUTHOR.AUTHOR_ID, authorId)
                .execute()
        }

        return bookId
    }

    /**
     * 書籍のステータス更新
     */
    @Transactional
    fun updateBookStatus(bookId: Int, newStatus: Int) {
        // 1. 現在のステータスを確認
        val currentStatus = BookStatus.fromCode(
            dsl.select(BOOK.STATUS)
                .from(BOOK)
                .where(BOOK.ID.eq(bookId))
                .fetchOne()?.value1()
                ?: throw IllegalArgumentException("指定されたID（${bookId}）の書籍は見つかりません")
        )

        // 要件：出版済み(1)から未出版(0)に戻すことはできない
        if (currentStatus == BookStatus.PUBLISHED && newStatus == BookStatus.UNPUBLISHED.code) {
            throw IllegalStateException("一度出版した書籍を未出版に戻すことはできません")
        }

        // 2. ステータス更新
        dsl.update(BOOK)
            .set(BOOK.STATUS, newStatus)
            .where(BOOK.ID.eq(bookId))
            .execute()
    }

    @Transactional
    fun updateBook(bookId: Int, title: String, price: Int, authorIds: List<Int>) {
        if (price < 0) throw IllegalArgumentException("価格がマイナスです")
        if (authorIds.isEmpty()) throw IllegalArgumentException("著者が指定されていません")

        // 1. 書籍が存在するかチェック
        val exists = dsl.fetchExists(
            dsl.selectFrom(BOOK).where(BOOK.ID.eq(bookId))
        )
        if (!exists) throw IllegalArgumentException("指定されたID（$bookId）の書籍は存在しません")

        // 2. 書籍情報を更新
        dsl.update(BOOK)
            .set(BOOK.TITLE, title)
            .set(BOOK.PRICE, price)
            .where(BOOK.ID.eq(bookId))
            .execute()

        // 3. 既存の著者紐付けを削除
        dsl.deleteFrom(BOOK_AUTHOR)
            .where(BOOK_AUTHOR.BOOK_ID.eq(bookId))
            .execute()

        // 4. 新しい著者紐付けを追加
        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHOR)
                .set(BOOK_AUTHOR.BOOK_ID, bookId)
                .set(BOOK_AUTHOR.AUTHOR_ID, authorId)
                .execute()
        }
    }

    /**
     * 著者IDによる書籍検索
     */
    fun findBooksByAuthor(authorId: Int): List<BookQueryResult> =
        dsl.select(
            BOOK.ID,
            BOOK.TITLE,
            BOOK.PRICE,
            BOOK.STATUS
        )
            .from(BOOK)
            .join(BOOK_AUTHOR).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
            .where(BOOK_AUTHOR.AUTHOR_ID.eq(authorId))
            .fetch { r ->
                BookQueryResult(
                    id = requireNotNull(r.get(BOOK.ID)),
                    title = requireNotNull(r.get(BOOK.TITLE)),
                    price = requireNotNull(r.get(BOOK.PRICE)),
                    status = BookStatus.fromCode(requireNotNull(r.get(BOOK.STATUS)))
                )
            }

}

/**
 * 検索結果を受け取るためのデータクラス
 */
data class BookQueryResult(
    val id: Int,
    val title: String,
    val price: Int,
    val status: BookStatus
)