package me.yugy.github.developquicksetting;

import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class SystemPropPoker extends AsyncTask<Void, Void, Void> {

    private static final String TAG = SystemPropPoker.class.getName();

    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground(Void... params) {
        String[] services;
        try {
            Class serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method listServicesMethod = serviceManagerClass.getMethod("listServices");
            services = (String[]) listServicesMethod.invoke(null);
            for (String service : services) {
                Method checkServiceMethod = serviceManagerClass.getMethod("checkService", String.class);
                IBinder obj = (IBinder) checkServiceMethod.invoke(null, service);
                if (obj != null) {
                    Parcel data = Parcel.obtain();
                    final int SYSPROPS_TRANSACTION = ('_'<<24)|('S'<<16)|('P'<<8)|'R'; //copy from source code in android.os.IBinder.java
                    try {
                        obj.transact(SYSPROPS_TRANSACTION, data, null, 0);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        Log.i(TAG, "Someone wrote a bad service '" + service
                                + "' that doesn't like to be poked: " + e);
                    }
                    data.recycle();
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return null;
    }
}
