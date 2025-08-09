package com.cafeed28.omori;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class GameActivity extends Activity {
    private OmoWebView mWebView;
    private Dialog mMenuDialog;
    private Dialog mQuitDialog;

    private void hideSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mWebView = findViewById(R.id.webView);
        mWebView.setOnCloseWindowListener(this::finishAndRemoveTask);

        ViewCompat.setOnApplyWindowInsetsListener(mWebView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            // i fucking hate android
            if (insets.left > 0) mlp.leftMargin = insets.left;
            else if (insets.right > 0) mlp.rightMargin = insets.right;

            v.setLayoutParams(mlp);

            return WindowInsetsCompat.CONSUMED;
        });

        mWebView.start();

        final CharSequence[] menuItems = new CharSequence[] {"Toggle FPS counter", "Toggle touch input", "Edit controls", "Quit game"};

        mMenuDialog = new AlertDialog.Builder(this)
                .setTitle("Menu")
                .setItems(menuItems, (d, w) -> {
                    switch (w) {
                        case 0: // Toggle FPS counter
                            mWebView.eval("Graphics._toggleFPSCounter();");
                            break;
                        case 1: // Toggle touch input
                            mWebView.eval("TouchInput._toggleTouchInput();");
                            break;
                        case 2: // Edit controls
                            mWebView.eval("Input._editControls();");
                            break;
                        case 3: // Quit game
                            mQuitDialog.show();
                            break;
                    }
                    d.dismiss();
                })
                .create();

        mQuitDialog = new AlertDialog.Builder(this)
                .setTitle("Quit game")
                .setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", (d, w) -> finishAndRemoveTask())
                .setNegativeButton("No", (d, w) -> d.cancel())
                .create();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            try {
                ViewGroup contentView = (ViewGroup) mWebView.getParent();
                contentView.removeView(mWebView);

                // causes "Renderer process crash detected (code -1)"
                // chatgpt says it is normal when we're calling destroy
                // i didn't found any proof of it in documentation or elsewhere
                mWebView.destroy();
                mWebView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();

        if (mWebView != null) {
            mWebView.onResume();
            mWebView.eval("window.nw.Window.get().dispatchEvent(new Event('restore'));");
        }
    }

    @Override
    protected void onPause() {
        if (mWebView != null) {
            mWebView.eval("window.nw.Window.get().dispatchEvent(new Event('minimize'));");
            mWebView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        mMenuDialog.show();
    }
}