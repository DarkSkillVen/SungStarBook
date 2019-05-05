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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@Suppress("PLUGIN_WARNING")
@SuppressLint("SimpleDateFormat")
class ChatActivity : AppCompatActivity() {

    private val reference = FirebaseDatabase.getInstance().reference.child("ChatDB")
    private var items: ArrayList<ChattingItem>? = null
    private var adapter:ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbar.title = ""
        setSupportActionBar(toolbar)

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
        adapter = ChatAdapter(items, applicationContext)

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
            val map = HashMap<String, Any>()

            val key = reference.push().key
            reference.updateChildren(map)

            val root = reference.child(key!!)

            val objectMap = HashMap<String, Any>()

            objectMap["name"] = myName!!
            objectMap["time"] = getTime()
            objectMap["msg"] = inputText!!.text.toString()
            objectMap["type"] = "txt"
            objectMap["uid"] = uid
            objectMap["profilePicUri"] = profilePicUri!!
            objectMap["contentUri"] = "YEE"

            root.updateChildren(objectMap)
            inputText!!.setText("")
        }

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                try {
                    dataConversation(dataSnapshot)
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

    fun dataConversation(dataSnapshot: DataSnapshot) {
        try {
            val i = dataSnapshot.children.iterator()

            while (i.hasNext()) {
                val contentUri = (i.next() as DataSnapshot).value as String?
                val msg = (i.next() as DataSnapshot).value as String?
                val name = (i.next() as DataSnapshot).value as String?
                val profilePicUri = (i.next() as DataSnapshot).value as String?
                val time = (i.next() as DataSnapshot).value as String?
                val type = (i.next() as DataSnapshot).value as String?
                val uid = (i.next() as DataSnapshot).value as String?

                val item = ChattingItem(name, time, msg, type, profilePicUri, contentUri)
                items!!.add(item)
                adapter!!.notifyDataSetChanged()
                chatView.scrollToPosition(adapter!!.itemCount - 1)
            }
        } catch (e: Exception) {
            Utils.error(applicationContext, e)
        }

    }

    private fun getTime(): String{
        val sdf = SimpleDateFormat("aa hh:mm")
        return sdf.format(Date(System.currentTimeMillis()))
    }

}