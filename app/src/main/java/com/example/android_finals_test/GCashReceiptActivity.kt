package com.example.android_finals_test

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class GCashReceiptActivity: AppCompatActivity() {
    private lateinit var getReceipt: ActivityResultLauncher<String>

    private lateinit var auth: FirebaseAuth
    private var receiptURL = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gcash_receipt_scan)

        //initialization======================================//
        val email = intent.getStringExtra("email").toString()
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        //---------------------------------------------------//



        //xml================================================//
        val logoutBtn = findViewById<Button>(R.id.mainLogoutButton)
        val addReceiptBtn = findViewById<ImageView>(R.id.receiptImage)
        val msgET = findViewById<EditText>(R.id.messageField)
        val submitBtn = findViewById<Button>(R.id.submitButton)
        val backBtn = findViewById<Button>(R.id.backButton)
        //---------------------------------------------------//

        //events=============================================//
        getReceipt = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if ( uri != null){
                addReceiptBtn.setImageURI(uri)
                uploadImage(uri)
            }
        }
        println(receiptURL)

        addReceiptBtn.setOnClickListener {
            getReceipt.launch("image/*")
        }
        submitBtn.setOnClickListener {
            val msg = msgET.text.toString()
            if(receiptURL.isNotEmpty() || receiptURL != ""){
                val data = hashMapOf(
                    "receipt" to receiptURL, "type" to "GCash", "message" to msg, "dateOfSubmission" to Timestamp(Date())
                )
                db.collection("account").document(email).collection("currentContracts").document(apartmentID)
                    .collection("transaction").document().set(data).addOnSuccessListener {
                        finish()
                        val intent = Intent(this, GcashActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("apartmentID", apartmentID)
                        startActivity(intent)
                    }
            }
            else Toast.makeText(baseContext,"Please input your receipt", Toast.LENGTH_SHORT).show()
        }

        backBtn.setOnClickListener {
            finish()
            val intent = Intent(this, GcashActivity::class.java)
            startActivity(intent)
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        //---------------------------------------------------//

    }

    private fun uploadImage(receipt: Uri){
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val fileName = "receipts/${System.currentTimeMillis()}.jpg" // Unique file name
        val imageRef = storageRef.child(fileName)
        val uploadTask = imageRef.putFile(receipt)

        uploadTask.addOnSuccessListener { result ->
            imageRef.downloadUrl.addOnSuccessListener { downloadURL ->
                receiptURL = downloadURL.toString()
            }.addOnFailureListener {
                Toast.makeText(baseContext,"Failed to obtain download link", Toast.LENGTH_SHORT).show()

            }
        }.addOnFailureListener{
            Toast.makeText(baseContext,"Failed to upload receipt", Toast.LENGTH_SHORT).show()
        }
    }


}