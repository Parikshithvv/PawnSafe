package com.pawnsafe.presentation.pledge.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.pawnsafe.core.utils.NumberToWords
import com.pawnsafe.core.utils.OcrResultHolder
import com.pawnsafe.core.utils.OcrHelper
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.presentation.pledge.CustomerLookupResult
import com.pawnsafe.presentation.pledge.PledgeFormState
import com.pawnsafe.presentation.pledge.PledgeViewModel
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PledgeFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    pledgeId: Int = 0,
    onCapturePhoto: (() -> Unit)? = null,
    capturedPhotoUri: String? = null,
    viewModel: PledgeViewModel = hiltViewModel()
) {
    val formState      by viewModel.formState.collectAsState()
    val editingPledge  by viewModel.editingPledge.collectAsState()
    val customerLookup by viewModel.customerLookup.collectAsState()
    val isEditMode     = pledgeId != 0

    LaunchedEffect(pledgeId) {
        if (pledgeId != 0) viewModel.loadPledgeForEdit(pledgeId)
    }

    var ticketNo           by remember { mutableStateOf("") }
    var date               by remember { mutableStateOf("") }
    var name               by remember { mutableStateOf("") }
    var relation           by remember { mutableStateOf("") }
    var cross              by remember { mutableStateOf("") }
    var place              by remember { mutableStateOf("") }
    var post               by remember { mutableStateOf("") }
    var taluk              by remember { mutableStateOf("") }
    var hobli              by remember { mutableStateOf("") }
    var profession         by remember { mutableStateOf("") }
    var phone              by remember { mutableStateOf("") }
    var loanAmountRs       by remember { mutableStateOf("") }
    var loanAmountWords    by remember { mutableStateOf("") }
    var articleDescription by remember { mutableStateOf("") }
    var purity             by remember { mutableStateOf("") }
    var grossWeightG       by remember { mutableStateOf("") }
    var grossWeightM       by remember { mutableStateOf("") }
    var nettWeightG        by remember { mutableStateOf("") }
    var nettWeightM        by remember { mutableStateOf("") }
    var presentValue       by remember { mutableStateOf("") }
    var photoUri           by remember { mutableStateOf<String?>(null) }
    var fieldsInitialized  by remember { mutableStateOf(false) }
    var ocrFilledKeys      by remember { mutableStateOf(emptySet<String>()) }
    var profileFilledKeys  by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(capturedPhotoUri) {
        if (!capturedPhotoUri.isNullOrBlank()) photoUri = capturedPhotoUri
    }

    val nameFocus     = remember { FocusRequester() }
    val relationFocus = remember { FocusRequester() }
    val crossFocus    = remember { FocusRequester() }
    val placeFocus    = remember { FocusRequester() }
    val talukFocus    = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (!isEditMode && !fieldsInitialized) {
            val ocr = OcrResultHolder.consume()
            if (!ocr.isEmpty()) {
                ticketNo     = ocr.fields[OcrHelper.Fields.TICKET_NO]      ?: ""
                date         = ocr.fields[OcrHelper.Fields.DATE]           ?: viewModel.todayIso()
                name         = ocr.fields[OcrHelper.Fields.NAME]           ?: ""
                relation     = ocr.fields[OcrHelper.Fields.RELATION]       ?: ""
                cross        = ocr.fields[OcrHelper.Fields.CROSS]          ?: ""
                place        = ocr.fields[OcrHelper.Fields.PLACE]          ?: ""
                taluk        = ocr.fields[OcrHelper.Fields.TALUK]          ?: ""
                hobli        = ocr.fields[OcrHelper.Fields.HOBLI]          ?: ""
                profession   = ocr.fields[OcrHelper.Fields.PROFESSION]     ?: ""
                phone        = ocr.fields[OcrHelper.Fields.PHONE]          ?: ""
                loanAmountRs = ocr.fields[OcrHelper.Fields.LOAN_AMOUNT]    ?: ""
                purity       = ocr.fields[OcrHelper.Fields.PURITY]         ?: ""
                grossWeightG = ocr.fields[OcrHelper.Fields.GROSS_WEIGHT_G] ?: ""
                grossWeightM = ocr.fields[OcrHelper.Fields.GROSS_WEIGHT_M] ?: ""
                nettWeightG  = ocr.fields[OcrHelper.Fields.NETT_WEIGHT_G]  ?: ""
                nettWeightM  = ocr.fields[OcrHelper.Fields.NETT_WEIGHT_M]  ?: ""
                presentValue = ocr.fields[OcrHelper.Fields.PRESENT_VALUE]  ?: ""
                ocrFilledKeys = ocr.filledKeys
                // auto-convert amount in words if OCR filled loan amount
                loanAmountWords = loanAmountRs.toLongOrNull()?.let { NumberToWords.convert(it) } ?: ""
                fieldsInitialized = true
                if (phone.length == 10) viewModel.lookupByPhone(phone)
                delay(350)
                when {
                    name.isBlank()     -> nameFocus.requestFocus()
                    relation.isBlank() -> relationFocus.requestFocus()
                    cross.isBlank()    -> crossFocus.requestFocus()
                    place.isBlank()    -> placeFocus.requestFocus()
                    taluk.isBlank()    -> talukFocus.requestFocus()
                }
            }
        }
    }

    LaunchedEffect(phone) {
        if (!isEditMode && phone.length == 10) viewModel.lookupByPhone(phone)
        else if (phone.length < 10) { viewModel.clearCustomerLookup(); profileFilledKeys = emptySet() }
    }

    LaunchedEffect(customerLookup) {
        val lookup = customerLookup ?: return@LaunchedEffect
        val profile = lookup.profile ?: return@LaunchedEffect
        if (isEditMode) return@LaunchedEffect
        val filled = mutableSetOf<String>()
        if (name.isBlank() && !profile.name.isNullOrBlank())             { name = profile.name; filled.add("name") }
        if (relation.isBlank() && !profile.relation.isNullOrBlank())     { relation = profile.relation; filled.add("relation") }
        if (cross.isBlank() && !profile.cross.isNullOrBlank())           { cross = profile.cross; filled.add("cross") }
        if (place.isBlank() && !profile.place.isNullOrBlank())           { place = profile.place; filled.add("place") }
        if (post.isBlank() && !profile.post.isNullOrBlank())             { post = profile.post; filled.add("post") }
        if (taluk.isBlank() && !profile.taluk.isNullOrBlank())           { taluk = profile.taluk; filled.add("taluk") }
        if (hobli.isBlank() && !profile.hobli.isNullOrBlank())           { hobli = profile.hobli; filled.add("hobli") }
        if (profession.isBlank() && !profile.profession.isNullOrBlank()) { profession = profile.profession; filled.add("profession") }
        profileFilledKeys = filled
    }

    LaunchedEffect(editingPledge) {
        val p = editingPledge
        if (p != null && !fieldsInitialized) {
            ticketNo           = p.ticketNo
            date               = p.date
            name               = p.name
            relation           = p.relation           ?: ""
            cross              = p.cross              ?: ""
            place              = p.place              ?: ""
            post               = p.post               ?: ""
            taluk              = p.taluk              ?: ""
            hobli              = p.hobli              ?: ""
            profession         = p.profession         ?: ""
            phone              = p.phone              ?: ""
            loanAmountRs       = p.loanAmountRs
            loanAmountWords    = p.loanAmountWords    ?: ""
            articleDescription = p.articleDescription ?: ""
            purity             = p.purity             ?: ""
            grossWeightG       = p.grossWeightG       ?: ""
            grossWeightM       = p.grossWeightM       ?: ""
            nettWeightG        = p.nettWeightG        ?: ""
            nettWeightM        = p.nettWeightM        ?: ""
            presentValue       = p.presentValue       ?: ""
            photoUri           = p.photoUri
            fieldsInitialized  = true
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditMode && date.isBlank()) date = viewModel.todayIso()
    }

    var ticketError by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    LaunchedEffect(formState) {
        if (formState is PledgeFormState.Saved) {
            viewModel.resetFormState()
            viewModel.clearEditingPledge()
            viewModel.clearCustomerLookup()
            onSaved()
        }
    }

    val emptyCount = if (!isEditMode && ocrFilledKeys.isNotEmpty())
        listOf(name, relation, cross, place, taluk, profession, loanAmountRs, grossWeightG, nettWeightG).count { it.isBlank() }
    else 0
    val ocrActive = !isEditMode && ocrFilledKeys.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Pledge" else "New Pledge Entry") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearEditingPledge()
                        viewModel.clearCustomerLookup()
                        onBack()
                    }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
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

            if (emptyCount > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), modifier = Modifier.fillMaxWidth()) {
                    Text("✏️  $emptyCount field${if (emptyCount > 1) "s" else ""} need manual entry  (orange = fill me)",
                        color = Color(0xFFE65100), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                }
            }

            val activePledges = customerLookup?.activePledges ?: emptyList()
            if (!isEditMode && activePledges.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⚠️  ${activePledges.size} active pledge${if (activePledges.size > 1) "s" else ""} already open for this customer",
                            color = Color(0xFFC62828), style = MaterialTheme.typography.bodyMedium)
                        activePledges.forEach { p ->
                            Text("• Ticket #${p.ticketNo}  •  Rs.${p.loanAmountRs}  •  ${p.date}",
                                color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }

            val history = customerLookup?.history ?: emptyList()
            if (!isEditMode && customerLookup?.profile != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("✅  Returning customer — personal details auto-filled", color = Color(0xFF1B5E20), style = MaterialTheme.typography.bodyMedium)
                        if (history.size > 1) {
                            Text("${history.size} past pledges on record", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                            history.take(3).forEach { p ->
                                Text("• #${p.ticketNo}  Rs.${p.loanAmountRs}  ${p.date}  [${p.status}]",
                                    color = Color(0xFF388E3C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 1.dp))
                            }
                        }
                    }
                }
            }

            // ── Photo section ────────────────────────────────────────────────
            SectionHeader("Jewellery Photo")
            if (!photoUri.isNullOrBlank() && File(photoUri!!).exists()) {
                Image(
                    painter = rememberAsyncImagePainter(File(photoUri!!)),
                    contentDescription = "Jewellery Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                OutlinedButton(
                    onClick = { onCapturePhoto?.invoke() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Retake Photo")
                }
            } else {
                OutlinedButton(
                    onClick = { onCapturePhoto?.invoke() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Capture Jewellery Photo")
                }
            }

            SectionHeader("Basic Details")
            OcrField("Ticket No *", ticketNo, { ticketNo = it; ticketError = false },
                OcrHelper.Fields.TICKET_NO in ocrFilledKeys, ocrActive, isError = ticketError, errorMsg = "Required")
            OcrField("Date (yyyy-MM-dd) *", date, { date = it },
                OcrHelper.Fields.DATE in ocrFilledKeys, ocrActive)
            OcrField("Name *", name, { name = it; nameError = false },
                ocrFilled = OcrHelper.Fields.NAME in ocrFilledKeys || "name" in profileFilledKeys,
                ocrAttempted = ocrActive || profileFilledKeys.isNotEmpty(),
                isError = nameError, errorMsg = "Required", focusRequester = nameFocus)
            OcrField("Relation (W/o S/o D/o)", relation, { relation = it },
                ocrFilled = OcrHelper.Fields.RELATION in ocrFilledKeys || "relation" in profileFilledKeys,
                ocrAttempted = ocrActive || profileFilledKeys.isNotEmpty(), focusRequester = relationFocus)
            OcrField("Cross", cross, { cross = it },
                ocrFilled = OcrHelper.Fields.CROSS in ocrFilledKeys || "cross" in profileFilledKeys,
                ocrAttempted = ocrActive || profileFilledKeys.isNotEmpty(), focusRequester = crossFocus)
            if (!isEditMode) QuickChips(listOf("Mullur","Tumkur","Sira","Tiptur","Kunigal","Madhugiri"), cross) { cross = it }

            SectionHeader("Address")
            OcrField("Place", place, { place = it },
                ocrFilled = OcrHelper.Fields.PLACE in ocrFilledKeys || "place" in profileFilledKeys,
                ocrAttempted = ocrActive || profileFilledKeys.isNotEmpty(), focusRequester = placeFocus)
            OcrField("Post", post, { post = it },
                ocrFilled = "post" in profileFilledKeys, ocrAttempted = profileFilledKeys.isNotEmpty())
            OcrField("Taluk", taluk, { taluk = it },
                ocrFilled = OcrHelper.Fields.TALUK in ocrFilledKeys || "taluk" in profileFilledKeys,
                ocrAttempted = ocrActive || profileFilledKeys.isNotEmpty(), focusRequester = talukFocus)
            if (!isEditMode) QuickChips(listOf("Tumkur","Sira","Tiptur","Madhugiri","Kunigal","Gubbi"), taluk) { taluk = it }
            OcrField("Hobli", hobli, { hobli = it },
                ocrFilled = OcrHelper.Fields.HOBLI in ocrFilledKeys || "hobli" in profileFilledKeys,
                ocrAttempted = ocrActive || profileFilledKeys.isNotEmpty())
            OcrField("Profession", profession, { profession = it },
                ocrFilled = OcrHelper.Fields.PROFESSION in ocrFilledKeys || "profession" in profileFilledKeys,
                ocrAttempted = ocrActive || profileFilledKeys.isNotEmpty())
            if (!isEditMode) QuickChips(listOf("Agriculture","Business","Labour","Housewife","Driver","Other"), profession) { profession = it }
            OcrField("Phone", phone, { phone = it },
                OcrHelper.Fields.PHONE in ocrFilledKeys, ocrActive, keyboardType = KeyboardType.Phone)

            SectionHeader("Loan Details")
            OcrField(
                label         = "Loan Amount (Rs) *",
                value         = loanAmountRs,
                onValueChange = {
                    loanAmountRs    = it
                    amountError     = false
                    loanAmountWords = it.toLongOrNull()?.let { n -> NumberToWords.convert(n) } ?: loanAmountWords
                },
                ocrFilled    = OcrHelper.Fields.LOAN_AMOUNT in ocrFilledKeys,
                ocrAttempted = ocrActive,
                isError      = amountError,
                errorMsg     = "Required",
                keyboardType = KeyboardType.Number
            )
            OcrField("Amount in Words", loanAmountWords, { loanAmountWords = it }, false, false)

            SectionHeader("Article / Jewellery")
            OcrField("Article Description", articleDescription, { articleDescription = it }, false, false)
            OcrField("Purity", purity, { purity = it }, OcrHelper.Fields.PURITY in ocrFilledKeys, ocrActive)
            if (!isEditMode) QuickChips(listOf("22K","18K","916","750"), purity) { purity = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OcrField("Gross G", grossWeightG, { grossWeightG = it },
                    OcrHelper.Fields.GROSS_WEIGHT_G in ocrFilledKeys, ocrActive,
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                OcrField("Gross M", grossWeightM, { grossWeightM = it },
                    OcrHelper.Fields.GROSS_WEIGHT_M in ocrFilledKeys, ocrActive,
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OcrField("Nett G", nettWeightG, { nettWeightG = it },
                    OcrHelper.Fields.NETT_WEIGHT_G in ocrFilledKeys, ocrActive,
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                OcrField("Nett M", nettWeightM, { nettWeightM = it },
                    OcrHelper.Fields.NETT_WEIGHT_M in ocrFilledKeys, ocrActive,
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
            }
            OcrField("Present Value", presentValue, { presentValue = it },
                OcrHelper.Fields.PRESENT_VALUE in ocrFilledKeys, ocrActive, keyboardType = KeyboardType.Number)

            if (formState is PledgeFormState.Error) {
                Text((formState as PledgeFormState.Error).message,
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    ticketError = ticketNo.isBlank()
                    nameError   = name.isBlank()
                    amountError = loanAmountRs.isBlank()
                    if (ticketError || nameError || amountError) return@Button
                    viewModel.savePledge(
                        Pledge(
                            id                 = if (isEditMode) pledgeId else 0,
                            ticketNo           = ticketNo.trim(),
                            date               = date.trim(),
                            name               = name.trim(),
                            relation           = relation.ifBlank { null },
                            cross              = cross.ifBlank { null },
                            place              = place.ifBlank { null },
                            post               = post.ifBlank { null },
                            taluk              = taluk.ifBlank { null },
                            hobli              = hobli.ifBlank { null },
                            profession         = profession.ifBlank { null },
                            phone              = phone.ifBlank { null },
                            loanAmountRs       = loanAmountRs.trim(),
                            loanAmountWords    = loanAmountWords.ifBlank { null },
                            articleDescription = articleDescription.ifBlank { null },
                            purity             = purity.ifBlank { null },
                            grossWeightG       = grossWeightG.ifBlank { null },
                            grossWeightM       = grossWeightM.ifBlank { null },
                            nettWeightG        = nettWeightG.ifBlank { null },
                            nettWeightM        = nettWeightM.ifBlank { null },
                            presentValue       = presentValue.ifBlank { null },
                            status             = editingPledge?.status ?: "ACTIVE",
                            photoUri           = photoUri,
                            createdAt          = editingPledge?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled  = formState !is PledgeFormState.Loading,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                if (formState is PledgeFormState.Loading)
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else
                    Text(if (isEditMode) "Update Pledge" else "Save Pledge")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OcrField(
    label: String, value: String, onValueChange: (String) -> Unit,
    ocrFilled: Boolean, ocrAttempted: Boolean,
    isError: Boolean = false, errorMsg: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    focusRequester: FocusRequester? = null
) {
    val unfocusedBorder = when {
        isError       -> MaterialTheme.colorScheme.error
        !ocrAttempted -> MaterialTheme.colorScheme.outline
        ocrFilled     -> Color(0xFF2E7D32)
        else          -> Color(0xFFE65100)
    }
    val focusedBorder = when {
        isError       -> MaterialTheme.colorScheme.error
        !ocrAttempted -> MaterialTheme.colorScheme.primary
        ocrFilled     -> Color(0xFF2E7D32)
        else          -> Color(0xFFE65100)
    }
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) }, isError = isError,
        supportingText = { if (isError) Text(errorMsg) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = unfocusedBorder, focusedBorderColor = focusedBorder,
            unfocusedLabelColor  = unfocusedBorder, focusedLabelColor  = focusedBorder,
        ),
        modifier = if (focusRequester != null) modifier.focusRequester(focusRequester) else modifier
    )
}

@Composable
private fun QuickChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { option ->
            FilterChip(selected = selected.equals(option, ignoreCase = true),
                onClick = { onSelect(option) },
                label = { Text(option, style = MaterialTheme.typography.labelSmall) })
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
    HorizontalDivider()
}