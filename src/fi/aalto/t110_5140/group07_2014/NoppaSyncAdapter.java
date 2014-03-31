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
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.format.Time;
import android.util.Log;

public class NoppaSyncAdapter extends AbstractThreadedSyncAdapter {

	ContentResolver contentResolver;

	public NoppaSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		Log.d("NoppaSyncAdapter", "constructor 1");
		contentResolver = context.getContentResolver();
	}

	public NoppaSyncAdapter(
			Context context,
			boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		Log.d("NoppaSyncAdapter", "constructor 2");
		contentResolver = context.getContentResolver();
	}

	@Override
	public void onPerformSync(
			Account account,
			Bundle extras,
			String authority,
			ContentProviderClient provider,
			SyncResult result) {
		
		Log.d("NoppaSyncAdapter", "onPerformSync(...)");
		
		Uri calendarUri;
		long calendarId = -1;
		// get calendar id
		Cursor c = contentResolver.query(
				Calendars.CONTENT_URI,
				new String[]{ Calendars._ID },
				Calendars.NAME + "=?",
				new String[]{ "testikalenteri" },
				null);
		// uncomment to delete events:
//		while (c.moveToNext()) {
//			Log.i("NoppaSyncAdapter",
//					"" +
//					contentResolver.delete(
//							Events.CONTENT_URI,
//							Events.CALENDAR_ID + "=?",
//							new String[]{ String.valueOf(c.getLong(0)) }) +
//					" events deleted");
//			return;
//		}
		if (c != null && c.moveToNext()) {
			calendarId = c.getLong(0);
			calendarUri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calendarId);
			Log.d("NoppaSyncAdapter", "content URI = " + calendarUri);
		}
		else {
			// add calendar if not present
			ContentValues val = new ContentValues();
			val.put(Calendars.ACCOUNT_NAME, account.name);
			val.put(Calendars.ACCOUNT_TYPE, account.type);
			val.put(Calendars.NAME, "testikalenteri");
			val.put(Calendars.CALENDAR_DISPLAY_NAME, "Testikalenteri");
			val.put(Calendars.CALENDAR_COLOR, 0x777777);
			val.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_READ);
			val.put(Calendars.OWNER_ACCOUNT, account.name);
			val.put(Calendars.SYNC_EVENTS, true);
			calendarUri = contentResolver.insert(
					NoppaSyncAdapter.asSyncAdapter(Calendars.CONTENT_URI, account),
					val);
			calendarId = ContentUris.parseId(calendarUri);
		}
		calendarUri = NoppaSyncAdapter.asSyncAdapter(calendarUri, account);
		
		// delete old events
		Log.i("NoppaSyncAdapter",
				"" +
				contentResolver.delete(
						Events.CONTENT_URI,
						Events.CALENDAR_ID + "=?",
						new String[]{ String.valueOf(calendarId) }) +
				" old events deleted");
		
		// add a new event
		ContentValues val = new ContentValues();
		long start = System.currentTimeMillis() + 10*60*60*1000;
		val.put(Events.TITLE, "nyt + 10h");
		val.put(Events.DTSTART, start);
		val.put(Events.DTEND, start + 30*60*1000); // +30min
		val.put(Events.EVENT_TIMEZONE, Time.getCurrentTimezone());
		val.put(Events.CALENDAR_ID, calendarId);
		contentResolver.insert(
				NoppaSyncAdapter.asSyncAdapter(Events.CONTENT_URI, account),
				val);
		
		// uncomment to delete calendars created by this program:
//		Log.i("NoppaSyncAdapter",
//				"" +
//				contentResolver.delete(
//						Calendars.CONTENT_URI,
//						Calendars.NAME + "=?", new String[]{ "testikalenteri" }) +
//				" calendars deleted");
	}
	
	private static Uri asSyncAdapter(Uri uri, Account account) {
		return uri.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, account.name)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, account.type)
				.build();
	}

}
