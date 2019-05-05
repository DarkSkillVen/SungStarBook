package com.sungbin.sungstarbook.view

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.iammert.library.readablebottombar.ReadableBottomBar
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.content_view.ChatActivity
import com.sungbin.sungstarbook.view.main_fragment.Friends


private var fm: FragmentManager? = null
private var fragmentTransaction: FragmentTransaction? = null

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.sungbin.sungstarbook.R.layout.activity_main)

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
        fragmentTransaction!!.replace(com.sungbin.sungstarbook.R.id.page, Friends())
        fragmentTransaction!!.commit()

        bottomBar.setOnItemSelectListener( object : ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {
                when(index){
                    0 -> {
                        fragmentTransaction = fm!!.beginTransaction()
                        fragmentTransaction!!.replace(com.sungbin.sungstarbook.R.id.page, Friends())
                        fragmentTransaction!!.commit()
                    }
                    else -> Utils.toast(applicationContext, "개발중...")
                }
            }
        })

        startActivity(Intent(applicationContext, ChatActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    }

}