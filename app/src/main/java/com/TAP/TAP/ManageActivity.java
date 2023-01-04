package com.TAP.TAP;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ManageActivity extends AppCompatActivity {

    private ManageCustomFragment managefrag;

    @Override
    public void onCreate(Bundle savedIntance) {
        super.onCreate(savedIntance);
        getSupportActionBar().hide();
        autoSwitch();
    }

    @Override
    public void onResume() {
        super.onResume();
        autoSwitch();
    }

    private void autoSwitch() {
        ManageCustomFragment frag = null;
        int res = 0;
        switch (Global.currentManage) {
            case Global.MANAGE_NONE:
                finish();
                break;
            case Global.MANAGE_SMARTLABEL:
                frag = new SmartLabelFragment(this);
                res = R.layout.manage_smartlabel_layout;
                break;
            case Global.MANAGE_ITERATE:
                frag = new IterateFragment(this, getSupportFragmentManager());
                res = R.layout.iterate_layout;
                break;
            case Global.MANAGE_TASKMANAGE:
                frag = new TaskManageFragment(this);
                res = R.layout.manage_taskmanage_layout;
                break;
        }
        if(frag != null && frag != managefrag) {
            frag.createView(res);
            setContentView(frag.getView());
            frag.onResume();
            managefrag = frag;
        }
    }
}
