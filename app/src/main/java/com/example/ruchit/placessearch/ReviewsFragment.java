package com.example.ruchit.placessearch;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class ReviewsFragment extends Fragment{
    List<ReviewLine> reviewLineList = new ArrayList<>();
    List<ReviewLine> googleReviewsList = new ArrayList<>();
    List<ReviewLine> yelpReviewList = new ArrayList<>();
    ReviewAdapter adapter;
    RecyclerView rv;
    TextView noReviewsMessage;
    Spinner reviewTypes, reviewSortingOrder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_reviews, container, false);
        rv = rootview.findViewById(R.id.reviews_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), 0));
        noReviewsMessage = rootview.findViewById(R.id.reviews_no_data);

        reviewTypes = rootview.findViewById(R.id.reviewTypes);
        reviewTypes.setSelection(0);
        reviewTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemSelectedForReviewType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        reviewSortingOrder = rootview.findViewById(R.id.reviewSortingOrder);
        reviewSortingOrder.setSelection(0);
        reviewSortingOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemSelectedForReviewSorting(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                System.out.println("");
            }
        });

        return rootview;
    }

    public void onItemSelectedForReviewType(int position){
        if (position == 0) { //Google Reviews
            try {
                if(googleReviewsList.size() > 0){
                    rv.setVisibility(View.VISIBLE);
                    noReviewsMessage.setVisibility(View.GONE);
                    reviewLineList.clear();
                    for (ReviewLine rl : googleReviewsList) {
                        reviewLineList.add(rl);
                    }
                    onItemSelectedForReviewSorting(reviewSortingOrder.getSelectedItemPosition());
                }
                else {
                    JSONObject placeDetails = new JSONObject(getActivity().getIntent().getStringExtra("data"));
                    if (placeDetails.has("reviews")) {
                        rv.setVisibility(View.VISIBLE);
                        noReviewsMessage.setVisibility(View.GONE);
                        if (googleReviewsList.size() == 0) {
                            googleReviewsList = getGoogleReviews();
                        }
                        reviewLineList.clear();
                        for (ReviewLine rl : googleReviewsList) {
                            reviewLineList.add(rl);
                        }
                        onItemSelectedForReviewSorting(reviewSortingOrder.getSelectedItemPosition());
                    } else {
                        reviewLineList.clear();
                        rv.setVisibility(View.GONE);
                        noReviewsMessage.setVisibility(View.VISIBLE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (position == 1) { //Yelp Reviews
            if(yelpReviewList.size() > 0){
                reviewLineList.clear();
                for (ReviewLine rl : yelpReviewList) {
                    reviewLineList.add(rl);
                }
                onItemSelectedForReviewSorting(reviewSortingOrder.getSelectedItemPosition());
            }
            else {
                getAndDisplayYelpReviews();
            }
        }
    }

    public void onItemSelectedForReviewSorting(int position){
        if (position == 0) {
            if(reviewTypes.getSelectedItem().toString().trim().equals("Google Reviews")){
                adapter = new ReviewAdapter(googleReviewsList, getActivity());
            }
            else{
                adapter = new ReviewAdapter(yelpReviewList, getActivity());
            }
            rv.setAdapter(adapter);
        } else {
            if (position == 1) { //Highest Rating
                reviewLineList.sort(new Comparator<ReviewLine>() {
                    @Override
                    public int compare(ReviewLine o1, ReviewLine o2) {
                        return Float.parseFloat(o1.getRating()) < Float.parseFloat(o2.getRating()) ? 1 : -1;
                    }
                });
            } else if (position == 2) { //Lowest Rating
                reviewLineList.sort(new Comparator<ReviewLine>() {
                    @Override
                    public int compare(ReviewLine o1, ReviewLine o2) {
                        return Float.parseFloat(o1.getRating()) < Float.parseFloat(o2.getRating()) ? -1 : 1;
                    }
                });
            } else if (position == 3) { //Most Recent
                reviewLineList.sort(new Comparator<ReviewLine>() {
                    @Override
                    public int compare(ReviewLine o1, ReviewLine o2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            return sdf.parse(o1.getTimestamp()).before(sdf.parse(o2.getTimestamp())) ? 1 : -1;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
            } else if (position == 4) { //Least Recent
                reviewLineList.sort(new Comparator<ReviewLine>() {
                    @Override
                    public int compare(ReviewLine o1, ReviewLine o2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            return sdf.parse(o1.getTimestamp()).before(sdf.parse(o2.getTimestamp())) ? -1 : 1;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
            }

            adapter = new ReviewAdapter(reviewLineList, getActivity());
            rv.setAdapter(adapter);
        }
    }

    public List<ReviewLine> getGoogleReviews() {
        List<ReviewLine> reviewList = new ArrayList<>();
        try {
            JSONObject placeDetails = new JSONObject(getActivity().getIntent().getStringExtra("data"));
            if (placeDetails.has("reviews")) {
                JSONArray reviewsArray = placeDetails.getJSONArray("reviews");
                for (int i = 0; i < reviewsArray.length(); i++) {
                    JSONObject j = (JSONObject) reviewsArray.get(i);
                    ReviewLine reviewLine = new ReviewLine(j.getString("author_name"), j.getString("author_url"),
                            j.getString("profile_photo_url"), j.getString("rating"), j.getString("text"),
                            getDateTime(Long.parseLong(j.getString("time"))));
                    reviewList.add(reviewLine);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reviewList;
    }

    public void getAndDisplayYelpReviews() {
        String dataForYelpMatchCall = getDataForYelpMatchCall();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Server.serverBaseUrl + "yelp/match?" + dataForYelpMatchCall;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        yelpReviewList = new ArrayList<>();
                        try {
                            if(!response.equals("{}")){
                                JSONArray reviewsArray = new JSONObject(new Gson().fromJson(response, String.class)).getJSONArray("reviews");
                                for(int i = 0; i < reviewsArray.length(); i++) {
                                    JSONObject j = (JSONObject) reviewsArray.get(i);
                                    JSONObject user = j.getJSONObject("user");
                                    ReviewLine reviewLine = new ReviewLine(user.getString("name"), j.getString("url"),
                                            user.getString("image_url"), j.getString("rating"), j.getString("text"),
                                            j.getString("time_created"));
                                    yelpReviewList.add(reviewLine);
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "Getting Yelp Reviews Failed", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        if (yelpReviewList.size() > 0) {
                            rv.setVisibility(View.VISIBLE);
                            noReviewsMessage.setVisibility(View.GONE);
                            reviewLineList.clear();
                            for (ReviewLine rl : yelpReviewList) {
                                reviewLineList.add(rl);
                            }
                            onItemSelectedForReviewSorting(reviewSortingOrder.getSelectedItemPosition());
                        } else {
                            rv.setVisibility(View.GONE);
                            noReviewsMessage.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Getting Yelp Reviews Failed", Toast.LENGTH_SHORT).show();
                System.out.println(error.toString());
            }
        });

        queue.add(stringRequest);
    }

    public String getDataForYelpMatchCall() {
        try {
            StringBuilder query = new StringBuilder();
            JSONObject placeDetails = new JSONObject(getActivity().getIntent().getStringExtra("data"));
            query.append("name=");
            query.append(Uri.encode(placeDetails.getString("name")));

            JSONArray address = placeDetails.getJSONArray("address_components");
            String temp;
            for (int i = 0; i < address.length(); i++) {
                temp = ((JSONObject) address.get(i)).getString("types");
                if (temp.contains("postal_code")) {
                    query.append("&");
                    query.append("postal_code=");
                    query.append(Uri.encode(((JSONObject) address.get(i)).getString("short_name")));

                } else if (temp.contains("country")) {
                    query.append("&");
                    query.append("country=");
                    query.append(Uri.encode(((JSONObject) address.get(i)).getString("short_name")));
                } else if (temp.contains("administrative_area_level_1")) {
                    query.append("&");
                    query.append("state=");
                    query.append(Uri.encode(((JSONObject) address.get(i)).getString("short_name")));
                } else if (temp.contains("locality")) {
                    query.append("&");
                    query.append("city=");
                    query.append(Uri.encode(((JSONObject) address.get(i)).getString("short_name")));
                } else if (temp.contains("route")) {
                    query.append("&");
                    query.append("address1=");
                    query.append(Uri.encode(((JSONObject) address.get(i)).getString("short_name")));
                }
            }

            query.append("&");
            query.append("latitude=");
            query.append(Double.parseDouble(placeDetails.getJSONObject("geometry").getJSONObject("location").getString("lat")));

            query.append("&");
            query.append("longitude=");
            query.append(Double.parseDouble(placeDetails.getJSONObject("geometry").getJSONObject("location").getString("lng")));

            return query.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getDateTime(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long time = timestamp * 1000;
            TimeZone timeZone = TimeZone.getDefault();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));
            java.util.Date currentZone = calendar.getTime();
            return sdf.format(currentZone);
        } catch (Exception e) {
        }
        return "";
    }
}
