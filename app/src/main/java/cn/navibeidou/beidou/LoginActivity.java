package cn.navibeidou.beidou;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.navibeidou.beidou.account.AuthApi;
import cn.navibeidou.beidou.account.UserSession;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_REQUIRE_LOGIN = "require_login";

    private final AuthApi authApi = new AuthApi();
    private EditText phoneInput;
    private EditText codeInput;
    private EditText passwordInput;
    private Button codeButton;
    private Button submitButton;
    private Button modeButton;
    private TextView errorView;
    private CheckBox agreementCheckBox;
    private boolean codeMode = true;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_login);
        StatusNavUtils.setStatusBarColor(this, 0x33000000);

        phoneInput = findViewById(R.id.login_phone);
        codeInput = findViewById(R.id.login_code);
        passwordInput = findViewById(R.id.login_password);
        codeButton = findViewById(R.id.login_get_code);
        submitButton = findViewById(R.id.login_submit);
        modeButton = findViewById(R.id.login_switch_mode);
        errorView = findViewById(R.id.login_error);
        agreementCheckBox = findViewById(R.id.login_agreement);

        findViewById(R.id.login_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.login_user_agreement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ProtocolActivity.class).putExtra("service", true));
            }
        });
        findViewById(R.id.login_privacy_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ProtocolActivity.class).putExtra("yinsi", true));
            }
        });
        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCode();
            }
        });
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeMode = !codeMode;
                refreshMode();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLogin();
            }
        });
        refreshMode();
    }

    private void refreshMode() {
        findViewById(R.id.login_code_row).setVisibility(codeMode ? View.VISIBLE : View.GONE);
        passwordInput.setVisibility(codeMode ? View.GONE : View.VISIBLE);
        modeButton.setText(codeMode ? "使用密码登录" : "使用验证码登录");
        showError(null);
    }

    private String normalizedPhone() {
        return phoneInput.getText().toString().replace(" ", "").replace("-", "")
                .replaceFirst("^\\+?86", "");
    }

    private boolean validPhone(String phone) {
        return phone.matches("^1[3-9]\\d{9}$");
    }

    private void requestCode() {
        String phone = normalizedPhone();
        if (!validPhone(phone)) {
            showError("请输入正确的手机号");
            return;
        }
        setCodeButtonLoading(true);
        authApi.sendSmsCode(phone, new AuthApi.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                startCountdown();
                Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                setCodeButtonLoading(false);
                showError(message);
            }
        });
    }

    private void submitLogin() {
        if (!agreementCheckBox.isChecked()) {
            showError("请先同意用户协议和隐私政策");
            return;
        }
        final String phone = normalizedPhone();
        if (!validPhone(phone)) {
            showError("请输入正确的手机号");
            return;
        }
        setLoading(true);
        AuthApi.ResultCallback<AuthApi.LoginResult> callback = new AuthApi.ResultCallback<AuthApi.LoginResult>() {
            @Override
            public void onSuccess(AuthApi.LoginResult loginResult) {
                new UserSession(LoginActivity.this).login(loginResult.phone, loginResult.accessToken);
                setResult(RESULT_OK);
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                showError(message);
            }
        };
        if (codeMode) {
            String code = codeInput.getText().toString().trim();
            if (code.length() != 6) {
                setLoading(false);
                showError("请输入6位验证码");
                return;
            }
            authApi.loginByCode(phone, code, callback);
        } else {
            String password = passwordInput.getText().toString();
            if (password.length() < 6) {
                setLoading(false);
                showError("密码不能少于6位");
                return;
            }
            authApi.loginByPassword(phone, password, callback);
        }
    }

    private void setLoading(boolean loading) {
        submitButton.setEnabled(!loading);
        submitButton.setText(loading ? "登录中…" : "登录");
        modeButton.setEnabled(!loading);
        codeButton.setEnabled(!loading && countDownTimer == null);
    }

    private void setCodeButtonLoading(boolean loading) {
        codeButton.setEnabled(!loading);
        codeButton.setText(loading ? "发送中…" : "获取验证码");
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(120000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                codeButton.setEnabled(false);
                codeButton.setText((millisUntilFinished / 1000L) + "s");
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                codeButton.setEnabled(true);
                codeButton.setText("获取验证码");
            }
        }.start();
    }

    private void showError(String message) {
        errorView.setText(TextUtils.isEmpty(message) ? "" : message);
        errorView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
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
