package com.pawnsafe.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.pawnsafe.ui.theme.*

private fun fmtAxis(value: Float): String = when {
    value >= 100_000f -> "${"%.1f".format(value / 100_000f)}L"
    value >= 1_000f   -> "${"%.1f".format(value / 1_000f)}K"
    else              -> value.toInt().toString()
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val data          by viewModel.dashboardData.collectAsState()
    val aiInsight     by viewModel.aiInsight.collectAsState()
    val aiLoading     by viewModel.aiLoading.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val barColor  = Terracotta.toArgb()
    val lineColor = TerracottaDark.toArgb()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPeach)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarmPeachCard)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sri Nanjundeshwara Jewellers",
                    fontSize = 11.sp,
                    color = Terracotta,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Dashboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            IconButton(onClick = { viewModel.loadDashboard() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Terracotta)
            }
        }

        if (data == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta)
            }
        } else {
            val d = data!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                item {
                    SectionLabel("Overview")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(Modifier.weight(1f), "Active", "${d.totalActive}", WarmPeachCard, TerracottaDark, Terracotta)
                        SummaryCard(Modifier.weight(1f), "Overdue", "${d.overdueCount}", CoralCard, CoralText, CoralLabel)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(Modifier.weight(1f), "Loan Out", "Rs.${"%.0f".format(d.totalLoanOut)}", BlueCard, BlueText, BlueText)
                        SummaryCard(Modifier.weight(1f), "Redeemed", "${d.totalRedeemed}", GreenCard, GreenText, GreenLabel)
                    }
                }

                item {
                    SectionLabel("Monthly Pledges")
                    Spacer(Modifier.height(8.dp))
                    if (d.monthStats.isNotEmpty()) {
                        val barProducer = remember(d.monthStats) {
                            ChartEntryModelProducer(
                                d.monthStats.mapIndexed { i, s -> entryOf(i.toFloat(), s.pledgeCount.toFloat()) }
                            )
                        }
                        val barChart = columnChart(
                            columns = listOf(
                                LineComponent(
                                    color = barColor,
                                    thicknessDp = 16f,
                                    shape = Shapes.roundedCornerShape(topLeftPercent = 40, topRightPercent = 40)
                                )
                            ),
                            mergeMode = ColumnChart.MergeMode.Grouped
                        )
                        val axisLabel = textComponent { color = android.graphics.Color.BLACK }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceWhite)
                                .padding(16.dp)
                        ) {
                            Column {
                                Chart(
                                    chart = barChart,
                                    chartModelProducer = barProducer,
                                    startAxis = rememberStartAxis(
                                        label = axisLabel,
                                        valueFormatter = { value, _ -> fmtAxis(value) }
                                    ),
                                    bottomAxis = rememberBottomAxis(
                                        label = axisLabel,
                                        valueFormatter = { value, _ ->
                                            d.monthStats.getOrNull(value.toInt())?.month?.takeLast(5) ?: ""
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Tap a month to see details", fontSize = 10.sp, color = TextMuted)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    d.monthStats.forEach { stat ->
                                        FilterChip(
                                            selected = selectedMonth?.month == stat.month,
                                            onClick = { viewModel.onMonthSelected(stat) },
                                            label = { Text(stat.month.takeLast(5), fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Terracotta,
                                                selectedLabelColor = SurfaceWhite
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = selectedMonth != null, enter = expandVertically(), exit = shrinkVertically()) {
                            selectedMonth?.let { month ->
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(WarmPeachCard)
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Text("${month.month} — ${month.pledgeCount} pledges", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TerracottaDark)
                                        Text("Total loan: Rs.${"%.0f".format(month.totalLoan)}", fontSize = 11.sp, color = TextSecondary)
                                        Spacer(Modifier.height(8.dp))
                                        month.pledges.forEach { pledge ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("#${pledge.ticketNo} ${pledge.name}", fontSize = 11.sp, color = TextSecondary)
                                                Text("Rs.${pledge.loanAmountRs}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SectionLabel("Monthly Loan Amount (Rs.)")
                    Spacer(Modifier.height(8.dp))
                    if (d.monthStats.isNotEmpty()) {
                        val lineProducer = remember(d.monthStats) {
                            ChartEntryModelProducer(
                                d.monthStats.mapIndexed { i, s -> entryOf(i.toFloat(), s.totalLoan.toFloat()) }
                            )
                        }
                        val lineChartColored = lineChart(
                            lines = listOf(
                                LineChart.LineSpec(
                                    lineColor = lineColor,
                                    lineThicknessDp = 3f,
                                    lineBackgroundShader = null
                                )
                            )
                        )
                        val axisLabel2 = textComponent { color = android.graphics.Color.BLACK }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceWhite)
                                .padding(16.dp)
                        ) {
                            Chart(
                                chart = lineChartColored,
                                chartModelProducer = lineProducer,
                                startAxis = rememberStartAxis(label = axisLabel2, valueFormatter = { value, _ -> fmtAxis(value) }),
                                bottomAxis = rememberBottomAxis(label = axisLabel2, valueFormatter = { value, _ ->
                                    d.monthStats.getOrNull(value.toInt())?.month?.takeLast(5) ?: ""
                                }),
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                            )
                        }
                    }
                }

                item {
                    SectionLabel("Debit / Credit Ledger")
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceWhite)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LedgerRow("Debit (Loans Given Out)", "Rs.${"%.0f".format(d.totalLoanOut)}", CoralText)
                            HorizontalDivider(color = DividerColor)
                            LedgerRow("Credit (Redemptions Collected)", "Rs.${"%.0f".format(d.totalRedeemedAmount)}", GreenText)
                            HorizontalDivider(color = DividerColor)
                            val net = d.totalRedeemedAmount - d.totalLoanOut
                            LedgerRow("Net Position", "Rs.${"%.0f".format(net)}", if (net >= 0) GreenText else CoralText)
                        }
                    }
                }

                item {
                    SectionLabel("AI Insights")
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(WarmPeachCard)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Powered by Gemini 2.5 Flash", fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.height(12.dp))
                            when {
                                aiLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Terracotta)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Analyzing your data...", fontSize = 12.sp, color = TextSecondary)
                                }
                                aiInsight != null -> Text(aiInsight!!, fontSize = 12.sp, color = TextPrimary, lineHeight = 20.sp)
                                else -> Text("Tap below to get AI-powered insights about your business.", fontSize = 12.sp, color = TextMuted)
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.fetchAiInsight() },
                                enabled = !aiLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (aiInsight == null) "Get Insights" else "Refresh Insights")
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    label: String,
    value: String,
    backgroundColor: Color,
    valueColor: Color,
    labelColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Column {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
            Spacer(Modifier.height(2.dp))
            Text(text = label, fontSize = 11.sp, color = labelColor)
        }
    }
}

@Composable
private fun LedgerRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}