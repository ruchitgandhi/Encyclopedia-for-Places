package com.example.ruchit.placessearch;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AutocompleteResultType;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ruchit.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, DirectionCallback {
    PlacesAutocompleteTextView autocomplete;
    private GoogleMap googleMap;
    private LatLng origin;
    private LatLng destination;
    Spinner mapTravelMode;
    boolean directionMode = false;
    String destinationPlaceName, originPlaceName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_map, container, false);
        double lat = 0,lng = 0;
        try {
            JSONObject placeDetails = new JSONObject(getActivity().getIntent().getStringExtra("data"));
            JSONObject geometry = placeDetails.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            lat = Double.parseDouble(location.getString("lat"));
            lng = Double.parseDouble(location.getString("lng"));
            destinationPlaceName = placeDetails.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        autocomplete = rootview.findViewById(R.id.places_autocomplete);

        Location loc = new Location("");
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        destination = new LatLng(lat, lng);
        autocomplete.setCurrentLocation(loc);

        autocomplete.setLocationBiasEnabled(true);
        autocomplete.setResultType(AutocompleteResultType.GEOCODE);
        autocomplete.setOnPlaceSelectedListener(
                new OnPlaceSelectedListener() {
                    @Override
                    public void onPlaceSelected(final Place place) {
                        autocomplete.getDetailsFor(place, new DetailsCallback() {
                                    @Override
                                    public void onSuccess(final PlaceDetails details) {
                                        originPlaceName = details.name;
                                        origin = new LatLng(details.geometry.location.lat, details.geometry.location.lng);
                                        directionMode = true;
                                        requestDirection(getTravelMode());
                                    }

                                    @Override
                                    public void onFailure(final Throwable failure) {
                                        Toast.makeText(getActivity(), "Places Auto Complete in Maps Failed. Try Again.", Toast.LENGTH_SHORT).show();
                                        Log.d("test", "failure " + failure);
                                    }
                                }
                        );
                    }
                });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapTravelMode = rootview.findViewById(R.id.travel_mode_spinner);
        mapTravelMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onTravelModeChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return rootview;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(destination)
                .title(destinationPlaceName));
        placeMarker.showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));
    }

    public void onTravelModeChange(){
        if(directionMode){
            requestDirection(getTravelMode());
        }
    }

    public String getTravelMode(){
        if(mapTravelMode.getSelectedItemPosition() == 0){
            return TransportMode.DRIVING;
        }
        else if(mapTravelMode.getSelectedItemPosition() == 1){
            return TransportMode.BICYCLING;
        }
        else if(mapTravelMode.getSelectedItemPosition() == 2){
            return TransportMode.TRANSIT;
        }
        return TransportMode.WALKING;
    }

    public void requestDirection(String modeOfTransport) {
        GoogleDirection.withServerKey("<YOUR_API_KEY>")
                .from(origin)
                .to(destination)
                .transportMode(modeOfTransport)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            googleMap.clear();
            Route route = direction.getRouteList().get(0);
            Marker sourceMarker = googleMap.addMarker(new MarkerOptions().position(origin).title(originPlaceName));
            sourceMarker.showInfoWindow();
            googleMap.addMarker(new MarkerOptions().position(destination));

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.BLUE));
            moveCameraWithDirections(route);
        } else {

        }
    }

    private void moveCameraWithDirections(Route route) {
        LatLng sw = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng ne = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(sw, ne);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        //
    }


}
