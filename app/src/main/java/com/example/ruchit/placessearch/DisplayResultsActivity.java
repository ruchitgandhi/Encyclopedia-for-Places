package com.example.ruchit.placessearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ruchit.
 */

public class DisplayResultsActivity extends AppCompatActivity {

    int currentPageSelection = 1;
    Map<Integer, List<ResultLine>> resultsMap = new HashMap<>();
    String nextPageToken = "";
    ResultAdapter adapter;
    RecyclerView rv;
    Button nextButton, previousButton;
    Context context;
    SharedPreferences myPreferences;
    ProgressDialog progressDialog;
    TextView noResultsMessage;
    List<ResultLine> resultObjList;

    @Override
    protected void onResume() {
        super.onResume();
        updateFavoritesOverWholeList();
        ResultAdapter adapter = new ResultAdapter(resultObjList, this);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_recycler_view);
        context = this;
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        progressDialog = new ProgressDialog(this);

        getSupportActionBar().setTitle("Search Results");
        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickNextButton(v);
            }
        });
        previousButton = findViewById(R.id.previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPreviousButton(v);
            }
        });
        noResultsMessage = findViewById(R.id.results_no_data);

        rv = findViewById(R.id.results_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        JSONObject dataJson = null;
        try {
            dataJson = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        resultObjList = formResultsObjListFromJson(data);
        if (resultObjList.isEmpty()) {
            rv.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            previousButton.setVisibility(View.GONE);

            noResultsMessage.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            previousButton.setVisibility(View.VISIBLE);

            noResultsMessage.setVisibility(View.GONE);

            resultsMap.put(currentPageSelection, resultObjList);
            ResultAdapter adapter = new ResultAdapter(resultObjList, this);
            rv.setAdapter(adapter);

            if (dataJson.has("next_page_token")) {
                nextButton.setEnabled(true);
                try {
                    nextPageToken = dataJson.getString("next_page_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onClickNextButton(View view) {
        currentPageSelection++;
        if (resultsMap.size() >= currentPageSelection) {
            resultObjList = resultsMap.get(currentPageSelection);
            updateFavoritesOverWholeList();
            adapter = new ResultAdapter(resultObjList, this);
            rv.setAdapter(adapter);
            previousButton.setEnabled(true);
            if (resultsMap.size() == currentPageSelection) {
                nextButton.setEnabled(false);
            }
        } else {
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Fetching Next Page");

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Server.serverBaseUrl + "places/more?pagetoken=" + nextPageToken;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (!response.equals("{}")) {
                                progressDialog.cancel();
                                try {
                                    resultObjList = formResultsObjListFromJson(response);
                                    adapter = new ResultAdapter(resultObjList, context);
                                    rv.setAdapter(adapter);
                                    resultsMap.put(currentPageSelection, resultObjList);
                                    JSONObject data = new JSONObject(response);
                                    if (data.has("next_page_token")) {
                                        nextPageToken = data.getString("next_page_token");
                                        nextButton.setEnabled(true);
                                    } else {
                                        nextPageToken = "";
                                        nextButton.setEnabled(false);
                                    }
                                    previousButton.setEnabled(true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(context, "Next Page did not return any results", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Getting Place Details Failed", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                    System.out.println(error.toString());
                }
            });

            queue.add(stringRequest);
        }
    }

    public void onClickPreviousButton(View view) {
        currentPageSelection--;
        if (currentPageSelection > 1) {
            previousButton.setEnabled(true);
        } else {
            previousButton.setEnabled(false);
        }
        nextButton.setEnabled(true);
        resultObjList = resultsMap.get(currentPageSelection);
        updateFavoritesOverWholeList();
        adapter = new ResultAdapter(resultObjList, context);
        rv.setAdapter(adapter);
    }

    public void updateFavoritesOverWholeList(){
        for(ResultLine res : resultObjList){
            if(checkIfFavorite(res.getPlaceId())){
                res.setFavorite(true);
            }
            else{
                res.setFavorite(false);
            }
        }
    }

    private List<ResultLine> formResultsObjListFromJson(String data) {
        List<ResultLine> resultObjList = new ArrayList<>();
        try {
            JSONArray resultsJson = new JSONObject(data).getJSONArray("results");
            for (int i = 0; i < resultsJson.length(); i++) {

                JSONObject j = resultsJson.getJSONObject(i);
                JSONObject geometry = j.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                boolean isFavorite = checkIfFavorite(j.getString("place_id"));
                ResultLine result = new ResultLine(j.get("name").toString(),
                        j.get("vicinity").toString(), j.get("icon").toString(),
                        (double) location.get("lat"), (double) location.get("lng"), j.get("place_id").toString(), isFavorite);
                resultObjList.add(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObjList;
    }

    private boolean checkIfFavorite(String place_id) {
        String favoritesArrayString = myPreferences.getString("Favorites", "unknown");
        if (favoritesArrayString.equals("unknown")) {
            return false;
        } else {
            return favoritesArrayString.contains(place_id);
        }
    }
}
