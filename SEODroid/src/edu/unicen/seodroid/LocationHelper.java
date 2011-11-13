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

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationHelper {

	private static final String TAG = "LocationHelper";

	private SEODroidMainActivity mainActivity;

	public LocationHelper(SEODroidMainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	/**
	 * This listener is used to subscribe to location updates, to get the user
	 * coordinates We need to use the reference both in start and stop methods.
	 */
	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.d(TAG, "onLocationChanged running");
			locationUpdated(location);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "onStatusChanged running: [provider: " + provider
					+ " status: " + status + "]");
		}

		public void onProviderEnabled(String provider) {
			Log.d(TAG, "onProviderEnabled running: [provider: " + provider
					+ "]");
		}

		public void onProviderDisabled(String provider) {
			Log.d(TAG, "onProviderDisabled running: [provider: " + provider
					+ "]");
		}
	};

	/**
	 * Subscribes the listener to the location updates.
	 */
	private void startListeningLocationUpdates() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) mainActivity
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
		LocationManager locationManager = (LocationManager) mainActivity
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(locationListener);
	}

	/**
	 * Callback for the location update event. Invoked by the location listener.
	 * 
	 * @param location
	 *            : The location obtained from the event.
	 */
	private void locationUpdated(final Location location) {
		// FIXME: Verify if the location we got is good, or we still have to
		// wait for a better one.
		stopListeningLocationUpdates();

		/**
		 * This code retrieves from Google Maps API the street name and number
		 * for the current latitude/longitude location.
		 */

		new Thread(new Runnable() {

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
							new InputStreamReader(response.getEntity()
									.getContent()));
					StringBuilder builder = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}

					JSONObject jsonResponse = (JSONObject) new JSONTokener(
							builder.toString()).nextValue();

					if (!jsonResponse.getString("status").equals("OK"))
						throw new Exception();

					Log.d(TAG, "Maps query: status==OK");
					JSONArray results = jsonResponse.getJSONArray("results");
					// Get address_components array for type street_address
					JSONArray address_components = null;
					for (int i = 0; i < results.length(); i++) {
						JSONArray types = results.getJSONObject(i)
								.getJSONArray("types");
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
							} else if (types.getString(j).equals(
									"street_number")) {
								street_number = address_components
										.getJSONObject(i).getString(
												"short_name");
							}
						}
					}
					if (country.equals("AR") && locality.equals("Tandil")) {
						Log.d(TAG, "Location is in Tandil, AR");
						if (street_number.matches("[0-9]+-[0-9]+"))
							street_number = street_number.split("-")[0];
						mainActivity.setLocation(route, street_number);
					} else {
						Log.d(TAG, "Location outside Tandil, AR");
						// TODO: Show information message
						throw new Exception();
					}

				} catch (Exception e) {
					mainActivity.setLocation(null, null);
				}
			}

		}).start();
	}

	public void updateLocation() {
		startListeningLocationUpdates();
	}

}
