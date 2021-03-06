package com.example.piotrek.tcpclientcmr;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;



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
                            outputStream.write(frameMessage());
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

    public static byte[] frameMessage() {

        int crcInt[] = new int[4];
        crcInt[0] = 5;
        crcInt[1] = 65535;
        crcInt[2] = 277;
        crcInt[3] = 9;

        int crc = GenerateChecksumCRC16(crcInt);
        String textCrc = String.valueOf(crc);
        Log.d("CRC", textCrc );

        ByteArrayOutputStream bObj = new ByteArrayOutputStream();

        byte first = 0x68;
        byte frameLength = 0x5;
        byte[] receiveAdr = new byte[2];
        receiveAdr[0] = (byte) (0xff & 0xff);
        receiveAdr[1] = (byte) (0xff & 0xff);
        byte[] sendAdr = new byte[2];
        sendAdr[0] = 0x1;
        sendAdr[1] = 0x15;
        byte msg = 0x09;
        byte answer = 0x00;
        byte[] CRC = new byte[2];
        CRC[0] = (byte) (0xBA & 0xff);
        CRC[1] = (byte) (0x8B & 0xff);
        byte end = 0x16;

        bObj.write(first);
        bObj.write(frameLength);
        bObj.write(receiveAdr[0]);
        bObj.write(receiveAdr[1]);
        bObj.write(sendAdr[0]);
        bObj.write(sendAdr[1]);
        bObj.write(msg);
        bObj.write(answer);
        bObj.write(CRC[0]);
        bObj.write(CRC[1]);
        bObj.write(end);

        byte[] Message = bObj.toByteArray();

//        byte[] request = new byte[11];
//        request[0] = first;
//        request[1] = frameLength;
//        request[2] = receiveAdr[0];
//        request[3] = receiveAdr[1];
//        request[4] = sendAdr[0];
//        request[5] = sendAdr[1];
//        request[6] = msg;
//        request[7] = answer;
//        request[8] = CRC[0];
//        request[9] = CRC[1];
//        request[10] = end;

        return Message;

    }

    public static int GenerateChecksumCRC16(int bytes[]) {

        int crc = 0xFFFF;
        int temp;
        int crc_byte;

        for (int byte_index = 0; byte_index < bytes.length; byte_index++) {

            crc_byte = bytes[byte_index];

            for (int bit_index = 0; bit_index < 8; bit_index++) {

                temp = ((crc >> 15)) ^ ((crc_byte >> 7));

                crc <<= 1;
                crc &= 0xFFFF;

                if (temp > 0) {
                    crc ^= 0x1021;
                    crc &= 0xFFFF;
                }

                crc_byte <<=1;
                crc_byte &= 0xFF;

            }
        }

        return crc;
    }

}
