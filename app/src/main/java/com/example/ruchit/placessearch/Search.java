package com.example.ruchit.placessearch;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AutocompleteResultType;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

/**
 * Created by gemin on 4/7/2018.
 */

public class Search extends Fragment {

    EditText keyword, distance;
    Spinner category;
    TextView keywordError, locationTextError;
    RadioGroup locationRadios;
    PlacesAutocompleteTextView locationTextField;
    String latitude = "34.0266", longitude = "-118.2831";
    ProgressDialog searchProgressDialog;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mRequestingLocationUpdates = true;
    private LocationCallback mLocationCallback;


    @Override
    public void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissionsList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissionsList, 1);
        }
        else {
            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_form, container, false);
        keyword = rootView.findViewById(R.id.keyword);
        keywordError = rootView.findViewById(R.id.keywordError);
        distance = rootView.findViewById(R.id.distance);
        locationTextField = rootView.findViewById(R.id.locationText);

        locationTextField.setResultType(AutocompleteResultType.GEOCODE);
        locationTextField.setHistoryManager(null);
        locationTextField.setOnPlaceSelectedListener(
                new OnPlaceSelectedListener() {
                    @Override
                    public void onPlaceSelected(final Place place) {
                        locationTextField.getDetailsFor(place, new DetailsCallback() {
                                    @Override
                                    public void onSuccess(final PlaceDetails details) {
                                        latitude = String.valueOf(details.geometry.location.lat);
                                        longitude = String.valueOf(details.geometry.location.lng);
                                    }

                                    @Override
                                    public void onFailure(final Throwable failure) {
                                        Toast.makeText(getActivity(), "Places Autocomplete in Search Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    }
                });

        locationTextError = rootView.findViewById(R.id.locationTextError);

        category = rootView.findViewById(R.id.category);
        category.setSelection(0);

        locationRadios = rootView.findViewById(R.id.locationRadios);
        locationRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (locationRadios.indexOfChild(locationRadios.findViewById(locationRadios.getCheckedRadioButtonId())) == 1) {
                    locationTextField.setEnabled(true);
                    stopLocationUpdates();
                } else {
                    locationTextField.setEnabled(false);
                    locationTextError.setVisibility(View.GONE);
                    startLocationUpdates();
                }
                locationTextField.getText().clear();
                locationTextField.dismissDropDown();//
            }
        });
        Button search = rootView.findViewById(R.id.search);
        Button clear = rootView.findViewById(R.id.clear);
        searchProgressDialog = new ProgressDialog(getActivity());
        searchProgressDialog.setMessage("Fetching Results");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if(locationRadios.indexOfChild(locationRadios.findViewById(locationRadios.getCheckedRadioButtonId())) == 0){
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                    }
                    System.out.println("Lat : " + latitude + " . Long : " + longitude);
                }
            }
        };

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keywordText = keyword.getText().toString();
                String locationText = locationTextField.getText().toString();
                boolean errorExists = false;
                if (keywordText.trim().length() == 0) {
                    keywordError.setVisibility(View.VISIBLE);
                    errorExists = true;
                }
                if (locationRadios.indexOfChild(locationRadios.findViewById(locationRadios.getCheckedRadioButtonId())) == 1
                        && locationText.trim().length() == 0) {
                    locationTextError.setVisibility(View.VISIBLE);
                    errorExists = true;
                }
                if (errorExists) {
                    Toast.makeText(getActivity(), "Please fix all fields with errors",
                            Toast.LENGTH_SHORT).show();
                } else {
                    searchProgressDialog.show();
                    searchProgressDialog.setCancelable(false);

                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    String url = Server.serverBaseUrl + "places?lat=" + latitude +
                            "&long=" + longitude +
                            "&radius=" + getDistance(distance.getText().toString())
                            + "&category=" + getCategoryString(category.getSelectedItem().toString())
                            + "&keyword=" + keywordText;

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    searchProgressDialog.cancel();
                                    Intent intent = new Intent(getActivity(), DisplayResultsActivity.class);
                                    intent.putExtra("data", response);

                                    startActivity(intent);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            searchProgressDialog.cancel();
                            Toast.makeText(getActivity(), "Failed to get Search Results", Toast.LENGTH_SHORT).show();
                            System.out.println(error.toString());
                        }
                    });

                    queue.add(stringRequest);
                }
            }
        });

        clear.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword.getText().clear();
                locationTextField.getText().clear();
                distance.getText().clear();
                category.setSelection(0);
                locationRadios.check(R.id.locationRadio1);
                keywordError.setVisibility(View.GONE);
                locationTextError.setVisibility(View.GONE);
            }
        }));
        return rootView;
    }

    private String getCategoryString(String s) {
        return s.toLowerCase().replace(" ", "_");
    }

    private String getDistance(String s){
        return s.isEmpty() ? "10" : s;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        startLocationUpdates();
    }
}
