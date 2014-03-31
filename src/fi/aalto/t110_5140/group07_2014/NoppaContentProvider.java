package fi.aalto.t110_5140.group07_2014;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class NoppaContentProvider extends ContentProvider {

	private static final String AUTHORITY =
			"fi.aalto.t110_5140.group07_2014.noppacalendarsync.provider";
	public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/");

	@Override
	public boolean onCreate() {
		Log.d("NoppaContentProvider", "onCreate()");
		return true;
	}

	@Override
	public String getType(Uri uri) {
		Log.d("NoppaContentProvider", "getType(" + uri + ")");
		return null;
	}

	@Override
	public Cursor query(
			Uri uri,
			String[] projection,
			String selection,
			String[] selectionArgs,
			String sortOrder) {
		
		Log.d("NoppaContentProvider", "query("+uri+", ...)");
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d("NoppaContentProvider", "insert("+uri+", "+values+")");
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d("NoppaContentProvider", "delete("+uri+", "+selection+", "+selectionArgs+")");
		return 0;
	}

	@Override
	public int update(
			Uri uri,
			ContentValues values,
			String selection,
			String[] selectionArgs) {
		
		Log.d("NoppaContentProvider", "update("+uri+", ...)");
		return 0;
	}

}
