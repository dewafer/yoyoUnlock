package wyq.android.yoyounlock.tool;

import wyq.android.yoyounlock.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreferences extends DialogPreference implements
		OnSeekBarChangeListener {

	private SeekBar mSeekbar;
	private int intValue;
	private int maxIntValue = 30;
	private TextView mTextView;
	private int minIntValue = 1;

	public SeekBarPreferences(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.seek_bar_preferences);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);

	}

	@Override
	public CharSequence getSummary() {

		int realValue = PreferenceManager.getDefaultSharedPreferences(
				getContext()).getInt(
				getContext().getString(R.string.seek_bar_preference_key),
				getIntValue());

		return getContext().getString(R.string.seek_bar_preference_summary,
				realValue);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			if (mSeekbar != null) {
				int newValue = mSeekbar.getProgress();
				if (callChangeListener(newValue)) {
					setIntValue(newValue);
					notifyChanged();
				}
			}
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			setIntValue(this.getPersistedInt(getIntValue()));
		} else {
			// Set default state from the XML attribute
			setIntValue((Integer) defaultValue);
		}
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		int realValue = PreferenceManager.getDefaultSharedPreferences(
				getContext()).getInt(
				getContext().getString(R.string.seek_bar_preference_key),
				getIntValue());

		setIntValue(realValue);

		mSeekbar = (SeekBar) view.findViewById(R.id.seek_bar);
		mSeekbar.setProgress(getIntValue());
		mSeekbar.setOnSeekBarChangeListener(this);
		mSeekbar.setMax(getMaxIntValue());
		mTextView = (TextView) view
				.findViewById(R.id.displaySeekBarValueTextView);
		mTextView.setText(String.valueOf(mSeekbar.getProgress()));

	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
		this.persistInt(intValue);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, 0);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.intValue = getIntValue();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setIntValue(myState.intValue);
	}

	private static class SavedState extends BaseSavedState {
		int intValue;

		public SavedState(Parcel source) {
			super(source);
			intValue = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(intValue);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (mTextView != null) {
			mTextView.setText(String.valueOf(progress));
		}
		if (progress == 0) {
			seekBar.setProgress(getMinIntValue());
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// leave blank
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// leave blank
	}

	public int getMaxIntValue() {
		return maxIntValue;
	}

	public void setMaxIntValue(int maxIntValue) {
		this.maxIntValue = maxIntValue;
	}

	public int getMinIntValue() {
		return minIntValue;
	}

	public void setMinIntValue(int minIntValue) {
		this.minIntValue = minIntValue;
	}

}
