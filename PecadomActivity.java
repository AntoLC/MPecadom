package com.mtarget.pecadom;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class PecadomActivity extends Activity {
	//private String pass_app="modac12"/*"aaa"*/;
	
	private EditText edit_pass;	
	private String formatage_donnees_sms="", separateur_donnee=";";
	private boolean erreur=false;
	
	private Map<String, ?> map_sms;
	private SharedPreferences preferences;
	
	private SimReadyService simReadyService;
	private int idViewCourrante;
	
	private boolean is_onPause=false;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("smsFormated", MODE_PRIVATE);
    	
        // ASSOCIE LE SERVICE A LACTIVITE
        Intent intentAssociation= new Intent(PecadomActivity.this, SimReadyService.class);
        bindService(intentAssociation, mConnection, Context.BIND_AUTO_CREATE);
        
        init_layout_pass();
    }
    
    private void init_layout_pass(){
    	setContentView(R.layout.pass);
    	idViewCourrante=R.layout.pass;
    			
    	Button b_pass= (Button)findViewById(R.id.b_pass);
    	edit_pass= (EditText)findViewById(R.id.editPass);
    	
    	edit_pass.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == 66){
					validation_pass();
					return false;
				}
				
				// TODO Auto-generated method stub
				return false;
			}
    	});
    	
    	b_pass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				validation_pass();
			}
		});
    }
    
    private void validation_pass(){
    	// ON RECUP LE PASS
		String recup_pass=edit_pass.getText().toString();
		
		// ON COMPARE AVEC LE PASS DE L APPLICATION
		if(recup_pass.compareTo(Config.PASS_APP) == 0){
		    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edit_pass.getWindowToken(), 0);  
			init_layout_accueil();
		}
		else
			Toast.makeText(PecadomActivity.this, "Mauvais mot de passe", Toast.LENGTH_SHORT).show();
    }
    
    public void init_layout_accueil(){
    	setContentView(R.layout.accueil);
    	idViewCourrante=R.layout.accueil;
    	
    	Button b_saisie_form= (Button)findViewById(R.id.b_saisie_form);
    	Button b_envoie_sms= (Button)findViewById(R.id.b_envoie_sms);
    	
    	// ON RECUP LES DONNEES A ENVOYER
        map_sms = preferences.getAll(); 
        
        int lng_map=map_sms.size();
        if(lng_map>0){
        	String fiche=(lng_map==1) ? " fiche" : " fiches";
        	b_envoie_sms.setText(lng_map+fiche+" a envoyer");
        	
        	b_envoie_sms.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				simReadyService.get_telephoneConnectListener().set_pecadomActivity(PecadomActivity.this);
    				simReadyService.get_telephoneConnectListener().send_sms();
    			}
    		});
        }
        else
        	b_envoie_sms.setText("Aucune fiche a envoyer");
    	
    	b_saisie_form.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				init_layout_form();
			}
		});
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// TODO Auto-generated method stub
    	if(keyCode== KeyEvent.KEYCODE_BACK && idViewCourrante == R.layout.form){
    		new AlertDialog.Builder(PecadomActivity.this)
				.setMessage("Etes-vous sžre de vouloir annuler cette fiche en cours de saisie?")
				.setCancelable(false)
				.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
	            	@Override
					public void onClick(DialogInterface dialog, int id) {
	            		init_layout_accueil();
	            	}
	            })
	           .setNegativeButton("NON", null)
	           .show();
			return false;
    	}
    	else
    		return super.onKeyDown(keyCode, event);
    }
    
    private void init_click_listener_display(int id_radio_button, final int id_linear, final int display){
    	Button b_display= (RadioButton)findViewById(id_radio_button);
    	b_display.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout linear_tdr= (LinearLayout)findViewById(id_linear);
				linear_tdr.setVisibility(display);
			}
		});
    }
     
    private void init_layout_form(){
    	setContentView(R.layout.form);
    	idViewCourrante=R.layout.form;
    	
    	Button b_valid_form= (Button)findViewById(R.id.b_valid_form);
    	b_valid_form.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				validation_form();
			}
		});
    	Button b_annuler_form= (Button)findViewById(R.id.b_annuler_form);
    	b_annuler_form.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PecadomActivity.this)
					.setMessage("Etes-vous sžre de vouloir annuler cette fiche en cours de saisie?")
					.setCancelable(false)
					.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
		            	@Override
						public void onClick(DialogInterface dialog, int id) {
		            		init_layout_accueil();
		            	}
		            })
		           .setNegativeButton("NON", null)
		           .show();
			}
		});
    	
    	// INIT DISPLAY RADIO TDR
    	init_click_listener_display(R.id.radio_tdr_effectNon, R.id.linear_tdr_result, View.GONE);
    	init_click_listener_display(R.id.radio_tdr_effectOui, R.id.linear_tdr_result, View.VISIBLE);
    	// INIT DISPLAY RADIO PALU
    	init_click_listener_display(R.id.radio_palu_effectNon, R.id.linear_palu_result, View.GONE);
    	init_click_listener_display(R.id.radio_palu_effectOui, R.id.linear_palu_result, View.VISIBLE);
    	// INIT DISPLAY RADIO Fievre
    	init_click_listener_display(R.id.radio_fievre_effectNon, R.id.linear_fievre_result, View.GONE);
    	init_click_listener_display(R.id.radio_fievre_effectOui, R.id.linear_fievre_result, View.VISIBLE);

    	/* SPINNER AGE*/
    	init_spinner_number(R.id.spinnerAge, 61, 1, 1, "int");
    	
    	/* SPINNER Date*/
    		// Jour
    	init_spinner_number(R.id.spinnerJour, 32, 1, 1, "int");
    		// Mois 
    	init_spinner_number(R.id.spinnerMois, 13, 1, 1, "int");
    		// Annee 
    	String annee_courante=String.valueOf(DateFormat.format("yyyy", new Date()));
    	float int_annee_courante=Float.valueOf(annee_courante);
    	init_spinner_number(R.id.spinnerAnnee, 20, int_annee_courante, 1, "int");
    	
    	/* Temperature */
    	init_spinner_number(R.id.spinnerTemperatureUni, 8, 36, 1, "int");
    	init_spinner_number(R.id.spinnerTemperatureDec, 11, 0, 0.1F, "float");
    	//init_spinner_number(R.id.spinnerTemperature, 14, 36, 0.5F, "float");
    	
    	/* SPINNER Initiale Patient*/
    	String tableauCaractere[] = {" ", "A","B","C","D","E","F","G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    	ArrayAdapter<String> adapterInitPatient = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tableauCaractere);
    	Spinner spinnerInitPatientPrenom = (Spinner) findViewById(R.id.spinnerInitPatientPrenom);
    	Spinner spinnerInitPatientNom = (Spinner) findViewById(R.id.spinnerInitPatientNom);
    	spinnerInitPatientPrenom.setAdapter(adapterInitPatient);
    	spinnerInitPatientNom.setAdapter(adapterInitPatient);
    	adapterInitPatient.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
    
    private void init_spinner_number(int id_spinner, int longueur_spinner, float depart_spinner, float pas_indent, String type_donnee){
    	String[] numbers=new String[longueur_spinner];
    	numbers[0]=" ";
    	String s_mois="", s_an="";
    	
    	for(int i=1; i<numbers.length; i++, depart_spinner+=pas_indent)
    	{
    		if(id_spinner==R.id.spinnerAge){
				int annee=(int) depart_spinner/12, mois=(int) depart_spinner%12;
				s_an=s_mois="";
				
				if(mois!=0){
					s_mois=mois+"M";
					if(annee!=0)
						s_mois="+"+s_mois;
				}
				if(annee!=0)
					s_an=annee+"AN";
				
				numbers[i]=s_an+s_mois;
    		}
    		else{
    			if(type_donnee=="int")
    				numbers[i]=String.valueOf((int) depart_spinner);
    			else{
    				DecimalFormat df = new DecimalFormat("#.#");
    				String s_depart_spinner=df.format(depart_spinner);
    				numbers[i]=s_depart_spinner;
    			}
    		}
    	}
    	
    	Spinner spinner = (Spinner) findViewById(id_spinner);
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, numbers);
    	spinner.setAdapter(adapter);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
    
    private void validation_form(){
    	erreur=false;
    	String donnee="";
    	
    	//formatage_donnees_sms="V1|"+DateFormat.format("yyyyMMddkkmm", new Date())+"|";
    	formatage_donnees_sms="V3"+separateur_donnee;
    	
    	donnee=validation_spinner(R.id.spinnerAnnee, "Vous n'avez pas renseignŽ la date.", false);
    	String donneeMois=validation_spinner(R.id.spinnerMois, "Vous n'avez pas renseignŽ la date.", false);
    	if(!erreur){
    		if(donneeMois.length()==1)
        		donnee+="0";
        	donnee+=donneeMois;
    	}
    	String donneeJour=validation_spinner(R.id.spinnerJour, "Vous n'avez pas renseignŽ la date.", false);
    	if(!erreur){
    		if(donneeJour.length()==1)
        		donnee+="0";
        	donnee+=donneeJour;
        	
        	formatage_donnees_sms+=donnee+separateur_donnee;
    	}
    	
    	validation_spinner(R.id.spinnerInitPatientPrenom, "Vous n'avez pas renseignŽ l'initiale du prŽnom du patient.", true);
    	validation_spinner(R.id.spinnerInitPatientNom, "Vous n'avez pas renseignŽ l'initiale du nom du patient.", true);
    	
    	traitement_donnees(R.id.radioSexe, true, "le sexe");
    	
    	donnee=validation_spinner(R.id.spinnerAge, "Vous n'avez pas renseignŽ l'‰ge du patient.", false);
    	if(!erreur){
        	// FORMATAGE DE LAGE EN MOIS
        	int mois=0;
        	String[] resultat;
        	if(donnee.contains("AN")){
        		resultat = donnee.split("AN");
        		mois=Integer.valueOf(resultat[0]) * 12;
        	}
        	if(donnee.contains("+")){
        		resultat = donnee.split("\\+");
        		resultat = resultat[1].split("M");
        		mois+=Integer.valueOf(resultat[0]);
        	}
        	else if(donnee.contains("M")){
        		resultat = donnee.split("M");
        		mois+=Integer.valueOf(resultat[0]);
        	}
        	formatage_donnees_sms+=mois+separateur_donnee;
    	}
    	
    	donnee=validation_spinner(R.id.spinnerTemperatureUni, "Vous n'avez pas renseignŽ la tempŽrature.", false);
    	String donnee_decimal=validation_spinner(R.id.spinnerTemperatureDec, "Vous n'avez pas renseignŽ la tempŽrature.", false);
    	if(!erreur){
	    	donnee+=donnee_decimal.replace("0", "");
	    	formatage_donnees_sms+=donnee+separateur_donnee;
    	}
    	
    	// FIEVRE EFFECT
    	donnee=traitement_donnees(R.id.radio_fievre_effect, false, "le champ \"Fivre\"");
    	if(!erreur){
    		if(donnee.contains("1")){
        		donnee=traitement_donnees(R.id.radio_fievre_duree, false, "le champ \"DurŽe de la fivre\"");
        		if(!erreur)
        			formatage_donnees_sms+=donnee.contains("SupŽrieur") ? "2"+separateur_donnee : "1"+separateur_donnee;
        	}
        	else
        		formatage_donnees_sms+="0"+separateur_donnee;
    	}
    	
    	// TEST DIAGNOS. EFFECT
    	donnee=traitement_donnees(R.id.radio_tdr_effect, false, "le champ \"Test de diagnostic rapide TDR\"");
    	if(!erreur){
	    	if(donnee.contains("1")){
	    		donnee=traitement_donnees(R.id.radio_tdr, false, "le champ \"RŽsultat du test de diagnostic rapide TDR\"");
	    		if(!erreur)
	    			formatage_donnees_sms+=donnee.contains("1") ? "2"+separateur_donnee : "1"+separateur_donnee;
	    	}
	    	else
	    		formatage_donnees_sms+="0"+separateur_donnee;
    	}
    	
    	// CTA
    	traitement_donnees(R.id.radio_cta, true, "le champ \"CTA\"");
    	// SRO
    	traitement_donnees(R.id.radio_sro, true, "le champ \"SRO\"");
    	// ARTESUNATE
    	traitement_donnees(R.id.radio_artesunate, true, "le champ \"ArtŽsunate Suppo 50mg\"");
    	// PARACETAMOL
    	traitement_donnees(R.id.radio_paracetamol, true, "le champ \"ParacŽtamol\"");
    	// AMOX
    	traitement_donnees(R.id.radio_amox, true, "le champ \"AMOX\"");
    	
    	// PALUDISME EFFECT
    	donnee=traitement_donnees(R.id.radio_palu_effect, false, "le champ \"Paludisme\"");
    	if(!erreur){
	    	if(donnee.contains("1")){
	    		donnee=traitement_donnees(R.id.radio_palu_grave, false, "le champ \"GravitŽ du paludisme\"");
	    		if(!erreur)
	    			formatage_donnees_sms+=donnee.contains("Grave") ? "2"+separateur_donnee : "1"+separateur_donnee;
	    	}
	    	else
	    		formatage_donnees_sms+="0"+separateur_donnee;
    	}
    	
    	// CENTRE DE SANTE
    	traitement_donnees(R.id.radio_orRefSante, true, "le champ \"OrientŽ au centre de santŽ\"");
    	
    	// ON RECUPERE LE COMPTEUR DE FICHE_SMS_PATIENT CREE
    	SharedPreferences pref_compteur_sms = getPreferences(Context.MODE_PRIVATE);
    	int compteur_sms=pref_compteur_sms.getInt("compteur_sms", 1);
    	Editor editor = pref_compteur_sms.edit();
    	if(!erreur)
    	{
        	formatage_donnees_sms += compteur_sms+separateur_donnee;
        	// ON INCREMENT LE COMPTEUR ET ON ENREGISTRE
        	editor.putInt("compteur_sms", compteur_sms+=1);
        	editor.commit();
    	}
    	
    	System.out.println(formatage_donnees_sms+" =Longueur= "+formatage_donnees_sms.length());
    	
    	// ON ENREGISTRE LES DONNEES (PERSISTANCE)
    	if(!erreur){
        	// ON GENERE UN NAME UNIQUE AFIN DE NE PAS ECRASER LES SMS A ENVOYER
	        double rand=Math.random();
        	long unique_long=(long)(100000000*rand);
        	String unique_key_sms="SMS_"+unique_long;
        	// ON ENREGISTRE LA FICHE_SMS_PATIENT
        	editor = preferences.edit();
        	editor.putString(unique_key_sms, formatage_donnees_sms);
            editor.commit();
        	
            simReadyService.get_telephoneConnectListener().set_pecadomActivity(PecadomActivity.this);
            System.out.println(unique_key_sms);
            
            // ON REVIENT A L ACCUEIL
        	init_layout_accueil();
    	}
    }

    private String validation_spinner(int id_spinner, String renseignement, boolean formated)
    {
    	CharSequence donnees_radio = null;
    	if(!erreur)
    	{
	    	donnees_radio=get_spinner_donnees(id_spinner);
	    	if(donnees_radio != " ")
	    	{
	    		if(formated)
	    			formatage_donnees_sms+=donnees_radio+separateur_donnee;
	    		changement_parent_bg(id_spinner, "#dddddd");
	    	}	
	    	else
	    	{
	    		donnees_manquante(id_spinner, renseignement);
	    		erreur=true;
	    	}
    	}
    	return (String) donnees_radio;
    }
    
    private String traitement_donnees(int id_view, boolean formated, String renseignement)
    {
    	CharSequence donnees_radio = null;
    	if(!erreur)
    	{
    		donnees_radio=get_radio_donnees(id_view);
        	if(donnees_radio != "")
        	{
        		if(((String) donnees_radio).compareTo("Oui") == 0 || ((String) donnees_radio).compareTo("Positif") == 0 )
        			donnees_radio="1";
        		else if(((String) donnees_radio).compareTo("Non") == 0 || ((String) donnees_radio).compareTo("NŽgatif") == 0)
        			donnees_radio="0";
        		
        		if(formated)
        			formatage_donnees_sms+=donnees_radio+separateur_donnee;
        		
        		changement_parent_bg(id_view, "#dddddd");
        	}	
        	else
        	{
        		donnees_manquante(id_view, "Vous n'avez pas renseignŽ "+renseignement);
        		erreur=true;
        	}
    	}
    	return (String) donnees_radio;
    }
    
    private CharSequence get_spinner_donnees(int id_spinner)
    {
    	CharSequence retour=""; 
    	Spinner spinner = (Spinner) findViewById(id_spinner);
    	retour=spinner.getSelectedItem().toString();
		return retour;
    }
    
    private CharSequence get_radio_donnees(int id_radio_group)
    {
    	CharSequence retour=""; 
    	RadioGroup radioGroup = (RadioGroup) findViewById(id_radio_group);
		int selectedId = radioGroup.getCheckedRadioButtonId();
		
		if(selectedId > 0)
		{
			RadioButton radioButton = (RadioButton) findViewById(selectedId);
			retour=radioButton.getText();
		}
		
		return retour;
    }
    
    private void donnees_manquante(int id_view, String infos)
    {
    	changement_parent_bg(id_view, "#f18787");
		Toast.makeText(PecadomActivity.this, infos, Toast.LENGTH_SHORT).show();
    }
    
    private void changement_parent_bg(int id_view, String s_color)
    {
    	View view = findViewById(id_view);
    	LinearLayout l_parent=(LinearLayout) view.getParent();
    	l_parent.setBackgroundColor(Color.parseColor(s_color));
    }
    
    // Le changement d'orientation du tel remet le programme ˆ zŽro
    // La mŽthode ci-dessous permet de sauvegarder la config 
    //(LiŽ au Manifest: android:configChanges="keyboardHidden|orientation")
    @Override
	public void onConfigurationChanged(Configuration newConfig){
    	super.onConfigurationChanged(newConfig);      
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	maj_button_send_sms();
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	maj_button_send_sms();
    	//System.out.println("onResume");
    	is_onPause=false;
    }

    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	//System.out.println("onPause");
    	super.onPause();
    	is_onPause=true; 
    }
    
    public boolean is_onPause(){
    	return is_onPause;
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }
    
    @Override
	protected void onDestroy(){
    	super.onDestroy();
    	unbindService(mConnection);
    	finish();   	 
    }
    
    public void maj_button_send_sms()
    {
		Button b_envoie_sms= (Button) findViewById(R.id.b_envoie_sms);
		map_sms = preferences.getAll(); 
		int lng_map=map_sms.size();
		String fiche=(lng_map==1) ? " fiche" : " fiches";
		
		if(b_envoie_sms!=null)
		{
			if(lng_map>0)
				b_envoie_sms.setText(lng_map+fiche+" a envoyer");
			else
				b_envoie_sms.setText("Aucune fiche a envoyer");
		}
	}
    
    private ServiceConnection mConnection=new ServiceConnection() 
    {
		@Override
		public void onServiceDisconnected(ComponentName name) 
		{
			// TODO Auto-generated method stub
			simReadyService=null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) 
		{
			// TODO Auto-generated method stub
			simReadyService=((SimReadyService.SimReadyBinder) service).getService();
		}
	};
}