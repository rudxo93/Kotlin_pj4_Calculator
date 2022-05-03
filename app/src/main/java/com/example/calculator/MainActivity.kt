package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.calculator.database.CalDatabase
import com.example.calculator.model.History
import kotlinx.coroutines.Runnable
import org.w3c.dom.Text
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    // 계산 결과 textView lazy
    private val tvResult: TextView by lazy {
        findViewById<TextView>(R.id.tvResult)
    }

    // 연산식 textView lazy
    private val tvCalculation: TextView by lazy {
        findViewById<TextView>(R.id.tvCalculation)
    }

    // history 레이아웃
    private val historyLayout: View by lazy {
        findViewById<View>(R.id.historyLayout)
    }

    // history linear 레이아웃
    private val historyLinearLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
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
            R.id.btnPlus -> operatorBtnClicked("+")
            R.id.btnMinus -> operatorBtnClicked("-")
            R.id.btnMultiply -> operatorBtnClicked("*")
            R.id.btnDivision -> operatorBtnClicked("÷")
            R.id.btnPercent -> operatorBtnClicked("%")
        }
    }

    // number button
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
        tvResult.text = calculateExpression()

    }

    private fun operatorBtnClicked(operator: String) {
        // 연산자가 이미 입력된 경우 무시하기
        if(tvCalculation.text.isEmpty()) { // 연산자가 한개만 입력되게끔
            return
        }

        when{
            isOperator -> { // 연산자를 입력하다 온 경우 연산자 수정
                val text = tvCalculation.text.toString()
                tvCalculation.text = text.dropLast(1) + operator
            }
            hasOperator -> {
                // 지금은 연산자를 딱 한번만 사용할 수 있도록 제한
                Toast.makeText(this, "연산자는 한 번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                tvCalculation.append(" $operator")
            }
        }

        val ssb = SpannableStringBuilder(tvCalculation.text) // SpannableStringBuilder -> text에 style넣기

        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green)), // 연산자는 color를 가져와서 다른색으로 보이게 만들기
            tvCalculation.text.length - 1, // 지금 추가한 연산자 한개만 변경
            tvCalculation.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvCalculation.text = ssb

        isOperator = true
        hasOperator= true
    }

    fun resultBtnClicked(v:View){
        val calculationTexts = tvCalculation.text.split(" ")

        if(tvCalculation.text.isEmpty() || calculationTexts.size == 1) { // 1 -> 숫자만 들어온 경우
            return
        }

        if(calculationTexts.size != 3 && hasOperator) {
            // 숫자와 연산자까지만 입력이 되고 마지막 값이 안온경우
            Toast.makeText(this, "아직 완성되지 않은 계산식입니다.", Toast.LENGTH_SHORT).show()
            return
       }

        if(calculationTexts[0].isNumber().not() || calculationTexts[2].isNumber().not()){
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val calculationText = tvCalculation.text.toString()
        val resultText = calculateExpression()

        // db에 넣어주는 부분
        // DB 입출력과정은 메인스레드 외에 추가 스레드에서 해야한다.
        // 스레드에는 Runnable 구현체가 들어간다.
        Thread(Runnable {
            // uid : null로 주어도 기본키라 자동으로 +1이 되어서 들어간다.
            db.historyDao().insertHistory(History(null, calculationText, resultText))
        }).start()

        tvResult.text = ""
        tvCalculation.text = resultText // 계산 결과값 올린다.

        isOperator = false
        hasOperator = false
    }

    private fun calculateExpression(): String {
        // tvCalculation에서 가져온 내용으로 계산한 결과를 반환한다.
        val calculateTexts = tvCalculation.text.split(" ")

        if(hasOperator.not() || calculateTexts.size != 3) {
            return ""
        } else if(calculateTexts[0].isNumber().not() || calculateTexts[2].isNumber().not()){
            return ""
        }

        val cal1 = calculateTexts[0].toBigInteger()
        val cal2 = calculateTexts[2].toBigInteger()
        val op = calculateTexts[1]

        return when(op) {
            "+" ->  (cal1 + cal2).toString()
            "-" ->  (cal1 - cal2).toString()
            "*" ->  (cal1 * cal2).toString()
            "÷" ->  (cal1 / cal2).toString()
            "%" ->  (cal1 % cal2).toString()
            else -> ""
        }
    }

    // history 클릭 버튼
    fun historyBtnClicked(v: View) {
        historyLayout.isVisible = true

        historyLinearLayout.removeAllViews() // 리니어 레이아웃 하위에 있는 모든 뷰 삭제

        // 디비에서 모든 기록 가져오기
        // 뷰에 모든 기록 할당
        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {
                // 뷰 생성해서 넣어주기
                // 레이아웃 인플레이터 기능 사용해보기
                // ui 스레드 열기
                runOnUiThread {
                    // 핸들러에 포스팅될 내용 작성
                    // R.layout.history_row에서 인플레이트 시킨다. root랑 attachToRoot. 나중에 addview를 통해 붙일거기 때문에 null, false
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.tvCalculation).text = it.expression
                    historyView.findViewById<TextView>(R.id.tvResult).text = "= ${it.result}"

                    historyLinearLayout.addView(historyView) // 뷰 추가
                }
            } // 리스트 뒤집어서 가져오기
        }).start()
    }



    // delete 구현예정
    fun deleteBtnClicked(v: View) {

    }

    // C버튼 클릭 -> clear
    fun clearBtnclicked(v: View) {
        tvCalculation.text = ""
        tvResult.text = ""
        isOperator = false
        hasOperator = false
    }

    // 확장함수 정의
    fun String.isNumber() : Boolean {
        return try {
            this.toBigInteger() // 무한대까지 저장 가능한 자료형
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

}