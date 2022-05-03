package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.example.calculator.database.CalDatabase

class MainActivity : AppCompatActivity() {

    // 계산 결과 textView lazy
    private val tvResult: TextView by lazy {
        findViewById<TextView>(R.id.tvResult)
    }

    // 연산식 textView lazy
    private val tvCalculation: TextView by lazy {
        findViewById<TextView>(R.id.tvCalculation)
    }

    private var isOperator = false // 오퍼레이터를 입력하다가 왔는지?
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

    // 0~9, 연산식 클릭
    fun buttonClicked(v: View) {
        when(v.id) {
            R.id.btn0 -> numberBtnclicked("0")
            R.id.btn1 -> numberBtnclicked("1")
            R.id.btn2 -> numberBtnclicked("2")
            R.id.btn3 -> numberBtnclicked("3")
            R.id.btn4 -> numberBtnclicked("4")
            R.id.btn5 -> numberBtnclicked("5")
            R.id.btn6 -> numberBtnclicked("6")
            R.id.btn7 -> numberBtnclicked("7")
            R.id.btn8 -> numberBtnclicked("8")
            R.id.btn9 -> numberBtnclicked("9")
            R.id.btnPlus -> operatorBtnclicked("+")
            R.id.btnMinus -> operatorBtnclicked("-")
            R.id.btnMultiply -> operatorBtnclicked("*")
            R.id.btnDivision -> operatorBtnclicked("÷")
            R.id.btnPercent -> operatorBtnclicked("%")
        }
    }

    private fun numberBtnclicked(num: String) {
        if(isOperator){
            tvCalculation.append(" ")
        }

        isOperator = false

        val calculationText = tvCalculation.text.split(" ")

        if(calculationText.isNotEmpty() && calculationText.last().length >= 16){ // 16자리 이상이거나 비어있지 않을떄
            Toast.makeText(this, "최대 16자리까지만 사용할 수 있습니다!", Toast.LENGTH_SHORT).show()
            return
        } else if(calculationText.last().isEmpty() && num == "0") { // 비었을때 또는 0이 왔다면
            Toast.makeText(this, "0은 맨 앞에 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        tvCalculation.append(num) // 연산식 textView에 number append


    }

    private fun operatorBtnclicked(operator: String) {
        
    }

}