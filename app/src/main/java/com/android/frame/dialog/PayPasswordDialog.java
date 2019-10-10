package com.android.frame.dialog;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.frame.R;
import com.android.frame.logic.api.API_Account;
import com.android.frame.view.ViewGT;

import org.json.JSONObject;
import org.rdengine.http.ErrorCode;
import org.rdengine.http.JSONResponse;
import org.rdengine.runtime.RT;
import org.rdengine.util.PhoneUtil;
import org.rdengine.util.UiUtil;
import org.rdengine.view.manager.ViewController;
import org.rdengine.view.manager.ViewParam;
import org.rdengine.widget.LoadingButton;
import org.rdengine.widget.PassWordEditText;
import org.rdengine.widget.ToastHelper;

public class PayPasswordDialog extends BaseDialog implements View.OnClickListener {
    // ----------------R.layout.dialog_pay_password-------------Start
    private ImageView iv_close;
    private PassWordEditText pwet_password;
    private TextView tv_forget_pwd;
    private LoadingButton btn_confirm; // ----------------R.layout.dialog_pay_password-------------End
    private TextView tv_tips;
    public PayPasswordDialog(Context context)
    {
        super(context, R.style.BaseDialog);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_pay_password);

        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();

        lp.width = (int) (PhoneUtil.getScreenWidth(context) - 2 * PhoneUtil.dipToPixel(26, getContext()));
        lp.gravity = Gravity.CENTER;
        onWindowAttributesChanged(lp);

        // 沉浸模式 dialog全屏
        if (UiUtil.isOpenTransparentStatusbar())
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            {
                w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else
            {
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }

        autoLoad_dialog_pay_password();
        iv_close.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
        tv_forget_pwd.setOnClickListener(this);
        pwet_password.setPasswordInputListenenr(inputListenenr);


    }
    public PayPasswordDialog (Context context,PinDialogOnclicklistener listener){

        this(context);
        this.listener = listener;
    }

    /** auto load R.layout.dialog_pay_password */
    private void autoLoad_dialog_pay_password()
    {
        iv_close = (ImageView) findViewById(R.id.iv_close);
        pwet_password = (PassWordEditText) findViewById(R.id.pwet_password);
        tv_forget_pwd = (TextView) findViewById(R.id.tv_forget_pwd);
        btn_confirm = (LoadingButton) findViewById(R.id.btn_confirm);
        tv_tips = findViewById(R.id.tv_tips);
    }

    @Override
    public void onClick(View v) {

            switch (v.getId()){

                case R.id.iv_close:
                {
                    dismiss();
                }
                break;
                case R.id.btn_confirm:
                {
                 //todo 确认密码

                    verifyPincode(pwet_password.getText());

                }
                break;
                case R.id.tv_forget_pwd:
                {
                    //忘记密码

                }
                break;
            }
    }

    PassWordEditText.PasswordInputListenenr inputListenenr = new PassWordEditText.PasswordInputListenenr() {
        @Override
        public void passWordChanged(String changedText) {

            btn_confirm.setEnabled(changedText.matches("\\d{6}"));
        }

        @Override
        public void passWordEnd() {
            String input_pw = pwet_password.getText().toString();
            if (error_count >= MAX_ERROR_COUNT)
            {
                btn_confirm.setEnabled(false);
            } else
            {
                btn_confirm.setEnabled(true);
            }

        }

        @Override
        public void keyEnterBack(String pwd, boolean isComplete) {

        }
    };
    public static final int LOCK_TIME_HOUR = 2;

    /** 当前步骤 最大重试数 */
    int error_count = 0;
    /** 当前步骤 已重试数 */
    public static final int MAX_ERROR_COUNT = 5;
    boolean hasTip = false;
    private void showErrorTip(int retryCount)
    {
        hasTip = true;
        pwet_password.setPwdBgColor(0xffFF537B);
        tv_tips.setText(getContext().getResources().getString(R.string.pincode_error_tip,
                String.valueOf(MAX_ERROR_COUNT - retryCount), String.valueOf(MAX_ERROR_COUNT),
                String.valueOf(LOCK_TIME_HOUR)));
        tv_tips.setVisibility(View.VISIBLE);
    }

    private void hideErrorTip()
    {
        if (hasTip)
        {
            pwet_password.setPwdBgColor(0xffCCCCCC);
            tv_tips.setVisibility(View.INVISIBLE);

            hasTip = false;
        }
    }
    private void verifyPincode(final String fundPwd){
        if(btn_confirm.isLoading())
            return;
        btn_confirm.startLoading();
        API_Account.checkFundPwd(fundPwd, new JSONResponse() {
            @Override
            public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached) {

                btn_confirm.endLoading();
                if(json != null && errCode == ErrorCode.ERROR_OK){
                    dismiss();
                    if (listener != null)
                        listener.onVerifySuccess(PayPasswordDialog.this,fundPwd);
                }else if(errCode > 0){

                    error_count++;

                    if (error_count >= MAX_ERROR_COUNT)
                    {
                        dismiss();
                        if (listener != null)
                        {
                            // todo 锁2小时 、 给提示
                            listener.onVerifyFail(PayPasswordDialog.this);
                        }
                    } else
                    {
                        // 重试
                        pwet_password.setText("");// 清空内容
                        showErrorTip(error_count);// 错误提示
                        pwet_password.openInputMethod();// 打开输入法
                    }
                }else {
                    ToastHelper.showToast(msg);
                }
            }
        });


    }
    private PinDialogOnclicklistener listener;
    public interface PinDialogOnclicklistener
    {

        void onVerifySuccess(PayPasswordDialog dialog, String pincode);

        void onVerifyFail(PayPasswordDialog dialog);

    }
}
