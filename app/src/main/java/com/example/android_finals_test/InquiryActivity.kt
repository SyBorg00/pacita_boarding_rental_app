package com.example.android_finals_test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Date

class InquiryActivity : AppCompatActivity() {
    private val logoUrl = " \"https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5\"\n"
    private val menuCollectionName = "inquiry"
    private lateinit var auth: FirebaseAuth


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomepageActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inflate_create_inquiry)
        auth = FirebaseAuth.getInstance()
        val email = intent.getStringExtra("email").toString()
        val db = Firebase.firestore
        //xml elements=========================================================
        val nameET = findViewById<EditText>(R.id.addInquiryUserField)
        val addressET = findViewById<EditText>(R.id.addAddressInquiryField)
        val messageET = findViewById<EditText>(R.id.inquiryMessageField)
        val sendInquiryButton = findViewById<Button>(R.id.sendInquiryButton)
        val addBack = findViewById<Button>(R.id.addBack)
        val logoutButton = findViewById<Button>(R.id.mainLogoutButton)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        //======================================================================
        Glide.with(this).load(logoUrl).into(mainLogo)

        //button events====================================================================
        addBack.setOnClickListener {
            finish()
            val intent = Intent(this, InboxActivity::class.java)
            startActivity(intent)
        }
        sendInquiryButton.setOnClickListener {
            val name = nameET.text.toString().trim()
            val address = addressET.text.toString().trim()
            val message = messageET.text.toString().trim()
            if(name.isNotEmpty() && address.isNotEmpty() && message.isNotEmpty()){
                db.collection("inbox").document(email).set("email" to email).addOnSuccessListener {
                    db.collection("inbox").document(email).collection("sentMessages").document()
                        .set(
                            hashMapOf("name" to name,
                                "address" to address,
                                "content" to message,
                                "type" to "inquiry",
                                "isRead" to false,
                                "timeCreated" to Timestamp(Date()))
                        ).addOnSuccessListener {
                            Toast.makeText(baseContext,"Inquiry successfully sent",Toast.LENGTH_SHORT).show()
                            finish()
                            val intent = Intent(this, InboxActivity::class.java)
                            startActivity(intent)
                        }
                }.addOnFailureListener {
                    Toast.makeText(baseContext,"Error sending inquiry, Please try again later",Toast.LENGTH_SHORT).show()
                }

            }
        }

        //logout process
        logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }
        //==================================================================================
    }
}