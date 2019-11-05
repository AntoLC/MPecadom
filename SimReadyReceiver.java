package com.mtarget.pecadom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SimReadyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    // TODO Auto-generated method stub
		//System.out.println("SimReadyReceiver.intent.getAction:"+intent.getAction());
		context.startService(intent.setClass(context, SimReadyService.class));
		
		if(intent.getAction().contains(Intent.ACTION_NEW_OUTGOING_CALL))
			context.startService(intent.setClass(context, TelephonyService.class));
	}
}