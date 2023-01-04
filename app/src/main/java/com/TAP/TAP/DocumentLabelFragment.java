package com.TAP.TAP;

import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.TAP.TAP.Labels.LabeledItem;

import java.util.ArrayList;
import java.util.List;

public class DocumentLabelFragment extends Fragment {
    View view;
    TextView ui_tv_title;
    LabelCustomTextView ui_tv_abstract;
    RadioGroup ui_label_group;
    List<LabeledItem>  labeledItems;
    ListView ui_action_list;
    Labels.Label currentLabel;
    Button ui_upload_button;
    PubmedClient.PubmedAbstract pubmedAbstract;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstance) {
        view = inflater.inflate(R.layout.document_label_fragment_layout, container, false);
        ui_tv_title = view.findViewById(R.id.dlabel_title_text);
        ui_tv_abstract = view.findViewById(R.id.dlabel_text);
        ui_label_group = view.findViewById(R.id.document_label_group);
        ui_action_list = view.findViewById(R.id.document_label_action_list);
        ui_tv_abstract.setMovementMethod(ScrollingMovementMethod.getInstance());
        ui_upload_button = view.findViewById(R.id.dblabel_upload);

        ui_upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pubmedAbstract == null || ui_action_list.getAdapter() == null) {
                    Toast.makeText(getContext(), "没有可上传的内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                ui_upload_button.setClickable(false);
                ui_upload_button.setEnabled(false);
                ui_upload_button.setText(R.string.uploading);
                Utils.asyncDo(getActivity(), null, new Utils.AsyncProc() {
                    @Override
                    public Object run() {
                        List<LabeledItem> labeled = new ArrayList<>();
                        for(int i = 0 ; i< ui_action_list.getAdapter().getCount(); i++) {
                            labeled.add((LabeledItem) ui_action_list.getAdapter().getItem(i));
                        }
                        return PubmedClient.uploadLabeledData(pubmedAbstract, labeled, new Utils.OnHttpsRequestError() {
                            @Override
                            public void onError(final String message) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "上传失败:" + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }, new Utils.AfterAsyncProc() {
                    @Override
                    public void run(Object result, boolean timeouted) {
                        ui_upload_button.setClickable(true);
                        ui_upload_button.setEnabled(true);
                        ui_upload_button.setText("上传");
                        if(null == getContext())
                            return;
                        if(timeouted) {
                            Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                        } else {
                            if(((Boolean)result) == false) {
                                Toast.makeText(getContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
                            }else{
                            Toast.makeText(getContext(), R.string.upload_ok, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, Global.timeoutLimit * 3);
            }
        });

        return view;
    }

    public void setupLabel(PubmedClient.PubmedAbstract pabs) {
        pubmedAbstract = pabs;

        ui_tv_title.setText(pabs.title);
        ui_tv_abstract.setText(pabs.abstractText);
        ui_tv_abstract.scrollTo(0, 0);
        ui_tv_abstract.setEnabled(false);
        ui_action_list.setAdapter(null);

        int offset = 1;
        LinearLayout current_ll = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.label_label_ll_layout, null);
        int rowCount = 0;
        ui_label_group.removeAllViews();
        for(int i = 0; i < Global.documentLabels.length; i++ ) {
            Button btn = (Button)LayoutInflater.from(getContext()).inflate(R.layout.label_label_button_layout, null);
            btn.setText(Global.documentLabels[i]);
            btn.setBackgroundColor(Color.parseColor(Global.documentLabelColor[i]));
            btn.setTextColor(Color.parseColor("#FFFFFF"));
            final int j = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentLabel = new Labels.Label();
                    currentLabel.name = Global.documentLabels[j];
                    currentLabel.back_color = Global.documentLabelColor[j];
                    currentLabel.fore_color = "#FFFFFF";
                    ui_tv_abstract.setLabelStyle(Color.parseColor("#FFFFFF"), Color.parseColor(Global.documentLabelColor[j]));
                }
            });

            current_ll.addView(btn);
            if(offset % 4 == 0) {
                ui_label_group.addView(current_ll);
                rowCount++;
                current_ll = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.label_label_ll_layout, null);
            }
            offset++;
        }
        if((offset-1) % 4 != 0) {
            ui_label_group.addView(current_ll);
            rowCount++;
        }
        //ui_label_group.setMinimumHeight(current_ll.getChildAt(0).getHeight() * rowCount);

        labeledItems = new ArrayList<>();

        ui_tv_abstract.setOnTextSelectedListener(new LabelCustomTextView.OnTextSelectedListener() {
            @Override
            public void onTextSelected(int start, int end, String text) {
                if(currentLabel == null)
                    return;

                LabeledItem item = new LabeledItem();
                item.start_position = start;
                item.end_position = end;
                item.text = text;
                item.label = new Labels.Label();
                item.label.name = currentLabel.name;
                item.label.fore_color = currentLabel.fore_color;
                item.label.back_color = currentLabel.back_color;
                setAdapter(0,  item);
            }
        });

        ui_tv_abstract.setEnabled(true);


    }

    // insert_pos >= 0，如果item不为null，插入元素item
    //                  如果item为null，删除insert_pos位置上的元素
    // insert_pos < 0 仅设置adapter，不改变元素
    @Nullable
    public void setAdapter(int insert_pos, @Nullable Labels.LabeledItem item) {
        Labels.LabeledItemListItemAdapter adapter = (Labels.LabeledItemListItemAdapter)ui_action_list.getAdapter();
        if(adapter == null) {
            adapter = new Labels.LabeledItemListItemAdapter(getContext(), new ArrayList<LabeledItem>(), ui_tv_abstract, true);
            final Labels.LabeledItemListItemAdapter adap = adapter;
            adapter.setItemRemoveExecuter(new Labels.LabeledItemListItemAdapter.ItemRemoveExecuter() {
                @Override
                public void remove(int position) {
                    LabeledItem item = adap.getItem(position);
                    ui_tv_abstract.clearSpan(item.start_position, item.end_position);
                    adap.remove(item);
                }
            });
        }
        if(insert_pos >= 0)
            adapter.insert(item, insert_pos);
        ui_action_list.setAdapter(adapter);
    }
}
