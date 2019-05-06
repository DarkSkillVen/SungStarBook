package com.sungbin.sungstarbook.view.main_fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.adapter.ChatRoomListAdapter
import com.sungbin.sungstarbook.dto.ChatRoomListItem
import com.sungbin.sungstarbook.utils.Utils
import org.apache.commons.lang3.StringUtils
import com.shashank.sony.fancytoastlib.FancyToast
import java.lang.Exception
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.*


private var uid:String? = null
private var items:ArrayList<ChatRoomListItem>? = null
@SuppressLint("StaticFieldLeak")
private var adapter:ChatRoomListAdapter? = null
private val reference = FirebaseDatabase.getInstance().reference.child("RoomDB")

@SuppressLint("InflateParams")
class Chatting : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        uid = Utils.readData(context!!, "uid", "null")!!
        items = ArrayList()
        adapter = ChatRoomListAdapter(items, activity!!)

        var myRoom = Utils.readData(context!!, "myRoom", "null")
        var myRoomCount = StringUtils.countMatches(myRoom, "/")
        val view = inflater.inflate(R.layout.fragment_chatting, null)

        val chatRoomListView = view.findViewById<RecyclerView>(R.id.chatRoomList)
        chatRoomListView.adapter = adapter
        chatRoomListView.layoutManager = LinearLayoutManager(context)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val roomData = dataSnapshot.getValue(ChatRoomListItem::class.java)
                items!!.add(roomData!!)
                adapter!!.notifyDataSetChanged()
                chatRoomListView.scrollToPosition(adapter!!.itemCount - 1)
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        if(myRoom != "null") {
            for (i in 1..myRoomCount) {
                try {
                    reference.child(myRoom!!.split("/")[i])
                        .addValueEventListener(postListener)
                }
                catch (e: Exception){
                    Utils.toast(context!!, "방을 불러올 수 없습니다.", FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                }
            }
        }

        view.findViewById<SwipeRefreshLayout>(R.id.refresh).setOnRefreshListener {
            myRoom = Utils.readData(context!!, "myRoom", "null")
            myRoomCount = StringUtils.countMatches(myRoom, "/")

            if(myRoom != "null") {
                for (i in 1..myRoomCount) {
                    try {
                        reference.child(myRoom!!.split("/")[i])
                            .addValueEventListener(postListener)
                    }
                    catch (e: Exception){
                        Utils.toast(context!!, "방을 불러올 수 없습니다.", FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                    }
                }
            }

            view.findViewById<SwipeRefreshLayout>(R.id.refresh).setColorSchemeColors(Color.parseColor("#9e9e9e"))
            view.findViewById<SwipeRefreshLayout>(R.id.refresh).isRefreshing = false

        }

        return view
    }
}