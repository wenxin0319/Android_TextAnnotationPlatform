package com.TAP.TAP;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jruby.ext.timeout.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
class MyX509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate certificates[],String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] ax509certificate,String s) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // TODO Auto-generated method stub
        return null;
    }
}

public class Utils {
    static boolean httpsSetupOK = false;
    public static synchronized boolean setupHttps() {
        if(httpsSetupOK)
            return true;
        try {
            /*
            SSLContext ctx = SSLContext.getInstance("SSL", "SunJSSE");
            ctx.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
            HostnameVerifier ignorer = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignorer);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
             */
            SSLContext tls = SSLContext.getInstance("TLS");
            MyX509TrustManager myX509TrustManager = new MyX509TrustManager();
            tls.init(null, new TrustManager[] {myX509TrustManager}, new SecureRandom());
            HostnameVerifier ignorer = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignorer);
            HttpsURLConnection.setDefaultSSLSocketFactory(tls.getSocketFactory());
            httpsSetupOK = true;

        } catch (Exception e) {
            httpsSetupOK = false;

        }
        return httpsSetupOK;
    }

    public static boolean checkNetworkAvailable(Context context) {
        if(context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if(connectivity == null) {
                return false;
            } else {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if(info != null) {
                    for(int i = 0; i < info.length; i++) {
                        if(info[i].isAvailable()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //返回下标...
    public static int[] splitStringByComma(String s) {
        Stack<Integer> record = new Stack<>();
        //record.push(0);
        int len = s.length();
        for(int i = 0 ; i < len; i++) {
            if(!(s.charAt(i) == ',' || s.charAt(i) == '，' || s.charAt(i) == '。' || s.charAt(i) == '\n' || s.charAt(i) == '；')) {
                record.push(i);
                while(i < len && !(s.charAt(i) == ',' || s.charAt(i) == '，' || s.charAt(i) == '。' || s.charAt(i) == '\n' || s.charAt(i) == '；'))
                    i++;
                i--;
            }
        }
        if(record.size() > 0 && record.peek() != len-1)
            record.push(len-1);
        int[] indexes = new int[record.size()];
        int p = 0;
        for(int i : record) {
            indexes[p] = i;
            p++;
        }
        return indexes;
    }


    /*
    public static String getAssetPath(Context context, String asset) {
        return context.getClass().getClassLoader().getResource("assets/"+asset).getFile().substring(5);
    }*/
    public static InputStream getAssetIS(Context context, String asset) {
        return context.getClass().getClassLoader().getResourceAsStream("assets/"+asset);
    }

    public interface OnBinarySearchMapKey {
        int getValue(int key);
    }
    public static int binSearch(int[] data, int findv, OnBinarySearchMapKey mapKey) {
        int l = 0, r = data.length - 1;
        int ans = -1;
        while(l <= r) {
            int m = (l + r) / 2;
            if(mapKey.getValue(m) <= findv) {
                ans = m;
                l = m+1;
            } else {
                r = m-1;
            }
        }
        return ans;
    }


    interface AsyncProc {
        abstract Object run();
    }
    interface AfterAsyncProc {
        abstract void run(Object result, boolean timeouted);
    }
    //timeLimit > 0时表示有时间限制，超时调用onTimeout
    public static void asyncDo(final Activity activity, final Runnable preProc, final AsyncProc asyncProc,
                               final AfterAsyncProc afterProc, final int timeLimit) {
        if(asyncProc == null)
            return;
        if(preProc != null) {
            activity.runOnUiThread(preProc);
        }

        class AsyncTaskWrapper extends AsyncTask {

            @Override
            protected Object doInBackground(Object... param) {
                return asyncProc.run();
            }
            @Override
            protected void onPostExecute(final Object res) {
                super.onPostExecute(res);
                if(afterProc != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            afterProc.run(res, false);
                        }
                    });
                }
            }
        }
        final AsyncTaskWrapper task = new AsyncTaskWrapper();
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.execute(0);
                if(timeLimit > 0) {
                    try {
                        task.get(timeLimit, TimeUnit.MILLISECONDS);
                    }catch (ExecutionException e) {

                    }catch (InterruptedException e) {

                    }catch (TimeoutException e) {
                        task.cancel(true);
                        if(!(activity.isDestroyed() || activity.isFinishing())) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    afterProc.run(null, true);
                                }
                            });
                        }
                    }
                } else {
                    try {
                        task.get();
                    }catch (ExecutionException e) {

                    }catch (InterruptedException e) {

                    }
                }
            }
        }).start();
    }

    public static String makeUrlRequestParameter(List<?> keys, List<?> values) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < keys.size(); i++) {
            sb.append(keys.get(i).toString());
            sb.append("=");
            sb.append(values.get(i).toString());
            if(i != keys.size() - 1) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    interface OnHttpsRequestError {
        void onError(String message);
    }


    public static String httpRequest(String method, String url, byte[] postData, OnHttpsRequestError onError) {
        try {
            HttpURLConnection urlCon = (HttpURLConnection) (new URL(url)).openConnection();
            urlCon.setRequestMethod(method);

            if(postData != null) {
                // urlCon.setRequestProperty("Cotent-Type", "application/x-www-form-urlencoded");
                urlCon.setRequestProperty("Content-Length", String.valueOf(postData.length));
                urlCon.setDoOutput(true);
            } else {
                urlCon.setInstanceFollowRedirects(false);
            }
            if(postData != null) {
                urlCon.getOutputStream().write(postData);
                urlCon.getOutputStream().flush();
                try {
                    urlCon.getOutputStream().close();
                }catch(IOException e) {

                }
            }
            urlCon.connect();
            if(urlCon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                onError.onError("建立HTTP连接失败，HTTP状态码:" + String.valueOf(urlCon.getResponseCode()));
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

            urlCon.getInputStream().close();
            return sb.toString();
        } catch (MalformedURLException e) {
            onError.onError("Request format error");
        }catch (IOException e) {
            e.printStackTrace();
            onError.onError("IO Exception");
        }catch (Exception e) {
            onError.onError(e.getMessage());
        }
        return null;
    }
    public static String httpGet(String url, OnHttpsRequestError onError) {
        return httpRequest("GET", url, null, onError);
    }

    public static String httpsRequest(String method, String Url, byte[] postData, OnHttpsRequestError onError) {

        if(!setupHttps()) {
            onError.onError("SSL证书错误 ");
            return null;
        }
        HttpsURLConnection urlCon = null;
        try {
            urlCon = (HttpsURLConnection) (new URL(Url)).openConnection();
            //urlCon.setDoInput(true);
            //urlCon.setDoOutput(true);
            urlCon.setRequestMethod(method);
            if(postData != null) {
                // urlCon.setRequestProperty("Cotent-Type", "application/x-www-form-urlencoded");
                urlCon.setRequestProperty("Content-Length", String.valueOf(postData.length));
                urlCon.setDoOutput(true);
            } else {
                urlCon.setInstanceFollowRedirects(false);
            }
            if(postData != null) {
                urlCon.getOutputStream().write(postData);
                urlCon.getOutputStream().flush();
                try {
                    urlCon.getOutputStream().close();
                }catch(IOException e) {

                }
            }
            urlCon.connect();
            if(urlCon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                onError.onError("建立HTTPS连接失败，HTTP状态码:" + String.valueOf(urlCon.getResponseCode()));
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            try {
                urlCon.getOutputStream().close();
            }catch(IOException e) {

            }
            urlCon.getInputStream().close();
            return sb.toString();
        }catch (MalformedURLException e) {
            onError.onError("Request format error");
        }catch (IOException e) {
            e.printStackTrace();
            onError.onError("IO Exception:" + e.getMessage());
        }catch (Exception e) {
            onError.onError(e.getMessage());
        }
        return null;
    }

    public static String httpsGet(String url, OnHttpsRequestError onError) {
        return httpsRequest("GET", url, null, onError);
    }
}
