package edu.unicen.seodroid;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LicenseHistory {

	private static final String TAG = "LicenseHistory";

	private static final int DEFAULT_HISTORY = 10;

	private SQLiteDatabase db;

	public LicenseHistory(Context context) {
		Log.d(TAG, "Getting database");
		LicenseHistoryOpenHelper helper = new LicenseHistoryOpenHelper(context);
		db = helper.getWritableDatabase();
	}

	public String[] getLatestLicenses() {
		return getLatestLicenses(DEFAULT_HISTORY);
	}

	public String[] getLatestLicenses(int count) {
		Log.d(TAG, "Trying to get latest " + Integer.toString(count) + " entries...");
		Cursor c = db.query(LicenseHistoryOpenHelper.TABLE_NAME,
				new String[] { "license" }, null, null, null, null,
				"lastUsed DESC", Integer.toString(count));
		String[] result = new String[c.getCount()];
		for (int i = 0; i < c.getCount(); i++) {
			result[i] = c.getString(0);
		}
		return result;
	}
	
	public void addLicense(String license) {
		Log.d(TAG, "Adding license: " + license);
		ContentValues cv = new ContentValues();
		cv.put("license", license);
		cv.put("lastUsed", (int)(Calendar.getInstance().getTimeInMillis()/1000L));
		db.insertWithOnConflict(LicenseHistoryOpenHelper.TABLE_NAME, "license", cv, SQLiteDatabase.CONFLICT_REPLACE);
	}
}
