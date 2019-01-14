package com.weex.app.component;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.weex.app.R;
import com.weex.app.util.DialogUtil;

import java.lang.ref.WeakReference;

/**
 * @author pengl
 */
public class WeexActHelper {

    private WeakReference<Activity> activityWeakReference;

    public WeexActHelper(Activity activity){
        activityWeakReference = new WeakReference<>(activity);
    }

    /*private static WeexActHelper instance;


    private WeexActHelper(){
        if(instance == null){
            synchronized (WeexActHelper.class){
                if(instance == null){
                    instance = new WeexActHelper();
                }
            }
        }
    }

    public static WeexActHelper get(){
        if(instance == null)
            instance = new WeexActHelper();

        return instance;
    }

    public void init(Activity activity){
        if(activity == null)
            return;

        activityWeakReference = new WeakReference<>(activity);
    }*/

    private Activity getActivity(){
        if(activityWeakReference == null)
            return null;

        return activityWeakReference.get();
    }


    public void checkPermission(/*Activity activity,*/int requestCode, String permission, Runnable runnable){
        Activity activity = getActivity();
        if(activity == null)
            return;

        if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                shouldShowRationale(activity,requestCode,permission);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        } else {
            if (runnable != null)
                runnable.run();
        }
    }


    private void shouldShowRationale(final Activity activity, final int requestCode, final String requestPermission) {
        DialogUtil.showOk(activity, R.string.permission_tip_title,R.string.permission_denied_hint,R.string.permission_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{requestPermission}, requestCode);
            }
        },null);
    }

    public void showProgressDialog(String message){
        showProgressDialog(message,true);
    }

    public void showProgressDialog(String message,boolean cancelable) {
        if (getActivity() == null)
            return;

        DialogUtil.showProgressDialog(getActivity(),message,cancelable);
    }

    public void stopProgressDialog(){
        DialogUtil.stopProgressDialog();
    }

}
