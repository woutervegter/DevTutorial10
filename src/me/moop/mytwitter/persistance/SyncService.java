package me.moop.mytwitter.persistance;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;

import me.moop.mytwitter.model.Tweet;
import me.moop.mytwitter.model.TwitterUser;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

public class SyncService extends IntentService {

	public static final String ACTION = "action";
	public static final String USERNAME = "username";
	public static final String RECEIVER = "resultreceiver";

	public static final int ACTION_TWITTERUSER = 1;
	public static final int ACTION_TWEETS = 2;

	DefaultHttpClient mHttpClient;

	private DatabaseHelper mDatabaseHelper;

	public SyncService() {
		super("SyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (mHttpClient == null){
			mHttpClient = createHttpClient();
		}

		String userName = intent.getStringExtra(USERNAME);
		int action = intent.getIntExtra(ACTION, 0);
		int result = download(userName, action);

		ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
		receiver.send(result, null);
	}

	private int download(String userName, int action) {
		int statusCode = 0;

		String encodedUserName = "";
		try {
			encodedUserName = URLEncoder.encode(userName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String fetchUrl = "";

		switch (action) {
		case ACTION_TWITTERUSER:
			fetchUrl = "http://api.twitter.com/1/users/show.json?screen_name=" + encodedUserName;
			break;
		case ACTION_TWEETS:
			fetchUrl = "http://api.twitter.com/1/statuses/user_timeline.json?include_rts=true&count=30&screen_name=" + encodedUserName;
			break;
		}

		DefaultHttpClient httpclient = createHttpClient();
		HttpGet httpget = new HttpGet(fetchUrl);

		try {
			HttpResponse response = httpclient.execute(httpget);
			StatusLine statusLine = response.getStatusLine();
			statusCode = statusLine.getStatusCode();

			if (statusCode == SyncResultReceiver.RESULT_OK) {
				String resultString = EntityUtils.toString(response.getEntity());

				switch (action) {
				case ACTION_TWITTERUSER:
					saveTwitterUser(resultString);
					break;
				case ACTION_TWEETS:
					saveTweets(resultString, userName);
					break;
				}
			}
			return statusCode;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return SyncResultReceiver.RESULT_ERROR;
		} catch (IOException e) {
			e.printStackTrace();
			return SyncResultReceiver.RESULT_ERROR;
		} catch (SQLException e) {
			e.printStackTrace();
			return SyncResultReceiver.RESULT_ERROR;
		} catch (JSONException e) {
			e.printStackTrace();
			return SyncResultReceiver.RESULT_ERROR;
		}
	}

	private void saveTweets(String resultString, String userName) throws SQLException, JSONException {
		Dao<TwitterUser, String> twitterUsersDao = getDatabaseHelper().getTwitterUsersDao();
		Dao<Tweet, Long> tweetsDao = getDatabaseHelper().getTweetsDao();

		TwitterUser twitterUser = twitterUsersDao.queryForId(userName.toLowerCase());
		tweetsDao.delete(twitterUser.getTweets());

		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		JSONArray jSONArray = new JSONArray(resultString);
		for (int counter = 0; counter < jSONArray.length(); counter++) {
			JSONObject jSONObject = jSONArray.getJSONObject(counter);
			Tweet tweet = new Tweet(jSONObject);
			tweets.add(tweet);

			tweet.setTwitterUser(twitterUser);
			tweetsDao.create(tweet);
		}

		twitterUser.setLastTweetsUpdate(System.currentTimeMillis());
		twitterUsersDao.update(twitterUser);
	}

	private void saveTwitterUser(String resultString) throws SQLException, JSONException{
		JSONObject jSONObject = new JSONObject(resultString);
		TwitterUser twitterUser = new TwitterUser(jSONObject);
		Dao<TwitterUser, String> twitterUsersDao = getDatabaseHelper().getTwitterUsersDao();
		twitterUsersDao.createOrUpdate(twitterUser);
	}

	private DefaultHttpClient createHttpClient() {
		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, 3000);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		ThreadSafeClientConnManager multiThreadedConnectionManager = new ThreadSafeClientConnManager(my_httpParams, registry);
		DefaultHttpClient httpclient = new DefaultHttpClient(multiThreadedConnectionManager, my_httpParams);
		return httpclient;
	}

	@Override
	public void onDestroy() {
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
