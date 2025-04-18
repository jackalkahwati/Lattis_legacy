package io.lattis.operator.presentation.ui.base.activity


import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import io.lattis.operator.presentation.base.BaseView
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import dagger.android.AndroidInjection
import io.lattis.operator.BuildConfig
import io.lattis.operator.BuildConfig.APPLICATION_ID
import io.lattis.operator.R
import io.lattis.operator.presentation.base.ConnectivityChangeReceiver
import io.lattis.operator.presentation.popup.PopUpActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_loading.*
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.no_internal_layout.*
import kotlinx.android.synthetic.main.view_progress_bar.*
import kotlinx.android.synthetic.main.view_toolbar.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

open abstract class BaseActivity<Presenter : ActivityPresenter<V>, V:BaseView> :
    AppCompatActivity(),
    BaseView, View.OnClickListener, ConnectivityChangeReceiver.OnConnectivityChangedListener {

    protected var subscriptions = CompositeDisposable()
    protected open lateinit var view: V
    protected open lateinit var presenter: Presenter


    private val NO_STUB_VIEW = -1
    protected open val viewStubLayoutId = NO_STUB_VIEW
    protected open val viewStubIdFullScreen = NO_STUB_VIEW
    protected open val viewStubIdFitScreen = NO_STUB_VIEW


//    @get:DrawableRes
//    protected val homeAsUpIndicator = -1


    ////// CODE FOR INTERNET CONNECTION ////////////
    private var connectivityChangeReceiver: ConnectivityChangeReceiver? = null


    @get:LayoutRes
    protected abstract val activityLayoutId: Int

    protected abstract fun onInternetConnectionChanged(isConnected: Boolean)


    open fun configureViews() {
//        setupAppbar(toolbar, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        configureWindow()
        setContentView(activityLayoutId)
        if (viewStubIdFullScreen != NO_STUB_VIEW) {
            val stub = findViewById<View>(viewStubIdFullScreen) as ViewStub
            stub.layoutResource = viewStubLayoutId
            stub.inflate()
        }else if (viewStubIdFitScreen != NO_STUB_VIEW) {
            val stub = findViewById<View>(viewStubIdFitScreen) as ViewStub
            stub.layoutResource = viewStubLayoutId
            stub.inflate()
        }
        if (savedInstanceState != null) {
            presenter.onReCreate(savedInstanceState, view)
        } else {
            presenter.onCreate(intent.extras, view)
        }
        configureViews()
        configureSubscriptions()

    }

    private fun startListeningToInternet() { ////// CODE FOR INTERNET CONNECTION ////////////
        connectivityChangeReceiver = ConnectivityChangeReceiver(this)
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(connectivityChangeReceiver, filter)
    }

    ////// CODE FOR INTERNET CONNECTION ////////////
    override fun onConnectivityChanged(isConnected: Boolean) { // TODO handle connectivity change
        if (rl_no_internet != null) {
            if (isConnected) rl_no_internet?.visibility = View.GONE else rl_no_internet?.visibility = View.VISIBLE
        }
        onInternetConnectionChanged(isConnected)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)
        presenter.onReenter()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume(view)
//        getToolbar(toolbar)
        startListeningToInternet()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveArguments(outState)
    }

    override fun onPause() {
        super.onPause()
        ////// CODE FOR INTERNET CONNECTION ////////////
        if (connectivityChangeReceiver != null) {
            unregisterReceiver(connectivityChangeReceiver)
            connectivityChangeReceiver = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
        presenter.onDestroy()
        presenter.onDestroy(isFinishing)
    }

    override fun finish() {
        super.finish()
        subscriptions.clear()
    }

//    override fun showLoading(hideContent: Boolean) {
//        presenter.setLoading(true)
//        if (progressBarBackground != null) {
//            progressBarBackground?.setBackgroundColor(
//                if (hideContent) ContextCompat.getColor(
//                    this,
//                    R.color.white
//                ) else ContextCompat.getColor(
//                    this,
//                    android.R.color.transparent
//                )
//            )
//            progressBarBackground?.visibility = View.VISIBLE
//        }
//        invalidateOptionsMenu()
//    }
//
//    override fun hideLoading() {
//        presenter.setLoading(false)
//        if (progressBarBackground != null) {
//            progressBarBackground?.visibility = View.GONE
//        }
//        invalidateOptionsMenu()
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.onClick(item.itemId)
        return false
    }


    override fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = currentFocus
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(
                window.decorView.windowToken,
                0
            )
        }
    }

    override fun onClick(v: View) {
        presenter.onClick(v.id)
    }

    open fun configureSubscriptions() {}


//    protected fun setToolBarBackGround(bgColor: Int) {
//        toolbar?.setBackgroundColor(bgColor)
//    }
//    protected open fun getToolbar(toolbar: Toolbar?) {}
//    protected fun setupAppbar(
//        toolbar: Toolbar?,
//        homeAsUpEnabled: Boolean
//    ) {
//        if (toolbar != null) {
//            setSupportActionBar(toolbar)
//            toolbar.title = ""
//            val actionBar = supportActionBar
//            if (actionBar != null) {
//                actionBar.setHomeButtonEnabled(true)
//                actionBar.setDisplayHomeAsUpEnabled(homeAsUpEnabled)
//                if (homeAsUpIndicator != homeAsUpIndicator) {
//                    actionBar.setHomeAsUpIndicator(homeAsUpIndicator)
//                }
//            }
//            if (homeAsUpEnabled) {
//                setToolBarBackGround(Color.parseColor("#F9F9F9"))
//            } else {
//                setToolBarBackGround(Color.WHITE)
//            }
//            setToolbarBackArrowAction()
//        }
//    }
//
//    open fun setToolbarBackArrowAction() {
//        if (toolbar != null) {
//            toolbar?.setNavigationOnClickListener { v: View? -> onBackPressed() }
//        }
//    }
//    override fun setToolbarHeader(title: String) {
//        toolbar_title?.setText(title)
//        toolbar_title?.setVisibility(View.VISIBLE)
//    }
//
//    override fun hideToolbar() {
//        toolbar_title?.setText("")
//        toolbar_subtitle?.setText("")
//        toolbar_subtitle?.setVisibility(View.GONE)
//        ll_currentLocation?.visibility = View.GONE
//    }
//
//    override fun setToolbarDescription(subtitle: String) {
//        toolbar_subtitle?.setText(subtitle)
//        toolbar_title?.setVisibility(View.GONE)
//        toolbar_subtitle?.setVisibility(View.VISIBLE)
//        ll_currentLocation?.visibility = View.VISIBLE
//    }

    protected fun configureWindow() {}





    protected fun replaceFragment(@IdRes containerId: Int, fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }

    protected fun addFragment(
        @IdRes containerId: Int,
        fragment: Fragment,
        addToBackStack: Boolean
    ) {
        val fragmentTransaction =
            supportFragmentManager
                .beginTransaction()
                .add(containerId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        }
        fragmentTransaction.commit()
    }

    protected fun getFragmentTag(@IdRes containerId: Int): String? {
        return supportFragmentManager.findFragmentById(containerId)?.tag
    }

    protected fun getFragment(@IdRes containerId: Int): Fragment? {
        return supportFragmentManager.findFragmentById(containerId)
    }

    protected fun clearBackStack() {
        supportFragmentManager.popBackStack()
    }


    fun finishMe(){
        Observable.timer(
            500.toLong(),
            TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                finish()
            }) { throwable ->

            }
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

        PopUpActivity.launchForResult(this,requestCode,title,subTitle1,subTitle2,actionBtnPositive1,actionBtnPositive2,actionBtnPositive3,actionBtnNegative)
    }

//    protected fun showServerGeneralError(requestCode: Int){
//        launchPopUpActivity(
//            requestCode,
//            getString(R.string.general_status_error_title),
//            getString(R.string.general_error_message),
//            null,
//            getString(R.string.general_btn_ok),
//            null,
//            null,
//            null
//        )
//    }

}