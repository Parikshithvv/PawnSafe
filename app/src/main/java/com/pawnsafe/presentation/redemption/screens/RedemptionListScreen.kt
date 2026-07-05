package com.pawnsafe.presentation.redemption.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pawnsafe.core.utils.DateUtils
import com.pawnsafe.domain.model.Redemption
import com.pawnsafe.presentation.redemption.RedemptionUIState
import com.pawnsafe.presentation.redemption.RedemptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedemptionListScreen(
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: RedemptionViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Redemption Register") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Redemption")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is RedemptionUIState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is RedemptionUIState.Error   -> Text(s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is RedemptionUIState.Success -> {
                    if (s.data.isEmpty()) {
                        Text("No redemptions yet", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(s.data, key = { it.id }) { redemption ->
                                RedemptionCard(redemption)
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun RedemptionCard(r: Redemption) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(r.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Ticket: ${r.ticketNo}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LabelValue("Pledged", DateUtils.isoToDisplay(r.pledgeDate))
                LabelValue("Returned", DateUtils.isoToDisplay(r.returnDate))
                LabelValue("Days", r.numberOfDays.toString())
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LabelValue("Principal", "Rs.${r.principalRs}")
                LabelValue("Interest", "Rs.${"%.2f".format(r.interestRs)}")
                LabelValue("Total", "Rs.${"%.2f".format(r.totalAmount)}")
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}