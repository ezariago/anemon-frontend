package me.ezar.anemon.ui.screens.registration.dialogs

import android.annotation.SuppressLint
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import me.ezar.anemon.data.repository.UserRepository
import me.ezar.anemon.ui.screens.registration.RegistrationViewModel
import java.nio.ByteBuffer
import java.util.concurrent.Executor

@Composable
fun ProfilePicturePickerDialog(
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

    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = ContextCompat.getMainExecutor(ctx)

    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().build()
    }

    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Dialog(onDismissRequest = onBackButton) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Foto Profil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
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
                        ProfilePictureCameraPreview(
                            lifecycleOwner = lifecycleOwner,
                            imageCapture = imageCapture
                        )

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
                                            val tempFile =
                                                ctx.cacheDir.resolve("profile_picture_temp.jpg")
                                            tempFile.writeBytes(byteArray)

                                            val cropped = Compressor.compress(
                                                ctx, tempFile,
                                                scope.coroutineContext
                                            ) {
                                                default(width = 1280, height = 1280)
                                                size(2_097_152) // 2MB
                                            }

                                            registrationViewModel.profilePictureBytes =
                                                cropped.readBytes()
                                            loading = false
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Meminta izin kamera...")
                        }
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
                val byteArray = image.toByteArray()
                onImageCaptured(byteArray)
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
        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
        .build()

    preview.surfaceProvider = previewView.surfaceProvider

    cameraProvider.unbindAll()

    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
}

@Composable
fun ProfilePictureCameraPreview(
    lifecycleOwner: LifecycleOwner,
    imageCapture: ImageCapture
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    AndroidView(
        modifier = Modifier
            .clip(CircleShape)
            .aspectRatio(1f)
            .border(8.dp, MaterialTheme.colorScheme.primary, CircleShape),
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

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun ProfilePicturePickerDialogPreview() {
    ProfilePicturePickerDialog(
        registrationViewModel = RegistrationViewModel(UserRepository()),
        onBackButton = {},
        onPhotoTaken = {}
    )
}