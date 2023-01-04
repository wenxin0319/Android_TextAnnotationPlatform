package com.TAP.TAP;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ManageFragment extends Fragment {
    private View manage_view;

    private void showTips() {
        Toast.makeText(getContext(), "模块优化中，敬请期待", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstance) {
        manage_view = inflater.inflate(R.layout.manage_layout, container, false);

        ((LinearLayout)manage_view.findViewById(R.id.manage_goto_smartlabel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTips();
                /*
                Global.currentManage = Global.MANAGE_SMARTLABEL;
                Intent intent = new Intent(getContext(), ManageActivity.class);
                startActivity(intent);*/
            }
        });

        ((LinearLayout)manage_view.findViewById(R.id.managae_goto_iterate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Global.currentManage = Global.MANAGE_ITERATE;
                Intent intent = new Intent(getContext(), ManageActivity.class);
                startActivity(intent);
            }
        });

        ((LinearLayout)manage_view.findViewById(R.id.manage_goto_taskmanage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Global.currentManage = Global.MANAGE_TASKMANAGE;
                Intent intent = new Intent(getContext(), ManageActivity.class);
                startActivity(intent);
            }
        });

        ((LinearLayout)manage_view.findViewById(R.id.manage_goto_document_label)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DocumentLabelActivity.class);
                startActivity(intent);
            }
        });

        return manage_view;
    }
}
