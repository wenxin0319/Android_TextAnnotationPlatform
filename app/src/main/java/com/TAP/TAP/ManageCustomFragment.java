package com.TAP.TAP;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public abstract class ManageCustomFragment {
    private Context context;
    private View fragView;
    ManageCustomFragment(Context context) {
        this.context = context;
    }

    Context getContext() {
        return context;
    }
    Activity getActivity() {return (Activity)context;}

    abstract void postCreateView(View view);

    void createView(int resid) {
        fragView = LayoutInflater.from(getContext()).inflate(resid, null);
        postCreateView(fragView);
    }

    View getView() {
        return fragView;
    }

    void onResume() {

    }
}
