package me.ezar.anemon.ui.screens.registration

import android.content.pm.PackageManager
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ezar.anemon.R
import me.ezar.anemon.ui.utils.PreviewBoilerplate
import me.ezar.anemon.ui.utils.SimpleTopBar
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executor


@Composable
fun VehiclePictureScreen(
    registrationViewModel: RegistrationViewModel,
    onBackButton: () -> Unit = {},
    onPhotoTaken: () -> Unit,
) {
    var permissionGranted: Boolean? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionGranted = it
    }

    var showInstructions by remember { mutableStateOf(false) }

    val instructionAlpha by animateFloatAsState(
        targetValue = if (showInstructions) 1f else 0f,
        animationSpec = spring(
            Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 0.01f
        ),
        label = "alpha"
    )
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = ContextCompat.getMainExecutor(ctx)

    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().build()
    }

    Scaffold(
        topBar = {
            SimpleTopBar {
                IconButton(onClick = { onBackButton() }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Kembali"
                    )
                }
                Text(
                    "Ambil Foto Kendaraan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        bottomBar = {
            var loading by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        enabled = !loading,
                        onClick = {
                            loading = true
                            takePhotoInMemory(
                                imageCapture = imageCapture,
                                executor = cameraExecutor,
                                onImageCaptured = { byteArray ->
                                    scope.launch {
                                        // compress
                                        val tempFile = File(
                                            ctx.cacheDir,
                                            System.currentTimeMillis().toString()
                                        )
                                        tempFile.writeBytes(byteArray)
                                        val correctedFile = Compressor.compress(ctx, tempFile) {
                                            size(4_194_304) // ~4MB
                                            format(android.graphics.Bitmap.CompressFormat.JPEG)
                                        }

                                        loading = false
                                        registrationViewModel.vehicleImageBytes =
                                            correctedFile.readBytes()
                                        onPhotoTaken()
                                    }
                                },
                                onError = { error ->
                                    loading = false
                                    Toast.makeText(
                                        ctx,
                                        "Gagal mengambil foto: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }) {
                        if (loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Ambil Foto")
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA)
                .let { permission ->
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = true
                    } else if (permissionGranted == null) {
                        LaunchedEffect(Unit) {
                            launcher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                }

            when (permissionGranted) {
                true -> {
                    LaunchedEffect(Unit) {
                        delay(500)
                        showInstructions = true
                    }
                    Box {
                        CameraPreview(
                            lifecycleOwner = lifecycleOwner,
                            imageCapture = imageCapture
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = instructionAlpha * 80 / 100
                                }) {

                        }
                        InstructionCard(
                            modifier = Modifier.graphicsLayer {
                                alpha = instructionAlpha
                            },
                            onClick = {
                                showInstructions = false
                            }
                        )
                    }
                }

                false -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Izin kamera ditolak.",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(onClick = {
                            launcher.launch(android.Manifest.permission.CAMERA)
                        }) {
                            Text("Berikan Izin")
                        }
                    }
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Meminta izin kamera...")
                    }
                }
            }
        }
    }
}

private fun takePhotoInMemory(
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (ByteArray) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bytes = image.toByteArray()
                onImageCaptured(bytes)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

private fun ImageProxy.toByteArray(): ByteArray {
    val planeProxy = this.planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}


fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture
) {
    val preview: androidx.camera.core.Preview = androidx.camera.core.Preview.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()
        )
        .build()

    val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    preview.surfaceProvider = previewView.surfaceProvider

    cameraProvider.unbindAll()

    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
}

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    imageCapture: ImageCapture
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Spacer(Modifier.height(32.dp))
    AndroidView(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        factory = { ctx ->
            PreviewView(ctx).apply {
                clipToOutline = true
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindCameraUseCases(
                        cameraProvider,
                        lifecycleOwner,
                        this,
                        imageCapture
                    )
                }, ContextCompat.getMainExecutor(ctx))
            }
        }
    )
}

@Composable
fun InstructionCard(onClick: () -> Unit = {}, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxHeight()
    ) {
        Text(
            "Cara Pengambilan Foto",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.padding(16.dp)) {
            InstructionSample(
                painterResource(R.drawable.benar), true, Modifier
                    .fillMaxWidth()
                    .weight(1f), "Foto yang sesuai"
            )
            Spacer(Modifier.width(16.dp))
            InstructionSample(
                painterResource(R.drawable.buram), false, Modifier
                    .fillMaxWidth()
                    .weight(1f), "Foto buram"
            )
        }
        Row(modifier = Modifier.padding(16.dp)) {
            InstructionSample(
                painterResource(R.drawable.tanpaplat), false, Modifier
                    .fillMaxWidth()
                    .weight(1f), "Foto tanpa pelat nomor"
            )
            Spacer(Modifier.width(16.dp))
            InstructionSample(
                painterResource(R.drawable.tanpakendaraan), false, Modifier
                    .fillMaxWidth()
                    .weight(1f), "Foto tanpa kendaraan"
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onClick) {
                Text(
                    "Mengerti",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun InstructionSample(
    painter: Painter,
    correct: Boolean,
    modifier: Modifier = Modifier,
    caption: String
) {
    Column(modifier = modifier) {
        Box(contentAlignment = Alignment.Center) {
            Image(painter = painter, null, modifier = Modifier.clip(RoundedCornerShape(16.dp)))
            if (correct) {
                Image(
                    painter = painterResource(R.drawable.baseline_circle_24),
                    null,
                    colorFilter = ColorFilter.tint(
                        Color.White, blendMode = BlendMode.SrcIn
                    )
                )
                Image(
                    painter = painterResource(R.drawable.baseline_check_circle_48),
                    null,
                    colorFilter = ColorFilter.tint(
                        Color(0xFF29CC54), blendMode = BlendMode.SrcIn
                    )
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.outline_dangerous_24),
                    null,
                    colorFilter = ColorFilter.tint(
                        Color.White, blendMode = BlendMode.SrcIn
                    )
                )
                Image(
                    painter = painterResource(R.drawable.baseline_dangerous_24),
                    null,
                    colorFilter = ColorFilter.tint(
                        Color.Red, blendMode = BlendMode.SrcIn
                    )
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            caption,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview
@Composable
fun InstructionCardPreview() {
    PreviewBoilerplate {
        InstructionCard(modifier = Modifier)
    }
}