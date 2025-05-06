package com.example.android_finals_test

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ViewInquiryActivity: AppCompatActivity() {
    private val logoUrl = " \"https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5\"\n"
    private val menuCollectionName = "inquiry"

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_view_inquiry)
        val db = Firebase.firestore
        val logoutButton = findViewById<Button>(R.id.mainLogoutButton)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        val inquiryL = findViewById<LinearLayout>(R.id.inquiryList)
        Glide.with(this).load(logoUrl).into(mainLogo)

        db.collection(menuCollectionName).get()
            .addOnSuccessListener {result ->
                for (document in result){
                    val name = document.data["name"].toString()
                    val address = document.data["address"].toString()
                    val message = document.data["message"].toString()

                    val linearLayout = LinearLayout(this).apply{
                        orientation = LinearLayout.VERTICAL
                    }
                    linearLayout.setPadding(30, 30, 30, 0)


                    //contents of the inquiry (checkbox for modification, the name of recipient, and message)
                    val checkBox = CheckBox(this)

                    val contentTxt = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                    }
                    val nameTxt = TextView(this).apply {
                        text = name
                        setPadding(0,0,30,0)
                        typeface = Typeface.DEFAULT_BOLD

                    }
                    val messageTxt = TextView(this).apply {
                        text = message

                    }
                    //stores nameTxt and messageTxt to contentTxt, for ease of opening full message
                    contentTxt.addView(nameTxt)
                    contentTxt.addView(messageTxt)

                    linearLayout.addView(checkBox)
                    linearLayout.addView(contentTxt)
                    inquiryL.addView(linearLayout)

                     contentTxt.setOnClickListener {
                        val viewFullInquiryActivity = Intent(this, ViewFullInquiryActivity::class.java)
                        viewFullInquiryActivity.putExtra("name", name)
                        viewFullInquiryActivity.putExtra("address", address)
                        viewFullInquiryActivity.putExtra("message", message)
                        startActivity(viewFullInquiryActivity)
                    }


                }
            }

    }
}