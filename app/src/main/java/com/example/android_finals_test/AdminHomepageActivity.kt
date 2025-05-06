package com.example.android_finals_test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class AdminHomepageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private val logoUrl =
        "https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_homepage)
        auth = FirebaseAuth.getInstance()

        //elements==============================================================
        val logoutButton = findViewById<Button>(R.id.mainLogoutButton)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        val listingButton = findViewById<ImageButton>(R.id.listingadButton)
        val ticketButton = findViewById<ImageButton>(R.id.ticketadButton)
        val userListButton = findViewById<ImageButton>(R.id.profileadButton)
        val viewStatisticsBtn = findViewById<ImageButton>(R.id.bookReqAdButton)
        //======================================================================
        Glide.with(this).load(logoUrl).into(mainLogo)


        //button events====================================================================
        logoutButton.setOnClickListener {//logs out of app
            Firebase.auth.signOut()
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }
        //redirect to inquiry list
        ticketButton.setOnClickListener {
            if (auth.currentUser != null) {
                // User is logged in, start MainActivity
                val intent = Intent(this, InboxActivity::class.java)
                intent.putExtra("loggedAsAdmin", true)
                startActivity(intent)
            } else {
                // User is not logged in, start LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        userListButton.setOnClickListener{
            if (auth.currentUser != null) {
                // User is logged in, start BookRequestActivity
                val intent = Intent(this, AdminUserListActivity::class.java)
                startActivity(intent)
            } else {
                // User is not logged in, start LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        listingButton.setOnClickListener{//redirect to apartment management
            if (auth.currentUser != null) {

                val intent = Intent(this, ApartmentViewingActivity::class.java)
                intent.putExtra("loggedAsAdmin", true)
                startActivity(intent)
            } else {
                // User is not logged in, start LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        viewStatisticsBtn.setOnClickListener{//redirect to apartment management
            if (auth.currentUser != null) {
                val intent = Intent(this, AdminViewStatisticsActivity::class.java)
                startActivity(intent)
            } else {
                // User is not logged in, start LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}