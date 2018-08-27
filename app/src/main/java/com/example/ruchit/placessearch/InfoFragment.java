package com.example.ruchit.placessearch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ruchit.
 */

public class InfoFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        try {
            JSONObject placeDetails = new JSONObject(getActivity().getIntent().getStringExtra("data"));
            if(placeDetails.has("formatted_address")){
                String formatted_address = placeDetails.getString("formatted_address");
                TableRow row = rootView.findViewById(R.id.info_address);
                ((TextView)row.findViewById(R.id.info_address_key)).setText("Address");
                ((TextView)row.findViewById(R.id.info_address_value)).setText(formatted_address);
            }
            else{
                rootView.findViewById(R.id.info_address).setVisibility(View.GONE);
            }

            if(placeDetails.has("international_phone_number")){
                String phone_number = placeDetails.getString("international_phone_number");
                TableRow row = rootView.findViewById(R.id.info_phone_number);
                ((TextView)row.findViewById(R.id.info_phone_number_key)).setText("Phone Number");
                ((TextView)row.findViewById(R.id.info_phone_number_value)).setText(phone_number);
            }
            else{
                rootView.findViewById(R.id.info_phone_number).setVisibility(View.GONE);
            }

            if(placeDetails.has("price_level")){
                int price_level = Integer.parseInt(placeDetails.getString("price_level"));
                TableRow row = rootView.findViewById(R.id.info_price_level);
                ((TextView)row.findViewById(R.id.info_price_level_key)).setText("Price Level");
                StringBuilder priceString = new StringBuilder();
                for(int i=0; i<price_level; i++){
                    priceString.append("$");
                }
                ((TextView)row.findViewById(R.id.info_price_level_value)).setText(priceString.toString());
            }
            else{
                rootView.findViewById(R.id.info_price_level).setVisibility(View.GONE);
            }

            if(placeDetails.has("rating")){
                String rating = placeDetails.getString("rating");
                TableRow row = rootView.findViewById(R.id.info_rating);
                ((TextView)row.findViewById(R.id.info_rating_key)).setText("Rating");
                ((RatingBar)row.findViewById(R.id.info_rating_value)).setRating(Float.parseFloat(rating));
                ((RatingBar)row.findViewById(R.id.info_rating_value)).setIsIndicator(true);
            }
            else{
                rootView.findViewById(R.id.info_rating).setVisibility(View.GONE);
            }

            if(placeDetails.has("url")){
                String google_page = placeDetails.getString("url");
                TableRow row = rootView.findViewById(R.id.info_google_page);
                ((TextView)row.findViewById(R.id.info_google_page_key)).setText("Google Page");
                ((TextView)row.findViewById(R.id.info_google_page_value)).setText(google_page);
            }
            else{
                rootView.findViewById(R.id.info_google_page).setVisibility(View.GONE);
            }

            if(placeDetails.has("website")){
                String website = placeDetails.getString("website");
                TableRow row = rootView.findViewById(R.id.info_website);
                ((TextView)row.findViewById(R.id.info_website_key)).setText("Website");
                ((TextView)row.findViewById(R.id.info_website_value)).setText(website);
            }
            else{
                rootView.findViewById(R.id.info_website).setVisibility(View.GONE);
            }

            System.out.println("Hello");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootView;
    }
}
