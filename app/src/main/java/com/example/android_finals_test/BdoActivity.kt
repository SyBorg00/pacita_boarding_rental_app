package com.example.android_finals_test

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

class BdoActivity : AppCompatActivity() {

    private val logoUrl =
        "https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gcash)
        val monthlyNotify: SharedPreferences = applicationContext.getSharedPreferences("monthlyNotifyCheck", Context.MODE_PRIVATE)
        val latePayCheck: SharedPreferences = applicationContext.getSharedPreferences("latePayDateCheck", Context.MODE_PRIVATE)

        //getExtra values======================================================//
        val email = intent.getStringExtra("email").toString()
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        val dueDate = intent.getStringExtra("dueDate").toString()
        //---------------------------------------------------------------------//

        //xml==================================================================//
        val directPayBtn = findViewById<Button>(R.id.directPayButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val logoutButton = findViewById<Button>(R.id.mainLogoutButton)
        val receiptBtn = findViewById<Button>(R.id.receiptButton)
        val mainLogo = findViewById<ImageView>(R.id.mainLogo)
        Glide.with(this).load(logoUrl).into(mainLogo)
        //---------------------------------------------------------------------//

        //button events========================================================//
        receiptBtn.setOnClickListener {
            finish()
            val receiptActivity = Intent(this, ReceiptScanActivity::class.java)
            startActivity(receiptActivity)
        }

        directPayBtn.setOnClickListener {
            directPay(it, email, apartmentID, dueDate, monthlyNotify, latePayCheck)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun directPay(view: View, email: String, apartmentID: String, dueDate: String,
                          monthlyNotify: SharedPreferences, latePayCheck: SharedPreferences) {
        //popup window===============================================================================
        val db = Firebase.firestore
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_gcash_transaction, null)
        val pWindow = PopupWindow(pView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.white))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        //===========================================================================================

        //xml elements================================================================================
        val dateTxt = pView.findViewById<TextView>(R.id.dateText)
        val phoneNumET = pView.findViewById<EditText>(R.id.phoneField)
        val amountET = pView.findViewById<EditText>(R.id.amountField)
        val msgET = pView.findViewById<EditText>(R.id.messageField)
        val submitBtn = pView.findViewById<Button>(R.id.submitButton)
        val cancelBtn = pView.findViewById<Button>(R.id.cancelButton)
        val logoutBtn = pView.findViewById<Button>(R.id.mainLogoutButton)
        //============================================================================================

        //===============================================================//
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val currDateString = currentDate.format(formatter)
        dateTxt.text = currDateString
        //---------------------------------------------------------------//

        //button events==================================================//
        submitBtn.setOnClickListener {
            val phoneNum = phoneNumET.text.toString().trim()
            val amount = amountET.text.toString().trim()
            val dueDateLocal = LocalDate.parse(dueDate, formatter)
            val dueYr = dueDateLocal.year.toString(); val dueMonth = dueDateLocal.month.toString() //accessing the set due dates billing

            if(phoneNum.isNotEmpty() && amount.isNotEmpty()){
                val amountNum = amount.toDouble()
                val update = mapOf("outstandingBalance" to 0, "dueDate" to setNewDueDate(dueDate,formatter))
                val updatedBill = mapOf("amountPaid" to amountNum, "hasPaid" to true)
                db.collection("account").document(email).collection("currentContracts").document(apartmentID)
                    .update(update).addOnSuccessListener {
                        monthlyNotify.edit().remove("hasBeenNotified$email$apartmentID").apply()
                        latePayCheck.edit().remove("lastDateOfChecking$email$apartmentID").apply()
                        db.collection("account").document(email).collection("currentContracts").document(apartmentID)
                            .collection("bill").document("$dueMonth-$dueYr").update(updatedBill)
                    }
            }
            else Toast.makeText(baseContext,"Please fill in the necessary fields",Toast.LENGTH_SHORT).show()
        }
        cancelBtn.setOnClickListener { pWindow.dismiss() }
        logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }
        //---------------------------------------------------------------//
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNewDueDate(dueDate: String, formatter: DateTimeFormatter): String{
        val temp = LocalDate.parse(dueDate,formatter)
        val newDueDate = temp.plusDays(30)
        val stringNewDueDate = newDueDate.format(formatter)
        return stringNewDueDate
    }
}