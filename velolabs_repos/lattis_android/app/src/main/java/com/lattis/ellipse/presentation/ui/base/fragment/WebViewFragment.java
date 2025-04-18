package com.lattis.ellipse.presentation.ui.base.fragment;

import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lattis.ellipse.R;

public class WebViewFragment extends Fragment {

    private final static String ARG_URL = "ARG_URL";

    @BindView(R.id.webview)
    WebView mWebView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBarView;

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            mProgressBarView.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);

            final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewFragment.this.getContext());
            builder.setMessage(R.string.notification_error_ssl_cert_invalid);
            builder.setPositiveButton(R.string.btn_continue, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
        }

    };

    private String mUrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            mUrl = args.getString(ARG_URL);
        } else {
            mUrl = savedInstanceState.getString(ARG_URL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWebView.setWebViewClient(mWebViewClient);
        mWebView.loadUrl(mUrl);
    }

    @Override
    public void onResume() {
        mWebView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_URL, mUrl);
    }

    public static WebViewFragment newInstance(final String url) {
        WebViewFragment f = new WebViewFragment();

        // Put parameters to fragment
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        f.setArguments(args);

        return f;
    }

}
