package com.TAP.TAP;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class DocumentLabelActivity extends AppCompatActivity {

    long firstTimePressBack = 0;
    ArrayList<Fragment> ui_fragments;
    NoScrollViewPager ui_viewpager;
    Button ui_document_search_vp_button, ui_document_label_vp_button;
    RadioGroup ui_tabs;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            long thisTime = System.currentTimeMillis();
            if(firstTimePressBack == 0 || thisTime - firstTimePressBack > 1000) {
                Toast.makeText(DocumentLabelActivity.this, R.string.press_again_document_return, Toast.LENGTH_SHORT).show();
                firstTimePressBack = thisTime;
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.document_label_layout);

        //设置pager
        ui_viewpager = findViewById(R.id.document_viewpager);
        ui_document_search_vp_button = findViewById(R.id.document_search_vp_button);
        ui_document_label_vp_button = findViewById(R.id.document_label_vp_button);

        ui_fragments = new ArrayList<>();
        ui_fragments.add(new DocumentSearchFragment());
        ui_fragments.add(new DocumentLabelFragment());
        ui_tabs = findViewById(R.id.document_label_tabs);

        FragmentPagerAdapter fragment_adapter = (new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ui_fragments.get(position);
            }

            @Override
            public int getCount() {
                return ui_fragments.size();
            }
        });

        ui_viewpager.setAdapter(fragment_adapter);

        ui_viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                ui_viewpager.setNoScroll(false);
                //getSupportActionBar().hide();
                switch (position) {
                    case 0:
                        ui_tabs.check(R.id.document_search_vp_button);
                        break;
                    case 1:
                        ui_viewpager.setNoScroll(true);
                        ui_tabs.check(R.id.document_label_vp_button);
                        break;
                }
                /*
                if(position >= 0 && position <= 3 && position != ui_viewpager.getCurrentItem()) {
                    switch (position) {

                    }
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        ui_document_search_vp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_viewpager.setCurrentItem(0);
            }
        });
        ui_document_label_vp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_viewpager.setCurrentItem(1);
            }
        });

    }

    public void switchTab(int position) {
        ui_viewpager.setCurrentItem(position);
    }

    public void setupLabelTask(PubmedClient.PubmedAbstract pab) {
        switchTab(1);
        ((DocumentLabelFragment)ui_fragments.get(1)).setupLabel(pab);
    }
}
