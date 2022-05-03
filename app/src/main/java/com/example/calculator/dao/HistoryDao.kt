package com.example.calculator.dao

import androidx.room.*
import com.example.calculator.model.History

@Dao
interface HistoryDao {

    @Query("select * from history_table") // 쿼리문 -> 전체 리스트 가져오기기
   fun getAll() : List<History>

   // 조건을 가진 결과만 가져온다 1개
   @Query("select * from history_table where result LIKE :result LIMIT 1")
   fun historyResult(result: String): History

   @Insert
   fun insertHistory(history: History)

   // 테이블 전체 삭제
   @Query("delete from history_table")
   fun deleteHistoryAll()

   // 해당 히스토리만 삭제
   @Delete
   fun deleteHistory(history: History)
}