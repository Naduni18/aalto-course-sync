package fi.aalto.t110_5140.group07_2014;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
import android.util.Xml;

public class NoppaSyncAdapter extends AbstractThreadedSyncAdapter {

	ContentResolver contentResolver;
	static String apiServer = "noppa-api-dev.aalto.fi";
	static String apiKey = "<removed>";		// Testi avain

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
		
		String[] courses = new String[] {
				"T-86.5310",
				"T-61.5010",
				"T-106.5150",
				"T-111.5360",
				"T-110.6220",
				"T-75.4300",
				"T-75.4400",
				"T-110.5140"
		};
    	boolean show_event_course = false;
    	boolean show_exams = true;
    	boolean show_mid_term_exams = true;
    	boolean show_other = true;
    	boolean show_seminar = true;
    	boolean show_casework = true;
    	boolean show_demonstration = true;
    	boolean show_group_studies = true;
    	boolean show_individual_studies = true;
    	boolean show_hybrid_studies = true;
    	boolean show_online_studies = true;
    	boolean show_unknown = false;
    	
    	String calendarName = "coursecalendar";
    	String calendarDisplayName = "Course calendar";
		
		/*
		 * Create calendar
		 */
		
		Uri calendarUri;
		long calendarId = -1;
		// get calendar id
		Cursor c = contentResolver.query(
				Calendars.CONTENT_URI,
				new String[]{ Calendars._ID },
				Calendars.NAME + "=?",
				new String[]{ calendarName },
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
			val.put(Calendars.NAME, calendarName);
			val.put(Calendars.CALENDAR_DISPLAY_NAME, calendarDisplayName);
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
		
		/*
		 * Fetch events
		 */
		
		for (String course : courses) {

			/*
			 * API request
			 */
			URL url = null;
			InputStream in = null;
			try {
				url = new URL("http://" + apiServer + "/api/v1/courses/" + course + "/events.xml?key=" + apiKey);
			} catch (MalformedURLException e) {
				Log.d("NoppaSyncAdapter", "MalformedURLException " + e.getMessage());
			}
	
			HttpURLConnection urlConnection = null;
			try {
				// Log.d("NoppaSyncAdapter", "HTTP Get API request, url: " + url.toString());
				urlConnection = (HttpURLConnection) url.openConnection();
				in = new BufferedInputStream(urlConnection.getInputStream());
				
			} catch (IOException e) {
				Log.d("NoppaSyncAdapter", "IOException " + e.getMessage());
			}
			
			/*
			 * Parse the XML responce.
			 */
			String title = "", type = "", description = "", location = "", start_time = "", end_time = "", start_date = "", end_date = "";
			try {
				XmlPullParser parser = Xml.newPullParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in, null);
				parser.nextTag();
				// Log.d("NoppaSyncAdapter", parser.getName());
				
				parser.require(XmlPullParser.START_TAG, null, "events");
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					if (parser.getEventType() == XmlPullParser.START_TAG && parser.getEventType() == XmlPullParser.END_TAG) {
						continue;
					}
					if (parser.getEventType() != XmlPullParser.START_TAG) {
						continue;
					}
					String name = parser.getName();
					// Log.d("NoppaSyncAdapter", "<" + parser.getName() + ">");
	
	
					if (name.equals("title")) {
						title = extractXMLtagContent(parser, "title");
					}
					if (name.equals("type")) {
						parser.require(XmlPullParser.START_TAG, null, "type");
					    if (parser.next() == XmlPullParser.TEXT) {
					    	// Log.d("NoppaSyncAdapter", parser.getText());
					    	if ( parser.getText().equals("event_course") ) {
					    		if ( !show_event_course ) { break; }
					    		type = "??";
					    	} else if ( parser.getText().equals("exams") ) {
					    		if ( !show_exams ) { break; }
					    		type = "Exam";
					    	} else if ( parser.getText().equals("mid_term_exams") ) {
					    		if ( !show_mid_term_exams ) { break; }
					    		type = "Mid term exam";
					    	} else if ( parser.getText().equals("other") ) {
					    		if ( !show_other ) { break; }
					    		type = "Other event";
					    	} else if ( parser.getText().equals("seminar") ) {
					    		if ( !show_seminar ) { break; }
					    		type = "Seminar";
					    	} else if ( parser.getText().equals("casework") ) {
					    		if ( !show_casework ) { break; }
					    		type = "Case work";
					    	} else if ( parser.getText().equals("demonstration") ) {
					    		if ( !show_demonstration ) { break; }
					    		type = "Demonstration";
					    	} else if ( parser.getText().equals("group_studies") ) {
					    		if ( !show_group_studies ) { break; }
					    		type = "Group study";
					    	} else if ( parser.getText().equals("individual_studies") ) {
					    		if ( !show_individual_studies ) { break; }
					    		type = "Individual study";
					    	} else if ( parser.getText().equals("hybrid_studies") ) {
					    		if ( !show_hybrid_studies ) { break; }
					    		type = "Hybrid study";
					    	} else if ( parser.getText().equals("online_studies") ) {
					    		if ( !show_online_studies ) { break; }
					    		type = "Online study";
					    	} else {
					    		if ( !show_unknown ) { break; }
					    		type = "???";
					    	}
					        parser.nextTag();
					    }
						parser.require(XmlPullParser.END_TAG, null, "type");
					}
					if (name.equals("location")) {
				    	location = extractXMLtagContent(parser, "location");
					}
					if (name.equals("description")) {
				    	description = extractXMLtagContent(parser, "description");
					}
					if (name.equals("start_date")) {
				    	start_date = extractXMLtagContent(parser, "start_date");
					}
					if (name.equals("end_date")) {
				    	end_date = extractXMLtagContent(parser, "end_date");
					}
					if (name.equals("start_time")) {
				    	start_time = extractXMLtagContent(parser, "start_time");
					}
					if (name.equals("end_time")) {
				    	end_time = extractXMLtagContent(parser, "end_time");
					}
				}
	    
			} catch (XmlPullParserException e) {
				Log.d("NoppaSyncAdapter", "XMLParserError " + e.getMessage());
			} catch (IOException e) {
				Log.d("NoppaSyncAdapter", "IOException  " + e.getMessage());
			} finally {
				try {
					in.close();
					urlConnection.disconnect();
				} catch (IOException e) {
					Log.d("NoppaSyncAdapter", "IOException  " + e.getMessage());
				}
			}
	
	
			/*
			 * Add a event to calendar
			 */
			ContentValues val = new ContentValues();
			long start = 0, end = 0;
			try {
	            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	            start = df.parse(start_date + " " + start_time).getTime();
	            end = df.parse(end_date + " " + end_time).getTime();
	            
				val.put(Events.TITLE, title + " - " + type);
				val.put(Events.DTSTART, start);
				val.put(Events.DTEND, end);
				val.put(Events.DESCRIPTION, description);
				val.put(Events.EVENT_LOCATION, location);
				val.put(Events.EVENT_TIMEZONE, Time.getCurrentTimezone());
				val.put(Events.CALENDAR_ID, calendarId);
				contentResolver.insert(
						NoppaSyncAdapter.asSyncAdapter(Events.CONTENT_URI , account),
						val);
			} catch (ParseException e) {
				Log.d("NoppaSyncAdapter", "ParseException " + e.getMessage());
				Log.d("NoppaSyncAdapter", title + " " + location + " " + description + " " + start_date + " " + start_time + " " + end_date + " " + end_time);
			}
					

		}
		
			// uncomment to delete calendars created by this program:
/*			Log.i("NoppaSyncAdapter",
					"" +
					contentResolver.delete(
							Calendars.CONTENT_URI,
							Calendars.NAME + "=?", new String[]{ calendarName }) +
					" calendars deleted");
*/	
	}
	
	private static Uri asSyncAdapter(Uri uri, Account account) {
		return uri.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, account.name)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, account.type)
				.build();
	}

	static String extractXMLtagContent(XmlPullParser parser, String requiredTag) throws XmlPullParserException, IOException {
		String content = "";
		parser.require(XmlPullParser.START_TAG, null, requiredTag);
	    if (parser.next() == XmlPullParser.TEXT) {
	    	content = android.text.Html.fromHtml(parser.getText()).toString();
	        parser.nextTag();
	    }
		parser.require(XmlPullParser.END_TAG, null, requiredTag);
		return content;
	}
}
