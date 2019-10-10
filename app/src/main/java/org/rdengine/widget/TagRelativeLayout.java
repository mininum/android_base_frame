package org.rdengine.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.rdengine.widget.taglayout.ITagLayoutProperty;
import org.rdengine.widget.taglayout.NoCalculateLayoutProperty;
import org.rdengine.widget.taglayout.TagExpressionLayoutProperty;
import org.rdengine.widget.taglayout.TagLayoutContext;

import java.util.ArrayList;
import java.util.List;

/**
 * android自带的layout太不好用了,为了写一个看起来布局简单的界面,各种试.简单估算下来,做一个app,70%-80%时间花在布局和调界面上,浪费生命啊.鉴于此,自己写这个相对定位. <br/>
 * 主要的功能如下:
 * <ul>
 * <li>宽高支持百分比
 * <li>位置暂时支持left和top。 支持简单的计算式，包括百分比和绝对值
 * </ul>
 * 因为android不支持原生组件的自定义属性，这些布局信息就只能屈就，放在android:tag里面，以字符串形式表示。格式如下：
 * <ul>
 * <li>属性和值作为一个属性对，用冒号分开。比如: w:20,w:20w%p,w:40w%id_img。属性w是width的缩写，代表宽度。其他属性所写包括：h（高度）, l（left）, t(top).属性值可以是数字，不包括单位，默认是dp。 也可以是百分比，%前面的字母(w、h、l、t)代表相对视图的属性，数字就是百分比。%后面代表的是相对视图的id。两种特殊情况是：如果没有写就是相对于自己，p代表父视图。那么w:20w%p，就是表示，此视图的宽度是父亲宽度的20%，l:40l%id_img，表示此视图的了left是R.id.id_img视图left的40%. <br/>
 * 此外，值支持简单的计算式，支持加减运算。比如:l:100l%id_img + 50w%id_img - 50w%。表示该视图的left等于R.id.id_img视图的left 加上宽度一半减去自己宽度的一半。简单说就是此视图和R.id.id_img的视图水平居中。 <br/>
 * w和h的值不应该有位置信息,比如,w:20l%id_img,就是不合法的,因为在计算宽高的时候,还没有布局,不知道为位置信息.l和t的值可以有宽高和位置信息. <br/>
 * <li>多个属性对以分号分开。
 * </ul>
 * 拿几个android自带的RelativeLayout中的布局属性，重新表示就是：
 * <ul>
 * <li>android:layout_above="@id/id_img"就是:android:tag="t:100t%id_img - 100h%"
 * <li>android:layout_toRightOf="@id/id_img"就是:android:tag="l:100l%id_img + 100w%id_img"
 * <li>android:layout_alignTop="@id/id_img"就是:android:tag="t:100t%id_img"
 * <li>android:layout_alignParentRight="true"就是:android:tag="l:100w%p - 100w%"
 * <li>android:layout_centerHorizontal="true"就是:android:tag="l:50w%p - 50w%"
 * <li>android:layout_centerInParent="true"就是:android:tag="l:50w%p-50w%;t:50h%p-50h%"
 * </ul>
 * 乍看下来，这样更复杂了，但是更灵活，RelativeLayout不能干的，这里可以干，比如：
 * <ol>
 * <li>和相对视图的水平、垂直居中</li>
 * <li>宽度相对视图的百分比，再也不需要LinerLayout不靠谱的layout_weight了</li>
 * </ol>
 * 关键是，设置可以很灵活，而不需要为了布局，构造复杂的视图结构，浪费大量的时间。
 * 
 * @author simplevita
 */
public class TagRelativeLayout extends ViewGroup
{

    private TagLayoutContext layoutContext;

    private static final String DEBUG_TAG = "tag_layout";

    private boolean layoutPropertyChanged;

    public TagRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    protected void onMeasure(int widthSpec, int heightSpec)
    {
        int count = this.getChildCount();
        // 重新建立节点关系
        if (layoutPropertyChanged || layoutContext == null)
        {
            layoutContext = new TagLayoutContext(this);
            for (int i = 0; i < count; i++)
            {
                View child = this.getChildAt(i);
                layoutContext.getTagLayoutParams(child);
            }
            layoutContext.linkSplit();
        } // 如果已经存在,则invalidate
        else
        {
            layoutContext.linkSplit();
            layoutContext.invalidate();
        }
        layoutContext.prepareMeasure(widthSpec, heightSpec);
        List<ITagLayoutProperty> layoutNodes = new ArrayList<ITagLayoutProperty>();
        // 已经去掉需要parent的节点
        layoutNodes.addAll(layoutContext.getLinkList());

        // 2,链表计算
        calculateProperties(layoutContext, layoutNodes);

        // 3:计算本身的宽高
        TagLayoutParams parentTagLayout = layoutContext.getTagLayoutParams(this);

        // 4: 计算需要parent的节点
        layoutNodes.clear();
        List<ITagLayoutProperty> dependProperties = layoutContext.getDependParentList();
        if (dependProperties != null)
        {
            layoutNodes.addAll(dependProperties);
        }
        calculateProperties(layoutContext, layoutNodes);
        setMeasuredDimension(parentTagLayout.getWidth().getValue(), parentTagLayout.getHeight().getValue());
    }

    @SuppressLint("DrawAllocation")
    private void calculateProperties(TagLayoutContext layoutContext, List<ITagLayoutProperty> layoutNodes)
    {
        while (layoutNodes.size() > 0)
        {
            int count = layoutNodes.size();
            while (--count >= 0)
            {
                ITagLayoutProperty node = layoutNodes.get(count);
                List<ITagLayoutProperty> calculated = calcuatePropertiesDeepFirst(layoutContext, node);
                if (calculated != null && calculated.size() > 0)
                {
                    layoutNodes.removeAll(calculated);
                    count = layoutNodes.size();
                }
            }
        }
    }

    private List<ITagLayoutProperty> calcuatePropertiesDeepFirst(TagLayoutContext context, ITagLayoutProperty node)
    {
        if (!node.canCalcuate())
        {
            return null;
        }
        List<ITagLayoutProperty> list = new ArrayList<ITagLayoutProperty>();
        node.calculate(context);
        list.add(node);
        List<ITagLayoutProperty> descends = node.getChildren();
        if (descends == null || descends.size() <= 0)
        {
            return list;
        }
        for (ITagLayoutProperty descend : descends)
        {
            List<ITagLayoutProperty> items = calcuatePropertiesDeepFirst(context, descend);
            if (items != null && descends.size() > 0)
            {
                list.addAll(items);
            }
        }
        return list;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int count = this.getChildCount();
        // Log.d("tag_layout", "" + this.hashCode() + "layout: " + count + " " + (r-l) + " " + (b-t));
        for (int i = 0; i < count; i++)
        {
            View child = this.getChildAt(i);
            TagLayoutParams params = layoutContext.getTagLayoutParams(child);
            int x = params.getLeft().getValue();
            int y = params.getTop().getValue();
            child.layout(x, y, x + params.getWidth().getValue(), y + params.getHeight().getValue());
            // Log.d("tag_layout", "child: x" + x + " " + y + " " + params.getWidth().getValue() + " " + params.getHeight().getValue());
        }
        // Log.d("tag_layout", "layout finished");
    }

    public static class TagLayoutParams
    {
        private ITagLayoutProperty width;

        private ITagLayoutProperty height;

        private ITagLayoutProperty left;

        private ITagLayoutProperty top;

        private TagLayoutContext context;

        private String tagString;

        private View child;

        public TagLayoutParams(TagLayoutContext context, View child, Object tag)
        {
            tagString = tag == null ? null : tag.toString();
            this.context = context;
            this.child = child;
        }

        public void createProperties()
        {

            String widthExpression = null;
            String heightExpression = null;
            String leftExpression = null;
            String topExpression = null;
            if (tagString != null)
            {
                String[] items = tagString.split(";");
                int count = items.length;
                while (--count >= 0)
                {
                    String item = items[count];
                    String[] inners = item.split(":");
                    String property = inners[0];
                    if (property.equals("w"))
                    {
                        widthExpression = inners[1];
                    } else if (property.equals("h"))
                    {
                        heightExpression = inners[1];
                    } else if (property.equals("l"))
                    {
                        leftExpression = inners[1];

                    }
                    if (property.equals("t"))
                    {
                        topExpression = inners[1];
                    }
                }
            }
            width = createPropertyNode(child, widthExpression, TagLayoutContext.RESULT_NO_CALCULATE);
            height = createPropertyNode(child, heightExpression, TagLayoutContext.RESULT_NO_CALCULATE);
            left = createPropertyNode(child, leftExpression, 0);
            top = createPropertyNode(child, topExpression, 0);

            context.unionLink(child, width, height);
            context.link(left);
            context.link(top);
        }

        private ITagLayoutProperty createPropertyNode(View child, String expression, int defaultValue)
        {
            if (expression == null || expression.length() == 0)
            {
                NoCalculateLayoutProperty prop = new NoCalculateLayoutProperty(child);
                prop.setDefault(defaultValue);
                return prop;
            }
            return new TagExpressionLayoutProperty(child, expression);
        }

        public ITagLayoutProperty getWidth()
        {
            return width;
        }

        public ITagLayoutProperty getHeight()
        {
            return height;
        }

        public ITagLayoutProperty getLeft()
        {
            return left;
        }

        public ITagLayoutProperty getTop()
        {
            return top;
        }
    }
}
