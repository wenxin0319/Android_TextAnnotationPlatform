package com.TAP.TAP;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginActivity extends Activity {

    private String getUsername() {
        TextView view = (TextView)findViewById(R.id.login_username);
        return view.getText().toString();
    }

    private String getPassword() {
        TextView view = (TextView)findViewById(R.id.login_password);
        return view.getText().toString();
    }

    private String getMysql() {
        return ((TextView)findViewById(R.id.mysql_server_addr)).getText().toString();
    }

    private String getHost() {
//        TextView v1 = (TextView)findViewById(R.id.login_ip);
//        TextView v2 = (TextView)findViewById(R.id.login_port);
//        return v1.getText().toString() + ":" + v2.getText().toString();
        return "www.tcmai.org:80";
    }

    private void setButtonLogining() {
        Button btn = ((Button)LoginActivity.this.findViewById(R.id.login_button));
        btn.setClickable(false);
        btn.setEnabled(false);
        btn.setText(R.string.login_ing);
    }
    private void setButtonLogin() {
        Button btn = ((Button)LoginActivity.this.findViewById(R.id.login_button));
        btn.setClickable(true);
        btn.setEnabled(true);
        btn.setText(R.string.login);
    }

    private ClientSession clientSession;

    private void authorization() {
        final String username = getUsername();
        final String password = getPassword();

        Global.serverHost = getHost();

        //if(clientSession == null)
        clientSession = new ClientSession(this, Global.serverHost);

        //异步处理登录事件
        final Activity activity = this;
        Utils.asyncDo(this,  //异步任务所处上下文
                new Runnable() {
                    @Override
                    public void run() {  //异步任务开始前的工作（保证在调用线程上进行）
                        setButtonLogining();
                    }
                },
                new Utils.AsyncProc() {
                    @Override
                    public Object run() {  //异步进行的任务（保证在一个新的线程上进行）
                        return clientSession.login(username, password);
                    }
                }, new Utils.AfterAsyncProc() {  //异步任务完成或超时后的工作（保证在调用者线程上进行）
                    @Override
                    public void run(Object result, boolean timeouted) {
                        if(activity.isFinishing() || activity.isDestroyed())
                            return;
                        if(timeouted) {
                            onLoginFailed(getString(R.string.login_timeount));
                        } else {
                            //没有超时
                            Boolean ok = (Boolean) result;
                            if (ok) {
                                onLoginSuccess();
                            } else {
                                onLoginFailed(getString(R.string.login_failed));

                            }
                        }
                        setButtonLogin();
                    }
                }, Global.timeoutLimit);
    }

    void onLoginSuccess() {
        Global.clear();
        Global.currentUser = new User();
        Global.currentUser.nickname = Global.currentUser.username = getUsername();
        Global.currentUser.password = getPassword();
        Global.currentUser.faceImage = null;
        Global.mysqlServerAddr = getMysql();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        Toast.makeText(LoginActivity.this, R.string.login_ok, Toast.LENGTH_SHORT).show();
    }
    void onLoginFailed(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.login_layout);

        Button login_button = (Button)findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.checkNetworkAvailable(LoginActivity.this) == false) {
                    Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    return;
                }

                authorization();
            }
        });

    }
}
