package com.example.toshiba.ferry;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.toshiba.ferry.Common.Common;
import com.example.toshiba.ferry.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends FragmentActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener {

	private GoogleMap mMap;

	private static final int MY_PERMISSION_REQUEST_CODE = 7000;
	private static final int PLAY_SERVICE_RES_REQUEST = 7001;

	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;

	private static int UPDATE_INTERVAL = 5000;
	private static int FASTEST_INTERVAL = 3000;
	private static int DISPLACEMENT = 10;


	DatabaseReference drivers, ports;
	GeoFire geoFire;

	Marker mCurrent;

	MaterialAnimatedSwitch location_switch;
	SupportMapFragment mapFragment;

	private EditText editPlace;
	private EditText editDest;
	private String destination;
	private Spinner spnPickup;
	private Spinner spnDest;
	private int fare;
	private Button btnBook;
	private TextView txtFare;
	String pickUp = "";
	String dest = "";

	FirebaseDatabase database;

	private IGoogleAPI mService;

	/*private List<LatLng> polyLineList;
	private Marker ferryMarker;
	private float v;
	private double lat, lng;
	private Handler handler;
	private LatLng startPosition, endPosition, currentPosition;
	private int index, next;
	private Button btnGo;
	private EditText editPlace;
	private String destination;
	private PolylineOptions polylineOptions, blackPolitionOptions;
	private Polyline blackPolyline, greyPolyline;

	private IGoogleAPI mService;
	*/


	/*
	Runnable drawPathRunnable = new Runnable() {
		@Override
		public void run() {
			if (index < polyLineList.size() - 1) {
				index++;
				next = index + 1;
			}
			if (index < polyLineList.size() - 1) {
				startPosition = polyLineList.get(index);
				endPosition = polyLineList.get(next);
			}

			ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
			valueAnimator.setDuration(3000);
			valueAnimator.setInterpolator(new LinearInterpolator());
			valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					v = animation.getAnimatedFraction();
					lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
					lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
					LatLng newPos = new LatLng(lat, lng);
					ferryMarker.setPosition(newPos);
					ferryMarker.setAnchor(0.5f, 0.5f);
					ferryMarker.setRotation(getBearing(startPosition, newPos));
					mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
							new CameraPosition.Builder()
									.target(newPos)
									.zoom(15.5f)
									.build()
					));

				}
			});

			valueAnimator.start();
			handler.postDelayed(this, 3000);
		}

	};
	*/

	private float getBearing(LatLng startPosition, LatLng endPosition) {
		double lat = Math.abs(startPosition.latitude - endPosition.latitude);
		double lng = Math.abs(startPosition.longitude - endPosition.longitude);

		if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
			return (float)(Math.toDegrees(Math.atan(lng/lat)));
		else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
			return (float)(Math.toDegrees(Math.atan(lng/lat))+90);
		else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
			return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
		else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
			return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);
		return -1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);


		location_switch = (MaterialAnimatedSwitch)findViewById(R.id.location_switch);
		location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(boolean isOnline) {
				if(isOnline)
				{
					startLocationUpdates();
					displayLocation();
					Snackbar.make(mapFragment.getView(), "You are online", Snackbar.LENGTH_SHORT)
							.show();
				}
				else
				{
					stopLocationUpdates();
					mCurrent.remove();
					mMap.clear();
					//handler.removeCallbacks(drawPathRunnable);
					Snackbar.make(mapFragment.getView(), "You are offline", Snackbar.LENGTH_SHORT)
							.show();
				}
			}
		});

		//polyLineList = new ArrayList<>();

		database = FirebaseDatabase.getInstance();
		ports = database.getReference().child("Ports");

		ArrayAdapter<CharSequence> adapterPorts;

		String[] ports = {"Port Klang", "Northports", "Westports", "Port Klang Free Zone", "Penang Port Commission", "Penang Port", "Johor Port", "Port of Tanjung Pelepas"};

		spnPickup = (Spinner)findViewById(R.id.spinnerPickup);
		spnDest = (Spinner)findViewById(R.id.spinnerDest);

		adapterPorts = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, ports);
		adapterPorts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnPickup.setAdapter(adapterPorts);
		spnDest.setAdapter(adapterPorts);

		pickUp = spnPickup.getSelectedItem().toString();
		dest = spnDest.getSelectedItem().toString();


		btnBook = (Button)findViewById(R.id.btnGo);
//		editPlace = (EditText)findViewById(R.id.edtPlace);
//		editDest = (EditText)findViewById(R.id.edtDest);
		txtFare = (TextView)findViewById(R.id.fare);

		drivers = FirebaseDatabase.getInstance().getReference("Drivers");
		geoFire = new GeoFire(drivers);

		setUpLocation();

		mService = Common.getGoogleAPI();


		spnPickup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				pickUp = spnPickup.getSelectedItem().toString();
				dest = spnDest.getSelectedItem().toString();

				setTotalFare(pickUp, dest);

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spnDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				pickUp = spnPickup.getSelectedItem().toString();
				dest = spnDest.getSelectedItem().toString();

				setTotalFare(pickUp, dest);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

	}



/*	fakeSpinnerPickUp.onSelectedListener(new onSelectedListener({
		String pickupValue = fakeSpinnerPickUp.selectedValue();
		String destValue = fakeSpinnerDest.selectedValue();

		setTotalFare(pickupValue, destValue);
	}));

	fakeSpinnerDest.onSelectedListener(new onSelectedListener({
		String pickupValue = fakeSpinnerPickUp.selectedValue();
		String destValue = fakeSpinnerDest.selectedValue();

		setTotalFare(pickupValue, destValue);
	}));*/


	private void setTotalFare(final String pickUp, final String dest) {
		ports.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				Map<String, String> locations = new HashMap<String, String>();
				dataSnapshot.child("Location");
				// get all data from the firebase and put it into locations array
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					Map<String, String> map = (Map<String, String>) ds.getValue();
					String portName = map.get("Name");
					String portLocation = map.get("Location");
					locations.put(portName, portLocation);
				}
				// validate edit text must be exists in the locations array
				if(!(locations.containsKey(pickUp) || locations.containsKey(dest))) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
					dialog.setTitle("Error");
					dialog.setMessage("Port not found");

					dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					dialog.show();
				} else {
					// TODO: do your price adding according to the edit texts
					if(locations.get(pickUp).equals("Klang") && locations.get(dest).equals("Klang"))
					{
						fare = 5;
					}
					else if(locations.get(pickUp).equals("Klang") && locations.get(dest).equals("Penang"))
					{
						fare = 10;
					}
					else if(locations.get(pickUp).equals("Klang") && locations.get(dest).equals("Johor"))
					{
						fare = 15;
					}
					else if(locations.get(pickUp).equals("Penang") && locations.get(dest).equals("Penang"))
					{
						fare = 5;
					}
					else if(locations.get(pickUp).equals("Penang") && locations.get(dest).equals("Johor"))
					{
						fare = 10;
					}
					else if(locations.get(pickUp).equals("Johor") && locations.get(dest).equals("Johor")) {
						fare = 5;
					}
					txtFare.setText(Integer.toString(fare));
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	/*
	private void getDirection() {
		currentPosition = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

		String requestApi = null;
		try{
			requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
					"mode=driving&"+
					"transit_routing_preferences=less_driving&"+
					"origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
					"destination="+destination+"&"+
					"key="+getResources().getString(R.string.google_direction_api);
			Log.d("Ferry", requestApi);
			mService.getPath(requestApi)
					.enqueue(new Callback<String>() {
						@Override
						public void onResponse(Call<String> call, Response<String> response) {

							try {
								JSONObject jsonObject = new JSONObject(response.body().toString());
								JSONArray jsonArray = jsonObject.getJSONArray("routes");
								for (int i=0; i<jsonArray.length(); i++)
								{
									JSONObject route = jsonArray.getJSONObject(i);
									JSONObject poly = route.getJSONObject("overview_polyline");
									String polyline = poly.getString("points");
									polyLineList = decodePoly(polyline);
								}

								LatLngBounds.Builder builder = new LatLngBounds.Builder();
								for(LatLng latLng:polyLineList)
									builder.include(latLng);
								LatLngBounds bounds = builder.build();
								CameraUpdate mCameraUpdate =  CameraUpdateFactory.newLatLngBounds(bounds,2);
								mMap.animateCamera(mCameraUpdate);

								polylineOptions = new PolylineOptions();
								polylineOptions.color(Color.GRAY);
								polylineOptions.width(5);
								polylineOptions.startCap(new SquareCap());
								polylineOptions.endCap(new SquareCap());
								polylineOptions.jointType(JointType.ROUND);
								polylineOptions.addAll(polyLineList);
								greyPolyline = mMap.addPolyline(polylineOptions);

								blackPolitionOptions = new PolylineOptions();
								blackPolitionOptions.color(Color.BLACK);
								blackPolitionOptions.width(5);
								blackPolitionOptions.startCap(new SquareCap());
								blackPolitionOptions.endCap(new SquareCap());
								blackPolitionOptions.jointType(JointType.ROUND);
								blackPolyline = mMap.addPolyline(blackPolitionOptions);

								mMap.addMarker(new MarkerOptions()
										.position(polyLineList.get(polyLineList.size()-1))
										.title("Pickup Location"));

								ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
								polyLineAnimator.setDuration(2000);
								polyLineAnimator.setInterpolator(new LinearInterpolator());
								polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
									@Override
									public void onAnimationUpdate(ValueAnimator animation) {
										List<LatLng> points = greyPolyline.getPoints();
										int percentValue = (int)animation.getAnimatedValue();
										int size = points.size();
										int newPoints = (int)(size*(percentValue/100.0f));
										List<LatLng> p = points.subList(0,newPoints);
										blackPolyline.setPoints(p);
									}
								});
								polyLineAnimator.start();

								ferryMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
								.flat(true)
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.ferry)));

								handler = new Handler();
								index = -1;
								next = 1;
								handler.postDelayed(drawPathRunnable,3000);


							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onFailure(Call<String> call, Throwable t) {
							Toast.makeText(Home.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	*/

	private List<LatLng> decodePoly(String encoded) {
		List poly = new ArrayList();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode)
		{
			case MY_PERMISSION_REQUEST_CODE:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					if(checkPlayServices())
					{
						buildGoogleApiClient();
						createLocationRequest();
						displayLocation();
						if(location_switch.isChecked())
						{
							displayLocation();
						}
					}
				}
		}
	}

	private void setUpLocation() {
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION
			}, MY_PERMISSION_REQUEST_CODE);
		}

		else
		{
			if(checkPlayServices())
			{
				buildGoogleApiClient();
				createLocationRequest();
				displayLocation();
				if(location_switch.isChecked())
				{
					displayLocation();
				}
			}
		}
	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
	}

	private void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mGoogleApiClient.connect();
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resultCode != ConnectionResult.SUCCESS)
		{
			if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
			else
			{
				Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
				finish();
			}
			return false;
		}
		return true;
	}

	private void stopLocationUpdates() {
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			return;
		}
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	}

	private void displayLocation() {
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			return;
		}
		mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if(mLastLocation != null)
		{
			final double latitude = mLastLocation.getLatitude();
			final double longitude = mLastLocation.getLongitude();

			geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
				@Override
				public void onComplete(String key, DatabaseError error) {
					if(mCurrent != null)
						mCurrent.remove();
					mCurrent = mMap.addMarker(new MarkerOptions()
							.position(new LatLng(latitude, longitude))
							.title("Your Location"));
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
				}
			});
			if(location_switch.isChecked())
			{
//				final double latitude = mLastLocation.getLatitude();
//				final double longitude = mLastLocation.getLongitude();

				geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
					@Override
					public void onComplete(String key, DatabaseError error) {
						if(mCurrent != null)
							mCurrent.remove();
						mCurrent = mMap.addMarker(new MarkerOptions()
													.position(new LatLng(latitude, longitude))
													.title("Your Location"));
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
					}
				});
			}
		}
		else
		{
			Log.d("ERROR", "Cannot get your location");
		}
	}

	private void rotateMarker(final Marker mCurrent, final int i, GoogleMap mMap) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		final float startRotation = mCurrent.getRotation();
		final long duration = 1500;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float)elapsed/duration);
				float rot = t*i+(1-t)*startRotation;
				mCurrent.setRotation(-rot > 180?rot/2:rot);
				if(t<1.0)
				{
					handler.postDelayed(this,16);
				}
			}
		});
	}

	private void startLocationUpdates() {
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
	}


	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		/*mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.setTrafficEnabled(false);
		mMap.setIndoorEnabled(false);
		mMap.setBuildingsEnabled(false);
		mMap.getUiSettings().setZoomControlsEnabled(true);*/

	}

	@Override
	public void onLocationChanged(Location location) {
		mLastLocation = location;
		displayLocation();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		displayLocation();
		startLocationUpdates();

	}

	@Override
	public void onConnectionSuspended(int i) {
		mGoogleApiClient.connect();

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}
}
