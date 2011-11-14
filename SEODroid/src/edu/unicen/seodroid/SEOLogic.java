package edu.unicen.seodroid;

import java.util.Hashtable;

import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SEOLogic {

	private static final String TAG = "SEOLogic";

	private static final String SEO_DESTINATION_NUMBER = "66736";

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
		gm2seo.put("Bartolomé Mitre", new SEOStreet("MI", "300", "1900")); // FIXME: Remove exception to include MI 1600
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

	public String sendSMS(String street, String number, String licenseInput)
			throws LicenseNotValidException, AddressNotValidException {

		String license = toLicense(licenseInput);

		String block = toBlock(street, number);

		// TODO: Get for how longer the user wants to park
		String duration = "1";

		String textMessage = license + " " + block + " " + duration;
		Log.d(TAG, "Sending SMS:" + '\n' + textMessage);
		
		// TODO: Wait and show confirmation
//		PendingIntent dummy = PendingIntent.getBroadcast(mainActivity, 0, new Intent("edu.unicen.seodroid.IGNORE_ME"), 0);
//		SmsManager.getDefault().sendTextMessage(SEO_DESTINATION_NUMBER, null, textMessage, dummy, dummy);
		return license;
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
	 * This method enforces the Argentina license number format (regexp: "[A-Z]{3} [0-9]{3}")
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

}
