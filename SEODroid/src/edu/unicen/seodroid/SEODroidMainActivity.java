package edu.unicen.seodroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SEODroidMainActivity extends Activity {

	private static final String TAG = "SEODroidMainActivity";

	private static final int HEADER_GETTING_LOCATION = 100;
	private static final int HEADER_ADDRESS_READY = 101;
	private static final int HEADER_LOCATION_FAILURE = 102;

	private Location location;
	private String street;
	private String number;
	
	// TODO: Implement onPause, onDestroy, onResume, etc...

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
//
//		final Button elboton = (Button) findViewById(R.id.elboton);
//		elboton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				refreshLocation();
//			}
//		});
//
//		final Button elboton2 = (Button) findViewById(R.id.elboton2);
//		elboton2.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				changeHeader(HEADER_LOCATION_FAILURE);
//			}
//		});
		
		String[] myArray = {"entrada 1", "entrada 2", "entrada 3", "entrada 4", "entrada 5", "entrada 6", "entrada 7"};
		ListView lv = (ListView) findViewById(R.id.licenseList);
		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem, myArray));
		
		

		refreshLocation();
	}
	
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
	        refreshLocation();
	        return true;
	    case R.id.about:
	        showAbout();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showAbout() {
		// TODO: About dialog
	}

	private void refreshLocation() {
		changeHeader(HEADER_GETTING_LOCATION);
		startListeningLocationUpdates();
	}

	private void changeHeader(int status) {
		String text = null;
		Drawable background = null;
		int pbVisibility = 0;
		switch (status) {
		case HEADER_GETTING_LOCATION:
			text = getString(R.string.getting_location);
			background = getResources().getDrawable(
					R.drawable.loadingbackground);
			pbVisibility = View.VISIBLE;
			break;
		case HEADER_ADDRESS_READY:
			text = street + " " + number;
			background = getResources().getDrawable(
					R.drawable.addressbackground);
			pbVisibility = View.GONE;
			break;
		case HEADER_LOCATION_FAILURE:
			text = getString(R.string.location_failure);
			background = getResources().getDrawable(R.drawable.errorbackground);
			pbVisibility = View.GONE;
			break;
		}
		if (text != null) {
			((TextView) findViewById(R.id.mainHeaderText)).setText(text);
			((RelativeLayout) findViewById(R.id.mainHeader))
					.setBackgroundDrawable(background);
			((ProgressBar) findViewById(R.id.mainHeaderProgressBar))
					.setVisibility(pbVisibility);
		}
	}

	private void setStreetAndNumber(String street, String number) {
		this.street = street;
		this.number = number;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				changeHeader(HEADER_ADDRESS_READY);
			}
		});
	}

	/**
	 * This listener is used to subscribe to location updates, to get the user
	 * coordinates We need to use the reference both in start and stop methods.
	 */
	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location
			// provider.
			locationUpdated(location);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	/**
	 * Subscribes the listener to the location updates.
	 */
	private void startListeningLocationUpdates() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);
	}

	/**
	 * Unsubscribes the location listener, to save battery.
	 */
	private void stopListeningLocationUpdates() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(locationListener);
	}

	/**
	 * Callback for the location update event. Invoked by the location listener.
	 * 
	 * @param location
	 *            : The location obtained from the event.
	 */
	private void locationUpdated(Location location) {
		// TODO: Verify if the location we got is good, or we still have to
		// wait for a better one.
		stopListeningLocationUpdates();
		this.location = location;
		new Thread(updateAddress).start();
	}

	/**
	 * This code retrieves from Google Maps API the street name and number for
	 * the current latitude/longitude location.
	 */
	private Runnable updateAddress = new Runnable() {

		@Override
		public void run() {
			// Create a new HttpClient and Get Request
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(
					"http://maps.googleapis.com/maps/api/geocode/json?&sensor=true&latlng="
							+ Double.toString(location.getLatitude()) + ","
							+ Double.toString(location.getLongitude()));

			try {
				// Execute HTTP Get Request
				HttpResponse response = httpclient.execute(httpget);

				if (response.getStatusLine().getStatusCode() != 200)
					throw new Exception();
				Log.d(TAG, "Received response (200/OK)");

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				StringBuilder builder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject jsonResponse = (JSONObject) new JSONTokener(
						builder.toString()).nextValue();

				Log.d(TAG, jsonResponse.getString("status"));

				if (!jsonResponse.getString("status").equals("OK"))
					throw new Exception();

				Log.d(TAG, "Maps query: status==OK");
				JSONArray results = jsonResponse.getJSONArray("results");
				// Get address_components array for type street_address
				JSONArray address_components = null;
				for (int i = 0; i < results.length(); i++) {
					JSONArray types = results.getJSONObject(i).getJSONArray(
							"types");
					boolean found = false;
					for (int j = 0; j < types.length(); j++) {
						if (types.getString(j).equals("street_address")) {
							found = true;
							break;
						}
					}
					if (found) {
						address_components = results.getJSONObject(i)
								.getJSONArray("address_components");
						break;
					} else
						throw new Exception();
				}

				// Obtain data within address_components array
				String country = null;
				String locality = null;
				String route = null;
				String street_number = null;
				for (int i = 0; i < address_components.length(); i++) {
					JSONArray types = address_components.getJSONObject(i)
							.getJSONArray("types");
					for (int j = 0; j < types.length(); j++) {
						if (types.getString(j).equals("country")) {
							country = address_components.getJSONObject(i)
									.getString("short_name");
						} else if (types.getString(j).equals("locality")) {
							locality = address_components.getJSONObject(i)
									.getString("short_name");
						} else if (types.getString(j).equals("route")) {
							route = address_components.getJSONObject(i)
									.getString("short_name");
						} else if (types.getString(j).equals("street_number")) {
							street_number = address_components.getJSONObject(i)
									.getString("short_name");
						}
					}
				}
				if (country.equals("AR") && locality.equals("Tandil")) {
					Log.d(TAG, "Location is in Tandil, AR");
					if (street_number.matches("[0-9]+-[0-9]+"))
						street_number = street_number.split("-")[0];
					setStreetAndNumber(route, street_number);
				} else {
					Log.d(TAG, "Location outside Tandil, AR");
					// TODO: Show information message
				}

			} catch (Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						changeHeader(HEADER_LOCATION_FAILURE);
					}
				});
			}
		}
	};

}