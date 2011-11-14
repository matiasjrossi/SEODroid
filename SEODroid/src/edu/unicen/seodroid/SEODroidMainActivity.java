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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
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
import android.widget.TimePicker;
import android.widget.Toast;
import edu.unicen.seodroid.SEOLogic.AddressNotValidException;
import edu.unicen.seodroid.SEOLogic.LicenseNotValidException;

public class SEODroidMainActivity extends Activity {

	private static final String TAG = "SEODroidMainActivity";

	private static final int STATUS_GETTING_LOCATION = 100;
	private static final int STATUS_ADDRESS_READY = 101;
	private static final int STATUS_LOCATION_FAILURE = 102;

	private static final int DIALOG_WRONG_LOCALITY = 200;
	private static final int DIALOG_UNKNOWN_BLOCK = 201;
	private static final int DIALOG_SENDING_SMS = 202;
	private static final int DIALOG_ABOUT = 203;
	private static final int DIALOG_TIME_PROMPT = 204;

	private String street = null;
	private String number = null;
	private SEOLogic.SMS currentMessage = null;
	// private int parkingTime = 0;

	private LicenseHistory licenseHistory;
	private LocationHelper locationHelper;
	private SEOLogic seoLogic;
	private String myText = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		licenseHistory = new LicenseHistory(this);
		locationHelper = new LocationHelper(this);

		initUi();
		updateLocation();

	}

	/**
	 * This method is called when the UI rotates, as stated in the application
	 * manifest.
	 */
	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
		myText = ((EditText) findViewById(R.id.licenseEditText)).getText()
				.toString();
		initUi();
	}

	private void initUi() {
		setContentView(R.layout.main);
		setLocation(street, number);
		reloadLicenseHistory();

		if (myText != null)
			((EditText) findViewById(R.id.licenseEditText)).setText(myText);

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

	private void changeStatus(int status) {
		String text = null;
		Drawable background = null;
		int progressBarVisibility = 0;
		boolean sendButtonEnabled = false;
		switch (status) {
		case STATUS_GETTING_LOCATION:
			text = getString(R.string.getting_location);
			background = getResources().getDrawable(
					R.drawable.loadingbackground);
			progressBarVisibility = View.VISIBLE;
			break;
		case STATUS_ADDRESS_READY:
			text = street + " " + number;
			background = getResources().getDrawable(
					R.drawable.addressbackground);
			progressBarVisibility = View.GONE;
			sendButtonEnabled = true;
			break;
		case STATUS_LOCATION_FAILURE:
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
		changeStatus(STATUS_GETTING_LOCATION);
		locationHelper.updateLocation();
	}

	private void doSend() {
		String inputText = ((EditText) findViewById(R.id.licenseEditText))
				.getText().toString();
		Log.d(TAG, "doSend: inputText=" + inputText);

		if (seoLogic == null)
			seoLogic = new SEOLogic(this);

		try {

			currentMessage = seoLogic.buildDefaultSMS(this.street, this.number,
					inputText);

			showDialog(DIALOG_TIME_PROMPT);

		} catch (LicenseNotValidException e) {
			Log.d(TAG, inputText + "->" + e.getLicense()
					+ ": is not a valid license");
			Toast.makeText(
					this,
					getString(R.string.license_not_valid_toast).replaceAll(
							"%l", e.getLicense()), Toast.LENGTH_LONG).show();
		} catch (AddressNotValidException e) {
			showDialog(DIALOG_UNKNOWN_BLOCK);
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
					changeStatus(STATUS_LOCATION_FAILURE);
				else
					changeStatus(STATUS_ADDRESS_READY);
			}
		});
	}

	/**
	 * Dialogs code
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "Creating dialog with id=" + id);
		Dialog dialog = null;
		AlertDialog.Builder builder = null;
		switch (id) {
		case DIALOG_WRONG_LOCALITY:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.error)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(R.string.wrong_locality_message)
					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
			dialog = builder.create();
			break;
		case DIALOG_UNKNOWN_BLOCK:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.error)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(R.string.unknown_block_message)
					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
			dialog = builder.create();
			break;
		case DIALOG_SENDING_SMS:
			ProgressDialog pDialog = new ProgressDialog(this);
			pDialog.setCancelable(false);
			pDialog.setTitle(R.string.sending_dialog_title);
			pDialog.setMessage(getString(R.string.sending_dialog_message));
			break;
		case DIALOG_ABOUT:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.about_dialog_title)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(R.string.about_dialog_message)
					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
			dialog = builder.create();
			break;
		case DIALOG_TIME_PROMPT:
			builder = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setText("1");
			input.setFilters(new InputFilter[] {
					// Maximum 2 characters.
					new InputFilter.LengthFilter(2),
					// Digits only.
					DigitsKeyListener.getInstance(), });

			// Digits only & use numeric soft-keyboard.
			input.setKeyListener(DigitsKeyListener.getInstance());

			builder
			// .setIcon()
			.setMessage(R.string.time_dialog_message)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									int hours = new Integer(input.getText().toString()).intValue();
									Log.d(TAG, "Input Hours: " + hours );
									seoLogic.sendSMS(currentMessage, hours);
									licenseHistory.addLicense(currentMessage.getLicense());
									currentMessage = null;
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).setView(input);
			dialog = builder.create();
			break;
		default:
			dialog = super.onCreateDialog(id);
			break;
		}
		Log.d(TAG, dialog.toString());
		return dialog;
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
			showDialog(DIALOG_ABOUT);
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