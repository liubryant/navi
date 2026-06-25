package cn.navibeidou.beidou;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.navibeidou.beidou.account.AuthApi;
import cn.navibeidou.beidou.account.UserSession;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

public class AccountActivity extends AppCompatActivity {
    private static final int LOGIN_REQUEST = 2201;

    private final AuthApi authApi = new AuthApi();
    private UserSession userSession;
    private LinearLayout loggedInSection;
    private LinearLayout setPasswordSection;
    private TextView phoneView;
    private TextView errorView;
    private EditText codeInput;
    private EditText passwordInput;
    private Button codeButton;
    private Button submitPasswordButton;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);
        userSession = new UserSession(this);

        loggedInSection = findViewById(R.id.account_logged_in_section);
        setPasswordSection = findViewById(R.id.account_set_password_section);
        phoneView = findViewById(R.id.account_phone);
        errorView = findViewById(R.id.account_error);
        codeInput = findViewById(R.id.account_code);
        passwordInput = findViewById(R.id.account_new_password);
        codeButton = findViewById(R.id.account_get_code);
        submitPasswordButton = findViewById(R.id.account_submit_password);

        findViewById(R.id.account_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.account_set_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetPasswordSection();
            }
        });
        findViewById(R.id.account_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSession.logout();
                Toast.makeText(AccountActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
                refreshState();
            }
        });
        findViewById(R.id.account_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCode(codeButton, new Runnable() {
                    @Override
                    public void run() {
                        startCountdown(codeButton);
                    }
                });
            }
        });
        submitPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPassword();
            }
        });
        refreshState();
    }

    private void refreshState() {
        boolean loggedIn = userSession.isLoggedIn();
        if (!loggedIn) {
            finish();
            return;
        }
        loggedInSection.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        setPasswordSection.setVisibility(View.GONE);
        phoneView.setText("已登录账号：" + maskPhone(userSession.getPhone()));
        showError(null);
    }

    private void showSetPasswordSection() {
        codeInput.setText("");
        passwordInput.setText("");
        setPasswordSection.setVisibility(View.VISIBLE);
        showError(null);
    }

    private void requestCode(final Button targetButton, final Runnable success) {
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        targetButton.setEnabled(false);
        targetButton.setText("发送中…");
        authApi.sendSmsCode(userSession.getPhone(), new AuthApi.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(AccountActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                success.run();
            }

            @Override
            public void onError(String message) {
                targetButton.setEnabled(true);
                targetButton.setText("获取验证码");
                showError(message);
            }
        });
    }

    private void submitPassword() {
        String code = codeInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (code.length() != 6) {
            showError("请输入6位验证码");
            return;
        }
        if (password.length() < 6) {
            showError("密码不能少于6位");
            return;
        }
        submitPasswordButton.setEnabled(false);
        submitPasswordButton.setText("设置中…");
        authApi.setPassword(userSession.getPhone(), code, password, new AuthApi.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                submitPasswordButton.setEnabled(true);
                submitPasswordButton.setText("确认设置");
                setPasswordSection.setVisibility(View.GONE);
                Toast.makeText(AccountActivity.this, "密码设置成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                submitPasswordButton.setEnabled(true);
                submitPasswordButton.setText("确认设置");
                showError(message);
            }
        });
    }

    private void showDeleteDialog() {
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        content.setPadding(padding, padding / 2, padding, 0);

        TextView warning = new TextView(this);
        warning.setText("注销后账号信息及相关数据将被永久删除，无法恢复。请验证手机号后继续。");
        warning.setTextColor(Color.parseColor("#667085"));
        warning.setTextSize(14f);
        content.addView(warning);

        final EditText deleteCodeInput = new EditText(this);
        deleteCodeInput.setHint("6位验证码");
        deleteCodeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        deleteCodeInput.setMaxLines(1);
        content.addView(deleteCodeInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52)));

        final Button deleteCodeButton = new Button(this);
        deleteCodeButton.setText("获取验证码");
        deleteCodeButton.setTextColor(Color.parseColor("#1677FF"));
        deleteCodeButton.setBackgroundResource(R.drawable.bg_login_secondary_round);
        content.addView(deleteCodeButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        final TextView deleteError = new TextView(this);
        deleteError.setTextColor(Color.parseColor("#E5484D"));
        deleteError.setTextSize(13f);
        deleteError.setVisibility(View.GONE);
        content.addView(deleteError);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("注销账号")
                .setView(content)
                .setNegativeButton("取消", null)
                .setPositiveButton("注销账号", null)
                .create();
        dialog.setOnShowListener(d -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            deleteButton.setTextColor(Color.parseColor("#E5484D"));
            deleteCodeButton.setOnClickListener(v -> requestCode(deleteCodeButton, () -> startCountdown(deleteCodeButton)));
            deleteButton.setOnClickListener(v -> {
                String code = deleteCodeInput.getText().toString().trim();
                if (code.length() != 6) {
                    deleteError.setText("请输入6位验证码");
                    deleteError.setVisibility(View.VISIBLE);
                    return;
                }
                new AlertDialog.Builder(AccountActivity.this)
                        .setTitle("确认注销账号？")
                        .setMessage("注销后账号及数据将被永久删除，且无法恢复。")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认注销", (confirmDialog, which) ->
                                executeDeleteAccount(code, dialog, deleteButton, deleteError))
                        .show();
            });
        });
        dialog.show();
    }

    private void executeDeleteAccount(String code, AlertDialog dialog, Button deleteButton, TextView deleteError) {
        deleteError.setVisibility(View.GONE);
        deleteButton.setEnabled(false);
        deleteButton.setText("注销中…");
        authApi.deleteAccount(userSession.getPhone(), code, new AuthApi.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                userSession.logout();
                Toast.makeText(AccountActivity.this, "账号已注销", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                refreshState();
            }

            @Override
            public void onError(String message) {
                deleteButton.setEnabled(true);
                deleteButton.setText("注销账号");
                deleteError.setText(TextUtils.isEmpty(message) ? "注销失败，请稍后重试" : message);
                deleteError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startCountdown(final Button targetButton) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(120000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                targetButton.setEnabled(false);
                targetButton.setText((millisUntilFinished / 1000L) + "s");
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                targetButton.setEnabled(true);
                targetButton.setText("获取验证码");
            }
        }.start();
    }

    private String maskPhone(String phone) {
        return phone != null && phone.length() == 11
                ? phone.substring(0, 3) + "****" + phone.substring(7)
                : phone;
    }

    private void showError(String message) {
        errorView.setText(TextUtils.isEmpty(message) ? "" : message);
        errorView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshState();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        super.onDestroy();
    }
}
