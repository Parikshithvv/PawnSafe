package com.pawnsafe.presentation.pledge.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pawnsafe.core.utils.DateUtils
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.presentation.pledge.PledgeUIState
import com.pawnsafe.presentation.pledge.PledgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PledgeListScreen(
    onBack:        () -> Unit,
    onAddClick:    () -> Unit,
    onScanClick:   () -> Unit,
    onPledgeClick: (Int) -> Unit,
    onRedeemClick: (Int) -> Unit,
    viewModel: PledgeViewModel = hiltViewModel()
) {
    val state          by viewModel.listState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    var query          by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pledge Book") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = onScanClick) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan")
                }
                Spacer(Modifier.height(8.dp))
                FloatingActionButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Pledge")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.isBlank()) viewModel.loadAllPledges() else viewModel.search(it)
                },
                placeholder = { Text("Search by name, ticket no or phone") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "ACTIVE", "REDEEMED", "OVERDUE").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick  = { viewModel.onFilterSelected(filter) },
                        label    = { Text(filter) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when (val s = state) {
                is PledgeUIState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is PledgeUIState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                }
                is PledgeUIState.Success -> {
                    if (s.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pledges found")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(s.data, key = { it.id }) { pledge ->
                                PledgeCard(
                                    pledge        = pledge,
                                    onClick       = { onPledgeClick(pledge.id) },
                                    onRedeemClick = { onRedeemClick(pledge.id) }
                                )
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
private fun PledgeCard(
    pledge: Pledge,
    onClick: () -> Unit,
    onRedeemClick: () -> Unit
) {
    val statusColor = when (pledge.status) {
        "ACTIVE"   -> MaterialTheme.colorScheme.primary
        "REDEEMED" -> MaterialTheme.colorScheme.secondary
        "OVERDUE"  -> MaterialTheme.colorScheme.error
        else       -> MaterialTheme.colorScheme.outline
    }
    Card(
        modifier  = Modifier.fillMaxWidth(),
        onClick   = onClick,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pledge.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Ticket: ${pledge.ticketNo}", style = MaterialTheme.typography.bodySmall)
                    Text("Date: ${DateUtils.isoToDisplay(pledge.date)}", style = MaterialTheme.typography.bodySmall)
                    Text("Rs. ${pledge.loanAmountRs}", style = MaterialTheme.typography.bodyMedium)
                    pledge.phone?.let {
                        Text("Ph: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text     = pledge.status,
                        color    = statusColor,
                        style    = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }
            if (pledge.status == "ACTIVE" || pledge.status == "OVERDUE") {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick  = onRedeemClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (pledge.status == "OVERDUE")
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) { Text("Redeem This Pledge") }
            }
        }
    }
}