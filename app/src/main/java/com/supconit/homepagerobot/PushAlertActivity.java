package com.supconit.homepagerobot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PushAlertActivity extends Activity {
    static final String EXTRA_TITLE = "push_title";
    static final String EXTRA_BODY = "push_body";
    static final String EXTRA_TYPE = "push_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        configureWindow();
        renderAlert();
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }

    private void renderAlert() {
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String body = getIntent().getStringExtra(EXTRA_BODY);
        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (title == null || title.isEmpty()) title = "机器人节点监控";
        if (body == null || body.isEmpty()) body = "收到一条推送消息";

        int accent = "offline".equals(type) ? Color.rgb(255, 105, 110) : Color.rgb(32, 200, 255);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(36, 28, 36, 28);
        root.setBackgroundColor(Color.rgb(2, 9, 20));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(34, 28, 34, 28);
        panel.setBackgroundColor(Color.rgb(5, 22, 48));
        root.addView(panel, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView badge = new TextView(this);
        badge.setText("设备消息提醒");
        badge.setTextColor(accent);
        badge.setTextSize(13);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        panel.addView(badge);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Color.rgb(238, 252, 255));
        titleView.setTextSize(26);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setPadding(0, 12, 0, 10);
        panel.addView(titleView);

        TextView bodyView = new TextView(this);
        bodyView.setText(body);
        bodyView.setTextColor(Color.rgb(184, 230, 248));
        bodyView.setTextSize(17);
        bodyView.setLineSpacing(4, 1);
        panel.addView(bodyView);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.RIGHT);
        actions.setPadding(0, 26, 0, 0);
        panel.addView(actions);

        Button closeButton = new Button(this);
        closeButton.setText("关闭");
        closeButton.setTextColor(Color.rgb(230, 246, 255));
        closeButton.setBackgroundColor(Color.rgb(31, 48, 72));
        closeButton.setOnClickListener(v -> finish());
        actions.addView(closeButton, new LinearLayout.LayoutParams(150, 54));

        Button openButton = new Button(this);
        openButton.setText("打开监控");
        openButton.setTextColor(Color.WHITE);
        openButton.setBackgroundColor(accent);
        openButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(190, 54);
        openParams.setMargins(14, 0, 0, 0);
        actions.addView(openButton, openParams);

        setContentView(root);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        }
    }
}
