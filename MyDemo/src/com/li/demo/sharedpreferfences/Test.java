package com.li.demo.sharedpreferfences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class Test {

	private void readConfig(Context context) {
		Context c = null;
		try {
			c = context.createPackageContext("com.konka.bumblebee",
					Context.CONTEXT_IGNORE_SECURITY);

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SharedPreferences sharedPreferences = c.getSharedPreferences(
				"bumblebee", Context.MODE_MULTI_PROCESS);
		boolean isRequestVoice = sharedPreferences.getBoolean("request_voice_key", false);
		String packagename = sharedPreferences.getString("package_name", "");
		String activityName = sharedPreferences.getString("activity_name", "");
	}

}
