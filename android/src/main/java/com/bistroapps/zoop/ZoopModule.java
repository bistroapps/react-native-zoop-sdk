package com.bistroapps.reactnative.zoop;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.os.Bundle;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.ActivityNotFoundException;

import android.app.AlertDialog;
import android.widget.Toast;

import java.util.List;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.zoop.zoopandroidsdk.TerminalListManager;
import com.zoop.zoopandroidsdk.ZoopAPI;
import com.zoop.zoopandroidsdk.ZoopTerminalPayment;
import com.zoop.zoopandroidsdk.terminal.ApplicationDisplayListener;
import com.zoop.zoopandroidsdk.terminal.DeviceSelectionListener;
import com.zoop.zoopandroidsdk.terminal.ExtraCardInformationListener;
import com.zoop.zoopandroidsdk.terminal.TerminalMessageType;
import com.zoop.zoopandroidsdk.terminal.TerminalPaymentListener;

/**
 * Created by heldersi on 10/4/23.
 */
public class ZoopModule extends ReactContextBaseJavaModule {

    private static String DEBUG_TAG = ZoopModule.class.getName();

    private Promise paymentPromise;
    private Promise scanPromise;
    private Promise deviceConnectionPromise;

    private ReactApplicationContext reactContext;

    public ZoopModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ReactNativeZoop";
    }

    public String makeJSONResponse(String status, String message, String data) {
      try{
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("success", status == "success");
        json.put("message", message);
        json.put("data", data);
        return json.toString();
      }catch(JSONException e){
        alert("Error: " + e.getMessage());
        e.printStackTrace();
      }
      return "{}";
    }

    public void alert(String message) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
      builder.setMessage(message);
      AlertDialog alert = builder.create();
      alert.show();
    }

    @ReactMethod
    private void initialize() throws Exception {
      ZoopAPI.initialize(context);

      deviceSelectionListener = new DeviceSelectionListener() {
        @Override
        public void showDeviceListForUserSelection(Vector<JSONObject> vector) {
          try {
            System.out.println("showDeviceListForUserSelection" + vector.toString());
            scanPromise.resolve(makeJSONResponse("success", "DeviceListForUserSelection", vector.toString()))
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void updateDeviceListForUserSelection(JSONObject jsonObject, Vector<JSONObject> vector, int i) {
          try {
            System.out.println("showDeviceListForUserSelection" + vector.toString());
            scanPromise.resolve(makeJSONResponse("success", "DeviceListForUserSelection", vector.toString()))
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void bluetoothIsNotEnabledNotification() {
          terminalListManager.enableDeviceBluetoothAdapter();
        }

        @Override
        public void deviceSelectedResult(JSONObject jsonObject, Vector<JSONObject> vector, int i) {
          System.out.println("deviceSelectedResult" + jsonObject + " - " + vector.toString());
          deviceConnectionPromise.resolve(makeJSONResponse("success", "deviceSelectedResult", vector.toString()))
        }
      };

      /// Create a new instance to TerminalListManager
      terminalListManager = new TerminalListManager(deviceSelectionListener,context);

      /// create a new listener to TerminalPayment
      /// this listener is responsible for the payment logic of the POS
      terminalPaymentListener = new TerminalPaymentListener() {
        @Override
        public void paymentFailed(JSONObject jsonObject) {
          try {
            // invokeMethodUIThread("paymentFailed", jsonObject.toString());
            paymentPromise.resolve(makeJSONResponse("failure", "paymentFailed", jsonObject.toString()));
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void paymentSuccessful(JSONObject jsonObject) {
          try {
            //("paymentSuccessful", jsonObject.toString());
            paymentPromise.resolve(makeJSONResponse("success", "paymentSuccessfull", jsonObject.toString()));
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void paymentAborted() {
          try {
           // invokeMethodUIThread("paymentAborted", "paymentAbort");
            paymentPromise.resolve(makeJSONResponse("failure", "paymentAborted", null));
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void cardholderSignatureRequested() {
          System.out.println("cardholderSignatureRequested");
        }

        @Override
        public void currentChargeCanBeAbortedByUser(boolean b) {
          System.out.println("currentChargeCanBeAbortedByUser" + b);

        }

        @Override
        public void signatureResult(int i) {
          System.out.println("signatureResult" + i);
        }

        @Override
        public void pixPaymentSuccessful(JSONObject jsonObject) {

        }

        @Override
        public void pixPaymentFailed(JSONObject jsonObject) {

        }
      };

      applicationDisplayListener = new ApplicationDisplayListener() {

        @Override
        public void showMessage(String s, TerminalMessageType terminalMessageType) {
          JSONObject json = new JSONObject();
          try {
            json.put("message", s);
            json.put("terminalMessageType", terminalMessageType.toString());
          } catch (JSONException e) {
            e.printStackTrace();
          }
          try {
            //invokeMethodUIThread("terminalMessage", json.toString());
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void showMessage(String s, TerminalMessageType terminalMessageType, String s1) {
          System.out.println("currentChargeCanBeAbortedByUser" + s + " - s1 - " + s1);
        }
      };

      extraCardInformationListener = new ExtraCardInformationListener() {
        @Override
        public void cardLast4DigitsRequested() {
          System.out.println("cardLast4DigitsRequested ");
        }

        @Override
        public void cardExpirationDateRequested() {
          System.out.println("cardExpirationDateRequested ");
        }

        @Override
        public void cardCVCRequested() {
          System.out.println("cardCVCRequested ");
        }
      };

      try {
        zoopTerminalPayment = new ZoopTerminalPayment();
        zoopTerminalPayment.setTerminalPaymentListener(terminalPaymentListener);
        zoopTerminalPayment.setApplicationDisplayListener(applicationDisplayListener);
        zoopTerminalPayment.setExtraCardInformationListener(extraCardInformationListener);
      } catch (Exception err) {
        System.out.println("zoopTerminalPayment exception - " +  err.toString());
      }

    }

    @ReactMethod
    private void startScan(final Promise promise) {
      try {
        scanPromise = promise;
        terminalListManager = new TerminalListManager(deviceSelectionListener, context);
        terminalListManager.startTerminalsDiscovery();
        //promise.resolve(makeJSONResponse("success", "Start Scanning...", null));
      } catch (Exception e) {
        promise.resolve(makeJSONResponse("failure", e.getMessage(), null));
      }
    }

    @ReactMethod
    public void stopScan(final Promise promise) {
      try {
        scanPromise = promise;
        terminalListManager.finishTerminalDiscovery();
        //promise.resolve(makeJSONResponse("success", "Stop Scanning...", null));
      } catch (Exception e) {
        promise.resolve(makeJSONResponse("failure", e.getMessage(), null));
      }
    }

    @ReactMethod
    private void requestConnection(String data, final Promise promise) {
      try {
        deviceConnectionPromise = promise;
        JSONObject jsonObject = new JSONObject(data);
        System.out.println("requestConnection - " +  jsonObject);
        terminalListManager.requestZoopDeviceSelection(jsonObject);
        //promise.resolve(makeJSONResponse("success", "Request Connection", null));
      } catch (Exception e) {
        promise.resolve(makeJSONResponse("failure", e.getMessage(), null));
      }
    }

    @ReactMethod
    private void charge(String data, final Promise promise) {
      try {
        paymentPromise = promise;
        JSONObject jsonObject = new JSONObject(data);
        BigDecimal valueToCharge = BigDecimal.valueOf((double) jsonObject.get("value"));
        int paymentOption = (int) jsonObject.get("paymentOption");
        int numberInstall = (int) jsonObject.get("installments");
        String marketplaceId = (String) jsonObject.get("marketplaceId");
        String sellerId = (String) jsonObject.get("sellerId");
        String publishableKey = (String) jsonObject.get("publishableKey");

        zoopTerminalPayment.charge(
                valueToCharge,
                paymentOption,
                numberInstall,
                marketplaceId,
                sellerId,
                publishableKey
        );
      } catch (Exception e) {
        promise.resolve(makeJSONResponse("failure", e.getMessage(), null));
      }
    }
}
