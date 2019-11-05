 package com.mtarget.pecadom;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

public class TelephoneConnectListener extends PhoneStateListener {
	//private String num_sms="+33625924732";	// DEBUG
	//private String num_sms="+33787898485";	// FRANCE
	//private String num_sms="+22558773252";	// COTE IVOIRE
	
	// NUMERO AUTORISER
	private String numero_appele;
	/*private String[] tab_numero_autorise={
		"651644397",
		"628304227",
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
		"59025818",
		"777"};
	*/
	
	// @arg Contructeur
	private PecadomActivity pecadomAct=null;
	private SimReadyService simReadyService;
	private TelephonyManager telephonyManager;
	
	// @ init_broadcastReceiver_SENT_SMS
	private PendingIntent sentPi;
	private Intent sentIntent;
	private static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	private BroadcastReceiver sent_sms_BReceiver;
	private boolean sms_send_en_cours=false;
	private boolean sms_thread_send_en_cours=false;
	
	private Map<String, ?> map_sms;
	
	private ProgressDialog myProgressDialog=null;
	private int index_close_progressDialog=0;
	
	private boolean raccrocher=false;
	
	public TelephoneConnectListener(SimReadyService _simReadyService, TelephonyManager _telephonyManager) {
		// TODO Auto-generated constructor stub
		simReadyService=_simReadyService;
		init_broadcastReceiver_SENT_SMS();
		telephonyManager=_telephonyManager;
	}
	
	public TelephoneConnectListener(TelephonyManager _telephonyManager_call, String _numero_appele) {
		// TODO Auto-generated constructor stub
		telephonyManager=_telephonyManager_call;
		numero_appele=_numero_appele;
	}

	public void set_pecadomActivity(PecadomActivity _pecadomAct){
		pecadomAct=_pecadomAct;
	}
	
	private void init_broadcastReceiver_SENT_SMS(){
		sentIntent= new Intent(SENT_SMS_ACTION);
		
		simReadyService.registerReceiver(sent_sms_BReceiver=new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						String sms_key=intent.getStringExtra("sms_key");
						
						//System.out.println("SMS_BROADCAST:"+sms_key);
						SharedPreferences preferences = simReadyService.getApplicationContext().getSharedPreferences("smsFormated", Context.MODE_PRIVATE);
						Editor editor = preferences.edit();
						editor.remove(sms_key);
    					editor.commit();
						
						CharSequence info_sms="Fiche patient envoyŽe";
						Toast.makeText(simReadyService, info_sms, Toast.LENGTH_SHORT).show();
	    				
						close_progressDialog();
						
						if(pecadomAct!=null)
							pecadomAct.maj_button_send_sms();
						
						sms_send_en_cours=false;
						sms_thread_send_en_cours=false;
					break;
					
					default:
						sms_send_en_cours=false;
						sms_thread_send_en_cours=false;
						close_progressDialog();
						Toast.makeText(simReadyService, "Fiche patient non envoyŽe (Plus de crŽdit?)", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT_SMS_ACTION));
	}
	
	public BroadcastReceiver get_sent_sms_BReceiver(){
		return sent_sms_BReceiver;
	}
	
	private void init_progressDialog(){
		if(pecadomAct!=null && !pecadomAct.is_onPause()){
			myProgressDialog = ProgressDialog.show(
				pecadomAct,
	    		"Patientez",
	    		"Vos fiches patients sont en cours d'envoi.",
	    		false);
		}
	}
	
	private void close_progressDialog(){
    	if(myProgressDialog != null && myProgressDialog.isShowing() && !pecadomAct.is_onPause() && index_close_progressDialog >= map_sms.size())
    		myProgressDialog.dismiss();
    }
	
	private void show_progressDialog(){
		if(myProgressDialog != null && !myProgressDialog.isShowing() && !pecadomAct.is_onPause()){
			index_close_progressDialog=0;
			myProgressDialog.show();
		}
	}
	
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		// TODO Auto-generated method stub
     	super.onCallStateChanged(state, incomingNumber);
		
     	switch (state) {
        	case TelephonyManager.CALL_STATE_IDLE:
        		//System.out.println("CALL_STATE_RINGING");
        	break;
        	
        	case TelephonyManager.CALL_STATE_RINGING: 
        		//System.out.println("CALL_STATE_RINGING");
            	break;
	        
        	case TelephonyManager.CALL_STATE_OFFHOOK:
	        	boolean b_raccrohe=true;
	        	
	        	//System.out.println("NUM APL::"+numero_appele);
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
	        	
	        	if(b_raccrohe)
	        		raccroche("CALL_STATE_OFFHOOK");
	        	
	            break;
        }
	}
	
	private void raccroche(String type_event){
        try {
        	System.out.println("Raccroche");
        	Class<?> c = Class.forName(telephonyManager.getClass().getName());
        	Method m = c.getDeclaredMethod("getITelephony");
        	m.setAccessible(true);
        	com.android.internal.telephony.ITelephony iTelephonyService = (ITelephony) m.invoke(telephonyManager);
        	iTelephonyService.endCall();
        	raccrocher=true;
        } 
        catch (Exception e) {
        	System.out.println("Error:Raccroche");
            e.printStackTrace();
        }
	}
	
	public boolean get_raccroche(){
		return raccrocher;
	}
	
	@Override
	public void onServiceStateChanged(ServiceState serviceState) {
		// TODO Auto-generated method stub
		super.onServiceStateChanged(serviceState);
		int etatSim=serviceState.getState();
		switch (etatSim) {
			case ServiceState.STATE_IN_SERVICE:
				if(!sms_send_en_cours){
					sms_send_en_cours=true;
					send_sms();
				}
			break;
			
			default:
	        	//sms_send_en_cours=false;
	        break;
		}
	}
	
	public void send_sms(){
    	int simState = telephonyManager.getSimState();
        switch (simState) {
	       case TelephonyManager.SIM_STATE_READY:
        		init_progressDialog();
        		close_progressDialog();
        		
	        	SharedPreferences preferences = simReadyService.getApplicationContext().getSharedPreferences("smsFormated", Context.MODE_PRIVATE);
	        	map_sms = preferences.getAll();
				
	        	if(map_sms.size()>0){
					show_progressDialog();
					// ON UTILISE UN THREAD AFIN DATTENDRE SANS BLOQUER LE PROGRAMME LA RECEPTION DU SMS, SI SMS NON RECEPTIONNE(PLUS DE CREDIT?) ON NE LE SUPPRIME PAS DE LA LISTE 
					Thread thread_raccrocher = new Thread(new Runnable() {
		    			@Override
		    			public void run() {
		    				// TODO Auto-generated method stub
		    				SmsManager smsManager=SmsManager.getDefault();
		    				
	    					for (Entry<String, ?> entry : map_sms.entrySet()) {
	    						index_close_progressDialog++;
	    						sms_thread_send_en_cours=true;
	    		        		
	    						//System.out.println("SMS_GETKEY:"+entry.getKey());
	    		        		sentIntent.putExtra("sms_key", entry.getKey());
	    		        		
	    			        	sentPi = PendingIntent.getBroadcast(simReadyService.getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_ONE_SHOT);
	    			        	smsManager.sendTextMessage(Config.NUM_SMS, null, (String) entry.getValue(), sentPi, null);
	    			        	
	    			        	int j = 0;
	    			        	while(sms_thread_send_en_cours &&  j < 40) {
			    					try {
			    						Thread.sleep(1000);
			    						j++;
			    					} catch (InterruptedException e) {
			    						// TODO Auto-generated catch block
			    						e.printStackTrace();
			    					}
	    			        	}
	    					}
		    			}
		    		});
		    		
		    		thread_raccrocher.start();
				}
	        break;
	        
	        default:
	        	sms_send_en_cours=false;
	        	Toast.makeText(simReadyService, "Fiche patient non envoyŽe", Toast.LENGTH_SHORT).show();
	        break;
        }
    }
}