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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.format.Time;
import android.util.Log;
import android.util.Xml;

public class NoppaSyncAdapter extends AbstractThreadedSyncAdapter {

	ContentResolver contentResolver;
	static final String apiServer = "noppa-api-dev.aalto.fi";
	static final String apiKey = "<removed>";		// Testi avain
	static final String noppaTimeZone = "Europe/Helsinki";
	
	/**
	 * Map of course events in other events list to strings displayed to users.
	 * For the events in the other events list, oOnly event types in the map
	 * are added to the calendar.
	 */
	private static final HashMap<String, String> otherTypeMap = new HashMap<String, String>();
	static {
		// comment out lines to disable event types
		// otherTypeMap.put("event_course", "Course");
		otherTypeMap.put("exams", "Exam");
		otherTypeMap.put("mid_term_exams", "Mid term exam");
		otherTypeMap.put("other", "Other event");
		otherTypeMap.put("seminar", "Seminar");
		otherTypeMap.put("casework", "Case work");
		otherTypeMap.put("demonstration", "Demonstration");
		otherTypeMap.put("group_studies", "Group study");
		otherTypeMap.put("individual_studies", "Individual study");
		otherTypeMap.put("hybrid_studies", "Hybrid study");
		otherTypeMap.put("online_studies", "Online study");
	}


	public NoppaSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		contentResolver = context.getContentResolver();
	}

	public NoppaSyncAdapter(
			Context context,
			boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		contentResolver = context.getContentResolver();
	}

	@Override
	public void onPerformSync(
			Account account,
			Bundle extras,
			String authority,
			ContentProviderClient provider,
			SyncResult result) {
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		Set<String> courses = pref.getStringSet("courses", new HashSet<String>());
		
		boolean show_lectures    = pref.getBoolean("include_lectures", true);
		boolean show_exercises   = pref.getBoolean("include_exercises", true);
		boolean show_assignments = pref.getBoolean("include_assignments", true);
		boolean show_other       = pref.getBoolean("include_other", true);
		
		// TODO: nämä konffattaviksi myös?
//		boolean show_event_course = false;
//		boolean show_exams = true;
//		boolean show_mid_term_exams = true;
//		boolean show_other = true;
//		boolean show_seminar = true;
//		boolean show_casework = true;
//		boolean show_demonstration = true;
//		boolean show_group_studies = true;
//		boolean show_individual_studies = true;
//		boolean show_hybrid_studies = true;
//		boolean show_online_studies = true;
//		boolean show_unknown = false;
		
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
			// Log.d("NoppaSyncAdapter", "content URI = " + calendarUri);
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
			
			if (show_lectures)
				fetchEvents(course, account, calendarId, "lecture", "Lecture");
			if (show_exercises)
				fetchEvents(course, account, calendarId, "exercise", "Exercise");
			if (show_assignments)
				fetchEvents(course, account, calendarId, "assignment", "Assignment");
			if (show_other)
				fetchEvents(course, account, calendarId, "event", otherTypeMap);
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
	
	private void fetchEvents(
			String course,
			Account account, long calendarId,
			String eventTag,
			String typeStr) {
		fetchEvents(course, account, calendarId, eventTag, null, typeStr);
	}
	
	private void fetchEvents(
			String course,
			Account account, long calendarId,
			String eventTag,
			Map<String, String> typeMap) {
		fetchEvents(course, account, calendarId, eventTag, typeMap, null);
	}
	
	private void fetchEvents(
			String course,
			Account account, long calendarId,
			String eventTag,
			Map<String, String> typeMap, String typeStr)
	{
		/*
		 * API request
		 */
		String objName = eventTag + "s";  // e.g. lecture has main tag lectures
		
		URL url = null;
		InputStream in = null;
		try {
			url = new URL("http://" + apiServer + "/api/v1/courses/" + course +
					"/" + objName + ".xml?key=" + apiKey);
		} catch (MalformedURLException e) {
			Log.d("NoppaSyncAdapter", "MalformedURLException " + e.getMessage());
			return;
		}

		HttpURLConnection urlConnection = null;
		try {
			Log.d("NoppaSyncAdapter", "HTTP Get API request, url: " + url.toString());
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream());
			
		} catch (IOException e) {
			Log.d("NoppaSyncAdapter", "IOException " + e.getMessage());
			return;
		}
		
		/*
		 * Parse the XML response.
		 */
		String title=null, type=null, content=null, location=null, group=null,
				start_time=null, end_time=null, start_date=null, end_date=null;
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			// Log.d("NoppaSyncAdapter", parser.getName());
			
			parser.require(XmlPullParser.START_TAG, null, objName);
			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.END_TAG) {
					
					/*
					 * List item end reached.
					 * => Add event to calendar.
					 */
					
					if (typeStr != null)
						type = typeStr;
					
					if (parser.getName().equals(eventTag) && type != null
							&& start_date != null && end_date != null
							&& start_time != null && end_time != null) {
						
						ContentValues val = new ContentValues();
						long start = 0, end = 0;
						try {
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							start = df.parse(start_date + " " + start_time).getTime();
							end = df.parse(end_date + " " + end_time).getTime();
							
							StringBuilder calTitle = new StringBuilder(course + " - ");
							StringBuilder calDesc = new StringBuilder();
							if (content != null && ! content.isEmpty()) {
								calDesc.append(content);
							}
							
							// handle different event types
							if (eventTag.equals("lecture")) {
								calTitle.append(type);
								
								if (title != null && ! title.isEmpty()) {
									if (calDesc.length() != 0) {
										calDesc.insert(0, "\n");
									}
									calDesc.insert(0, title);
								}
							}
							else if (eventTag.equals("exercise")) {
								calTitle.append(type);
								if (group != null && ! group.isEmpty())
									calTitle.append(" (group " + group + ")");
							}
							else if (eventTag.equals("assignment") ||
									eventTag.equals("event")) {
								if (title != null && ! title.isEmpty()) {
									calTitle.append(title + " - ");
								}
								calTitle.append(type);
							}
							
							val.put(Events.TITLE, calTitle.toString());
							val.put(Events.DTSTART, start);
							val.put(Events.DTEND, end);
							val.put(Events.DESCRIPTION, calDesc.toString());
							val.put(Events.EVENT_LOCATION, location);
							val.put(Events.EVENT_TIMEZONE, noppaTimeZone);
							val.put(Events.CALENDAR_ID, calendarId);
							contentResolver.insert(
									NoppaSyncAdapter.asSyncAdapter(Events.CONTENT_URI , account),
									val);
							
							// Log.d("NoppaSyncAdapter", "event: " + val.toString());
						} catch (ParseException e) {
							Log.d("NoppaSyncAdapter", "ParseException " + e.getMessage());
							Log.d("NoppaSyncAdapter", title + " " + location + " " + content + " " + start_date + " " + start_time + " " + end_date + " " + end_time);
						}
					}
				}
				else if (parser.getEventType() == XmlPullParser.START_TAG) {
					
					String name = parser.getName();
					// Log.d("NoppaSyncAdapter", "<" + parser.getName() + ">");
					
					
					if (name.equals(eventTag)) {
						title = type = content = location = group =
								start_time = end_time = start_date = end_date = null;
					}
					else if (name.equals("title")) {
						title = extractXMLtagContent(parser, "title");
					}
					else if (name.equals("type") && typeMap != null) {
						String typeVal = extractXMLtagContent(parser, "type");
						// if map != null and type not in map, this event is ignored
						type = typeMap.get(typeVal);
					}
					else if (name.equals("location")) {
						location = extractXMLtagContent(parser, "location");
					}
					else if (name.equals("content")) {
						content = extractXMLtagContent(parser, "content");
					}
					else if (name.equals("additional_info")) {
						content += "\n" + extractXMLtagContent(parser, "additional_info");
					}
					else if (name.equals("start_date")) {
						start_date = extractXMLtagContent(parser, "start_date");
					}
					else if (name.equals("end_date")) {
						end_date = extractXMLtagContent(parser, "end_date");
					}
					else if (name.equals("start_time")) {
						start_time = extractXMLtagContent(parser, "start_time");
					}
					else if (name.equals("end_time")) {
						end_time = extractXMLtagContent(parser, "end_time");
					}
					else if (name.equals("date")) {
						start_date = end_date = extractXMLtagContent(parser, "date");
					}
					else if (name.equals("deadline")) {
						String t = extractXMLtagContent(parser, "deadline");
						String[] parts = t.split("[tT]", 2);
						if (parts.length == 2) {
							start_date = end_date = parts[0];
							start_time = end_time = parts[1];
						}
					}
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
	}
}
