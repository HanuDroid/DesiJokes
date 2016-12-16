/**
 * 
 */
package org.varunverma.desijokes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * @author Varun
 *
 */
public class PostPagerAdapter extends FragmentStatePagerAdapter {
	
	private int size;
	
	public PostPagerAdapter(FragmentManager fm, int size) {
		super(fm);
		this.size = size;
	}


	@Override
	public int getCount() {
		return size;
	}
	
	void setNewSize(int size){
		this.size = size;
	}

	@Override
	public Fragment getItem(int postIndex) {
		
		// Post Id is actually position
		
		Fragment fragment = new PostDetailFragment();
		Bundle arguments = new Bundle();
		arguments.putInt("PostIndex", postIndex);
		fragment.setArguments(arguments);
		
		return fragment;
	}

}
