package com.sungbin.sungstarbook.view

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_error_activty.*

import kotlinx.android.synthetic.main.content_error_activty.*
import android.content.Intent
import com.sungbin.sungstarbook.utils.Utils


class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.sungbin.sungstarbook.R.layout.activity_error_activty)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        error_show.text = intent.getStringExtra("error")

        send_error.setOnClickListener {
            val email = Intent(Intent.ACTION_SEND)
            email.type = "plain/text"
            val address = arrayOf("sungstarbook.error@gmail.com")
            email.putExtra(Intent.EXTRA_EMAIL, address)
            email.putExtra(Intent.EXTRA_SUBJECT, "성별책 에러 제보")
            email.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra("error"))
            startActivity(email)
        }

        refresh.setOnClickListener {
            Utils.restart(applicationContext)
        }
    }

}
