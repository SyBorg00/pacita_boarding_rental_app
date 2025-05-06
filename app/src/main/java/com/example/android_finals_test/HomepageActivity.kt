package com.example.android_finals_test
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class HomepageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val logoUrl ="https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        auth = FirebaseAuth.getInstance()

        val listingButton = findViewById<ImageButton>(R.id.listingButton)
        val inboxButton = findViewById<ImageButton>(R.id.inboxButton)
        val payHistoryButton=findViewById<ImageButton>(R.id.payHistoryButton)
        val profileButton=findViewById<ImageButton>(R.id.profileButton)
        val logoutButton = findViewById<Button>(R.id.mainLogoutButton)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        Glide.with(this).load(logoUrl).into(mainLogo)

        //logout
        logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }

        //access available rooms
        listingButton.setOnClickListener {
            if (auth.currentUser != null) {
                // User is logged in, start ApartmentListActivity
                val intent = Intent(this, ApartmentViewingActivity::class.java)
                intent.putExtra("loggedAsAdmin", false)
                startActivity(intent)
            } else {
                // User is not logged in, start LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        //accessing inquiry ticket
        inboxButton.setOnClickListener {
            val productAddActivity = Intent(this, InboxActivity::class.java)
            startActivity(productAddActivity)
        }

        //access payment options
        payHistoryButton.setOnClickListener {
            val paymentActivity = Intent(this, PaymentHistoryActivity::class.java)
            startActivity(paymentActivity)
        }
        //access user profile
        profileButton.setOnClickListener {
            finish()
            val profileActivity = Intent(this, ProfileActivity::class.java)
            startActivity(profileActivity)
        }

    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}
