package com.calculatrice.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var tvExpression: TextView

    private var currentInput = ""
    private var firstOperand = ""
    private var operator = ""
    private var newInput = true
    private var secretCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("activated", false)) {
            startActivity(Intent(this, ActivationActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tv_result)
        tvExpression = findViewById(R.id.tv_expression)

        val digits = mapOf(
            R.id.btn_0 to "0", R.id.btn_1 to "1", R.id.btn_2 to "2",
            R.id.btn_3 to "3", R.id.btn_4 to "4", R.id.btn_5 to "5",
            R.id.btn_6 to "6", R.id.btn_7 to "7", R.id.btn_8 to "8",
            R.id.btn_9 to "9", R.id.btn_dot to "."
        )
        digits.forEach { (id, d) -> findViewById<Button>(id).setOnClickListener { onDigit(d) } }

        findViewById<Button>(R.id.btn_add).setOnClickListener { onOperator("+") }
        findViewById<Button>(R.id.btn_sub).setOnClickListener { onOperator("-") }
        findViewById<Button>(R.id.btn_mul).setOnClickListener { onOperator("×") }
        findViewById<Button>(R.id.btn_div).setOnClickListener { onOperator("÷") }
        findViewById<Button>(R.id.btn_eq).setOnClickListener { onEqual() }
        findViewById<Button>(R.id.btn_clear).setOnClickListener { onClear() }
        findViewById<Button>(R.id.btn_sign).setOnClickListener { onSign() }
        findViewById<Button>(R.id.btn_percent).setOnClickListener { onPercent() }
    }

    private fun onDigit(d: String) {
        if (d == "." && currentInput.contains(".")) return
        if (newInput) {
            currentInput = if (d == ".") "0." else d
            newInput = false
        } else {
            currentInput = if (currentInput == "0" && d != ".") d else currentInput + d
        }

        // Secret code detection: 1919
        if (d != ".") {
            secretCode += d
            if (secretCode.length > 4) secretCode = secretCode.takeLast(4)
            if (secretCode == "1919") {
                secretCode = ""
                startActivity(Intent(this, BrowserActivity::class.java))
                return
            }
        }

        tvResult.text = currentInput
    }

    private fun onOperator(op: String) {
        secretCode = ""
        if (currentInput.isNotEmpty()) firstOperand = currentInput
        operator = op
        tvExpression.text = "$firstOperand $op"
        newInput = true
    }

    private fun onEqual() {
        secretCode = ""
        if (firstOperand.isEmpty() || operator.isEmpty() || currentInput.isEmpty()) return
        val a = firstOperand.toDoubleOrNull() ?: return
        val b = currentInput.toDoubleOrNull() ?: return
        val result = when (operator) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else Double.NaN
            else -> return
        }
        tvExpression.text = "$firstOperand $operator $currentInput ="
        val formatted = if (result == result.toLong().toDouble())
            result.toLong().toString() else result.toString()
        tvResult.text = formatted
        currentInput = formatted
        firstOperand = ""
        operator = ""
        newInput = true
    }

    private fun onClear() {
        secretCode = ""
        currentInput = ""
        firstOperand = ""
        operator = ""
        tvResult.text = "0"
        tvExpression.text = ""
        newInput = true
    }

    private fun onSign() {
        val v = currentInput.toDoubleOrNull() ?: return
        val r = -v
        currentInput = if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
        tvResult.text = currentInput
    }

    private fun onPercent() {
        val v = currentInput.toDoubleOrNull() ?: return
        currentInput = (v / 100).toString()
        tvResult.text = currentInput
    }
    override fun onResume() {
    super.onResume()
    secretCode = ""
    currentInput = ""
    firstOperand = ""
    operator = ""
    tvResult.text = "0"
    tvExpression.text = ""
    newInput = true
}
}
