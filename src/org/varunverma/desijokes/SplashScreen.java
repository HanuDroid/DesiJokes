package org.varunverma.desijokes;

import java.util.ArrayList;
import java.util.List;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.desijokes.billingutil.IabHelper;
import org.varunverma.desijokes.billingutil.IabHelper.OnIabSetupFinishedListener;
import org.varunverma.desijokes.billingutil.IabHelper.QueryInventoryFinishedListener;
import org.varunverma.desijokes.billingutil.IabResult;
import org.varunverma.desijokes.billingutil.Inventory;
import org.varunverma.desijokes.billingutil.Purchase;
import org.varunverma.hanu.Application.Application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class SplashScreen extends Activity implements Invoker,
		OnIabSetupFinishedListener, QueryInventoryFinishedListener {

	private Application app;
	private IabHelper billingHelper;
	private TextView statusView;
	private boolean appStarted = false;
	private boolean firstUse = false;
	private boolean showNewFeatures = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash_screen);
		
		statusView = (TextView) findViewById(R.id.status);
			
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());
		
		// Tracking.
        EasyTracker.getInstance().activityStart(this);
        
		// Accept my Terms
        app.setEULAResult(true);
		if (!app.isEULAAccepted()) {
			
			Intent eula = new Intent(SplashScreen.this, DisplayFile.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Aggrement: ");
			SplashScreen.this.startActivityForResult(eula, Application.EULA);
			
		} else {
			// Start the Main Activity
			startMainActivity();
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	
    		return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	private void startMainActivity() {
		
		// Register application.
        app.registerAppForGCM();
		
		// Instantiate billing helper class
		billingHelper = IabHelper.getInstance(this, Constants.getPublicKey());
		
		// Set up
		try{
			billingHelper.startSetup(this);
		}
		catch(Exception e){
			Log.w(Application.TAG, e.getMessage(), e);
		}

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {

		case Application.EULA:
			if (!app.isEULAAccepted()) {
				finish();
			} else {
				// Start Main Activity
				startMainActivity();
			}
			break;
			
		case 900:
			
			startApp();
			
			// Kill this activity.
			Log.i(Application.TAG, "Kill Main Activity");
			SplashScreen.this.finish();
			
			break;
			
		case 998:
    		if(resultCode == RESULT_OK){
    			startMainActivity();
    		}
    		else{
    			finish();
    		}
    		break;
		}
	}
	
	private void initializeApp(){
		
		// For the first use, ask language preference.
		if (!app.getOptions().containsKey("EN_Lang")) {

			Intent lang = new Intent(SplashScreen.this, Settings.class);
			lang.putExtra("Code", "LangSettings");
			SplashScreen.this.startActivityForResult(lang, 998);
			return;

		}
		
		// Initialize app...
		if (app.isThisFirstUse()) {
			// This is the first run !
			
			firstUse = true;
			
			statusView.setText("Initializing app for first use.\nPlease wait, this may take a while");
			app.initializeAppForFirstUse(this);		

		} else {
			
			// Regular use. Initialize App
			statusView.setText("Initializing application...");
			app.initialize(this);
			
			// Check if this is upgrade
			int oldFrameworkVersion = app.getOldFrameworkVersion();
			int newFrameworkVersion = app.getNewFrameworkVersion();
			
			int oldAppVersion = app.getOldAppVersion();
			int newAppVersion;
			try {
				newAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
				newAppVersion = 0;
				Log.e(Application.TAG, e.getMessage(), e);
			}
			
			if(newAppVersion > oldAppVersion ||	newFrameworkVersion > oldFrameworkVersion){
				showNewFeatures = true;
				app.updateVersion();
			}
		}
		
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		Log.i(Application.TAG, "Command Execution completed");
		
		if (!result.isCommandExecutionSuccess()) {

			if (result.getResultCode() == 420) {
				// Application is not registered.
				String message = "This application is not registered with Hanu-Quiz.\n"
						+ "Please inform the developer about this error.";
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				
				alertDialogBuilder
					.setTitle("Application not registered !")
					.setMessage(message).setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							SplashScreen.this.finish();
						}
					}).
					create().
					show();
			}
		}
		
		// Start the app
		startApp();

	}

	private void showHelp() {
		
		Intent help = new Intent(SplashScreen.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		SplashScreen.this.startActivityForResult(help, 900);
		
	}

	private void showWhatsNew() {
		
		Intent newFeatures = new Intent(SplashScreen.this, DisplayFile.class);
		newFeatures.putExtra("File", "NewFeatures.html");
		newFeatures.putExtra("Title", "New Features: ");
		SplashScreen.this.startActivityForResult(newFeatures, 900);
	}

	private void startApp() {
		
		if(appStarted){
			return;
		}
		
		appStarted = true;
		
		if (showNewFeatures) {
			showWhatsNew();
		}

		if (firstUse) {
			showHelp();
		}

		// Start the Quiz List
		Log.i(Application.TAG, "Start Quiz List");
		Intent start = new Intent(SplashScreen.this, Main.class);
		SplashScreen.this.startActivity(start);
		
		// Kill this activity.
		Log.i(Application.TAG, "Kill Main Activity");
		SplashScreen.this.finish();
	}

	@Override
	public void ProgressUpdate(ProgressInfo progress) {
		
		String message = progress.getProgressMessage();
		if(message != null && !message.contentEquals("")){
			
			if (message.contentEquals("Show UI")) {
				startApp();
			}
			
		}
		
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		
		if (!result.isSuccess()) {
			
			// Log error ! Now I don't know what to do
			Log.w(Application.TAG, result.getMessage());
			
			Constants.setPremiumVersion(false);
			
			// Initialize the app
			initializeApp();
			
			
		} else {
			
			// Check if the user has purchased premium service			
			// Query for Product Details
			
			List<String> productList = new ArrayList<String>();
			productList.add(Constants.getProductKey());
			
			try{
				billingHelper.queryInventoryAsync(true, productList, this);
			}
			catch(Exception e){
				Log.w(Application.TAG, e.getMessage(), e);
			}
			
		}
		
	}

	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		
		if (result.isFailure()) {
			
			// Log error ! Now I don't know what to do
			Log.w(Application.TAG, result.getMessage());
			
			Constants.setPremiumVersion(false);
			
		} else {
			
			String productKey = Constants.getProductKey();
			
			Purchase item = inv.getPurchase(productKey);
			
			if (item != null) {
				// Has user purchased this premium service ???
				Constants.setPremiumVersion(inv.hasPurchase(productKey));
				
			}
			else{
				Constants.setPremiumVersion(false);
			}
			
			Constants.setProductTitle(inv.getSkuDetails(productKey).getTitle());
			Constants.setProductDescription(inv.getSkuDetails(productKey).getDescription());
			Constants.setProductPrice(inv.getSkuDetails(productKey).getPrice());
		}
		
		// Initialize the app
		initializeApp();
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
	protected void onDestroy(){
		
		if(Constants.isPremiumVersion()){
			try{
				IabHelper.getInstance().dispose();
			}
			catch(Exception e){
				Log.w(Application.TAG, e.getMessage(), e);
			}
		}
		super.onDestroy();
	}

}