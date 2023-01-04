package com.TAP.TAP;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jsoup.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DocumentSearchFragment extends Fragment {
    View view;
    Button ui_search_button;
    EditText ui_search_text;
    ListView ui_results_list;
    int nextPageToSearch;
    boolean isSearching = false;

    private static class ResultsAdapter extends ArrayAdapter<PubmedClient.PubmebSummary> {
        interface OnGetMore {
            void  getMore();
        }
        OnGetMore onGetMore;
        ResultsAdapter(Context context, List<PubmedClient.PubmebSummary> objects, OnGetMore getMore) {
            super(context, R.layout.manage_taskmanage_item_layout, objects);
            onGetMore = getMore;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            PubmedClient.PubmebSummary item = getItem(position);
            if(item != null) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.document_result_item_layout, null);
                ((TextView) v.findViewById(R.id.dres_title_text)).setText(item.title);
                ((TextView) v.findViewById(R.id.dres_id_text)).setText(item.articleId);
                ((TextView) v.findViewById(R.id.dres_snippet_text)).setText(item.snippet);
                return v;
            } else {
                final Button btn = new Button(getContext());
                ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btn.setLayoutParams(param);
                btn.setText(R.string.get_more);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(onGetMore != null)
                            //btn.setEnabled(false);
                            //btn.setClickable(false);
                            onGetMore.getMore();
                    }
                });
                return btn;
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstance) {
        view = inflater.inflate(R.layout.document_search_fragment_layout, container, false);

        ui_search_button = view.findViewById(R.id.document_search_button);
        ui_search_text = view.findViewById(R.id.document_search_text);

        ui_search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentSearchFragment.this.onSearch(1);
            }
        });

        ui_results_list = view.findViewById(R.id.document_results_list);
        ui_results_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i < 0 || i >= adapterView.getCount())
                    return;

                PubmedClient.PubmebSummary ps = (PubmedClient.PubmebSummary)adapterView.getItemAtPosition(i);

                onClickResultItem(ps);
            }
        });

        return view;
    }

    private void onSearch(final int page) {
        if(isSearching)
            return;

        final String searchText = ui_search_text.getText().toString().trim();

        if(searchText.length() == 0) {
            Toast.makeText(getContext(), R.string.donot_search_empty_content, Toast.LENGTH_SHORT).show();
            return;
        }
        Utils.asyncDo(getActivity(), new Runnable() {
            @Override
            public void run() {
                isSearching = true;
                ui_search_button.setText(R.string.searching);
                ui_search_button.setEnabled(false);
                ui_search_button.setClickable(false);

            }
        }, new Utils.AsyncProc() {
            @Override
            public Object run() {
                return PubmedClient.search(searchText, page, new Utils.OnHttpsRequestError() {
                    @Override
                    public void onError(final String message) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "错误: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        }, new Utils.AfterAsyncProc() {
            @Override
            public void run(Object result, boolean timeouted) {
                isSearching = false;
                if (getContext() == null)
                    return;
                ui_search_button.setText(R.string.search);
                ui_search_button.setEnabled(true);
                ui_search_button.setClickable(true);

                if (result == null) {
                    Toast.makeText(getContext(), "无数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                ResultsAdapter.OnGetMore gm = new ResultsAdapter.OnGetMore() {
                    @Override
                    public void getMore() {
                        onSearch(nextPageToSearch);
                    }
                };

                List<PubmedClient.PubmebSummary> searchResults = (List<PubmedClient.PubmebSummary>) result;
                if(ui_results_list.getAdapter() == null || ui_results_list.getAdapter().getCount() == 0) {
                    searchResults.add(null);
                    ui_results_list.setAdapter(new ResultsAdapter(getContext(), searchResults, gm));
                } else {
                    ListAdapter adap = ui_results_list.getAdapter();
                    int oldCount = adap.getCount();

                    List<PubmedClient.PubmebSummary> allResults = new ArrayList<>();
                    for(int i = 0; i < ui_results_list.getCount(); i++) {
                        PubmedClient.PubmebSummary pss = (PubmedClient.PubmebSummary)ui_results_list.getAdapter().getItem(i);
                        if(pss != null)
                            allResults.add(pss);
                    }
                    allResults.addAll(searchResults);
                    allResults.add(null);
                    ui_results_list.setAdapter(new ResultsAdapter(getContext(), allResults, gm));
                    ui_results_list.setSelection(oldCount - 2);
                }

                nextPageToSearch = page+1;

            }
        }, Global.timeoutLimit * 2);
    }

    private void onClickResultItem(final PubmedClient.PubmebSummary ps) {
        Utils.asyncDo(getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        ui_search_button.setEnabled(false);
                        ui_search_button.setClickable(false);
                        ui_results_list.setEnabled(false);
                    }
                },
                new Utils.AsyncProc() {
                    @Override
                    public Object run() {
                        PubmedClient.PubmedAbstract ab = PubmedClient.fetchAbstract(ps.articleId, new Utils.OnHttpsRequestError() {
                            @Override
                            public void onError(final String message) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "下载文献失败: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        return ab;
                    }
                }, new Utils.AfterAsyncProc() {
                    @Override
                    public void run(Object result, boolean timeouted) {
                        if (null == getContext())
                            return;
                        ui_search_button.setEnabled(true);
                        ui_search_button.setClickable(true);
                        ui_results_list.setEnabled(true);
                        if (timeouted) {
                            Toast.makeText(getContext(), R.string.request_timeout, Toast.LENGTH_SHORT).show();
                        } else {
                            if (!(getActivity() instanceof DocumentLabelActivity))
                                return;

                            PubmedClient.PubmedAbstract ab = (PubmedClient.PubmedAbstract) result;
                            if (ab == null) {
                                Toast.makeText(getContext(), R.string.resolve_failed, Toast.LENGTH_SHORT);
                                return;
                            }
                            ab.title = ps.title;
                            ((DocumentLabelActivity) getActivity()).setupLabelTask(ab);
                        }
                    }
                }, Global.timeoutLimit * 2);
    }
}
