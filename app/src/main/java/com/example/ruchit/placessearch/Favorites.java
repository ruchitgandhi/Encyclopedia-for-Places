package com.example.ruchit.placessearch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruchit.
 */

public class Favorites extends Fragment{

    RecyclerView rv;
    TextView noFavoritesMessage;
    SharedPreferences myPreferences;
    FavoritesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        View rootView = inflater.inflate(R.layout.favorites_recycler_view, container, false);

        rv = rootView.findViewById(R.id.favorites_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        noFavoritesMessage = rootView.findViewById(R.id.favorites_no_data);

        String favoritesArrayString = myPreferences.getString("Favorites", "unknown");
        if(favoritesArrayString.equals("unknown")){
            noFavoritesMessage.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
        else{
            noFavoritesMessage.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            List<String> arr = new Gson().fromJson(favoritesArrayString, List.class);
            List<ResultLine> resultObjList = new ArrayList<>();
            for(int i=0; i<arr.size(); i++){
                resultObjList.add(new Gson().fromJson(arr.get(i), ResultLine.class));
            }
            adapter = new FavoritesAdapter(resultObjList, getActivity(), this);
            rv.setAdapter(adapter);
        }
        return rootView;
    }

    public void updateRecyclerView(List<ResultLine> resultObjList) {
        if(resultObjList.isEmpty()){
            noFavoritesMessage.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
        else{
            adapter = new FavoritesAdapter(resultObjList, getActivity(), this);
            rv.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String favoritesArrayString = myPreferences.getString("Favorites", "unknown");
        if(favoritesArrayString.equals("unknown")){
            noFavoritesMessage.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
        else{
            noFavoritesMessage.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            List<String> arr = new Gson().fromJson(favoritesArrayString, List.class);
            List<ResultLine> resultObjList = new ArrayList<>();
            for(int i=0; i<arr.size(); i++){
                resultObjList.add(new Gson().fromJson(arr.get(i), ResultLine.class));
            }
            adapter = new FavoritesAdapter(resultObjList, getActivity(), this);
            rv.setAdapter(adapter);
        }
    }
}
