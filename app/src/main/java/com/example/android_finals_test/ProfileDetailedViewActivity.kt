package com.example.android_finals_test

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.text.Text.Line
import org.w3c.dom.Text
import java.time.LocalDate
import java.util.Locale

class ProfileDetailedViewActivity: AppCompatActivity() {
    private lateinit var documents: List<QueryDocumentSnapshot>
    private var currIndex = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_detailed_profile_rental)

        //val initialization===================================================//
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        val email = intent.getStringExtra("email").toString()
        val photoURL = intent.getStringExtra("apartmentPhoto").toString()
        val address = intent.getStringExtra("address").toString()
        val room = intent.getStringExtra("room").toString()
        val dueDate = intent.getStringExtra("dueDate").toString()
        val outBal = intent.getStringExtra("outstandingBalance").toString().toDoubleOrNull() ?: 0.00
        //---------------------------------------------------------------------//

        val rentalTxt = findViewById<TextView>(R.id.rentalText)
        val dueDateTxt = findViewById<TextView>(R.id.dueDateText)
        val priceTxt = findViewById<TextView>(R.id.outstandingBalanceText)

        val contractDetailBtn = findViewById<LinearLayout>(R.id.contractIcon)
        val soaBtn = findViewById<LinearLayout>(R.id.SOAIcon)
        val transactionBtn = findViewById<LinearLayout>(R.id.paymentIcon)

        rentalTxt.text = address
        dueDateTxt.text = dueDate
        priceTxt.text = String.format(Locale.US, "PHP %.2f", outBal)

        soaBtn.setOnClickListener{
            val intent = Intent(this, ViewAccountStatementActivity::class.java)
            intent.putExtra("email",email)
            intent.putExtra("apartmentID", apartmentID)
            startActivity(intent)
        }
    }

    private fun convert(num: Int, context: Context): Int {
        return(num * context.resources.displayMetrics.density).toInt()
    }

    private fun dimBehind(popupWindow: PopupWindow) {
        val container = popupWindow.contentView.rootView
        val context = popupWindow.contentView.context
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = (container.layoutParams as WindowManager.LayoutParams).apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        windowManager.updateViewLayout(container, layoutParams)
    }
}


