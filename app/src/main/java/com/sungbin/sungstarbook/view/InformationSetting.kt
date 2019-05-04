package com.sungbin.sungstarbook.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sungbin.sungstarbook.utils.Utils
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.android.synthetic.main.activity_information_setting.*
import kotlinx.android.synthetic.main.content_information_setting.*
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kakao.usermgmt.StringSet.id
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import org.apache.commons.lang3.StringUtils


private var profileUri:Uri? = null

class InformationSetting : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information_setting)

        toolbar.title = ""
        setSupportActionBar(toolbar)

        val uid = intent.getStringExtra("uid")
        Utils.saveData(applicationContext, "uid", uid)

        fab.setOnClickListener {
            if(StringUtils.isBlank(input_nickname.text.toString())){
                Utils.toast(applicationContext, "닉네임을 입력해 주세요.",
                    FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                return@setOnClickListener
            }
            else {
                val reference = FirebaseDatabase.getInstance()
                    .reference.child("UserDB").child(uid)

                val map = HashMap<String, Any>()
                map["nickname"] = input_nickname.text.toString()

                reference.updateChildren(map)

                uploadProfileImage(profileUri!!, uid)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        profile_image.setOnClickListener {
            val permissionlistener = object : PermissionListener {
                override fun onPermissionGranted() {
                    TedBottomPicker.with(this@InformationSetting)
                        .setImageProvider { imageView, imageUri ->
                            val options = RequestOptions().centerCrop()
                            Glide.with(baseContext).load(imageUri).apply(options).into(imageView)
                        }
                        .show {
                            CropImage.activity(it)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                                .setAutoZoomEnabled(true)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setFixAspectRatio(true)
                                .start(this@InformationSetting)
                        }
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Utils.toast(applicationContext, "권한 사용에 동의 해 주셔야 프로필 사진을 불러올 수 있습니다.\n" +
                            "내부메모리 접근 권한 사용이 거절되어, 프로필 사진이 기본 사진으로 대채됩니다.",
                        FancyToast.WARNING, FancyToast.LENGTH_SHORT)
                }
            }

            TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleTitle("권한 필요")
                .setRationaleMessage("프로필 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "권한 사용을 허용해 주세요.")
                .setDeniedTitle("내부 메모리 접근 권한 필요")
                .setDeniedMessage("프로필 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "어플 설정애서 해당 권한의 사용을 허락해 주세요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
        }

    }

    @SuppressLint("MissingSuperCall")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                profileUri = resultUri
                Glide.with(this).load(resultUri).into(profile_image)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Utils.error(this, "프로필 사진을 선택하는 도중에 오류가 발생했습니다.\n$error")
            }
        }
    }

    private fun uploadProfileImage(url: Uri, uid:String){
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "프로필 사진 업로드중..."
        pDialog.setCancelable(false)
        pDialog.show()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://sungstarbook-6f4ce.appspot.com/")
            .child("Profile_Image/$uid/Profile.png")
        storageRef.putFile(url).addOnSuccessListener {
            pDialog.dismissWithAnimation()
            Utils.toast(applicationContext, "프로필 사진이 업로드 되었습니다.",
                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            startActivity(Intent(applicationContext, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.addOnFailureListener {
            pDialog.dismissWithAnimation()
            Utils.toast(applicationContext, "프로필 사진이 업로드중에 문제가 발생하였습니다.\n\n${it.cause}",
                FancyToast.LENGTH_SHORT, FancyToast.WARNING)
        }.addOnProgressListener {
            val progress = (100 * it.bytesTransferred) /  it.totalByteCount.toInt()
           pDialog.titleText = "프로필 사진 업로드중... ($progress/100)"
        }
    }

}
