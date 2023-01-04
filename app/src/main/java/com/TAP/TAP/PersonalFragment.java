package com.TAP.TAP;

import android.content.Context;
import android.content.Intent;
import android.gesture.GestureLibraries;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PersonalFragment extends Fragment {
    private ListView ui_task_menu, ui_personal_menu, ui_setting_menu;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstance) {
        View view = inflater.inflate(R.layout.personal_layout, container, false);

        ui_task_menu = view.findViewById(R.id.personal_task_menu);
        ui_personal_menu = view.findViewById(R.id.personal_personal_menu);
        ui_setting_menu = view.findViewById(R.id.personal_setting_menu);

        if(Global.currentUser != null) {

            ((TextView) (view.findViewById(R.id.personal_nickname))).setText(Global.currentUser.nickname);
            ((TextView) (view.findViewById(R.id.personal_username))).setText(Global.currentUser.username);

            if (Global.currentUser.faceImage == null) {
                //TODO : 用户头像
            }
        }

        setupMenus(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    //菜单项
    private class PersonalMenuItem {
        private String text;
        private int imgId;

        public PersonalMenuItem(String text, int imgId) {
            this.text = text;
            this.imgId = imgId;
        }

        public String getText() {
            return this.text;
        }

        public int getImgId(){
            return this.imgId;
        }

    }

    private class MemuItemAdapter extends ArrayAdapter<PersonalMenuItem> {
        private int resourceId;
        public MemuItemAdapter(Context context, int resid, List<PersonalMenuItem> objects) {
            super(context, resid, objects);
            resourceId = resid;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PersonalMenuItem item = getItem(position);

            View view;
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);

            ImageView imgv = view.findViewById(R.id.personal_menu_icon);
            TextView tv = view.findViewById(R.id.personal_menu_text);

            tv.setText(item.getText());
            imgv.setImageResource(item.getImgId());

            return view;
        }

    }


    void setupMenus(View view) {
        //task
        List<PersonalMenuItem> myTaskItems = new ArrayList<PersonalMenuItem>();

        myTaskItems.add(new PersonalMenuItem("已完成任务", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));
//        myTaskItems.add(new PersonalMenuItem("未完成任务", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));
        myTaskItems.add(new PersonalMenuItem("待审核任务", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));
        myTaskItems.add(new PersonalMenuItem("审核通过任务", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));
        myTaskItems.add(new PersonalMenuItem("审核不通过", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));


        MemuItemAdapter myTaskMenu = new MemuItemAdapter(getContext(), R.layout.personal_menuitem_layout, myTaskItems);
        ListView myTaskView = view.findViewById(R.id.personal_task_menu);
        myTaskView.setAdapter(myTaskMenu);

        //personal
        List<PersonalMenuItem> myPersonalItems = new ArrayList();
        myPersonalItems.add(new PersonalMenuItem("我的收藏", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));
        myPersonalItems.add(new PersonalMenuItem("积分商城", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));


        MemuItemAdapter personalMenu = new MemuItemAdapter(getContext(), R.layout.personal_menuitem_layout, myPersonalItems);
        ListView personalView = view.findViewById(R.id.personal_personal_menu);
        personalView.setAdapter(personalMenu);

        //settings
        List<PersonalMenuItem> settingsItems = new ArrayList();
        settingsItems.add(new PersonalMenuItem("设置", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));
        settingsItems.add(new PersonalMenuItem("退出", com.google.android.material.R.drawable.ic_mtrl_chip_checked_circle));
        MemuItemAdapter settingsMenu = new MemuItemAdapter(getContext(), R.layout.personal_menuitem_layout, settingsItems);
        ListView settingsView = view.findViewById(R.id.personal_setting_menu);
        settingsView.setAdapter(settingsMenu);


        //setup listeners
        //TODO: task
        personalView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        //point shop
                        Intent intent = new Intent(PersonalFragment.this.getContext(), ShopActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
        //TODO: personal

        //TODO: settings
        settingsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:  //设置
                        break;
                    case 1: {
                        Global.clear();
                        Intent intent = new Intent();
                        intent.setClass(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }break;
                }
            }
        });
    }
}
