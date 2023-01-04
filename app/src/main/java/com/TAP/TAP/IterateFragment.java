package com.TAP.TAP;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.column.Column;


import org.w3c.dom.Text;
import org.yecht.TokenScanner;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IterateFragment extends ManageCustomFragment {
    private NoScrollViewPager iterate_viewpager;
    private List<IterateTableFragment> tables;
    private RadioGroup iterate_tabs;
    private RadioButton iterate_entity_button, iterate_result_button;
    private FragmentManager fragmentManager;
    private Spinner result_type_spinner, entity_type_spinner;
    private Button iterate_operate_button;
    private int iterate_operate_status;
    private static final int ITER_OP_QUERY = 0;
    private static final int ITER_OP_ITERATE = 1;
    private static final int ITER_OP_ITERATING = 2;
    private Boolean need_get_results;
    private ClientSession session;

    private int result_type;
    private String entity_type;

    private List<String> selected_entities;
    private List<String> selected_entity_type;

    private Timer iterate_query_timer;
    private TimerTask iterate_query_task;

    public static class IterateTableFragment extends Fragment {
        CustomTableView table;

        Runnable onCreate;

        public void setOnCreateListener(Runnable listener) {
            onCreate = listener;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
            table = new CustomTableView(getContext());

            if(onCreate != null)
                onCreate.run();

            return table;
        }
        public CustomTableView getTable() {
            return table;
        }
    }

    IterateFragment(Context context, FragmentManager fragmentManager) {
        super(context);
        this.fragmentManager = fragmentManager;
        selected_entity_type = new ArrayList<>();
        selected_entities = new ArrayList<>();
    }

    public void postCreateView(View view) {

        iterate_viewpager = view.findViewById(R.id.iterate_viewpager);
        iterate_tabs = view.findViewById(R.id.iterate_tabs);

        tables = new ArrayList<>();
        tables.add(new IterateTableFragment());
        tables.add(new IterateTableFragment());
        iterate_viewpager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return tables.get(position);
            }

            @Override
            public int getCount() {
                return tables.size();
            }
        });
        iterate_viewpager.setOffscreenPageLimit(2);
        iterate_viewpager.setNoScroll(true);
        iterate_entity_button = view.findViewById(R.id.iterate_entity_type_check);
        iterate_result_button = view.findViewById(R.id.iterate_iterate_result_check);
        iterate_viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                iterate_tabs.check(position);
                if(position == 0) {
                    iterate_entity_button.setTextColor(Color.parseColor("#2c3e7f"));
                    iterate_result_button.setTextColor(Color.parseColor("#000000"));
                } else {
                    iterate_result_button.setTextColor(Color.parseColor("#2c3e7f"));
                    iterate_entity_button.setTextColor(Color.parseColor("#000000"));
                    if(need_get_results)
                        getResults();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        iterate_entity_button.setTextColor(Color.parseColor("#2c3e7f"));
        ((RadioButton)view.findViewById(R.id.iterate_entity_type_check)).setButtonDrawable(null);
        ((RadioButton)view.findViewById(R.id.iterate_entity_type_check)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iterate_viewpager.setCurrentItem(0);
            }
        });

        ((RadioButton)view.findViewById(R.id.iterate_iterate_result_check)).setButtonDrawable(null);
        ((RadioButton)view.findViewById(R.id.iterate_iterate_result_check)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iterate_viewpager.setCurrentItem(1);
            }
        });
        iterate_viewpager.setCurrentItem(0, false);


        //TODO: stub! 这里是写死的
        ArrayList<String> result_types = new ArrayList<>();
        result_types.add("全部");
        result_types.add("智能标注");
        result_types.add("任务标注");
        result_types.add("词典标注");
        result_type_spinner = view.findViewById(R.id.iterate_result_type_spinner);
        result_type_spinner.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, result_types));
        result_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                result_type = i-1;
                getEntities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        result_type_spinner.setSelection(0);

        final ArrayList<String> entity_types = new ArrayList<>();
        entity_types.add("全部");
        entity_types.add("中医疾病");
        entity_types.add("中药");
        entity_types.add("西药");
        entity_types.add("阳性症状");
        entity_types.add("西医疾病");
        entity_types.add("阴性症状");
        entity_types.add("处方");
        entity_types.add("舌脉");
        entity_types.add("证候");
        entity_types.add("治则治法");
        entity_type_spinner = view.findViewById(R.id.iterate_entity_type_spinner);
        entity_type_spinner.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, entity_types));
        entity_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                entity_type = entity_types.get(i);
                getEntities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        entity_type = "全部";
        result_type = -1;
        iterate_operate_status = ITER_OP_ITERATE;
        iterate_operate_button = view.findViewById(R.id.iterate_operate_button);
        iterate_operate_button.setText(R.string.iterate);
        iterate_operate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ITER_OP_ITERATE == iterate_operate_status) {
                    selected_entities.clear();
                    selected_entity_type.clear();
                    tables.get(0).getTable().iterateSelected(new CustomTableView.SelectedTraveler() {
                        @Override
                        public void call(View v) {
                            ViewGroup gp = (ViewGroup)v;
                            TextView tv0 = (TextView)gp.getChildAt(1);
                            TextView tv1 = (TextView)gp.getChildAt(2);
                            selected_entities.add(tv0.getText().toString());
                            selected_entity_type.add(tv1.getText().toString());
                        }
                    });
                    if(selected_entities.size() == 0) {
                        Toast.makeText(getContext(), R.string.plase_select_iterate_entities, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //!TODO: make it beautiful
                    iterate_query_timer = new Timer();
                    iterate_query_task = new TimerTask() {
                        @Override
                        public void run() {
                            final int rate = (int) (session.doIterate(selected_entities, selected_entity_type) + 0.5);
                            if (rate >= 100) {
                                getView().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        iterate_query_timer.cancel();
                                        //reload entity list
                                        getEntities();
                                        need_get_results = true;
                                        iterate_operate_button.setText(R.string.iterate);
                                        iterate_operate_status = ITER_OP_ITERATE;
                                    }
                                });

                            } else {
                                getView().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        iterate_operate_button.setText(rate + "%");
                                    }
                                });
                            }
                        }
                    };
                    iterate_operate_status = ITER_OP_ITERATING;
                    iterate_query_timer.schedule(iterate_query_task, 0, 400);

                }else if(ITER_OP_ITERATING == iterate_operate_status) {
                    Toast.makeText(getContext(), R.string.please_wait_for_iterate, Toast.LENGTH_LONG).show();
                }
            }
        });
        need_get_results = true;

        //设置表头，要等待加载成功后执行这段代码。。。太扯淡了
        tables.get(0).setOnCreateListener(new Runnable() {
            @Override
            public void run() {
                tables.get(0).getTable().post(new Runnable() {
                    @Override
                    public void run() {
                        String[] headers = {"", "实体", "实体类型"};
                        int[] weights = {2, 6, 4};
                        tables.get(0).getTable().setHeaders(headers, weights);
                    }
                });
                tables.get(0).getTable().setOnHeaderClickListener(new CustomTableView.OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(int position) {
                        //TODO: 点击表头时候的响应（比如排序）
                    }
                });
                tables.get(0).getTable().setOnItemClickListener(new CustomTableView.OnItemClickListener() {
                    @Override
                    public void OnItemClick(int position, ViewGroup item, boolean is_make_selected) {
                        TextView tv = (TextView)(item.getChildAt(0));
                        if(is_make_selected) {
                            tv.setText("√");
                        } else {
                            tv.setText("");
                        }
                    }
                });
                tables.get(0).getTable().setCreateItemFunctor(new CustomTableView.OnCreateItem() {
                    @Override
                    public ViewGroup createItem(int i, Object _item) {
                        Entities.Entity item = (Entities.Entity)_item;
                        LinearLayout container = new LinearLayout(getContext());
                        container.setOrientation(LinearLayout.HORIZONTAL);

                        TextView t0 = new TextView(getContext());
                        t0.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        t0.setTextSize(15);
                        t0.setTextColor(Color.parseColor("#000000"));
                        //t0.setText("-");
                        TextView t1 = new TextView(getContext());
                        t1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        t1.setText(item.entity);
                        t1.setTextSize(15);
                        t1.setTextColor(Color.parseColor("#000000"));
                        TextView t2 = new TextView(getContext());
                        t2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        t2.setText(item.entity_type);
                        t2.setTextSize(15);
                        t2.setTextColor(Color.parseColor("#000000"));

                        container.addView(t0);
                        container.addView(t1);
                        container.addView(t2);
                        return container;
                    }
                });
            }
        });

        tables.get(1).setOnCreateListener(new Runnable() {
            @Override
            public void run() {
                tables.get(1).getTable().post(new Runnable() {
                    @Override
                    public void run() {
                        String[] headers = {"实体", "迭代样本数"};
                        int[] weights = {2, 1};
                        tables.get(1).getTable().setHeaders(headers, weights);
                    }
                });
                tables.get(1).getTable().setOnHeaderClickListener(new CustomTableView.OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(int position) {
                        //TODO: 点击表头时候的响应（比如排序）
                    }
                });
                tables.get(1).getTable().setCreateItemFunctor(new CustomTableView.OnCreateItem() {
                    @Override
                    public ViewGroup createItem(int i, Object _item) {
                        IterateResults.IterateResult item = (IterateResults.IterateResult)_item;
                        LinearLayout container = new LinearLayout(getContext());
                        container.setOrientation(LinearLayout.HORIZONTAL);

                        TextView t0 = new TextView(getContext());
                        t0.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        t0.setTextColor(Color.parseColor("#000000"));
                        t0.setTextSize(15);
                        t0.setText(item.entity);

                        TextView t1 = new TextView(getContext());
                        t1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        t1.setTextColor(Color.parseColor("#000000"));
                        t1.setTextSize(15);
                        t1.setText(Integer.toString(item.sample_sum));

                        container.addView(t0);
                        container.addView(t1);
                        return container;
                    }
                });
            }
        });

        //------
        session = new ClientSession(getContext(), Global.serverHost);
        if(getContext() != null)
            Toast.makeText(getContext(), R.string.loading_please_wait, Toast.LENGTH_SHORT).show();
        /*
        if(Global.currentUser != null)
            session.login(Global.currentUser.username, Global.currentUser.password);
         */
    }

    public static class Entities {
        public static class Entity {
            String entity, entity_type;
            long mes_id;
        }
        List<Entity> data;
    }
    public static class IterateResults {
        public static class IterateResult {
            String entity;
            Integer sample_sum;
        }
        List<IterateResult> data;
    }

    private void getResults() {
        if(!need_get_results) return;

        Utils.asyncDo(getActivity(), null, new Utils.AsyncProc() {
            @Override
            public Object run() {
                if(!session.login(Global.currentUser.username, Global.currentUser.password)) return  null;
                return session.getIterateResults();
            }
        }, new Utils.AfterAsyncProc() {
            @Override
            public void run(Object result, boolean timeouted) {
                if(null == getContext())
                    return;
                if(timeouted) {
                    Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                    need_get_results = true;
                } else {
                    IterateResults rs = (IterateResults)result;
                    if(rs == null) {
                        Toast.makeText(getContext(), R.string.download_iterate_result_failed, Toast.LENGTH_SHORT).show();
                        need_get_results = true;
                    }
                    else {
                        if(tables.get(1).getTable() != null)
                        tables.get(1).getTable().setItems(rs.data);
                    }
                }
            }
        }, (int)(Global.timeoutLimit * 1.5));

        need_get_results = false;

    }


    /*
    private void doIterate() {

    }*/

    private void getEntities() {

        Utils.asyncDo(getActivity(), null, new Utils.AsyncProc() {
            @Override
            public Object run() {
                if(!session.login(Global.currentUser.username, Global.currentUser.password))
                    return null;
                return session.getIterateEntities(result_type, entity_type);
            }
        }, new Utils.AfterAsyncProc() {
            @Override
            public void run(Object result, boolean timeouted) {
                if(null == getContext())
                    return;
                if(timeouted) {
                    Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_LONG).show();
                } else {
                    Entities es = (Entities)result;
                    if(es == null) {
                        Toast.makeText(getContext(), R.string.download_iterate_entities_failed, Toast.LENGTH_SHORT).show();
                    } else {
                        if(tables.get(0).getTable() != null)
                            tables.get(0).getTable().setItems(es.data);
                    }
                    //
                }
            }
        }, (int)(3*Global.timeoutLimit));

    }

}
