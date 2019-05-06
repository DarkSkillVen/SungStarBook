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
import android.util.Log
import android.view.Menu
import com.shashank.sony.fancytoastlib.FancyToast
import com.yarolegovich.slidingrootnav.SlideGravity
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.view.MenuItem
import kotlinx.android.synthetic.main.chat_right_drawer_menu.*
import com.google.firebase.database.DataSnapshot




@Suppress("PLUGIN_WARNING")
@SuppressLint("SimpleDateFormat")
class ChatActivity : AppCompatActivity() {

    private var reference: DatabaseReference? = null
    private var items: ArrayList<ChattingItem>? = null
    private var adapter: ChatAdapter? = null
    private var slidingRootNav: SlidingRootNav? = null
    private var lastMsg: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val roomName = intent.getStringExtra("name")
        val roomUid = intent.getStringExtra("roomUid")

        toolbar.title = ""
        toolbar_title.text = roomName
        setSupportActionBar(toolbar)

        slidingRootNav = SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withContentClickableWhenMenuOpened(false)
            .withSavedState(savedInstanceState)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.chat_right_drawer_menu)
            .inject()

        roomUserList.setOnClickListener {
            Utils.toast(applicationContext, "되니?")
        }

        getRoomUid.setOnClickListener {
            Utils.toast(applicationContext, "방의 입장코드가 복사되었습니다.",
                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            Utils.copy(applicationContext, roomUid, false)
        }

        reference = FirebaseDatabase.getInstance().reference.child("ChatDB")
            .child(roomUid).child("chat")

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
            profilePicUri = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png"
        }

        /*reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageData in dataSnapshot.children) {
                    items!!.add(messageData.getValue(ChattingItem::class.java)!!)
                }
                adapter!!.notifyDataSetChanged()
                chatView.scrollToPosition(adapter!!.itemCount - 1)
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })*/

        reference!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                try {
                    val chatData = dataSnapshot.getValue(ChattingItem::class.java)
                    if(lastMsg == chatData!!.msg){
                        return
                    }
                    else lastMsg = chatData.msg!!
                    Log.d("TTT", chatData.msg)
                    items!!.add(chatData)
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

    }

    private fun getTime(): String{
        val sdf = SimpleDateFormat("aa hh:mm")
        return sdf.format(Date(System.currentTimeMillis()))
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_user -> {
                slidingRootNav!!.openMenu()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}