package com.example.androidtaskcompose.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.scratch.Blocks
import com.example.scratch.createVariable.numbersMap
import java.util.*
import java.util.Stack

val stackTemp = Stack<Double>()

object GlobalStack {
    val values = Stack<Double>()
}

@Composable
fun opsExpression(viewBlocks: MutableList<Blocks>) {
    var keyTextFieldValue by rememberSaveable { mutableStateOf("") }
    var valueTextFieldValue by rememberSaveable { mutableStateOf("") }
    var savedKey by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(0.0) }
    var buttonColor by remember {
        mutableStateOf(Color(android.graphics.Color.parseColor("#FF4C64")))
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = keyTextFieldValue,
                onValueChange = { newVariable ->
                    if (newVariable.length <= 10) {
                        keyTextFieldValue = newVariable
                    }
                },
                modifier = Modifier.padding(top = 4.dp)
                    .weight(1f),
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color(0xFF333333)
                )
            )
            Text(text = " = ", color = Color.White)
            TextField(
                value = valueTextFieldValue,
                onValueChange = { newValue ->
                    valueTextFieldValue = newValue
                },
                modifier = Modifier.padding(top = 4.dp)
                    .weight(2f),
                maxLines = 1,
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color(0xFF444444)
                )
            )
        }
        Button(
            onClick = {
                if (valueTextFieldValue.isNotBlank()) {
                    numbersMap[keyTextFieldValue] = valueTextFieldValue
                    savedKey = keyTextFieldValue
                    stackTemp.addAll(GlobalStack.values.toList())
                    result = ops(valueTextFieldValue)
                    GlobalStack.values.clear()
                    GlobalStack.values.addAll(stackTemp.toList())
                    stackTemp.clear()
                    println(GlobalStack.values)
                    println(stackTemp)
                    buttonColor = Color.Green
                }
                else{
                    result = 0.0
                    stackTemp.clear()
                    buttonColor = Color(android.graphics.Color.parseColor("#FF4C64"))
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
        ) {
            Text("Result", color = androidx.compose.ui.graphics.Color.White)
        }
        Text(
            text = "$savedKey = $result",
            color = Color.White
        )
    }
}
fun ops(expression: String): Double {
    val operators = Stack<Char>()
    val outputQueue = LinkedList<String>()
    val savedValues = Stack<Double>()

    // Проходим по выражению и формируем очередь выхода
    var i = 0
    while (i < expression.length) {
        val c = expression[i]
        when (c) {
            in '0'..'9', '.' -> {
                // Если текущий символ - цифра или точка, то добавляем ее и все следующие символы числа в очередь выхода
                var number = ""
                while (i < expression.length && (expression[i] in '0'..'9' || expression[i] == '.')) {
                    number += expression[i]
                    i++
                }
                outputQueue.add(number)
                i--
            }
            '(' -> operators.push(c)
            ')' -> {
                while (operators.isNotEmpty() && operators.peek() != '(') {
                    outputQueue.add(operators.pop().toString())
                }
                if (operators.isNotEmpty() && operators.peek() == '(') {
                    operators.pop() // Удаляем открывающую скобку из стека
                } else {
                    throw IllegalArgumentException("Неправильное использование скобок")
                }
            }
            '+', '-', '*', '/', '%' -> {
                while (operators.isNotEmpty() && getPrecedence(c) <= getPrecedence(operators.peek())) {
                    outputQueue.add(operators.pop().toString())
                }
                operators.push(c)
            }
        }
        i++
    }

    // Добавляем оставшиеся операторы из стека в очередь выхода
    while (operators.isNotEmpty()) {
        if (operators.peek() == '(' || operators.peek() == ')') {
            throw IllegalArgumentException("Неправильное использование скобок")
        }
        outputQueue.add(operators.pop().toString())
    }

    // Вычисляем значение выражения из очереди выхода
    for (token in outputQueue) {
        when {
            token.matches("[-+]?\\w+([.]?\\w+)?".toRegex()) -> {
                val variable = token.toDoubleOrNull()
                if (variable == null) {
                    val value = numbersMap[token] ?: throw IllegalArgumentException("Variable $token not found in the numbersMap dictionary")
                } else {
                    GlobalStack.values.push(variable)
                }
            }
            else -> {
                val b = GlobalStack.values.pop()
                val a = if (GlobalStack.values.isNotEmpty()) GlobalStack.values.pop() else 0.0
                val result = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> a / b
                    "%" -> a % b
                    else -> throw IllegalArgumentException("Неподдерживаемый оператор: $token")
                }
                GlobalStack.values.push(result)
            }
        }
    }
    println(GlobalStack.values)
// Возвращаем результат вычисления
    return GlobalStack.values.pop()
}

fun getPrecedence(c: Char): Int {
    return when (c) {
        '+', '-' -> 1
        '*', '/' , '%' -> 2
        else -> 0
    }
}