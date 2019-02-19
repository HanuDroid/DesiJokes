package org.varunverma.desijokes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;
import com.ayansh.hanudroid.Post;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private Application app;
	private int postIndex;
	private PostPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	private DrawerLayout mDrawerLayout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		MobileAds.initialize(this, "ca-app-pub-4571712644338430~3902578709");

		mDrawerLayout = findViewById(R.id.drawer_layout);

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        
        if(savedInstanceState != null){
			postIndex = savedInstanceState.getInt("PostIndex");
        }
        else{
			postIndex = 0;
        }

        // Get Application Instance.
        app = Application.getApplicationInstance();
		app.setContext(getApplicationContext());

		// Start the Main Activity
		startMainScreen();
    }

	private void startMainScreen() {

		// Show Swipe Help
		showSwipeHelp();

		Bundle extras = new Bundle();
		extras.putString("max_ad_content_rating", "MA");

		AdRequest adRequest = new AdRequest.Builder()
				.addNetworkExtrasBundle(AdMobAdapter.class, extras)
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("9F11CAC92EB404500CAA3F8B0BBA5277").build();

		AdView adView = (AdView) findViewById(R.id.adView);

		// Start loading the ad in the background.
		adView.loadAd(adRequest);

		// Request InterstitialAd
		MyInterstitialAd.getInterstitialAd(this);
		MyInterstitialAd.requestNewInterstitial();

		// Load Posts.
		Application.getApplicationInstance().getAllPosts();

		// Create view Pager
		viewPager = (ViewPager) findViewById(R.id.post_pager);

		viewPager.setClipToPadding(false);
		viewPager.setPageMargin(-50);

		pagerAdapter = new PostPagerAdapter(getSupportFragmentManager(),app.getPostList().size());
		viewPager.setAdapter(pagerAdapter);
	}

	private void showSwipeHelp(){

		final LinearLayout swipeHelpLayout = (LinearLayout) findViewById(R.id.swipe_help);

		if(swipeHelpLayout == null){
			return;
		}

		String swipeHelp = app.getOptions().get("SwipeHelp");

		if(swipeHelp != null && swipeHelp.contentEquals("Skip")){
			// Skip the swipe help
			swipeHelpLayout.setVisibility(View.GONE);
		}
		else{

			final CheckBox showHelpAgain = (CheckBox) swipeHelpLayout.findViewById(R.id.show_again);

			Button dismissHelp = (Button) swipeHelpLayout.findViewById(R.id.dismiss_help);
			dismissHelp.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					// Hide the swipe help
					swipeHelpLayout.setVisibility(View.GONE);

					if (showHelpAgain.isChecked()) {
						Application.getApplicationInstance().addParameter("SwipeHelp", "Skip");
					}
				}
			});

		}

	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
    }
    
	@Override
	protected void onDestroy(){
		
		app.close();
		super.onDestroy();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()){

			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				break;

			case R.id.Upload:
				Intent upload = new Intent(Main.this, CreateNewPost.class);
				Main.this.startActivity(upload);
				break;

		}

		return true;
    }

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

		//menuItem.setChecked(true);
		mDrawerLayout.closeDrawers();

		switch (menuItem.getItemId()){

			case R.id.Help:
				Intent help = new Intent(Main.this, DisplayFile.class);
				help.putExtra("File", "help.html");
				help.putExtra("Title", "Help: ");
				Main.this.startActivity(help);
				break;

			case R.id.ShowEula:
				Intent eula = new Intent(Main.this, DisplayFile.class);
				eula.putExtra("File", "eula.html");
				eula.putExtra("Title", "Terms and Conditions: ");
				Main.this.startActivity(eula);
				break;

			case R.id.Settings:
				Intent settings = new Intent(Main.this, SettingsActivity.class);
				Main.this.startActivity(settings);
				break;

			case R.id.About:
				Intent info = new Intent(Main.this, DisplayFile.class);
				info.putExtra("File", "about.html");
				info.putExtra("Title", "About: ");
				Main.this.startActivity(info);
				break;

			case R.id.MyApps:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Ayansh+TechnoSoft+Pvt.+Ltd"));
				startActivity(browserIntent);
				break;
		}

		return true;
	}
}