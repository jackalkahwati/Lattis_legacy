package cc.skylock.skylock.ui.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 15-11-2016.
 */

public class WebviewFragment extends Fragment {
    View view;
    private WebView webView = null;
    private static int loadURL;
    ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_webview, null);
        Bundle bundle = getArguments();
        if (bundle != null) {
            loadURL = bundle.getInt("URL");
            System.out.println(loadURL);
        }
        webView = (WebView) view.findViewById(R.id.webview);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        loadWebViewLoad();
        return view;
    }

    private void loadWebViewLoad() {

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.notification_error_ssl_cert_invalid);
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        if (loadURL == 1)
            webView.loadUrl(SkylockConstant.BASE_URL_HELP);
        else
            webView.loadUrl(SkylockConstant.BASE_URL_ORDER);


    }


    @Override
    public void onResume() {
        super.onResume();
        if (loadURL == 1)
            ((HomePageActivity) getActivity()).changeHeaderUI("HELP",
                    ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null),
                    Color.WHITE);
        else
            ((HomePageActivity) getActivity()).changeHeaderUI("ORDER YOUR ELLIPSE NOW",
                    ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null),
                    Color.WHITE);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UtilHelper.analyticTrackUserAction("Help screen open", "Custom", "", null, "ANDROID");
    }
}
