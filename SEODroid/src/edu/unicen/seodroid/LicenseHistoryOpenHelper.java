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

    private static final String CREATE_QUERY =
                "CREATE TABLE " + TABLE_NAME + " (" +
                "license" + " TEXT PRIMARY KEY, " +
                "lastUsed" + " INTEGER);";

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
