package com.example.android_finals_test

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.os.Build
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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class InboxActivity: AppCompatActivity() {
    private val logoUrl = " \"https://firebasestorage.googleapis.com/v0/b/t2023it2-hbmc.appspot.com/o/istockphoto-1192999620-612x612.jpg?alt=media&token=27cd9a16-37aa-4c3d-baac-1cad6ea06bf5\"\n"
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val loggedAsAdmin = intent.getBooleanExtra("loggedAsAdmin", false)

        //elements===============================================================
        val email = auth.currentUser?.email.toString()
        setContentView(R.layout.activity_inbox)
        val inboxL = findViewById<LinearLayout>(R.id.inboxList)
        val addInqBtn = findViewById<FloatingActionButton>(R.id.addInquiryButton)
        val backBtn = findViewById<Button>(R.id.backButton)
        //=======================================================================

        //loading messages=======================================================
        if(!loggedAsAdmin){
            db.collection("inbox").document(email).collection("receivedMessages").get().addOnSuccessListener {
                result ->
                if(!result.isEmpty){
                    for(inbox in result){
                        val name = inbox.data["name"].toString()
                        val address = inbox.data["address"].toString()
                        val content = inbox.data["content"].toString()
                        val type = inbox.data["type"].toString()
                        val timestamp = inbox.getTimestamp("timeCreated").toString()


                        val cardV = CardView(this)
                        cardV.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, convert(50, context = this)
                        ).apply { setMargins(30,0,0,30) }
                        val infoL= LinearLayout(this).apply{
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.START
                            layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            setPadding(10)
                        }

                        val imgV = ImageView(this).apply {
                            layoutParams = ViewGroup.LayoutParams(convert(30,context),convert(30,context))

                        }
                        Glide.with(this).load(R.drawable.profile).into(imgV)
                        val nameV = TextView(this).apply {
                            text = "$name:  "
                        }
                        val contentV = TextView(this).apply {
                            text = if(type != "receipt"){content}else{"<<image>>"}
                            maxLines  = 1
                            ellipsize = TextUtils.TruncateAt.END
                        }

                        infoL.addView(imgV); infoL.addView(nameV); infoL.addView(contentV)
                        cardV.addView(infoL); inboxL.addView(cardV)

                        cardV.setOnClickListener {
                            viewFullInquiry(it, name, address, content, type, timestamp)
                        }
                    }
                }
            }
        }

        //=======================================================================


        //click events===========================================================
        addInqBtn.setOnClickListener {
            val intent = Intent(this, InquiryActivity::class.java)
            intent.putExtra("email",email)
            startActivity(intent)
        }
        backBtn.setOnClickListener{
            if(!loggedAsAdmin){
                val intent = Intent(this, HomepageActivity::class.java)
                startActivity(intent)
            }
            else{
                val intent = Intent(this, AdminHomepageActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun viewFullInquiry(view: View, name: String, address: String, content: String,
        type: String, timestamp: String) {
        //popup window=======================================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_full_view_inquiry, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-------------------------------------------------------------------//

        //xml elements=======================================================//
        val nameTxt = pView.findViewById<TextView>(R.id.nameText)
        val adrTxt = pView.findViewById<TextView>(R.id.addressText)
        val backBtn = pView.findViewById<Button>(R.id.backButton)
        val dateTxt = pView.findViewById<TextView>(R.id.dateText)
        val msgL = pView.findViewById<LinearLayout>(R.id.messageContentField)
        //-------------------------------------------------------------------//

        val contentV = TextView(this).apply {
            text = content
        }

        val imgContentV = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        Glide.with(this).load(content).into(imgContentV)

        nameTxt.text = name; adrTxt.text = address; dateTxt.text = timestamp

        if(type == "receipt"){
            msgL.addView(imgContentV)
        }
        else{
            msgL.addView(contentV)
        }
        backBtn.setOnClickListener{
            pWindow.dismiss()
        }
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

    private fun convert(num: Int, context: Context): Int {
        return(num * context.resources.displayMetrics.density).toInt()
    }
}