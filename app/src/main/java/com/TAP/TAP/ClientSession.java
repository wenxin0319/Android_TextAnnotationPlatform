package com.TAP.TAP;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import org.jruby.runtime.builtin.IRubyObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSession {
    private Context activity_context;
    private String host;
    private RubyEngine rubyEngine;
    Gson gson;

    ClientSession(Context _activity_context, String host) {
        activity_context = _activity_context;
        rubyEngine = new RubyEngine();

        this.host = host;
        this.gson = new Gson();
        rubyEngine.setGlobalVariable("$host", host);
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "basicdef.rb"));

        //wrapper，仅仅是做一个包装为了跟json格式保持一致
        task_info_wrapper = new TaskInfoWrapper();
        doIterateDataWrapper = new DoIterateDataWrapper();
    }

    public ClientSession fork() {
        ClientSession new_session = new ClientSession(activity_context, host);
        new_session.getRuby().setScope(rubyEngine.getScope());
        return new_session;
    }

    public RubyEngine getRuby() {
        return rubyEngine;
    }

    public boolean login(String username, String password) {
        rubyEngine.setGlobalVariable("$username", username);
        rubyEngine.setGlobalVariable("$password", password);
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "login.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return false;
        } else {
            return true;
        }
    }

    public Tasks.TaskSetJsonData getTaskSet(int page) {
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "gettaskset.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return null;
        } else {
            String json_data = rubyEngine.getLastReturn().asString().toString();
            //Log.println(Log.DEBUG, "TaskSetJson", json_data);

            Tasks.TaskSetJsonData data = gson.fromJson(json_data, Tasks.TaskSetJsonData.class);
            return data;
        }
    }

    public boolean getMoreSamples(String taskSetId) {
        rubyEngine.setGlobalVariable("$taskset", taskSetId);
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "getnewsample.rb"));
        return rubyEngine.getLastError() == null;
    }

    public List<Tasks.TaskJsonData.TaskJsonItem> getTasks(int page, String taskSetId) {
        rubyEngine.setGlobalVariable("$page", page);
        rubyEngine.setGlobalVariable("$taskset", taskSetId);
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "gettasks.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return null;
        }
        String json_data = rubyEngine.getLastReturn().asString().toString();

        Tasks.TaskJsonData data = gson.fromJson(json_data, Tasks.TaskJsonData.class);
        List<Tasks.TaskJsonData.TaskJsonItem> container = new ArrayList<>();
        for(int i = 0; i < data.data.length; i++) {
            container.add(data.data[i]);
        }return container;
    }

    public boolean uploadLabeledResult(Tasks.LabeledUploadJsonData data) {
        String upload_json = gson.toJson(data, Tasks.LabeledUploadJsonData.class);
        rubyEngine.setGlobalVariable("$upload_json", upload_json);
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "upload_labeled_result.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return false;
        }else return true;
    }

    public IterateFragment.Entities getIterateEntities(int result_type, String entity_type) {
        rubyEngine.setGlobalVariable("$iterate_result_type", result_type);
        rubyEngine.setGlobalVariable("$iterate_entity_type", entity_type);
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "iterate_select.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return null;
        } else {
            String json_data = rubyEngine.getLastReturn().asString().toString();
            IterateFragment.Entities res = gson.fromJson(json_data, IterateFragment.Entities.class);
            return res;
        }
    }

    public IterateFragment.IterateResults getIterateResults() {
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "iterate_get_results.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return null;
        }
        String json_data = rubyEngine.getLastReturn().asString().toString();
        IterateFragment.IterateResults res = gson.fromJson(json_data, IterateFragment.IterateResults.class);
        return res;
    }

    private class DoIterateDataWrapper {
        List<String> entities, entity_type;
    }private DoIterateDataWrapper doIterateDataWrapper;

    public double doIterate(List<String> entities, List<String> entity_type) {
        double iterate_process = 0.0;
        Thread w = new Thread(new Runnable() {
            @Override
            public void run() {
                rubyEngine.runScript(Utils.getAssetIS(activity_context, "doiterate_queryprocess.rb"));
            }
        }); w.start();
        try {
            w.join();
        }catch (InterruptedException e) {
            return 0;
        }
        if(rubyEngine.getLastError() != null) {
            showError();
            return 0;
        } else {
            String ret = rubyEngine.getLastReturn().asString().toString();
            if(ret.equals(""))
                iterate_process = 0.0;
            else
                iterate_process = Double.parseDouble(ret);
        }

        //正在进行迭代，直接返回
        if(!(Math.abs(iterate_process-100.0) < 1e-6
                || Math.abs(iterate_process) < 1e-6)) {
            return iterate_process; //TODO: to be verified
        }

        doIterateDataWrapper.entities = entities;
        doIterateDataWrapper.entity_type = entity_type;
        String json_data = gson.toJson(doIterateDataWrapper, DoIterateDataWrapper.class);
        rubyEngine.setGlobalVariable("$iterate_data", json_data);

        w = new Thread(new Runnable() {
            @Override
            public void run() {
                rubyEngine.runScript(Utils.getAssetIS(activity_context, "doiterate.rb"));
            }
        }); w.start();
        try {
            w.join();
        }catch (InterruptedException e) {
            return 0;
        }
        if(rubyEngine.getLastError() != null) {
            showError();
            return 0;
        }else{
            return iterate_process;
        }
    }

    private class TaskInfoWrapper {
        Tasks.TaskJsonData.TaskJsonItem data;
    }
    static private class ExclusionNevalAndEvent implements ExclusionStrategy {
        static String[] acceptFields = {"txt", "sam_id", "entity_labels", "entity_results", "entity_mention",
                                        "start_pos", "end_pos", "entity_type", "result_state", "result_type", "bg_color", "txt_color",
                                        "labelName", "TAG_GROUP", "TAG_NOTE"};
        @Override
        public boolean shouldSkipField(FieldAttributes attr) {
            String name = attr.getName();
            /*
            for(int i = 0; i < acceptFields.length; i++) {
                if(name.compareTo(acceptFields[i]) == 0) {
                    return false;
                }
            }
            return true;*/
            return name.startsWith("event") || name.startsWith("neval");
        }

        @Override
        public boolean shouldSkipClass(Class<?> klass) {
            return false;
        }
    }
    private TaskInfoWrapper task_info_wrapper;
    public Tasks.Task selectTask(Tasks.TaskJsonData.TaskJsonItem task_info) {
        task_info_wrapper.data = task_info;
        String task_info_json = gson.toJson(task_info_wrapper);
        rubyEngine.setGlobalVariable("$selected_task_json", task_info_json);
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "gettask.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return null;
        }
        Gson _gson = new GsonBuilder().setExclusionStrategies(new ExclusionNevalAndEvent()).create();
        String json_data = rubyEngine.getLastReturn().asString().toString();
        Tasks.Task tk = _gson.fromJson(json_data, Tasks.Task.class);
        return tk;
    }

    public TaskManageFragment.TaskInfoSet getTasksInfo() {
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "gettaskinfo.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
            return null;
        }

        String js = rubyEngine.getLastReturn().asJavaString();
        TaskManageFragment.TaskInfoSet ts = gson.fromJson(js, TaskManageFragment.TaskInfoSet.class);
        return ts;
    }

    public void runTaskScript() {
        rubyEngine.runScript(Utils.getAssetIS(activity_context, "just_task.rb"));
        if(rubyEngine.getLastError() != null) {
            showError();
        }
    }

    private void showError() {
        final String s = new String(rubyEngine.getLastError().toCharArray()); //copy it!
        ((Activity)activity_context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity_context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
