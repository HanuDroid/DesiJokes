package org.varunverma.desijokes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class PostDetailActivity extends AppCompatActivity implements
		PostDetailFragment.Callbacks {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();

		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);

		LinearLayout swipeHelpLayout = (LinearLayout) findViewById(R.id.swipe_help);
		swipeHelpLayout.setVisibility(View.GONE);

		// Hide some views
		View postList = findViewById(R.id.post_list);
		if(postList != null){
			postList.setVisibility(View.GONE);
		}

		// Show Ad.
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("9F11CAC92EB404500CAA3F8B0BBA5277").build();

		AdView adView = (AdView) findViewById(R.id.adView);

		// Start loading the ad in the background.
		adView.loadAd(adRequest);

		Intent intent = getIntent();
		int postId = intent.getIntExtra("PostId", 0);

		// Create the Fragment.
		FragmentManager fm = this.getSupportFragmentManager();

		// Create Post List Fragment
		Fragment fragment = new PostDetailFragment();
		Bundle arguments = new Bundle();
		arguments.putInt("PostId", postId);
		fragment.setArguments(arguments);

		fm.beginTransaction().replace(R.id.post_detail, fragment)
				.commitAllowingStateLoss();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void loadPostsByCategory(String taxonomy, String name) {
		// Nothing to do
	}

	@Override
	public boolean isDualPane() {
		return false;
	}
}