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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LicenseHistoryOpenHelper extends SQLiteOpenHelper {

	private static final String TAG = "LicenseHistoryOpenHelper";

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "SEODroid";
	public static final String TABLE_NAME = "licenseHistory";

	private static final String CREATE_QUERY = "CREATE TABLE " + TABLE_NAME
			+ " (" + "license" + " TEXT PRIMARY KEY, " + "lastUsed"
			+ " INTEGER);";

	LicenseHistoryOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(TAG, "Created.");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Creating table using query:" + CREATE_QUERY);
		db.execSQL(CREATE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
