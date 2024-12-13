package cn.navibeidou.beidou;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import cn.navibeidou.beidou.Util.SpUtil;

public class RegisterActivity extends AppCompatActivity {


//    private MyCountDownTimer mTime;

    private SimpleToolbar mSimpleToolbar;

    private EditText phone;
    //    private EditText SMSCode;
    private EditText password, d, t;
    private EditText password2;
    private String strd, strt;
    //    private Button mGetSMSButton;
    //private Button btLogon;
    private String regextPhoneNumgber = "1[13568][\\d]{9}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logon);

        mSimpleToolbar = findViewById(R.id.simple_toolbar);
        phone = (EditText) findViewById(R.id.editPhone);
//        SMSCode = (EditText) findViewById(R.id.editSMS);
        password = (EditText) findViewById(R.id.editPassword);
        password2 = (EditText) findViewById(R.id.editPassword2);
        d = (EditText) findViewById(R.id.editD);
        t = (EditText) findViewById(R.id.editT);
        String tel = getIntent().getStringExtra("tel");
        phone.setText(tel);
        d.setEnabled(false);
        t.setEnabled(false);
        strd = SpUtil.get(getApplication(), "register_d", "").toString();
        strt = SpUtil.get(getApplication(), "register_t", "").toString();
        d.setText(strd);
        t.setText(strt);
//        mGetSMSButton = findViewById(R.id.buttonSMS);
        //btLogon = findViewById(R.id.buttonLogon);

//        mSimpleToolbar.setMainTitle(getResources().getString(R.string.title_activity_logon));
//        mSimpleToolbar.setLeftTitleText(getResources().getString(R.string.toolbar_back));

        mSimpleToolbar.setLeftTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });
    }

    public void LogonOnClick(View view) {
        phone.setText(phone.getText().toString().trim().replaceAll("\\s*", ""));
        if (!phone.getText().toString().matches(regextPhoneNumgber)) {
//            SnackbarUtil.Message(view, getResources().getString(R.string.phonenumber));
            return;
        }
        if (password.getText().toString().length() == 0) {
//            SnackbarUtil.Message(view, getResources().getString(R.string.password));
            return;
        }
        if (password2.getText().toString().length() == 0) {
//            SnackbarUtil.Message(view, getResources().getString(R.string.password2));
            return;
        }
        if (password.getText().toString().length() < 6 || password.getText().toString().length() > 20) {
//            SnackbarUtil.Message(view, getResources().getString(R.string.password_lengh));
            return;
        }
        if (!password.getText().toString().equals(password2.getText().toString())) {
//            SnackbarUtil.Message(view, getResources().getString(R.string.check_password));
            return;
        }

    }

}

