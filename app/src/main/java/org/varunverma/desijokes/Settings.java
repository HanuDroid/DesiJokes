package org.varunverma.desijokes;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ayansh.hanudroid.Application;

public class Settings extends AppCompatActivity implements OnClickListener {
	
	private CheckBox pwdEnabled, showMemes;
	private TextView password;
	private RadioButton rbLangAll, rbLangEn;
	private Button save, cancel;
	private String code;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);       
        setContentView(R.layout.settings);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();

		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);
        
        code = getIntent().getStringExtra("Code");
        
        LinearLayout langSettings = (LinearLayout) findViewById(R.id.language_settings);
        RelativeLayout pwdSettings = (RelativeLayout) findViewById(R.id.pwd_settings);
        
        pwdEnabled = (CheckBox) findViewById(R.id.enable_pwd);
        password = (TextView) findViewById(R.id.password);
        rbLangAll = (RadioButton) findViewById(R.id.lang_All);
        rbLangEn = (RadioButton) findViewById(R.id.lang_en);
		showMemes = (CheckBox) findViewById(R.id.show_memes);
        
        save = (Button) findViewById(R.id.ok);
        save.setOnClickListener(this);
        
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        
        if(code.contentEquals("Settings")){
        	// We should keep all.
        }
        
        if(code.contentEquals("LangSettings")){
        	// We should remove password settings
        	pwdSettings.setVisibility(View.GONE);
        }
        
        if(code.contentEquals("Password")){
        	// We should remove language settings
        	langSettings.setVisibility(View.GONE);
        	pwdEnabled.setVisibility(View.GONE);
        }
        
		String pwd_info = "The application cannot be launched without entering password. " +
				"If you forget the password, un-install and install again.";
		TextView pwdInfo = (TextView) findViewById(R.id.pwd_info);
		pwdInfo.setText(pwd_info);
        
	}
	
	@Override
    public void onStart() {
		
		super.onStart();
		
		Application app = Application.getApplicationInstance();
		
		if(code.contentEquals("Settings")){
			// Settings. We must default Values
			boolean pwd_enabled = Boolean.valueOf(app.getOptions().get("pwd_enabled"));
			if(pwd_enabled){
				pwdEnabled.setChecked(true);
				password.setText(app.getOptions().get("password"));
			}
			else{
				pwdEnabled.setChecked(false);
			}
			
			boolean en_lang = Boolean.valueOf(app.getOptions().get("EN_Lang"));
			if(en_lang){
				rbLangAll.setChecked(false);
				rbLangEn.setChecked(true);
			}
			else{
				rbLangAll.setChecked(true);
				rbLangEn.setChecked(false);
			}
			
		}
		
		if(code.contentEquals("LangSettings")){
        	// Language Settings
			rbLangAll.setChecked(true);
			rbLangEn.setChecked(false);
        }
		
	}

	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
		
		case R.id.ok:
			
			String pwd = password.getEditableText().toString();
			
			if(code.contentEquals("Settings") ){
				if(pwdEnabled.isChecked() && pwd.contentEquals("")){
					Toast.makeText(getApplicationContext(), "Enter a valid password !", Toast.LENGTH_LONG).show();
					break;
				}
			}
					
			if(code.contentEquals("Settings") || code.contentEquals("LangSettings") ){
				saveSettings();
				setResult(RESULT_OK);
				finish();
			}
			
			if(code.contentEquals("Password")){
				
				if(pwd.contentEquals("")){
					Toast.makeText(getApplicationContext(), "Enter a valid password !", Toast.LENGTH_LONG).show();
					break;
				}
				
				boolean success = validatePassword();
				if(success){
					setResult(RESULT_OK);
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), "Wrong password !", Toast.LENGTH_LONG).show();
				}
				
			}
			
			break;
			
		case R.id.cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		
		}
		
	}

	private boolean validatePassword() {
		
		Application app = Application.getApplicationInstance();
		
		String pwd = app.getOptions().get("password");
		if(pwd == null){
			return true;
		}
		
		if(pwd.contentEquals(password.getEditableText().toString())){
			return true;
		}
		
		return false;
	}

	private void saveSettings() {
		
		Application app = Application.getApplicationInstance();
		
		if(code.contentEquals("Settings")){
			
			if(pwdEnabled.isChecked()){
				
				app.addParameter("pwd_enabled", String.valueOf(pwdEnabled.isChecked()));
				app.addParameter("password", password.getEditableText().toString());
				
			}
			else{
				
				app.removeParameter("pwd_enabled");
				app.removeParameter("password");
				
			}
			
			if(rbLangAll.isChecked()){
				
				app.addParameter("EN_Lang", "false");
				// Set both English and Hindi
				app.addSyncCategory("English");
				app.addSyncCategory("Hindi");
			}
			else{
				
				// Only English Language
				app.addParameter("EN_Lang", String.valueOf(rbLangEn.isChecked()));

				// Add to Sync Parameters
				app.addSyncCategory("English");
				app.removeSyncCategory("Hindi");
				
			}

			if(showMemes.isChecked()){

				// Show Memes - Save Parameter
				app.addParameter("Show_Memes", String.valueOf(true));

				// Add to Sync Parameters
				app.addSyncCategory("Meme");

			}
			else{

				// Show Memes - Save Parameter
				app.addParameter("Show_Memes", String.valueOf(false));

				// Add to Sync Parameters
				app.removeSyncCategory("Meme");
			}

		}
		
		if(code.contentEquals("LangSettings")){
        	
			if(rbLangAll.isChecked()){
				
				app.addParameter("EN_Lang", "false");
				app.addSyncCategory("English");
				app.addSyncCategory("Hindi");
			}
			else{
				
				// Only English Language
				app.addParameter("EN_Lang", String.valueOf(rbLangEn.isChecked()));

				// Add to Sync Parameters
				app.addSyncCategory("English");
				app.removeSyncCategory("Hindi");
				
			}
        }
		
		if(code.contentEquals("Password")){
			
		}
		
	}
	
}