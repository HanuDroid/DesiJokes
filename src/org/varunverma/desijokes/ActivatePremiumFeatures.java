/**
 * 
 */
package org.varunverma.desijokes;

import org.varunverma.desijokes.billingutil.IabHelper;
import org.varunverma.desijokes.billingutil.IabHelper.OnIabPurchaseFinishedListener;
import org.varunverma.desijokes.billingutil.IabResult;
import org.varunverma.desijokes.billingutil.Purchase;
import org.varunverma.hanu.Application.Application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author varun
 *
 */
public class ActivatePremiumFeatures extends Activity implements
		OnClickListener, OnIabPurchaseFinishedListener {

	private TextView prodName, prodDesc, prodHelp;
	private Button buy, cancel;
	private IabHelper billingHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.premium_features);
		
		prodName = (TextView) findViewById(R.id.product_name);
		prodDesc = (TextView) findViewById(R.id.product_desc);
		prodHelp = (TextView) findViewById(R.id.product_help);
		
		buy = (Button) findViewById(R.id.buy);
		buy.setOnClickListener(this);
		
		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
				
	}
	
	@Override
	protected void onStart() {
		
		super.onStart();
		
		billingHelper = IabHelper.getInstance();
		
		if(billingHelper == null){
			finish();
		}
		
		prodName.setText(Constants.getProductTitle());
		prodDesc.setText(Constants.getProductDescription());
		buy.setText(Constants.getProductPrice());
		
		showProductHelp();
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
			case android.R.id.home:
				finishActivity(false);
				return true;
				
			default:
	            return false;
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	
	    	finishActivity(false);
	    	return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	private void showProductHelp() {
		
		String help = "By purchasing this premium service, all advertisements from the app will be removed permanently.\n" +
				"This is a one time purchase only.";
		
		prodHelp.setText(help);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.buy:
			buy();
			break;

		case R.id.cancel:
			setResult(RESULT_OK, new Intent());
			finish();
			break;
		}
		
	}

	private void buy() {
		
		String devPayLoad = ""; //Application.get_instance().getEmail();
		billingHelper.launchPurchaseFlow(this, Constants.getProductKey(), 22, this, devPayLoad);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (billingHelper.handleActivityResult(requestCode, resultCode, data)) {
			// Nothing
		} else {
			// Handle
		}

	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		
		if (result.isFailure()) {
			
			String message = "Error purchasing: " + result.getMessage();
			
			Log.d(Application.TAG, message);
			
			showAlertDialog(message, false);
			return;
		}
		
		if(info.getSku().contentEquals(Constants.getProductKey())){
			// Purchase was success
			Constants.setPremiumVersion(true);
			
			String message = "Congratulations. You have access to premium service.\n" +
					"Please restart the app to see the changes";
			
			showAlertDialog(message, true);
		}
		
	}

	private void showAlertDialog(String message, final boolean restartApp) {
		
		// Show alert dialog
		new AlertDialog.Builder(this)
			.setTitle("Purchase Status:")
			.setMessage(message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					// Dismiss the dialog
					dialog.dismiss();
					finishActivity(restartApp);
					
				}
			})
			.show();
		
	}
	
	private void finishActivity(boolean restartApp) {
		
		Intent returnData = new Intent();
		returnData.putExtra("RestartApp", restartApp);
		
		setResult(RESULT_OK, returnData);
		
		// Close this activity
		finish();
		
	}
}