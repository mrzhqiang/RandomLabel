package cn.qiang.zhang.randomlabel;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.qiang.zhang.library.LabelView;

/**
 * 标签管理
 * <p>
 * Created by mrZQ on 2017/2/3.
 */
public final class LabelManager {
    private static final String TAG = "LabelManager";

    /** 初始标签 */
    private final LabelView labelView;
    private final FrameLayout layout;

    /** 标签布局的矩形范围 */
    private Rect container;
    /** 添加标签时，在四个区域内随机选区并随机显示 */
    private List<Rect> spaceList = new ArrayList<>();
    /** 额外的需要避开的区域，通常是那些遮挡标签的上层View */
    private List<Rect> otherList = new ArrayList<>();
    private List<LabelView> labelList = new ArrayList<>();

    public static LabelManager create(View targetView, String message) {
        return new LabelManager(targetView, message);
    }

    private LabelManager(View targetView, String message) {
        this(new LabelView.Builder(targetView.getContext())
                     .with(targetView)
                     .margin(20)
                     .message(message)
                     .build());
    }

    public LabelManager(LabelView labelView) {
        this.labelView = labelView;
        // 用于移除所有新添加的标签
        this.layout = (FrameLayout) labelView.getTargetView().getTag();
        labelView.show();
    }

    /** 设置占位中心 */
    public void setPlaceHolder(View layout, View view) {
        spaceList.clear();
        this.container = getRect(layout);
        for (int i = 0; i < 4; i++) {
            spaceList.add(createSpace(i, getRect(view)));
        }
    }

    public void addPlaceHolder(View view) {
        otherList.add(getRect(view));
    }

    public void clear() {
        otherList.clear();
        for (LabelView labelView : labelList) {
            layout.removeView(labelView);
        }
        labelList.clear();
    }

    /** 通过下标和占位视图创建可添加的空间 */
    private Rect createSpace(int i, Rect viewRect) {
        // 占位视图范围
        int vL = viewRect.left;
        int vR = viewRect.right;
        int vT = viewRect.top;
        int vB = viewRect.bottom;
        switch (i) {
            case 0:
                // 取水平方向的一半
                return new Rect(container.left + dipToPixels(16),
                                container.top + dipToPixels(36),
                                (container.left + vL) / 2,
                                container.bottom - dipToPixels(16));
            case 1:
                return new Rect(container.left + dipToPixels(16),
                                vB,
                                (container.right + vR) / 2,
                                container.bottom - dipToPixels(16));
            case 2:
                // 取水平方向的一半
                return new Rect(vR,
                                container.top + dipToPixels(16),
                                (container.right + vR) / 2,
                                container.bottom - dipToPixels(16));
            case 3:
                return new Rect(container.left + dipToPixels(16),
                                container.top + dipToPixels(36),
                                (container.right + vR) / 2,
                                vT - dipToPixels(20));
        }
        return container;
    }

    public void addLabel(String message) {
        LabelView.Builder builder = labelView.newBuilder()
                .message(message)
                .position(LabelView.POSITION.TOP_LEFT)
                .badgeColor(getRandomColor())
                .useDip(false);
        // 获取不在占位资源范围内标签的随机坐标
        int[] randomLocation = getNotHolderRect(getRandomIndex());
        // 生成View
        LabelView labelView = builder
                .margin(randomLocation[0], randomLocation[1])
                .build();
        labelView.show();
        labelList.add(labelView);
    }

    private int dipToPixels(int dip) {
        Resources r = labelView.getContext().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r
                .getDisplayMetrics());
        return (int) px;
    }

    private Rect getRect(View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect;
    }

    /** 随机颜色 */
    private int getRandomColor() {
        return Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    /**
     * 随机的偏离占位中心的坐标
     * @param randomIndex 随机区域下标
     */
    private int[] getNotHolderRect(int randomIndex) {
        return getRandomLocation(spaceList.get(randomIndex));
    }

    /** 随机位置 */
    private int[] getRandomLocation(Rect rect) {
        // 跳出其他必须避开的区域
        for (Rect other : otherList) {
            // 范围是否包含需要避开的区域
            if (rect.bottom > other.top) {
                // 目前是粗略的避开
                rect = new Rect(rect.left, rect.top, rect.right, other.top - dipToPixels(20));
            }
        }
        // 范围内随机，约定起点即划分坐标区域，随后减去目标的顶点即等于相对于左上角的margin值
        int x = rect.left + (int) (Math.random() * (rect.right - rect.left)) - container.left;
        int y = rect.top + (int) (Math.random() * (rect.bottom - rect.top)) - container.top;
        return new int[]{x, y};
    }

    private int currentIndex = 0;

    /** 随机区域下标 */
    private int getRandomIndex() {
        if (currentIndex < 0 || currentIndex >= spaceList.size()) {
            currentIndex = 0;
        }
        return currentIndex++;
    }

}
