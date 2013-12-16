package org.varunverma.desijokes;

import org.varunverma.desijokes.billingutil.IabHelper;
import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuFragmentInterface;
import org.varunverma.hanu.Application.Post;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class Main extends FragmentActivity implements PostListFragment.Callbacks, 
												PostDetailFragment.Callbacks {

	private boolean dualPane;
	private Application app;
	private HanuFragmentInterface fragmentUI;
	private int postId;
	private PostPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	
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
			FrameLayout postDetail = (FrameLayout) findViewById(R.id.post_detail);
	        if(postDetail != null){
	        	postDetail.setVisibility(View.GONE);
	        }
		}
        
        // Tracking.
        EasyTracker.getInstance().activityStart(this);
        
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
		// Start the Main Activity
		String pwdEnabled = app.getOptions().get("pwd_enabled");
		if (pwdEnabled != null && Boolean.parseBoolean(pwdEnabled)
				&& !pwdEntered) {
			// Password is enabled !
			Intent password = new Intent(Main.this, Settings.class);
			password.putExtra("Code", "Password");
			Main.this.startActivityForResult(password, 999);
		} else {
			startMainScreen();
		}
    }

	private void startMainScreen() {
		
		if (!Constants.isPremiumVersion()) {
			
			// Show Ad.
			AdRequest adRequest = new AdRequest();
			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
			adRequest.addTestDevice("E16F3DE5DF824FE222EDDA27A63E2F8A");
			AdView adView = (AdView) findViewById(R.id.adView);

			// Start loading the ad in the background.
			adView.loadAd(adRequest);
		}

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
			
			fragmentUI = (HanuFragmentInterface) fragment;
			
		} else {
			// Create view Pager
			viewPager = (ViewPager) findViewById(R.id.post_pager);

			pagerAdapter = new PostPagerAdapter(getSupportFragmentManager(),app.getPostList().size());
			viewPager.setAdapter(pagerAdapter);
		}
		
	}

	private void showHelp() {
		// Show Help
		EasyTracker.getTracker().sendView("/Help");
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
        
        if(Constants.isPremiumVersion()){
        	menu.findItem(R.id.Buy).setVisible(false);
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
	    if(fragmentUI != null && dualPane){
			outState.putInt("PostId", fragmentUI.getSelectedItem());
		}
		else if(!dualPane && viewPager != null){
			outState.putInt("PostId", viewPager.getCurrentItem());
		}
	}
	
	@Override
	protected void onDestroy(){
		
		// Close billing helper
		try {
			IabHelper.getInstance().dispose();
		} catch (Exception e) {
			Log.w(Application.TAG, e.getMessage(), e);
		}

		app.close();
		super.onDestroy();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	
    	int id;
    	
    	switch (item.getItemId()){
    	
    	case R.id.Rate:
    		
    		if(dualPane){
    			id = fragmentUI.getSelectedItem();
    		}
    		else{
    			id = viewPager.getCurrentItem();
    		}
    		Intent rate = new Intent(Main.this, PostRating.class);
    		rate.putExtra("PostId", id);
			Main.this.startActivity(rate);
    		break;
    		
    	case R.id.Buy:
    		Intent buy = new Intent(Main.this, ActivatePremiumFeatures.class);
    		Main.this.startActivityForResult(buy,900);
    		break;
    		
    	case R.id.Delete:
    		deletePost();
    		return true;
    		
    	case R.id.Search:
    		onSearchRequested();
            return true;
    		
    	case R.id.Settings:
    		EasyTracker.getTracker().sendView("/Settings");
    		Intent settings = new Intent(Main.this, Settings.class);
    		settings.putExtra("Code", "Settings");
			Main.this.startActivity(settings);
    		break;
    		
    	case R.id.Help:
    		showHelp();
    		break;
    		
    	case R.id.Share:
    		try{
    			if(dualPane){
        			id = fragmentUI.getSelectedItem();
        		}
        		else{
        			id = viewPager.getCurrentItem();
        		}
    			Post post = app.getPostList().get(id);
        		EasyTracker.getTracker().sendView("/Share/" + post.getTitle());
        		Intent send = new Intent(android.content.Intent.ACTION_SEND);
        		send.setType("text/plain");
        		send.putExtra(android.content.Intent.EXTRA_SUBJECT, post.getTitle());
        		send.putExtra(android.content.Intent.EXTRA_TEXT, post.getContent(true));
        		startActivity(Intent.createChooser(send, "Share with..."));
    		}catch(Exception e){
    			Log.e(Application.TAG, e.getMessage(), e);
    			finish();
    		}
    		break;
    		
    	case R.id.About:
    		EasyTracker.getTracker().sendView("/About");
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
    		break;

    	case R.id.Upload:
    		EasyTracker.getTracker().sendView("/Upload");
    		Intent upload = new Intent(Main.this, CreateNewPost.class);
			Main.this.startActivity(upload);
    		break;
    		
    	}
    	
    	return true;
    }
    
    private void deletePost(){
    	
    	final int postId;
    	if(dualPane){
    		postId = fragmentUI.getSelectedItem();
		}
		else{
			postId = viewPager.getCurrentItem();
		}
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete? " +
				"You won't be able to view this joke later.")
				.setCancelable(true)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try{
									app.deletePost(app.getPostList().get(postId).getId());
						    		if(dualPane){
						    			fragmentUI.reloadUI();
						    		}
					    		}catch(Exception e){
					    			Log.e(Application.TAG, e.getMessage(), e);
					    			finish();
					    		}
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

    	case 900:
    		if(data.getBooleanExtra("RestartApp", false)){
				finish();
			}
    		break;
    		
    	case 999:
    		if(resultCode == RESULT_OK){
    			startMainScreen();
    		}
    		else{
    			finish();
    		}
    		break;
    		
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
		    	if(dualPane){
		    		fragmentUI.reloadUI();
		    	}
		    	else{
		    		pagerAdapter.setNewSize(app.getPostList().size());
		    		pagerAdapter.notifyDataSetChanged();
		    		viewPager.setCurrentItem(0);
		    	}
		    }
		});
	}
	
	@Override
	public boolean isDualPane(){
		return dualPane;
	}

}