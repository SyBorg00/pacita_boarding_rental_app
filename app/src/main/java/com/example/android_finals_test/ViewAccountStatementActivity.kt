package com.example.android_finals_test

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ViewAccountStatementActivity: AppCompatActivity() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("email").toString()
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        setContentView(R.layout.activity_view_account_statement)

        val SoAL = findViewById<LinearLayout>(R.id.SOAList)
        val backBtn = findViewById<Button>(R.id.backButton)

        db.collection("account").document(email).collection("currentContracts")
            .document(apartmentID).collection("bill").get().addOnSuccessListener { i ->
                if(!i.isEmpty){
                    for(bill in i){
                        val billDate = bill.id
                        val soaTxt = TextView(this).apply {
                            text = billDate
                            textSize = 24F
                            setOnClickListener {
                                viewInDetails(email, apartmentID)
                            }
                        }
                        SoAL.addView(soaTxt)
                    }
                }
                else {
                    val soaTxt = TextView(this).apply {
                        text = String.format("You have no statement of account just yet")
                        textSize = 24F
                        setTextColor(Color.DKGRAY)
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }
                    SoAL.addView(soaTxt)
                }
            }

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun viewInDetails(email: String, apartmentID: String) {

    }
}