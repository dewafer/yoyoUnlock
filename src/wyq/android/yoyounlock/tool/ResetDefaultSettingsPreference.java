package wyq.android.yoyounlock.tool;

import wyq.android.yoyounlock.R;
import wyq.android.yoyounlock.ShakeListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class ResetDefaultSettingsPreference extends DialogPreference {

	public ResetDefaultSettingsPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogIcon(android.R.drawable.ic_dialog_info);
		setDialogMessage(R.string.reset_default_settings_message);
		setDialogTitle(R.string.reset_default_settings_title);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			String key = getContext().getString(
					R.string.seek_bar_preference_key);

			if (callChangeListener(ShakeListener.MIN_ACCELERATION_DEFAULT)) {

				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(getContext());

				pref.edit().putInt(key, ShakeListener.MIN_ACCELERATION_DEFAULT)
						.commit();

				notifyChanged();

			}
		}
	}

}
