package org.rdengine.widget.tagcells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;
import org.rdengine.util.PhoneUtil;

import java.util.ArrayList;
import java.util.List;

public class TagCells extends LinearLayout implements OnClickListener
{

    private LayoutInflater mLayoutInflater;
    private int parentWidth = 0;// 控件宽度

    int divide_horizontal = 6;// tag水平间距
    int divide_vertical = 6;// tag垂直间距
    int cell_layout_id = 0;// tag的layoutID
    boolean isCenter = false;// 每行tag是否居中
    boolean isSingleline = false;// 是否只显示一行tag
    int maxLines = Integer.MAX_VALUE;// 最大行数

    boolean SingleSelectMode = false;

    private int tagViewMaxWidth = -1;// 每个textview的最大宽度 像素

    GradientDrawable def_tag_bg = null;

    ArrayList<TagObj> taglist = null;

    TagOnClickListener tagonclicklistener = null;

    int Delay = 10;

    public TagCells(Context context)
    {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public TagCells(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.setOrientation(LinearLayout.VERTICAL);
        mLayoutInflater = LayoutInflater.from(getContext());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagCells);
        initParams(a);
    }

    @SuppressLint("NewApi")
    public TagCells(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.setOrientation(LinearLayout.VERTICAL);
        mLayoutInflater = LayoutInflater.from(getContext());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TagCells, defStyle, 0);
        initParams(a);
    }

    /**
     * 初始化layout布局中的参数
     * 
     * @param ta
     */
    protected void initParams(TypedArray ta)
    {
        try
        {
            divide_horizontal = ta.getDimensionPixelSize(R.styleable.TagCells_divide_horizontal, divide_horizontal);
            divide_vertical = ta.getDimensionPixelSize(R.styleable.TagCells_divide_vertical, divide_vertical);
            cell_layout_id = ta.getResourceId(R.styleable.TagCells_cell_layout_id, cell_layout_id);
            isCenter = ta.getBoolean(R.styleable.TagCells_cell_is_center, isCenter);
            isSingleline = ta.getBoolean(R.styleable.TagCells_singleline, isSingleline);
            maxLines = ta.getInt(R.styleable.TagCells_maxlines, Integer.MAX_VALUE);
            int editmode_size = ta.getInt(R.styleable.TagCells_editmode_size, 11);

            if (isInEditMode())
            {
                parentWidth = 480;
                ArrayList<String> textdata = new ArrayList<String>();
                for (int i = 0; i < editmode_size; i++)
                {
                    textdata.add(i % 2 == 0 ? "测试" : "InEditMode");
                }
                setData(textdata);
            }
        } catch (Exception e)
        {
        }
    }

    public void setTagViewMaxWidth(int maxwidth)
    {
        tagViewMaxWidth = maxwidth;
    }

    public void setSingleSelectMode(boolean isSingleSelect)
    {
        SingleSelectMode = isSingleSelect;
    }

    public boolean isSingleSelectMode()
    {
        return SingleSelectMode;
    }

    public void setMaxLines(int maxLines)
    {
        this.maxLines = maxLines;
    }

    public int getMaxLines()
    {
        return this.maxLines;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if (changed)
        {
            // 获取控件实际宽度
            int w = r - l - this.getPaddingLeft() - this.getPaddingRight();

            if (w != parentWidth)
            {
                parentWidth = w;
                if (taglist != null)
                    this.postDelayed(new Runnable()
                    {

                        public void run()
                        {
                            toFillLayout();
                        }
                    }, Delay);
            }
        }
    }

    /**
     * 设置每行都把标签居中
     * 
     * @param isCenter
     *            true 每行标签都居中显示
     */
    public void setCenter(boolean isCenter)
    {
        this.isCenter = isCenter;
        if (taglist != null)
        {
            fillLayout(taglist);// 刷新一下
        }
    }

    public void clearTags()
    {
        taglist = null;
        toFillLayout();
    }

    /**
     * 设置数据 类型需要中转
     * 
     * @param data
     */
    public void setData(List<String> data)
    {
        taglist = new ArrayList<TagObj>();
        if (data != null)
        {
            for (int i = 0; i < data.size(); i++)
            {
                Object obj = data.get(i);
                taglist.add(new TagObj(0, obj.toString(), false));
            }
        }
        toFillLayout();
    }

    /**
     * 设置数据
     * 
     * @param data
     */
    public void setData(ArrayList<TagObj> data)
    {
        taglist = data;
        toFillLayout();
    }

    public ArrayList<TagObj> getData()
    {
        return taglist;
    }

    public boolean hasData()
    {
        return taglist != null && taglist.size() > 0;
    }

    boolean first = true;

    private void toFillLayout()
    {
        long a = System.currentTimeMillis();
        if (first)
        {
            first = false;
            if (parentWidth > 0)
            {
                fillLayout(taglist);
            }
        } else
        {
            if (parentWidth > 0)
                fillLayout(taglist);
        }
        long b = System.currentTimeMillis();
        if (!isInEditMode() && RT.DEBUG)
            DLOG.e("cccmax", "tagcells filllayout time = " + (b - a));
    }

    private LinearLayout createLayout()
    {
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        if (isCenter)
        {
            LayoutParams pp = new LayoutParams(-2, -2);
            pp.gravity = Gravity.CENTER_HORIZONTAL;
            ll.setLayoutParams(pp);
        }
        return ll;
    }

    /**
     * 创建标签view 如果没有设置layoutID的话 就用默认的TextView
     * 
     * @return
     */
    private TextView createTagview(int index)
    {
        TextView tagview = null;
        try
        {
            if (cell_layout_id != 0)
            {
                tagview = (TextView) mLayoutInflater.inflate(cell_layout_id, null);
            }
        } catch (Exception e)
        {
        }
        if (tagview == null && mTagviewBuilder != null)
        {
            tagview = mTagviewBuilder.buildTagview(index);
        }

        if (tagview == null)
        {
            // 生成一个shape
            if (def_tag_bg == null)
            {
                def_tag_bg = new GradientDrawable();// 创建drawable
                def_tag_bg.setColor(0xff2eb089);// 背景色
                def_tag_bg.setCornerRadius(100);// 圆角半径大于控件最小边的话 就会是半圆的效果 而不是小圆角了
            }
            // 生成一个TextView
            tagview = new TextView(getContext());
            tagview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            tagview.setTextColor(0xffffffff);
            tagview.setGravity(Gravity.CENTER);
            tagview.setBackgroundDrawable(def_tag_bg);
            tagview.setPadding(20, 0, 20, 0);
            tagview.setMinHeight(40);
        }
        return tagview;
    }

    /**
     * 开始布局 添加数据
     * 
     * @param tags
     */
    protected void fillLayout(ArrayList<TagObj> tags)
    {
        Paint p = new Paint();
        removeAllViews();

        if (tags == null)
            return;

        int width = 0;
        int width_no_divide = 0;
        int sumWidth = 0;
        int sumHeight = getPaddingTop() + getPaddingBottom();
        View lasttag = null;
        LinearLayout ll_line = createLayout();

        if (mTagviewBuilder != null && mTagviewBuilder.getHeight() > 0)
        {
            addView(ll_line, -1, mTagviewBuilder.getHeight());// 控件中添加新的一行
            sumHeight += mTagviewBuilder.getHeight();
        } else
        {
            addView(ll_line, -1 - 2);// 控件中添加新的一行
        }

        for (int i = 0; i < tags.size(); i++)
        {
            TagObj t = tags.get(i);
            TextView tagView = null;
            int drawableWidth = 0;

            // 构造 symbol 视图
            tagView = createTagview(i);
            tagView.setOnClickListener(this);
            // 设置数据对象
            tagView.setTag(t);
            // 计算tag宽度
            p.setTextSize(tagView.getTextSize());
            String s = t.text;
            int drawablePadding = tagView.getCompoundDrawablePadding();

            if (mTagviewBuilder != null)
            {
                // 用构造器设置内容，并且计算宽度并返回
                width_no_divide = mTagviewBuilder.setDataAndCalcWidth(tagView, t, i);
            } else
            {
                // tag宽度 不算间隔
                width_no_divide = (int) (p.measureText(s) + tagView.getPaddingLeft() + tagView.getPaddingRight())
                        + drawablePadding + drawableWidth;
                // 设置内容
                tagView.setText(s);

                if (PhoneUtil.hasJellyBean())
                {
                    // 计算尺寸 < 最小宽度 ，按最小宽度计算
                    int minwidth = tagView.getMinWidth();
                    if (minwidth != -1 && width_no_divide < minwidth)
                    {
                        width_no_divide = minwidth;
                    }
                }
            }

            // 控制最大宽度
            if (tagViewMaxWidth > -1 && width_no_divide > tagViewMaxWidth)
            {
                width_no_divide = tagViewMaxWidth;
                tagView.setMaxWidth(tagViewMaxWidth);
            }

            // tag宽度 算间隔
            width = width_no_divide + divide_horizontal;

            // if (SingleSelectMode)
            {
                tagView.setSelected(t.select);
                t.setView(tagView);
            }

            boolean outsize = sumWidth + width > parentWidth;// tag宽度超出了当前行的范围
            boolean outsize_no_divide = sumWidth + width_no_divide > parentWidth;// tag不算间隔的宽度 超出当前行范围

            if (outsize)
            {
                if (!outsize_no_divide)
                {
                    LayoutParams tag_lp = new LayoutParams(-2, -2);
                    if (mTagviewBuilder != null && mTagviewBuilder.getHeight() > 0)
                    {
                        tag_lp.height = mTagviewBuilder.getHeight();
                    }
                    ll_line.addView(tagView, tag_lp);// 添加tag //没有间隔

                    try
                    {
                        // sumWidth 当前tagview在x轴起始位置
                        // width_no_divide 当前tagview的宽度
                        if (mTagviewBuilder != null)
                        {
                            mTagviewBuilder.onCaclPosition(tagView, i, parentWidth, sumWidth, width_no_divide);
                        }
                    } catch (Exception ex)
                    {
                    }
                    sumWidth += width_no_divide;
                    lasttag = tagView;
                    continue;
                }

                if (isSingleline)// 如果设置只有一行的话 就不再换行了 也不生成新的tag
                    break;

                if (getChildCount() >= maxLines)// 如果超过最大行数 也结束添加
                    break;

                ll_line = createLayout();
                LayoutParams line_lp = new LayoutParams(-1, -2);
                line_lp.setMargins(0, divide_vertical, 0, 0);// 行间距 垂直间距
                if (mTagviewBuilder != null && mTagviewBuilder.getHeight() > 0)
                {
                    line_lp.height = mTagviewBuilder.getHeight();
                    sumHeight += mTagviewBuilder.getHeight() + divide_vertical;
                } else
                {
                }
                addView(ll_line, line_lp);// 控件添加新的一行
                if (isCenter)
                {
                    if (line_lp != null)
                    {
                        line_lp.width = LayoutParams.WRAP_CONTENT;
                        line_lp.gravity = Gravity.CENTER_HORIZONTAL;
                    }
                }
                sumWidth = 0;
                crealLastTagMargins(lasttag);
            }

            LayoutParams tag_lp = new LayoutParams(-2, -2);
            tag_lp.setMargins(0, 0, divide_horizontal, 0);// 水平间隔
            if (mTagviewBuilder != null && mTagviewBuilder.getHeight() > 0)
            {
                tag_lp.height = mTagviewBuilder.getHeight();
            } else
            {
            }
            ll_line.addView(tagView, tag_lp);// 添加tag
            try
            {
                // sumWidth 当前tagview在x轴起始位置
                // width_no_divide 当前tagview的宽度
                if (mTagviewBuilder != null)
                {
                    mTagviewBuilder.onCaclPosition(tagView, i, parentWidth, sumWidth, width_no_divide);
                }
            } catch (Exception ex)
            {
            }
            sumWidth += width;
            lasttag = tagView;

        }
        crealLastTagMargins(lasttag);

        this.requestLayout();

        if (mOnTagLayoutListener != null)
        {
            ViewGroup.LayoutParams lp_tagcell = getLayoutParams();
            lp_tagcell.height = sumHeight;
            this.setLayoutParams(lp_tagcell);

            mOnTagLayoutListener.onLayoutFinish(this, getChildCount(), sumWidth, sumHeight);
        }
    }

    private void crealLastTagMargins(View lasttag)
    {
        if (lasttag != null)// 上一行的最后一个tag 取消间隔
        {
            LayoutParams last_tag_lp = (LayoutParams) lasttag.getLayoutParams();
            if (last_tag_lp != null)
            {
                last_tag_lp.setMargins(0, 0, 0, 0);
                lasttag.setLayoutParams(last_tag_lp);
            }
        }
    }

    public void setTagOnClickListener(TagOnClickListener tagonclicklistener)
    {
        this.tagonclicklistener = tagonclicklistener;
    }

    @Override
    public void onClick(View v)
    {
        TagObj obj = null;
        try
        {
            obj = (TagObj) v.getTag();
        } catch (Exception e)
        {
        }

        try
        {
            obj.select = !obj.select;
            v.setSelected(obj.select);
            if (SingleSelectMode)
            {
                for (TagObj to : taglist)
                {
                    if (!to.equals(obj))
                    {
                        to.select = false;
                        if (to.getView() != null)
                        {
                            to.getView().setSelected(false);
                        }
                    }
                }
            }
            if (mOnItemSelectListener != null)
            {
                mOnItemSelectListener.onItemSelected(this, v, taglist.indexOf(obj), obj);
            }
        } catch (Exception e)
        {
        }

        if (tagonclicklistener == null || v == null)
            return;

        if (obj != null)
        {
            tagonclicklistener.tagOnClick(obj, (TextView) v, taglist.indexOf(obj));
        }
    }

    public static ArrayList<TagObj> getTestData(int test_tag_num)
    {
        String s = "预览数据";
        ArrayList<TagObj> temp = new ArrayList<TagObj>();
        for (int i = 0; i < test_tag_num; i++)
        {
            TagObj to = new TagObj(0, s.substring(0, 2 + i % 3), false);
            temp.add(to);
        }
        return temp;
    }

    public ArrayList<TagObj> getSelectedItems()
    {
        ArrayList<TagObj> ret = new ArrayList<TagObj>();
        if (taglist != null)
        {
            for (TagObj to : taglist)
            {
                if (to.select)
                {
                    ret.add(to);
                }
            }
        }
        return ret;
    }

    public void cleanSelecteds()
    {
        if (taglist == null)
            return;
        for (TagObj to : taglist)
        {
            to.select = false;
            if (to.getView() != null)
            {
                to.getView().setSelected(false);
            }
        }
    }

    private OnItemSelectListener mOnItemSelectListener;

    public void setOnItemSelectListener(OnItemSelectListener listener)
    {
        mOnItemSelectListener = listener;
    }

    public static interface OnItemSelectListener
    {

        public void onItemSelected(TagCells tc, View item, int index, TagObj tagobj);
    }

    TagviewBuilder mTagviewBuilder;

    public void setTagviewBuilder(TagviewBuilder tagviewBuilder)
    {
        mTagviewBuilder = tagviewBuilder;
    }

    public interface TagviewBuilder
    {

        TextView buildTagview(int index);

        int getHeight();

        /**
         * 填充数据，并且要自行计算宽度
         * 
         * @param item
         * @param index
         * @return 如果再次修改内容或添加图片 需要返回一个修改后的宽度差值
         */
        int setDataAndCalcWidth(TextView item, TagObj t, int index);

        /**
         * 计算每个tag所在位置
         * 
         * @param tagview
         * @param index
         * @param parentWidth
         * @param tag_left
         * @param tag_width
         */
        public void onCaclPosition(TextView tagview, int index, int parentWidth, int tag_left, int tag_width);
    }

    OnTagLayoutListener mOnTagLayoutListener;

    public void setOnTagLayoutListener(OnTagLayoutListener onTagLayoutListener)
    {
        mOnTagLayoutListener = onTagLayoutListener;
    }

    public static interface OnTagLayoutListener
    {
        public void onLayoutFinish(TagCells tagcell, int lineCount, int lastLineWidth, int height);
    }
}
