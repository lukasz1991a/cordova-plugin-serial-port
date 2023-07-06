package com.plugin.SerialPortPlugin;

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

public class SerialPortConnection {
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ReadDataThread readThread;
    private boolean dataModel;


    public void openDevice(String message, CallbackContext callbackContext) {
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
                    callbackContext.error("get json array exception");
                    return;
                }
                try {
                    arg = jsonArray.getJSONObject(0);
                }
                catch(Exception e){
                    callbackContext.error("get arg exception");
                    return;
                }
                try {
                    devName = arg.getString("dev");
                }
                catch(Exception e){
                    callbackContext.error("get dev exception");
                    return;
                }
                try {
                    baudrate =  arg.getInt("baudrate");
                }
                catch(Exception e){
                    callbackContext.error("get baudrate exception");
                    return;
                }
                try {
                    flags = arg.getInt("flags");
                }
                catch(Exception e){
                    callbackContext.error("get flags exception");
                    return;
                }
                try {
                    this.dataModel = arg.getBoolean("isHex");
                }
                catch(Exception e){
                    this.dataModel = false;
                }
                System.out.println("dataModel:" + this.dataModel);

                serialPort = new SerialPort(new File(devName), baudrate, flags);
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
                readThread = new ReadDataThread( "Thread-Read", inputStream, this.dataModel);
                readThread.start();
                callbackContext.success("open device success");
            } catch (IOException e) {
                e.printStackTrace();
                callbackContext.error("open device exception");
            }
        } else {
            callbackContext.error("open device fail");
        }
    }

    public void closeDevice(CallbackContext callbackContext) {
            try {
                readThread.stop();
                outputStream.close();
                serialPort.close();
                callbackContext.success("close device success");
            } catch (Throwable e) {
                e.printStackTrace();
                callbackContext.error("close device exception");
        }
    }

    public void readSerialData(CallbackContext callbackContext) {
        String data = readThread.getData();
        if(data == null){
            callbackContext.error("null");
        }else {
            callbackContext.success(data);
        }
    }

    public void writeSerialData(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            try {
                byte[] byteArray;
                if(this.dataModel == true) {
                    byteArray =FormatUtil.hexString2Bytes(message);
                } else {
                    byteArray = message.getBytes();
                }
                outputStream.write(byteArray);
                System.out.println("writestr:" + message);
                callbackContext.success("write data success");
            } catch (IOException e) {
                e.printStackTrace();
                callbackContext.error("write data exception");
            }
        } else {
            callbackContext.error("write data fail");
        }
    }

    public void sendDataAndWaitResponse(String message, int timeout, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            try {
                byte[] byteArray;
                if(this.dataModel == true) {
                    byteArray =FormatUtil.hexString2Bytes(message);
                } else {
                    byteArray = message.getBytes();
                }
                outputStream.write(byteArray);
                String data = readThread.getData();
                int i = 0;
                while(data == null && i < (int)(timeout/10)) {
                    try {
                        Thread.sleep(10);
                    } catch(Exception e) {
                    }
                    i++;
                    data = readThread.getData();
                }
                if(data == null) {
                    callbackContext.error("read data timeout");
                } else {
                    callbackContext.success(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
                callbackContext.error("write data exception");
            }
        } else {
            callbackContext.error("write data fail");
        }
    }

    public void setHex(String message) {
        if (message != null && message.length() > 0) {
            this.dataModel = Boolean.parseBoolean(message);
        } else {
            this.dataModel = false;
        }
        readThread.setHex(this.dataModel);
    }
}

