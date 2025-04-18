package io.lattis.operator.presentation.popup

import android.os.Bundle
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import javax.inject.Inject

class PopUpActivityPresenter @Inject internal constructor() :
    ActivityPresenter<PopUpActivityView>() {
    private var title: String? = null
    private var subTitle1: String? = null
    private var subTitle2: String? = null
    private var actionBtnPositive1: String? = null
    private var actionBtnPositive2: String? = null
    private var actionBtnPositive3: String? = null
    private var actionBtnNegative: String? = null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            if (arguments.containsKey(PopUpActivity.TITLE_POP_UP)) {
                title = arguments.getString(PopUpActivity.TITLE_POP_UP)
            }
            if (arguments.containsKey(PopUpActivity.SUBTITLE1_POP_UP)) {
                subTitle1 = arguments.getString(PopUpActivity.SUBTITLE1_POP_UP)
            }
            if (arguments.containsKey(PopUpActivity.SUBTITLE2_POP_UP)) {
                subTitle2 = arguments.getString(PopUpActivity.SUBTITLE2_POP_UP)
            }
            if (arguments.containsKey(PopUpActivity.ACTIONBTN_POSITIVE1)) {
                actionBtnPositive1 = arguments.getString(PopUpActivity.ACTIONBTN_POSITIVE1)
            }
            if (arguments.containsKey(PopUpActivity.ACTIONBTN_POSITIVE2)) {
                actionBtnPositive2 = arguments.getString(PopUpActivity.ACTIONBTN_POSITIVE2)
            }
            if (arguments.containsKey(PopUpActivity.ACTIONBTN_POSITIVE3)) {
                actionBtnPositive3 = arguments.getString(PopUpActivity.ACTIONBTN_POSITIVE3)
            }
            if (arguments.containsKey(PopUpActivity.ACTIONBTN_NEGATIVE)) {
                actionBtnNegative = arguments.getString(PopUpActivity.ACTIONBTN_NEGATIVE)
            }
            view!!.setView(title, subTitle1, subTitle2, actionBtnPositive1,actionBtnPositive2,actionBtnPositive3, actionBtnNegative)
        }
    }

    override fun updateViewState() {}
}