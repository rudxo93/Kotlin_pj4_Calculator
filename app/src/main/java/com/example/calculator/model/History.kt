package com.example.calculator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// 데이터 클래스로 선언
@Entity(tableName = "history_table") // room사용 db테이블
class History (
    @PrimaryKey val id: Int?, // 유니크한 아이디
    @ColumnInfo(name = "expression") val expression: String?,
    @ColumnInfo(name = "result") val result: String?
)