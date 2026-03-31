package com.example.demo

import com.example.demo.jooq.tables.references.AUTHOR
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AuthorService(private val dsl: DSLContext) {

    /**
     * 著者の新規登録
     * @return 登録された著者のID
     */
    @Transactional
    fun registerAuthor(name: String, birthDate: LocalDate): Int { // 戻り値の型を Int に指定
        // 要件：生年月日は現在日以前であること
        if (birthDate.isAfter(LocalDate.now())) {
            throw IllegalArgumentException("生年月日に未来の日付は指定できません")
        }

        // returningResult を使って、生成された ID を取得して返却する
        return dsl.insertInto(AUTHOR)
            .set(AUTHOR.NAME, name)
            .set(AUTHOR.BIRTH_DATE, birthDate)
            .returningResult(AUTHOR.ID) // IDを返すよう指定
            .fetchOne()
            ?.value1() ?: throw IllegalStateException("著者のID取得に失敗しました")
    }

    /**
     * 著者の情報更新
     */
    @Transactional
    fun updateAuthor(id: Int, name: String, birthDate: LocalDate) {
        if (birthDate.isAfter(LocalDate.now())) {
            throw IllegalArgumentException("生年月日に未来の日付は指定できません")
        }

        val updated = dsl.update(AUTHOR)
            .set(AUTHOR.NAME, name)
            .set(AUTHOR.BIRTH_DATE, birthDate)
            .where(AUTHOR.ID.eq(id))
            .execute()

        if (updated == 0) {
            throw IllegalArgumentException("指定されたID（${id}）の著者は存在しません")
        }
    }
}