package cn.qiang.zhang.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * 自定义标签
 * <p>
 * 继承于{@link TextView}，使用Builder方式构建属性，通过build创建实例，使用with贴上标签，最终show出来。
 * <p>
 * 需要注意的是，这里会把target view的原本布局属性变成一个FrameLayout，然后让target view和这个label放在
 * 同一layout中，使target实例不变，通过设定重心改变label位置达到贴标签的目的。
 * <p>
 * PS:这个组件修改自github上的<a href="https://github.com/stefanjauker/BadgeView">BadgeView</a>
 * <p>
 * Created by mrZQ on 2016/10/18.
 */
public class LabelView extends TextView {

    /** 位置枚举 */
    public enum POSITION {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER,
    }

    /*默认参数*/
    /** 边距是指相对于父布局，当前标签的间隔距离，通常用于设置标签的位置 */
    private static final int DEFAULT_MARGIN_DIP = 5;
    /** 内距是指内部文字与边界的距离，通常不需要改动它 */
    private static final int DEFAULT_LR_PADDING_DIP = 5;
    /** 角标圆半径是指左右边界的圆角半径，通常不改动，除非TextSize太大而显得不美观 */
    private static final int DEFAULT_CORNER_RADIUS_DIP = 8;
    /** 重心位置，通常是粘贴在右上角，使用边距进行偏移量设置，以改动当前标签的位置 */
    private static final POSITION DEFAULT_POSITION = POSITION.TOP_RIGHT;
    /** 背景颜色，默认红色，可以在Builder中设置 */
    private static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#FF3B30"); //Color.RED;
    /** 文字颜色 */
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    /** 文字大小 */
    private static final float DEFAULT_TEXT_SIZE = 12;

    /*实例参数*/
    /** 动画类：入场 */
    final Animation fadeIn;
    /** 动画类：出场 */
    final Animation fadeOut;
    /** 上下文：事实上也可以通过getContext()获取 */
    final Context context;
    /** 目标视图，即悬浮其上的视图 */
    final View targetView;
    /** 角标位置 */
    final POSITION badgePosition;
    /** 角标水平边距 */
    final int badgeMarginH;
    /** 角标垂直边距 */
    final int badgeMarginV;
    /** 角标文字颜色 */
    final int badgeColor;
    /** 角标文字大小 */
    final float badgeSize;
    /** 目标视图为Tab子View时的下标 */
    final int targetTabIndex;
    /** 是否使用粗体字 */
    final boolean isUseBold;
    /** 是否在设置单位时直接使用dip转换方法 */
    final boolean isUseDip;
    /** 文字颜色 */
    final int textColor;
    /** 文字内容 */
    final String message;

    /*动态参数*/
    /** 是否显示 */
    private boolean isShown;
    /** 角标背景 */
    private ShapeDrawable badgeBg;

    public LabelView(Context context) {
        this(new Builder(context));
    }

    public LabelView(Builder builder) {
        this(builder.context, builder.targetView, null, android.R.attr.textViewStyle, builder);
    }

    /**
     * 如果仅仅需要改变其中一个属性，就调用这个方法去重新构造
     * @return 构造器
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * 必须要有的构造方法
     * @param context    上下文
     * @param targetView 目标View
     * @param attrs      属性集合
     * @param defStyle   默认风格
     * @param builder    构造器
     */
    public LabelView(Context context, View targetView, @Nullable AttributeSet attrs, int defStyle,
                     Builder builder) {
        super(context, attrs, defStyle);
        this.context = context;
        if (targetView instanceof TabWidget) {
            targetView = ((TabWidget) targetView).getChildTabViewAt(builder.targetTabIndex);
        }
        this.targetView = targetView;
        this.fadeIn = builder.fadeIn;
        this.fadeOut = builder.fadeOut;
        this.badgePosition = builder.badgePosition;
        this.badgeColor = builder.badgeColor;
        this.badgeSize = builder.badgeSize;
        this.targetTabIndex = builder.targetTabIndex;
        this.isShown = builder.isShown;
        this.isUseBold = builder.isUseBold;
        this.isUseDip = builder.isUseDip;
        if (isUseDip) {
            this.badgeMarginH = dipToPixels(builder.badgeMarginH);
            this.badgeMarginV = dipToPixels(builder.badgeMarginV);
        } else {
            this.badgeMarginH = builder.badgeMarginH;
            this.badgeMarginV = builder.badgeMarginV;
        }
        if (!TextUtils.isEmpty(builder.message)) {
            setText(builder.message);
        }
        if (builder.textColor != DEFAULT_TEXT_COLOR) {
            setTextColor(builder.textColor);
        }
        this.textColor = builder.textColor;
        this.message = builder.message;

        init();
    }

    /**
     * 初始化
     */
    private void init() {
        this.badgeBg = getDefaultBackground();
        // 设置文字气泡的左右填充边距
        int paddingPixels = dipToPixels(DEFAULT_LR_PADDING_DIP);
        setPadding(paddingPixels, 0, paddingPixels, 0);
        // 设置文字默认颜色
        setTextColor(DEFAULT_TEXT_COLOR);
        // 设置文字默认大小
        setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 阴影
            setTranslationZ(dipToPixels(1));
        }
        // 判断是否传入目标视图
        if (this.targetView != null) {
            // 应用到目标视图
            applyTo(this.targetView);
        } else {
            // 直接显示当前视图
            show();
        }
    }

    /**
     * 应用到目标视图
     * @param target 目标视图
     */
    private void applyTo(View target) {
        // 取得目标视图的布局参数
        ViewGroup.LayoutParams lp = target.getLayoutParams();
        // 得到目标视图的父布局实例
        ViewParent parent = target.getParent();
        // 得到目标tag存储的帧布局
        FrameLayout container = (FrameLayout) target.getTag();
        // 如果不存在，创建这个帧布局——用于替换目标视图所在位置，同时容纳目标视图和当前角标视图
        if (container == null) {
            container = new FrameLayout(context);
            // 暂存新建的帧布局，用于页面处理
            target.setTag(container);
        }
        // 如果这个帧布局已是目标的父布局，那么直接添加
        if (container.equals(parent)) {
            this.setVisibility(View.GONE);
            container.addView(this);
        } else {
            // 如果目标视图是TabWidget组件
            if (target instanceof TabWidget) {
                // 添加帧布局
                ((ViewGroup) target).addView(
                        container,
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                // 设置当前角标视图消失
                this.setVisibility(View.GONE);
                // 添加到帧布局中
                container.addView(this);
            } else {
                // 如果是其他组件
                ViewGroup group = (ViewGroup) parent;
                // 找到目标视图在父容器中的位置
                int index = group.indexOfChild(target);
                // 移除目标视图
                group.removeView(target);
                // 将帧布局添加到原先目标视图的位置
                group.addView(container, index, lp);
                // 添加原组件到帧布局中
                container.addView(target);
                // 使当前角标视图消失
                this.setVisibility(View.GONE);
                // 添加角标视图到帧布局中
                container.addView(this);
                // 重绘父容器
                group.invalidate();
            }
        }
    }

    /**
     * 显示角标1——没有动画
     */
    public void show() {
        show(false, null);
    }

    /**
     * 显示角标2——是否使用动画
     * @param animate true 表示使用动画；false 表示不使用动画
     */
    public void show(boolean animate) {
        show(animate, fadeIn);
    }

    /**
     * 显示角标3——使用自定义动画
     * @param anim 自定义动画
     */
    public void show(Animation anim) {
        show(true, anim);
    }

    /**
     * 隐藏角标1——没有动画
     */
    public void hide() {
        hide(false, null);
    }

    /**
     * 隐藏角标2——是否使用动画
     * @param animate true 表示使用动画；false 表示不使用动画
     */
    public void hide(boolean animate) {
        hide(animate, fadeOut);
    }

    /**
     * 隐藏角标3——使用自定义动画
     * @param anim 自定义动画
     */
    public void hide(Animation anim) {
        hide(true, anim);
    }

    /**
     * 切换角标1——没有动画
     */
    public void toggle() {
        toggle(false, null, null);
    }

    /**
     * 切换角标2——是否使用动画
     * @param animate true 表示使用动画；false 表示不使用动画
     */
    public void toggle(boolean animate) {
        toggle(animate, fadeIn, fadeOut);
    }

    /**
     * 切换角标3——使用自定义动画
     * @param animIn  自定义入场动画
     * @param animOut 自定义出场动画
     */
    public void toggle(Animation animIn, Animation animOut) {
        toggle(true, animIn, animOut);
    }

    /**
     * 显示角标
     * @param animate 是否显示动画
     * @param anim    动画对象
     */
    private void show(boolean animate, Animation anim) {
        // 是否使用粗字体
        if (isUseBold) {
            setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            // 默认字体
            setTypeface(Typeface.DEFAULT);
        }
        // 检查是否改变字体大小
        if (DEFAULT_TEXT_SIZE != badgeSize) {
            // 应用改变后的字体大小：sp单位
            setTextSize(TypedValue.COMPLEX_UNIT_SP, badgeSize);
        }
        // 如果没有设置背景
        if (getBackground() == null) {
            // 没有自定义背景
            if (badgeBg == null) {
                // 使用默认背景
                badgeBg = getDefaultBackground();
            }
            // 根据SDK版本采用方法
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection deprecation
                setBackgroundDrawable(badgeBg);
            } else {
                setBackground(badgeBg);
            }
        }
        // 应用布局参数
        applyLayoutParams();
        // 是否显示入场动画
        if (animate) {
            // 启动入场动画
            this.startAnimation(anim);
        }
        // 显示
        this.setVisibility(View.VISIBLE);
        // 标记已显示
        isShown = true;
    }

    /**
     * 隐藏角标
     * @param animate 是否显示动画
     * @param anim    动画对象
     */
    private void hide(boolean animate, Animation anim) {
        // 消失
        this.setVisibility(View.GONE);
        // 是否显示出场动画
        if (animate) {
            // 启动出场动画
            this.startAnimation(anim);
        }
        // 标记已消失
        isShown = false;
    }

    /**
     * 切换角标
     * @param animate 是否显示动画
     * @param animIn  入场动画
     * @param animOut 出场动画
     */
    private void toggle(boolean animate, Animation animIn, Animation animOut) {
        // 显示与否的标记
        if (isShown) {
            // 显示则隐藏
            hide(animate && (animOut != null), animOut);
        } else {
            // 隐藏则显示
            show(animate && (animIn != null), animIn);
        }
    }

    /**
     * 增加一个数字角标，如果当前显示的文字无法转换为Integer类型，则将当前文字设为——0
     * @param offset 数字增量
     * @return 加上offset之后的值
     */
    public int increment(int offset) {
        CharSequence txt = getText();
        int i;
        if (txt != null) {
            try {
                i = Integer.parseInt(txt.toString());
            } catch (NumberFormatException e) {
                i = 0;
            }
        } else {
            i = 0;
        }
        i = i + offset;
        setText(String.valueOf(i));
        return i;
    }

    /**
     * 减去一个数字角标，利用加上负数的性质，调用增加角标方法
     * @param offset 数字减量
     * @return 减去offset之后的值
     */
    public int decrement(int offset) {
        return increment(-offset);
    }

    /**
     * 获取默认背景
     * @return 背景图
     */
    private ShapeDrawable getDefaultBackground() {
        // 默认的圆角半径
        int r = dipToPixels(DEFAULT_CORNER_RADIUS_DIP);
        // 创建外圆范围
        float[] outerR = new float[]{r, r, r, r, r, r, r, r};
        // 根据外圆范围创建圆——忽略内圆和圆环
        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        // 画出来这个圆
        ShapeDrawable drawable = new ShapeDrawable(rr);
        // 设置圆颜色
        drawable.getPaint().setColor(badgeColor);
        return drawable;
    }

    /**
     * 应用布局参数
     */
    @SuppressLint("RtlHardcoded")
    private void applyLayoutParams() {
        // 创建帧布局参数
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // 根据设定位置设置边距
        switch (badgePosition) {
            case TOP_LEFT:
                lp.gravity = Gravity.LEFT | Gravity.TOP;
                lp.setMargins(badgeMarginH, badgeMarginV, 0, 0);
                break;
            case TOP_RIGHT:
                lp.gravity = Gravity.RIGHT | Gravity.TOP;
                lp.setMargins(0, badgeMarginV, badgeMarginH, 0);
                break;
            case BOTTOM_LEFT:
                lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                lp.setMargins(badgeMarginH, 0, 0, badgeMarginV);
                break;
            case BOTTOM_RIGHT:
                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                lp.setMargins(0, 0, badgeMarginH, badgeMarginV);
                break;
            case CENTER:
                lp.gravity = Gravity.CENTER;
                lp.setMargins(0, 0, 0, 0);
                break;
            default:
                break;
        }
        // 将参数设置进当前角标视图
        setLayoutParams(lp);
    }

    /**
     * 返回已设置进来的目标视图
     * @return 目标视图
     */
    public View getTargetView() {
        return targetView;
    }

    /**
     * 当前视图是否已显示
     */
    @Override
    public boolean isShown() {
        return isShown;
    }

    /**
     * 得到当前视图位于原目标视图的重心位置
     * @return 重心位置
     */
    public POSITION getBadgePosition() {
        return badgePosition;
    }

    /**
     * 得到角标相对于原目标视图位置的内距偏移量——左上位置则偏移左边，右上位置则偏移右边
     * @return 偏移的像素值
     */
    public int getHorizontalBadgeMargin() {
        return badgeMarginH;
    }

    /**
     * 得到角标相对于原目标视图位置的内距偏移量——左上位置则偏移上面，左下位置则偏移下面
     * @return 偏移的像素值
     */
    public int getVerticalBadgeMargin() {
        return badgeMarginV;
    }

    /**
     * 获得当前背景颜色
     * @return 颜色值
     */
    public int getBadgeBackgroundColor() {
        return badgeColor;
    }

    /**
     * 内置的单位转换工具
     * @param dip 需要转换的单位值
     * @return 从数值转换为dip单位的值
     */
    public int dipToPixels(int dip) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r
                .getDisplayMetrics());
        return (int) px;
    }

    /**
     * 是否使用粗体字样
     * @return true 表示正在使用；false 表示没有使用
     */
    public boolean isUseBold() {
        return isUseBold;
    }

    /**
     * 获得当前字体大小
     * @return 字体大小值
     */
    public float getBadgeSize() {
        return badgeSize;
    }

    /**
     * 是否在设置时直接使用Dip转换单位
     * @return true 表示使用；false 表示不使用
     */
    public boolean isUseDip() {
        return isUseDip;
    }

    public static final class Builder {
        Animation fadeIn;
        Animation fadeOut;
        Context context;
        View targetView;
        POSITION badgePosition;
        int badgeMarginH;
        int badgeMarginV;
        int badgeColor;
        float badgeSize;
        boolean isShown;
        int targetTabIndex;
        boolean isUseBold;
        boolean isUseDip;
        private String message;
        private int textColor;

        Builder(LabelView labelView) {
            this.fadeIn = labelView.fadeIn;
            this.fadeOut = labelView.fadeOut;
            this.context = labelView.context;
            this.targetView = labelView.targetView;
            this.badgePosition = labelView.badgePosition;
            this.badgeMarginH = labelView.badgeMarginH;
            this.badgeMarginV = labelView.badgeMarginV;
            this.badgeColor = labelView.badgeColor;
            this.badgeSize = labelView.badgeSize;
            this.targetTabIndex = labelView.targetTabIndex;
            this.isShown = labelView.isShown;
            this.isUseBold = labelView.isUseBold;
            this.isUseDip = labelView.isUseDip;
            this.message = labelView.getText().toString();
            this.textColor = labelView.getTextColors().getDefaultColor();
        }

        public Builder(Context context) {
            this(context, 0);
        }

        public Builder(Context context, int tabIndex) {
            this.context = context;
            fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(300);
            fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(300);
            badgePosition = DEFAULT_POSITION;
            badgeMarginH = DEFAULT_MARGIN_DIP;
            badgeMarginV = badgeMarginH;
            badgeColor = DEFAULT_BACKGROUND_COLOR;
            badgeSize = DEFAULT_TEXT_SIZE;
            targetTabIndex = tabIndex;
            isShown = false;
            isUseBold = false;
            isUseDip = true;
            message = "";
            textColor = LabelView.DEFAULT_TEXT_COLOR;
        }

        public Builder with(View targetView) {
            this.targetView = targetView;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder animationFadeIn(Animation fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public Builder animationFadeOut(Animation fadeOut) {
            this.fadeOut = fadeOut;
            return this;
        }

        public Builder position(POSITION badgePosition) {
            this.badgePosition = badgePosition;
            return this;
        }

        public Builder margin(int badgeMargin) {
            this.badgeMarginH = badgeMargin;
            this.badgeMarginV = badgeMargin;
            return this;
        }

        /**
         * 设置相对于上下左右的偏移量
         * @param horizontal 内距水平方向的偏移量像素值
         * @param vertical   内距垂直方向的偏移量像素值
         */
        public Builder margin(int horizontal, int vertical) {
            this.badgeMarginH = horizontal;
            this.badgeMarginV = vertical;
            return this;
        }

        public Builder badgeColor(@ColorInt int badgeColor) {
            this.badgeColor = badgeColor;
            return this;
        }

        public Builder textSize(int badgeSize) {
            this.badgeSize = badgeSize;
            return this;
        }

        public Builder textColor(@ColorInt int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder tabIndex(int targetTabIndex) {
            this.targetTabIndex = targetTabIndex;
            return this;
        }

        public Builder useBold(boolean isUseBold) {
            this.isUseBold = isUseBold;
            return this;
        }

        public Builder useDip(boolean isUseDip) {
            this.isUseDip = isUseDip;
            return this;
        }

        public LabelView build() {
            if (targetView == null) {
                throw new NullPointerException("targetView is null");
            }
            return new LabelView(this);
        }

    }

}
