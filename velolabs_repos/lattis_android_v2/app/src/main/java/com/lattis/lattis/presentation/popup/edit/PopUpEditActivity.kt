package com.lattis.lattis.presentation.popup.edit

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.mapbox.mapboxsdk.style.layers.Property
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_pop_up.*
import kotlinx.android.synthetic.main.activity_pop_up_edit.*
import javax.inject.Inject

class PopUpEditActivity : BaseActivity<PopUpEditActivityPresenter, PopUpEditActivityView>(),
    PopUpEditActivityView {


    private val REQUEST_CODE_ERROR = 4393

    @Inject
    override lateinit var presenter: PopUpEditActivityPresenter
    override val activityLayoutId = R.layout.activity_pop_up_edit
    override var view: PopUpEditActivityView = this

    override fun configureViews() {
        super.configureViews()
        btn_pop_up_edit_submit.setOnClickListener {
            var intent = Intent()
            intent.putExtra(EDIT_TEXT_VALUE_POP_UP_EDIT,et_pop_up_edit_editing.text.toString())
            if(!TextUtils.isEmpty(et_pop_up_2_edit_editing.text)){
                intent.putExtra(EDIT_TEXT_VALUE_2_POP_UP_EDIT,et_pop_up_2_edit_editing.text.toString())
            }
            setResult(Activity.RESULT_OK,intent)
            finish()
        }

        btn_pop_up_edit_cancel.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val TITLE_POP_UP_EDIT = "TITLE_POP_UP_EDIT"
        const val SUBTITLE1_POP_UP_EDIT = "SUBTITLE1_POP_UP_EDIT"
        const val ICON_POP_UP_EDIT = "ICON_POP_UP_EDIT"
        const val SUBMIT_POP_UP_EDIT = "SUBMIT_POP_UP_EDIT"
        const val EDIT_TEXT_VALUE_POP_UP_EDIT = "EDIT_TEXT_VALUE_1_POP_UP_EDIT"
        const val EDIT_TEXT_VALUE_2_POP_UP_EDIT = "EDIT_TEXT_VALUE_2_POP_UP_EDIT"
        const val SHOW_SECOND_EDIT_TEXT_POP_UP_EDIT = "SHOW_SECOND_EDIT_TEXT_POP_UP_EDIT"
        const val HINT_EDIT_TEXT_POP_UP_EDIT = "HINT_EDIT_TEXT_POP_UP_EDIT"
        const val HINT_EDIT_TEXT_2_POP_UP_EDIT = "HINT_EDIT_TEXT_2_POP_UP_EDIT"
        const val KEYBOARD_EDIT_TEXT_POP_UP_EDIT = "KEYBOARD_EDIT_TEXT_POP_UP_EDIT"


        fun launchForResult(
            activity: Activity,
            requestCode: Int,
            title: String?,
            subTitle1: String?,
            icon: Int?,
            submitBtnTitle: String?,
            editTextValue: String?,
            showSecondEditText: Boolean,
            editTextHint: String?,
            editTextKeyboard: Int?,
            editText2Hint: String? = null
        ) {
            val intent = Intent(activity, PopUpEditActivity::class.java)
            intent.putExtra(TITLE_POP_UP_EDIT, title)
            intent.putExtra(SUBTITLE1_POP_UP_EDIT, subTitle1)
            intent.putExtra(ICON_POP_UP_EDIT, icon)
            intent.putExtra(SUBMIT_POP_UP_EDIT, submitBtnTitle)
            intent.putExtra(EDIT_TEXT_VALUE_POP_UP_EDIT, editTextValue)
            intent.putExtra(SHOW_SECOND_EDIT_TEXT_POP_UP_EDIT, showSecondEditText)
            intent.putExtra(HINT_EDIT_TEXT_POP_UP_EDIT, editTextHint)
            intent.putExtra(KEYBOARD_EDIT_TEXT_POP_UP_EDIT, editTextKeyboard)
            intent.putExtra(HINT_EDIT_TEXT_2_POP_UP_EDIT, editText2Hint)
            activity.startActivityForResult(intent, requestCode)
        }

        fun launchForResultFromFragment(
            fragment: Fragment,
            activity: Activity,
            requestCode: Int,
            title: String?,
            subTitle1: String?,
            icon: Int?,
            submitBtnTitle: String?,
            editTextValue: String?,
            showSecondEditText: Boolean,
            editTextHint: String?,
            editTextKeyboard: Int?,
            editText2Hint: String? = null
        ) {
            val intent = Intent(activity, PopUpEditActivity::class.java)
            intent.putExtra(TITLE_POP_UP_EDIT, title)
            intent.putExtra(SUBTITLE1_POP_UP_EDIT, subTitle1)
            intent.putExtra(ICON_POP_UP_EDIT, icon)
            intent.putExtra(SUBMIT_POP_UP_EDIT, submitBtnTitle)
            intent.putExtra(EDIT_TEXT_VALUE_POP_UP_EDIT, editTextValue)
            intent.putExtra(SHOW_SECOND_EDIT_TEXT_POP_UP_EDIT, showSecondEditText)
            intent.putExtra(HINT_EDIT_TEXT_POP_UP_EDIT, editTextHint)
            intent.putExtra(KEYBOARD_EDIT_TEXT_POP_UP_EDIT, editTextKeyboard)
            intent.putExtra(HINT_EDIT_TEXT_2_POP_UP_EDIT, editText2Hint)
            fragment.startActivityForResult(intent, requestCode)
        }
    }


    override fun setView(
        title: String?,
        subTitle1: String?,
        icon:Int?,
        submitBtnTitle: String?,
        editTextValue:String?,
        showSecondEditText:Boolean,
        editTextHint: String?,
        editTextKeyboard :Int?,
        editText2Hint: String?
    ) {
        if (!TextUtils.isEmpty(title)) {
            pop_up_edit_title.text = (title)
        } else {
            pop_up_edit_title.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(subTitle1)) {
            pop_up_edit_sub_title1.text = (subTitle1)
        } else {
            pop_up_edit_sub_title1.setVisibility(View.GONE)
        }

        if (icon!=null && icon !=0) {
            iv_pop_up_edit_editing.setImageDrawable(ContextCompat.getDrawable(this,icon!!))
            iv_pop_up_2_edit_editing.setImageDrawable(ContextCompat.getDrawable(this,icon!!))
        } else {
            iv_pop_up_edit_editing.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(submitBtnTitle)) {
            btn_pop_up_edit_submit.text = (submitBtnTitle)
        } else {
            btn_pop_up_edit_submit.setVisibility(View.GONE)
        }

        if (!TextUtils.isEmpty(editTextValue)) {
            et_pop_up_edit_editing.setText(editTextValue)
        }

        if(!showSecondEditText){
            cl_pop_up_2_edit_editing.visibility=View.GONE
        }

        if(editTextHint!=null){
            et_pop_up_edit_editing.hint = editTextHint
            if(editText2Hint==null) et_pop_up_2_edit_editing.hint = editTextHint else et_pop_up_2_edit_editing.hint = editText2Hint
        }

        if(editTextKeyboard!=null){
            et_pop_up_edit_editing.inputType = editTextKeyboard
            et_pop_up_2_edit_editing.inputType = editTextKeyboard
        }

    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

}