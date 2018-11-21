package kr.co.itsm.plugin;

import android.os.Message;
import android.util.Log;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import smartro.co.kr.main.SMTCatLinkageModuleMain;
import smartro.co.kr.protocol.SMTMsgDataMap;
import smartro.co.kr.transdirvermodule.SMTTransDriverMain.OnTransCallback;
import smartro.co.kr.util.SMTCommon;

public class HGUHCE extends CordovaPlugin {
  private CordovaInterface cordova;
  public static CordovaWebView gWebView;

  String SDPATH;

  public HGUHCE() {}

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
      super.initialize(cordova, webView);
      this.cordova = cordova;
      gWebView = webView;
      SDPATH = "/Android/data/" + cordova.getActivity().getPackageName();

      Log.d(TAG, "==> HGUHCE initialize");
  }

  @Override
  public void onDestroy() {
    delete();
  }

  public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
      Log.d(TAG,"==> HGUHCE execute: "+ action);

      try{
        // READY //
        if (action.equals("ready")) {
          callbackContext.success();
        }
        // LISTEN //
        else if (action.equals("listen")) {
          cordova.getThreadPool().execute(new Runnable() {
            public void run() {
              File d = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), SDPATH);
              if (!d.exists()) d.mkdirs(); //make dir

              File f = new File(d, "card.txt");
              try {
                OutputStream os = new FileOutputStream(f);
                byte[] data = new byte[16];
                
                String uid = args.getString(0);
                String reg = args.getString(1);
                if (reg.length() == 1)
                  reg = "0" + reg;
                
                LoginInfo.REG = reg;
                
                String hcedata = uid + reg + "861221";
                System.arraycopy (hcedata.getBytes(), 0, data, 0, 16);
                os.write(data, 0, data.length);

                os.flush();
                os.close();
              } catch (Exception e) {
                Log.e(LOG_TAG, "Error: " + e.toString());
              }
            }
          });
        }
        // CLOSE //
        else if (action.equals("close")) {
          cordova.getThreadPool().execute(new Runnable() {
            public void run() {
              delete();
              callbackContext.success();
            }
          });
        }
        else{
          callbackContext.error("Method not found");
          return false;
        }
      }catch(Exception e){
        Log.d(TAG, "ERROR: onPluginAction: " + e.getMessage());
        callbackContext.error(e.getMessage());
        return false;
      }

      return true;
  }

  private void delete() {
    File d = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), SDPATH);
    if (d.exists()) {
      File f = new File(d, "card.txt");
      f.delete();
    }
  }
}
