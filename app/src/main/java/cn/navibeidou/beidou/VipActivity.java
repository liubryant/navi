package cn.navibeidou.beidou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import cn.navibeidou.beidou.account.UserSession;
import cn.navibeidou.beidou.pay.AlipayManager;
import cn.navibeidou.beidou.pay.PaymentApi;
import cn.navibeidou.beidou.pay.VipProduct;
import cn.navibeidou.beidou.pay.WeChatPayManager;
import cn.navibeidou.beidou.pay.WeChatPayParams;

public class VipActivity extends AppCompatActivity {
    public static final String ACTION_WX_PAY_RESULT = "cn.navibeidou.beidou.WX_PAY_RESULT";
    public static final String EXTRA_WX_ERROR_CODE = "wx_error_code";

    private static final int LOGIN_REQUEST = 1201;
    private static final int MAX_QUERY_COUNT = 6;
    private static final String RECHARGE_AGREEMENT_URL = "http://39.108.144.196/navi/recharge-agreement.html";
    private static final String VIP_SERVICE_URL = "http://39.108.144.196/navi/vip-service.html";

    private final PaymentApi paymentApi = new PaymentApi();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UserSession userSession;
    private LinearLayout productContainer;
    private TextView statusView;
    private TextView phoneView;
    private TextView memberSubtitleView;
    private TextView loginButton;
    private TextView bottomPriceView;
    private TextView bottomHintView;
    private TextView agreementTextView;
    private TextView payChannelWechatText;
    private TextView payChannelAlipayText;
    private Button payButton;
    private View payChannelWechatRow;
    private View payChannelAlipayRow;
    private View payChannelWechatCheck;
    private View payChannelAlipayCheck;
    private VipProduct selectedProduct;
    private String selectedPayChannel = "wechat";
    private String currentOrderId;
    private int queryCount;
    private boolean memberActive;
    private boolean memberStatusLoaded;

    private final BroadcastReceiver payResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int errorCode = intent.getIntExtra(EXTRA_WX_ERROR_CODE, -1);
            if (errorCode == 0 && currentOrderId != null) {
                statusView.setText("支付结果确认中…");
                queryCount = 0;
                queryOrder();
            } else if (errorCode == -2) {
                setPayEnabled(true);
                statusView.setText("已取消支付");
            } else {
                setPayEnabled(true);
                statusView.setText("支付未完成，请稍后重试");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#0D0907"));
        userSession = new UserSession(this);
        productContainer = findViewById(R.id.vip_products);
        statusView = findViewById(R.id.vip_status);
        phoneView = findViewById(R.id.vip_phone);
        memberSubtitleView = findViewById(R.id.vip_member_subtitle);
        loginButton = findViewById(R.id.vip_login_button);
        bottomPriceView = findViewById(R.id.vip_bottom_price);
        bottomHintView = findViewById(R.id.vip_bottom_hint);
        agreementTextView = findViewById(R.id.vip_agreement_text);
        payChannelWechatText = findViewById(R.id.vip_wechat_text);
        payChannelAlipayText = findViewById(R.id.vip_alipay_text);
        payButton = findViewById(R.id.vip_pay_button);
        payChannelWechatRow = findViewById(R.id.vip_pay_channel_wechat);
        payChannelAlipayRow = findViewById(R.id.vip_pay_channel_alipay);
        payChannelWechatCheck = findViewById(R.id.vip_pay_channel_wechat_check);
        payChannelAlipayCheck = findViewById(R.id.vip_pay_channel_alipay_check);
        findViewById(R.id.vip_agreement_check).setSelected(true);
        payChannelWechatRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPayChannel("wechat");
            }
        });
        payChannelAlipayRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPayChannel("alipay");
            }
        });
        selectPayChannel("wechat");

        findViewById(R.id.vip_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(VipActivity.this, LoginActivity.class)
                        .putExtra(LoginActivity.EXTRA_REQUIRE_LOGIN, true), LOGIN_REQUEST);
            }
        });
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });
        setupAgreementText();
        IntentFilter payResultFilter = new IntentFilter(ACTION_WX_PAY_RESULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(payResultReceiver, payResultFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(payResultReceiver, payResultFilter);
        }
        refreshMemberHeader();
        loadProducts();
    }

    private void refreshMemberHeader() {
        if (userSession.isLoggedIn()) {
            memberStatusLoaded = false;
            setPayEnabled(false);
            String phone = userSession.getPhone();
            String masked = phone.length() == 11
                    ? phone.substring(0, 3) + "****" + phone.substring(7)
                    : phone;
            phoneView.setText(masked);
            loginButton.setVisibility(View.GONE);
            loadMembershipStatus();
        } else {
            memberActive = false;
            memberStatusLoaded = true;
            phoneView.setText("登录后开通会员");
            loginButton.setVisibility(View.VISIBLE);
            showPayBottomState();
        }
    }

    private void loadMembershipStatus() {
        paymentApi.getMembership(userSession.getAccessToken(), new PaymentApi.ResultCallback<PaymentApi.MembershipStatus>() {
            @Override
            public void onSuccess(PaymentApi.MembershipStatus status) {
                if (status.active) {
                    memberActive = true;
                    memberStatusLoaded = true;
                    memberSubtitleView.setText("尊贵会员 · 有效期至 " + status.expiresAt);
                    memberSubtitleView.setTextColor(android.graphics.Color.parseColor("#FFEDBD"));
                    statusView.setText("会员已开通，可继续续费");
                    showPaidBottomState();
                } else {
                    memberActive = false;
                    memberStatusLoaded = true;
                    memberSubtitleView.setText("开通会员，享受专属导航权益");
                    memberSubtitleView.setTextColor(android.graphics.Color.parseColor("#CDAF7A"));
                    statusView.setText(selectedProduct == null ? "请先选择会员套餐" : "请使用套餐并支付");
                    showPayBottomState();
                }
            }

            @Override
            public void onError(String message) {
                memberStatusLoaded = true;
                statusView.setText(selectedProduct == null ? message : "请使用套餐并支付");
                showPayBottomState();
            }
        });
    }

    private void loadProducts() {
        statusView.setText("正在加载会员套餐…");
        setPayEnabled(false);
        paymentApi.getProducts(new PaymentApi.ResultCallback<List<VipProduct>>() {
            @Override
            public void onSuccess(List<VipProduct> products) {
                productContainer.removeAllViews();
                if (products.isEmpty()) {
                    showProductPlaceholder();
                    statusView.setText("暂无可购买的会员套餐");
                    return;
                }
                for (final VipProduct product : products) {
                    TextView button = new TextView(VipActivity.this);
                    button.setText(product.name + "\n¥" + product.price + "\n" + product.description);
                    button.setTextSize(12f);
                    button.setLineSpacing(dp(3), 1f);
                    button.setMaxLines(4);
                    button.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    button.setTextColor(Color.parseColor("#B6A58E"));
                    button.setGravity(Gravity.CENTER);
                    button.setPadding(dp(10), dp(10), dp(10), dp(10));
                    button.setBackgroundResource(R.drawable.selector_new_vip_item_bg_dark);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectProduct(product, v);
                        }
                    });
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(132), dp(124));
                    params.setMargins(dp(4), 0, dp(4), 0);
                    productContainer.addView(button, params);
                }
                productContainer.getChildAt(0).performClick();
                if (!memberStatusLoaded) {
                    setPayEnabled(false);
                } else if (memberActive) {
                    statusView.setText("会员已开通，可继续续费");
                    showPaidBottomState();
                } else {
                    statusView.setText(userSession.isLoggedIn() ? "请使用套餐并支付" : "支付前需要先登录");
                }
            }

            @Override
            public void onError(String message) {
                showProductPlaceholder();
                statusView.setText(message);
            }
        });
    }

    private void showProductPlaceholder() {
        productContainer.removeAllViews();
        TextView placeholder = new TextView(this);
        placeholder.setText("会员套餐\n\n等待服务端配置");
        placeholder.setGravity(Gravity.CENTER);
        placeholder.setTextColor(Color.parseColor("#9C8A75"));
        placeholder.setTextSize(14f);
        placeholder.setBackgroundResource(R.drawable.selector_new_vip_item_bg_dark);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(160), dp(112));
        params.setMargins(dp(6), 0, dp(6), 0);
        productContainer.addView(placeholder, params);
        bottomPriceView.setText("￥--");
        bottomHintView.setText("立即开通");
    }

    private void selectProduct(VipProduct product, View selectedButton) {
        selectedProduct = product;
        for (int i = 0; i < productContainer.getChildCount(); i++) {
            View child = productContainer.getChildAt(i);
            child.setSelected(child == selectedButton);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(Color.parseColor(
                        child == selectedButton ? "#864F2D" : "#B6A58E"));
            }
        }
        if (!memberStatusLoaded) {
            setPayEnabled(false);
        } else if (memberActive) {
            showPaidBottomState();
        } else {
            bottomPriceView.setText("￥" + product.price);
            bottomHintView.setText("立即开通");
            setPayEnabled(true);
        }
    }

    private void selectPayChannel(String channel) {
        selectedPayChannel = channel;
        boolean wechatSelected = "wechat".equals(channel);
        payChannelWechatRow.setSelected(wechatSelected);
        payChannelAlipayRow.setSelected(!wechatSelected);
        payChannelWechatText.setTextColor(Color.parseColor(wechatSelected ? "#864F2D" : "#FEEBB9"));
        payChannelAlipayText.setTextColor(Color.parseColor(wechatSelected ? "#FEEBB9" : "#864F2D"));
        payChannelWechatCheck.setVisibility(wechatSelected ? View.VISIBLE : View.GONE);
        payChannelAlipayCheck.setVisibility(wechatSelected ? View.GONE : View.VISIBLE);
    }

    private void setupAgreementText() {
        String text = "点击开通按钮即代表同意《充值协议》《会员服务协议》";
        SpannableString spannable = new SpannableString(text);
        setAgreementSpan(spannable, text, "《充值协议》", RECHARGE_AGREEMENT_URL);
        setAgreementSpan(spannable, text, "《会员服务协议》", VIP_SERVICE_URL);
        agreementTextView.setText(spannable);
        agreementTextView.setMovementMethod(LinkMovementMethod.getInstance());
        agreementTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private void setAgreementSpan(SpannableString spannable, String text, String label, final String url) {
        int start = text.indexOf(label);
        if (start < 0) {
            return;
        }
        int end = start + label.length();
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openAgreement(url);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.parseColor("#FFEDBD"));
                ds.setUnderlineText(false);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void openAgreement(String url) {
        startActivity(new Intent(this, WebActivity.class).putExtra("url", url));
    }

    private void startPayment() {
        if (!userSession.isLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class)
                    .putExtra(LoginActivity.EXTRA_REQUIRE_LOGIN, true), LOGIN_REQUEST);
            return;
        }
        if (memberActive) {
            statusView.setText("会员已开通，可继续续费");
            showPaidBottomState();
        }
        if (!memberStatusLoaded) {
            statusView.setText("正在确认会员状态…");
            setPayEnabled(false);
            return;
        }
        if (!userSession.canPay()) {
            userSession.logout();
            refreshMemberHeader();
            statusView.setText("登录状态已升级，请重新登录后支付");
            startActivityForResult(new Intent(this, LoginActivity.class)
                    .putExtra(LoginActivity.EXTRA_REQUIRE_LOGIN, true), LOGIN_REQUEST);
            return;
        }
        if (selectedProduct == null) {
            Toast.makeText(this, "请先选择会员套餐", Toast.LENGTH_SHORT).show();
            return;
        }
        setPayEnabled(false);
        statusView.setText("正在确认会员状态…");
        paymentApi.getMembership(userSession.getAccessToken(), new PaymentApi.ResultCallback<PaymentApi.MembershipStatus>() {
            @Override
            public void onSuccess(PaymentApi.MembershipStatus status) {
                memberStatusLoaded = true;
                if (status.active) {
                    memberActive = true;
                    memberSubtitleView.setText("尊贵会员 · 有效期至 " + status.expiresAt);
                    memberSubtitleView.setTextColor(android.graphics.Color.parseColor("#FFEDBD"));
                } else {
                    memberActive = false;
                }
                createOrderAfterMembershipChecked();
            }

            @Override
            public void onError(String message) {
                memberStatusLoaded = true;
                statusView.setText(message);
                showPayBottomState();
            }
        });
    }

    private void createOrderAfterMembershipChecked() {
        if (selectedProduct == null) {
            Toast.makeText(this, "请先选择会员套餐", Toast.LENGTH_SHORT).show();
            showPayBottomState();
            return;
        }
        setPayEnabled(false);
        statusView.setText("正在创建安全支付订单…");
        paymentApi.createOrder(userSession.getAccessToken(), selectedProduct.id, selectedPayChannel,
                new PaymentApi.ResultCallback<PaymentApi.OrderResult>() {
                    @Override
                    public void onSuccess(PaymentApi.OrderResult order) {
                        currentOrderId = order.orderId;
                        if (order.mock) {
                            statusView.setText("本地模拟支付完成，正在确认会员状态…");
                            queryCount = 0;
                            queryOrder();
                            return;
                        }
                        if ("alipay".equals(order.payChannel)) {
                            startAlipay(order.aliPayOrderString);
                        } else {
                            startWeChatPay(order.payParams);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        setPayEnabled(true);
                        statusView.setText(message);
                    }
                });
    }

    private void startWeChatPay(WeChatPayParams payParams) {
        if (!WeChatPayManager.get(this).isWeChatInstalled()) {
            setPayEnabled(true);
            statusView.setText("请先安装微信");
            return;
        }
        if (!WeChatPayManager.get(this).pay(payParams)) {
            setPayEnabled(true);
            statusView.setText("微信支付未能启动，请重试");
        } else {
            statusView.setText("请在微信中完成支付");
        }
    }

    private void startAlipay(String aliPayOrderString) {
        AlipayManager.pay(this, aliPayOrderString, new AlipayManager.PayResultCallback() {
            @Override
            public void onSuccess() {
                statusView.setText("支付结果确认中…");
                queryCount = 0;
                queryOrder();
            }

            @Override
            public void onProcessing() {
                statusView.setText("支付结果确认中…");
                queryCount = 0;
                queryOrder();
            }

            @Override
            public void onCancelled() {
                setPayEnabled(true);
                statusView.setText("已取消支付");
            }

            @Override
            public void onFailed(String message) {
                setPayEnabled(true);
                statusView.setText(message);
            }
        });
    }

    private void queryOrder() {
        if (currentOrderId == null || !userSession.canPay()) {
            setPayEnabled(true);
            return;
        }
        queryCount++;
        paymentApi.queryOrder(userSession.getAccessToken(), currentOrderId,
                new PaymentApi.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String status) {
                        if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                            memberActive = true;
                            statusView.setText("支付成功，会员已开通");
                            showPaidBottomState();
                            loadMembershipStatus();
                        } else if (queryCount < MAX_QUERY_COUNT) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    queryOrder();
                                }
                            }, 1500L);
                        } else {
                            setPayEnabled(true);
                            statusView.setText("订单处理中，请稍后重新进入页面查看");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (queryCount < MAX_QUERY_COUNT) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    queryOrder();
                                }
                            }, 1500L);
                        } else {
                            setPayEnabled(true);
                            statusView.setText("暂时无法确认订单，请稍后查看");
                        }
                    }
                });
    }

    private void setPayEnabled(boolean enabled) {
        payButton.setEnabled(enabled && memberStatusLoaded);
    }

    private void showPaidBottomState() {
        if (selectedProduct != null) {
            bottomPriceView.setText("￥" + selectedProduct.price);
        } else {
            bottomPriceView.setText("￥--");
        }
        bottomHintView.setText("立即续费");
        payButton.setVisibility(View.VISIBLE);
        setPayEnabled(selectedProduct != null);
    }

    private void showPayBottomState() {
        payButton.setVisibility(View.VISIBLE);
        if (selectedProduct != null) {
            bottomPriceView.setText("￥" + selectedProduct.price);
        } else {
            bottomPriceView.setText("￥--");
        }
        bottomHintView.setText("立即开通");
        setPayEnabled(selectedProduct != null);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            refreshMemberHeader();
            if (!memberStatusLoaded) {
                statusView.setText("正在确认会员状态…");
                setPayEnabled(false);
            } else if (memberActive) {
                statusView.setText("会员已开通，可继续续费");
                showPaidBottomState();
            } else {
                statusView.setText("登录成功，请确认套餐后支付");
                setPayEnabled(selectedProduct != null);
            }
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(payResultReceiver);
        super.onDestroy();
    }
}
