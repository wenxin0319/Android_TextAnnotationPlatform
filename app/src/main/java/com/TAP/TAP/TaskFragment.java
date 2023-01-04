package com.TAP.TAP;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bin.david.form.data.table.PageTableData;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskFragment extends Fragment {

    private View task_view;
    public Tasks tasks;
    private ListView task_list_view;
    private ClientSession session;
    private Spinner task_set_spinner;
    private Button get_more_button, refresh_button;
    private boolean downloading_flag;  //正在下载某个样本的具体数据的标记，这个标记置位时排斥其它任务列表中选中的操作

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstance) {
        task_view = inflater.inflate(R.layout.task_layout, container, false);

        task_list_view = task_view.findViewById(R.id.task_list);
        //refreshTaskList();
        task_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int i, final long l) {

                if(downloading_flag)
                    return;

                Utils.asyncDo(getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        downloading_flag = true;
                        task_list_view.setEnabled(false);
                        TextView tv = view.findViewById(R.id.task_loading);
                        if(tv != null) {
                            tv.setText(R.string.loading);
                        }
                    }
                }, new Utils.AsyncProc() {
                    @Override
                    public Object run() {
                        Global.currentTask = session.selectTask(tasks.tasks_info.get(i));
                        return Global.currentTask != null;
                    }
                }, new Utils.AfterAsyncProc() {
                    @Override
                    public void run(Object result, boolean timeouted) {
                        if(null == getContext())
                            return;
                        if(timeouted) {
                            Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                        } else {
                            //Tasks.Task task = Global.currentTask;
                            Boolean ok = (Boolean)result;
                            if(!ok) {
                                Toast.makeText(getContext(), R.string.download_task_data_failed, Toast.LENGTH_SHORT).show();
                            } else {

                                //下载成功，将数据导入到UI
                                ((MainActivity)getActivity()).switchTab(1);
                                LabelFragment lf = (LabelFragment)((MainActivity)getActivity()).ui_fragments.get(1);
                                Global.currentTaskSplit = Utils.splitStringByComma(Global.currentTask.content);
                                lf.clearAdapterCache(Global.currentTaskSplit.length);

                                //-------------------------------//<- Suggest: 可以改成异步，目前感觉没有必要
                                //translate labeled
                                Global.currentLabels = new Labels();
                                for(Map.Entry<String, Tasks.Task.LabelEntity[]> entry : Global.currentTask.entity_labels.entrySet()) {
                                    Tasks.Task.LabelEntity[] labels = entry.getValue();
                                    for(int j = 0; j < labels.length; j++) {
                                        Labels.Label lb = new Labels.Label();
                                        lb.fore_color = labels[j].txt_color;
                                        lb.back_color = labels[j].bg_color;
                                        lb.name = labels[j].label_name;
                                        Global.currentLabels.labels.add(lb);
                                    }
                                }
                                for(int j = Global.currentTask.txt_label_result.length - 1; j >= 0; j--) {
                                    Tasks.Task.LabelResult br = Global.currentTask.txt_label_result[j];
                                    int adapter_id = -1;
                                    for(int s = 0; s < Global.currentTaskSplit.length; s++) {
                                        if(Global.currentTaskSplit[s] >= br.start_pos) {
                                            if(Global.currentTaskSplit[s] == br.start_pos)
                                                adapter_id = s;
                                            else
                                                adapter_id = s-1;
                                            break;
                                        }
                                    }

                                    if(adapter_id < 0 || adapter_id >= Global.currentTaskSplit.length)
                                        continue;
                                    Labels.LabeledItem labled = new Labels.LabeledItem();
                                    labled.sentence_id = adapter_id;
                                    labled.start_position = br.start_pos - Global.currentTaskSplit[adapter_id];
                                    labled.end_position = br.end_pos - Global.currentTaskSplit[adapter_id];

                                    labled.text = Global.getCurrentSentence(adapter_id).substring(labled.start_position, labled.end_position);

                                    int _t = Global.currentLabels.labels.size();
                                    for(int s = 0; s < _t; s++) {
                                        Labels.Label lb = Global.currentLabels.labels.get(s);
                                        if(lb.name.equals(br.entity_type)) {
                                            labled.label = lb;
                                            break;
                                        }
                                    }

                                    if(labled.label == null) continue;
                                    lf.setAdapter(adapter_id, 0, labled);
                                }
                                lf.refresh();
                            }
                        }
                        TextView tv = view.findViewById(R.id.task_loading);
                        if(tv != null) {
                            tv.setText("");
                        }
                        downloading_flag = false;
                        task_list_view.setEnabled(true);
                    }
                }, Global.timeoutLimit);

            }
        });

        task_set_spinner = task_view.findViewById(R.id.task_set_spinner);

        get_more_button = ((Button)task_view.findViewById(R.id.task_get_more_button));
        get_more_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Global.currentTaskSetId != null) {
                    Utils.asyncDo(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            preRefresh(true);
                        }
                    }, new Utils.AsyncProc() {
                        @Override
                        public Object run() {
                            session.getMoreSamples(Global.currentTaskSetId);
                           // session.getMoreSamples(Global.currentTaskSetId);
                            return 0;
                        }
                    }, new Utils.AfterAsyncProc() {
                        @Override
                        public void run(Object result, boolean timeouted) {
                            if(null == getContext())
                                return;
                            if(timeouted) {
                                Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                            } else {
                                refreshTaskList();
                                Toast.makeText(getContext(), R.string.get_more_ok, Toast.LENGTH_SHORT).show();
                            }
                            postRefresh();
                        }
                    }, Global.timeoutLimit);
                }
            }
        });

        //refresh_button = ((AppCompatActivity)getActivity()).getSupportActionBar().getCustomView().findViewById(R.id.actionbar_refresh_button);
        refresh_button = ((MainActivity)getActivity()).getActionBarView(0).findViewById(R.id.actionbar_refresh_button);

        session = new ClientSession(getContext(), Global.serverHost);
        downloading_flag = false;
        doRefresh();
        return task_view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void preRefresh(boolean change_get_more) {
        task_list_view.setEnabled(false);
        if(refresh_button != null) {
            //if(change_get_more)
            refresh_button.setText(R.string.refreshing);
            refresh_button.setClickable(false);
            refresh_button.setEnabled(false);
        }
        if(change_get_more)
            get_more_button.setText(R.string.getting_more);
        get_more_button.setClickable(false);
        get_more_button.setEnabled(false);
    }

    private void postRefresh() {
        task_list_view.setEnabled(true);
        if(refresh_button != null) {
            refresh_button.setText(R.string.refresh);
            refresh_button.setClickable(true);
            refresh_button.setEnabled(true);
        }
        get_more_button.setText(R.string.get_more);
        get_more_button.setClickable(true);
        get_more_button.setEnabled(true);
    }

    public void refreshTaskList() {
        if(tasks == null)
            return;

        Utils.asyncDo(getActivity(), new Runnable() {
            @Override
            public void run() {
                preRefresh(false);
            }
        }, new Utils.AsyncProc() {
            @Override
            public Object run() {
                return session.getTasks(1, Global.currentTaskSetId);
            }
        }, new Utils.AfterAsyncProc() {
            @Override
            public void run(Object result, boolean timeouted) {
                if(null == getContext())
                    return;

                if(timeouted) {
                    Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                } else {
                    List<Tasks.TaskJsonData.TaskJsonItem> tasks_info = (List<Tasks.TaskJsonData.TaskJsonItem>)result;
                    if(tasks_info == null) {

                    } else {
                        tasks.tasks_info = tasks_info;
                        Tasks.MenuItemAdapter adapter = new Tasks.MenuItemAdapter(getContext(), tasks.tasks_info);
                        adapter.notifyDataSetChanged();
                        task_list_view.setAdapter(adapter);
                    }
                }
                postRefresh();
            }
        }, Global.timeoutLimit);
    }

    // 响应action bar中的刷新按钮
    public void doRefresh() {

        //这里刷新任务集
        if(Global.currentUser == null)
            return;

        if(tasks == null)
            tasks = new Tasks();
        if(tasks.task_set == null)
            tasks.task_set = new ArrayList<>();

        tasks.task_set.clear();

        class PassedData {
            ArrayList<String> task_ids;
            ArrayList<String> task_des_list;
            Tasks.TaskSetJsonData task_set_data;
            int current_index;
        }

        Utils.asyncDo(getActivity(), new Runnable() {
            @Override
            public void run() {
                preRefresh(false);;
            }
        }, new Utils.AsyncProc() {
            @Override
            public Object run() {
                session.login(Global.currentUser.username, Global.currentUser.password);
                Tasks.TaskSetJsonData tset = session.getTaskSet(1);
                if(tset == null)
                    return null;

                ArrayList<String> task_ids = new ArrayList<>();      //任务id表
                ArrayList<String> task_des_list = new ArrayList<>(); //仅用于显示
                int current = 0;
                for (int i = 0; i < tset.data.length; i++) {
                    tasks.task_set.add(tset.data[i]);
                    task_ids.add(tset.data[i].task_id);
                    String temp_des = null;
                    if(tset.data[i].task_des.length() > 7) { //超过长度，截断
                        temp_des = tset.data[i].task_des.substring(0, 7) + "...";
                    } else {
                        temp_des = tset.data[i].task_des;
                    }
                    task_des_list.add(tset.data[i].task_id + ":" +  temp_des);
                    if (tset.data[i].task_id.equals(tset.current_task)) {
                        Global.currentTaskSetId = tset.data[i].task_id;
                        current = i;
                    }
                }

                PassedData data = new PassedData();
                data.task_ids = task_ids;
                data.task_des_list = task_des_list;
                data.task_set_data = tset;
                data.current_index = current;
                return data;
            }
        }, new Utils.AfterAsyncProc() {
            @Override
            public void run(Object result, boolean timeouted) {
                if(getContext() == null)
                    return;
                if(timeouted) {
                    Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                } else {
                    final PassedData data = (PassedData) result;

                    if(data == null) {
                        postRefresh();
                        return;
                    }

                    task_set_spinner.setAdapter(new ArrayAdapter<String>(getContext(),
                            R.layout.support_simple_spinner_dropdown_item, data.task_des_list));
                    task_set_spinner.setSelection(data.current_index);

                    task_set_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (!Global.currentTaskSetId.equals(data.task_set_data.data[i].task_id)) {

                                Global.currentTaskSetId = data.task_set_data.data[i].task_id;
                                refreshTaskList();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                    refreshTaskList();
                }
                postRefresh();
            }
        }, Global.timeoutLimit);

    }
}
