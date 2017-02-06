package cn.qiang.zhang.randomlabel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DemoActivity extends AppCompatActivity {

    @BindView(R.id.demo_tv_content)
    TextView tvStatus;
    @BindView(R.id.demo_layout_label)
    FrameLayout layoutLabel;
    @BindView(R.id.demo_iv_placeholder)
    ImageView ivPlaceHolder;
    @BindView(R.id.demo_layout_placeholder)
    FrameLayout layoutPlaceHolder;
    @BindView(R.id.demo_btn_placeholder)
    Button btnPlaceHolder;

    @BindView(R.id.demo_layout_all)
    RelativeLayout layoutAll;

    LabelManager labelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @OnClick({R.id.demo_btn_placeholder, R.id.demo_iv_placeholder})
    void addLabel(View view) {
        if (view.equals(ivPlaceHolder)) {
            if (labelManager != null) {
                labelManager.clear();
                return;
            }
        }
        addRandom();
    }



    public void addRandom() {

        if (labelManager == null) {

            labelManager = LabelManager.create(layoutLabel, "已认证");
            labelManager.setPlaceHolder(layoutAll, ivPlaceHolder);
            labelManager.addPlaceHolder(layoutPlaceHolder);
            for (int i = 0; i < 10; i++) {
                labelManager.addLabel("序列" + i);
            }

        }
        labelManager.addLabel("随机" + (int) (10 + 99 * Math.random()));
    }
}
