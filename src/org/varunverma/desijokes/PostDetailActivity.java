package org.varunverma.desijokes;

import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.Post;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

@SuppressLint("SetJavaScriptEnabled")
public class PostDetailActivity extends Activity {

	private WebView wv;
	private AdView adView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.post_detail);

        EasyTracker.getInstance().activityStart(this);
        
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice("E16F3DE5DF824FE222EDDA27A63E2F8A");
        AdView adView = (AdView) findViewById(R.id.adView);
        
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
        	
		// Find the ListView resource.   
		wv = (WebView) findViewById( R.id.webview );
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		wv.setBackgroundColor(Color.TRANSPARENT);
		wv.setBackgroundResource(R.drawable.background);
		
		Intent intent = getIntent();
		int postId = intent.getIntExtra("PostId", 0);
		
		Post post = Application.getApplicationInstance().getPostList().get(postId);
		EasyTracker.getTracker().trackView("/PostTitle/" + post.getTitle());
		wv.loadDataWithBaseURL("fake://not/needed", post.getHTMLCode(), "text/html", "UTF-8", "");
		
	}
	
	@Override
	protected void onDestroy(){
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
}