package com.TAP.TAP;

import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private NoScrollViewPager ui_viewpager;
    private RadioGroup ui_tabs;
    private RadioButton ui_taskbutton, ui_labelbutton, ui_checkbutton, ui_personalbutton;
    public ArrayList<Fragment> ui_fragments;
    long firstTimePressBack = 0;

    private View task_actionbar_view, label_actionbar_view, manage_actionbar_view, personal_actionbar_view;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            long thisTime = System.currentTimeMillis();
            if(firstTimePressBack == 0 || thisTime - firstTimePressBack > 1000) {
                Toast.makeText(MainActivity.this, R.string.press_again, Toast.LENGTH_SHORT).show();
                firstTimePressBack = thisTime;
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        getSupportActionBar().hide();

        setContentView(R.layout.main_layout);

        init();

        //设置pager
        ui_fragments = new ArrayList<>();
        ui_fragments.add(new TaskFragment());
        ui_fragments.add(new LabelFragment());
        ui_fragments.add(new ManageFragment());
        ui_fragments.add(new PersonalFragment());

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
                        getSupportActionBar().setCustomView(task_actionbar_view);
                        getSupportActionBar().show();
                        ui_tabs.check(R.id.main_task);
                        break;
                    case 1:
                        if(Global.currentTask != null && Global.currentTask.content.length() > 0) {
                            ui_viewpager.setNoScroll(true);
                        }
                        getSupportActionBar().setCustomView(label_actionbar_view);
                        getSupportActionBar().show();
                        ui_tabs.check(R.id.main_label);
                        break;
                    case 2:
                        getSupportActionBar().setCustomView(manage_actionbar_view);
                        getSupportActionBar().show();
                        ui_tabs.check(R.id.main_check);
                        break;
                    case 3:
                        getSupportActionBar().setCustomView(personal_actionbar_view);
                        getSupportActionBar().show();
                        ui_tabs.check(R.id.main_personal);
                        break;
                }
                if(position >= 0 && position <= 3 && position != ui_viewpager.getCurrentItem()) {
                    switch (position) {

                    }
                }
            }


            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        ui_taskbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_viewpager.setCurrentItem(0);
            }
        });
        ui_labelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_viewpager.setCurrentItem(1);
            }
        });
        ui_checkbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_viewpager.setCurrentItem(2);
            }
        });
        ui_personalbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_viewpager.setCurrentItem(3);
            }
        });
        setupActionBar();

        getSupportActionBar().show();
    }

    public View getActionBarView(int page_index) {
        switch (page_index) {
            case 0:
                return task_actionbar_view;
                //break;
            case 1:
                return label_actionbar_view;
                //break;
            case 2:
                return manage_actionbar_view;
               // break;
            case 3:
                return personal_actionbar_view;
                //break;
        }return null;
    }

    private void init() {
        ui_viewpager = findViewById(R.id.main_viewpager);
        ui_taskbutton = findViewById(R.id.main_task);
        ui_labelbutton = findViewById(R.id.main_label);
        ui_checkbutton = findViewById(R.id.main_check);
        ui_personalbutton = findViewById(R.id.main_personal);
        ui_tabs = findViewById(R.id.main_tabs);
    }

    public void switchTab(int position) {
        ui_viewpager.setCurrentItem(position);
    }

    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);

        //for task
        ab.setCustomView(R.layout.task_actionbar_layout);
        task_actionbar_view = ab.getCustomView();
        ((Button)task_actionbar_view.findViewById(R.id.actionbar_refresh_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 ((TaskFragment)ui_fragments.get(0)).doRefresh();
            }
        });

        //for label
        ab.setCustomView(R.layout.label_actionbar_layout);
        label_actionbar_view = ab.getCustomView();
        SeekBar skp = label_actionbar_view.findViewById(R.id.label_font_seekbar);
        skp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ((LabelFragment)ui_fragments.get(1)).setTextSize(24 + i / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((Button)label_actionbar_view.findViewById(R.id.label_revert_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LabelFragment)ui_fragments.get(1)).doRevert();
            }
        });
        ((Button)label_actionbar_view.findViewById(R.id.label_save_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LabelFragment)ui_fragments.get(1)).doSave();
            }
        });

        //for manage
        ab.setCustomView(R.layout.manage_actionbar_layout);
        manage_actionbar_view = ab.getCustomView();

        //for personal
        ab.setCustomView(R.layout.personal_actionbar_layout);
        personal_actionbar_view = ab.getCustomView();

        ab.setCustomView(task_actionbar_view);
    }
}
