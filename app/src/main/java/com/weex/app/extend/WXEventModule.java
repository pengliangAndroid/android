package com.weex.app.extend;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.devtools.common.LogUtil;
import com.weex.app.WXPageActivity;
import com.weex.app.component.ImageChooseHelper;
import com.weex.app.component.WeexActHelper;
import com.weex.app.util.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class WXEventModule extends WXModule {
  private static final String TAG = "WXEventModule";

  private static final int CAMERA_REQUEST_CODE = 100;
  private static final int READ_SD_REQUEST_CODE = 101;

  ImageChooseHelper helper;

  WeexActHelper weexActHelper;
  JSCallback jsCallback;

  public void buildHelper(){
    if(helper != null)
      return;

    weexActHelper = new WeexActHelper((Activity) mWXSDKInstance.getContext());
    helper = new ImageChooseHelper((Activity) mWXSDKInstance.getContext());
    helper.setCrop(false);

    helper.setOnChooseImageListener(new ImageChooseHelper.OnChooseImageListener() {
      @Override
      public void onChooseImageFinish(Uri uri, File file) {
        Log.d(TAG,"selectImagePath:"+file.getPath());
        weexActHelper.showProgressDialog("处理图片中...");

        Observable.just(file)
                .map(new Function<File, String>() {
                  @Override
                  public String apply(File file) throws Exception {
                    String dir = mWXSDKInstance.getContext().getExternalCacheDir().getAbsolutePath();
                    String imageBase64 = compressImage(dir, file.getAbsolutePath());
                    Log.d(TAG,imageBase64);
                    return imageBase64;
                  }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                  @Override
                  public void onSubscribe(Disposable d) {
                  }

                  @Override
                  public void onNext(String s) {
                    weexActHelper.stopProgressDialog();
                    if(jsCallback != null) {
                      HashMap<String,Object> map = new HashMap<>();
                      map.put("result","0");
                      map.put("data",s.substring(0,10));
                      //String str = JSONObject.toJSONString(map);
                      jsCallback.invoke(map);
                    }
                  }

                  @Override
                  public void onError(Throwable e) {
                    weexActHelper.stopProgressDialog();
                    if(jsCallback != null) {
                      HashMap<String,Object> map = new HashMap<>();
                      map.put("result","1");
                      String str = JSONObject.toJSONString(map);
                      jsCallback.invoke(map);
                    }
                  }

                  @Override
                  public void onComplete() {

                  }
                });
        }

    });
  }

  private String compressImage(String dirPath,String filePath){
    String base64 = "";
    String name = null;
    try {

      int index = filePath.lastIndexOf("/");
      if (index == -1) {
        name = System.currentTimeMillis() + ".png";
      } else {
        name = filePath.substring(index + 1);
      }
      LogUtil.d("save fileName:" + name);


      String tmpPath = null;
      if (!name.endsWith(".gif")) {
        tmpPath = dirPath + File.separator + name;
        BitmapUtils.compressAndGenImage(filePath, tmpPath, 1024, false);
      } else {
        tmpPath = filePath;
      }

      LogUtil.d("path:" + tmpPath);

      BitmapFactory.Options options = new BitmapFactory.Options();
      Bitmap bitmap = BitmapFactory.decodeFile(tmpPath, options);

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
      byte[] bytes = bos.toByteArray();
      base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
      bos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return base64;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(requestCode == CAMERA_REQUEST_CODE){
      if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
        helper.startCamera();
      }
    }else if(requestCode == READ_SD_REQUEST_CODE){
      if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
        helper.startImageChoose();
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(TAG,"onActivityResult");
    //拍照回调
    if (helper != null) {
        helper.onActivityResult(requestCode, resultCode, data);
    }
  }

  @JSMethod()
  public void openURL(String url) {
    Log.d("WXEventModule","openURL");
    if (TextUtils.isEmpty(url)) {
      return;
    }
    Uri uri = Uri.parse(url);
    String scheme = uri.getScheme();

    if (scheme.equals("http") || scheme.equals("https") || scheme.equals("file")) {
      Intent renderIntent = new Intent(mWXSDKInstance.getContext(), WXPageActivity.class);
      renderIntent.setData(uri);
      mWXSDKInstance.getContext().startActivity(renderIntent);
    } else {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(uri);
      mWXSDKInstance.getContext().startActivity(intent);
    }
  }

  @JSMethod()
  public void openCameraFront(JSCallback jsCallback) {
    Log.d(TAG,"openCameraFront");
    this.jsCallback = jsCallback;

    buildHelper();
    displayItemDialog();
  }

  @Override
  public void onActivityCreate() {
    super.onActivityCreate();
    Log.d(TAG,"onActivityCreate");
  }

  public void onActivityStart(){
    super.onActivityStart();
    Log.d(TAG,"onActivityStart:"+mWXSDKInstance);
  }

  public void onActivityPause(){
    super.onActivityPause();
    Log.d(TAG,"onActivityPause");
  }

  public void onActivityResume(){
    super.onActivityResume();
    Log.d(TAG,"onActivityResume");
  }

  public void onActivityStop(){
    super.onActivityStop();
    Log.d(TAG,"onActivityStop");
  }

  public void onActivityDestroy(){
    super.onActivityDestroy();
    Log.d(TAG,"onActivityDestroy");
  }


  private void displayItemDialog(){
    Context context = mWXSDKInstance.getContext();
    String[] items = new String[]{"拍照","从相册选择图片","取消"};
     new AlertDialog.Builder(context)
             .setTitle("选择")
             .setItems(items, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                  if(which == 0){
                    weexActHelper.checkPermission(CAMERA_REQUEST_CODE, Manifest.permission.CAMERA, new Runnable() {
                      @Override
                      public void run() {
                        helper.startCamera();
                      }
                    });

                  }else if(which == 1){
                    weexActHelper.checkPermission(READ_SD_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE, new Runnable() {
                      @Override
                      public void run() {
                        helper.startImageChoose();
                      }
                    });
                  }
               }
             })
             .setCancelable(true)
             .show();
  }


}
