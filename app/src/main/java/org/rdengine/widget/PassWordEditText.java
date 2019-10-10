package org.rdengine.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.android.frame.R;


/**
 * 密码输入框<br>
 * Created by FirFrog on 2019/04/24.
 */
public class PassWordEditText extends RelativeLayout
{

    // 密码长度
    private int pwdLength;
    private Paint bPaint, cPaint, tPaint;

    // 下滑线间距
    private int linePadding = 20;

    // 一个密码的宽度
    private int pwdWidth;

    // 边框颜色
    private int bgColor, textColor;

    // 输入的密码
    private String pwdText = "";

    // 边界宽度和画笔宽度
    private int stWidth = 5;

    // 圆点半径
    private int radius = 15;

    // 密码字体大小
    private int pwdTextSize;

    // 明文还是密文展示
    private boolean isSecret = true;

    private EditText editText;

    // 输入框样式
    public enum InStyle
    {
        // 下滑线
        BOTTOMLINE(1),

        // 方框
        RECTF(0);

        private int Instyle;

        InStyle(int instyle)
        {
            this.Instyle = instyle;

        }

        public int getInstyle()
        {

            return this.Instyle;
        }

        static InStyle forInStyle(int instyle)
        {
            for (InStyle i : values())
            {

                if (instyle == i.Instyle)
                {

                    return i;
                }
            }

            throw new IllegalArgumentException();

        }

    }

    // 设置输入框样式
    private InStyle mInStyle = InStyle.RECTF;

    private PasswordInputListenenr passwordInputListenenr;

    public PassWordEditText(Context context)
    {
        this(context, null);

    }

    public PassWordEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // 自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PassWordEditText);

        pwdLength = typedArray.getInteger(R.styleable.PassWordEditText_pwet_pwdLength, 6);
        linePadding = typedArray.getDimensionPixelSize(R.styleable.PassWordEditText_pwet_bottomLinePadding, 20);
        radius = typedArray.getDimensionPixelSize(R.styleable.PassWordEditText_pwet_circleRadius, 15);
        pwdTextSize = typedArray.getDimensionPixelSize(R.styleable.PassWordEditText_pwet_pwdTextSize, 10);
        stWidth = typedArray.getDimensionPixelSize(R.styleable.PassWordEditText_pwet_stWidth, 5);
        bgColor = typedArray.getColor(R.styleable.PassWordEditText_pwet_bgColor, Color.parseColor("#000000"));
        textColor = typedArray.getColor(R.styleable.PassWordEditText_pwet_textColor, Color.parseColor("#000000"));
        mInStyle = InStyle.forInStyle(
                typedArray.getInt(R.styleable.PassWordEditText_pwet_pwdStyle, InStyle.BOTTOMLINE.getInstyle()));
        pwdWidth = typedArray.getDimensionPixelSize(R.styleable.PassWordEditText_pwet_pwdWidth, 0);
        typedArray.recycle();
        setBackgroundColor(0x00000000);
        initPaint();
        setWillNotDraw(false);
        initView(context);

    }

    private void initView(Context context)
    {

        // 放一个EditText在最底，使用他的吊起系统输入法的方法
        editText = new EditText(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(layoutParams);
        editText.setCursorVisible(false);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextColor(0x00000000);
        editText.setMaxLines(1);
        editText.setFilters(new InputFilter[]
        { new InputFilter.LengthFilter(pwdLength) });
        addView(editText);

        // 在EditText覆盖一个View拦截它的Touch事件
        View view = new View(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(layoutParams));
        view.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    // 打开系统输入法
                    openInputMethod();
                }
                return true;
            }
        });
        addView(view);

        setTextChange();
        editText.setOnKeyListener(new MyOnKeyListener());
    }

    public void openInputMethod()
    {
        try
        {
            editText.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(editText, 0);
        } catch (Exception e)
        {
        }
    }

    private void initPaint()
    {

        // 初始化边框的画笔

        bPaint = new Paint();
        bPaint.setColor(bgColor);
        bPaint.setAntiAlias(true);
        bPaint.setDither(true);

        // 圆点画笔
        cPaint = new Paint();
        cPaint.setColor(textColor);
        cPaint.setAntiAlias(true);
        cPaint.setDither(true);

        // 文本画笔
        tPaint = new Paint();
        tPaint.setColor(textColor);
        tPaint.setAntiAlias(true);
        tPaint.setDither(true);
        tPaint.setStrokeWidth(10);
        tPaint.setTextSize(pwdTextSize);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 动态改变画布大小
        if (pwdWidth > 0)
        {

            widthMeasureSpec = (linePadding) * Math.max((pwdLength - 1), 0) + getPaddingLeft() + getPaddingRight()
                    + pwdWidth * pwdLength + stWidth * 2;

        } else
        {
            pwdWidth = (getMeasuredWidth() - (linePadding) * Math.max((pwdLength - 1), 0) - getPaddingRight()
                    - getPaddingLeft()) / pwdLength;
        }
        setMeasuredDimension(widthMeasureSpec, pwdWidth + stWidth * 2);

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // super.onDraw(canvas);

        if (mInStyle == InStyle.BOTTOMLINE)
        {
            drawBottomLine(canvas);

        } else if (mInStyle == InStyle.RECTF)
        {

            drawRect(canvas);
        }

        if (isSecret)
        {
            drawcircle(canvas);
        } else
        {

            drawText(canvas);
        }

    }

    // 绘制密码框
    private void drawRect(Canvas canvas)
    {

        for (int i = 0; i < pwdLength; i++)
        {

            int startX = getPaddingLeft() + (pwdWidth) * i + stWidth + linePadding * i;
            int startY = stWidth ;
            int endX = startX + pwdWidth;
            int enY = pwdWidth + stWidth;
            RectF rectF = new RectF(startX, startY, endX, enY);

            bPaint.setStrokeWidth(stWidth);
            bPaint.setStyle(Paint.Style.STROKE);

            canvas.drawRect(rectF, bPaint);

        }

    }

    // 绘制圆点
    private void drawcircle(Canvas canvas)
    {

        cPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // 找圆心坐标x坐标

        for (int i = 0; i < pwdText.length(); i++)
        {

            int cx = getPaddingLeft() + pwdWidth * i + linePadding * i + pwdWidth / 2;

            int cy = (pwdWidth / 2);

            canvas.drawCircle(cx, cy, radius, cPaint);

        }
    }

    // 绘制明文密码
    private void drawText(Canvas canvas)
    {

        // 文字在输入框里居中
        Paint.FontMetrics fontMetrics = tPaint.getFontMetrics();
        tPaint.setTextAlign(Paint.Align.CENTER);
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float textY = pwdWidth / 2 + distance;
        float textX;
        for (int i = 0; i < pwdText.length(); i++)
        {
            textX = getPaddingLeft() + linePadding * i + pwdWidth * i + pwdWidth / 2;
            canvas.drawText(pwdText.charAt(i) + "", textX, textY, tPaint);

        }

    }

    // 绘制下滑线
    private void drawBottomLine(Canvas canvas)
    {

        bPaint.setStrokeWidth(stWidth);

        for (int i = 0; i < pwdLength; i++)
        {

            canvas.drawLine(getPaddingLeft() + (pwdWidth + linePadding) * i, pwdWidth,
                    getPaddingLeft() + (pwdWidth + linePadding) * i + pwdWidth, pwdWidth, bPaint);
        }

    }

    // 输入监听
    private void setTextChange()
    {

        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {

                String password = s.toString();
                pwdText = password;

                postInvalidate();

                if (password.length() >= pwdLength)
                {
                    if (passwordInputListenenr != null)
                    {
                        passwordInputListenenr.passWordEnd();
                    }
                    return;
                }
                if (passwordInputListenenr != null)
                {

                    passwordInputListenenr.passWordChanged(pwdText);

                }

            }
        });
    }

    // 输入监听接口
    public interface PasswordInputListenenr
    {

        // 密码变化回掉
        void passWordChanged(String changedText);

        // 输入完成回掉
        void passWordEnd();

        // 确认键点击回掉
        void keyEnterBack(String pwd, boolean isComplete);

    }

    public void setPasswordInputListenenr(PasswordInputListenenr passwordInputListenenr)
    {

        this.passwordInputListenenr = passwordInputListenenr;

    }

    // 监听按键事件
    class MyOnKeyListener implements OnKeyListener
    {
        @Override
        public boolean onKey(View view, int keycode, KeyEvent keyEvent)
        {

            int action = keyEvent.getAction();

            if (action == KeyEvent.ACTION_DOWN)
            {

                if (keycode == KeyEvent.KEYCODE_ENTER)
                {

                    if (passwordInputListenenr != null)
                    {

                        passwordInputListenenr.keyEnterBack(pwdText, false);
                    }
                }
            }
            return false;
        }
    }

    // 明文密文
    public void setIsSecret(boolean isSecret)
    {

        this.isSecret = isSecret;
        postInvalidate();

    }

    // 设置边框样式
    public void setmInStyle(InStyle inStyle)
    {

        this.mInStyle = inStyle;
        postInvalidate();

    }

    // 设置边框颜色
    public void setPwdBgColor(int bgColor)
    {

        this.bgColor = bgColor;
        postInvalidate();
    }

    // 设置密码文字颜色
    public void setPwdTextColor(int pwdTextColor)
    {

        this.textColor = pwdTextColor;
        postInvalidate();
    }

    // 设置文本
    public void setText(String password)
    {

        this.pwdText = password;
        editText.setText(password);
        postInvalidate();
    }

    // 获取文本
    public String getText()
    {
        return pwdText;
    }

    private class ActionModeCallbackInterceptor implements ActionMode.Callback
    {
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode)
        {}
    }

}
