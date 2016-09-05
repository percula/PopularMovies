package com.perculacreative.peter.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // Set detail fragment

        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SELECTED_MOVIE_KEY, getIntent().getParcelableExtra(MainActivity.SELECTED_MOVIE_KEY));

        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail_scrollview, detailFragment, MainActivity.DETAILFRAGMENT_TAG)
                .commit();
    }
}
