package fi.aalto.t110_5140.group07_2014;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.format.Time;

public class NoppaSyncAdapter extends AbstractThreadedSyncAdapter {

	ContentResolver mContentResolver;

	public NoppaSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContentResolver = context.getContentResolver();
	}

	public NoppaSyncAdapter(
			Context context,
			boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		mContentResolver = context.getContentResolver();
    }

	@Override
	public void onPerformSync(
			Account account,
			Bundle extras,
			String authority,
			ContentProviderClient provider,
			SyncResult result) {
		
	}
	
}
