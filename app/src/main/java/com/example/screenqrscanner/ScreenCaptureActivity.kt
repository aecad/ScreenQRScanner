package com.example.screenqrscanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenCaptureActivity : Activity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val scanner: BarcodeScanner = BarcodeScanning.getClient()

    companion object {
        const val REQUEST_CODE_SCREEN_CAPTURE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        // 请求屏幕捕获权限
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                captureScreen(resultCode, data)
            } else {
                Toast.makeText(this, "需要屏幕录制权限才能扫码", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun captureScreen(resultCode: Int, data: Intent) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val density = displayMetrics.densityDpi

        // 创建 ImageReader 用于捕获屏幕
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        // 延迟一点确保画面已捕获
        Handler(Looper.getMainLooper()).postDelayed({
            processCapturedImage()
        }, 500)
    }

    private fun processCapturedImage() {
        val image = imageReader?.acquireLatestImage()
        
        if (image != null) {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            // 创建 Bitmap
            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            
            // 裁剪到实际屏幕大小
            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
            bitmap.recycle()
            
            image.close()
            
            // 识别二维码
            scanQRCode(croppedBitmap)
        } else {
            Toast.makeText(this, "截图失败，请重试", Toast.LENGTH_SHORT).show()
            cleanup()
            finish()
        }
    }

    private fun scanQRCode(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    handleScanResult(barcodes[0])
                } else {
                    Toast.makeText(this, "未检测到二维码", Toast.LENGTH_SHORT).show()
                }
                bitmap.recycle()
                cleanup()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "识别失败: ${e.message}", Toast.LENGTH_SHORT).show()
                bitmap.recycle()
                cleanup()
                finish()
            }
    }

    private fun handleScanResult(barcode: Barcode) {
        val value = barcode.rawValue
        if (value != null) {
            // 显示结果对话框
            val intent = Intent(this, ScanResultActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("scan_result", value)
                putExtra("barcode_type", barcode.valueType)
            }
            startActivity(intent)
        }
    }

    private fun cleanup() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }
}
