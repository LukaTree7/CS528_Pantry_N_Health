package com.example.afinal

import LocalAppState
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.afinal.ml.MobilenetFreshness
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@Composable
fun ClassifyScreen() {
    val context = LocalContext.current
    val appState = LocalAppState.current

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // Permissions granted
        } else {
            // Permissions denied
            Log.e("Camera", "Camera permissions denied")
        }
    }

    val hasPermission = CAMERA_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(CAMERA_PERMISSIONS)
        }
    }

    if (!hasPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission required")
        }
        return
    }

    MaterialTheme(
        colorScheme = if (appState.isDarkMode) darkColorScheme() else lightColorScheme()
    ) {
        val controller = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            }
        }

        val capturedImage = remember {
            mutableStateOf<Bitmap?>(null)
        }

        val predictionResult = remember {
            mutableStateOf<String?>(null)
        }
        Column (
            modifier = Modifier
                .padding(PaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                contentAlignment = Alignment.Center
            ){
                if (capturedImage.value != null) {
                    Image(
                        bitmap = capturedImage.value!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    CameraPreview(
                        controller = controller,
                        modifier = Modifier
                            .fillMaxSize()
                            .height(500.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Detect how edible is your food")

            predictionResult.value?.let { result ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = result,
                    color = if (appState.isDarkMode) Color.White else Color.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
//-------------------------------------------------------------------------
            if (capturedImage.value != null) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        capturedImage.value = null
                        predictionResult.value = null
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                ) {
                    Text(
                        "Try Another", fontSize = 16.sp
                    )
                }
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        takePhoto(
                            controller = controller,
                            onPhotoTaken = { bitmap ->
                                capturedImage.value = bitmap
                                predictionResult.value = outputGenerator(context, bitmap)
                            },
                            context = context
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Detect", fontSize = 16.sp)
                }
            }
//----------------------------------------------------------------------
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                onClick = {
//                    takePhoto(
//                        controller = controller,
//                        onPhotoTaken = { bitmap ->
//                            capturedImage.value = bitmap
//                            predictionResult.value = outputGenerator(context, bitmap)
//                        },
//                        context = context
//                    )
//                },
//                shape = RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary
//                )
//            ) {
//                Text("Detect", fontSize = 16.sp)
//            }


        }
    }
}

private fun outputGenerator(context: Context, bitmap: Bitmap): String {
    try {
        // Resize and convert bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        // Initialize model
        val model = MobilenetFreshness.newInstance(context)

        // Convert bitmap to ByteBuffer with proper normalization
        val inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).apply {
            order(ByteOrder.nativeOrder())
            val pixels = IntArray(224 * 224)
            resizedBitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)
            for (pixel in pixels) {
                putFloat(((pixel shr 16) and 0xFF) / 255.0f) // R
                putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // G
                putFloat((pixel and 0xFF) / 255.0f)          // B
            }
        }

        // Create input tensor
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(inputBuffer)

        // Run inference
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        // Get probabilities
        val freshProb = outputFeature0[0]
        val rottenProb = outputFeature0[1]

        model.close()

        return if (freshProb > rottenProb) {
            "Fresh (${"%.1f%%".format(freshProb * 100)})"
        } else {
            "Rotten (${"%.1f%%".format(rottenProb * 100)})"
        }
    } catch (e: Exception) {
        Log.e("Classification", "Error during inference", e)
        return "Error: ${e.message}"
    }
}

@Throws(IOException::class)
private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelFile)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}

private fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val rotationDegrees = image.imageInfo.rotationDegrees
                val originalBitmap = image.toBitmap()

                val matrix = android.graphics.Matrix().apply {
                    postRotate(rotationDegrees.toFloat())
                }

                val rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    0,
                    0,
                    originalBitmap.width,
                    originalBitmap.height,
                    matrix,
                    true
                )

                image.close()
                onPhotoTaken(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo", exception)
            }
        }
    )
}

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

private val CAMERA_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA
)
