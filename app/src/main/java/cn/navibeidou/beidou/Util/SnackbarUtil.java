package cn.navibeidou.beidou.Util;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import cn.navibeidou.beidou.R;

public class SnackbarUtil {

    public static void MessageBottom(@NonNull View view, @NonNull CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
    }


}
