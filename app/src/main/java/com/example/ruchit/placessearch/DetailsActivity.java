package com.example.ruchit.placessearch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ruchit.
 */

public class DetailsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private boolean isFavoriteItem = false;
    SharedPreferences myPreferences;
    JSONObject currentPlaceJsonObj;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_details, menu);
        if (isFavoriteItem) {
            menu.getItem(1).setIcon(R.drawable.heart_white_filled);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_bar_favorite:
                if (!isFavoriteItem) {
                    try {
                        addToFavorites();
                        isFavoriteItem = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    item.setIcon(R.drawable.heart_white_filled);
                } else {
                    try {
                        removeFromFavorites();
                        isFavoriteItem = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    item.setIcon(R.drawable.heart_white_outline);
                }
                return true;

            case R.id.action_bar_twitter:
                String twitterText = "";
                try {
                    twitterText = "Check out " + currentPlaceJsonObj.getString("name") + " located at " +
                            currentPlaceJsonObj.getString("formatted_address") +
                            ".\n" + " Website: " + currentPlaceJsonObj.getString("website");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/intent/tweet?text=" + twitterText + " &hashtags=TravelAndEntertainmentSearch"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(mSectionsPagerAdapter.getTabDesign(i));
        }

        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        String placeNameHeader = "";
        String placeId = "";
        try {
            currentPlaceJsonObj = new JSONObject(data);
            placeNameHeader = currentPlaceJsonObj.getString("name");
            placeId = currentPlaceJsonObj.getString("place_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        isFavoriteItem = inFavorites(placeId);

        getSupportActionBar().setTitle(placeNameHeader);
        Bundle bundle = new Bundle();
        bundle.putString("place_details_data", data);
        InfoFragment infoFragmentObj = new InfoFragment();
        infoFragmentObj.setArguments(bundle);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new InfoFragment(), "Info");
        adapter.addFragment(new PhotosFragment(), "Photos");
        adapter.addFragment(new MapFragment(), "Map");
        adapter.addFragment(new ReviewsFragment(), "Reviews");
        viewPager.setAdapter(adapter);
    }

    private boolean inFavorites(String placeId) {
        if (!placeId.isEmpty()) {
            String favoritesArrayString = myPreferences.getString("Favorites", "unknown");
            if (favoritesArrayString.equals("unknown")) {
                return false;
            } else {
                return favoritesArrayString.contains(placeId);
            }
        }
        return false;
    }

    private void addToFavorites() throws JSONException {
        JSONObject geometry = currentPlaceJsonObj.getJSONObject("geometry");
        JSONObject location = geometry.getJSONObject("location");
        ResultLine selectedPlaceObj = new ResultLine(currentPlaceJsonObj.get("name").toString(),
                currentPlaceJsonObj.get("vicinity").toString(), currentPlaceJsonObj.get("icon").toString(),
                (double) location.get("lat"), (double) location.get("lng"),
                currentPlaceJsonObj.get("place_id").toString(), isFavoriteItem);

        SharedPreferences.Editor myEditor = myPreferences.edit();
        String name = myPreferences.getString("Favorites", "unknown");
        List<String> existingFavs;
        if (name.equals("unknown")) {
            existingFavs = new ArrayList<>();
        } else {
            existingFavs = new Gson().fromJson(name, List.class);
        }
        existingFavs.add(selectedPlaceObj.toString());
        myEditor.putString("Favorites", new Gson().toJson(existingFavs));
        myEditor.commit();

        Toast.makeText(this, currentPlaceJsonObj.getString("name") + " was added to favorites",
                Toast.LENGTH_SHORT).show();
    }

    private void removeFromFavorites() throws JSONException {
        String placeId = currentPlaceJsonObj.getString("place_id");

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
        Toast.makeText(this, currentPlaceJsonObj.getString("name") + " was removed from favorites",
                Toast.LENGTH_SHORT).show();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private List<String> tabHeadings = Arrays.asList("INFO", "PHOTOS", "MAP", "REVIEWS");
        private List<Integer> tabIcons = Arrays.asList(R.drawable.info_icon, R.drawable.photos_icon,
                R.drawable.map_icon, R.drawable.reviews_icon);

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public View getTabDesign(int pos) {
            View view = LayoutInflater.from(DetailsActivity.this).inflate(R.layout.tab_layout, null);
            TextView tv = view.findViewById(R.id.tab_text_view);
            ImageView img = view.findViewById(R.id.tab_image_view);
            tv.setText(tabHeadings.get(pos));
            img.setImageResource(tabIcons.get(pos));
            return view;
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }
}
