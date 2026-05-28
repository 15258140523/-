package com.supconit.homepagerobot;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity {
    private static final String TAG = "HomepageRobot";
    private static final String HOME_URL = "http://172.19.8.25:5173/home";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1001;
    private static final String PREFS_NAME = "robot_app";
    private static final String PREF_GETUI_CID = "getui_cid";
    private static WeakReference<MainActivity> activeActivity;

    private FrameLayout rootView;
    private WebView webView;
    private ProgressBar progressBar;
    private TextView errorView;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        rootView = new FrameLayout(this);
        rootView.setBackgroundColor(Color.rgb(2, 9, 20));
        setContentView(rootView);

        setupWebView();
        setupProgressBar();
        setupErrorView();
        setupNotifications();
        hideSystemBars();
        activeActivity = new WeakReference<>(this);
        loadHome();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(2, 9, 20));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.addJavascriptInterface(new AppBridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                emitGetuiClientId();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    showLoadError();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                rootView.addView(customView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                webView.setVisibility(View.GONE);
                hideSystemBars();
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }
        });

        rootView.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void setupProgressBar() {
        progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(56, 56);
        params.gravity = Gravity.CENTER;
        rootView.addView(progressBar, params);
    }

    private void setupErrorView() {
        errorView = new TextView(this);
        errorView.setGravity(Gravity.CENTER);
        errorView.setTextColor(Color.rgb(220, 247, 255));
        errorView.setTextSize(15);
        errorView.setPadding(32, 24, 32, 24);
        errorView.setVisibility(View.GONE);
        errorView.setOnClickListener(v -> loadHome());
        rootView.addView(errorView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void loadHome() {
        progressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(HOME_URL);
    }

    private void showLoadError() {
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        String message = isNetworkAvailable()
                ? "页面加载失败，点击重试\n" + HOME_URL
                : "网络不可用，点击重试";
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (manager == null) return false;
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void setupNotifications() {
        NotificationHelper.ensureChannel(this);
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void showPushNotification(String title, String body, String type, String key) {
        NotificationHelper.showNotification(this, title, body, type, key);
    }

    public static void saveGetuiClientId(Context context, String cid) {
        if (context == null || cid == null || cid.isEmpty()) return;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_GETUI_CID, cid)
                .apply();
        Log.i(TAG, "Getui CID = " + cid);
    }

    public static void onGetuiClientId(String cid) {
        MainActivity activity = activeActivity == null ? null : activeActivity.get();
        if (activity == null) return;

        activity.runOnUiThread(() -> {
            activity.saveGetuiClientId(activity, cid);
            activity.emitGetuiClientId();
        });
    }

    public static void onGetuiTransmission(String payload) {
        MainActivity activity = activeActivity == null ? null : activeActivity.get();
        if (activity == null) return;

        activity.runOnUiThread(() -> {
            activity.emitGetuiPayload(payload);
        });
    }

    private String getGetuiClientId() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_GETUI_CID, "");
    }

    private void emitGetuiClientId() {
        if (webView == null) return;
        String cid = getGetuiClientId();
        if (cid == null || cid.isEmpty()) return;
        evaluateEvent("getui-cid", cid);
    }

    private void emitGetuiPayload(String payload) {
        evaluateEvent("getui-message", payload);
    }

    private void evaluateEvent(String eventName, String detail) {
        if (webView == null) return;
        try {
            String script = "window.dispatchEvent(new CustomEvent("
                    + JSONObject.quote(eventName)
                    + ",{detail:"
                    + JSONObject.quote(detail == null ? "" : detail)
                    + "}));";
            webView.evaluateJavascript(script, null);
        } catch (Exception error) {
            Log.w(TAG, "emit event failed", error);
        }
    }

    private class AppBridge {
        @JavascriptInterface
        public void requestNotificationPermission() {
            runOnUiThread(() -> requestNotificationPermissionIfNeeded());
        }

        @JavascriptInterface
        public void pushMessage(String title, String body, String type, String key) {
            runOnUiThread(() -> showPushNotification(title, body, type, key));
        }

        @JavascriptInterface
        public String getGetuiClientId() {
            return MainActivity.this.getGetuiClientId();
        }

        @JavascriptInterface
        public void notifyDeviceOnline(String title, String body) {
            runOnUiThread(() -> showPushNotification(title, body, "online", body));
        }

        @JavascriptInterface
        public void notifyDeviceOffline(String title, String body) {
            runOnUiThread(() -> showPushNotification(title, body, "offline", body));
        }
    }

    private void hideCustomView() {
        if (customView == null) return;
        rootView.removeView(customView);
        customView = null;
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }
        webView.setVisibility(View.VISIBLE);
        hideSystemBars();
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustomView();
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        if (webView != null) webView.onResume();
        emitGetuiClientId();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            rootView.removeView(webView);
            webView.destroy();
            webView = null;
        }
        MainActivity activity = activeActivity == null ? null : activeActivity.get();
        if (activity == this) {
            activeActivity = null;
        }
        super.onDestroy();
    }
}
