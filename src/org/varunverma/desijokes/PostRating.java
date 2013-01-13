/**
 * 
 */
package org.varunverma.desijokes;

import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.Post;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * @author Varun
 *
 */
public class PostRating extends Activity implements OnRatingBarChangeListener {

	private RatingBar ratingBar;
	Post post;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.rate);
        
        Intent intent = getIntent();
        int postId = intent.getIntExtra("PostId", 0);
        
        try{
        	post = Application.getApplicationInstance().getAllPosts().get(postId);
        }catch (Exception e){
        	finish();
        }
        
        if(post == null){
        	finish();
        }
        
        EasyTracker.getInstance().activityStart(this);
        
        setTitle("Rate this post:");
		
        ratingBar = (RatingBar) findViewById(R.id.ratingbar);
        ratingBar.setNumStars(5);
        ratingBar.setRating(post.getMyRating());
        ratingBar.setOnRatingBarChangeListener(this);
        
	}

	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onRatingChanged(RatingBar view, float rating, boolean fromUser) {
		// Rating was changed
		if(fromUser){
			
			post.addRating(rating);
			
		}
		// Close activity after the rating is finished
		finish();		
	}
	
}