package com.example.android_finals_test

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GcashActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val logoUrl =
        "https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gcash)
        val packageName = "com.globe.gcash.android" // GCash package name
        val packageManager: PackageManager = this.packageManager

        //getExtra values======================================================//
        val email = intent.getStringExtra("email").toString()
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        val dueDate = intent.getStringExtra("dueDate").toString()
        //---------------------------------------------------------------------//

        //xml==================================================================//
        val redirectBtn = findViewById<Button>(R.id.redirectButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val logoutButton = findViewById<Button>(R.id.mainLogoutButton)
        val receiptBtn = findViewById<Button>(R.id.receiptButton)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        Glide.with(this).load(logoUrl).into(mainLogo)
        //---------------------------------------------------------------------//

        //button events========================================================//
        receiptBtn.setOnClickListener {
            finish()
            val intent = Intent(this, GCashReceiptActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("apartmentID", apartmentID)
            startActivity(intent)
        }

        redirectBtn.setOnClickListener {
            try {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                // GCash is installed, open it
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                this.startActivity(intent)

            } catch (e: PackageManager.NameNotFoundException) {
                // GCash is not installed, redirect to Play Store
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                this.startActivity(playStoreIntent)
            }
        }

        logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }

        backButton.setOnClickListener {
            finish()
            val back = Intent(this, PaymentActivity::class.java)
            startActivity(back)
        }
        //---------------------------------------------------------------------//
    }
}