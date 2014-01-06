/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.geofence;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.geofence.GeofenceUtils.REMOVE_TYPE;
import com.example.android.geofence.GeofenceUtils.REQUEST_TYPE;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UI handler for the Location Services Geofence sample app. Allow input of
 * latitude, longitude, and radius for two geofences. When registering
 * geofences, check input and then send the geofences to Location Services. Also
 * allow removing either one of or both of the geofences. The menu allows you to
 * clear the screen or delete the geofences stored in persistent memory.
 */
public class MainActivity extends FragmentActivity {

	// Store the current request
	private REQUEST_TYPE mRequestType;

	// Store the current type of removal
	private REMOVE_TYPE mRemoveType;

	// Persistent storage for geofences
	private SimpleGeofenceStore mPrefs;

	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences;

	// Add geofences handler
	private GeofenceRequester mGeofenceRequester;
	// Remove geofences handler
	private GeofenceRemover mGeofenceRemover;
	// Handle to geofence 1 latitude in the UI

	ArrayList<Location> list_location = new ArrayList<Location>();

	/*
	 * Internal lightweight geofence objects for geofence 1 and 2
	 */
	// private SimpleGeofence mUIGeofence2;

	// Store the list of geofences to remove
	private List<String> mGeofenceIdsToRemove;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Instantiate a new geofence storage area
		mPrefs = new SimpleGeofenceStore(this);

		// Instantiate the current List of geofences
		mCurrentGeofences = new ArrayList<Geofence>();

		// Instantiate a Geofence requester
		mGeofenceRequester = new GeofenceRequester(this);

		// Instantiate a Geofence remover
		mGeofenceRemover = new GeofenceRemover(this);

		// Attach to the main UI
		setContentView(R.layout.activity_main);

		initLocations();

	}

	private void initLocations() {

		// Pune
		Location targetLocation = new Location("Pune");
		targetLocation.setLatitude(18.5079381);
		targetLocation.setLongitude(73.8433911);
		list_location.add(targetLocation);

		// Mumbai
		Location targetLocation1 = new Location("Mumbai");
		targetLocation1.setLatitude(19.1183631);
		targetLocation1.setLongitude(72.8564027);
		list_location.add(targetLocation1);

		Log.e("Location list ", list_location.toString());

	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in GeofenceRemover and GeofenceRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult. calls
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// Choose what to do based on the request code
		switch (requestCode) {

		// If the request code matches the code sent in onConnectionFailed
		case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// If the request was to add geofences
				if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

					// Toggle the request flag and send a new request
					mGeofenceRequester.setInProgressFlag(false);

					// Restart the process of adding the current geofences
					mGeofenceRequester.addGeofences(mCurrentGeofences);

					// If the request was to remove geofences
				} else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType) {

					// Toggle the removal flag and send a new removal request
					mGeofenceRemover.setInProgressFlag(false);

					// If the removal was by Intent
					if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

						// Restart the removal of all geofences for the
						// PendingIntent
						mGeofenceRemover
								.removeGeofencesByIntent(mGeofenceRequester
										.getRequestPendingIntent());

						// If the removal was by a List of geofence IDs
					} else {

						// Restart the removal of the geofence list
						mGeofenceRemover
								.removeGeofencesById(mGeofenceIdsToRemove);
					}
				}
				break;

			// If any other result was returned by Google Play services
			default:

				// Report that Google Play services was unable to resolve the
				// problem.
				Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
			}

			// If any other request code was received
		default:
			// Report that this Activity received an unknown requestCode
			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.unknown_activity_request_code,
							requestCode));

			break;
		}
	}

	/*
	 * Whenever the Activity resumes, reconnect the client to Location Services
	 * and reload the last geofences that were set
	 */
	@Override
	protected void onResume() {
		super.onResume();

	}

	/*
	 * Inflate the app menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;

	}

	/*
	 * Respond to menu item selections
	 */

	/*
	 * Save the current geofence settings in SharedPreferences.
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.play_services_available));

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getSupportFragmentManager(),
						GeofenceUtils.APPTAG);
			}
			return false;
		}
	}

	/**
	 * Called when the user clicks the "Remove geofences" button
	 * 
	 * @param view
	 *            The view that triggered this callback
	 */
	public void onUnregisterByPendingIntentClicked(View view) {
		/*
		 * Remove all geofences set by this app. To do this, get the
		 * PendingIntent that was added when the geofences were added and use it
		 * as an argument to removeGeofences(). The removal happens
		 * asynchronously; Location Services calls
		 * onRemoveGeofencesByPendingIntentResult() (implemented in the current
		 * Activity) when the removal is done
		 */

		/*
		 * Record the removal as remove by Intent. If a connection error occurs,
		 * the app can automatically restart the removal if Google Play services
		 * can fix the error
		 */
		// Record the type of removal
		mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;

		/*
		 * Check for Google Play services. Do this after setting the request
		 * type. If connecting to Google Play services fails, onActivityResult
		 * is eventually called, and it needs to know what type of request was
		 * in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		// Try to make a removal request
		try {
			/*
			 * Remove the geofences represented by the currently-active
			 * PendingIntent. If the PendingIntent was removed for some reason,
			 * re-create it; since it's always created with FLAG_UPDATE_CURRENT,
			 * an identical PendingIntent is always created.
			 */
			mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester
					.getRequestPendingIntent());

		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this,
					R.string.remove_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * Called when the user clicks the "Register geofences" button. Get the
	 * geofence parameters for each geofence and add them to a List. Create the
	 * PendingIntent containing an Intent that Location Services sends to this
	 * app's broadcast receiver when Location Services detects a geofence
	 * transition. Send the List and the PendingIntent to Location Services.
	 */
	public void onRegisterClicked(View view) {

		/*
		 * Record the request as an ADD. If a connection error occurs, the app
		 * can automatically restart the add request if Google Play services can
		 * fix the error
		 */
		mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

		/*
		 * Check for Google Play services. Do this after setting the request
		 * type. If connecting to Google Play services fails, onActivityResult
		 * is eventually called, and it needs to know what type of request was
		 * in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		/*
		 * Create a version of geofence 1 that is "flattened" into individual
		 * fields. This allows it to be stored in SharedPreferences.
		 */
		for (int i = 0; i < list_location.size(); i++) {

			SimpleGeofence mUIGeofence2 = new SimpleGeofence(list_location.get(
					i).getProvider(),
					// Get latitude, longitude, and radius from the UI
					Double.valueOf(list_location.get(i).getLatitude()),
					Double.valueOf(list_location.get(i).getLongitude()),
					Float.valueOf(30),
					// Set the expiration time
					Geofence.NEVER_EXPIRE,
					// Detect both entry and exit transitions
					Geofence.GEOFENCE_TRANSITION_ENTER
							| Geofence.GEOFENCE_TRANSITION_EXIT);

			// Store this flat version in SharedPreferences
			mPrefs.setGeofence("" + i, mUIGeofence2);

			/*
			 * Add Geofence objects to a List. toGeofence() creates a Location
			 * Services Geofence object from a flat object
			 */
			mCurrentGeofences.add(mUIGeofence2.toGeofence());

		}
		// Start the request. Fail if there's already a request in progress
		try {
			// Try to add geofences
			mGeofenceRequester.addGeofences(mCurrentGeofences);
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this,
					R.string.add_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}
	}
	
	   public void removeGeofenceById(String id) {
	        /*
	         * Remove the geofence by creating a List of geofences to
	         * remove and sending it to Location Services. The List
	         * contains the id of geofence 1 ("1").
	         * The removal happens asynchronously; Location Services calls
	         * onRemoveGeofencesByPendingIntentResult() (implemented in
	         * the current Activity) when the removal is done.
	         */

	        // Create a List of 1 Geofence with the ID "1" and store it in the global list
	        mGeofenceIdsToRemove = Collections.singletonList(id);

	        /*
	         * Record the removal as remove by list. If a connection error occurs,
	         * the app can automatically restart the removal if Google Play services
	         * can fix the error
	         */
	        mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

	        /*
	         * Check for Google Play services. Do this after
	         * setting the request type. If connecting to Google Play services
	         * fails, onActivityResult is eventually called, and it needs to
	         * know what type of request was in progress.
	         */
	        if (!servicesConnected()) {

	            return;
	        }

	        // Try to remove the geofence
	        try {
	            mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

	        // Catch errors with the provided geofence IDs
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (UnsupportedOperationException e) {
	            // Notify user that previous request hasn't finished.
	            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
	                        Toast.LENGTH_LONG).show();
	        }
	    }


	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
