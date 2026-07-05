package com.pawnsafe.presentation.redemption.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pawnsafe.core.utils.DateUtils
import com.pawnsafe.domain.model.Redemption
import com.pawnsafe.presentation.redemption.RedemptionFormState
import com.pawnsafe.presentation.redemption.RedemptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedemptionFormScreen(
    onBack:   () -> Unit,
    onSaved:  () -> Unit,
    pledgeId: Int = 0,
    viewModel: RedemptionViewModel = hiltViewModel()
) {
    val formState   by viewModel.formState.collectAsState()
    val prefill     by viewModel.prefillState.collectAsState()

    var ticketNo     by remember { mutableStateOf("") }
    var internalPledgeId by remember { mutableStateOf(0) }
    var customerName by remember { mutableStateOf("") }
    var address      by remember { mutableStateOf("") }
    var pledgeDate   by remember { mutableStateOf("") }
    var returnDate   by remember { mutableStateOf(viewModel.todayIso()) }
    var principalRs  by remember { mutableStateOf("") }

    var numberOfDays by remember { mutableStateOf(0) }
    var interestRs   by remember { mutableStateOf(0.0) }
    var interestPs   by remember { mutableStateOf(0.0) }
    var totalAmount  by remember { mutableStateOf(0.0) }
    var interestRate by remember { mutableStateOf(0.0) }
    var calcDone     by remember { mutableStateOf(false) }
    var prefilled    by remember { mutableStateOf(false) }
    var ticketError  by remember { mutableStateOf(false) }

    // Load pledge from ID if coming from Redeem button
    LaunchedEffect(pledgeId) {
        if (pledgeId != 0) viewModel.loadPledgeById(pledgeId)
    }

    // Apply prefill when it arrives
    LaunchedEffect(prefill) {
        val p = prefill ?: return@LaunchedEffect
        if (!prefilled) {
            ticketNo         = p.ticketNo
            internalPledgeId = p.pledgeId
            customerName     = p.customerName
            address          = p.address
            pledgeDate       = p.pledgeDate
            principalRs      = p.principalRs.toString()
            prefilled        = true
            // Auto-calculate interest immediately
            if (pledgeDate.isNotBlank() && principalRs.isNotBlank()) {
                viewModel.calculateInterest(p.principalRs, p.pledgeDate, returnDate)
            }
        }
    }

    LaunchedEffect(formState) {
        when (val s = formState) {
            is RedemptionFormState.InterestReady -> {
                numberOfDays = s.result.numberOfDays
                interestRs   = s.result.interestRs
                interestPs   = s.result.interestPs
                totalAmount  = s.result.totalAmount
                interestRate = s.result.rate
                calcDone     = true
            }
            is RedemptionFormState.Saved -> {
                viewModel.resetFormState()
                viewModel.clearPrefill()
                onSaved()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (prefilled) "Redeem — #$ticketNo" else "New Redemption") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.clearPrefill(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Pre-filled banner
            if (prefilled) {
                Card(
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "✅  Pledge details loaded — verify and save",
                        color    = Color(0xFF1B5E20),
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ── Ticket lookup (manual entry mode) ──────────────────────────
            if (!prefilled) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value         = ticketNo,
                        onValueChange = { ticketNo = it; ticketError = false },
                        label         = { Text("Ticket No *") },
                        isError       = ticketError,
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (ticketNo.isBlank()) { ticketError = true; return@IconButton }
                        viewModel.lookupPledge(ticketNo) { pid, name, date, principal ->
                            internalPledgeId = pid
                            customerName     = name
                            pledgeDate       = date
                            principalRs      = principal.toString()
                            viewModel.calculateInterest(principal, date, returnDate)
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Lookup")
                    }
                }
                if (ticketError) Text("Enter ticket no", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                // Read-only ticket display in pre-filled mode
                OutlinedTextField(
                    value         = ticketNo,
                    onValueChange = {},
                    label         = { Text("Ticket No") },
                    readOnly      = true,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            SectionLabel("Customer")
            OutlinedTextField(value = customerName, onValueChange = { customerName = it },
                label = { Text("Customer Name *") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it },
                label = { Text("Address") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())

            SectionLabel("Dates")
            OutlinedTextField(value = pledgeDate, onValueChange = { pledgeDate = it },
                label = { Text("Pledge Date (yyyy-MM-dd)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value         = returnDate,
                onValueChange = {
                    returnDate = it
                    val p = principalRs.toDoubleOrNull()
                    if (p != null && pledgeDate.isNotBlank() && it.length == 10) {
                        viewModel.calculateInterest(p, pledgeDate, it)
                    }
                },
                label    = { Text("Return Date (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel("Loan")
            OutlinedTextField(
                value         = principalRs,
                onValueChange = { principalRs = it },
                label         = { Text("Principal Amount (Rs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Calculate button (manual mode)
            if (!calcDone) {
                Button(
                    onClick = {
                        val p = principalRs.toDoubleOrNull()
                        if (p != null && pledgeDate.isNotBlank()) {
                            viewModel.calculateInterest(p, pledgeDate, returnDate)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Calculate Interest") }
            }

            // Interest result card
            if (calcDone) {
                Card(
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Interest Calculation", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary)
                        HorizontalDivider()
                        CalcRow("Days",          numberOfDays.toString())
                        CalcRow("Rate",          "$interestRate %/month")
                        CalcRow("Principal",     "Rs. $principalRs")
                        CalcRow("Interest",      "Rs. %.2f".format(interestRs))
                        HorizontalDivider()
                        CalcRow("Total Payable", "Rs. %.2f".format(totalAmount), bold = true)
                    }
                }

                if (formState is RedemptionFormState.Error) {
                    Text((formState as RedemptionFormState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        viewModel.saveRedemption(
                            Redemption(
                                id           = 0,
                                pledgeId     = internalPledgeId,
                                ticketNo     = ticketNo.trim(),
                                customerName = customerName.trim(),
                                address      = address.ifBlank { null },
                                pledgeDate   = pledgeDate,
                                returnDate   = returnDate,
                                numberOfDays = numberOfDays,
                                principalRs  = principalRs.toDoubleOrNull() ?: 0.0,
                                principalPs  = 0.0,
                                interestRs   = interestRs,
                                interestPs   = interestPs,
                                totalAmount  = totalAmount,
                                createdAt    = System.currentTimeMillis()
                            )
                        )
                    },
                    enabled  = formState !is RedemptionFormState.Loading,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    if (formState is RedemptionFormState.Loading)
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else
                        Text("Save Redemption")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
    HorizontalDivider()
}

@Composable
private fun CalcRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (bold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}