package org.varunverma.desijokes;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;

public class Search extends AppCompatActivity implements PostListFragment.Callbacks,
												PostDetailFragment.Callbacks {

	private boolean dualPane;
	private Application app;
	private HanuFragmentInterface fragmentUI;
	private int postId;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		LinearLayout swipeHelpLayout = (LinearLayout) findViewById(R.id.swipe_help);
		swipeHelpLayout.setVisibility(View.GONE);
        
        if(savedInstanceState != null){
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
			ViewPager viewPager = (ViewPager) findViewById(R.id.post_pager);
			viewPager.setVisibility(View.GONE);
		}

        
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());

		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();

		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);

		// Load Posts  that match search criteria !!
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	
          String query = intent.getStringExtra(SearchManager.QUERY);
          setTitle("Search results for: '" + query + "'");
          app.performSearch(query);
          
          // Save Query for future
          SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
        		  SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
          
          suggestions.saveRecentQuery(query, null);
          
        }
		
		// Create the Fragment.
		FragmentManager fm = this.getSupportFragmentManager();
		Fragment fragment;

		// Create Post List Fragment
		fragment = new PostListFragment();
		Bundle arguments = new Bundle();
		arguments.putInt("PostId", postId);
		fragment.setArguments(arguments);
		
		if(dualPane){
			arguments.putBoolean("ShowFirstItem", true);
			fm.beginTransaction().replace(R.id.post_list, fragment).commitAllowingStateLoss();
		}
		else{
			arguments.putBoolean("ShowFirstItem", false);
			fm.beginTransaction().replace(R.id.post_detail, fragment).commitAllowingStateLoss();
		}
		
		fragmentUI = (HanuFragmentInterface) fragment;
		
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
		else{
			Intent postDetail = new Intent(Search.this, PostDetailActivity.class);
			postDetail.putExtra("PostId", id);
			Search.this.startActivity(postDetail);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	    if(fragmentUI != null){
			outState.putInt("PostId", fragmentUI.getSelectedItem());
		}
	}
	
	@Override
	protected void onDestroy(){
		// This will load all posts.
		Application.getApplicationInstance().getPostsByAuthor(null);

		super.onDestroy();
	}

	@Override
	public boolean isDualPane(){
		return dualPane;
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

}