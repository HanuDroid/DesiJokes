package org.varunverma.desijokes;

import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.CreateNewPostCommand;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class CreateNewPost extends Activity implements OnClickListener, Invoker{

	private ProgressDialog pd;
	private EditText title, content;
	
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.create_post);
		setTitle("Upload new Joke");
		
		// Tracking.
        EasyTracker.getInstance().activityStart(this);
        
        title = (EditText) findViewById(R.id.title);
		content = (EditText) findViewById(R.id.content);
		
		// Initialize Application
		Application app = Application.getApplicationInstance();
		app.setContext(getApplicationContext());
		
		// Get Intent
		Intent intent = getIntent();
		
		String action = intent.getAction();
		String type = intent.getType();
		
		// Filter - to be sure
		if(action != null && action.contentEquals(Intent.ACTION_SEND) && type!=null && type.contains("text")){
			title.setText(intent.getStringExtra(Intent.EXTRA_SUBJECT));
			content.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
		}
		
		Button submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(this);
		
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View view) {
		// On Click of a View
		switch(view.getId()){
		
		case R.id.submit:
			createNewPost();
			break;
			
		case R.id.cancel:
			finish();
			break;
		
		}
		
	}

	private void createNewPost() {
		// Create New Post
		String title = this.title.getEditableText().toString();
		String content = this.content.getEditableText().toString();
		String name = ((EditText) findViewById(R.id.nick_name)).getEditableText().toString();
		
		if(title == null || title.contentEquals("") || content == null || content.contentEquals("")){
			Toast.makeText(this, "Title/Content cannot be empty", Toast.LENGTH_LONG).show();
			return;
		}
		
		CommandExecuter ce = new CommandExecuter();
		CreateNewPostCommand command = new CreateNewPostCommand(this,title,content,name);
		
		pd = ProgressDialog.show(this, "Uploading Joke", "Uploading new joke, please wait...");
		
		ce.execute(command);
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		// Command Execution is success.
		
		pd.dismiss();
		
		if(result.isCommandExecutionSuccess() && result.getResultCode() == 200){
			Toast.makeText(this, "Joke uploaded successfully...", Toast.LENGTH_LONG).show();
			finish();
		}
		else{
			String message = "Following error while uploading joke: " + result.getErrorMessage() + " -- Please try again!";
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
		
	}

	@Override
	public void ProgressUpdate(ProgressInfo progress) {
		// Nothing to do
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}
	
}