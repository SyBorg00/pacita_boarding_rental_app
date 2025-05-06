package com.example.android_finals_test
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReceiptScanActivity: AppCompatActivity() {
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_scan)
        val imagePickerBtn = findViewById<ImageButton>(R.id.imagePickerButton)

        imagePickerBtn.setOnClickListener{
            getContent.launch("image/*")
        }

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if ( uri != null){
                val bitmap: Bitmap = uriToBitmap(uri)
                findViewById<ImageButton>(R.id.imagePickerButton).setImageURI(uri)
                scanTextFromImage(bitmap)
            } } }

    //convert uri of the image to bitmap to be processed by mlkit
    private fun uriToBitmap(uri: Uri): Bitmap {
        val contentResolver = contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun scanTextFromImage(bitmap: Bitmap){
        val img = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(img)
            .addOnSuccessListener { result ->
                processTextBlock(result)
            }
    }

    private fun processTextBlock(result: Text){
        var text: String
        var hasAmountPaid: Boolean = false
        var hasGCashLogo: Boolean = false
        var hasRefNum: Boolean = false
        var hasAccount: Boolean = false
        for(block in result.textBlocks){
            for(line in block.lines){
                text = line.text
                println(text)
                if(text == "Amount Paid"){
                    hasAmountPaid = true
                }
            }
        }
    }




}