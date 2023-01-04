package com.TAP.TAP;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TaskManageFragment extends ManageCustomFragment {
    TaskManageFragment(Context context) {
        super(context);
    }

    private Button refresh_button;
    private ClientSession session;
    private ListView task_info_list;

    static class TaskInfoSet {
        static class TaskInfo {
            String DS_LIST;
            String TASK_DEADLINE;
            String TASK_DES;
            double TASK_FINISH_RATE;
            String TASK_ID, TASK_TYPE;
            String TASK_WORKERS;
        }
        ArrayList<TaskInfo> data;
    }

    private static class TaskInfoAdapter extends ArrayAdapter<TaskInfoSet.TaskInfo> {
        TaskInfoAdapter(Context context, List<TaskInfoSet.TaskInfo> ojbects) {
            super(context, R.layout.manage_taskmanage_item_layout, ojbects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.manage_taskmanage_item_layout, null);
            TaskInfoSet.TaskInfo item = getItem(position);
            ((TextView)v.findViewById(R.id.task_manage_task_id)).setText(item.TASK_ID);
            ((TextView)v.findViewById(R.id.task_manage_task_desc)).setText(item.TASK_DES);
            ((TextView)v.findViewById(R.id.task_manage_task_ddl)).setText(item.TASK_DEADLINE);
            ((TextView)v.findViewById(R.id.task_manage_task_files)).setText(item.DS_LIST);
            String rt = Double.toString(item.TASK_FINISH_RATE * 100);
            if(rt.length() > 5)rt = rt.substring(0, 5);
            ((TextView)v.findViewById(R.id.task_manage_task_rate)).setText( rt+ "%");
            ((TextView)v.findViewById(R.id.task_manage_task_worker)).setText(item.TASK_WORKERS);
            return v;
        }
    }

    void postCreateView(View view) {
        refresh_button = view.findViewById(R.id.task_manage_refresh_button);
        if(refresh_button != null) {
            refresh_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doRefresh();
                }
            });
        }
        task_info_list = view.findViewById(R.id.task_manage_info_list);
        session = new ClientSession(getActivity(), Global.serverHost);
        doRefresh();
    }
    private void doRefresh() {
        Utils.asyncDo(getActivity(), new Runnable() {
            @Override
            public void run() {
                refresh_button.setText(R.string.refreshing);
                refresh_button.setClickable(false);
                refresh_button.setEnabled(false);
            }
        }, new Utils.AsyncProc() {
            @Override
            public Object run() {
                if(!session.login(Global.currentUser.username, Global.currentUser.password)) return  null;
                return session.getTasksInfo();
            }
        }, new Utils.AfterAsyncProc() {
            @Override
            public void run(Object result, boolean timeouted) {
                if(null == getContext())
                    return;
                if(timeouted) {
                    Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                } else {
                    TaskInfoSet ts = (TaskInfoSet)result;
                    if(ts == null) {
                        Toast.makeText(getContext(), R.string.download_task_info_failed, Toast.LENGTH_SHORT).show();
                    }else {
                        TaskInfoAdapter adapter = new TaskInfoAdapter(getContext(), ts.data);
                        task_info_list.setAdapter(adapter);
                    }
                }
                refresh_button.setText(R.string.refresh);
                refresh_button.setClickable(true);
                refresh_button.setEnabled(true);
            }
        }, (int)(Global.timeoutLimit * 1.5));
    }
}
