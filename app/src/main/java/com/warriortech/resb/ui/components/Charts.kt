package com.warriortech.resb.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warriortech.resb.model.PaymentModeData
import com.warriortech.resb.model.WeeklySalesData
import com.warriortech.resb.ui.theme.SurfaceLight

@Composable
fun PaymentModePieChart(
    data: List<PaymentModeData>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),

    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // ensure Column can center its content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Payment Modes - Today",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pie Chart
                Canvas(
                    modifier = Modifier.size(120.dp),
                ) {
                    drawPieChart(data, total)
                }

                // Legend
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.forEach { item ->
                        PaymentModeLegendItem(
                            paymentMode = item.paymentMode,
                            amount = item.amount,
                            color = item.color,
                            percentage = (item.amount / total * 100).toInt()
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PaymentModeLegendItem(
    paymentMode: String,
    amount: Double,
    color: Color,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = paymentMode,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.0f", amount)} ($percentage%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun DrawScope.drawPieChart(data: List<PaymentModeData>, total: Double) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 2.5f
    var currentAngle = -90f

    data.forEach { item ->
        val sweepAngle = (item.amount / total * 360).toFloat()

        drawArc(
            color = item.color,
            startAngle = currentAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        currentAngle += sweepAngle
    }
}

@Composable
fun WeeklySalesBarChart(
    data: List<WeeklySalesData>,
    modifier: Modifier = Modifier
) {
    val maxAmount = data.maxOfOrNull { it.amount } ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // ensure Column can center its content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "This Week Sales",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    BarChartItem(
                        label = item.date,
                        amount = item.amount,
                        maxAmount = maxAmount,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun BarChartItem(
    label: String,
    amount: Double,
    maxAmount: Double,
    modifier: Modifier = Modifier
) {
    val barHeight = (amount / maxAmount * 160).dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%.0f", amount),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(barHeight)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp
        )
    }
}
