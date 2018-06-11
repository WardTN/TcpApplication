package com.example.chen.tcpapplication;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    TextView tvSample;

    public static final String TAG = "MainActivity";
    MyThread myThread;

    Handler mHandler = new Handler(this);



    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSample = (TextView) findViewById(R.id.tvSample);
        myThread = new MyThread();
        myThread.start();
    }


    @Override
    public boolean handleMessage(Message msg) {
        tvSample.append((String)msg.obj);
        return false;
    }

    void writeLog(String message){
        mHandler.sendMessage(mHandler.obtainMessage(0,message));
    }


    class MyThread extends Thread{
        boolean isExist;

        @Override
        public void run() {
           writeLog("createSocket ...\n");
           if (!createSocket())
           {
               writeLog("createSocket failed!\n");
               return ;
           }
           boolean isConnected = false;
           int rlen;
           byte[] buffer = new byte[4096];
           while(!isExist){
               try{
                   if (!isConnected){
                       writeLog("connectSocket...\n");
                       if (connectSocket("192.168.0.150",6000)){
                           isConnected = true;
                           Log.d(TAG,"");
                           writeLog("connect succeed!\n");
                       }else{
                           sleep(1000);
                            continue;
                       }
                   }

                   rlen = recvSocket(buffer,0,buffer.length);
                   writeLog("receive length:"+rlen+"\n");
                   if (rlen>0)
                   {
                       mHandler.sendMessage(mHandler.obtainMessage(0,new String(buffer,0,rlen,"GB2312")));
                       sendSocket("OK\n");
                   }else{
                       writeLog("receive failed!\n");
                       closeSocket();
                       isConnected = false;
                       writeLog("disconnected!\n");
                       createSocket();
                   }


               } catch (InterruptedException e) {
                   e.printStackTrace();
               } catch (UnsupportedEncodingException e) {
                   e.printStackTrace();
               }
           }
            closeSocket();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public native boolean createSocket();
    public native boolean connectSocket(String ip, int port);

    public native int recvSocket(byte[] buffer, int offset, int count);

    public native boolean sendSocket(String sendData);

    public native boolean closeSocket();


}
