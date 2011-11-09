package me.moop.mytwitter.persistance;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class SyncResultReceiver extends ResultReceiver {
	
	public static final int RESULT_OK = 200;
	public static final int RESULT_UNAUTHORIZED = 401;
	public static final int RESULT_DOES_NOT_EXIST = 404;
	public static final int RESULT_ERROR = 0;

	SyncCallbackReceiver mSyncCallbackReceiver;
	
	public SyncResultReceiver() {
		super(new Handler());
	}
	
	public void setSyncCallbackReceiver(SyncCallbackReceiver syncCallbackReceiver){
		mSyncCallbackReceiver = syncCallbackReceiver;
	}
	
	public void onReceiveResult(int resultCode, Bundle resultData){
		if (mSyncCallbackReceiver != null){
			mSyncCallbackReceiver.onSyncCallback(resultCode, resultData);
		}
	}


}
