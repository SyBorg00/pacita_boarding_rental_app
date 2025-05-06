package com.example.android_finals_test

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.Locale

class PaymentHistoryActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        val db = Firebase.firestore
        val transactionL = findViewById<LinearLayout>(R.id.transactionList)
        findViewById<Button>(R.id.processTransactionButton).visibility = View.GONE
        val backBtn = findViewById<Button>(R.id.backButton)

        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email.toString()

        db.collection("account").document(email).collection("currentContracts").get().addOnSuccessListener {
            result ->
            for(contract in result){
                val apartmentID = contract.id
                val room = contract.data["room"].toString()

                db.collection("account").document(email).collection("currentContracts").document(apartmentID)
                    .collection("transaction").get().addOnSuccessListener { result2 ->
                        for(transaction in result2){
                            val receiptURI = transaction.data["receipt"].toString()
                            val type = transaction.data["type"].toString()
                            val msg = transaction.data["message"].toString()
                            val date = transaction.getTimestamp("dateOfSubmission")
                            val localDate = date?.toDate()
                            val ardentBook = ResourcesCompat.getFont(this, R.font.ardent_sans_book)
                            val cardV = CardView(this).apply {
                                layoutParams = LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(0, 0, 0, resources.getDimensionPixelOffset(R.dimen.card_margin_bottom))
                                    setPadding(convert(20, context),convert(20, context),convert(20, context),convert(20, context))
                                } }

                            val contentL = LinearLayout(this).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                            }

                            val imgCardV = CardView(this).apply {
                                radius = 10F
                                layoutParams = LayoutParams(convert(120, context), convert(120, context)).apply {
                                    setMargins(convert(10, context), convert(10, context), convert(10, context), convert(10, context))
                                }
                            }

                            val imgV = ImageView(this).apply {
                                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }

                            Glide.with(this).load(receiptURI).into(imgV)
                            imgCardV.addView(imgV)

                            val detailL= LinearLayout(this).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                            }

                            val typeV = TextView(this).apply {
                                textSize = 18F
                                text = String.format(Locale.US, "%s receipt Payment", type)
                                typeface = ardentBook
                            }

                            val space = Space(this).apply {
                                layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convert(10,context))
                            }
                            val timeV = TextView(this).apply {
                                textSize = 14F
                                text = localDate.toString()
                                setTextColor(Color.GRAY)
                                typeface = ardentBook
                            }

                            val space2 = Space(this).apply {
                                layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convert(20,context))
                            }

                            val msgV = TextView(this).apply {
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                textSize = 14F
                                text = msg
                                setTextColor(Color.GRAY)
                                typeface = ardentBook
                            }
                            cardV.setOnClickListener {
                                fullReceiptView(it, receiptURI)
                            }

                            detailL.addView(typeV); detailL.addView(space); detailL.addView(timeV); detailL.addView(space2); detailL.addView(msgV)
                            contentL.addView(imgCardV); contentL.addView(detailL)
                            cardV.addView(contentL); transactionL.addView(cardV)
                        }
                    }

            }
        }
        backBtn.setOnClickListener {
            finish()
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

    private fun fullReceiptView(view: View, receiptURI: String) {
        //popup window===============================================================================
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_full_image, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.MATCH_PARENT,true)
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //===========================================================================================
        val receiptImg = pView.findViewById<ImageView>(R.id.receiptImage)
        Glide.with(this).load(receiptURI).into(receiptImg)

    }
}