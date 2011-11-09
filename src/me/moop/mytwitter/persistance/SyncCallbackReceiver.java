package me.moop.mytwitter.persistance;

import android.os.Bundle;

public interface SyncCallbackReceiver {
	
	public void onSyncCallback(int resultCode, Bundle resultData);
	
}
