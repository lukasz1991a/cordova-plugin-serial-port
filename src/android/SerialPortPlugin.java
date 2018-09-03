package com.plugin.SerialPortPlugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.example.x6.serial.SerialPort;
import java.util.concurrent.locks.*;
/**
 * This class echoes a string called from JavaScript.
 */
public class SerialPortPlugin extends CordovaPlugin {
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    ReadDataThread readThread;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("openSerialPort")) {
            String message = args.getString(0);
            this.openSerialPort(message, callbackContext);
            return true;
        }
        else if (action.equals("writeSerialData")) {
            String message = args.getString(0);
            this.writeSerialData(message, callbackContext);
            return true;
        }
        else if (action.equals("readSerialData")) {
            this.readSerialData(callbackContext);
            return true;
        }
        else if (action.equals("closeSerialPort")) {
            this.closeSerialPort(callbackContext);
            return true;
        }

        return false;
    }

    private void openSerialPort(String message, CallbackContext callbackContext) {
        JSONArray jsonArray = null;
        JSONObject arg = null;
        String devName = null;
        int baudrate = 0;
        int flags = 0;
        if (message != null && message.length() > 0) {
            try {
                try {
                    jsonArray = new JSONArray(message);
                }
                catch(Exception e){
                    System.out.println("Wrong!");
                }
                try {
                    arg = jsonArray.getJSONObject(0);
                }
                catch(Exception e){
                    System.out.println("Wrong!");
                }
                try {
                    devName = arg.getString("dev");
                }
                catch(Exception e){
                    System.out.println("Wrong!");
                }
                try {
                    baudrate =  arg.getInt("baudrate");
                }
                catch(Exception e){
                    System.out.println("Wrong!");
                }
                try {
                    flags = arg.getInt("flags");
                }
                catch(Exception e){
                    System.out.println("Wrong!");
                }

                serialPort = new SerialPort(new File(devName), baudrate, flags);
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
                readThread = new ReadDataThread( "Thread-Read", inputStream);
                readThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            callbackContext.error("无法打开串口");
        }
    }

    private void writeSerialData(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            try {
                byte[] byteArray = message.getBytes();
                outputStream.write(byteArray);
                System.out.println("write:"+ byteArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //callbackContext.success("write:" + message);
        } else {
            callbackContext.error("无法写入串口数据");
        }
    }

    private void readSerialData(CallbackContext callbackContext) {
 /*       byte[] byteArray = new byte[1024];
            try {
                inputStream.read(byteArray);
                System.out.println("read:"+ byteArray+ "str:" + new String(byteArray));
                //callbackContext.success(new String(byteArray));
            } catch (IOException e) {
                e.printStackTrace();
        }*/
        String data = readThread.getData();
        if(data == null){
            callbackContext.error("null");
        }else {
            callbackContext.success(data);
        }
        //System.out.println("---read:"+ readThread.getData());
    }

    private void closeSerialPort(CallbackContext callbackContext) {
            try {
                serialPort.close();
                callbackContext.success("串口已经关闭");
            } catch (Throwable e) {
                e.printStackTrace();
        }
    }

}

class ReadDataThread implements Runnable {
   private Thread t;
   private String threadName;
   byte[] byteArray = new byte[1024];
   InputStream input;
   String readData;
    private  Lock lock=new ReentrantLock();


   ReadDataThread( String name, InputStream inputStream) {
      threadName = name;
      input = inputStream;
      System.out.println("Creating " +  threadName );
   }

   public void run() {
      while(true)
      {
        try {
                input.read(byteArray);
                lock.lock();
                readData = new String(byteArray);
                lock.unlock();
                System.out.println("read:"+ byteArray+ "str:" + readData);
            } catch (IOException e) {
                System.out.println("Thread " +  threadName + " exiting..");
                e.printStackTrace();
        }
      }
   }

   public String getData() {
        String data = null;
        lock.lock();
        if(readData != null) {
            data = new String(readData);
            readData = null;
        }
        lock.unlock();
        return data;
   }

   public void start() {
      System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}