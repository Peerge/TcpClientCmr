package com.example.piotrek.tcpclientcmr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by piotrek on 15.11.16.
 */

public class ClientThread implements Runnable {

    private final String TAG = "ClientThread";

    private Socket socket;
    private String ip;
    private int port;
    private Handler receiveHandler;
    public Handler sendHandler;
    BufferedReader bufferedReader;
    private InputStream inputStream;
    private OutputStream outputStream;
    public boolean isConnect = false;

    public ClientThread(Handler handler, String ip, String port)  {
        this.receiveHandler = handler;
        this.ip = ip;
        this.port = Integer.valueOf(port);
        Log.d(TAG, "ClientThread's construct is OK!");
    }

    public void run() {
        try {
            Log.d(TAG, "Into the run()");
            socket = new Socket(ip, port);
            isConnect = socket.isConnected();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            if(socket.isConnected()) {
                Log.d("CONNECTION", "Połączono");
            }


            new Thread() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];

                    final StringBuilder stringBuilder = new StringBuilder();
                    try {
                        while (socket.isConnected()) {
                            int readSize = inputStream.read(buffer);
                            Log.d(TAG, "readSize:" + readSize);

                            if (readSize == -1) {
                                inputStream.close();
                                outputStream.close();
                            }
                            if (readSize == 0) continue;

                            try {
                                stringBuilder.append(new String(buffer, 0, readSize));
                                Message msg = new Message();
                                msg.what = 0x123;
                                msg.obj = stringBuilder.toString();
                                receiveHandler.sendMessage(msg);

                            } catch (StringIndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }

                        }
                        if(!socket.isConnected()) {
                            Log.e("CONNECTION", "Brak połączenia z serwerem");
                        }

                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                        e.printStackTrace();
                    }


                }
            }.start();

            //To send message to server
            Looper.prepare();
            sendHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0x852) {
                        try {
                            outputStream.write(("68h\r\n" + "\r\n" + "217.153.10.141\r\n" + "172.21.77.137\r\n" + "09h\r\n" + "\r\n" + "\r\n" + "16h").getBytes());
                            outputStream.flush();

                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            };
            Looper.loop();
        } catch (SocketTimeoutException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }





}
