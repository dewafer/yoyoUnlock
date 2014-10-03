package wyq.android.yoyounlock.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import wyq.android.yoyounlock.R;
import android.app.ListActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 这个就是隐藏的能列出所有感应器的类
 * 
 * @author dewafer
 * 
 */
public class ListSensors extends ListActivity {

	private SensorManager mSensorManager;
	private List<Sensor> allSensors;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		allSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

		// Use our own list adapter
		setListAdapter(new SensorListAdapter(this));

		// To enable the app icon as an Up button, call
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A sample ListAdapter that presents content from arrays of speeches and
	 * text.
	 * 
	 */
	private class SensorListAdapter extends BaseAdapter {
		public SensorListAdapter(Context context) {
			mContext = context;
		}

		/**
		 * The number of items in the list is determined by the number of
		 * speeches in our array.
		 * 
		 * @see android.widget.ListAdapter#getCount()
		 */
		public int getCount() {
			return allSensors.size();
		}

		/**
		 * Since the data comes from an array, just returning the index is
		 * sufficent to get at the data. If we were using a more complex data
		 * structure, we would return whatever object represents one row in the
		 * list.
		 * 
		 * @see android.widget.ListAdapter#getItem(int)
		 */
		public Object getItem(int position) {
			return position;
		}

		/**
		 * Use the array index as a unique id.
		 * 
		 * @see android.widget.ListAdapter#getItemId(int)
		 */
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Make a SpeechView to hold each row.
		 * 
		 * @see android.widget.ListAdapter#getView(int, android.view.View,
		 *      android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			if (convertView == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.sensor_item,
						parent, false);
			} else {
				v = (View) convertView;
			}

			TextView sensorName = (TextView) v
					.findViewById(R.id.sensorNameTextView);
			TextView sensorType = (TextView) v
					.findViewById(R.id.sensorTypetextView);
			TextView sensorVendor = (TextView) v
					.findViewById(R.id.sensorVendorTextView);

			Sensor sensor = allSensors.get(position);

			sensorName.setText(sensor.getName());
			sensorType.setText(findSensorTypeName(sensor.getType()));
			sensorVendor.setText(sensor.getVendor());

			v.setClickable(false);

			return v;
		}

		/**
		 * Remember our context so we can use it when constructing views.
		 */
		private Context mContext;

		private String findSensorTypeName(int sensorType) {
			Class<Sensor> clazz = Sensor.class;
			Field[] fields = clazz.getFields();
			for (Field f : fields) {
				if (f.getName().startsWith("TYPE_")
						&& Modifier.isStatic(f.getModifiers())) {
					try {
						if (sensorType == f.getInt(null)) {
							return f.getName();
						}
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return "Unknown";
		}
	}
}
