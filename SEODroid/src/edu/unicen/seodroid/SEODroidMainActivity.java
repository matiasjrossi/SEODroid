package edu.unicen.seodroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.view.View;
import android.widget.Button;
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final Button elboton = (Button) findViewById(R.id.elboton);
		elboton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshLocation();
			}
		});

		final Button elboton2 = (Button) findViewById(R.id.elboton2);
		elboton2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				changeHeader(HEADER_LOCATION_FAILURE);
			}
		});

		refreshLocation();
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
			text = "Sarasa 1200";
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
		// FIXME: Verify if the location we got is good, or we still have to
		// wait for a better one.
		stopListeningLocationUpdates();
		this.location = location;
		// FIXME: Change this to run in a separate thread
		updateAddress.run();
	}

	/**
	 * This thread retrieves from Google Maps API the street name and number for
	 * the current latitude/longitude location.
	 */
	private Thread updateAddress = new Thread() {
		public void run() {
			// Create a new HttpClient and Get Request
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(
					"http://maps.googleapis.com/maps/api/geocode/json?&sensor=true&latlng="
							+ Double.toString(location.getLatitude()) + ","
							+ Double.toString(location.getLongitude()));

			try {
				Thread.sleep(7000);
				// Execute HTTP Get Request
				HttpResponse response = httpclient.execute(httpget);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				StringBuilder builder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject jsonResponse = (JSONObject) new JSONTokener(
						builder.toString()).nextValue();

			} catch (Exception e) {
				changeHeader(HEADER_LOCATION_FAILURE);
			}
			changeHeader(HEADER_ADDRESS_READY);
		}
	};

}