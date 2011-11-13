package edu.unicen.seodroid;

import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LicenseHistory {

	private static final String TAG = "LicenseHistory";

	private static final int DEFAULT_HISTORY = 10;

	private SQLiteDatabase db;
	private SEODroidMainActivity mainActivity;

	public LicenseHistory(SEODroidMainActivity mainActivity) {
		this.mainActivity = mainActivity;
		Log.d(TAG, "Getting database");
		LicenseHistoryOpenHelper helper = new LicenseHistoryOpenHelper(mainActivity);
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
		Log.d(TAG, "Retrieved " + c.getCount() + " rows");
		String[] result = new String[c.getCount()];
		for (int i=0; c.moveToNext(); i++) {
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
		mainActivity.reloadLicenseHistory();
	}
}
