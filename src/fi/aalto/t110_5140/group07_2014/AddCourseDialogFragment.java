package fi.aalto.t110_5140.group07_2014;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import fi.aalto.t110_5140.group07_2014.noppacalendarsync.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

public class AddCourseDialogFragment extends DialogFragment {

	private static class Course implements Comparable<Course> {
		public String id;
		public String name;
		public boolean checked = false;
		
		public Course(String id, String name) {
			this.id = id;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return id + " - " + name;
		}
		
		@Override
		public boolean equals(Object other) {
			try {
				Course o = (Course) other;
				return id.equalsIgnoreCase(o.id) && name.equals(o.name);
			} catch (ClassCastException e) {
				return false;
			}
		}
		
		@Override
		public int compareTo(Course other) {
			int diff = id.compareToIgnoreCase(other.id);
			if (diff == 0)
				diff = name.compareTo(other.name);
			return diff;
		}
	}
	
	private class Searcher implements Runnable {
		
		@Override
		public void run() {
			
			String searchStr = searchField.getText().toString();
			
			/*
			 * API request
			 */
			try {
				URL url = null;
				try {
					url = new URL("http://" + NoppaSyncAdapter.apiServer +
							"/api/v1/courses.xml?key=" + NoppaSyncAdapter.apiKey +
							"&search=" + Uri.encode(searchStr));
				} catch (MalformedURLException e) {
					Log.d("AddCourseDialogFragment", "MalformedURLException " + e.getMessage());
					return;
				}
				
				InputStream in = null;
				HttpURLConnection urlConnection = null;
				try {
					urlConnection = (HttpURLConnection) url.openConnection();
					in = new BufferedInputStream(urlConnection.getInputStream());
				} catch (IOException e) {
					Log.d("AddCourseDialogFragment", "IOException " + e.getMessage());
					return;
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
						int eventType = parser.getEventType();
						String name = parser.getName();
		
						if (eventType == XmlPullParser.START_TAG) {
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
						}
						else if (eventType == XmlPullParser.END_TAG &&
								name.equals("course") &&
								course_id != null && course_name != null) {
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
				
				Collections.sort(courses);
			}
			finally {
				progressBar.post(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
				
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		}
	}

	private EditText searchField;
	private ProgressBar progressBar;
	private ListView courseListView;
	private ArrayList<Course> courses;
	ArrayAdapter<Course> adapter;
	
	private OnItemClickListener courseClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        Course course = (Course) parent.getItemAtPosition(position);
	        CheckedTextView ctv = (CheckedTextView) v;
	        ctv.toggle();
	        course.checked = ctv.isChecked();
	    }
	};
	
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
						progressBar.setVisibility(View.VISIBLE);
						
						courses.clear();
						adapter.notifyDataSetChanged();
						
						new Thread(new Searcher()).start();
					}
				});
		
		adapter = new ArrayAdapter<AddCourseDialogFragment.Course>(
				view.getContext(),
				R.layout.course_list_item,
				courses );
		courseListView.setAdapter(adapter);
		courseListView.setOnItemClickListener(courseClickedHandler);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.add_courses);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// update preferences
				SharedPreferences pref =
						PreferenceManager.getDefaultSharedPreferences(getActivity());
				// make a copy of the set, because original set should not be modified
				Set<String> courseSet = new HashSet<String>(
						pref.getStringSet("courses", new HashSet<String>()) );
				
				for (Course course : courses) {
					if (course.checked) {
						courseSet.add(course.id);
					}
				}
				
				Editor editor = pref.edit();
				editor.putStringSet("courses", courseSet);
				editor.commit();
				
				((PreferencesActivity) getActivity()).updateCourseList();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {}
		});
		
		return builder.create();
	}

}
