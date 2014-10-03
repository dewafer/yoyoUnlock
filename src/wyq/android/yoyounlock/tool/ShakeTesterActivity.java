package wyq.android.yoyounlock.tool;

import wyq.android.yoyounlock.R;
import wyq.android.yoyounlock.ShakeListener;
import wyq.android.yoyounlock.ShakeListener.GravityFilter;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShakeTesterActivity extends Activity implements
		SensorEventListener, OnClickListener {

	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;

	private GraphView[] mGraphViews = new GraphView[3];
	private SensorManager mSensorManager;
	private TextView[] mAxisTextViews = new TextView[3];

	private ShakeListener.GravityFilter mGravityFilter = new GravityFilter();
	private CheckBox mGravityFilterCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake_tester);

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.test_linear_layout);

		for (int i = 0; i < 3; i++) {
			// X-Y-Z axis
			getLayoutInflater().inflate(R.layout.axis_shake_tester,
					linearLayout);
			initGraphViews(i, linearLayout.getChildAt(i));
		}

		mGravityFilterCheckBox = (CheckBox) findViewById(R.id.useGravityFilterCheckBox);
		mGravityFilterCheckBox.setOnClickListener(this);

		// To enable the app icon as an Up button, call
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// force portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	private void initGraphViews(int axis, View shaker) {
		FrameLayout xframe = (FrameLayout) shaker
				.findViewById(R.id.xAxisGraphView);

		mGraphViews[axis] = new GraphView(this);
		mGraphViews[axis].setId(axis);
		mGraphViews[axis].setOnClickListener(this);
		xframe.addView(mGraphViews[axis]);

		mAxisTextViews[axis] = (TextView) shaker
				.findViewById(R.id.xAxisTextView);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.useGravityFilterCheckBox:
			if (mGravityFilterCheckBox.isChecked()) {
				mGravityFilter = new GravityFilter();
			}
			break;
		case X:
		case Y:
		case Z:
			maximums = new float[] { 0.0f, 0.0f, 0.0f };
			minimums = new float[] { 0.0f, 0.0f, 0.0f };
			break;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Sensor sensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	private float[] maximums = { 0.0f, 0.0f, 0.0f };
	private float[] minimums = { 0.0f, 0.0f, 0.0f };
	private static final String[] AXIS_NAME = { "X", "Y", "Z" };

	private static final String TAG = "ShakeTesterActivity";

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			float[] linearAcceleration = { event.values[X], event.values[Y],
					event.values[Z] };

			if (mGravityFilterCheckBox.isChecked()) {
				mGravityFilter.removeGravity(event);

				linearAcceleration[X] = mGravityFilter.getLinearAcceleration()[X];
				linearAcceleration[Y] = mGravityFilter.getLinearAcceleration()[Y];
				linearAcceleration[Z] = mGravityFilter.getLinearAcceleration()[Z];
			}

			for (int i = 0; i < 3; i++) {
				// set maximums
				maximums[i] = Math.max(maximums[i], linearAcceleration[i]);

				// set minimums
				minimums[i] = Math.min(minimums[i], linearAcceleration[i]);

				// X-Y-X axis
				mGraphViews[i].setAcceleration(linearAcceleration[i]);
				mGraphViews[i].setMaximum(maximums[i]);
				mGraphViews[i].setMinimum(minimums[i]);
				mGraphViews[i].invalidate();
				mAxisTextViews[i].setText(getString(
						R.string.test_axis_view_text, AXIS_NAME[i],
						linearAcceleration[i], maximums[i], minimums[i]));

			}

			// Log.d(TAG, "la=" + linearAcceleration.toString());
			// Log.d(TAG, "max=" + maximums.toString());
			// Log.d(TAG, "min=" + minimums.toString());

		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// leave blank
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

	class GraphView extends View {

		private Bitmap mBitmap;
		private Canvas mCanvas = new Canvas();
		private Paint mPaint = new Paint();
		private float mDensity = 1f;
		private float mThreshold = 2f / 3f;
		private float mAcceleration;
		private float mMaximum;
		private float mMinimum;

		private int mOffsetX;
		private int mHeight;
		private int mWidth;
		private int localMinAcceleration;

		public GraphView(Context context) {
			super(context);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mHeight = h;
			mWidth = w;
			mOffsetX = Math.round(w / 2);
			drawBitmap(w, h);
		}

		public void redrawBitmap() {
			if (mHeight > 0 && mWidth > 0) {
				drawBitmap(mWidth, mHeight);
			}
		}

		protected void drawBitmap(int w, int h) {
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mCanvas.setBitmap(mBitmap);
			mCanvas.drawColor(Color.WHITE);

			// draw base line
			mPaint.setColor(Color.BLACK);
			mCanvas.drawLine(mOffsetX, 0, mOffsetX, mHeight, mPaint);

			localMinAcceleration = ShakeListener.MIN_ACCELERATION;
			mDensity = mOffsetX * mThreshold / localMinAcceleration;

			// draw threshold line
			mPaint.setColor(Color.BLUE);
			int thresholdOffset = Math.round(mOffsetX * mThreshold);
			mCanvas.drawLine(mOffsetX + thresholdOffset, 0, mOffsetX
					+ thresholdOffset, mHeight, mPaint);
			mCanvas.drawLine(mOffsetX - thresholdOffset, 0, mOffsetX
					- thresholdOffset, mHeight, mPaint);

			// draw gravity line
			mPaint.setColor(Color.GREEN);
			int mGravityOffset = Math.round(SensorManager.STANDARD_GRAVITY
					* mDensity);
			mCanvas.drawLine(mOffsetX + mGravityOffset, 0, mOffsetX
					+ mGravityOffset, mHeight, mPaint);
			mCanvas.drawLine(mOffsetX - mGravityOffset, 0, mOffsetX
					- mGravityOffset, mHeight, mPaint);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			synchronized (this) {

				if (isRequireRedrawBitmap()) {
					redrawBitmap();
				}
				canvas.drawBitmap(mBitmap, 0, 0, null);

				mPaint.setColor(Color.RED);
				float accOffset = mAcceleration * mDensity;
				float left = Math.min(mOffsetX, mOffsetX - accOffset);
				float right = Math.max(mOffsetX, mOffsetX - accOffset);
				canvas.drawRect(left, 0, right, mHeight, mPaint);
				Log.d(TAG, "left=" + mOffsetX + " top=0 right="
						+ (mOffsetX - accOffset) + " bottom=" + mHeight);

				// draw maximum
				mPaint.setColor(Color.RED);
				float maxiOffset = mMaximum * mDensity;
				canvas.drawLine(mOffsetX - maxiOffset, 0,
						mOffsetX - maxiOffset, mHeight, mPaint);

				// draw minimum
				mPaint.setColor(Color.RED);
				float miniOffset = mMinimum * mDensity;
				canvas.drawLine(mOffsetX - miniOffset, 0,
						mOffsetX - miniOffset, mHeight, mPaint);

			}
		}

		protected boolean isRequireRedrawBitmap() {
			return localMinAcceleration != ShakeListener
					.updateMinAcceleration(ShakeTesterActivity.this);
		}

		public void setAcceleration(float mAcceleration) {
			this.mAcceleration = mAcceleration;
		}

		public void setMaximum(float maximum) {
			this.mMaximum = maximum;
		}

		public void setMinimum(float minimum) {
			this.mMinimum = minimum;
		}

	}

}
