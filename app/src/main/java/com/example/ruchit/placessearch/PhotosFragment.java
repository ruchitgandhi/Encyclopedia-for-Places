package com.example.ruchit.placessearch;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PhotosFragment extends Fragment {

    GeoDataClient mGeoDataClient;
    ImageView imageView;
    RecyclerView rv;
    List<Bitmap> photoBitmaps = new ArrayList<>();
    int photoMetadataBufferSize;
    TextView noPhotosMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_photos_recycler_view, container, false);

        rv = rootview.findViewById(R.id.photos_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        noPhotosMessage = rootview.findViewById(R.id.photos_no_data);

        mGeoDataClient = Places.getGeoDataClient(getActivity());
        try {
            JSONObject placeDetails = new JSONObject(getActivity().getIntent().getStringExtra("data"));
            getPhotos(placeDetails.getString("place_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootview;

    }

    private void getPhotos(String placeId) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                photoMetadataBufferSize = photoMetadataBuffer.getCount();
                if(photoMetadataBufferSize > 0){
                    photoBitmaps.clear();
                    for(PlacePhotoMetadata photoMetadata : photoMetadataBuffer){

                        Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                                PlacePhotoResponse photo = task.getResult();
                                photoBitmaps.add(photo.getBitmap());
                                if(photoBitmaps.size() == photoMetadataBufferSize){
                                    rv.setVisibility(View.VISIBLE);
                                    noPhotosMessage.setVisibility(View.GONE);
                                    PhotosAdapter adapter = new PhotosAdapter(photoBitmaps, getActivity());
                                    rv.setAdapter(adapter);
                                }
                            }
                        });
                    }
                    photoMetadataBuffer.release();
                }
                else{
                    rv.setVisibility(View.GONE);
                    noPhotosMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
