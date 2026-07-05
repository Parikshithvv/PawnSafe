package com.pawnsafe.presentation.pledge.screens

import android.util.Log
import android.view.ViewTreeObserver
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pawnsafe.core.utils.OcrHelper
import com.pawnsafe.core.utils.OcrResult
import com.pawnsafe.core.utils.OcrResultHolder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTicketScreen(
    onBack: () -> Unit,
    onScanned: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var scanStatus     by remember { mutableStateOf("Hold camera steady over the ticket") }
    val hasScanned     = remember { AtomicBoolean(false) }
    val firstTextSeenAt = remember { AtomicLong(0L) }
    val STABLE_MS = 1500L

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            recognizer.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType          = PreviewView.ScaleType.FILL_CENTER
                    layoutParams       = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                previewView.viewTreeObserver.addOnGlobalLayoutListener(
                    object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            previewView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            if (previewView.width == 0 || previewView.height == 0) return

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                            if (!hasScanned.get()) {
                                                analyzeFrame(
                                                    imageProxy      = imageProxy,
                                                    recognizer      = recognizer,
                                                    hasScanned      = hasScanned,
                                                    firstTextSeenAt = firstTextSeenAt,
                                                    stableMs        = STABLE_MS,
                                                    onStatusUpdate  = { msg ->
                                                        ContextCompat.getMainExecutor(ctx)
                                                            .execute { scanStatus = msg }
                                                    },
                                                    onResult = { result ->
                                                        OcrResultHolder.set(result)
                                                        ContextCompat.getMainExecutor(ctx)
                                                            .execute { onScanned() }
                                                    },
                                                    onError = { e ->
                                                        Log.e("ScanTicket", "OCR error", e)
                                                    }
                                                )
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalyzer
                                    )
                                } catch (e: Exception) {
                                    scanStatus = "Camera error: ${e.message}"
                                    Log.e("ScanTicket", "Camera bind failed", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                        }
                    }
                )
                previewView
            }
        )

        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint               = androidx.compose.ui.graphics.Color.White
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shape    = MaterialTheme.shapes.medium
        ) {
            Text(
                text     = scanStatus,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style    = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyzeFrame(
    imageProxy:      ImageProxy,
    recognizer:      com.google.mlkit.vision.text.TextRecognizer,
    hasScanned:      AtomicBoolean,
    firstTextSeenAt: AtomicLong,
    stableMs:        Long,
    onStatusUpdate:  (String) -> Unit,
    onResult:        (OcrResult) -> Unit,
    onError:         (Exception) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) { imageProxy.close(); return }

    try {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text.trim()

                if (text.length < 20) {
                    firstTextSeenAt.set(0L)
                    onStatusUpdate("Hold camera steady over the ticket")
                } else {
                    val now   = System.currentTimeMillis()
                    val first = firstTextSeenAt.get()

                    if (first == 0L) {
                        firstTextSeenAt.set(now)
                        onStatusUpdate("Keep steady… reading ticket")
                    } else if (now - first >= stableMs) {
                        if (hasScanned.compareAndSet(false, true)) {
                            val result = OcrHelper.extractFields(visionText)
                            onStatusUpdate("Ticket scanned!")
                            onResult(result)
                        }
                    } else {
                        val pct = ((now - first) * 100 / stableMs).toInt().coerceIn(0, 99)
                        onStatusUpdate("Keep steady… $pct%")
                    }
                }
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                firstTextSeenAt.set(0L)
                onError(e)
                imageProxy.close()
            }
    } catch (e: Exception) {
        onError(e)
        imageProxy.close()
    }
}