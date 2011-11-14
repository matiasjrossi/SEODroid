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

import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class SEOLogic {

	private static final String TAG = "SEOLogic";

	private static final String SEO_DESTINATION_NUMBER = "66736";
	public static final String SENT_INTENT = "edu.unicen.seodroid.SMS_SENT";

	private SEODroidMainActivity mainActivity;

	private class SEOStreet {
		private String code;
		private String min;
		private String max;

		public SEOStreet(String code, String min, String max) {
			this.code = code;
			this.min = min;
			this.max = max;
		}

		public String getCode() {
			return code;
		}

		public String getMin() {
			return min;
		}

		public String getMax() {
			return max;
		}

		@Override
		public String toString() {
			return "{ \"code\": " + code + ", \"min\": " + min + ", \"max\": "
					+ max + " }";
		}

	}

	Hashtable<String, SEOStreet> gm2seo;

	private void populateGm2seoDict() {
		gm2seo = new Hashtable<String, SEOStreet>();
		gm2seo.put("Gral. Paz", new SEOStreet("PA", "300", "800"));
		gm2seo.put("Leandro N. Alem", new SEOStreet("AL", "300", "800"));
		gm2seo.put("9 de Julio", new SEOStreet("NU", "300", "800"));
		gm2seo.put("Gral. Rodriguez", new SEOStreet("RO", "300", "800"));
		gm2seo.put("Independencia", new SEOStreet("FU", "300", "300"));
		gm2seo.put("Hipólito Irigoyen", new SEOStreet("YR", "500", "800"));
		gm2seo.put("Chacabuco", new SEOStreet("CH", "300", "800"));
		gm2seo.put("14 de Julio", new SEOStreet("CA", "300", "800"));
		gm2seo.put("Av España", new SEOStreet("ES", "300", "900"));
		 // FIXME: Remove exception to include MI 1600 :-)
		gm2seo.put("Bartolomé Mitre", new SEOStreet("MI", "300", "1900"));
		gm2seo.put("Sarmiento", new SEOStreet("SA", "300", "900"));
		gm2seo.put("San Martin", new SEOStreet("SM", "300", "900"));
		gm2seo.put("Gral. Pinto", new SEOStreet("PI", "300", "900"));
		gm2seo.put("Belgrano", new SEOStreet("BE", "300", "900"));
		gm2seo.put("Maipú", new SEOStreet("MA", "300", "900"));
	}

	public SEOLogic(SEODroidMainActivity mainActivity) {
		this.mainActivity = mainActivity;
		populateGm2seoDict();
	}

	public void sendSMS(SMS message, int hours) {
		Log.d(TAG, "Sending SMS:" + '\n' + message.withTime(hours));

		PendingIntent result = PendingIntent.getBroadcast(mainActivity, 0,
				new Intent("edu.unicen.seodroid.SMS_SENT"), 0);

		fakeSendTextMessage(SEO_DESTINATION_NUMBER, null,
				message.withTime(hours), result, null, 
//				Activity.RESULT_OK);
				SmsManager.RESULT_ERROR_GENERIC_FAILURE);

	}

	private void fakeSendTextMessage(String destination,
			String smsc, final String message, final PendingIntent onSent,
			PendingIntent onReceived, final int result) {
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				Log.d(TAG, "FakeSMSSent: " + message);
				try {
					onSent.send(result);
				} catch (CanceledException e) {
					Log.e(TAG, "Canceled PendingIntent");
					e.printStackTrace();
				}
				
			}
		}, (long)(new Random().nextDouble() * 5000));
		
	}

	public SMS buildDefaultSMS(String street, String number, String licenseInput)
			throws LicenseNotValidException, AddressNotValidException {

		SMS message = new SMS(toLicense(licenseInput), toBlock(street, number));
		Log.d(TAG, "Built SMS:" + '\n' + message.getSMS());
		return message;
	}

	private String toBlock(String street, String number)
			throws AddressNotValidException {
		SEOStreet seoStreet = gm2seo.get(street);

		try {
			Log.d(TAG, "Lookup for street: " + street + ", number: " + number
					+ " resulted in:" + '\n' + seoStreet.toString());

			int minI = new Integer(seoStreet.getMin()).intValue();
			int numberI = new Integer(number).intValue();
			int maxI = new Integer(seoStreet.getMax()).intValue();

			if (minI <= numberI && numberI <= maxI)
				return new String(seoStreet.getCode() + " " + number);
			else
				throw new AddressNotValidException();
		} catch (NullPointerException e) {
			Log.d(TAG, "Lookup for street: " + street + ", number: " + number
					+ " resulted in: NullPointerException");
			throw new AddressNotValidException();
		}
	}

	/**
	 * This method enforces the Argentina license number format (regexp:
	 * "[A-Z]{3} [0-9]{3}")
	 * 
	 * @param licenseInput
	 * @return The license matching the regexp
	 * @throws LicenseNotValidException
	 */
	private String toLicense(String licenseInput)
			throws LicenseNotValidException {
		String license = licenseInput.replaceAll(" +", "").replaceAll("-+", "")
				.replaceAll("_+", "").toUpperCase();

		Log.d(TAG, "Processing input text: " + licenseInput + " to: " + license);

		if (!license.matches("[A-Z]{3}[0-9]{3}"))
			throw new LicenseNotValidException(licenseInput);

		return license.substring(0, 3) + " " + license.substring(3);
	}

	@SuppressWarnings("serial")
	public class LicenseNotValidException extends Exception {

		private String license;

		public LicenseNotValidException(String license) {
			super();
			this.license = license;
		}

		public String getLicense() {
			return license;
		}

	}

	@SuppressWarnings("serial")
	public class AddressNotValidException extends Exception {
	}

	public class SMS {
		private String license;
		private String block;

		private SMS(String license, String block) {
			this.license = license;
			this.block = block;
		}

		public String getLicense() {
			return this.license;
		}

		public String getBlock() {
			return this.block;
		}

		public String getSMS() {
			return this.license + " " + this.block;
		}

		public String withTime(int hours) {
			return this.getSMS() + " " + Integer.toString(hours);
		}
	}

}
