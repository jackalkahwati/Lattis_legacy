package io.lattis.operator.presentation.authentication

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import io.lattis.operator.R
import io.lattis.operator.presentation.home.HomeActivity
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_loading.*
import kotlinx.android.synthetic.main.activity_signin.*
import javax.inject.Inject

class SignInActivity : BaseActivity<SignInActivityPresenter, SignInActivityView>(),
    SignInActivityView {


    @Inject
    override lateinit var presenter: SignInActivityPresenter
    override val activityLayoutId = R.layout.activity_signin
    override var view: SignInActivityView = this


    override fun configureSubscriptions() {
        super.configureSubscriptions()
        subscriptions.add(
            RxTextView.textChangeEvents(et_sign_in_email)
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                onTyping()
                presenter.setEmail(textViewTextChangeEvent.text().toString())
            }
        )
        subscriptions.add(
            RxTextView.textChangeEvents(et_sign_in_password)
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                onTyping()
                presenter.setPassword(textViewTextChangeEvent.text().toString())
            }
        )
    }

     override fun configureViews() {
         super.configureViews()
         btn_sign_in_login_in_selected.setOnClickListener {
             showProgressLoading()
             presenter.trySignIn()
         }
         iv_sign_in_password_show.setOnClickListener {
             if(et_sign_in_password.transformationMethod==null) {
                 et_sign_in_password.setTransformationMethod(PasswordTransformationMethod())
             }else{
                 et_sign_in_password.setTransformationMethod(null)
             }
         }


         btn_sign_in_login_in_selected.setOnClickListener {
             showProgressLoading()
             presenter.trySignIn()
         }
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        et_sign_in_email.setText("ravil@lattis.io")
//        et_sign_in_password.setText("ravillattis1")
    }

    //// toggle login btn: start

    override fun passwordToggleButton(active: Boolean) {
        iv_sign_in_password_show.visibility = if(active)View.VISIBLE else View.GONE
    }

    override fun onEmailPasswordValid() {
        btn_sign_in_login_in_selected.visibility = View.VISIBLE
        btn_sign_in_login_in_unselected.visibility = View.GONE
    }

    override fun onEmailPasswordInvalid() {
        btn_sign_in_login_in_selected.visibility = View.GONE
        btn_sign_in_login_in_unselected.visibility = View.VISIBLE
    }
    //// show hide email password:end

    fun onTyping(){
        iv_sign_in_email.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_right_password));
        et_sign_in_email.setTextColor(ContextCompat.getColor(this, R.color.sign_in_right_password))
        et_sign_in_email.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_right_password)))
        iv_sign_in_password.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_right_password));
        et_sign_in_password.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_right_password)))
    }

    override fun onAuthenticationFailed() {
        hideProgressLoading()
        iv_sign_in_password.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_wrong_password));
        et_sign_in_password.setTextColor(ContextCompat.getColor(this, R.color.sign_up_wrong_password))
        et_sign_in_password.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_up_wrong_password)))
    }

    override fun onAuthenticationSuccess() {
        startHomeActivity()
    }

    private fun startHomeActivity() {
        val i = Intent(this, HomeActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finishMe()
    }


    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun showProgressLoading(){
        if(sign_in_loading!=null){
            sign_in_loading.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(sign_in_loading!=null){
            sign_in_loading.visibility= View.GONE
        }
    }
}