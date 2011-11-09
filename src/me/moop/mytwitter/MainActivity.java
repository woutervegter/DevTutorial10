package me.moop.mytwitter;

import java.sql.SQLException;
import java.util.List;

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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SyncCallbackReceiver {

	Button mBtnDownload;
	AutoCompleteTextView mActxtUsername;
	ProgressDialog mProgressDialog;

	TextView mTxtvUserNameTitle;
	TextView mTxtvUserName;
	TextView mTxtvUrlTitle;
	TextView mTxtvUrl;
	TextView mTxtvFavouritesCountTitle;
	TextView mTxtvFavouritesCount;
	TextView mTxtvDescriptionTitle;
	TextView mTxtvDescription;
	Button mBtnTweets;

	TwitterUser mTwitterUser;
	DatabaseHelper mDatabaseHelper;
	private String mUsername;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.nicelayout);

		mBtnDownload = (Button) findViewById(R.id.btnDownload);
		mActxtUsername = (AutoCompleteTextView) findViewById(R.id.actxtvUsername);

		mTxtvUserNameTitle = (TextView) findViewById(R.id.txtvUserNameTitle);
		mTxtvUserName = (TextView) findViewById(R.id.txtvUserName); 
		mTxtvUrlTitle = (TextView) findViewById(R.id.txtvUrlTitle); 
		mTxtvUrl = (TextView) findViewById(R.id.txtvUrl);

		mTxtvFavouritesCountTitle = (TextView) findViewById(R.id.txtvFavouritesCountTitle);
		mTxtvFavouritesCount = (TextView) findViewById(R.id.txtvFavouritesCount); 
		mTxtvDescriptionTitle = (TextView) findViewById(R.id.txtvDescriptionTitle); 
		mTxtvDescription = (TextView) findViewById(R.id.txtvDescription);

		mBtnTweets = (Button) findViewById(R.id.btnTweets);

		updateView();
	}

	public void downloadUserInfo(View view){
		if (view == mBtnDownload){
			mUsername = mActxtUsername.getText().toString();
			if (mUsername.length() > 0){
				downloadOrShowFromDb();
			}
			else{
				Toast.makeText(this, "Voer een twitter gebruikersnaam in", Toast.LENGTH_LONG).show();
			}
		}
	}

	public void showTweets(View view){
		if (view == mBtnTweets){
			Intent intent = new Intent(this, TweetsActivity.class);
			intent.putExtra("twitter_user_name", mTwitterUser.getUserName());
			startActivity(intent);
		}
	}

	private void updateView(){

		Dao<TwitterUser, String> twitterUsersDao;
		try {
			twitterUsersDao = getDatabaseHelper().getTwitterUsersDao();
			List<TwitterUser> twitterUsers = twitterUsersDao.queryForAll();
			ArrayAdapter<TwitterUser> adapter = new ArrayAdapter<TwitterUser>(this, android.R.layout.simple_list_item_1, twitterUsers);
			mActxtUsername.setAdapter(adapter);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (mTwitterUser == null){
			mTxtvUrlTitle.setVisibility(View.INVISIBLE);
			mTxtvUrl.setVisibility(View.INVISIBLE);
			mTxtvUserNameTitle.setVisibility(View.INVISIBLE);
			mTxtvUserName.setVisibility(View.INVISIBLE);

			mTxtvFavouritesCountTitle.setVisibility(View.INVISIBLE);
			mTxtvFavouritesCount.setVisibility(View.INVISIBLE);
			mTxtvDescriptionTitle.setVisibility(View.INVISIBLE);
			mTxtvDescription.setVisibility(View.INVISIBLE);

			mBtnTweets.setVisibility(View.INVISIBLE);
		}
		else {
			mTxtvUrlTitle.setVisibility(View.VISIBLE);
			mTxtvUrl.setVisibility(View.VISIBLE);
			mTxtvUserNameTitle.setVisibility(View.VISIBLE);
			mTxtvUserName.setVisibility(View.VISIBLE);

			mTxtvFavouritesCountTitle.setVisibility(View.VISIBLE);
			mTxtvFavouritesCount.setVisibility(View.VISIBLE);
			mTxtvDescriptionTitle.setVisibility(View.VISIBLE);
			mTxtvDescription.setVisibility(View.VISIBLE);

			mBtnTweets.setVisibility(View.VISIBLE);

			mTxtvUrl.setText(mTwitterUser.getWebsite());
			mTxtvUserName.setText(mTwitterUser.getUserName());
			mTxtvFavouritesCount.setText(mTwitterUser.getFavouritesCount() + "");
			mTxtvDescription.setText(mTwitterUser.getDescription());
		}
	}

	private void downloadOrShowFromDb() {
		TwitterUser twitterUser = null;
		try {
			Dao<TwitterUser, String> twitterUsersDao = getDatabaseHelper().getTwitterUsersDao();
			twitterUser = twitterUsersDao.queryForId(mUsername.toLowerCase());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (twitterUser != null && System.currentTimeMillis() - twitterUser.getLastUpdate() < 1000*60*1){
			mTwitterUser = twitterUser;
			updateView();
		}
		else {
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
		intent.putExtra(SyncService.USERNAME, mUsername);
		intent.putExtra(SyncService.ACTION, SyncService.ACTION_TWITTERUSER);
		startService(intent);
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

	public void onSyncCallback(int resultCode, Bundle resultData) {
		mProgressDialog.dismiss();
		switch(resultCode){
		case SyncResultReceiver.RESULT_OK:
			downloadOrShowFromDb();
			break;
		case SyncResultReceiver.RESULT_DOES_NOT_EXIST:
			Toast.makeText(this, "De gevraagde gebruiker bestaat niet.", Toast.LENGTH_LONG).show();
			break;
		case SyncResultReceiver.RESULT_ERROR:
			Toast.makeText(this, "Gegevens konden niet worden opgehaald. Controleer uw internetverbinding en probeer het opnieuw." , Toast.LENGTH_LONG).show();
			break;
		default:
			Toast.makeText(this, "Er is in verbindingsfout opgetreden met foutcode " + resultCode, Toast.LENGTH_LONG).show();
			break;
		}
	}

}
