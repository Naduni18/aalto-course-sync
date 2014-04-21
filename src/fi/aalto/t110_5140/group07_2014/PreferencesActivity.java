package fi.aalto.t110_5140.group07_2014;

import fi.aalto.t110_5140.group07_2014.noppacalendarsync.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class PreferencesActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.preferences);
		
//		Button addButton = (Button) findViewById(R.id.addCoursesButton);
//		Button removeButton = (Button) findViewById(R.id.removeCoursesButton);
//		LinearLayout courseListLayout = (LinearLayout) findViewById(R.id.courseList);
	}

	public void addCourse(View view) {
		new AddCourseDialogFragment().show(
				getFragmentManager(),
				"addCourseDialogFragment" );
	}

	public void removeCourses(View view) {
		LinearLayout courseListLayout = (LinearLayout) findViewById(R.id.courseList);
		
	}

}
