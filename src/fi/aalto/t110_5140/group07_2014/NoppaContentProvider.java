package fi.aalto.t110_5140.group07_2014;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class NoppaContentProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(Uri arg0) {
		return "";
	}

	@Override
	public Cursor query(
			Uri uri,
			String[] projection,
			String selection,
			String[] selectionArgs,
			String sortOrder) {
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(
			Uri uri,
			ContentValues values,
			String selection,
			String[] selectionArgs) {
		
		return 0;
	}

}
