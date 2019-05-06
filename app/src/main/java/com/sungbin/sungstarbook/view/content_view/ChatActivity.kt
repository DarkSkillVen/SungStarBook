package com.sungbin.sungstarbook.view.content_view

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import com.sungbin.sungstarbook.dto.ChattingItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.sungbin.sungstarbook.R
import kotlinx.android.synthetic.main.content_chat.*
import com.sungbin.sungstarbook.adapter.ChatAdapter
import com.sungbin.sungstarbook.utils.Utils
import kotlinx.android.synthetic.main.activity_chat.*
import android.annotation.SuppressLint
import com.shashank.sony.fancytoastlib.FancyToast
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@Suppress("PLUGIN_WARNING")
@SuppressLint("SimpleDateFormat")
class ChatActivity : AppCompatActivity() {

    private var reference: DatabaseReference? = null
    private var items: ArrayList<ChattingItem>? = null
    private var adapter: ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val roomName = intent.getStringExtra("name")
        val roomUid = intent.getStringExtra("roomUid")

        toolbar.title = ""
        toolbar_title.text = roomName
        setSupportActionBar(toolbar)

        reference = FirebaseDatabase.getInstance().reference.child("ChatDB").child(roomUid)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        val uid = Utils.readData(applicationContext, "uid", "null")!!
        val myName:String = Utils.readData(applicationContext, "myName", "null")!!
        var profilePicUri:String? = null

        items = ArrayList()
        adapter = ChatAdapter(items, this)

        (chatView as RecyclerView).layoutManager = LinearLayoutManager(applicationContext)
        (chatView as RecyclerView).adapter = adapter

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://sungstarbook-6f4ce.appspot.com/")
            .child("Profile_Image/$uid/Profile.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            profilePicUri = uri.toString()
        }
        storageRef.downloadUrl.addOnFailureListener { e ->

        }

        sendText!!.setOnClickListener {
            if(StringUtils.isBlank(inputText!!.text.toString())) {
                Utils.toast(applicationContext,
                    "내용을 입력해 주세요.",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.WARNING)
            }
            else {
                val chatData = ChattingItem(
                    myName,
                    getTime(),
                    inputText!!.text.toString(),
                    "msg",
                    profilePicUri,
                    "null",
                    uid
                )
                reference!!.push().setValue(chatData)
                inputText!!.setText("")
            }
        }

        reference!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                try {
                    val chatData = dataSnapshot.getValue(ChattingItem::class.java)
                    items!!.add(chatData!!)
                    adapter!!.notifyDataSetChanged()
                    chatView.scrollToPosition(adapter!!.itemCount - 1)
                } catch (e: Exception) {
                    Utils.error(applicationContext, e)
                }

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getTime(): String{
        val sdf = SimpleDateFormat("aa hh:mm")
        return sdf.format(Date(System.currentTimeMillis()))
    }

}