package org.varunverma.desijokes;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuFragmentInterface;
import org.varunverma.hanu.Application.Post;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class Main extends FragmentActivity implements PostListFragment.Callbacks, 
												PostDetailFragment.Callbacks, Invoker {

	private boolean dualPane;
	private Application app;
	private ProgressDialog dialog;
	private boolean firstUse;
	private boolean appClosing;
	private HanuFragmentInterface fragmentUI;
	private int postId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        boolean pwdEntered = false;
        if(savedInstanceState != null){
        	pwdEntered = savedInstanceState.getBoolean("PwdEntered");
        	postId = savedInstanceState.getInt("PostId");
        }
        else{
        	postId = 0;
        }
        
        if (findViewById(R.id.post_list) != null) {
            dualPane = true;
        }
		else{
			dualPane = false;
		}
        
        // Tracking.
        EasyTracker.getInstance().activityStart(this);
        
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
        	if( pwdEnabled != null && Boolean.parseBoolean(pwdEnabled) && !pwdEntered){
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

	private void startMainScreen() {

		showWhatsNew();

		// Load Posts.
		Application.getApplicationInstance().getAllPosts();

		// Create the Fragment.
		FragmentManager fm = this.getSupportFragmentManager();
		Fragment fragment;

		if (dualPane) {
			// Create Post List Fragment
			fragment = new PostListFragment();
			Bundle arguments = new Bundle();
			arguments.putInt("PostId", postId);
			fragment.setArguments(arguments);
			fm.beginTransaction().replace(R.id.post_list, fragment).commitAllowingStateLoss();
		} else {
			// Create Post Detail Fragment
			fragment = new PostDetailFragment();
			Bundle arguments = new Bundle();
			arguments.putInt("PostId", postId);
			fragment.setArguments(arguments);
			fm.beginTransaction().replace(R.id.post_detail, fragment).commitAllowingStateLoss();
		}

		fragmentUI = (HanuFragmentInterface) fragment;
		
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
		
		if(firstUse){
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

	private void showHelp() {
		// Show Help
		EasyTracker.getTracker().trackView("/Help");
		Intent help = new Intent(Main.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		Main.this.startActivity(help);
	}

	@SuppressLint("NewApi")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
        getMenuInflater().inflate(R.menu.main, menu);
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.Search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
        
        return true;
    }
    
	@Override
	public void onItemSelected(int id) {
		
		if (dualPane) {
            Bundle arguments = new Bundle();
            arguments.putInt("PostId", id);
            PostDetailFragment fragment = new PostDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.post_detail, fragment)
                    .commit();

        }
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	    outState.putBoolean("PwdEntered", true);
	    if(fragmentUI != null){
			outState.putInt("PostId", fragmentUI.getSelectedItem());
		}
	}
	
	@Override
	protected void onDestroy(){
		appClosing = true;
		app.close();
		super.onDestroy();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()){
    	
    	case R.id.Rate:
    		Intent rate = new Intent(Main.this, PostRating.class);
    		rate.putExtra("PostId", fragmentUI.getSelectedItem());
			Main.this.startActivity(rate);
    		break;
    		
    	case R.id.Delete:
    		deletePost();
    		return true;
    		
    	case R.id.Search:
    		onSearchRequested();
            return true;
    		
    	case R.id.Settings:
    		EasyTracker.getTracker().trackView("/Settings");
    		Intent settings = new Intent(Main.this, DisplayFile.class);
    		settings.putExtra("File", "settings.html");
    		settings.putExtra("Title", "Settings: ");
			Main.this.startActivity(settings);
    		break;
    		
    	case R.id.Help:
    		showHelp();
    		break;
    		
    	case R.id.Share:
    		Post post = app.getPostList().get(fragmentUI.getSelectedItem());
    		EasyTracker.getTracker().trackView("/Share/" + post.getTitle());
    		Intent send = new Intent(android.content.Intent.ACTION_SEND);
    		send.setType("text/plain");
    		send.putExtra(android.content.Intent.EXTRA_SUBJECT, post.getTitle());
    		send.putExtra(android.content.Intent.EXTRA_TEXT, post.getContent(true));
    		startActivity(Intent.createChooser(send, "Share with..."));
    		break;
    		
    	case R.id.About:
    		EasyTracker.getTracker().trackView("/About");
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
    		break;

    	case R.id.Upload:
    		EasyTracker.getTracker().trackView("/Upload");
    		Intent upload = new Intent(Main.this, CreateNewPost.class);
			Main.this.startActivity(upload);
    		break;
    		
    	}
    	
    	return true;
    }
    
    private void deletePost(){
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete? " +
				"You won't be able to view this joke later.")
				.setCancelable(true)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								app.deletePost(app.getPostList().get(fragmentUI.getSelectedItem()).getId());
					    		fragmentUI.reloadUI();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
    	
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
			app.getAllPosts();
			if(dualPane){
				fragmentUI.reloadUI();
			}
		}
		
	}

	@Override
	public void loadPostsByCategory(String taxonomy, String name) {
		
		if(taxonomy.contentEquals("category")){
			app.getPostsByCategory(name);
		}
		else if(taxonomy.contentEquals("post_tag")){
			app.getPostsByTag(name);
		}
		else if(taxonomy.contentEquals("author")){
			app.getPostsByAuthor(name);
		}
		
		this.runOnUiThread(new Runnable() {
		    public void run(){
		    	fragmentUI.reloadUI();
		    }
		});
	}
	
	@Override
	public boolean isDualPane(){
		return dualPane;
	}

}