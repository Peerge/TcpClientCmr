package com.example.piotrek.tcpclientcmr;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";

    private EditText edit_ip = null;
    private EditText edit_port = null;
    private Button btn_connect = null;
    private Button btn_send = null;
    private EditText edit_receive = null;

    //private boolean isConnected = false;

    Handler handler;
    ClientThread clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_ip = (EditText) this.findViewById(R.id.edit_ip);
        edit_port = (EditText) this.findViewById(R.id.edit_port);
        btn_connect = (Button) this.findViewById(R.id.btn_connect);
        btn_send = (Button) this.findViewById(R.id.btn_send);
        edit_receive = (EditText) this.findViewById(R.id.edit_receive);


        init();

        //Click here to connect
        btn_connect.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {

                String ip = edit_ip.getText().toString();
                String port = edit_port.getText().toString();

                Log.d(TAG, ip + port);

                try {
                    clientThread = new ClientThread(handler, ip, port);
                    new Thread(clientThread).start();
                    Log.d(TAG, "clientThread is start!!");
                    if(clientThread.isConnect)
                    {
                        btn_connect.setText(R.string.btn_disconnect);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

            }});

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    Message msg = new Message();
                    msg.what = 0x852;
                    msg.obj = "68h\r\n" + "\r\n" + "217.153.10.141\r\n" + "172.21.77.137\r\n" + "09h\r\n" + "\r\n" + "\r\n" + "16h";
                    clientThread.sendHandler.sendMessage(msg);

                }
                catch (Exception e)
                {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    private void init() {
        SharedPreferences sharedata = getSharedPreferences("data", 0);
        String ip = sharedata.getString("ip", "217.153.10.141");
        String port = sharedata.getString("port", "6503");
        edit_ip.setText(ip);
        edit_port.setText(port);

        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what == 0x123)
                {
                    edit_receive.setText("\n" + msg.obj);
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    public boolean onDestory(){
        return true;

    }

}
