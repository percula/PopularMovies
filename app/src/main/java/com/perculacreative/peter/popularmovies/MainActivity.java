package com.perculacreative.peter.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements GridFragment.Callback {


    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String GRIDFRAGMENT_TAG = "GRIDTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridFragment gridFragment = new GridFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, gridFragment, GRIDFRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onItemSelected(MovieItem selectedMovie) {
        Bundle args = new Bundle();
        args.putParcelable(GridFragment.CURRENT_MOVIE_KEY, selectedMovie);

        DetailFragment fragment = new DetailFragment();
//        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, DETAILFRAGMENT_TAG)
                .commit();

        Log.v("onItemSelected", "Clicked");
    }
}
