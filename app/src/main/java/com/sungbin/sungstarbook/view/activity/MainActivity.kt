package com.sungbin.sungstarbook.view.activity

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
import kotlinx.android.synthetic.main.activity_main.toolbar
import java.text.SimpleDateFormat
import java.util.*
import android.view.animation.TranslateAnimation


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

        join_room.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("채팅 방 입장")

            val textInputLayout = TextInputLayout(this)
            textInputLayout.isFocusableInTouchMode = true
            textInputLayout.isCounterEnabled = true

            val textInputEditText = TextInputEditText(this)
            textInputEditText.filters = arrayOf(InputFilter.LengthFilter(20))
            textInputEditText.hint = "입장할 방의 참여코드..."
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
                val roomUid = textInputEditText.text.toString()
                val preMyRoom = Utils.readData(applicationContext, "myRoom", "null")
                val nowMyRoom = "$preMyRoom/$roomUid"
                Utils.toast(applicationContext, "채팅방이 생성되었습니다." +
                        "\n당겨서 새로고침을 해주세요.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                Utils.saveData(applicationContext, "myRoom", nowMyRoom)
                fab.close(true)
            }
            dialog.show()
        }

        create_room.setOnClickListener {
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

            params.leftMargin = resources.getDimensionPixelSize(com.sungbin.sungstarbook.R.dimen.dialog_margin)
            params.rightMargin = resources.getDimensionPixelSize(com.sungbin.sungstarbook.R.dimen.dialog_margin)
            params.topMargin = resources.getDimensionPixelSize(com.sungbin.sungstarbook.R.dimen.dialog_margin)

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
                Utils.toast(applicationContext, "채팅방이 생성되었습니다." +
                        "\n당겨서 새로고침을 해주세요.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                Utils.saveData(applicationContext, "myRoom", nowMyRoom)
                fab.close(true)
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

                        if(fab.visibility == View.VISIBLE) {
                            val animate = TranslateAnimation(0F, fab.width.toFloat(), 0F, 0F)
                            animate.duration = 300
                            animate.fillAfter = true
                            fab.startAnimation(animate)
                            fab.visibility = View.INVISIBLE
                        }
                    }
                    1 -> {
                        fragmentTransaction = fm!!.beginTransaction()
                        fragmentTransaction!!.replace(R.id.page, Chatting())
                        fragmentTransaction!!.commit()

                        if(fab.visibility == View.INVISIBLE) {
                            val animate = TranslateAnimation(fab.width.toFloat(), 0F, 0F, 0F)
                            animate.duration = 300
                            animate.fillAfter = true
                            fab.startAnimation(animate)
                            fab.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        Utils.toast(applicationContext, "개발중...")
                        fragmentTransaction = fm!!.beginTransaction()
                        fragmentTransaction!!.replace(R.id.page, Friends())
                        fragmentTransaction!!.commit()

                        if(fab.visibility == View.VISIBLE) {
                            val animate = TranslateAnimation(0F, fab.width.toFloat(), 0F, 0F)
                            animate.duration = 300
                            animate.fillAfter = true
                            fab.startAnimation(animate)
                            fab.visibility = View.INVISIBLE
                        }
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

        return buf.toString() + getTimeForUid()
    }

    private fun getTime(): String{
        val sdf = SimpleDateFormat("aa hh:mm")
        return sdf.format(Date(System.currentTimeMillis()))
    }

    private fun getTimeForUid(): String{
        val sdf = SimpleDateFormat("hh:mm:ss")
        return sdf.format(Date(System.currentTimeMillis()))
    }

}