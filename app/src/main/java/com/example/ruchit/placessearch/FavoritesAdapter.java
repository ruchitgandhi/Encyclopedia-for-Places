package com.example.ruchit.placessearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Ruchit.
 */

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ResultViewHolder> {

    List<ResultLine> results;
    Context context;
    Favorites parentObj;

    FavoritesAdapter(List<ResultLine> results, Context context, Favorites parentObj) {
        this.results = results;
        this.context = context;
        this.parentObj = parentObj;
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.results_card_view, viewGroup, false);
        ResultViewHolder pvh = new ResultViewHolder(v, context, results);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ResultViewHolder resultViewHolder, int i) {
        resultViewHolder.placeName.setText(results.get(i).getPlaceName());
        resultViewHolder.placeAddress.setText(results.get(i).getPlaceAddress());
        Picasso.get().load(results.get(i).getCategoryImgUrl()).into(resultViewHolder.categoryImage);
        resultViewHolder.removeFromFavorites.setImageResource(R.drawable.heart_red_filled);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView placeAddress;
        TextView placeName;
        ImageView categoryImage;
        ImageButton removeFromFavorites;
        Context context;
        List<ResultLine> results;
        SharedPreferences myPreferences;
        ProgressDialog detailsProgressDialog;

        ResultViewHolder(View itemView, Context context, List<ResultLine> results) {
            super(itemView);
            this.context = context;
            myPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            this.results = results;
            cv = itemView.findViewById(R.id.result_card_view);
            placeAddress = itemView.findViewById(R.id.place_address);
            placeName = itemView.findViewById(R.id.place_name);
            categoryImage = itemView.findViewById(R.id.category_image);
            removeFromFavorites = itemView.findViewById(R.id.favorite);
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCardView(v);
                }
            });

            removeFromFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickFavorites(v);
                }
            });

            detailsProgressDialog = new ProgressDialog(context);
            detailsProgressDialog.setMessage("Fetching Details");
        }

        public void onClickCardView(View v) {
            int position = getAdapterPosition();
            ResultLine clickedResult = this.results.get(position);
            String placeId = clickedResult.getPlaceId();

            RequestQueue queue = Volley.newRequestQueue(context);
            String url = Server.serverBaseUrl + "placeDetails?placeId=" + placeId;

            detailsProgressDialog.show();
            detailsProgressDialog.setCancelable(false);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            detailsProgressDialog.cancel();
                            if(!response.equals("{}")){
                                Intent intent = new Intent(context, DetailsActivity.class);
                                try {
                                    intent.putExtra("data", new JSONObject(response).get("result").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                context.startActivity(intent);
                            }
                            else{
                                Toast.makeText(context, "No Place Details were returned", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    detailsProgressDialog.cancel();
                    Toast.makeText(context, "Getting Place Details Failed", Toast.LENGTH_SHORT).show();
                    System.out.println(error.toString());
                }
            });
            queue.add(stringRequest);
        }

        public void onClickFavorites(View v) {
            int clickedPlacePosition = getAdapterPosition();
            ResultLine selectedPlace = this.results.get(clickedPlacePosition);
            String removedPlaceName = selectedPlace.getPlaceName();
            this.results.remove(clickedPlacePosition);
            removeFromFavorites(selectedPlace.getPlaceId());
            parentObj.updateRecyclerView(this.results);
            Toast.makeText(context, removedPlaceName + " was removed from favorites",
                    Toast.LENGTH_SHORT).show();
        }

        private void removeFromFavorites(String placeId) {
            SharedPreferences.Editor myEditor = myPreferences.edit();
            String favoritesArrayString = myPreferences.getString("Favorites", "unknown");
            List<String> arr = new Gson().fromJson(favoritesArrayString, List.class);
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).toString().contains(placeId)) {
                    arr.remove(i);
                    break;
                }
            }
            if (arr.isEmpty()) {
                myEditor.remove("Favorites");
            } else {
                myEditor.putString("Favorites", new Gson().toJson(arr));
            }
            myEditor.commit();
        }
    }
}