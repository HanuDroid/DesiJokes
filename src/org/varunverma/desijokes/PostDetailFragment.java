package org.varunverma.desijokes;

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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

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
		
		if(getArguments() != null){
			if (getArguments().containsKey("PostId")) {
	        	int index = getArguments().getInt("PostId");
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
			html = post.getHTMLCode();
		}
		
		wv.loadDataWithBaseURL("fake://not/needed", html, "text/html", "UTF-8", "");
		
	}


	@Override
	public void swipeLeft() {
		// Show Next
		if (position == app.getPostList().size() - 1) {
			position = 0;
		} else {
			position++;
		}
		post = app.getPostList().get(position);
		showPost();
		
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
		post = app.getPostList().get(position);
		showPost();
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
		
		public void loadPosts(String t, String n){
			activity.loadPostsByCategory(t, n);
		}		
	}
}