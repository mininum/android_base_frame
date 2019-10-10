package org.rdengine.view.manager;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.runtime.RT;
import org.rdengine.util.ClickUtil;
import org.rdengine.util.StringUtil;

import java.util.Random;

public class ContentStateLayout extends FrameLayout implements OnClickListener
{
    public enum ContentStateType
    {
        // 加载中
        Loading,
        // 网络问题
        NetErr,
        // 加载错误
        LoadErr,
        // 无内容
        Empty,
        // 没登录
        UnLogin,
    }

    public ContentStateLayout(Context context)
    {
        super(context);
        init();
    }

    public void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_content_state, this);
        autoLoad_layout_content_state();
        statelayout.setOnClickListener(this);
        btn_tryagain.setOnClickListener(this);
        btn_tryagain.setVisibility(View.GONE);
    }

    // ----------------R.layout.layout_content_state-------------Start
    public FrameLayout statelayout;
    public LinearLayout container;
    private ImageView iv_icon;
    private org.rdengine.widget.ProgressBarCircular progressbar;
    private TextView tv_tip;
    private TextView btn_tryagain;

    public void autoLoad_layout_content_state()
    {
        statelayout = (FrameLayout) findViewById(R.id.statelayout);
        container = (LinearLayout) findViewById(R.id.container);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        progressbar = (org.rdengine.widget.ProgressBarCircular) findViewById(R.id.progressbar);
        tv_tip = (TextView) findViewById(R.id.tv_tip);
        btn_tryagain = (TextView) findViewById(R.id.btn_tryagain);
    }

    // ----------------R.layout.layout_content_state-------------End

    ContentStateType laststate;

    public ContentStateType getCurrentState()
    {
        return laststate;
    }

    public void showState(ContentStateType type, int imgID, String tip)
    {
        laststate = type;
        if (type == ContentStateType.Loading)
        {
            // 加载中
            // btn_tryagain.setVisibility(View.GONE);
            iv_icon.setVisibility(View.GONE);
            progressbar.setVisibility(View.VISIBLE);
            if (StringUtil.isEmpty(tip))
            {
                // tv_tip.setText(getRandomStringFromArray(R.array.content_loading_array));
                tv_tip.setText(RT.getString(R.string.content_loading));
            } else
            {
                tv_tip.setText(tip);
            }
            tv_tip.setVisibility(View.VISIBLE);
        } else if (type == ContentStateType.NetErr)
        {
            // 网络错误
            iv_icon.setImageResource(imgID != 0 ? imgID : R.drawable.icon_content_neterror);
            iv_icon.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
            // btn_tryagain.setVisibility(View.VISIBLE);
            // btn_tryagain.setText("刷新试一试");
            if (StringUtil.isEmpty(tip))
            {
                tv_tip.setText(RT.getString(R.string.error_network_content));
            } else
            {
                tv_tip.setText(tip);
            }
            tv_tip.setVisibility(View.VISIBLE);
        } else if (type == ContentStateType.LoadErr)
        {
            // 加载错误
            iv_icon.setImageResource(imgID != 0 ? imgID : R.drawable.icon_content_neterror);
            iv_icon.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
            // btn_tryagain.setVisibility(View.VISIBLE);
            // btn_tryagain.setText("刷新试一试");
            if (StringUtil.isEmpty(tip))
            {
                tv_tip.setText(RT.getString(R.string.error_load));
            } else
            {
                tv_tip.setText(tip);
            }
            tv_tip.setVisibility(View.VISIBLE);
        } else if (type == ContentStateType.Empty)
        {
            // 无内容
            iv_icon.setImageResource(imgID != 0 ? imgID : R.drawable.icon_content_empty);
            iv_icon.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
            // btn_tryagain.setVisibility(View.GONE);
            if (StringUtil.isEmpty(tip))
            {
                tv_tip.setText(RT.getString(R.string.content_empty));
            } else
            {
                tv_tip.setText(tip);
            }
            tv_tip.setVisibility(View.VISIBLE);
        } else if (type == ContentStateType.UnLogin)
        {
            // 没登录
            iv_icon.setImageResource(imgID != 0 ? imgID : R.drawable.icon_content_neterror);
            iv_icon.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
            // btn_tryagain.setVisibility(View.VISIBLE);
            // btn_tryagain.setText("登录");
            if (StringUtil.isEmpty(tip))
            {
                tv_tip.setText(RT.getString(R.string.need_login));
            } else
            {
                tv_tip.setText(tip);
            }
            tv_tip.setVisibility(View.VISIBLE);
        }
        if (hideAnimator != null)
        {
            hideAnimator.cancel();
        }
        this.setAlpha(1);
        container.setVisibility(View.VISIBLE);
        this.setVisibility(View.VISIBLE);
    }

    public void hide()
    {
        this.setVisibility(View.GONE);
    }

    public void hideAnimator()
    {
        // this.setVisibility(View.GONE);
        startHideAnimator();
    }

    ValueAnimator hideAnimator;

    private void startHideAnimator()
    {
        if (hideAnimator != null)
        {
            if (hideAnimator.isStarted() || hideAnimator.isRunning())
            {
                return;
            } else
            {
                hideAnimator.cancel();
            }
        } else
        {
            container.setVisibility(View.GONE);
            float alpha = this.getAlpha();
            hideAnimator = ValueAnimator.ofFloat(alpha, 0);
            hideAnimator.setDuration((int) (500 * alpha));
            hideAnimator.addListener(new AnimatorListener()
            {
                public void onAnimationStart(Animator animation)
                {}

                public void onAnimationRepeat(Animator animation)
                {

                }

                public void onAnimationEnd(Animator animation)
                {
                    ContentStateLayout.this.setVisibility(View.GONE);
                }

                public void onAnimationCancel(Animator animation)
                {

                }
            });
            hideAnimator.addUpdateListener(new AnimatorUpdateListener()
            {
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    float curValue = (Float) animation.getAnimatedValue();
                    ContentStateLayout.this.setAlpha(curValue);
                }
            });
        }
        hideAnimator.start();
    }

    public static String getRandomStringFromArray(int stringArrayID)
    {
        try
        {
            String[] array = RT.application.getResources().getStringArray(stringArrayID);
            int index = new Random().nextInt(array.length);
            return array[index];
        } catch (Exception e)
        {
        }
        return null;
    }

    @Override
    public void onClick(View v)
    {
        if (ClickUtil.isFastDoubleClick())
            return;
        switch (v.getId())
        {
        case R.id.statelayout :
            // break;
        case R.id.btn_tryagain :
        {
            if (mContentStateClickListener != null)
            {
                mContentStateClickListener.onStateLayoutClick(laststate, v);
            }
        }
            break;
        }

    }

    ContentStateClickListener mContentStateClickListener;

    public void setContentStateClickListener(ContentStateClickListener mContentStateClickListener)
    {
        this.mContentStateClickListener = mContentStateClickListener;
    }

    public interface ContentStateClickListener
    {
        void onStateLayoutClick(ContentStateType type, View v);
    }

    /** 设置背景颜色 */
    public void setBgColor(int color)
    {
        statelayout.setBackgroundColor(color);
    }

    /** 设置文字颜色 */
    public void setTextColor(int color)
    {
        tv_tip.setTextColor(color);
    }
}
