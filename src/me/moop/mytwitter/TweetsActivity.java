package me.moop.mytwitter;

import java.sql.SQLException;
import java.util.ArrayList;

import me.moop.mytwitter.helper.TweetsListAdapter;
import me.moop.mytwitter.model.Tweet;
import me.moop.mytwitter.model.TwitterUser;
import me.moop.mytwitter.persistance.DatabaseHelper;
import me.moop.mytwitter.persistance.SyncCallbackReceiver;
import me.moop.mytwitter.persistance.SyncResultReceiver;
import me.moop.mytwitter.persistance.SyncService;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TweetsActivity extends Activity implements SyncCallbackReceiver {

	TextView mTxtvTitle;

	ProgressDialog mProgressDialog;
	ArrayList<Tweet> mTweets;
	ListView mLvTweets;

	DatabaseHelper mDatabaseHelper;

	TwitterUser mTwitterUser;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweets);

		mTxtvTitle = (TextView) findViewById(R.id.txtvTitle);
		mLvTweets = (ListView) findViewById(R.id.lvTweets);

		Intent intent = getIntent();
		Bundle extrasBundle = intent.getExtras();
		String userName = extrasBundle.getString("twitter_user_name");
		try {
			Dao<TwitterUser, String> twitterUsersDao = getDatabaseHelper().getTwitterUsersDao(); 
			mTwitterUser = twitterUsersDao.queryForId(userName.toLowerCase());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		updateView();

		downloadOrShowFromDb();
	}

	private void updateView() {
		mTxtvTitle.setText(mTwitterUser.getUserName());

		if (mTweets != null){
			TweetsListAdapter tweetsListAdapter = new TweetsListAdapter(this, mTweets);
			mLvTweets.setAdapter(tweetsListAdapter);
		}
	}

	private void downloadOrShowFromDb() {
		if (System.currentTimeMillis() - mTwitterUser.getLastTweetsUpdate() < 1000*60*1){
			mTweets = new ArrayList<Tweet>();
			mTweets.addAll(mTwitterUser.getTweets());
			updateView();
		} else{
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Bezig met het ophalen van gegevens...");
			mProgressDialog.show();
			callSyncService();
		}
	}

	private void callSyncService() {
		SyncResultReceiver receiver = new SyncResultReceiver();
		receiver.setSyncCallbackReceiver(this);
		
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
		intent.putExtra(SyncService.RECEIVER, receiver);
		intent.putExtra(SyncService.USERNAME, mTwitterUser.getUserName());
		intent.putExtra(SyncService.ACTION, SyncService.ACTION_TWEETS);
		startService(intent);
	}

	public void onSyncCallback(int resultCode, Bundle resultData) {
		mProgressDialog.dismiss();
		switch(resultCode){
		case SyncResultReceiver.RESULT_OK:
			try {
				Dao<TwitterUser, String> twitterUsersDao = getDatabaseHelper().getTwitterUsersDao();
				twitterUsersDao.refresh(mTwitterUser);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			downloadOrShowFromDb();
			break;
		case SyncResultReceiver.RESULT_UNAUTHORIZED:
			Toast.makeText(this, "De timeline van deze gebruiker is niet publiek toegankelijk.", Toast.LENGTH_LONG).show();
			break;
		case SyncResultReceiver.RESULT_ERROR:
			Toast.makeText(this, "Gegevens konden niet worden opgehaald. Controleer uw internetverbinding en probeer het opnieuw." , Toast.LENGTH_LONG).show();
			break;
		default:
			Toast.makeText(this, "Er is in verbindingsfout opgetreden met foutcode " + resultCode, Toast.LENGTH_LONG).show();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDatabaseHelper != null) {
			OpenHelperManager.releaseHelper();
			mDatabaseHelper = null;
		}
	}

	private DatabaseHelper getDatabaseHelper() {
		if (mDatabaseHelper == null) {
			mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return mDatabaseHelper;
	}
}
