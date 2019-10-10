package org.rdengine.view.manager;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.frame.R;
import com.android.frame.dialog.BaseDialog;
import com.android.frame.dialog.GbTipDialog;
import com.umeng.analytics.MobclickAgent;

import org.rdengine.runtime.RT;

/**
 * 通用activity
 * 
 * @author CCCMAX
 */
public class GenericActivity extends BaseActivity
{
    public static final String BASEVIEW_CLASS_NAME = "baseview_class_name";
    public static final String ANIM_FINISH = "anim_finish";

    private int[] anim_finish;

    public String firstBaseViewName;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        if (getIntent() != null)
        {
            String className = getIntent().getStringExtra(BASEVIEW_CLASS_NAME);
            anim_finish = getIntent().getIntArrayExtra(ANIM_FINISH);
            if (TextUtils.isEmpty(className))
            {
                this.finish();
                return;
            }

            try
            {
                String key = getIntent().getStringExtra(IntentParams.INTENT_PARAMS_KEY);
                ViewParam mViewParam = IntentParams.getInstance().get(key);
                IntentParams.getInstance().remove(key);

                Class clazz = getClassLoader().loadClass(className);
                showView(clazz, mViewParam);

                firstBaseViewName = clazz.getName();
                ActivityRecord.ins().push(this);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void finish()
    {
        if (isFinishing())
            return;
        super.finish();
        ActivityRecord.ins().remove(this);
        try
        {
            if (anim_finish != null)
                overridePendingTransition(anim_finish[0], anim_finish[1]);
        } catch (Exception e)
        {
        }
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        try
        {
            ActivityRecord.ins().remove(this);
        } catch (Exception ex)
        {
        }
    }

    @Override
    public void onBackPressed()
    {

        if (container.backInMask())
            if (this.backView())
            {
                if (mViewManager.getTopView().getClass().isAnnotationPresent(FirstBaseViewNeedBackEvent.class))
                {
                    GbTipDialog td = new GbTipDialog(this);
                    td.setTitle(RT.getString(R.string.back_ask));
                    td.setBaseDialogOnclicklistener(new BaseDialog.BaseDialogOnclicklistener()
                    {
                        @Override
                        public void onOkClick(Dialog dialog)
                        {
                            finish();
                            MobclickAgent.onKillProcess(RT.application);
                            System.exit(0);
                        }

                        @Override
                        public void onCancleClick(Dialog dialog)
                        {

                        }
                    });
                    td.show();

                } else
                {

                    finish();
                }

            }

    }
}
