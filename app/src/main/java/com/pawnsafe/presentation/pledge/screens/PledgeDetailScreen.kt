package com.pawnsafe.presentation.pledge.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.pawnsafe.core.utils.DateUtils
import com.pawnsafe.core.utils.InterestCalculator
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.presentation.pledge.PledgeViewModel
import java.io.File
import androidx.core.content.FileProvider
import com.pawnsafe.core.utils.ReceiptGenerator
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PledgeDetailScreen(
    pledgeId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onRedeem: (Int) -> Unit,
    viewModel: PledgeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val editingPledge by viewModel.editingPledge.collectAsState()

    LaunchedEffect(pledgeId) {
        viewModel.loadPledgeForEdit(pledgeId)
    }

    val pledge = editingPledge

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pledge != null) "Ticket #${pledge.ticketNo}" else "Pledge Detail") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.clearEditingPledge(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (pledge != null) {
                        IconButton(onClick = { onEdit(pledgeId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { shareReceipt(context, pledge) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (pledge == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // -- Photo card -----------------------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    if (!pledge.photoUri.isNullOrBlank() && File(pledge.photoUri).exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(File(pledge.photoUri)),
                            contentDescription = "Jewellery Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No photo captured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Article info below photo
                    Column(modifier = Modifier.padding(16.dp)) {
                        val statusColor = when (pledge.status) {
                            "ACTIVE"   -> MaterialTheme.colorScheme.primary
                            "OVERDUE"  -> MaterialTheme.colorScheme.error
                            "REDEEMED" -> Color(0xFF2E7D32)
                            else       -> MaterialTheme.colorScheme.outline
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Jewellery Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    pledge.status,
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        pledge.articleDescription?.let { DetailRow("Article", it) }
                        pledge.purity?.let { DetailRow("Purity", it) }
                        val gross = listOfNotNull(pledge.grossWeightG, pledge.grossWeightM).joinToString("-")
                        val nett  = listOfNotNull(pledge.nettWeightG,  pledge.nettWeightM).joinToString("-")
                        if (gross.isNotBlank()) DetailRow("Gross Weight", "${gross}g")
                        if (nett.isNotBlank())  DetailRow("Nett Weight",  "${nett}g")
                        pledge.presentValue?.let { DetailRow("Present Value", "?$it") }
                    }
                }

                // -- Customer + pledge info -----------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pledge Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Ticket No", "#${pledge.ticketNo}")
                        DetailRow("Date", DateUtils.isoToDisplay(pledge.date))
                        DetailRow("Name", pledge.name)
                        pledge.relation?.let    { DetailRow("Relation", it) }
                        pledge.phone?.let       { DetailRow("Phone", it) }
                        pledge.place?.let       { DetailRow("Place", it) }
                        pledge.taluk?.let       { DetailRow("Taluk", it) }
                        pledge.profession?.let  { DetailRow("Profession", it) }
                        DetailRow("Loan Amount", "Rs. ${pledge.loanAmountRs}")
                        pledge.loanAmountWords?.let { DetailRow("In Words", it) }
                    }
                }

                // -- Interest calculation -------------------------------------
                if (pledge.status != "REDEEMED") {
                    val today = LocalDate.now().toString()
                    val days  = try {
                        ChronoUnit.DAYS.between(
                            LocalDate.parse(pledge.date),
                            LocalDate.parse(today)
                        ).toInt() + 1
                    } catch (e: Exception) { 0 }
                    val principal = pledge.loanAmountRs.toDoubleOrNull() ?: 0.0
                    val rate      = 1.16
                    val interest  = InterestCalculator.calculate(principal, rate, days)
                    val total     = interest.totalAmount

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Interest Calculation (as of today)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            DetailRow("Pledge Date", DateUtils.isoToDisplay(pledge.date))
                            DetailRow("Today", DateUtils.isoToDisplay(today))
                            DetailRow("Days", "$days days")
                            DetailRow("Rate", "$rate% per month")
                            DetailRow("Principal", "Rs. ${"%.2f".format(principal)}")
                            DetailRow("Interest", "Rs. ${"%.2f".format(interest.interestRs)}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Due", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Rs. ${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // -- Action buttons -------------------------------------------
                if (pledge.status == "ACTIVE" || pledge.status == "OVERDUE") {
                    Button(
                        onClick = { onRedeem(pledgeId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pledge.status == "OVERDUE") MaterialTheme.colorScheme.error
                                             else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Redeem This Pledge", style = MaterialTheme.typography.titleSmall)
                    }
                }

                OutlinedButton(
                    onClick = { shareReceipt(context, pledge) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share Receipt")
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.5f))
    }
}

private fun shareReceipt(context: Context, pledge: Pledge) {
    val today     = LocalDate.now().toString()
    val days      = try {
        ChronoUnit.DAYS.between(LocalDate.parse(pledge.date), LocalDate.parse(today)).toInt() + 1
    } catch (e: Exception) { 0 }
    val principal = pledge.loanAmountRs.toDoubleOrNull() ?: 0.0
    val rate      = 1.16
    val interest  = InterestCalculator.calculate(principal, rate, days)
    val total     = interest.totalAmount
    val gross     = listOfNotNull(pledge.grossWeightG, pledge.grossWeightM).joinToString("-").ifBlank { null }
    val nett      = listOfNotNull(pledge.nettWeightG,  pledge.nettWeightM).joinToString("-").ifBlank { null }

    val bitmap = ReceiptGenerator.generateReceiptBitmap(
        shopName    = "Sri Nanjundeshwara Jewellers",
        ticketNo    = pledge.ticketNo,
        name        = pledge.name,
        phone       = pledge.phone,
        date        = DateUtils.isoToDisplay(pledge.date),
        article     = pledge.articleDescription,
        purity      = pledge.purity,
        grossWeight = gross,
        nettWeight  = nett,
        principal   = principal,
        days        = days,
        rate        = rate,
        interest    = interest.interestRs,
        total       = total,
        status      = pledge.status
    )

    val file = ReceiptGenerator.saveBitmapToCache(context, bitmap)
    val uri  = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt via"))
}