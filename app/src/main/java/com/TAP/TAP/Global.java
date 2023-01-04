package com.TAP.TAP;

import java.util.ArrayList;

public class Global {
    // general
    public static int timeoutLimit = 8000;  //8秒

    // constants
    public static String serverHost = "www.tcmai.org";
    public static String labelForeColors[] = {"#00ffff", "#f02020", "#ff00ff", "#2f2ff0", "#f02faf", "#ff3f5f",
                                              "#ff000f", "#2f3f7f", "#5f6f7f", "#3f6f12", "#0000ff", "#5f9fff"};
    public static String labelBackColors[] = {"#ff0000", "#ffffff", "#345678", "#ffffff", "#0000ff", "#00ffff",
                                              "#00ff0f", "#ffffff", "#12ff0f", "#ffffff", "#ffffff", "#ffffff"};

    // variables
    public static Tasks.Task currentTask;
    public static String currentTaskSetId;
    public static int[] currentTaskSplit;
    public static Labels currentLabels;
    public static Labels.Label currentLabel;
    public static User currentUser;

    public static int currentManage;  //当前管理的内容
    public static final int MANAGE_NONE = 0;
    public static final int MANAGE_SMARTLABEL = 1;
    public static final int MANAGE_ITERATE = 2;
    public static final int MANAGE_TASKMANAGE = 3;

    public static String entityResultStateNote = "任务标注-待审核";
    public static String entityResultTypeNote = "任务标注";


    public static String getCurrentSentence(int i) {
        if(Global.currentTaskSplit == null || Global.currentTask == null)
            return "";
        if(i == Global.currentTaskSplit.length - 1) {
            return Global.currentTask.content.substring(Global.currentTaskSplit[i]);
        }else {
            return Global.currentTask.content.substring(Global.currentTaskSplit[i], Global.currentTaskSplit[i+1]);
        }
    }

    public static void clear() {
        currentLabel = null;
        currentLabels = null;
        currentTask = null;
        currentTaskSplit = null;
        currentUser = null;
        currentManage = MANAGE_NONE;
    }

    public static String mysqlServerAddr;

    public static String documentLabels[] = {"手术", "阳性症状", "疾病和诊断", "影像检查", "药物", "解剖部位", "实验室检验", "实体头", "实体尾", "实体连接"};
    public static String documentLabelColor[] = {"#AA1122", "#BB6666", "#1234AA", "#778811", "#11BA41", "#761088", "#885511", "#121522", "#891245", "#AABB33"};
}

