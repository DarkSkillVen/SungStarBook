package com.sungbin.sungstarbook.view

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.iammert.library.readablebottombar.ReadableBottomBar
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.main_fragment.Chatting
import com.sungbin.sungstarbook.view.main_fragment.Friends
import android.annotation.SuppressLint
import android.text.InputFilter
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.dto.ChatRoomListItem
import com.sungbin.sungstarbook.dto.ChattingItem
import java.text.SimpleDateFormat
import java.util.*


private var fm: FragmentManager? = null
private var fragmentTransaction: FragmentTransaction? = null
private val reference = FirebaseDatabase.getInstance().reference.child("RoomDB")

@SuppressLint("SimpleDateFormat")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.title = ""
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        fm = supportFragmentManager
        fragmentTransaction = fm!!.beginTransaction()
        fragmentTransaction!!.replace(R.id.page, Friends())
        fragmentTransaction!!.commit()

        val uid = Utils.readData(applicationContext, "uid", "null")!!
        val myName:String = Utils.readData(applicationContext, "myName", "null")!!
        var profilePicUri:String? = null

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://sungstarbook-6f4ce.appspot.com/")
            .child("Profile_Image/$uid/Profile.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            profilePicUri = uri.toString()
        }
        storageRef.downloadUrl.addOnFailureListener { e ->

        }

        addFab.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("채팅 방 생성")

            val textInputLayout = TextInputLayout(this)
            textInputLayout.isFocusableInTouchMode = true
            textInputLayout.isCounterEnabled = true

            val textInputEditText = TextInputEditText(this)
            textInputEditText.filters = arrayOf(InputFilter.LengthFilter(20))
            textInputEditText.hint = "생성할 방 이름..."
            textInputLayout.addView(textInputEditText)

            val container = FrameLayout(this)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            params.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)

            textInputLayout.layoutParams = params
            container.addView(textInputLayout)

            dialog.setView(container)
            dialog.setPositiveButton("확인") { _, _ ->
                val roomUid = makeRandomString()
                val preMyRoom = Utils.readData(applicationContext, "myRoom", "null")
                val nowMyRoom = "$preMyRoom/$roomUid"
                val roomData = ChatRoomListItem(
                    textInputEditText.text.toString(),
                    getTime(),
                    "[채팅방을 생성했습니다]",
                    profilePicUri,
                    roomUid
                )
                reference.child(roomUid).setValue(roomData)
                Utils.toast(applicationContext, "채팅방이 생성되었습니다.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                Utils.saveData(applicationContext, "myRoom", nowMyRoom)
            }
            dialog.show()
        }

        bottomBar.setOnItemSelectListener( object : ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {
                when(index){
                    0 -> {
                        fragmentTransaction = fm!!.beginTransaction()
                        fragmentTransaction!!.replace(R.id.page, Friends())
                        fragmentTransaction!!.commit()
                        addFab.hide()
                    }
                    1 -> {
                        fragmentTransaction = fm!!.beginTransaction()
                        fragmentTransaction!!.replace(R.id.page, Chatting())
                        fragmentTransaction!!.commit()
                        addFab.show()
                    }
                    else -> {
                        Utils.toast(applicationContext, "개발중...")
                        addFab.hide()
                    }
                }
            }
        })

    }

    private fun makeRandomString(): String{
        val rnd = Random()
        val buf = StringBuffer()

        for (i in 0..10) {
            if (rnd.nextBoolean()) {
                buf.append((rnd.nextInt(26) + 97).toChar())
            } else {
                buf.append(rnd.nextInt(10))
            }
        }

        return buf.toString() + getTime()
    }

    private fun getTime(): String{
        val sdf = SimpleDateFormat("hh:mm:ss")
        return sdf.format(Date(System.currentTimeMillis()))
    }

}