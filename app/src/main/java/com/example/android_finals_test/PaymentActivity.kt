package com.example.android_finals_test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class PaymentActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private val logoUrl =
        "https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        //getExtra values===================================================================
        val email = intent.getStringExtra("email").toString()
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        //==================================================================================

        //xml elements======================================================================
        val backButton = findViewById<Button>(R.id.backButton)
        val logoutButton = findViewById<Button>(R.id.mainLogoutButton)
        val bdoButton = findViewById<ImageButton>(R.id.bdoButton)
        val gCashButton = findViewById<ImageButton>(R.id.gcashButton)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        //===================================================================================

        //others=============================================================================
        Glide.with(this).load(logoUrl).into(mainLogo)
        //===================================================================================

        //button events============================================================================
        logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }

        backButton.setOnClickListener {
            finish()
            val back = Intent(this, ProfileActivity::class.java)
            startActivity(back)
        }
        bdoButton.setOnClickListener {
            finish()
            val bdoActivity = Intent(this, BdoActivity::class.java)
            bdoActivity.putExtra("email",email)
            bdoActivity.putExtra("apartmentID", apartmentID)
            startActivity(bdoActivity)
        }
        gCashButton.setOnClickListener {
            finish()
            val gcashActivity = Intent(this, GcashActivity::class.java)
            gcashActivity.putExtra("email",email)
            gcashActivity.putExtra("apartmentID", apartmentID)
            startActivity(gcashActivity)
        }
        //==========================================================================================
    }
}