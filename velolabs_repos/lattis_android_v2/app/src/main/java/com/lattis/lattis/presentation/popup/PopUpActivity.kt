package com.lattis.lattis.presentation.popup

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment
import com.lattis.lattis.presentation.authentication.signin.SignInActivityPresenter
import com.lattis.lattis.presentation.authentication.signin.SignInActivityView
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_pop_up.*
import javax.inject.Inject

class PopUpActivity : BaseActivity<PopUpActivityPresenter, PopUpActivityView>(),
    PopUpActivityView {


    @Inject
    override lateinit var presenter: PopUpActivityPresenter
    override val activityLayoutId = R.layout.activity_pop_up
    override var view: PopUpActivityView = this

    override fun configureViews() {
        super.configureViews()
        btn_pop_up_positive1.setOnClickListener {
            var intent = Intent()
            intent.putExtra(POSITIVE_LEVEL,1)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }

        btn_pop_up_positive2.setOnClickListener {
            intent.putExtra(POSITIVE_LEVEL,2)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }

        btn_pop_up_positive3.setOnClickListener {
            intent.putExtra(POSITIVE_LEVEL,3)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }

        btn_pop_up_negative.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun setView(
        title: String?,
        subTitle1: String?,
        subTitle2: String?,
        actionBtnPositive1: String?,
        actionBtnPositive2: String?,
        actionBtnPositive3: String?,
        actionBtnNegative: String?
    ) {
        if (!TextUtils.isEmpty(title)) {
            pop_up_title.text = (title)
        } else {
            pop_up_title.setVisibility(View.GONE)
        }
        if (!TextUtils.isEmpty(subTitle1)) {
            pop_up_sub_title1.setText(subTitle1)
        } else {
            pop_up_sub_title1.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(subTitle2)) {
            pop_up_sub_title2.setText(subTitle2)
        } else {
            pop_up_sub_title2.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(actionBtnPositive1)) {
            btn_pop_up_positive1.setText(actionBtnPositive1)
        } else {
            btn_pop_up_positive1.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(actionBtnPositive2)) {
            btn_pop_up_positive2.setText(actionBtnPositive2)
        } else {
            btn_pop_up_positive2.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(actionBtnPositive3)) {
            btn_pop_up_positive3.setText(actionBtnPositive3)
        } else {
            btn_pop_up_positive3.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(actionBtnNegative)) {
            btn_pop_up_negative.setText(actionBtnNegative)
        } else {
            pop_up_divider.visibility=View.GONE
            btn_pop_up_negative.setVisibility(View.GONE)
        }
    }

    protected override fun onInternetConnectionChanged(isConnected: Boolean) {}

    companion object {
        const val TITLE_POP_UP = "TITLE_POP_UP"
        const val SUBTITLE1_POP_UP = "SUBTITLE1_POP_UP"
        const val SUBTITLE2_POP_UP = "SUBTITLE2_POP_UP"
        const val ACTIONBTN_POSITIVE1 = "ACTIONBTN_POSITIVE1"
        const val ACTIONBTN_POSITIVE2 = "ACTIONBTN_POSITIVE2"
        const val ACTIONBTN_POSITIVE3 = "ACTIONBTN_POSITIVE3"
        const val ACTIONBTN_NEGATIVE = "ACTIONBTN_NEGATIVE"
        const val POSITIVE_LEVEL = "POSITIVE_LEVEL"



        fun launchForResult(
            activity: Activity,
            requestCode: Int,
            title: String?,
            subTitle1: String?,
            subTitle2: String?,
            actionBtnPositive1: String?,
            actionBtnPositive2: String?,
            actionBtnPositive3: String?,
            actionBtnNegative: String?
        ) {
            val intent = Intent(activity, PopUpActivity::class.java)
            intent.putExtra(TITLE_POP_UP, title)
            intent.putExtra(SUBTITLE1_POP_UP, subTitle1)
            intent.putExtra(SUBTITLE2_POP_UP, subTitle2)
            intent.putExtra(ACTIONBTN_POSITIVE1, actionBtnPositive1)
            intent.putExtra(ACTIONBTN_POSITIVE2, actionBtnPositive2)
            intent.putExtra(ACTIONBTN_POSITIVE3, actionBtnPositive3)
            intent.putExtra(ACTIONBTN_NEGATIVE, actionBtnNegative)
            activity.startActivityForResult(intent, requestCode)
        }

        fun launchForResultFromFragment(
            fragment: Fragment,
            activity: Activity?,
            requestCode: Int,
            title: String?,
            subTitle1: String?,
            subTitle2: String?,
            actionBtnPositive1: String?,
            actionBtnPositive2: String?,
            actionBtnPositive3: String?,
            actionBtnNegative: String?
        ) {
            val intent = Intent(activity, PopUpActivity::class.java)
            intent.putExtra(TITLE_POP_UP, title)
            intent.putExtra(SUBTITLE1_POP_UP, subTitle1)
            intent.putExtra(SUBTITLE2_POP_UP, subTitle2)
            intent.putExtra(ACTIONBTN_POSITIVE1, actionBtnPositive1)
            intent.putExtra(ACTIONBTN_POSITIVE2, actionBtnPositive2)
            intent.putExtra(ACTIONBTN_POSITIVE3, actionBtnPositive3)
            intent.putExtra(ACTIONBTN_NEGATIVE, actionBtnNegative)
            fragment.startActivityForResult(intent, requestCode)
        }
    }


}