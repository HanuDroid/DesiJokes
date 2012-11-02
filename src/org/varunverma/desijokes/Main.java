package org.varunverma.desijokes;

import java.util.List;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuGestureAnalyzer;
import org.varunverma.hanu.Application.HanuGestureListener;
import org.varunverma.hanu.Application.Post;
import org.varunverma.hanu.Application.Tracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

@SuppressLint("SetJavaScriptEnabled")
public class Main extends Activity implements Invoker, HanuGestureListener {

	private Application app;
	private ProgressDialog dialog;
	private boolean firstUse;
	private int position;
	WebView wv;
	private GestureDetector detector;
	List<Post> postList;
	private boolean appClosing;
	private AdView adView;
	private String taxonomy, name;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());

        // Accept my Terms
        if(!app.isEULAAccepted()){
        	// Show EULA.
        	Intent eula = new Intent(Main.this, DisplayFile.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Aggrement: ");
			Main.this.startActivityForResult(eula, Application.EULA);
        }
        else{
        	// Start the Main Activity
        	String pwdEnabled = app.getOptions().get("pwd_enabled");
        	if( pwdEnabled != null && Boolean.parseBoolean(pwdEnabled)){
        		// Password is enabled !
        		Intent password = new Intent(Main.this, DisplayFile.class);
        		password.putExtra("File", "password.html");
        		password.putExtra("Title", "Enter your secret password: ");
    			Main.this.startActivityForResult(password, 999);
        	}
        	else{
        		startMainActivity();
        	}
        }
    }

    private void startMainActivity() {
		// Register application.
        app.registerAppForGCM();
        
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice("E16F3DE5DF824FE222EDDA27A63E2F8A");
                
        adView = (AdView) findViewById(R.id.adView);
        
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
        
        // Initialize app...
        if(app.isThisFirstUse()){
        	// This is the first run ! 
        	
        	String message = "Please wait while the application is initialized for first usage...";
    		dialog = ProgressDialog.show(Main.this, "", message, true);
    		app.initializeAppForFirstUse(this);
    		firstUse = true;
        }
        else{
        	firstUse = false;
        	app.initialize(this);
        	
        	// Start Main Activity.
        	startMainScreen();
        }

	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	switch (requestCode) {
    	
    	case Application.EULA:
    		if(!app.isEULAAccepted()){
    			finish();
    		}
    		else{
    			// Start Main Activity
    			startMainActivity();
    		}
    		break;
    		
    	case 999:
    		if(resultCode == RESULT_OK){
    			startMainActivity();
    		}
    		else{
    			finish();
    		}
    	
    	}
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()){
    	
    	case R.id.Rate:
    		Intent rate = new Intent(Main.this, PostRating.class);
    		rate.putExtra("PostId", position);
			Main.this.startActivity(rate);
    		break;
    		
    	case R.id.Settings:
    		Tracker.getInstance().trackPageView("/Settings");
    		Intent settings = new Intent(Main.this, DisplayFile.class);
    		settings.putExtra("File", "settings.html");
    		settings.putExtra("Title", "Settings: ");
			Main.this.startActivity(settings);
    		break;
    		
    	case R.id.Help:
    		showHelp();
    		break;
    		
    	case R.id.Share:
    		Post post = postList.get(position);
    		Tracker.getInstance().trackPageView("/Share/" + post.getTitle());
    		Intent send = new Intent(android.content.Intent.ACTION_SEND);
    		send.setType("text/plain");
    		send.putExtra(android.content.Intent.EXTRA_SUBJECT, post.getTitle());
    		send.putExtra(android.content.Intent.EXTRA_TEXT, post.getContent());
    		startActivity(Intent.createChooser(send, "Share with..."));
    		break;
    		
    	case R.id.About:
    		Tracker.getInstance().trackPageView("/About");
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
    		break;
    	
    	}
    	
    	return true;
    }
    
	private void showHelp() {
		// Show Help
		Tracker.getInstance().trackPageView("/Help");
		Intent help = new Intent(Main.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		Main.this.startActivity(help);
	}

	private void startMainScreen() {

		showWhatsNew();
		
		position = 0;
		
		// Find the ListView resource.   
		wv = (WebView) findViewById( R.id.webview );
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new PostJavaScriptInterface(), "Main");
		wv.setBackgroundColor(0);
		
		postList = app.getAllPosts();
		    
		showPost(position);
		
		// Fling handling
		detector = new GestureDetector(this, new HanuGestureAnalyzer(this));
    	wv.setOnTouchListener(new OnTouchListener() {
	  	    public boolean onTouch(View view, MotionEvent e) {
	  	        detector.onTouchEvent(e);
	  	        return false;
	  	    }
	  	});
		
	}
	
	private void showWhatsNew() {
		// Show what's new in this version.
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
		
		if(app.isThisFirstUse()){
			showHelp();
			return;
		}
		
		if(newAppVersion > oldAppVersion ||
			newFrameworkVersion > oldFrameworkVersion){
			
			app.updateVersion();
			
			Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "NewFeatures.html");
			info.putExtra("Title", "What's New?");
			Main.this.startActivity(info);
			
		}
		
	}

	private void showPost(int pos) {
		// Show Post 
		if(postList.size() < 1){
			return;
		}
		
		Post post = postList.get(pos);
		Tracker.getInstance().trackPageView("/PostTitle/" + post.getTitle());
		wv.loadDataWithBaseURL("fake://not/needed", post.getHTMLCode(), "text/html", "UTF-8", "");
		
	}
	
	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		if(appClosing && result.getResultStatus() == ResultObject.ResultStatus.CANCELLED){
			app.close();
		}
		
		if(!result.isCommandExecutionSuccess()){
			
			if(result.getResultCode() == 420){
				// Application is not registered.
				String message = "This application is not registered with Hanu-Droid.\n" +
						"Please inform the developer about this error.";
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder
					.setTitle("Application not registered !")
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,int id) {
													Main.this.finish();	}})
					.create()
					.show();
			}
			
			Toast.makeText(getApplicationContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
		}
		
		if(firstUse){
			
			if(dialog.isShowing()){
				
				dialog.dismiss();
				startMainScreen();	// Start Main Activity.
			}
		}		
	}
	
	@Override
	protected void onDestroy(){
		appClosing = true;
		if (adView != null) {
			adView.destroy();
		}
		app.close();
		super.onDestroy();
	}

	@Override
	public void ProgressUpdate(ProgressInfo progress) {
		// Show UI.
		if(progress.getProgressMessage().contentEquals("Show UI")){
			
			if(dialog.isShowing()){
				
				dialog.dismiss();
				startMainScreen();	// Start Main Activity.
			}
		}
		
		// Update UI.
		if(progress.getProgressMessage().contentEquals("Update UI")){
			loadPostsByCategory();
		}
		
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
	
	public void swipeRight() {
		// Show previous
		if(position == 0){
			position = postList.size() - 1;
		}
		else{
			position--;
		}
		
		showPost(position);
	}

	public void swipeLeft() {
		// Show Next
		if(position == postList.size() - 1){
			position = 0;
		}
		else{
			position++;
		}
		
		showPost(position);
	}

	@Override
	public void swipeUp() {
		// Nothing
	}

	@Override
	public void swipeDown() {
		// Nothing
	}
	
	private void loadPostsByCategory(){
		
		if(taxonomy == null){
			postList = app.getAllPosts();
			return;
		}
		
		if(taxonomy.contentEquals("category")){
			postList = app.getPostsByCategory(name);
		}
		else if(taxonomy.contentEquals("post_tag")){
			postList = app.getPostsByTag(name);
		}
		else if(taxonomy.contentEquals("author")){
			postList = app.getPostsByAuthor(name);
		}
		
	}
	
	class PostJavaScriptInterface{
		
		public void loadPosts(String t, String n){
			taxonomy = t;
			name = n;
			Tracker.getInstance().trackEvent("Clicks", taxonomy, name, 1);
			loadPostsByCategory();
			position = 0;
			showPost(position);
		}
		
	}

}