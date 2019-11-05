package com.mtarget.pecadom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class TelephonyService extends Service {
	
	PhoneStateListener listener;
	
	@Override
	public void onStart(Intent intent, final int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		final TelephonyManager telephonyManager_call=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final TelephoneConnectListener telephoneConnectListener_call=new TelephoneConnectListener(telephonyManager_call, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
		telephonyManager_call.listen(telephoneConnectListener_call, PhoneStateListener.LISTEN_CALL_STATE);
	   
		Thread thread_raccrocher = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int i = 0;
				while(!telephoneConnectListener_call.get_raccroche() ||  i < 10) {
					try {
						Thread.sleep(1000);
						i++;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(telephoneConnectListener_call.get_raccroche() ||  i >= 10){
					telephonyManager_call.listen(telephoneConnectListener_call, PhoneStateListener.LISTEN_NONE);
					stopSelf(startId);
				}
			}
		});
		
		thread_raccrocher.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}