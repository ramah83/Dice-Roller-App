package com.example.diceroller

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diceroller.ui.theme.DiceRollerTheme

data class RollHistory(
    val diceCount: Int,
    val results: List<Int>,
    val sum: Int,
    val product: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class Statistics(
    val totalRolls: Int = 0,
    val highestSum: Int = 0,
    val lowestSum: Int = Int.MAX_VALUE,
    val averageSum: Double = 0.0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DiceRollerTheme { DiceScreen() } }
    }
}

@Preview
@Composable
fun PreviewDice() { DiceScreen() }

@Composable
fun DiceScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("DiceRollerPrefs", Context.MODE_PRIVATE)
    var diceCount by remember { mutableStateOf(prefs.getInt("diceCount", 2)) }
    var diceResults by remember { mutableStateOf(loadDiceResults(prefs)) }
    var displayResults by remember { mutableStateOf(diceResults) }
    var isRolling by remember { mutableStateOf(false) }
    var rollHistory by remember { mutableStateOf(loadRollHistory(prefs)) }
    var statistics by remember {
        mutableStateOf(Statistics(
            totalRolls = prefs.getInt("totalRolls", 0),
            highestSum = prefs.getInt("highestSum", 0),
            lowestSum = prefs.getInt("lowestSum", Int.MAX_VALUE),
            averageSum = prefs.getFloat("averageSum", 0f).toDouble()
        ))
    }
    var showStats by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("isDarkMode", false)) }
    var totalRollsCount by remember { mutableStateOf(prefs.getInt("totalRolls", 0)) }
    DisposableEffect(diceCount, displayResults, statistics, isDarkMode, totalRollsCount) {
        onDispose {
            prefs.edit().apply {
                putInt("diceCount", diceCount)
                putString("diceResults", displayResults.joinToString(","))
                putInt("totalRolls", totalRollsCount)
                putInt("highestSum", statistics.highestSum)
                putInt("lowestSum", statistics.lowestSum)
                putFloat("averageSum", statistics.averageSum.toFloat())
                putBoolean("isDarkMode", isDarkMode)
                apply()
            }
        }
    }

    val throwAnimation by animateFloatAsState(
        targetValue = if (isRolling) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "throw"
    )

    val rotationAnimation by animateFloatAsState(
        targetValue = if (isRolling) 720f else 0f,
        animationSpec = tween(800, easing = LinearEasing),
        label = "rotation"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isRolling) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    LaunchedEffect(isRolling) {
        if (isRolling) {
            vibratePhone(context)
            
            kotlinx.coroutines.delay(800)
            displayResults = diceResults
            val newRoll = RollHistory(
                diceCount = diceCount,
                results = diceResults,
                sum = diceResults.sum(),
                product = diceResults.fold(1) { a, b -> a * b }
            )
            rollHistory = (listOf(newRoll) + rollHistory).take(10)
            totalRollsCount++
            val currentSum = diceResults.sum()
            statistics = statistics.copy(
                totalRolls = totalRollsCount,
                highestSum = maxOf(statistics.highestSum, currentSum),
                lowestSum = if (statistics.lowestSum == Int.MAX_VALUE) currentSum else minOf(statistics.lowestSum, currentSum),
                averageSum = ((statistics.averageSum * (totalRollsCount - 1)) + currentSum) / totalRollsCount
            )
            
            prefs.edit().apply {
                putInt("diceCount", diceCount)
                putString("diceResults", displayResults.joinToString(","))
                putInt("totalRolls", totalRollsCount)
                putInt("highestSum", statistics.highestSum)
                putInt("lowestSum", statistics.lowestSum)
                putFloat("averageSum", statistics.averageSum.toFloat())
                saveRollHistory(this, rollHistory)
                apply()
            }
            
            isRolling = false
        }
    }
    
    LaunchedEffect(isDarkMode) {
        prefs.edit().putBoolean("isDarkMode", isDarkMode).apply()
    }

    val total = displayResults.sum()
    val product = displayResults.fold(1) { a, b -> a * b }

    val targetDiceSize = when (diceCount) {
        1 -> 180f
        2 -> 140f
        3 -> 120f
        4 -> 105f
        5 -> 95f
        6 -> 88f
        7 -> 82f
        8 -> 78f
        else -> 74f
    }

    val animatedDiceSize by animateDpAsState(
        targetValue = targetDiceSize.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "diceSize"
    )

    val bgColors = if (isDarkMode) {
        listOf(Color(0xFF2C2C2C), Color(0xFF1F1F1F), Color(0xFF0F0F0F))
    } else {
        listOf(Color(0xFFFAF8F4), Color(0xFFF4EFE9), Color(0xFFEFE6DE))
    }
    
    val textColor = if (isDarkMode) Color(0xFFFAF8F4) else Color(0xFF3F3F3F)
    val cardColor = if (isDarkMode) Color(0xFF3F3F3F) else Color.White
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgColors))
    ) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { showStats = !showStats },
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(cardColor, CircleShape)
            ) {
                Text("📊", fontSize = 24.sp)
            }
            
            IconButton(
                onClick = { isDarkMode = !isDarkMode },
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(cardColor, CircleShape)
            ) {
                Text(if (isDarkMode) "☀️" else "🌙", fontSize = 24.sp)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎲", fontSize = 64.sp)
                Text(
                    "DICE ROLLER",
                    fontSize = 34.sp,
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                if (totalRollsCount > 0) {
                    Text(
                        "Total Rolls: $totalRollsCount",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                val glowAlpha by infiniteTransition.animateFloat(
                    0.3f, 0.8f,
                    animationSpec = infiniteRepeatable(
                        tween(1500, easing = FastOutSlowInEasing),
                        RepeatMode.Reverse
                    ),
                    label = "glow"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                12.dp,
                                RoundedCornerShape(24.dp),
                                spotColor = Color(0x40CC0000)
                            )
                            .clickable {
                                shareResult(context, diceCount, total, product)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(cardColor, RoundedCornerShape(24.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFCC0000), Color(0xFFB30000))
                                        ),
                                        RoundedCornerShape(22.dp)
                                    )
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "sum",
                                        color = Color(0xFFFAF8F4).copy(alpha = 0.8f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "$total",
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                12.dp,
                                RoundedCornerShape(24.dp),
                                spotColor = Color(0x40CC0000)
                            )
                            .clickable {
                                shareResult(context, diceCount, total, product)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(cardColor, RoundedCornerShape(24.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFCC0000), Color(0xFFB30000))
                                        ),
                                        RoundedCornerShape(22.dp)
                                    )
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Product",
                                        color = Color(0xFFFAF8F4).copy(alpha = 0.8f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "$product",
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showStats) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "📊 Statistics",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    if (statistics.totalRolls > 0) {
                        StatCard("Total Rolls", statistics.totalRolls.toString(), cardColor, textColor)
                        StatCard("Highest Sum", statistics.highestSum.toString(), cardColor, textColor)
                        StatCard("Lowest Sum", statistics.lowestSum.toString(), cardColor, textColor)
                        StatCard("Average Sum", String.format("%.1f", statistics.averageSum), cardColor, textColor)
                    } else {
                        Text(
                            "No rolls yet! 🎲",
                            fontSize = 18.sp,
                            color = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 40.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val gridColumns = when {
                        diceCount == 1 -> 1
                        diceCount <= 4 -> 2
                        else -> 3
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        itemsIndexed(
                            displayResults,
                            key = { index, _ -> "dice_$index" }
                        ) { index, result ->
                            Dice3D(
                                value = result,
                                scale = scaleAnimation,
                                rotation = rotationAnimation,
                                throwProgress = throwAnimation,
                                index = index,
                                size = animatedDiceSize,
                                isRolling = isRolling
                            )
                        }
                    }
                    
                    if (rollHistory.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                "Recent Rolls",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(rollHistory.take(5)) { roll ->
                                    HistoryCard(roll, cardColor, textColor)
                                }
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(40.dp))
                        .background(cardColor, RoundedCornerShape(40.dp))
                        .padding(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (diceCount > 1 && !isRolling) {
                                diceCount--
                                val newList = List(diceCount) { (1..6).random() }
                                diceResults = newList
                                displayResults = newList
                            }
                        },
                        enabled = !isRolling,
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFCC0000), Color(0xFFB30000))
                                ),
                                CircleShape
                            )
                    ) {
                        Text("−", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "$diceCount Dice",
                        color = textColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (diceCount < 9 && !isRolling) {
                                diceCount++
                                val newList = List(diceCount) { (1..6).random() }
                                diceResults = newList
                                displayResults = newList
                            }
                        },
                        enabled = !isRolling,
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFCC0000), Color(0xFFB30000))
                                ),
                                CircleShape
                            )
                    ) {
                        Text("+", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!isRolling) {
                            isRolling = true
                            diceResults = List(diceCount) { (1..6).random() }
                        }
                    },
                    enabled = !isRolling,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(68.dp)
                        .shadow(
                            12.dp,
                            RoundedCornerShape(50.dp),
                            spotColor = Color(0x50CC0000)
                        ),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFCC0000),
                                        Color(0xFFB30000),
                                        Color(0xFFCC0000)
                                    )
                                ),
                                RoundedCornerShape(50.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "🎲 ROLL DICE 🎲",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Dice3D(
    value: Int,
    scale: Float,
    rotation: Float,
    throwProgress: Float,
    index: Int,
    size: androidx.compose.ui.unit.Dp,
    isRolling: Boolean
) {

    val img = when (value) {
        1 -> R.drawable.p1
        2 -> R.drawable.p2
        3 -> R.drawable.p3
        4 -> R.drawable.p4
        5 -> R.drawable.p5
        else -> R.drawable.p6
    }

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatY by infiniteTransition.animateFloat(
        -4f, 4f,
        animationSpec = infiniteRepeatable(
            tween(1500 + index * 150, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val throwOffsetY = if (isRolling) -150f * throwProgress * (1f - throwProgress) * 4 else 0f
    val throwOffsetX = if (isRolling) ((index % 2) * 2 - 1) * 30f * throwProgress else 0f

    val dicePadding = when {
        size.value > 150f -> 8.dp
        size.value > 100f -> 6.dp
        else -> 4.dp
    }

    Box(
        modifier = Modifier
            .padding(dicePadding)
            .size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    y = (if (isRolling) throwOffsetY else floatY).dp,
                    x = throwOffsetX.dp
                )
                .shadow(
                    if (isRolling) 20.dp else 12.dp,
                    RoundedCornerShape(22.dp),
                    spotColor = if (isRolling) Color(0x80CC0000) else Color(0x40CC0000)
                )
                .background(Color.White, RoundedCornerShape(22.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = img),
                contentDescription = "Dice showing $value",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        rotationZ = if (isRolling) rotation + (index * 120f) else 0f,
                        rotationX = if (isRolling) rotation * 0.5f else 0f,
                        rotationY = if (isRolling) rotation * 0.3f else 0f,
                        scaleX = scale,
                        scaleY = scale,
                        cameraDistance = 12f
                    )
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, cardColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(cardColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCC0000)
        )
    }
}
@Composable
fun HistoryCard(roll: RollHistory, cardColor: Color, textColor: Color) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(cardColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "${roll.diceCount}🎲",
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f)
        )
        Text(
            roll.results.joinToString("+"),
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.6f),
            maxLines = 1
        )
        Text(
            "= ${roll.sum}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCC0000)
        )
    }
}

fun vibratePhone(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(100)
    }
}
fun shareResult(context: Context, diceCount: Int, sum: Int, product: Int) {
    val shareText = "🎲 I rolled $diceCount dice and got:\n" +
            "Sum: $sum\n" +
            "Product: $product\n\n" +
            "Try Dice Roller app!"
    
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    
    val shareIntent = Intent.createChooser(sendIntent, "Share your roll")
    context.startActivity(shareIntent)
}

fun loadDiceResults(prefs: android.content.SharedPreferences): List<Int> {
    val savedResults = prefs.getString("diceResults", null)
    return if (savedResults != null && savedResults.isNotEmpty()) {
        savedResults.split(",").mapNotNull { it.toIntOrNull() }
    } else {
        List(prefs.getInt("diceCount", 2)) { (1..6).random() }
    }
}

fun saveRollHistory(editor: android.content.SharedPreferences.Editor, history: List<RollHistory>) {
    val historyString = history.take(10).joinToString(";") { roll ->
        "${roll.diceCount},${roll.results.joinToString(",")},${roll.sum},${roll.product}"
    }
    editor.putString("rollHistory", historyString)
}

fun loadRollHistory(prefs: android.content.SharedPreferences): List<RollHistory> {
    val historyString = prefs.getString("rollHistory", null) ?: return emptyList()
    if (historyString.isEmpty()) return emptyList()
    
    return try {
        historyString.split(";").mapNotNull { rollString ->
            val parts = rollString.split(",")
            if (parts.size >= 4) {
                val diceCount = parts[0].toIntOrNull() ?: return@mapNotNull null
                val results = parts.subList(1, parts.size - 2).mapNotNull { it.toIntOrNull() }
                val sum = parts[parts.size - 2].toIntOrNull() ?: return@mapNotNull null
                val product = parts[parts.size - 1].toIntOrNull() ?: return@mapNotNull null
                RollHistory(diceCount, results, sum, product)
            } else null
        }
    } catch (e: Exception) {
        emptyList()
    }
}
