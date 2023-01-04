package com.TAP.TAP;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.google.gson.*;

import org.w3c.dom.Text;

import java.util.Hashtable;
import java.util.Map;

import static com.google.android.material.R.drawable.avd_show_password;

public class Tasks {
    static class TaskJsonData {
        static class TaskJsonItem {
            String annotated, ds_id, sam_id, sample_des, sample_state;
            double score;
        }
        TaskJsonItem[] data;
    }

    static class TaskSetJsonData {
        String current_task;
        static class TaskSetItem {
            String task_id, task_des;
        }
        TaskSetItem[] data;
    }

    static class Task {

        @SerializedName("txt")
        String content;

        String sam_id;

        static class LabelResult {
            String entity_mention;
            int start_pos, end_pos;
            String entity_type;
            String result_type;
            String result_state;
            String bg_color, txt_color;
        }

        static class LabelEntity {
            @SerializedName("labelName")
            String label_name;

            String bg_color, txt_color;

            @SerializedName("TAG_GROUP")
            String tag_group;

            @SerializedName("TAG_NOTE")
            String tag_note;
        }

        Map<String, LabelEntity[]> entity_labels;

        @SerializedName("entity_results")
        LabelResult[] txt_label_result;
    }

    //标注后上传给服务器的JSON格式
    static class LabeledUploadJsonData {
        String sam_id;

        static class LabeledUploadItem {
            @SerializedName("entity_mention")
            String text;

            int start_pos, end_pos;

            String entity_type;
            int result_state;
            int result_type;

            String result_state_note;
            String result_type_note;

            @SerializedName("bg_color")
            String back_color;

            @SerializedName("txt_color")
            String fore_color;
        }

        List<LabeledUploadItem> entity_result;
        int mark_time;

        public LabeledUploadJsonData() {
            entity_result = new ArrayList<>();
        }
    }


    List<TaskSetJsonData.TaskSetItem> task_set;
    List<Task> tasks;
    List<TaskJsonData.TaskJsonItem> tasks_info;

    Tasks() {
        tasks = new ArrayList<>();
        tasks_info = new ArrayList<>();
    }

    public static class MenuItemAdapter extends ArrayAdapter<TaskJsonData.TaskJsonItem> {
        MenuItemAdapter(Context context, List<TaskJsonData.TaskJsonItem> objects) {
            super(context, R.layout.task_taskitem_layout, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TaskJsonData.TaskJsonItem item = getItem(position);
            View view;
            view = LayoutInflater.from(getContext()).inflate(R.layout.task_taskitem_layout, null);

            ImageView iconv = view.findViewById(R.id.task_icon);
            iconv.setImageResource(avd_show_password);

            TextView tv = view.findViewById(R.id.task_title);
            tv.setText(item.sam_id);
            tv = view.findViewById(R.id.task_preview);
            tv.setText(item.sample_des);
            if(item.annotated.charAt(0) == '0' && item.sample_state.charAt(0) == '2') {
                tv = view.findViewById(R.id.task_new);
                tv.setText("   New");
            }

            return view;
        }
    }

    /*
    public static class TaskSetAdapter extends ArrayAdapter<TaskSetJsonData.TaskSetItem> {
        TaskSetAdapter(Context context, List<TaskSetJsonData.TaskSetItem> objects) {
            super(context, R.layout.task_taskset_spinner_item_layout, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TaskSetJsonData.TaskSetItem item = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.task_taskset_spinner_item_layout, null);

            ((TextView)view).setText(item.task_id.toString());
            return view;
        }
    }
    */
}
