package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.room.Room
import com.example.calculator.database.CalDatabase

class MainActivity : AppCompatActivity() {

    // 계산 결과 textView lazy
    private val tvResult: TextView by lazy {
        findViewById(R.id.tvResult)
    }

    // 연산식 textView lazy
    private val tvCalculation: TextView by lazy {
        findViewById(R.id.tvCalculation)
    }
    
    private var hasOperator = false // 지금은 연산자를 한번만 사용하도록

    lateinit var db: CalDatabase // 전역

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create시 db 변수에 데이터베이스 빌드해서 할당
        db = Room.databaseBuilder(
            applicationContext,
            CalDatabase::class.java,
            "HistoryDB"
        ).build()
    }
}