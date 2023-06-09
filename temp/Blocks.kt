package com.example.scratch
import androidx.compose.runtime.remember
import AppBar
import Block
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.Animatable
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidtaskcompose.ui.theme.GlobalStack
import com.example.androidtaskcompose.ui.theme.opsExpression
import com.example.scratch.printBlock.printBlock
import kotlinx.coroutines.launch
import java.util.Stack
import com.example.scratch.conditions.conditions
import com.example.scratch.createVariable.numbersMap
import com.example.scratch.createVariable.textFieldWithMapValue
import com.example.scratch.printBlock.variableForView
import drawerBody
import drawerHeader
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt
import kotlin.math.sign

// https://developersbreach.com/drag-to-reorder-compose/

@Preview(showBackground = true)
@Composable
fun mainDisplay() {
    Box(
        modifier = Modifier
            .background(Color(android.graphics.Color.parseColor("#0E1621")))
            .fillMaxSize()
    ) {
        navigationPanel()
    }
}

enum class SlideState {
    NONE,
    UP,
    DOWN
}

data class Blocks(
    val blockID: Int,
    val color: String,
    val blockFunction: @Composable () -> Unit
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun navigationPanel() {
    var iDOfBlock = 0
    var tempVariableForView = ""
    val viewBlocks = remember { mutableStateListOf<Blocks>() }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val isExpanded = remember { mutableStateOf(false) }

    val slideStates = remember {
        mutableStateMapOf<Blocks, SlideState>().apply {
            viewBlocks.map { blockList ->
                blockList to SlideState.NONE
            }.toMap().also {
                putAll(it)
            }
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppBar(
                onNavigationIconClick = {
                    scope.launch {
                        scaffoldState.drawerState.open()
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                modifier = Modifier
                    .height(50.dp),
                backgroundColor = (Color(android.graphics.Color.parseColor("#0E1621"))),
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "OpenMenu") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Play") },
                    selected = true,
                    onClick = {
                        tempVariableForView = variableForView
                        variableForView = ""
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = "Console") },
                    selected = false,
                    onClick = { isExpanded.value = !isExpanded.value }
                )
            }
        },
        drawerContent = {
            drawerHeader()
            drawerBody(
                items = listOf(
                    Block(
                        id = "createVariable",
                        title = "Create new variable",
                        contentDescription = "CreatingVariable",
                        icon = Icons.Default.AddCircle,
                        boxColor = "#FF7F50"
                    ),
                    Block(
                        id = "mathOperation",
                        title = "Math operation",
                        contentDescription = "mathBlocks",
                        icon = Icons.Default.AddCircle,
                        boxColor = "#FF4C64"
                    ),
                    Block(
                        id = "createPrint",
                        title = "Create print block",
                        contentDescription = "creatingPrint",
                        icon = Icons.Default.AddCircle,
                        boxColor = "#EC2EFF"
                    ),
                    Block(
                        id = "createConditions",
                        title = "Create condition block",
                        contentDescription = "creatingCondition",
                        icon = Icons.Default.AddCircle,
                        boxColor = "#60ff60"
                    ),
                ),
                onItemClick = {
                    if (it.id == "createVariable") {
                        viewBlocks.add(
                            Blocks(
                                blockID = iDOfBlock++,
                                color = "#FF7F50",
                                blockFunction = { textFieldWithMapValue(viewBlocks) }
                            )
                        )
                    } else if (it.id == "mathOperation") {
                        viewBlocks.add(
                            Blocks(
                                blockID = iDOfBlock++,
                                color = "#FF4C64",
                                blockFunction = { opsExpression(viewBlocks) }
                            )
                        )
                    } else if (it.id == "createPrint") {
                        viewBlocks.add(
                            Blocks(
                                blockID = iDOfBlock++,
                                color = "#EC2EFF",
                                blockFunction = { printBlock(viewBlocks) }
                            )
                        )
                    } else if (it.id == "createConditions") {
                        viewBlocks.add(
                            Blocks(
                                blockID = iDOfBlock++,
                                color = "#60ff60",
                                blockFunction = { conditions(viewBlocks) }
                            )
                        )
                    }
                }
            )
        },
        contentColor = (Color(android.graphics.Color.parseColor("#FFFFFF"))),
        drawerBackgroundColor = (Color.Black.copy(alpha = 0.4f)),
        backgroundColor = (Color(android.graphics.Color.parseColor("#17212B"))),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            blocksOnMainScreen(
                viewBlocks = viewBlocks,
                slideStates,
                updateSlideState = { blockList, slideState -> slideStates[blockList] = slideState },
                updateItemPosition = { currentIndex, destinationIndex ->
                    val blockList = viewBlocks[currentIndex]
                    viewBlocks.removeAt(currentIndex)
                    viewBlocks.add(destinationIndex, blockList)
                    slideStates.apply {
                        viewBlocks.map { blockList -> blockList to SlideState.NONE }.toMap().also {
                            putAll(it)
                        }
                    }
                }
            ) //Функция для отображения всех блоков на главном экране
        }
        console(
            tempVariableForView,
            isExpanded
        ) //Функция для отображения консоли на экране (кнопка снизу справа)
    }
}

@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun blocksOnMainScreen(
    viewBlocks: MutableList<Blocks>,
    slideStateMap: Map<Blocks, SlideState>,
    updateSlideState: (blockList: Blocks, slideState: SlideState) -> Unit,
    updateItemPosition: (currentIndex: Int, destinationIndex: Int) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val isDragged = remember { mutableStateOf(false) }
    val zIndex = if (isDragged.value) 1.0f else 0.0f

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = viewBlocks,
            key = { index, item ->
                item.hashCode()
            }
        ) { index, item ->
            val slideState = slideStateMap[item] ?: SlideState.NONE
            val block = viewBlocks.getOrNull(index)
            val state = rememberDismissState(
                confirmStateChange = {
                    if (it == DismissValue.DismissedToStart && GlobalStack.values.isNotEmpty()) {
                        val keyList = numbersMap.keys.toList()
                        val keyToRemove = keyList[index]
                        numbersMap.remove(keyToRemove)
                        val tempStack = Stack<Double>()
                        while (!GlobalStack.values.empty()) {
                            val value = GlobalStack.values.pop()
                            if (GlobalStack.values.size != index) {
                                tempStack.push(value)
                            }
                        }
                        while (!tempStack.empty()) {
                            GlobalStack.values.push(tempStack.pop())
                        }
                        viewBlocks.remove(item)
                    } else {
                        viewBlocks.remove(item)
                    }
                    true
                }
            )
            SwipeToDismiss(
                state = state,
                background = {
                    val color = when (state.dismissDirection) {
                        DismissDirection.StartToEnd -> Color.Transparent
                        DismissDirection.EndToStart -> Color.Red
                        null -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .zIndex(zIndex)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                },
                dismissContent = {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp)
                        .background(
                            color = (Color(android.graphics.Color.parseColor(item.color))),
                            shape = RoundedCornerShape(8.dp)
                        )
                    ) {
                        if (block != null) {
                            key(block) {
                                draggableVariable(
                                    block = block,
                                    slideState = slideState,
                                    blocksList = viewBlocks,
                                    updateSlideState = updateSlideState,
                                    updateItemPosition = updateItemPosition
                                )
                            }
                        }
                    }
                },
                directions = setOf(DismissDirection.EndToStart)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private var itemHeight = 0
private var slotItemDifference = 0f

@Composable
fun draggableVariable(
    block: Blocks,
    slideState: SlideState,
    blocksList: MutableList<Blocks>,
    updateSlideState: (blockList: Blocks, slideState: SlideState) -> Unit,
    updateItemPosition: (currentIndex: Int, destinationIndex: Int) -> Unit
){
    val itemHeightDp = 170.dp
    with(LocalDensity.current) {
        itemHeight = itemHeightDp.toPx().toInt()
        slotItemDifference = 16.dp.toPx()
    }
    val verticalTranslation by animateIntAsState(
        targetValue = when (slideState) {
            SlideState.UP -> -itemHeight
            SlideState.DOWN -> itemHeight
            else -> 0
        },
    )
    val isDragged = remember { mutableStateOf(false) }
    val zIndex = if (isDragged.value) 1.0f else 0.0f

    val currentIndex = remember { mutableStateOf(0) }
    val destinationIndex = remember { mutableStateOf(0) }

    val isPlaced = remember { mutableStateOf(false) }
    LaunchedEffect(isPlaced.value) {
        if (isPlaced.value) {
            if (currentIndex.value != destinationIndex.value) {
                updateItemPosition(currentIndex.value, destinationIndex.value)
            }
            isPlaced.value = false
        }
    }
    Box(modifier = Modifier
        .dragToReorder(
            block,
            blocksList,
            itemHeight,
            updateSlideState,
            isDraggedAfterLongPress = true,
            { isDragged.value = true },
            { cIndex, dIndex ->
                isDragged.value = false
                isPlaced.value = true
                currentIndex.value = cIndex
                destinationIndex.value = dIndex
            }
        )
        .offset { IntOffset(0, verticalTranslation) }
        .zIndex(zIndex)
        .fillMaxWidth()
        .height(150.dp)
        .shadow(2.dp)
        .background(
            color = (Color(android.graphics.Color.parseColor(block.color))),
            shape = RoundedCornerShape(8.dp)
        )
    ){
        block.blockFunction()
    }
}



fun Modifier.dragToReorder(
    block: Blocks,
    blocksList: MutableList<Blocks>,
    itemHeight: Int,
    updateSlideState: (blockList: Blocks, slideState: SlideState) -> Unit,
    isDraggedAfterLongPress: Boolean,
    onStartDrag: () -> Unit,
    onStopDrag: (currentIndex: Int, destinationIndex: Int) -> Unit,
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    pointerInput(Unit) {
        coroutineScope {
            val blockIndex = blocksList.indexOf(block)
            val offsetToSlide = itemHeight / 4
            var numberOfItems = 0
            var previousNumberOfItems: Int
            var listOffset = 0

            val onDragStart = {
                launch {
                    offsetX.stop()
                    offsetY.stop()
                }
                onStartDrag()
            }
            val onDrag = { change: PointerInputChange ->
                val horizontalDragOffset = offsetX.value + change.positionChange().x
                launch {
                    offsetX.snapTo(horizontalDragOffset)
                }
                val verticalDragOffset = offsetY.value + change.positionChange().y
                launch {
                    offsetY.snapTo(verticalDragOffset)
                    val offsetSign = offsetY.value.sign.toInt()
                    previousNumberOfItems = numberOfItems
                    numberOfItems = calculateNumberOfSlidItems(
                        offsetY.value * offsetSign,
                        itemHeight,
                        offsetToSlide,
                        previousNumberOfItems
                    )
                    Log.i("DragToReorder", "numberOfItems: $numberOfItems")

                    if (previousNumberOfItems > numberOfItems) {
                        Log.i(
                            "DragToReorder",
                            "previousNumberOfItems $previousNumberOfItems > numberOfItems $numberOfItems"
                        )
                        updateSlideState(
                            blocksList[blockIndex + previousNumberOfItems * offsetSign],
                            SlideState.NONE
                        )
                    } else if (numberOfItems != 0) {
                        try {
                            Log.i(
                                "DragToReorder",
                                "Item is inside, numberOfItems: $numberOfItems, offsetSign: $offsetSign, blockIndex: $blockIndex, ind: ${blockIndex + numberOfItems * offsetSign}"
                            )
                            updateSlideState(
                                blocksList[blockIndex + numberOfItems * offsetSign],
                                if (offsetSign == 1)
                                    SlideState.UP
                                else
                                    SlideState.DOWN
                            )
                        } catch (e: IndexOutOfBoundsException) {
                            numberOfItems = previousNumberOfItems
                            Log.i("DragToReorder", "Item is outside or at the edge, numberOfItems: $numberOfItems")
                        }
                    }
                    listOffset = numberOfItems * offsetSign
                }
                // Consume the gesture event, not passed to external
                change.consumePositionChange()
            }
            val onDragEnd = {
                launch {
                    offsetX.animateTo(0f)
                }
                launch {
                    Log.i("LOG", "DragEnd")
                    offsetY.animateTo(itemHeight * numberOfItems * offsetY.value.sign)
                    onStopDrag(blockIndex, blockIndex + listOffset)
                    Log.i("LOG", "DragEnd end")
                }
            }
            if (isDraggedAfterLongPress)
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, _ -> onDrag(change) },
                    onDragEnd = { onDragEnd() }
                ) else
                while (true) {
                    val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                    awaitPointerEventScope {
                        drag(pointerId) { change ->
                            onDragStart()
                            onDrag(change)
                        }
                    }
                    onDragEnd()
                }
        }
    }
        .offset {
            // Use the animating offset value here.
            IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
        }
}


private fun calculateNumberOfSlidItems(
    offsetY: Float,
    itemHeight: Int,
    offsetToSlide: Int,
    previousNumberOfItems: Int
): Int {
    val numberOfItemsInOffset = (offsetY / itemHeight).toInt()
    val numberOfItemsPlusOffset = ((offsetY + offsetToSlide) / itemHeight).toInt()
    val numberOfItemsMinusOffset = ((offsetY - offsetToSlide - 1) / itemHeight).toInt()
    return when {
        offsetY - offsetToSlide - 1 < 0 -> 0
        numberOfItemsPlusOffset > numberOfItemsInOffset -> numberOfItemsPlusOffset
        numberOfItemsMinusOffset < numberOfItemsInOffset -> numberOfItemsInOffset
        else -> previousNumberOfItems
    }
}

@Composable
fun console(tempVariableForView: String, isExpanded: MutableState<Boolean>) {
    if (isExpanded.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.6f))
                .padding(bottom = 55.dp),
            Arrangement.Bottom,
        )
        {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
                    .shadow(2.dp)
                    .background(
                        Color(android.graphics.Color.parseColor("#0E1621")),
                        shape = RoundedCornerShape(8.dp)
                    ),

                Alignment.Center,

                ) {
                LazyRow()
                {
                    item {
                        Text(text = tempVariableForView, fontSize = 30.sp)
                    }
                }
            }
        }
    }
}