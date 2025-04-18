package com.lattis.lattis.presentation.base.fragment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.base.DataView
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import dagger.android.support.AndroidSupportInjection
import io.lattis.lattis.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_pop_up.*


abstract class BaseFragment<Presenter : FragmentPresenter<V>, V : BaseView> :
    Fragment(), BaseView, View.OnClickListener {

    protected var meVisible = false
    protected var subscriptions = CompositeDisposable()
    private var parentDataView: DataView<*>? = null
    protected open lateinit var view: V
    var menuId = -1


    protected abstract val presenter: Presenter
    @get:LayoutRes
    protected abstract val fragmentLayoutId: Int

    open fun configureViews() {}
    protected fun configureSubscriptions() {}
    protected fun getToolbar(toolbar: Toolbar?) {}
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        if (menuId != menuId) {
            inflater.inflate(menuId, menu)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
        if (context is DataView<*>) {
            parentDataView = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        parentDataView = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val presenter = presenter

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(fragmentLayoutId, container, false)
        return view
    }

    override fun onViewCreated(
        viewInCreated: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(viewInCreated, savedInstanceState)

        if (savedInstanceState == null) {
            presenter?.onCreate(arguments, view)
        } else {
            presenter?.onRecreate(savedInstanceState, view)
        }

        configureViews()
        setHasOptionsMenu(menuId != menuId)
    }

    override fun onResume() {
        super.onResume()
        presenter?.onResume(view)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
        subscriptions.clear()


        meVisible = false
        val isFinishing = activity?.isFinishing?:false || isRemoving
        presenter?.onDestroy(isFinishing)
    }

    override fun showLoading(hideContent: Boolean) {
        if (parentDataView != null) {
            parentDataView?.showLoading(hideContent)
        }
    }

    override fun setToolbarDescription(subtitle: String) {
        if (parentDataView != null) {
            parentDataView?.setToolbarDescription(subtitle)
        }
    }

    override fun setToolbarHeader(title: String) {
        if (parentDataView != null) {
            parentDataView?.setToolbarHeader(title)
        }
    }

    override fun hideLoading() {
        if (parentDataView != null) {
            parentDataView?.hideLoading()
        }
    }

    override fun hideToolbar() {
        if (parentDataView != null) {
            parentDataView?.hideToolbar()
        }
    }

    override fun hideKeyboard() {
        val currentFocus = activity?.currentFocus
        if (currentFocus != null) {
            val inputMethodManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity?.currentFocus?.windowToken,
                0
            )
        }
    }

    override fun onClick(v: View) {
        presenter?.onClick(v.id)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        meVisible = !hidden
    }

    protected fun logCustomException(e: Throwable?) {
        presenter?.logCustomException(e)
    }

    abstract fun onMapboxMoved(latlongBounds: LatLngBounds)
    abstract fun onMapClicked(@NonNull screenPoint: PointF)


    protected fun showServerGeneralError(requestCode: Int){
        launchPopUpActivity(
            requestCode,
            getString(R.string.general_error_title),
            getString(R.string.general_error_message),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }


    ///// launch pop up activity
    protected fun launchPopUpActivity(
        requestCode: Int,
        title: String?,
        subTitle1: String?,
        subTitle2: String?,
        actionBtnPositive1: String?,
        actionBtnPositive2: String?,
        actionBtnPositive3: String?,
        actionBtnNegative: String?
    ): Unit {

        PopUpActivity.launchForResultFromFragment(
            this,
            activity,
            requestCode,
            title,
            subTitle1,
            subTitle2,
            actionBtnPositive1,
            actionBtnPositive2,
            actionBtnPositive3,
            actionBtnNegative
        )
//        val intent = Intent(activity, PopUpActivity::class.java)
//        intent.putExtra(PopUpActivity.TITLE_POP_UP, title)
//        intent.putExtra(PopUpActivity.SUBTITLE1_POP_UP, subTitle1)
//        intent.putExtra(PopUpActivity.SUBTITLE2_POP_UP, subTitle2)
//        intent.putExtra(PopUpActivity.ACTIONBTN_POSITIVE1, actionBtnPositive1)
//        intent.putExtra(PopUpActivity.ACTIONBTN_POSITIVE2, actionBtnPositive2)
//        intent.putExtra(PopUpActivity.ACTIONBTN_POSITIVE3, actionBtnPositive3)
//        intent.putExtra(PopUpActivity.ACTIONBTN_NEGATIVE, actionBtnNegative)
//        startActivityForResult(intent, requestCode)
    }

    //// Local notification

    fun createLocalNotification(
        title: String,
        message: String,
        channelId: String,
        channelName: String,
        notificationID: Int
    ){

        val b: NotificationCompat.Builder = NotificationCompat.Builder(requireContext(), channelId)

        b.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.notification_icon)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)

        val importance = NotificationManager.IMPORTANCE_HIGH

        val mNotificationManager = (requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                channelId, channelName, importance
            )
            mNotificationManager.createNotificationChannel(mChannel)
        }

        mNotificationManager.notify(
            notificationID,
            b.build()
        )
    }

    fun removeNotification(notificationID: Int){
        NotificationManagerCompat.from(requireContext()).cancel(notificationID)
    }

}