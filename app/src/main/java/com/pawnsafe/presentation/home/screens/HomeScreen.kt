package com.pawnsafe.presentation.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pawnsafe.presentation.home.HomeStats
import com.pawnsafe.presentation.home.HomeUIState
import com.pawnsafe.presentation.home.HomeViewModel
import com.pawnsafe.ui.theme.*

@Composable
fun HomeScreen(
    onPledgeListClick: () -> Unit,
    onAddPledgeClick: () -> Unit,
    onScanClick: () -> Unit,
    onRedemptionListClick: () -> Unit,
    onExportClick: () -> Unit,
    onInterestRatesClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPeach)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val s = uiState) {
                is HomeUIState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Terracotta) }
                }
                is HomeUIState.Error -> {
                    Text(
                        text = s.message,
                        fontSize = 13.sp,
                        color = CoralText
                    )
                }
                is HomeUIState.Success -> {
                    StatCardsGrid(s.stats)
                    TotalRedemptionsCard(s.stats.totalRedemptions)
                }
            }

            QuickActionsSection(
                onPledgeBook = onPledgeListClick,
                onScanTicket = onScanClick,
                onRedemptions = onRedemptionListClick,
                onExport = onExportClick,
                onInterestRates = onInterestRatesClick
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarmPeachCard)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Sri Nanjundeshwara Jewellers",
            fontSize = 11.sp,
            color = Terracotta,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "PawnSafe",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun StatCardsGrid(stats: HomeStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.totalPledges.toString(),
                label = "Total pledges",
                backgroundColor = WarmPeachCard,
                valueColor = TerracottaDark,
                labelColor = Terracotta
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.activePledges.toString(),
                label = "Active",
                backgroundColor = WarmPeachCard,
                valueColor = TerracottaDark,
                labelColor = Terracotta
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.redeemedPledges.toString(),
                label = "Redeemed",
                backgroundColor = CoralCard,
                valueColor = CoralText,
                labelColor = CoralLabel
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.overduePledges.toString(),
                label = "Overdue",
                backgroundColor = CoralCard,
                valueColor = CoralText,
                labelColor = CoralLabel
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
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
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = labelColor
            )
        }
    }
}

@Composable
private fun TotalRedemptionsCard(total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeutralCard)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = total.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeutralText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Total redemptions",
                fontSize = 11.sp,
                color = NeutralLabel
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onPledgeBook: () -> Unit,
    onScanTicket: () -> Unit,
    onRedemptions: () -> Unit,
    onExport: () -> Unit,
    onInterestRates: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Quick actions",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.MenuBook,
                label = "Pledge book",
                onClick = onPledgeBook
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CameraAlt,
                label = "Scan ticket",
                onClick = onScanTicket
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Receipt,
                label = "Redemptions",
                onClick = onRedemptions
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.TableChart,
                label = "Export Excel",
                onClick = onExport
            )
        }

        QuickActionButton(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Percent,
            label = "Interest rates",
            onClick = onInterestRates
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceWhite)
            .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Terracotta,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}