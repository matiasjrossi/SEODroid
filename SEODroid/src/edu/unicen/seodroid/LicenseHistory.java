/*
 *     This file is part of SEODroid.
 *
 *    SEODroid is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SEODroid is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SEODroid.  If not, see <http://www.gnu.org/licenses/>.
 *    
 */

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
		LicenseHistoryOpenHelper helper = new LicenseHistoryOpenHelper(
				mainActivity);
		db = helper.getWritableDatabase();
	}

	public String[] getLatestLicenses() {
		return getLatestLicenses(DEFAULT_HISTORY);
	}

	public String[] getLatestLicenses(int count) {
		Log.d(TAG, "Trying to get latest " + Integer.toString(count)
				+ " entries...");
		Cursor c = db.query(LicenseHistoryOpenHelper.TABLE_NAME,
				new String[] { "license" }, null, null, null, null,
				"lastUsed DESC", Integer.toString(count));
		Log.d(TAG, "Retrieved " + c.getCount() + " rows");
		String[] result = new String[c.getCount()];
		for (int i = 0; c.moveToNext(); i++) {
			result[i] = c.getString(0);
		}
		return result;
	}

	public void addLicense(String license) {
		Log.d(TAG, "Adding license: " + license);
		ContentValues cv = new ContentValues();
		cv.put("license", license);
		cv.put("lastUsed",
				(int) (Calendar.getInstance().getTimeInMillis() / 1000L));
		db.insertWithOnConflict(LicenseHistoryOpenHelper.TABLE_NAME, "license",
				cv, SQLiteDatabase.CONFLICT_REPLACE);
		mainActivity.reloadLicenseHistory();
	}

	public void deleteLicense(String license) {
		Log.d(TAG, "Deleting license: " + license);
		db.delete(LicenseHistoryOpenHelper.TABLE_NAME, "license = ?",
				new String[] { license });
		mainActivity.reloadLicenseHistory();
	}
}
