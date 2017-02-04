package cn.qiang.zhang.randomlabel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;

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
    /** 标签布局的矩形范围 */
    private final Rect container;
    /** 添加标签时，在四个区域内随机选区并随机显示 */
    private List<Rect> spaceList = new ArrayList<>();

    public static LabelManager create(Context context, View targetView, String message) {
        LabelView labelView = new LabelView.Builder(context)
                .with(targetView)
                .margin(20)
                .message(message)
                .build();
        // 先显示出来这个初始标签，才有可能计算正确其他标签的屏幕坐标
        labelView.show();
        return new LabelManager(labelView);
    }

    private LabelManager(LabelView labelView) {
        this.labelView = labelView;
        // 得到目标布局的矩形范围
        container = getRect(labelView.getTargetView());
    }

    /** 设置占位中心 */
    public void setPlaceHolder(View view) {
        spaceList.clear();
        for (int i = 0; i < 4; i++) {
            spaceList.add(createSpace(i, view));
        }
    }

    /** 通过下标和占位视图创建可添加的空间 */
    private Rect createSpace(int i, View view) {
        Rect viewRect = getRect(view);
        int vL = viewRect.left;
        int vR = viewRect.right;
        int vT = viewRect.top;
        int vB = viewRect.bottom;
        switch (i) {
            case 0:
                return new Rect(container.left, container.top, vL, container.bottom);
            case 1:
                return new Rect(container.left, vB, container.right, container.bottom);
            case 2:
                return new Rect(vR, container.top, container.right, container.bottom);
            case 3:
                return new Rect(container.left, container.top, container.right, vT);
            default:
                break;
        }
        return container;
    }

    public void addLabel(String message) {
        LabelView.Builder builder = labelView.newBuilder()
                .message(message)
                .position(LabelView.POSITION.TOP_LEFT)
                .badgeColor(getRandomColor())
                .useDip(false);
        // 获取不在占位资源范围内的标签随机坐标
        Rect randomLocation = getNotHolderRect();
        LabelView labelView = builder.margin(randomLocation.left, randomLocation.top).build();
        labelView.show();
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

    /** 随机的偏离占位中心的坐标 */
    private Rect getNotHolderRect() {
        int randomIndex = getRandomIndex();
        Rect rect = spaceList.get(randomIndex);
        int x = rect.left + (int) (Math.random() * rect.right) - container.left;
        int y = rect.top + (int) (Math.random() * rect.bottom) - container.top;
        return new Rect(x, y, x, y);
    }

    private int getRandomIndex() {
        // 0--3
        int random = (int) (Math.random() * 4);
        if (random < 0 || random >= spaceList.size()) {
            random = 0;
        }
        return random;
    }

}
