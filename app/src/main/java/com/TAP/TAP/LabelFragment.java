package com.TAP.TAP;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.jruby.Main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LabelFragment extends Fragment {
    private LabelCustomTextView text_view;
    private View label_view;
    private RadioGroup label_group;
    private int split_pos;
    private ListView labeled_list;
    private Labels.LabeledItemListItemAdapter cache_labeled_adapters[];
    private ClientSession session;
    private Button upload_button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstance) {
        label_view = inflater.inflate(R.layout.label_layout, container, false);
        init();
        refresh();

        split_pos = -1;

        return label_view;
    }

    @Override
    public void onResume() {
        super.onResume();
        switchToSentence(split_pos);
    }

    private void init() {
        text_view = label_view.findViewById(R.id.label_content_full);
        //text_view.setMovementMethod(ScrollingMovementMethod.getInstance());
        text_view.setOnTextSelectedListener(new LabelCustomTextView.OnTextSelectedListener() {
            @Override
            public void onTextSelected(int start, int end, String text) {
                //Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                Labels.LabeledItem item = new Labels.LabeledItem();
                item.sentence_id = split_pos;
                item.start_position = start;
                item.end_position = end;
                item.text = text;
                item.label = Global.currentLabel;
                setAdapter(split_pos, 0, item);
            }
        });

        //text_view.setMovementMethod(ScrollingMovementMethod.getInstance());
        /*
        ((Button)label_view.findViewById(R.id.label_confirm_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(text_view.hasSelection()) {
                    int s = text_view.getSelectionStart();
                    int t = text_view.getSelectionEnd();
                    String text = text_view.getText().subSequence(s, t).toString();
                    Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
                }
            }
        });
        */
        label_group = label_view.findViewById(R.id.label_group);


        ((Button)label_view.findViewById(R.id.label_prev_sentence_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToSentence(split_pos-1);
            }
        });

        ((Button)label_view.findViewById(R.id.label_next_sentence_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToSentence(split_pos+1);
            }
        });

        labeled_list = (label_view.findViewById(R.id.label_action_list));
        //upload_button = ((AppCompatActivity)getActivity()).getSupportActionBar().getCustomView().findViewById(R.id.label_save_button);
        upload_button = ((MainActivity)getActivity()).getActionBarView(1).findViewById(R.id.label_save_button);

        session = new ClientSession(getContext(), Global.serverHost);

    }

    private void redrawLabeled(Labels.LabeledItemListItemAdapter adapter) {
        int count = adapter.getCount();
        for(int i = count-1; i >= 0; i--) {
            Labels.LabeledItem item = adapter.getItem(i);
            text_view.labelSpan(item.start_position, item.end_position,
                    Color.parseColor(item.label.fore_color),
                    Color.parseColor(item.label.back_color));
        }
    }

    private void switchToSentence(int pos) {
        if(null == Global.currentTaskSplit) return;

        if(pos < 0)pos = 0;
        if(pos >= Global.currentTaskSplit.length) pos = Global.currentTaskSplit.length - 1;
        if(split_pos != pos) {
            split_pos = pos;
            if (Global.currentTask != null) {
                text_view.setText(Global.getCurrentSentence(pos));
                setAdapter(pos, -1, null);
                Labels.LabeledItemListItemAdapter adapter = getAdapter(pos);
                redrawLabeled(adapter);
            } else {
                text_view.setText("");
            }
        }
    }

    public void clearAdapterCache(int size) {
        cache_labeled_adapters = new Labels.LabeledItemListItemAdapter[size];
    }

    private Labels.LabeledItemListItemAdapter getAdapter(int i) {
        if(cache_labeled_adapters[i] == null) {
            List<Labels.LabeledItem> l = new LinkedList<>();
            cache_labeled_adapters[i] = new Labels.LabeledItemListItemAdapter(getContext(), l, text_view);
            cache_labeled_adapters[i].setItemRemoveExecuter(new Labels.LabeledItemListItemAdapter.ItemRemoveExecuter() {
                @Override
                public void remove(int position) {
                    removeLabeled(position);
                }
            });
        } return cache_labeled_adapters[i];
    }

    // insert_pos >= 0，如果item不为null，插入元素item
    //                  如果item为null，删除insert_pos位置上的元素
    // insert_pos < 0 仅设置adapter，不改变元素
    @Nullable
    public void setAdapter(int adapter_i, int insert_pos, @Nullable Labels.LabeledItem item) {
        Labels.LabeledItemListItemAdapter adapter = getAdapter(adapter_i);
        if(insert_pos >= 0)
            adapter.insert(item, insert_pos);
        labeled_list.setAdapter(adapter);
    }

    public void refresh() {
        if(Global.currentTask == null || Global.currentTaskSplit == null || Global.currentLabels == null)
            return;

        split_pos = -1;
        switchToSentence(0);

        int offset = 1;
        LinearLayout current_ll = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.label_label_ll_layout, null);
        label_group.removeAllViews();
        for(final Labels.Label label : Global.currentLabels.labels) {

            Button btn = (Button)LayoutInflater.from(getContext()).inflate(R.layout.label_label_button_layout, null);
            btn.setText(label.name);
            btn.setBackgroundColor(Color.parseColor(label.back_color));
            btn.setTextColor(Color.parseColor(label.fore_color));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Global.currentLabel = label;
                    //text_view.setHighlightColor(Color.parseColor(label.back_color));
                    text_view.setLabelStyle(Color.parseColor(label.fore_color), Color.parseColor(label.back_color));
                }
            });

            current_ll.addView(btn);
            if(offset % 4 == 0) {
                label_group.addView(current_ll);
                current_ll = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.label_label_ll_layout, null);
            }
            offset++;
        }
        if((offset-1) % 4 != 0)
            label_group.addView(current_ll);

        //set current label
        if(Global.currentLabels.labels.size() == 0)
            Global.currentLabel = new Labels.Label();
        else
            Global.currentLabel = Global.currentLabels.labels.get(0);
        text_view.setLabelStyle(Color.parseColor(Global.currentLabel.fore_color),
                                Color.parseColor(Global.currentLabel.back_color));
    }

    public void setTextSize(int size) {
        text_view.setTextSize(size);
    }

    public void doRevert() {
        removeLabeled(0);
    }

    public void removeLabeled(int position) {
        if(Global.currentTaskSplit == null) return;

        Labels.LabeledItemListItemAdapter adapter = getAdapter(split_pos);
        if(adapter.getCount() < 1) return;
        if(position < 0 || position >= adapter.getCount()) return;
        Labels.LabeledItem item = adapter.getItem(position);
        text_view.clearSpan(item.start_position, item.end_position);
        adapter.remove(item);
        redrawLabeled(adapter);
    }

    public void doSave() {
        if(Global.currentTask == null || Global.currentTaskSplit == null) return;
        if(cache_labeled_adapters == null)return;

        final Tasks.LabeledUploadJsonData upload = new Tasks.LabeledUploadJsonData();
        upload.sam_id = Global.currentTask.sam_id;

        //TODO: What is mark time?
        upload.mark_time = 20;   //Set a mark time?


        for(int i = 0; i < cache_labeled_adapters.length; i++) {
            Labels.LabeledItemListItemAdapter adapter = getAdapter(i);
            int t = adapter.getCount();
            for(int j = 0; j < t; j++) {
                Labels.LabeledItem item = adapter.getItem(j);
                if(item == null)
                    continue;
                Tasks.LabeledUploadJsonData.LabeledUploadItem up = new Tasks.LabeledUploadJsonData.LabeledUploadItem();
                up.back_color = item.label.back_color;
                up.fore_color = item.label.fore_color;
                up.text = item.text;
                up.start_pos = item.start_position + Global.currentTaskSplit[i];
                up.end_pos = item.end_position + Global.currentTaskSplit[i];
                up.entity_type = item.label.name;
                up.result_state_note = Global.entityResultStateNote;
                up.result_type_note = Global.entityResultTypeNote;

//                if(Global.currentTask.txt_label_result[j].result_state.equals("3"))
                up.result_state = 2;
                up.result_type = 1;

                upload.entity_result.add(up);
            }
        }


        Utils.asyncDo(getActivity(), new Runnable() {
            @Override
            public void run() {
                if(upload_button == null) return;
                upload_button.setText(R.string.uploading);
                upload_button.setEnabled(false);
                upload_button.setClickable(false);
            }
        }, new Utils.AsyncProc() {
            @Override
            public Object run() {
                if (!session.login(Global.currentUser.username, Global.currentUser.password))
                    return false;
                if (!session.uploadLabeledResult(upload))
                    return false;
                return true;
            }
        }, new Utils.AfterAsyncProc() {
            @Override
            public void run(Object result, boolean timeouted) {
                if(null == getContext())
                    return;
                if(timeouted) {
                    Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                } else {
                    Boolean ok = (Boolean)result;
                    if(ok) {
                        Toast.makeText(getContext(), R.string.upload_ok, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
                    }
                }
                if(upload_button != null) {
                    upload_button.setText(R.string.save);
                    upload_button.setEnabled(true);
                    upload_button.setClickable(true);
                }
            }
        }, Global.timeoutLimit);
    }
}
