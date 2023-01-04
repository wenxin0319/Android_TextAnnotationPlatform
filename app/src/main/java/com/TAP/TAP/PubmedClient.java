package com.TAP.TAP;

import android.nfc.FormatException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class PubmedClient {

    static class PubmebSummary {
        String title, snippet;
        String articleId;
    }

    static List<PubmebSummary> search(String keyWord, int page, Utils.OnHttpsRequestError onError) {
        String webDocument = Utils.httpsGet("https://pubmed.ncbi.nlm.nih.gov/?term=" + keyWord + "&page=" + String.valueOf(page), onError);
        List<PubmebSummary> articleList = new ArrayList<>();
        if(webDocument == null)
            return articleList;

        Document document = Jsoup.parse(webDocument);
        Elements articles = document.getElementsByClass("docsum-content");
        if(articles == null)
            return articleList;

        for(Element contentItem : articles) {
            PubmebSummary ps = new PubmebSummary();

            //标题
            Element titleItem = contentItem.getElementsByClass("docsum-title").first();
            if(null == titleItem) continue;
            ps.title = titleItem.text().trim();

            //snippet
            Element snippetItem = contentItem.getElementsByClass("full-view-snippet").first();
            if(null == snippetItem) continue;
            ps.snippet = snippetItem.text();

            ps.articleId = titleItem.attr("data-article-id");

            articleList.add(ps);
        }

        return articleList;
    }

    static class PubmedAbstract {
        String title;
        String articleId;
        String authorInfo;
        String miscInfo;
        String abstractText;
        String keywords;
    }
    static PubmedAbstract fetchAbstract(String articleId, Utils.OnHttpsRequestError onError) {
        String webDocument = Utils.httpsGet("https://pubmed.ncbi.nlm.nih.gov/" + String.valueOf(articleId) + "/", onError);

        if(null == webDocument)
            return null;

        Document document = Jsoup.parse(webDocument);
        PubmedAbstract ab = new PubmedAbstract();

        Element fullViewItem = document.getElementById("full-view-heading");
        if(null == fullViewItem) return null;

        Element titleItem = fullViewItem.getElementsByClass("heading-title").first();
        if(null == titleItem) return null;
        ab.title = titleItem.text().trim();

        Element abstractItem = document.getElementById("abstract");
        if(null == abstractItem) return null;
        ab.abstractText = abstractItem.text().trim();
        ab.articleId = articleId;

        return ab;
    }

    static class UploadLabeledData {
        static class UploadLabeledItem {
            String article_id;
            int start_index, end_index;
            String text;
            String label;
        }
        List<UploadLabeledItem> data;

        public UploadLabeledData() {
            data = new ArrayList<>();
        }
    }

    static String makeSqlServerURL(String subpath) {
        if(!Global.mysqlServerAddr.startsWith("http://"))
            return "http://" + Global.mysqlServerAddr  + subpath;
        return Global.mysqlServerAddr + "/" + subpath;
    }

    static boolean uploadLabeledData(PubmedAbstract pabs, List<Labels.LabeledItem> labeled, Utils.OnHttpsRequestError OnError) {
        String exists = Utils.httpGet(makeSqlServerURL("/article/test_exists?article_id=" + pabs.articleId), OnError);
        if(exists == null) {
            OnError.onError("test_exists无应答数据");
            return false;
        }
        boolean needUploadArticle = false;
        try {
            if(Integer.parseInt(exists.trim()) != 1) {
                needUploadArticle = true;
            }
        }catch (Exception e) {
            OnError.onError("服务器响应格式错误");
            return false;
        }
        if(needUploadArticle) {
            List<String> keys = new ArrayList<>(), values = new ArrayList<>();
            keys.add("article_id");
            values.add(pabs.articleId);
            keys.add("title");
            values.add(pabs.title);
            keys.add("abstract");
            values.add(pabs.abstractText);
            //TODO: 完善
            keys.add("author");
            values.add("no");
            keys.add("publication");
            values.add("no");


            //TODO: 判断是否成功
            Utils.httpRequest("POST", makeSqlServerURL("/article/add"), Utils.makeUrlRequestParameter(keys,values).getBytes(), OnError);
        }
        UploadLabeledData udata = new UploadLabeledData();
        for(int i = 0; i < labeled.size(); i++) {
            UploadLabeledData.UploadLabeledItem item = new UploadLabeledData.UploadLabeledItem();
            item.article_id = pabs.articleId;
            item.start_index = labeled.get(i).start_position;
            item.end_index = labeled.get(i).end_position;
            item.label = labeled.get(i).label.name;
            item.text = labeled.get(i).text;
            udata.data.add(item);
        }
        String dataJson = (new GsonBuilder()).create().toJson(udata, UploadLabeledData.class);
        List<String> keys = new ArrayList<>(), values = new ArrayList<>();
        keys.add("data");
        values.add(dataJson);
        String uploadOK = Utils.httpRequest("POST", makeSqlServerURL("/article/upload_labels"),
                                Utils.makeUrlRequestParameter(keys, values).getBytes(), OnError);
        try {
            if(uploadOK == null) {
                return false;
            }
            if(Integer.parseInt(uploadOK.trim()) == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            OnError.onError(e.getMessage());
            return false;
        }
    }

}
