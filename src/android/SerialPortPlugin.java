package com.plugin.SerialPortPlugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
/**
 * This class echoes a string called from JavaScript.
 */
public class SerialPortPlugin extends CordovaPlugin {
    private ArrayList<SerialPortConnection> connections = new ArrayList<SerialPortConnection>();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("create")) {
            connections.add(new SerialPortConnection());
            callbackContext.success(connections.size());
            return true;
        } else if (action.equals("openDevice")) {
            String message = args.getString(0);
            connections.get(args.getInt(1)).openDevice(message, callbackContext);
            return true;
        } else if (action.equals("sendDataAndWaitResponse")) {
            connections.get(args.getInt(2))
                .sendDataAndWaitResponse(
                    args.getString(0),
                    args.getInt(1),
                    callbackContext
                );
            return true;
        } else if (action.equals("closeDevice")) {
            connections.get(args.getInt(0)).closeDevice(callbackContext);
            return true;
        } else if (action.equals("read")) {
            connections.get(args.getInt(0)).readSerialData(callbackContext);
            return true;
        } else if (action.equals("write")) {
            connections.get(args.getInt(1)).writeSerialData(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("setHex")) {
            connections.get(args.getInt(1)).setHex(args.getString(0));
            return true;
        }
        return false;
    }
}
