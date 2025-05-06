package com.example.android_finals_test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.Date

class RentApartmentActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var payType = ""
    private var payURI = ""
    private var idURI = ""
    private lateinit var getIDImage: ActivityResultLauncher<String>
    private lateinit var getPayImage: ActivityResultLauncher<String>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_apartment2)
        createNotificationChannel()
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email.toString()

        //elements===================================================//
        val lNameET = findViewById<EditText>(R.id.lnameField)
        val fNameET = findViewById<EditText>(R.id.fnameField)
        val numField = findViewById<EditText>(R.id.contactNumField)
        val birthDateET = findViewById<EditText>(R.id.birthDateField)
        val occupationET = findViewById<EditText>(R.id.occupationField)
        val adrTxt = findViewById<TextView>(R.id.addressText)
        val roomTxt = findViewById<TextView>(R.id.roomText)
        val priceTxt = findViewById<TextView>(R.id.priceText)
        val gcashCheck = findViewById<CheckBox>(R.id.gcashCheckBox)
        val bdoCheck = findViewById<CheckBox>(R.id.bdoCheckBox)
        val addID = findViewById<FrameLayout>(R.id.addID)
        val idImg = findViewById<ImageView>(R.id.IDImage)
        val addPay = findViewById<FrameLayout>(R.id.addPay)
        val payLaterCheck = findViewById<CheckBox>(R.id.payLaterCheck)
        val payImg = findViewById<ImageView>(R.id.paymentImage)
        val equipCheck = findViewById<CheckBox>(R.id.equipmentChecker)
        val equipBtn = findViewById<ImageView>(R.id.addEquipImage)
        val equipL = findViewById<LinearLayout>(R.id.equipmentList)
        val submitBtn = findViewById<Button>(R.id.submitButton)
        val cancelBtn = findViewById<Button>(R.id.cancelButton)
        val termCheck = findViewById<CheckBox>(R.id.termsCheck)
        //-----------------------------------------------------------//

        //load existing account data into the text fields for automation
        db.collection("account").document(email).get().addOnSuccessListener { result ->
            if (result.exists()){
                lNameET.isFocusable = false; fNameET.isFocusable = false
                numField.isFocusable = false; birthDateET.isFocusable = false
                occupationET.isFocusable = false

                val lName = result.data?.get("lastName").toString()
                val fName = result.data?.get("firstName").toString()
                val contactNum = result.data?.get("contactNum").toString()
                val birthDate = result.data?.get("birthDate").toString()
                val occupation = result.data?.get("occupation").toString()
                lNameET.setText(lName); fNameET.setText(fName)
                numField.setText(contactNum); birthDateET.setText(birthDate); occupationET.setText(occupation)
            }
        }
        //-----------------------------------------------------------//

        //data exported from ApartmentViewingActivity==================
        val thisIntent: Intent = intent
        val apartmentID = thisIntent.getStringExtra("addressID").toString()
        val apartmentAddress = thisIntent.getStringExtra("address").toString()
        val apartmentRoom = thisIntent.getStringExtra("roomNum").toString().trim()
        val apartmentType = thisIntent.getStringExtra("type").toString()
        val apartmentPrice = thisIntent.getStringExtra("price").toString()
        val equipmentList = HashMap<String, HashMap<String, Any>>()
        //=============================================================

        //initialization==========================================
        equipL.visibility = View.GONE
        adrTxt.text = apartmentAddress
        roomTxt.text = apartmentRoom.ifEmpty { "--" }
        priceTxt.text = apartmentPrice
        //========================================================

        //button events=============================================
        getIDImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null){
                idImg.setImageURI(uri)
                idURI = uri.toString()
            } }

        getPayImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null){
                payImg.setImageURI(uri)
                payURI = uri.toString()
            } }

        addID.setOnClickListener{ getIDImage.launch("image/*") }
        addPay.setOnClickListener{getPayImage.launch("image/*") }

        gcashCheck.setOnClickListener { bdoCheck.isChecked = false; payType = "GCash" }
        bdoCheck.setOnClickListener { gcashCheck.isChecked = false; payType = "BDO" }
        equipCheck.setOnClickListener{ equipL.visibility = if(equipCheck.isChecked){View.VISIBLE}else{View.GONE} }
        equipBtn.setOnClickListener { addEquipment(it, equipL, equipmentList) }
        cancelBtn.setOnClickListener { finish() }

        numField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (isValidContactNumber(input)) {
                    numField.error = null // Clear the error if valid
                } else {
                    numField.error = "Invalid contact number"
                } }
            override fun afterTextChanged(s: Editable?) {}
        })

        //handling submission========================================//
        submitBtn.setOnClickListener {
            val fname = fNameET.text.toString(); val lname = lNameET.text.toString()
            val contactNum = numField.text.toString(); val birthDate = birthDateET.text.toString()
            val occupation = occupationET.text.toString()
            if(fname != "" && lname != "" && contactNum != "" && birthDate != "" && occupation != ""){
                val priceNum = apartmentPrice.toFloatOrNull()?: 0.0f
                if(gcashCheck.isChecked || bdoCheck.isChecked){
                    if((payURI != "" || payLaterCheck.isChecked) && idURI != ""){
                        if(termCheck.isChecked){
                            Toast.makeText(baseContext, "Processing rental request process", Toast.LENGTH_SHORT).show()
                            submit(email, lname, fname, contactNum, birthDate, occupation, apartmentID, apartmentType, apartmentRoom,
                                priceNum,  equipmentList, apartmentAddress, payLaterCheck)
                        }
                        else Toast.makeText(baseContext, "Please check the terms and conditions first", Toast.LENGTH_SHORT).show()
                    }
                    else Toast.makeText(baseContext, "Please fill in the verification section", Toast.LENGTH_SHORT).show()
                }
                else {gcashCheck.error = "Please specify"; bdoCheck.error = "Please specify"}
            }
            else{
                Toast.makeText(baseContext, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } }
        //----------------------------------------------------------//
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun submit(
        email: String, lname: String, fname: String,
        contactNum: String, birthDate: String, occupation: String,
        apartmentID: String, apartmentType: String, apartmentRoom: String,
        priceNum: Float,
        equipmentList: HashMap<String, HashMap<String, Any>>,
        apartmentAddress: String,
        payLaterCheck: CheckBox) {

        var idURL: String
        var payURL: String

        uploadImage(idURI.toUri()){
            url ->
            idURL = url
            uploadImage(payURI.toUri()){
                    url2 ->
                payURL = url2
                val account = hashMapOf(
                    "email" to email, "lastName" to lname,
                    "firstName" to fname, "contactNum" to contactNum,
                    "birthDate" to birthDate, "occupation" to occupation,
                    "dateOfContract" to Timestamp(Date()),
                    "status" to "pending"
                )
                val contract = hashMapOf(
                    "IDCard" to idURL, "payForLater" to payLaterCheck.isChecked, "paymentProof" to payURL,
                    "apartmentID" to apartmentID,
                    "room" to if(apartmentType == "apartment"){apartmentRoom}else {null},
                    "monthlyPay" to priceNum,
                    "outstandingBalance" to priceNum,
                    "billStore" to HashMap<String, Any>(),
                    "equipments" to equipmentList,
                    "paymentMethod" to payType,
                    "dateOfContract" to Timestamp(Date()),
                    "status" to "pending")

                db.collection("account").document(email).set(account).addOnSuccessListener {
                    if(apartmentType == "house"){
                        db.collection("apartment").document(apartmentID).update("isOccupied", true)
                    }
                    else{
                        val updateRoom = mapOf("rooms.$apartmentRoom.isOccupied" to true)
                        db.collection("apartment").document(apartmentID).update(updateRoom)
                    }
                    db.collection("account").document(email).collection("currentContracts").document(apartmentID).
                    set(contract).addOnSuccessListener {

                        val intent = Intent(this, ApartmentViewingActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(baseContext, "Process successful. Wait for Admin approval", Toast.LENGTH_SHORT).show()
                        finish()
                        val chosenRentalText = if(apartmentRoom.isNotEmpty()){"$apartmentAddress @Room $apartmentRoom"}else{apartmentAddress}
                        db.collection("request").document().set(
                            hashMapOf(
                                "date" to Timestamp(Date()),
                                "email" to email, "apartmentID" to apartmentID,
                                "content" to "User $lname, $fname ($email) made a request to rent $chosenRentalText",
                                "status" to "pending"))
                    }.addOnFailureListener{
                        Toast.makeText(baseContext, "Failed to process the contract, Please check your internet connection ", Toast.LENGTH_SHORT).show()
                    }
                    if(auth.currentUser?.email.toString() == email){
                        showNotification("New Rental Request", "User $lname, $fname ($email) sent a rental request",10)
                    }
                }
            }
        }
    }


    //additional useful functions=====================================================================================================
    private fun addEquipment(view: View, equipL: LinearLayout, equipmentList: HashMap<String, HashMap<String, Any>>) {
        //popup window=============================================================================//
        val options = listOf("Oven", "Television", "Washing Machine", "Electric Fan", "Air Conditioner")
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_equipment_management, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-----------------------------------------------------------------------------------------//

        //popup xml element========================================================================//
        val xTxt = pView.findViewById<TextView>(R.id.xText)
        val equipImg = pView.findViewById<ImageView>(R.id.equipmentImage)
        val equipTypeET = pView.findViewById<Spinner>(R.id.equipmentSpinner)
        val equipDescET = pView.findViewById<EditText>(R.id.equipDescriptionField)
        val equipQtyET = pView.findViewById<EditText>(R.id.equipQuantityField)
        val okButton = pView.findViewById<Button>(R.id.okButton)
        val ardent = ResourcesCompat.getFont(this, R.font.ardent_sans_book)
        //-----------------------------------------------------------------------------------------//

        ///initialization==========================================================================//
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        equipTypeET.adapter = adapter
        //-----------------------------------------------------------------------------------------//

        //button events============================================================================//
        xTxt.setOnClickListener {
            pWindow.dismiss()
        }
        okButton.setOnClickListener{
            val equipType = equipTypeET.selectedItem.toString().trim()
            val equipDesc = equipDescET.text.toString().trim()
            val equipQty = equipQtyET.text.toString().trim().toIntOrNull()?: 0

            if(equipDesc.isNotEmpty() && equipType.isNotEmpty() && equipQty != 0){
                val imgUrl = ""
                val cardV = CardView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(convert(122,context), convert(148,context))
                }

                val mainL = FrameLayout(this).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT).apply {
                    } }
                val frameL = FrameLayout(this).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT).apply {
                    } }
                val imgV = ImageView(this).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                val xBtn = ImageView(this).apply {
                    setOnClickListener{
                        equipmentList.remove(equipDesc)
                        equipL.removeView(mainL)
                    } }

                Glide.with(this).load(imgUrl).placeholder(R.drawable.furniture_icon).into(imgV)
                val equipTV = TextView(this).apply {
                    text = equipType
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    typeface = ardent
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                }
                frameL.addView(imgV); frameL.addView(equipTV); cardV.addView(frameL); mainL.addView(xBtn); mainL.addView(cardV)
                equipL.addView(mainL)

                equipmentList[equipDesc] = hashMapOf("type" to equipType,
                    "description" to equipDesc, "quantity" to equipQty, "image" to imgUrl)

                cardV.setOnClickListener{
                    editEquipment(view, imgV, equipTV, equipmentList, equipType, equipDesc, equipQty, imgUrl)
                }
                pWindow.dismiss()
            }
            else Toast.makeText(baseContext,"Please fill up the data",Toast.LENGTH_SHORT).show()
        }
        //-----------------------------------------------------------------------------------------//
    }

    private fun editEquipment(view: View, imgV: ImageView, equipTV: TextView, equipmentList: HashMap<String, HashMap<String, Any>>,
        equipType: String, equipDesc: String, equipQty: Int, imgUrl: String) {
        //popup window=============================================================================//
        val inflater: LayoutInflater = layoutInflater
        val pView: View = inflater.inflate(R.layout.inflate_equipment_management, null)
        val pWindow = PopupWindow(pView, convert(400, context = this), ViewGroup.LayoutParams.WRAP_CONTENT,true)
        pWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.border_white_bg))
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        dimBehind(pWindow)
        //-----------------------------------------------------------------------------------------//

        //popup xml element========================================================================//
        val xTxt = pView.findViewById<TextView>(R.id.xText)
        val equipImg = pView.findViewById<ImageView>(R.id.equipmentImage)
        val equipTypeET = pView.findViewById<Spinner>(R.id.equipmentSpinner)
        val equipDescET = pView.findViewById<EditText>(R.id.equipDescriptionField)
        val equipQtyET = pView.findViewById<EditText>(R.id.equipQuantityField)
        val okButton = pView.findViewById<Button>(R.id.okButton)
        //-----------------------------------------------------------------------------------------//

        //initialization===========================================================================//
        Glide.with(this).load(imgUrl).into(equipImg)
        equipDescET.setText(equipDesc)
        equipQtyET.setText(equipQty.toString())
        val options = listOf("Oven", "Television", "Washing Machine", "Electric Fan", "Air Conditioner")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        equipTypeET.adapter = adapter
        equipTypeET.setSelection(options.indexOf(equipType))
        //-----------------------------------------------------------------------------------------//

        //button events============================================================================//
        okButton.setOnClickListener {
            val newImgURI = ""
            val newType = equipTypeET.selectedItem.toString().trim()
            val newDesc = equipDescET.text.toString().trim()
            val newQty = equipQtyET.text.toString().trim()

            if(newType.isNotEmpty() &&newDesc.isNotEmpty() && newQty.isNotEmpty()){
                equipmentList.remove(equipDesc)
                equipmentList[newDesc] = hashMapOf("type" to newType,
                    "description" to newDesc, "quantity" to newQty, "image" to imgUrl)

                imgV.setImageURI(newImgURI.toUri())
                equipTV.text = newType
                pWindow.dismiss()
            }
            else Toast.makeText(baseContext, "Please do not leave the fields as empty", Toast.LENGTH_SHORT).show()
        }
        xTxt.setOnClickListener {
            pWindow.dismiss()
        }
        //-----------------------------------------------------------------------------------------//

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

    //custom dp converter
    private fun convert(num: Int, context: Context): Int {
        return(num * context.resources.displayMetrics.density).toInt()
    }

    private fun isValidContactNumber(contactNum: String):Boolean{
        val regex = "^[+]?[0-9]{11}$".toRegex()
        return regex.matches(contactNum)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "request_notifier"
            val channelName = "Request User Notification"
            val channelDescription = "Notifications for handling rental requests"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val channelId = "request_notifier"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun uploadImage(receipt: Uri, onSuccess: (String) -> Unit){
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val fileName = "proofs/${System.currentTimeMillis()}.jpg" // Unique file name
        val imageRef = storageRef.child(fileName)
        val uploadTask = imageRef.putFile(receipt)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadURL ->
                onSuccess(downloadURL.toString())
            }.addOnFailureListener {
                Toast.makeText(baseContext,"Failed to obtain download link", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(baseContext,"Failed to upload receipt", Toast.LENGTH_SHORT).show()
        }
    }

    //==========================================================================
}