package com.example.android_finals_test

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class ViewFullInquiryActivity: AppCompatActivity() {
    private val logoUrl = " \"https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5\"\n"
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_inquiry)

        val thisIntent: Intent = intent
        val backBtn = findViewById<Button>(R.id.addBack)

        val name: String = thisIntent.getStringExtra("name").toString()
        val address: String = thisIntent.getStringExtra("address").toString()
        val message: String = thisIntent.getStringExtra("message").toString()
        val nameTxt = findViewById<TextView>(R.id.nameText)
        val addressTxt = findViewById<TextView>(R.id.addressText)
        val msgTxt = findViewById<TextView>(R.id.messageText)

        val nameLabel = nameTxt.text.toString()
        val addressLabel = addressTxt.text.toString()
        nameTxt.text = nameLabel + name
        addressTxt.text = addressLabel  + address
        msgTxt.text = message

        backBtn.setOnClickListener {
            finish()
            val homepageActivity = Intent(this, AdminHomepageActivity::class.java)
            startActivity(homepageActivity)
        }







    }
}