package com.sungbin.sungstarbook.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.view.MainActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import android.telephony.TelephonyManager
import java.util.*


@Suppress("DEPRECATION")
@SuppressLint("MissingPermission", "HardwareIds")
object Utils {

    var sdcard = Environment.getExternalStorageDirectory().absolutePath

    fun createFolder(name: String) {
        File("$sdcard/SungStarBook/$name/").mkdirs()
    }

    fun toast(ctx: Context?, content: String) {
        Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show()
    }

    fun read(name: String, _null: String): String {
        try {
            val file = File("$sdcard/SungStarBook/$name/")
            if (!file.exists()) return _null
            val fis = FileInputStream(file)
            val isr = InputStreamReader(fis)
            val br = BufferedReader(isr)
            var str = br.readLine()

            while (true) {
                val inputLine = br.readLine() ?: break
                str += "\n" + inputLine
            }
            fis.close()
            isr.close()
            br.close()
            return str.toString()
        } catch (e: Exception) {
            Log.e("READ", e.toString())
        }

        return _null
    }

    fun save(name: String, content: String) {
        try {
            val file = File("$sdcard/SungStarBook/$name")
            val fos = java.io.FileOutputStream(file)
            fos.write(content.toByteArray())
            fos.close()
        } catch (e: Exception) {
            Log.e("SAVE", e.toString())
        }

    }

    fun delete(name: String) {
        File("$sdcard/SungStarBook/$name").delete()
    }

    fun readData(ctx: Context, name: String, _null: String): String? {
        val pref = ctx.getSharedPreferences("pref", MODE_PRIVATE)
        return pref.getString(name, _null)
    }

    fun saveData(ctx: Context, name: String, value: String) {
        val pref = ctx.getSharedPreferences("pref", MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString(name, value)
        editor.apply()
    }

    fun clearData(ctx: Context) {
        val pref = ctx.getSharedPreferences("pref", MODE_PRIVATE)
        val editor = pref.edit()
        editor.clear()
        editor.apply()
    }

    fun copy(ctx: Context, text: String) {
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.primaryClip = clip
        Toast.makeText(ctx, "클립보드에 복사되었습니다.", Toast.LENGTH_LONG).show()
    }

    fun copy(ctx: Context, text: String, showToast: Boolean) {
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.primaryClip = clip
        if (showToast) Toast.makeText(ctx, "클립보드에 복사되었습니다.", Toast.LENGTH_LONG).show()
    }

    fun error(ctx: Context, content: String) {
        Utils.toast(ctx, content, FancyToast.LENGTH_SHORT, FancyToast.ERROR)
        Utils.copy(ctx, content)
        Log.e("Error", content)
    }

    fun error(ctx: Context, e: Exception) {
        val data = "Error: " + e + "\nLineNumber: " + e.stackTrace[0].lineNumber
        Utils.toast(ctx, "Error: $e", FancyToast.LENGTH_SHORT, FancyToast.ERROR)
        Utils.copy(ctx, data)
        Log.e("Error", data)
    }

    fun restart(context: Context) {
        val mStartActivity = Intent(context, MainActivity::class.java)
        val mPendingIntentId = 123456
        val mPendingIntent =
            PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)
    }

    fun toast(ctx: Context, txt: String, length: Int, type: Int) {
        com.shashank.sony.fancytoastlib.FancyToast.makeText(ctx, txt, length, type, false).show()
    }

    fun getDevicesUUID(ctx: Context): String {
        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val tmDevice: String = tm.deviceId
        val tmSerial: String = tm.simSerialNumber
        val androidId: String = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        val deviceUuid = UUID(androidId.hashCode().toLong(),
            tmDevice.hashCode().toLong() shl 32 or tmSerial.hashCode().toLong())
        return deviceUuid.toString()
    }

}