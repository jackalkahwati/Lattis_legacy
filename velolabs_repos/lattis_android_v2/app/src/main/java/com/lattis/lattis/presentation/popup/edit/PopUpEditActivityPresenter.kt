package com.lattis.lattis.presentation.popup.edit

import android.os.Bundle
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.popup.PopUpActivity
import javax.inject.Inject
import javax.inject.Named

class PopUpEditActivityPresenter @Inject constructor(
): ActivityPresenter<PopUpEditActivityView>(){


    private var title: String? = null
    private var subTitle1: String? = null
    private var icon:Int? = null
    private var submitBtnTitle: String? = null
    private var editTextValue:String? = null
    private var editTextHint:String? = null
    private var editText2Hint:String? = null
    private var editTextKeyboard:Int? = null
    private var showSecondEditText:Boolean = false


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            if (arguments.containsKey(PopUpEditActivity.TITLE_POP_UP_EDIT)) {
                title = arguments.getString(PopUpEditActivity.TITLE_POP_UP_EDIT)
            }
            if (arguments.containsKey(PopUpEditActivity.SUBTITLE1_POP_UP_EDIT)) {
                subTitle1 = arguments.getString(PopUpEditActivity.SUBTITLE1_POP_UP_EDIT)
            }
            if (arguments.containsKey(PopUpEditActivity.ICON_POP_UP_EDIT)) {
                icon = arguments.getInt(PopUpEditActivity.ICON_POP_UP_EDIT)
            }
            if (arguments.containsKey(PopUpEditActivity.SUBMIT_POP_UP_EDIT)) {
                submitBtnTitle = arguments.getString(PopUpEditActivity.SUBMIT_POP_UP_EDIT)
            }
            if (arguments.containsKey(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)) {
                editTextValue = arguments.getString(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            }

            if (arguments.containsKey(PopUpEditActivity.SHOW_SECOND_EDIT_TEXT_POP_UP_EDIT)) {
                showSecondEditText = arguments.getBoolean(PopUpEditActivity.SHOW_SECOND_EDIT_TEXT_POP_UP_EDIT)
            }

            if (arguments.containsKey(PopUpEditActivity.HINT_EDIT_TEXT_POP_UP_EDIT)) {
                editTextHint = arguments.getString(PopUpEditActivity.HINT_EDIT_TEXT_POP_UP_EDIT)
            }
            if (arguments.containsKey(PopUpEditActivity.HINT_EDIT_TEXT_2_POP_UP_EDIT)) {
                editText2Hint = arguments.getString(PopUpEditActivity.HINT_EDIT_TEXT_2_POP_UP_EDIT)
            }
            if (arguments.containsKey(PopUpEditActivity.KEYBOARD_EDIT_TEXT_POP_UP_EDIT)) {
                editTextKeyboard = arguments.getInt(PopUpEditActivity.KEYBOARD_EDIT_TEXT_POP_UP_EDIT)
            }
            view?.setView(title, subTitle1,icon,submitBtnTitle,editTextValue,showSecondEditText,editTextHint,editTextKeyboard,editText2Hint)
        }
    }
}