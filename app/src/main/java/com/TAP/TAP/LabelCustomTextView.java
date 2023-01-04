package com.TAP.TAP;

import android.content.Context;

import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

public class LabelCustomTextView extends AppCompatTextView {
    private int selection_start, selection_end;
    private int clear_fore_color, label_fore_color;
    private int label_back_color, clear_back_color;
    private SpannableString rich_text;
    private boolean is_selecting;
    private int last_scrolly;
    private boolean is_grabmode;
    private float startY0, startY1, endY0, endY1;
    private int textScrollY, lastDeltaY;
    private long grabEndTime = 0;
    private long touchStartTime = 0;

    public LabelCustomTextView(Context context, AttributeSet attr) {
        super(context, attr);
        selection_start = selection_end = -1;
        setClearStyle(Color.parseColor("#000000"), Color.parseColor("#ffffff"));
        setLabelStyle(Color.parseColor("#000000"), Color.parseColor("#ffffff"));
        clearRichText();
        is_selecting = false;
        is_grabmode = false;
        startY0 = startY1 = endY1 = endY0 = 0;
        textScrollY = 0;
    }
    public void setClearStyle(int fore_color, int back_color) {
        clear_fore_color = fore_color;
        clear_back_color = back_color;
    }
    public void setLabelStyle(int fore_color, int back_color) {
        label_fore_color = fore_color;
        label_back_color = back_color;
    }
    public void setGrabMode(boolean enable) {
        is_grabmode = enable;
    }


    public SpannableString getRichText() {
        return rich_text;
    }
    public void clearRichText() {
        rich_text = new SpannableString("");
    }
    public void setRichText(String str) {
        rich_text = new SpannableString(str);
    }
    public void clearSpan(int s, int t) {
        rich_text.setSpan(new ForegroundColorSpan(clear_fore_color), s, t, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        rich_text.setSpan(new BackgroundColorSpan(clear_back_color), s, t, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        setText(rich_text);
    }
    public void labelSpan(int s, int t, int f, int b) {
        rich_text.setSpan(new ForegroundColorSpan(f), s, t, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        rich_text.setSpan(new BackgroundColorSpan(b), s, t, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        setText(rich_text);
    }
    public void labelSpan(int s, int t) {
        labelSpan(s, t, label_fore_color, label_back_color);
    }


    public void setText(String str) {
        super.setText(str);
        setRichText(str);
        is_selecting = false;
        selection_start = selection_end = -1;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) { }

    public static interface OnTextSelectedListener {
        void onTextSelected(int start, int end, String text);
    }
    private OnTextSelectedListener text_selected_listener;
    void setOnTextSelectedListener(OnTextSelectedListener ls) {
        text_selected_listener = ls;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        /*
        if(is_grabmode || event.getPointerCount() >= 2) {  //双指触屏的时候可以上下翻页
            return super.onTouchEvent(event);
        }*/
        if(is_grabmode || event.getPointerCount() >= 2) {
            //Log.d("2 pointers", "2 pointers");
            boolean oldGrabMode = is_grabmode;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:   //it seems never reach in this case.
                    Log.d("2p", "2 down");
                    if(event.getPointerCount() < 2)
                        return true;
                    startY0 = event.getY(0);
                    startY1 = event.getY(1);
                    endY0 = startY0;
                    endY1 = startY1;
                    if(event.getPointerCount() >= 2)
                        is_grabmode = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.d("2p", "2 move");
                case MotionEvent.ACTION_UP:
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        Log.d("2p", "2up");
                    }

                    if(event.getPointerCount() >= 2) {
                        is_grabmode = true;
                    } else {
                        is_grabmode = false;
                        grabEndTime = System.currentTimeMillis();
                    }
                    if(!oldGrabMode) {
                        startY0 = event.getY(0);
                        startY1 = event.getY(1);
                    }
                    if(!is_grabmode)
                        break;
                    endY0 = event.getY(0);
                    endY1 = event.getY(1);
                    int deltaY = (int)Math.max(endY1 - startY1, endY0 - startY0);
                    this.scrollBy(0, -(deltaY));
                    if(this.getScrollY() <= 0)
                        this.scrollTo(0, 0);
                    int maxSY = Math.max(0, this.getLineCount() * this.getLineHeight() - this.getHeight() / 2);
                    if(this.getScrollY() >= maxSY)
                        this.scrollTo(0, maxSY);
                    //lastDeltaY = deltaY;
                    startY0 = endY0;
                    startY1 = endY1;
                    break;
            }
            return true;
        }
        if(is_grabmode)
            return true;

        //延迟一小段时间再结束双指滑动的判定
        //因为双指拿开总有一个先后顺序，会有一瞬间变成单指
        //这里多留一小段时间，在这个时间之内出现单指不会判定选词
        if(System.currentTimeMillis() - grabEndTime < 1000)
            return true;

        int action = event.getAction();
        Layout layout = getLayout();
        int line = 0;
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                line = layout.getLineForVertical(getScrollY()+ (int)event.getY());
                selection_start = layout.getOffsetForHorizontal(line, (int)event.getX());
                selection_end = -1;
                is_selecting = true;
                touchStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                if(!is_selecting)break;
                if(System.currentTimeMillis() - touchStartTime < 100) {
                    return true;
                }
                line = layout.getLineForVertical(getScrollY()+(int)event.getY());
                int tmp_end = layout.getOffsetForHorizontal(line, (int)event.getX());
                if(tmp_end < selection_end) {
                    clearSpan(selection_start, selection_end);
                }selection_end = tmp_end;
                if(selection_end < selection_start) {
                    int a = selection_end;
                    selection_end = selection_start;
                    selection_start = a;
                }

                labelSpan(selection_start, selection_end);

                if(is_selecting) {
                    setText(rich_text);
                    if(action == MotionEvent.ACTION_UP) {
                        if(text_selected_listener != null && selection_end - selection_start >= 1) {
                            text_selected_listener.onTextSelected(selection_start, selection_end,
                                    getText().subSequence(selection_start, selection_end).toString());
                        }
                        is_selecting = false;
                        selection_start = -1;
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public int getSelectionStart() {
        return selection_start;
    }

    @Override
    public int getSelectionEnd() {
        return selection_end;
    }

}