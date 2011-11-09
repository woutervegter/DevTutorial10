package me.moop.mytwitter.persistance;

import java.sql.SQLException;

import me.moop.mytwitter.model.Tweet;
import me.moop.mytwitter.model.TwitterUser;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	static final String DATABASE_NAME = "mytwitter.sqlite";
	static final int DATABASE_VERSION = 4;

	Dao<TwitterUser, String> mTwitterUsersDao;
	Dao<Tweet, Long> mTweetsDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, TwitterUser.class);
			TableUtils.createTable(connectionSource, Tweet.class);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, TwitterUser.class, true);
			TableUtils.dropTable(connectionSource, Tweet.class, true);
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Dao<Tweet, Long> getTweetsDao() throws SQLException {
		if (mTweetsDao == null) {
			mTweetsDao = getDao(Tweet.class);
		}
		return mTweetsDao;
	}
	
	public Dao<TwitterUser, String> getTwitterUsersDao() throws SQLException {
		if (mTwitterUsersDao == null) {
			mTwitterUsersDao = getDao(TwitterUser.class);
		}
		return mTwitterUsersDao;
	}

	@Override
	public void close() {
		super.close();
		mTwitterUsersDao = null;
		mTweetsDao = null;
	}
}

