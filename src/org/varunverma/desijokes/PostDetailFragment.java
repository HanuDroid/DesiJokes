package org.varunverma.desijokes;

import java.text.SimpleDateFormat;

import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuFragmentInterface;
import org.varunverma.hanu.Application.HanuGestureAnalyzer;
import org.varunverma.hanu.Application.HanuGestureListener;
import org.varunverma.hanu.Application.Post;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

@SuppressLint("SetJavaScriptEnabled")
public class PostDetailFragment extends Fragment implements HanuFragmentInterface, HanuGestureListener{

	private Post post;
	private WebView wv;
	private Callbacks activity = sDummyCallbacks;
	private int position;
	private Application app;
	
	public interface Callbacks {
		public void loadPostsByCategory(String taxonomy, String name);
		public boolean isDualPane();
	}
	
	private static Callbacks sDummyCallbacks = new Callbacks() {

		@Override
		public void loadPostsByCategory(String taxonomy, String name) {			
		}

		@Override
		public boolean isDualPane() {
			return false;
		}
		
    };
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		app = Application.getApplicationInstance();
		
		if(app.getPostList().isEmpty()){
			return;
		}
		
		if(getArguments() != null){
			if (getArguments().containsKey("PostId")) {
				int index = getArguments().getInt("PostId");
	        	if(index >= app.getPostList().size()){
	        		index = app.getPostList().size();
	        	}
	            post = app.getPostList().get(index);
	        }
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.post_detail, container, false);
		
		wv = (WebView) rootView.findViewById(R.id.webview);
		
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new PostJavaScriptInterface(), "Main");
		
		// Fling handling
		if(!activity.isDualPane()){
			
			wv.setBackgroundColor(Color.TRANSPARENT);
			wv.setBackgroundResource(R.drawable.background);
			
			final GestureDetector detector = new GestureDetector(getActivity().getApplicationContext(), new HanuGestureAnalyzer(this));
			wv.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View view, MotionEvent e) {
					detector.onTouchEvent(e);
					return false;
				}
			});
		}
		
		showPost();
		
		AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice("E16F3DE5DF824FE222EDDA27A63E2F8A");
        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
		
		return rootView;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        this.activity = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = sDummyCallbacks;
    }
    
	@Override
	public void reloadUI() {
		// Reloading the UI
		post = app.getPostList().get(0);	
	}

	@Override
	public int getSelectedItem() {
		return position;
	}

	private void showPost() {
		
		String html = "";
		if(post != null){
			html = getHTMLCode(post);
			EasyTracker.getTracker().trackView("/Post/" + post.getTitle());
		}
		wv.loadDataWithBaseURL("fake://not/needed", html, "text/html", "UTF-8", "");
		
	}
	
	private String getHTMLCode(Post post) {
		
		SimpleDateFormat df = new SimpleDateFormat();
		
		String html = "<HTML>" +

				// HTML Head
				"<head>" +

				// Java Script
				"<script type=\"text/javascript\">function loadPosts(taxonomy,name)" +
				"{Main.loadPosts(taxonomy,name);}</script>" +

				// CSS
				"<style>" + 
				"h3 {color:blue;font-family:arial,helvetica,sans-serif;}" +
				"#pub_date {color:black;font-family:verdana,geneva,sans-serif;font-size:14px;}" +
				"#content {color:black;font-family:arial,helvetica,sans-serif; font-size:18px;}" +
				".taxonomy {color:black;font-family:arial,helvetica,sans-serif; font-size:14px;}" +
				"#comments {color:black;font-family:arial,helvetica,sans-serif; font-size:16px;}" +
				"#ratings {color:black; font-family:verdana,geneva,sans-serif; font-size:14px;}" +
				"#footer {color:#0000ff; font-family:verdana,geneva,sans-serif; font-size:14px;}"+
				"</style>" +

				"</head>" +

				// HTML Body
				"<body>" +

				// Heading
				"<h3>" + post.getTitle() + "</h3>" +

				// Pub Date
				"<div id=\"pub_date\">" + df.format(post.getPublishDate()) + "</div>" +
				"<hr />" +

				// Content
				"<div id=\"content\">" + post.getContent(false) + "</div>" +
				"<hr />" +
				
				// Author
				"<div class=\"taxonomy\">" +
				"by <a href=\"javascript:loadPosts('author','" + post.getAuthor() + "')\">" + post.getAuthor() + "</a>" +
				"</div>";

		// Ratings
		if (post.getMetaData().size() > 0
				&& !post.getMetaData().get("ratings_users").contentEquals("0")) {
			// We have some ratings !
			html = html + "<div id=\"ratings\">" + "<br>Rating: "
					+ String.format("%.2g%n", Float.valueOf(post.getMetaData().get("ratings_average")))
					+ " / 5 (by " + post.getMetaData().get("ratings_users") + " users)";

			html = html + "</div>";
		}

		// Footer
		html = html + "<br /><hr />" + "<div id=\"footer\">" 
				+ "Powered by <a href=\"http://hanu-droid.varunverma.org\">Hanu-Droid framework</a>"
				+ "</div>" +

				"</body>" +
				"</html>";

		return html;
		
	}

	@Override
	public void swipeLeft() {
		// Show Next
		if (position == app.getPostList().size() - 1) {
			position = 0;
		} else {
			position++;
		}
		
		try{
			post = app.getPostList().get(position);
			showPost();
		}catch(Exception e){
			Log.e(Application.TAG, e.getMessage(), e);
		}
		
	}

	@Override
	public void swipeRight() {
		// Show Previous
		if(position == 0){
			position = app.getPostList().size() - 1;
		}
		else{
			position--;
		}
		
		try{
			post = app.getPostList().get(position);
			showPost();
		}catch(Exception e){
			Log.e(Application.TAG, e.getMessage(), e);
		}
	}

	@Override
	public void swipeUp() {
		//Nothing to do
	}
	
	@Override
	public void swipeDown() {
		//Nothing to do		
	}

	class PostJavaScriptInterface{
		@JavascriptInterface
		public void loadPosts(String t, String n){
			activity.loadPostsByCategory(t, n);
		}		
	}
}