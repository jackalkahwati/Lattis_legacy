package com.lattis.lattis.presentation.webview

import android.app.Activity
import android.content.Intent
import android.view.View
import android.webkit.*
import androidx.fragment.app.Fragment
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_webview.*
import javax.inject.Inject

class WebviewActivity : BaseActivity<WebviewActivityPresenter, WebviewActivityView>(),
    WebviewActivityView {


    private val REQUEST_CODE_ERROR = 4393

    companion object{
        const val WEB_URL = "WEB_URL"

        fun launchForResult(
            activity: Activity,
            requestCode: Int,
            url: String?){
            val intent = Intent(activity, WebviewActivity::class.java)
            intent.putExtra(WEB_URL, url)
            activity.startActivityForResult(intent, requestCode)
        }

        fun launchForResultFromFragment(
            fragment: Fragment,
            activity: Activity,
            requestCode: Int,
            url: String?){
            val intent = Intent(activity, WebviewActivity::class.java)
            intent.putExtra(WEB_URL, url)
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    @Inject
    override lateinit var presenter: WebviewActivityPresenter
    override val activityLayoutId = R.layout.activity_webview
    override var view: WebviewActivityView = this

    override fun configureViews() {
        super.configureViews()
        webview_in_webview.settings.setAppCacheEnabled(false)
        webview_in_webview.settings.cacheMode= WebSettings.LOAD_NO_CACHE
        webview_in_webview.setWebViewClient(mWebViewClient)

        iv_close_in_webview.setOnClickListener {
            finish()
        }

    }

    override fun loadWebView(url: String) {
        showProgressLoading(getString(R.string.loading))
        webview_in_webview.loadUrl(url)
    }

    override fun handleError() {
        webview_in_webview.visibility=View.GONE
    }

    private val mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            hideProgressLoading()
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            hideProgressLoading()
            handleError()
        }
    }



    fun showProgressLoading(message:String){
        webview_activity_loading_view.ct_loading_title.text = message
        webview_activity_loading_view.visibility = View.VISIBLE
    }

    fun hideProgressLoading(){
        webview_activity_loading_view.visibility = View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}