package com.example.android_finals_test

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
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
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdminEditTenantRentalActivity: AppCompatActivity() {
    private lateinit var documents: List<QueryDocumentSnapshot>
    private val db = Firebase.firestore
    private lateinit var equipments: HashMap<String, Any>
    private lateinit var billStore: HashMap<String, Any>
    private var currIndex = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tenant_rental)

        //xml==============================================//
        val apartmentImg = findViewById<ImageView>(R.id.apartmentImage)
        val rentTxt = findViewById<TextView>(R.id.rentalText)
        val dueDateTxt = findViewById<TextView>(R.id.dueDateText)
        val payTxt = findViewById<TextView>(R.id.monthlyPayText)
        val outBalTxt = findViewById<TextView>(R.id.outstandingBalanceText)
        val billTxt = findViewById<TextView>(R.id.billInfo)
        val oldBalTxt = findViewById<TextView>(R.id.oldBalanceText)
        val otherL = findViewById<LinearLayout>(R.id.othersList)
        val penaltyL = findViewById<LinearLayout>(R.id.penaltiesList)
        val amountPaidTxt = findViewById<TextView>(R.id.amountPaidText)
        val totalPayTxt = findViewById<TextView>(R.id.totalPaymentText)
        val prevBtn = findViewById<Button>(R.id.previousButton)
        val nextBtn = findViewById<Button>(R.id.nextButton)
        val equipLayout = findViewById<LinearLayout>(R.id.equipmentLayout)
        val equipL = findViewById<LinearLayout>(R.id.equipmentList)
        val backBtn = findViewById<Button>(R.id.backButton)
        val manageBillBtn = findViewById<TextView>(R.id.manageBillButton)
        val viewTransactBtn = findViewById<TextView>(R.id.viewTransactionButton)
        //-------------------------------------------------//

        //initialization====================================//
        val apartmentID = intent.getStringExtra("apartmentID").toString()
        val email = intent.getStringExtra("email").toString()
        val dueDate = intent.getStringExtra("dueDate").toString().trim()
        val apartImgURI = intent.getStringExtra("apartmentImage").toString()
        val outBal = intent.getStringExtra("outstandingBalance").toString().toDoubleOrNull()?: 0.00
        val address = intent.getStringExtra("address").toString()

        rentTxt.text = String.format(Locale.US, address)
        dueDateTxt.text = String.format(Locale.US, dueDate)
        outBalTxt.text = String.format(Locale.US,"PHP %.2f", outBal)
        Glide.with(this).load(apartImgURI).placeholder(R.drawable.room).into(apartmentImg)
        //-------------------------------------------------//

        //loading equipments===============================//
        db.collection("account").document(email).collection("currentContracts").document(apartmentID)
            .get().addOnSuccessListener { result ->
            equipments = result["equipments"] as HashMap<String, Any>
            billStore = result["billStore"] as HashMap<String, Any>

            if(equipments.isNotEmpty()){
                for(i in equipments.keys){
                    val equipSet  = equipments[i] as HashMap<*,*>
                    equipSet.let {
                        val desc = it["description"].toString()
                        val img = it["image"].toString()
                        val type = it["type"].toString()
                        val qty = it["quantity"].toString().toIntOrNull()?: 0

                        val cardV = CardView(this).apply {
                            layoutParams = ActionBar.LayoutParams(
                                convert(120, context), ActionBar.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(convert(5,context),0,convert(5,context),0)
                            }
                            cardElevation = 4F
                        }
                        val infoL = LinearLayout(this).apply {
                            layoutParams = ActionBar.LayoutParams(
                                ActionBar.LayoutParams.MATCH_PARENT,
                                ActionBar.LayoutParams.MATCH_PARENT
                            )
                            gravity = Gravity.CENTER_HORIZONTAL
                            orientation = LinearLayout.VERTICAL
                            setPadding(resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                                resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y),
                                resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_x),
                                resources.getDimensionPixelOffset(R.dimen.cardLinearLayout_padding_y))
                        }
                        val imgV = ImageView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(convert(90, context), convert(90,context))
                        }
                        Glide.with(this).load(R.drawable.furniture_icon).into(imgV)
                        val nameV = TextView(this).apply {
                            text = desc
                        }
                        cardV.setOnClickListener {
                            viewEquipment(it, desc, type, qty)
                        }

                        infoL.addView(imgV); infoL.addView(nameV); cardV.addView(infoL)
                        equipL.addView(cardV)
                    }
                }
            } else equipLayout.visibility = View.GONE

            db.collection("account").document(email).collection("currentContracts").document(apartmentID)
            .collection("bill").get().addOnSuccessListener { querySnapshot ->
                  documents = querySnapshot.documents as List<QueryDocumentSnapshot>
                  if(documents.isNotEmpty()){
                        traverseBilling(documents[currIndex], payTxt, oldBalTxt, otherL, penaltyL, totalPayTxt,
                            amountPaidTxt, billTxt)
                  }
            }
        }
        //button events========================================================//
        prevBtn.setOnClickListener {
            if(::documents.isInitialized && currIndex > 0){
                currIndex --
                prevBtn.visibility = if(currIndex==1){View.GONE}else{View.VISIBLE}
                penaltyL.removeAllViews()
                otherL.removeAllViews()
                traverseBilling(documents[currIndex], payTxt, oldBalTxt, otherL, penaltyL,
                    totalPayTxt, amountPaidTxt, billTxt)
            }
        }
        nextBtn.setOnClickListener {
            if(::documents.isInitialized && currIndex < documents.size-1){
                currIndex ++
                penaltyL.removeAllViews()
                otherL.removeAllViews()
                nextBtn.visibility = if(currIndex==documents.size-1){View.GONE}else{View.VISIBLE}
                traverseBilling(documents[currIndex], payTxt, oldBalTxt, otherL,
                    penaltyL, totalPayTxt, amountPaidTxt, billTxt)
            }
        }
        backBtn.setOnClickListener {
            finish()
        }

        viewTransactBtn.setOnClickListener {
            val intent = Intent(this, ViewTransactionActivity::class.java)
            intent.putExtra("loggedAsAdmin", true)
            intent.putExtra("email", email)
            intent.putExtra("dueDate", dueDate)
            intent.putExtra("apartmentID", apartmentID)
            startActivity(intent)
        }

        manageBillBtn.setOnClickListener {
            manageBilling(it,email, apartmentID, otherL, penaltyL)
        }
        //-------------------------------------------------//
    }

    private fun manageBilling(view: View, email: String, apartmentID: String, otherL: LinearLayout, penaltyL: LinearLayout) {

        //popup window==============================================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_manage_billing, null)
        val pWindow = PopupWindow(pView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-------------------------------------------------------------------------//

        for(i in billStore.keys){
            val innerMap = billStore[i] as HashMap<*,*>
            innerMap.let {
                val desc = it["description"].toString()
                val type = it["type"].toString()
                val price = it["price"].toString().toDoubleOrNull()?: 0.00

                val mainL = LinearLayout(this).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                }

                val descV = TextView(this).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    text = String.format("%s %20s", desc, String.format(Locale.US, "PHP %.2f", price))
                }

                val xBtn = TextView(this).apply {
                    text = "X"
                    textSize = 20F
                    setOnClickListener {
                        billStore.remove(desc)
                        if(type == "others"){
                            otherL.removeView(mainL)
                        }
                        else{
                            penaltyL.removeView(mainL)
                        }
                    }
                }
                mainL.addView(descV); mainL.addView(xBtn)
                if(type == "others"){
                    otherL.addView(mainL)
                }
                else{
                    penaltyL.addView(mainL)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun traverseBilling(billings: QueryDocumentSnapshot, initPayTxt: TextView, oldBalTxt: TextView,
        otherL: LinearLayout, penaltyL: LinearLayout, totalPayTxt: TextView, amountPaidTxt: TextView, billTxt: TextView) {

        val initialPay = billings["initialPayment"].toString().toDoubleOrNull()?: 0.00
        val billDue = billings["dueDate"].toString()
        val oldBal = billings["oldBalance"].toString().toDoubleOrNull()?: 0.00
        val amountPaid = billings["amountPaid"].toString().toDoubleOrNull()?: 0.00
        val others = billings["others"] as HashMap<String, Any>
        var penaltyPrice = 0.00
        var otherPrice = 0.00

        initPayTxt.text = String.format(Locale.US, "PHP %.2f", initialPay)
        oldBalTxt.text = String.format(Locale.US, "PHP %.2f", oldBal)
        billTxt.text = String.format(Locale.US, billDue)


        if(others.isNotEmpty()){
            for(i in others.keys){
                val innerMap = others[i] as HashMap<*,*>
                innerMap.let {
                    val desc = it["description"].toString()
                    val type = it["type"].toString()
                    val price = it["price"].toString().toDoubleOrNull()?: 0.00
                    penaltyPrice += price

                    val billV = TextView(this).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {

                        }
                        text = String.format(Locale.US, "%s: %10s", desc, String.format(Locale.US, "%.2f", price))
                    }
                    if(type == "others"){
                        otherPrice += price
                        otherL.addView(billV)
                    }
                    else{
                        penaltyPrice += price
                        penaltyL.addView(billV)
                    }
                }
            }
        } else otherL.visibility = View.GONE; penaltyL.visibility = View.GONE
        amountPaidTxt.text = String.format(Locale.US, "PHP %.2f", amountPaid)
        totalPayTxt.text = String.format(Locale.US, "PHP %.2f", ((initialPay + oldBal + penaltyPrice + otherPrice)-amountPaid))
    }

    private fun viewEquipment(view: View, desc: String, type: String, qty: Int) {
        //popup window==============================================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_view_equipment_details, null)
        val pWindow = PopupWindow(pView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-------------------------------------------------------------------------//

        //xml======================================================================//
        val xTxt = pView.findViewById<TextView>(R.id.xText)
        val equipImg = pView.findViewById<ImageView>(R.id.equipmentImage)
        val typeTxt = pView.findViewById<TextView>(R.id.equipType)
        val descTxt = pView.findViewById<TextView>(R.id.descriptionText)
        val qtyTxt = pView.findViewById<TextView>(R.id.quantityText)
        //-------------------------------------------------------------------------//

        //init======================================================================//
        Glide.with(this).load("").placeholder(R.drawable.img).into(equipImg)
        typeTxt.text = String.format(Locale.US, "Equipment Type: %15s", type)
        descTxt.text = desc
        qtyTxt.text = String.format(Locale.US, "Quantity: 15%d", qty)
        //-------------------------------------------------------------------------//
        xTxt.setOnClickListener { pWindow.dismiss() }
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