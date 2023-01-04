package com.TAP.TAP;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.material.R.drawable.avd_show_password;

public class Labels {
    public static class Label {
        String name, fore_color, back_color;  //名称，前景色，背景色

        Label() {
            fore_color = "#000000";
            back_color = "#ffffff";
            //is_cleaner = false;
        }

    }

    List<Label> labels;

    public Labels() {
        labels = new ArrayList<>();
    }

    public static Labels stubGetLabels() {
        Labels ls = new Labels();
        Label a = new Label();
        a.fore_color = "#ff0000";
        a.name = "症状";
        ls.labels.add(a);

        Label b = new Label();
        b.fore_color = "#0000ff";
        b.back_color = "#ff00ff";
        b.name = "药品";
        ls.labels.add(b);

        Label d = new Label();
        d.fore_color = "#4f0d8f";
        d.back_color = "#7f2f3f";
        d.name = "理化指标";
        ls.labels.add(d);

        Label e = new Label();
        e.fore_color = "#6f7e2c";
        e.back_color = "#233333";
        e.name = "时间点";
        ls.labels.add(e);
        
        /*
        Label c = new Label();
        c.fore_color = "#000000";
        c.back_color = "#ffffff";
        c.name = "清除标记";
        ls.labels.add(c);
        */

        return ls;
    }

    static class LabeledItem {
        int sentence_id, start_position, end_position;
        String text;
        Label label;
    }

    static class LabeledJsonItem {
        String entity_mention, entity_type;
        int start_pos, end_pos;
        public static LabeledJsonItem fromLabeledItem(LabeledItem item) {
            LabeledJsonItem ji = new LabeledJsonItem();
            ji.start_pos = item.start_position;
            ji.end_pos = item.end_position;
            //TODO:适配

            return ji;
        }
    }

    public static class LabeledItemListItemAdapter extends ArrayAdapter<LabeledItem> {
        public interface ItemRemoveExecuter {
            void remove(int position);
        }

        private LabelCustomTextView text_view;
        private ItemRemoveExecuter item_remover;
        boolean isDocumentLabel;

        public LabeledItemListItemAdapter(Context context, List<LabeledItem> objects, LabelCustomTextView tv) {
            this(context, objects, tv, false);
        }
        public LabeledItemListItemAdapter(Context context, List<LabeledItem> objects, LabelCustomTextView tv, boolean documentLabel) {
            super(context, R.layout.label_labeled_listitem_layout, objects);
            text_view = tv;
            isDocumentLabel = documentLabel;
        }

        public void setItemRemoveExecuter(ItemRemoveExecuter executer) {
            item_remover = executer;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LabeledItem item = getItem(position);
            final View view;
            view = LayoutInflater.from(getContext()).inflate(R.layout.label_labeled_listitem_layout, null);

            if(item == null || item.label == null)
                return view;

            SpannableString rich_text = new SpannableString(item.text);
            rich_text.setSpan(new ForegroundColorSpan(Color.parseColor(item.label.fore_color)), 0, item.text.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            rich_text.setSpan(new BackgroundColorSpan(Color.parseColor(item.label.back_color)), 0, item.text.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            ((TextView)view.findViewById(R.id.label_labeled_item_text)).setText(rich_text);
            ((TextView)view.findViewById(R.id.label_labeled_item_hint)).setText(item.label.name);

            if(!isDocumentLabel) {
                ((Button) view.findViewById(R.id.label_labeled_ll_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        if (_item.start_position - 1 < 0) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.start_position -= 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }
                        String currentSentence = Global.getCurrentSentence(_item.sentence_id);
                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });

                ((Button) view.findViewById(R.id.label_labeled_lr_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        String currentSentence = Global.getCurrentSentence(_item.sentence_id);
                        if (_item.start_position + 1 >= currentSentence.length()) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.start_position += 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }

                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });

                ((Button) view.findViewById(R.id.label_labeled_rl_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        if (_item.end_position - 1 < 0) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.end_position -= 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }

                        String currentSentence = Global.getCurrentSentence(_item.sentence_id);
                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });

                ((Button) view.findViewById(R.id.label_labeled_rr_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        String currentSentence = Global.getCurrentSentence(_item.sentence_id);
                        if (_item.end_position + 1 >= currentSentence.length()) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.end_position += 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }
                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });
            } else {
                ((Button) view.findViewById(R.id.label_labeled_ll_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        if (_item.start_position - 1 < 0) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.start_position -= 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }
                        String currentSentence = text_view.getText().toString();
                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });

                ((Button) view.findViewById(R.id.label_labeled_lr_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        String currentSentence = text_view.getText().toString();
                        if (_item.start_position + 1 >= currentSentence.length()) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.start_position += 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }

                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });

                ((Button) view.findViewById(R.id.label_labeled_rl_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        if (_item.end_position - 1 < 0) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.end_position -= 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }
                        String currentSentence = text_view.getText().toString();
                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });

                ((Button) view.findViewById(R.id.label_labeled_rr_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        LabeledItem _item = getItem(position);

                        String currentSentence = text_view.getText().toString();
                        if (_item.end_position + 1 >= currentSentence.length()) return;

                        //clear old
                        text_view.clearSpan(_item.start_position, _item.end_position);

                        _item.end_position += 1;
                        if (_item.start_position >= _item.end_position) {
                            if (item_remover != null)
                                item_remover.remove(position);
                            return;
                        }
                        _item.text = currentSentence.substring(_item.start_position, _item.end_position);
                        SpannableString new_text = new SpannableString(_item.text);
                        new_text.setSpan(new ForegroundColorSpan(Color.parseColor(_item.label.fore_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        new_text.setSpan(new BackgroundColorSpan(Color.parseColor(_item.label.back_color)), 0, _item.text.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        ((TextView) view.findViewById(R.id.label_labeled_item_text)).setText(new_text);

                        //apply new
                        text_view.labelSpan(_item.start_position, _item.end_position,
                                Color.parseColor(_item.label.fore_color),
                                Color.parseColor(_item.label.back_color));
                    }
                });
            }
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(item_remover != null)
                        item_remover.remove(position);
                    return true;
                }
            });

            return view;
        }
    }
}
