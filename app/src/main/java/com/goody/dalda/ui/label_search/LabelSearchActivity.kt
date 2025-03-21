package com.goody.dalda.ui.label_search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.goody.dalda.R
import com.goody.dalda.base.BaseActivity
import com.goody.dalda.databinding.ActivityLabelSearchBinding
import com.goody.dalda.extention.cropBitmap
import com.goody.dalda.extention.resizeWidth
import com.goody.dalda.extention.rotate
import com.goody.dalda.extention.toBitmap
import com.goody.dalda.ui.custom.GraphicOverlay
import com.goody.dalda.ui.custom.TextGraphic
import com.goody.dalda.ui.dialog.NoResultsDialog
import com.goody.dalda.ui.dialog.SearchResultsDialog
import com.goody.dalda.ui.state.UiState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class LabelSearchActivity : BaseActivity<ActivityLabelSearchBinding>() {
    private val viewModel: LabelSearchViewModel by viewModels()
    private var imageCapture: ImageCapture? = null
    private lateinit var photoFile: File
    private lateinit var cameraExecutor: ExecutorService

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                val inputImage = InputImage.fromFilePath(baseContext, uri)
                runTextRecognition(inputImage)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true

            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    permissionGranted = false
                }
            }

            if (permissionGranted) {
                startCamera()
            } else {
                Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
            }
        }

    override val bindingInflater: (LayoutInflater) -> ActivityLabelSearchBinding
        get() = ActivityLabelSearchBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        createCacheFile()
        setupCaptureClickListener()
        setupAlbumClickListener()
        setupToolbar()
        subscribe()
    }

    private fun subscribe() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is UiState.Uninitialized -> {}
                is UiState.Empty -> {
                    showEmptyDialog()
                }

                is UiState.Error -> {}
                is UiState.Loading -> {}
                is UiState.Success -> {
                    val bottomSheet = SearchResultsDialog(state.data)
                    bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                }
            }
        }
    }

    private fun showEmptyDialog() {
        val dialog = NoResultsDialog(this)
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        photoFile.deleteOnExit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (android.R.id.home == item.itemId) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.labelSearchToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_left)
    }

    private fun createCacheFile() {
        val name =
            SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA)
                .format(System.currentTimeMillis())
        photoFile = File(cacheDir, "$name.jpg")
    }

    private fun setupCaptureClickListener() {
        binding.imageCaptureButton.setOnClickListener {
            binding.imageCaptureButton.isEnabled = false
            photoFile.delete()

            if (imageCapture == null) {
                binding.imageCaptureButton.isEnabled = true
            } else {
                takePhoto(imageCapture!!)
            }
        }
    }

    private fun setupAlbumClickListener() {
        binding.labelSearchAlbumBtn.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun takePhoto(imageCapture: ImageCapture) {
        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    binding.imageCaptureButton.isEnabled = true
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val bitmap =
                        image.toBitmap()
                            .rotate(image.imageInfo.rotationDegrees.toFloat())
                            .resizeWidth(binding.viewFinder)
                            .cropBitmap(binding.viewFinder, binding.labelSearchGuideBox)

                    image.close()

                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    runTextRecognition(inputImage)
                }
            },
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview =
                Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Crop rect
            val cropWidth = binding.viewFinder.width
            val cropHeight = binding.viewFinder.height
            val cropRotation = ContextCompat.getDisplayOrDefault(this).rotation

            val viewPort = ViewPort.Builder(Rational(cropWidth, cropHeight), cropRotation).build()
            val useCaseGroup =
                UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture!!)
                    .setViewPort(viewPort)
                    .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    useCaseGroup,
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() =
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it,
            ) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * 텍스트 인식 감지기를 구성하고 processTextRecognitionResult 응답으로 함수를 호출
     */
    private fun runTextRecognition(image: InputImage) {
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                binding.imageCaptureButton.isEnabled = true
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                binding.imageCaptureButton.isEnabled = true
                e.printStackTrace()
            }
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(baseContext, "No text found", Toast.LENGTH_SHORT).show()
            showEmptyDialog()
            binding.imageCaptureButton.isEnabled = true
            return
        }

        showRecognitionResultOverlay(blocks)

        val pieces = getRecognitionResultPieceList(blocks)
        pieces.sortWith(Piece.HeightComparator)
        val searchText = pieces.take(2).joinToString(separator = " ") { it.text }
        viewModel.requestSearchApi(searchText)
    }

    private fun getRecognitionResultPieceList(blocks: List<Text.TextBlock>): MutableList<Piece> {
        val pieces = mutableListOf<Piece>()

        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val rect = lines[j].boundingBox
                if (rect != null) {
                    val height = rect.bottom - rect.top
                    pieces.add(Piece(lines[j].text, height))
                }
            }
        }
        return pieces
    }

    private fun showRecognitionResultOverlay(blocks: List<Text.TextBlock>) {
        binding.graphicOverlay.clear()

        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                val rect = lines[j].boundingBox

                if (rect != null) {
                    val height = rect.bottom - rect.top
                    Log.d(
                        TAG,
                        "kch [" + lines[j].text + "] rect Height : " + height,
                    )
                }

                for (k in elements.indices) {
                    val textGraphic: GraphicOverlay.Graphic =
                        TextGraphic(binding.graphicOverlay, elements[k])
                    binding.graphicOverlay.add(textGraphic)

                    Log.d(TAG, "kch [" + lines[j].text + "] element : " + elements[k].text)
                }
            }
        }
    }

    data class Piece(val text: String, val height: Int) {
        object HeightComparator : Comparator<Piece> {
            override fun compare(
                o1: Piece,
                o2: Piece,
            ): Int {
                return o2.height.compareTo(o1.height)
            }
        }
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
