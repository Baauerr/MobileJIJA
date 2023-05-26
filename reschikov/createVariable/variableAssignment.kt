package com.example.scratch.createVariable

import Blocks
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.example.scratch.`Math Operations`.process
import com.example.scratch.printBlock.printBlock


val numbersMap = mutableStateMapOf("textFieldValue" to "")
@Composable
fun textFieldWithMapValue(viewBlocks: MutableList<Blocks>, blockID: Int) {
    var keyTextFieldValue by rememberSaveable { mutableStateOf("") }
    var valueTextFieldValue by rememberSaveable { mutableStateOf("") }
    var savedKey by remember { mutableStateOf("") }
    var currentKey by remember { mutableStateOf("") }
    var buttonColor by remember {
        mutableStateOf(Color(android.graphics.Color.parseColor("#FF4C64")))
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = keyTextFieldValue,
                onValueChange = { newVariable ->
                    if (newVariable.length <= 10) {
                        keyTextFieldValue = newVariable
                    }
                },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .weight(1f),
                maxLines = 1,
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color(0xFF333333)
                )
            )
            Text(text = " = ", color = Color.White)
            TextField(
                value = valueTextFieldValue,
                onValueChange = { newValue ->
                    if (newValue.length <= 10) {
                        valueTextFieldValue = newValue
                        viewBlocks[blockID].expression.value = "= $keyTextFieldValue $valueTextFieldValue"

                    }
                },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .weight(2f),
                maxLines = 1,
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color(0xFF444444)
                )
            )
        }
    }
}