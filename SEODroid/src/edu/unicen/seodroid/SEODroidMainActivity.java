package edu.unicen.seodroid;

import edu.unicen.seodroid.SEOLogic.AddressNotValidException;
import edu.unicen.seodroid.SEOLogic.LicenseNotValidException;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SEODroidMainActivity extends Activity {

	private static final String TAG = "SEODroidMainActivity";

	private static final int HEADER_GETTING_LOCATION = 100;
	private static final int HEADER_ADDRESS_READY = 101;
	private static final int HEADER_LOCATION_FAILURE = 102;

	private String street;
	private String number;

	private LicenseHistory licenseHistory;
	private LocationHelper locationHelper;
	private SEOLogic seoLogic;

	// TODO: Implement onPause, onDestroy, onResume, etc...

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		licenseHistory = new LicenseHistory(this);
		locationHelper = new LocationHelper(this);
		reloadLicenseHistory();
		updateLocation();

		((Button) findViewById(R.id.sendButton))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						doSend();
					}
				});

		((ListView) findViewById(R.id.licenseList))
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> av, View v, int pos,
							long id) {
						EditText et = (EditText) SEODroidMainActivity.this
								.findViewById(R.id.licenseEditText);
						et.setText(((TextView) (av.getChildAt(pos))).getText());
					}
				});

		registerForContextMenu((ListView) findViewById(R.id.licenseList));

	}

	private void showAbout() {
		// TODO: About dialog
	}

	private void changeStatus(int status) {
		String text = null;
		Drawable background = null;
		int progressBarVisibility = 0;
		boolean sendButtonEnabled = false;
		switch (status) {
		case HEADER_GETTING_LOCATION:
			text = getString(R.string.getting_location);
			background = getResources().getDrawable(
					R.drawable.loadingbackground);
			progressBarVisibility = View.VISIBLE;
			break;
		case HEADER_ADDRESS_READY:
			text = street + " " + number;
			background = getResources().getDrawable(
					R.drawable.addressbackground);
			progressBarVisibility = View.GONE;
			sendButtonEnabled = true;
			break;
		case HEADER_LOCATION_FAILURE:
			text = getString(R.string.location_failure);
			background = getResources().getDrawable(R.drawable.errorbackground);
			progressBarVisibility = View.GONE;
			break;
		}

		((TextView) findViewById(R.id.mainHeaderText)).setText(text);
		((RelativeLayout) findViewById(R.id.mainHeader))
				.setBackgroundDrawable(background);
		((ProgressBar) findViewById(R.id.mainHeaderProgressBar))
				.setVisibility(progressBarVisibility);
		((Button) findViewById(R.id.sendButton)).setEnabled(sendButtonEnabled);
	}

	/**
	 * Actions
	 */
	public void reloadLicenseHistory() {
		ListView lv = (ListView) findViewById(R.id.licenseList);
		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem,
				licenseHistory.getLatestLicenses()));
	}

	private void updateLocation() {
		changeStatus(HEADER_GETTING_LOCATION);
		locationHelper.updateLocation();
	}

	private void doSend() {
		String inputText = ((EditText) findViewById(R.id.licenseEditText))
				.getText().toString();
		Log.d(TAG, "doSend: inputText=" + inputText);

		if (seoLogic == null)
			seoLogic = new SEOLogic(this);

		try {

			String license = seoLogic.sendSMS(this.street, this.number,
					inputText);
			licenseHistory.addLicense(license);

		} catch (LicenseNotValidException e) {
			Log.d(TAG, inputText + "->" + e.getLicense()
					+ ": is not a valid license");
			Toast.makeText(
					this,
					getString(R.string.license_not_valid_toast).replaceAll(
							"%l", e.getLicense()), Toast.LENGTH_LONG).show();
		} catch (AddressNotValidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Set the location to be displayed.
	 * 
	 * @param street
	 * @param number
	 */
	public void setLocation(String street, String number) {
		this.street = street;
		this.number = number;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (SEODroidMainActivity.this.street == null
						|| SEODroidMainActivity.this.number == null)
					changeStatus(HEADER_LOCATION_FAILURE);
				else
					changeStatus(HEADER_ADDRESS_READY);
			}
		});
	}

	/**
	 * Options menu code
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.updateLocation:
			updateLocation();
			return true;
		case R.id.about:
			showAbout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.license_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete:
			licenseHistory.deleteLicense(((TextView) info.targetView).getText()
					.toString());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

}