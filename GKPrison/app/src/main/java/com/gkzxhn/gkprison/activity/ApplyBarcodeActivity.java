package com.gkzxhn.gkprison.activity;

import android.view.View;
import android.widget.ArrayAdapter;

import com.gkzxhn.gkprison.R;
import com.weiwangcn.betterspinner.library.BetterSpinner;

/**
 * created by hzn 2015/12/15
 * 申请二维码页面
 */
public class ApplyBarcodeActivity extends BaseActivity {

    private final String[] PRISONS = {"监狱1", "监狱2", "监狱3", "监狱4"};

    private BetterSpinner bs_prison_choose;
    private ArrayAdapter prisonAdapter;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_apply_barcode, null);
        bs_prison_choose = (BetterSpinner) view.findViewById(R.id.bs_prison_choose);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("申请二维码");
        setBackVisibility(View.VISIBLE);
        prisonAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, PRISONS);
        bs_prison_choose.setAdapter(prisonAdapter);
    }
}
