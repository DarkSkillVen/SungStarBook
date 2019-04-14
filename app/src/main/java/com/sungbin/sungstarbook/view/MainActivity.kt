package com.sungbin.sungstarbook.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.kakao.auth.ErrorCode
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeResponseCallback
import com.kakao.usermgmt.response.model.UserProfile
import com.kakao.util.exception.KakaoException
import com.karlgao.materialroundbutton.MaterialButton
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.shashank.sony.fancytoastlib.FancyToast
import com.shazam.android.widget.text.reflow.ReflowTextAnimatorHelper
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    lateinit var googleLoginClient: GoogleApiClient
    private val RC_SIGN_IN = 1000
    private var mAuth: FirebaseAuth? = null
    private lateinit var snsLoginCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var snsLoginCallBackManager: CallbackManager
    var mOAuthLoginModule: OAuthLogin? = null
    private val mOAuthLoginHandler = @SuppressLint("HandlerLeak")
    object : OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (success) {
                Utils.toast(applicationContext,
                    "네이버 로그인 성공",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            } else {
                val errorCode = mOAuthLoginModule!!.getLastErrorCode(applicationContext).code
                Utils.error(applicationContext, "네이버 로그인 실패\n에러 코드 : $errorCode")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        /* ----- Firebase Auth ----- */
        mAuth = FirebaseAuth.getInstance()
        mAuth!!.setLanguageCode("kr")
        /* ---------- */

        /* ----- 익명 로그인 -----*/
        /*mAuth!!.signInAnonymously()
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Utils.toast(applicationContext,
                        "익명 로그인에 성공했습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                } else {
                   Utils.error(applicationContext,
                       "익명 로그인 과정에서 오류가 발생했습니다.\n${it.exception}")
                }
            }*/
        /* ---------- */

        /* ----- 네이버 로그인 ----- */
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule!!.init(
            this,
            getString(R.string.naver_login_client_id),
            getString(R.string.naver_login_client_secret),
            getString(R.string.naver_login_client_name)
        )
        /* ---------- */

        /* ----- 카카오톡 로그인 ----- */
        Session.getCurrentSession().addCallback(
            KakaoCallBack(
                applicationContext
            )
        )
        /* ---------- */

        /* ----- 구글 로그인 ----- */
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_login_client_id))
            .requestEmail()
            .build()

        googleLoginClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        /* ---------- */

        /* ----- 전화번호 인증 ----- */
        snsLoginCallBackManager = CallbackManager.Factory.create()

        snsLoginCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Utils.error(applicationContext, "전화번호 인증에서 오류가 발생했습니다.\n$e")
            }

            override fun onCodeSent(
                verificationId: String?,
                token: PhoneAuthProvider.ForceResendingToken) {
                showNumberCheckDialog(verificationId!!)
            }
        }
        /* ---------- */

        /* ------ 페이스북 로그인 ----- */
        LoginManager.getInstance().registerCallback(snsLoginCallBackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    firebaseAuthWithFacebook(loginResult.accessToken)
                }

                override fun onCancel() {
                }

                override fun onError(e: FacebookException) {
                    Utils.error(applicationContext,
                        "페이스북 로그인에서 오류가 발생했습니다.\n$e")
                }
            })
        /* ---------- */

        /* ----- 로그인 버튼 등록 ----- */
        google_login.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleLoginClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        kakao_login.setOnClickListener {
            kakao_login_origin.performClick()
        }

        naver_login.setOnClickListener {
            naver_login_origin.setOAuthLoginHandler(mOAuthLoginHandler)
            naver_login_origin.performClick()
        }

        more_login.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this, snsLoginCallBack)
            bottomSheetDialog.show(supportFragmentManager, "More Login")
        }
        /* ---------- */

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        welcomeCenter.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                welcomeCenter.viewTreeObserver.removeOnPreDrawListener(this)

                val animator = ReflowTextAnimatorHelper
                    .Builder(welcomeCenter, welcomeTop)
                    .withDuration(1000, 3000).buildAnimator()

                animator.startDelay = 500
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        welcomeCenter.visibility = View.GONE
                        welcomeTop.visibility = View.VISIBLE

                        val fade = AlphaAnimation(0f, 1f)
                        fade.duration = 1500

                        google_login.visibility = View.VISIBLE
                        kakao_login.visibility = View.VISIBLE
                        naver_login.visibility = View.VISIBLE
                        more_login.visibility = View.VISIBLE
                        copyright.visibility = View.VISIBLE

                        google_login.animation = fade
                        kakao_login.animation = fade
                        naver_login.animation = fade
                        more_login.animation = fade
                        copyright.animation = fade
                    }
                })
                animator.start()

                return true
            }
        })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) { //Google Login
           val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
            else {
                Utils.error(applicationContext,
                    "구글 로그인을 하는 중에 오류가 발생했습니다.")
            }
        }
        else //전화번호 인증
            snsLoginCallBackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) { //구글 로그인 파베에 등록
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Utils.toast(applicationContext,
                        "구글 로그인 Firebase Auth 처리에 성공했습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                } else {
                    Utils.error(applicationContext,
                        "구글 로그인 Firebase Auth 처리에 실패했습니다.\n${task.exception}")
                }
            }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) { //뭐였지?

    }

    private fun showNumberCheckDialog(verificationId: String){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("인증번호 입력")

        val textInputLayout = TextInputLayout(this)
        textInputLayout.isCounterEnabled = true

        val textInputEditText = TextInputEditText(this)
        textInputEditText.inputType = InputType.TYPE_CLASS_NUMBER
        textInputEditText.filters = arrayOf(InputFilter.LengthFilter(6))
        textInputLayout.addView(textInputEditText)

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)

        textInputLayout.layoutParams = params
        container.addView(textInputLayout)

        dialog.setView(container)
        dialog.setPositiveButton("확인") { _, _ ->
            val credential = PhoneAuthProvider.getCredential(verificationId, textInputEditText.text.toString())
            mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Utils.toast(this,
                            "인증번호를 통한 로그인에 성공했습니다.",
                            FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                    }
                    else {
                        Utils.error(this,
                            "인증번호를 통한 로그인에 실패했습니다.\n${task.exception}")
                    }
                }
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun firebaseAuthWithFacebook(token: AccessToken) { //페북 로그인 파베에 등록
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Utils.toast(applicationContext,
                        "페이스북 로그인 Firebase Auth 처리에 성공했습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                }
                else {
                    Utils.error(applicationContext,
                        "페이스북 로그인 Firebase Auth 처리에 실패했습니다.\n${task.exception}")
                }
            }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) { //인증번호를 통한 로그인
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Utils.toast(applicationContext,
                        "인증번호를 통한 로그인에 성공했습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                }
                else {
                    Utils.error(applicationContext,
                        "인증번호를 통한 로그인에 실패했습니다.\n${task.exception}")
                }
            }
    }

    private class KakaoCallBack(applicationContext: Context) : ISessionCallback { //카카오톡 로그인 콜백

        var ctx: Context = applicationContext

        override fun onSessionOpened() {
            UserManagement.requestMe(object : MeResponseCallback() {
                override fun onFailure(errorResult: ErrorResult?) {
                    val result = ErrorCode.valueOf(errorResult!!.errorCode)
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        Utils.error(ctx,
                            "카카오톡 로그인에 실패했습니다.\n${errorResult.errorMessage}")
                    }
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                }

                override fun onNotSignedUp() {
                }

                override fun onSuccess(userProfile: UserProfile) {
                    Utils.toast(ctx,
                        "카카오톡 로그인에 성공했습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                }
            })

        }

        override fun onSessionOpenFailed(exception: KakaoException) {
        }
    }

    fun createEmailAccount(email: String, pw: String) { //게스트 계정 생성 (이메일, 비밀번호)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pw)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.toast(applicationContext,
                        "게스트 회원가입에 성공했습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                } else {
                    Utils.error(applicationContext,
                        "게스트 회원가입에 실패했습니다.\n${task.exception}")
                }
            }
    }

    fun loginEmailAccount(email: String, pw: String) { //게스트 계정 로그인 (이메일, 비밀번호)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pw)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.toast(applicationContext,
                        "게스트 로그인에 성공했습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                } else {
                    Utils.error(applicationContext,
                        "게스트 로그인에 실패했습니다.\n${task.exception}")
                }
            }
    }

    @SuppressLint("ValidFragment")
    class BottomSheetDialog @SuppressLint("ValidFragment") constructor(private var act: Activity, private var callBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks) : BottomSheetDialogFragment(), View.OnClickListener {

        private var guest: MaterialButton? = null
        private var facebook: MaterialButton? = null
        private var sns: MaterialButton? = null
        private var no_name: MaterialButton? = null

        private var facebook_origin: LoginButton? = null

        @Nullable
        override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.more_login_navigation_view, container, false)

            guest = view.findViewById(R.id.guest_login)
            facebook = view.findViewById(R.id.facebook_login)
            sns = view.findViewById(R.id.sns_login)
            no_name = view.findViewById(R.id.no_name_login)

            facebook_origin = view.findViewById(R.id.facebook_login_origin)

            guest!!.setOnClickListener(this)
            facebook!!.setOnClickListener(this)
            no_name!!.setOnClickListener(this)
            sns!!.setOnClickListener(this)

            facebook_origin!!.setReadPermissions("email")

            return view
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.facebook_login -> facebook_origin!!.performClick()
                R.id.guest_login -> Utils.toast(context!!, "준비중", FancyToast.LENGTH_SHORT, FancyToast.INFO)
                R.id.sns_login -> {
                    val dialog = AlertDialog.Builder(context!!)
                    dialog.setTitle("전화번호 입력")

                    val textInputLayout = TextInputLayout(context!!)
                    textInputLayout.isCounterEnabled = true

                    val textInputEditText = TextInputEditText(context!!)
                    textInputEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    textInputEditText.filters = arrayOf(InputFilter.LengthFilter(11))
                    textInputLayout.addView(textInputEditText)

                    val container = FrameLayout(context!!)
                    val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)

                    textInputLayout.layoutParams = params
                    container.addView(textInputLayout)

                    dialog.setView(container)
                    dialog.setPositiveButton("확인") { _, _ ->
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            textInputEditText.text.toString()
                                .replaceFirst("0", "+82"),
                            60,
                            TimeUnit.SECONDS,
                            act,
                            callBack)
                        Utils.toast(act,
                            "인증번호가 문자 메세지로 전송 되었습니다.\n잠시만 기다려 주세요.",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS)
                    }
                    dialog.show()
                }
                R.id.no_name_login -> Utils.toast(context!!, "준비중", FancyToast.LENGTH_SHORT, FancyToast.INFO)
            }
            dismiss()
        }
    }

}