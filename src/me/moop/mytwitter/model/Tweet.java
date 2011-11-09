package me.moop.mytwitter.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Tweet {

	@DatabaseField(id = true)
	long mId;
	@DatabaseField(foreign = true)
	TwitterUser mTwitterUser;
	
	@DatabaseField
	Date mCreatedAt;
	
	@DatabaseField
	String mText;
	
	static SimpleDateFormat sEnglishSimpleDateFormat;
	
	@SuppressWarnings("unused")
	private Tweet(){
	}

	public Tweet(JSONObject jSONObject){
		String dateString = jSONObject.optString("created_at");

			if (sEnglishSimpleDateFormat == null){
				Locale englishLocale = Locale.ENGLISH;
				sEnglishSimpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", englishLocale);
			}
		
		try {
			mCreatedAt = sEnglishSimpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		mText = jSONObject.optString("text");
		mId = jSONObject.optLong("id");
	}

	public Date getCreatedAt(){
		return mCreatedAt;
	}

	public String getText(){
		return mText;
	}
	
	public long getId(){
		return mId;
	}
	
	public TwitterUser getTwitterUser(){
		return mTwitterUser;
	}
	
	public void setTwitterUser(TwitterUser twitterUser){
		mTwitterUser = twitterUser;
	}
}