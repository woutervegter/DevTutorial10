package me.moop.mytwitter.model;

import java.util.Collection;


import org.json.JSONObject;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class TwitterUser {
	
	@DatabaseField(id=true)
	String mId;
	@DatabaseField
	long mLastUpdate = 0;
	@DatabaseField
	long mLastTweetsUpdate = 0;
	@ForeignCollectionField
	ForeignCollection<Tweet> mTweets;
	
	@DatabaseField
	String mWebsite;
	@DatabaseField
	String mDescription;
	@DatabaseField
	int mFavouritesCount;
	@DatabaseField
	String mUserName;
	
	@SuppressWarnings("unused")
	private TwitterUser(){
	}
	
	public TwitterUser(JSONObject jSONObject){
		mUserName = jSONObject.optString("screen_name");
		mId = mUserName.toLowerCase();
		mWebsite = jSONObject.optString("url");
		mDescription = jSONObject.optString("description");
		mFavouritesCount = jSONObject.optInt("favourites_count");
		mLastUpdate = System.currentTimeMillis();
	}
	
	public String getId(){
		return mId;
	}

	public String getUserName(){
		return mUserName;
	}

	public String getWebsite(){
		return mWebsite;
	}
	
	public String getDescription(){
		return mDescription;
	}
	
	public int getFavouritesCount(){
		return mFavouritesCount;
	}
	
	public long getLastUpdate(){
		return mLastUpdate;
	}

	public long getLastTweetsUpdate() {
		return mLastTweetsUpdate;
	}

	public void setLastTweetsUpdate(long lastTweetsUpdate) {
		mLastTweetsUpdate = lastTweetsUpdate;
	}

	public void setTweets(ForeignCollection<Tweet> tweets) {
		mTweets = tweets;
	}

	public void addTweets(Collection<Tweet> tweets) {
		mTweets.addAll(tweets);
	}
	
	public ForeignCollection<Tweet> getTweets(){
		return mTweets;
	}
	
	@Override
	public String toString(){
		return mUserName;
	}
	
	
}
