package com.example.imagescalingdemo

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imagescalingdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val selImagesRequestCode = 1
    // Set expected image size to 512 Kb
    val expectedSize = 1024*512
    val logTag = "ImgProcessing"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnSelectImages.setOnClickListener {
                openGalleryForImages()
            }
        }
    }

    private fun openGalleryForImages() {
        var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, selImagesRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == selImagesRequestCode) {
            data?.let { intent ->
                intent.clipData?.let { imgData ->
                    doSerialCompression(imgData)
                    /* TODO -
                      1) реализовать сжатие всех выбранных изображений параллельно, по
                    *,1-й корутине для каждого изображения
                    * 2) Вывести в лог время последовательного сжатия, и время параллельного.
                    * 3) Сравнить полученные результаты для 5, 10 и 20 изображений
                    * 4) Сравнить время работы и количество созданных корутин при запуске в эмуляторе
                    * и на реальном устройстве.
                     */
                }
            }
        }
    }

    private fun doSerialCompression(imgData: ClipData) {
        var count = imgData.itemCount
        Log.d(logTag, "Start compressing $count large images...")
        var compressionTime = measureTimeMillis {
            for(i in 0 until count) {
                var imgUri: Uri = imgData.getItemAt(i).uri
                Log.d(logTag, "Img $i URI: $imgUri")
                runBlocking {
                    compressImage(imgUri)
                }
            }
        }
        Log.d(logTag,"Serial compression time: $compressionTime ms")
    }

    suspend fun compressImage(imgUri: Uri) : Bitmap {
        Log.d(logTag, "Execturing compression from Thread: ${Thread.currentThread().name}")
        var imgOptions = BitmapFactory.Options()
        imgOptions.inJustDecodeBounds = true
        val inputStream = applicationContext.contentResolver.openInputStream(imgUri)
        val byteLength = inputStream?.available()

        val imageBitmap = BitmapFactory.decodeStream(inputStream)

        // Scale image to size < 512 Kb.
        var imgSize = imageBitmap.allocationByteCount
        var scaledImg = imageBitmap
        while(imgSize > expectedSize) {
            scaledImg = resizeImage(scaledImg, 0.5f)
            imgSize = scaledImg.allocationByteCount
        }
        return scaledImg
    }

    // Doesn't work properly! Loses quality, but doesn't reduce image size.
    private fun resizePhoto(bitmap: Bitmap) : Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat()/height
        val newWidth = 640
        val newHeight = (newWidth*aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
    }

    suspend fun resizeImage(bitmap: Bitmap, scaleFactor: Float) : Bitmap {
        val newWidth = bitmap.width * scaleFactor
        val newHeight = bitmap.height * scaleFactor
        var scaledBitmap = Bitmap.createBitmap(newWidth.toInt(), newHeight.toInt(), Bitmap.Config.ARGB_8888)

        val ratioX = newWidth / bitmap.getWidth()
        val ratioY = newHeight / bitmap.getHeight()
        val middleX = newWidth / 2.0f
        val middleY = newHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
                bitmap,
                middleX - bitmap.getWidth() / 2,
                middleY - bitmap.getHeight() / 2,
                Paint(Paint.FILTER_BITMAP_FLAG)
        )
        return scaledBitmap
    }

    private fun splitRowsToTable(bitmapsList: List<Bitmap>) : List<ImageRow> {
        var imageRows = mutableListOf<ImageRow>()
        var rowsAmount = bitmapsList.size / 3
        for (i in 0 until rowsAmount) {
            var firstImgIndex = i * 3
            var imgRow = ImageRow(bitmapsList[firstImgIndex], bitmapsList[firstImgIndex+1], bitmapsList[firstImgIndex+1])
            imageRows.add(imgRow)
        }

        // Fill in last row(may be not full)
        var imagesLeft = bitmapsList.size % 3
        if (imagesLeft > 0) {
            val lastRow = ImageRow()

            lastRow.img1 = bitmapsList[rowsAmount*3]
            if (imagesLeft > 1) {
                lastRow.img2 = bitmapsList[rowsAmount*3 + 1]
            }
        }
        return imageRows
    }
}