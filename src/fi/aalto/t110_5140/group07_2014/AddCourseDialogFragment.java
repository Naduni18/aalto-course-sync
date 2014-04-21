package fi.aalto.t110_5140.group07_2014;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import fi.aalto.t110_5140.group07_2014.noppacalendarsync.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

public class AddCourseDialogFragment extends DialogFragment {

	private static class Course {
		public String id;
		public String name;
		
		public Course(String id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public String toString() {
			return id + " - " + name;
		}
	}
	
	private class Searcher implements Runnable {
		
		@Override
		public void run() {
			progressBar.post(new Runnable() {
				@Override
				public void run() {
					progressBar.setVisibility(View.VISIBLE);
				}
			});
			
			String searchStr = searchField.getText().toString();
			
			/*
			 * API request
			 */
			URL url = null;
			try {
				url = new URL("http://" + NoppaSyncAdapter.apiServer +
						"/api/v1/courses.xml?key=" + NoppaSyncAdapter.apiKey +
						"&search=" + Uri.encode(searchStr));
			} catch (MalformedURLException e) {
				Log.d("AddCourseDialogFragment", "MalformedURLException " + e.getMessage());
			}
			
			InputStream in = null;
			HttpURLConnection urlConnection = null;
			try {
				urlConnection = (HttpURLConnection) url.openConnection();
				in = new BufferedInputStream(urlConnection.getInputStream());
			} catch (IOException e) {
				Log.d("AddCourseDialogFragment", "IOException " + e.getMessage());
			}
			
			/*
			 * Parse the XML response.
			 */
			String course_id = null, course_name = null;
			try {
				XmlPullParser parser = Xml.newPullParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in, null);
				parser.nextTag();
				
				parser.require(XmlPullParser.START_TAG, null, "courses");
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					if (parser.getEventType() == XmlPullParser.START_TAG && parser.getEventType() == XmlPullParser.END_TAG) {
						continue;
					}
					if (parser.getEventType() != XmlPullParser.START_TAG) {
						continue;
					}
					String name = parser.getName();
					// Log.d("NoppaSyncAdapter", "<" + parser.getName() + ">");
	
					if (name.equals("course")) {
						course_id = null;
						course_name = null;
					}
					else if (name.equals("course_id")) {
						course_id = NoppaSyncAdapter.extractXMLtagContent(parser, "course_id");
					}
					else if (name.equals("name")) {
						course_name = NoppaSyncAdapter.extractXMLtagContent(parser, "name");
					}
					
					if (course_id != null && course_name != null) {
						courses.add(new Course(course_id, course_name));
					}
				}
	    
			} catch (XmlPullParserException e) {
				Log.d("AddCourseDialogFragment", "XMLParserError " + e.getMessage());
			} catch (IOException e) {
				Log.d("AddCourseDialogFragment", "IOException  " + e.getMessage());
			} finally {
				try {
					in.close();
					urlConnection.disconnect();
				} catch (IOException e) {
					Log.d("AddCourseDialogFragment", "IOException  " + e.getMessage());
				}
			}
			
			progressBar.post(new Runnable() {
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
			
					progressBar.setVisibility(View.GONE);
				}
			});
		}
	}

	private EditText searchField;
	private ProgressBar progressBar;
	private ListView courseListView;
	private ArrayList<Course> courses;
	ArrayAdapter<Course> adapter;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.course_codes_add, null);
		
		courses = new ArrayList<Course>();
		
		searchField = (EditText) view.findViewById(R.id.searchField);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		courseListView = (ListView) view.findViewById(R.id.listView);
		((Button) view.findViewById(R.id.searchButton)).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						new Thread(new Searcher()).start();
					}
				});
		
		adapter = new ArrayAdapter<Course>(
				view.getContext(),
				android.R.layout.simple_list_item_multiple_choice,
				courses );
		courseListView.setAdapter(adapter);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.add_courses);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {}
		});
		
		return builder.create();
	}

}
