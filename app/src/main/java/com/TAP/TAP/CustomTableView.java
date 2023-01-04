package com.TAP.TAP;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bin.david.form.core.SmartTable;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

public class CustomTableView extends RelativeLayout {
    private Button select_all_button, unselect_all_button, search_button;
    private EditText search_text_view;
    private ListView item_list;
    private LinearLayout item_headers;
    private int header_size;
    private int[] header_weights;
    private List<View> views;
    boolean[] selected_status;

    public CustomTableView(Context context) {
        this(context, null);
    }

    public CustomTableView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public CustomTableView(Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.custom_iterate_table_layout, this);
        select_all_button = findViewById(R.id.custom_iterate_table_selectall_button);
        unselect_all_button = findViewById(R.id.custom_iterate_table_unselectall_button);

        select_all_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int n = item_list.getCount();
                for(int i = 0; i < n; i++) {
                    selected_status[i] = true;
                    if(onItemClickListener != null)
                        onItemClickListener.OnItemClick(i, (ViewGroup)views.get(i), true);
                }
            }
        });
        unselect_all_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int n = item_list.getCount();
                for(int i = 0; i < n; i++) {
                    selected_status[i] = false;
                    if(onItemClickListener != null)
                        onItemClickListener.OnItemClick(i, (ViewGroup)views.get(i), false);
                }
            }
        });

        search_text_view = findViewById(R.id.custom_iterate_search_edit_view);
        search_button = findViewById(R.id.custom_iterate_search_button);
        search_button.setVisibility(INVISIBLE);

        item_headers = findViewById(R.id.custom_iterate_item_headers);
        item_list = findViewById(R.id.custom_iterate_item_list);

        views = new ArrayList<>();
    }

    public void removeSelectButtons() {
        select_all_button.setVisibility(View.INVISIBLE);
        unselect_all_button.setVisibility(View.INVISIBLE);
    }

    public String getSearchText() {
        return search_text_view.getText().toString();
    }

    interface OnHeaderClickListener {
        abstract void onHeaderClick(int position);
    }OnHeaderClickListener headerClickListener;

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        headerClickListener = listener;
    }

    interface OnCreateItem {
        abstract ViewGroup createItem(int i, Object item);
    }OnCreateItem createItem;

    public void setCreateItemFunctor(OnCreateItem creator) {
        createItem = creator;
    }

    interface OnItemClickListener {
        void OnItemClick(int position, ViewGroup item, boolean is_make_selected);
    }OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {onItemClickListener = listener;}

    public void setHeaders(String[] headers, int[] weights) {
        header_size = headers.length;
        header_weights = weights;
        int total_weights = 0;
        for(int i = 0; i < header_size; i++) {
            total_weights += header_weights[i];
        }
        int total_width = item_headers.getWidth();
        int total_height = item_headers.getHeight();
        Canvas cv = new Canvas(Bitmap.createBitmap(total_width, total_height, Bitmap.Config.ARGB_8888));
        Paint pt = new Paint();
        pt.setColor(0);
        int current_x = 0;
        for(int i = 0; i < header_size; i++) {
            TextView tv = new TextView(getContext());
            tv.setWidth( (int)(1.0 * weights[i] / total_weights * total_width) );
            current_x += (int)(1.0 * weights[i] / total_weights * total_width);
            tv.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            tv.setText(headers[i]);
            tv.setClickable(true);
            final int id = i;
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(headerClickListener != null)
                        headerClickListener.onHeaderClick(id);
                }
            });

            item_headers.addView(tv);
            if(i != header_size -1) {
                cv.drawLine(current_x, 0, current_x, total_height, pt);
            }
        }
        item_headers.draw(cv); //FIXME: bug: 并没能画出来分割线
    }

    private class ItemAdapter extends ArrayAdapter<View> {

        public ItemAdapter(Context context, List<View> objects) {
            super(context, R.layout.support_simple_spinner_dropdown_item, objects);
        }

        @Override
        @NonNull
        public View getView(int position, View oldView, @NonNull  ViewGroup group) {
            View v = getItem(position);
            if(v != null)
                return v;
            else
                return new View(getContext());
        }

    }

    interface SelectedTraveler {
        void call(View v);
    }

    public void iterateSelected(SelectedTraveler traveler) {
        int n = item_list.getCount();
        for(int i = 0; i < n; i++) {
            if(selected_status[i])
                traveler.call((View)item_list.getAdapter().getItem(i));
        }
    }

    public void setItems(List<?> items) {

        if(createItem == null)
            return;

        int total_weights = 0;
        int total_width = item_headers.getWidth();
        for(int i = 0; i < header_weights.length; i++) {
            total_weights += header_weights[i];
        }

        int n = items.size();
        selected_status = null;
        selected_status = new boolean[n];
        views.clear();
        for(int i = 0; i < n; i++) {
            selected_status[i] = false;
            final ViewGroup v = createItem.createItem(i, items.get(i));
            v.setMinimumWidth(item_headers.getWidth());
            for(int j = 0; j < v.getChildCount(); j++) {
                final View ch = v.getChildAt(j);
                ch.setMinimumWidth( (int)(1.0 * header_weights[j] / total_weights * total_width) );
            }
            final int id = i;
            v.setClickable(true);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemClickListener != null) {
                        onItemClickListener.OnItemClick(id, v, selected_status[id] = !selected_status[id]);
                    }
                }
            });
            views.add(v);
        }

        item_list.setAdapter(new ItemAdapter(getContext(), views));

    }


}
