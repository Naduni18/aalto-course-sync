package fi.aalto.ekman_lehtomaki.aaltocoursesync;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import fi.aalto.ekman_lehtomaki.aaltocoursesync.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class PreferencesActivity extends Activity {

	private class CheckBoxListener implements OnCheckedChangeListener {
		
		private String prefKey;
		
		public CheckBoxListener(String prefKey) {
			this.prefKey = prefKey;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Editor editor = pref.edit();
			editor.putBoolean(prefKey, isChecked);
			editor.commit();
		}
	}
	
	private SharedPreferences pref;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.preferences);
		
		// set checkbox values from preferences
		
		CheckBox lectures = (CheckBox) findViewById(R.id.lecturesCheckBox);
		lectures.setChecked(pref.getBoolean("include_lectures", true));
		lectures.setOnCheckedChangeListener(new CheckBoxListener("include_lectures"));
		
		CheckBox exercises = (CheckBox) findViewById(R.id.exercisesCheckBox);
		exercises.setChecked(pref.getBoolean("include_exercises", true));
		exercises.setOnCheckedChangeListener(new CheckBoxListener("include_exercises"));
		
		CheckBox assignments = (CheckBox) findViewById(R.id.assignmentsCheckBox);
		assignments.setChecked(pref.getBoolean("include_assignments", true));
		assignments.setOnCheckedChangeListener(new CheckBoxListener("include_assignments"));
		
		CheckBox other = (CheckBox) findViewById(R.id.otherCheckBox);
		other.setChecked(pref.getBoolean("include_other", true));
		other.setOnCheckedChangeListener(new CheckBoxListener("include_other"));
		
		updateCourseList();
	}

	/** Called on add courses button click */
	public void addCourses(View view) {
		new AddCourseDialogFragment().show(
				getFragmentManager(),
				"addCourseDialogFragment" );
	}

	/** Called on remove courses button click */
	public void removeCourses(View view) {
		LinearLayout courseListLayout = (LinearLayout) findViewById(R.id.courseList);
		
		Set<String> courseSet = new HashSet<String>(
				pref.getStringSet("courses", new HashSet<String>()) );
		
		for (int i=0; i<courseListLayout.getChildCount(); i++) {
			CheckBox cb = (CheckBox) courseListLayout.getChildAt(i);
			if (cb.isChecked()) {
				String course = cb.getText().toString();
				courseSet.remove(course);
				Log.d("PreferencesActivity", "removing: " + course);
			}
		}
		
		Editor editor = pref.edit();
		editor.putStringSet("courses", courseSet);
		editor.commit();
		
		updateCourseList();
	}
	
	/** Read set of courses from preferences and update course list */
	void updateCourseList() {
		LinearLayout courseListLayout = (LinearLayout) findViewById(R.id.courseList);
		courseListLayout.removeAllViews();
		
		// copy to TreeSet to get items in sorted order
		Set<String> courseSet = new TreeSet<String>(
				pref.getStringSet("courses", new HashSet<String>()) );
		
		for (String course : courseSet) {
			CheckBox cb = new CheckBox(this);
			cb.setText(course);
			cb.setChecked(false);
			courseListLayout.addView(cb);
		}
	}

}
