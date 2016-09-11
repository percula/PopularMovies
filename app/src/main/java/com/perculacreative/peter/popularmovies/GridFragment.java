package com.perculacreative.peter.popularmovies;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GridFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private final String LOG_TAG = GridFragment.class.getSimpleName();

    private MoviePosterAdapter mMovieAdapter;
    private ArrayList<MovieItem> mMovieList = new ArrayList<MovieItem>();

    private boolean mSortOrderPopular;
    private boolean mSortOrderFavorites;
    private boolean mSortOrderSearch;


    private String API_KEY_LABEL = "api_key";
    private String API_KEY = BuildConfig.API_KEY;

    public static final String MOVIE_LIST_KEY = "MOVIE_LIST";

    private ArrayList<MovieItem> mFavoriteMovies = new ArrayList<>();

    public GridFragment() {
        // Required empty public constructor
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(MovieItem selectedMovie);

        public void moviesLoaded(MovieItem firstMovie);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        // Retrieve movie list when device is rotated
        if (savedInstanceState != null) {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
            Log.v("Retrieved Movie List", mMovieList.size() + "");

        } else {
            getMovieData(rootView, "");
        }

        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_KEY, 0);
        mSortOrderPopular = settings.getBoolean(MainActivity.PREFS_SORT_KEY, true);

        // Create custom adapter
        mMovieAdapter = new MoviePosterAdapter(getActivity(), mMovieList);

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MovieItem selectedMovie = mMovieAdapter.getItem(i);
                ((Callback) getActivity()).onItemSelected(selectedMovie);
            }
        });

        return rootView;
    }

    private void loadFavoriteMovies()
            throws JSONException {
        // Get favorites from sharedpreferences
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_KEY, 0);
        String favoriteString = prefs.getString(MainActivity.FAVORITE_STRING_KEY, null);
        if (favoriteString != null) {
            Log.v("favoriteString", favoriteString);
            Type type = new TypeToken<List<MovieItem>>() {
            }.getType();
            Gson gson = new Gson();
            mFavoriteMovies = gson.fromJson(favoriteString, type);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE_LIST_KEY, mMovieList);
        Log.v("Putting Array List", "NOW");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_grid, menu);

        final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                mSortOrderSearch = true;
                Log.v("SearchQuery", query);
                getMovieData(getView(), query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_order:
                showSortMenu();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remember sort preference for next time
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_KEY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MainActivity.PREFS_SORT_KEY, mSortOrderPopular).apply();
    }

    private void getMovieData(View view, String searchQuery) {
        mMovieList.clear();

        if (isOnline()) {
            view.findViewById(R.id.network_error).setVisibility(View.GONE);
            FetchMoviesTask moviesTask = new FetchMoviesTask();
            moviesTask.execute(searchQuery);
        } else {
            mMovieAdapter.notifyDataSetChanged();
            view.findViewById(R.id.network_error).setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This checks if there is an internet connection. This is from http://stackoverflow.com/a/4009133.
     *
     * @return boolean whether the user has an internet connection.
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showSortMenu() {
        View sortButton = getActivity().findViewById(R.id.sort_order);
        PopupMenu sortPopup = new PopupMenu(getActivity(), sortButton);
        sortPopup.setOnMenuItemClickListener(this);
        sortPopup.getMenuInflater().inflate(R.menu.sort, sortPopup.getMenu());
        if (mSortOrderPopular && !mSortOrderFavorites) {
            sortPopup.getMenu().getItem(0).setChecked(true);
        }
        if (!mSortOrderPopular && !mSortOrderFavorites) {
            sortPopup.getMenu().getItem(1).setChecked(true);
        }
        if (mSortOrderFavorites) {
            sortPopup.getMenu().getItem(2).setChecked(true);
        }
        sortPopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_popular:
                mSortOrderPopular = true;
                mSortOrderFavorites = false;
                getMovieData(getView(), "");
                return true;
            case R.id.sort_rated:
                mSortOrderPopular = false;
                mSortOrderFavorites = false;
                getMovieData(getView(), "");
                return true;
            case R.id.sort_favorites:
                mSortOrderPopular = false;
                mSortOrderFavorites = true;

                // Get favorite movies
                try {
                    loadFavoriteMovies();
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }

                mMovieList.clear();
                mMovieList.addAll(mFavoriteMovies);
                mMovieAdapter.notifyDataSetChanged();

                // If there are any favorite movies, show the first one by default
                if (mMovieList.size() > 0) {
                    ((Callback) getActivity()).moviesLoaded(mMovieList.get(0));
                }
            default:
                return false;
        }
    }


    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private ProgressDialog progressDialog;


        /**
         * Parses JSON data and returns a string array for the list adapter
         */
        private String[] getMovieDataFromJson(String movieJSONStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MV_RESULTS = "results";
            final String MV_ID = "id";
            final String MV_TITLE = "title";
            final String MV_POSTER = "poster_path";
            final String MV_RELEASE = "release_date";
            final String MV_VOTE = "vote_average";
            final String MV_PLOT = "overview";

//            final String GB_INFO = "volumeInfo";

            JSONObject movieJson = new JSONObject(movieJSONStr);
            JSONArray movieArray = movieJson.getJSONArray(MV_RESULTS);

            //Determine number of returned results
            int numReturnedResults = movieArray.length();

            //If no results, return error string array
            if (numReturnedResults == 0) {
                String[] resultStrs = new String[1];
                resultStrs[0] = "No results found. Please try again.";
                return resultStrs;
            }

            //Create string array with length of returned results
            String[] resultStrs = new String[numReturnedResults];

            for (int i = 0; i < movieArray.length(); i++) {
                // Get the JSON object representing the current movie
                JSONObject currentMovie = movieArray.getJSONObject(i);

                // Get movie info
                String id = currentMovie.getString(MV_ID);
                String title = currentMovie.getString(MV_TITLE);
                String poster = currentMovie.getString(MV_POSTER);
                String release = currentMovie.getString(MV_RELEASE);
                String vote = currentMovie.getString(MV_VOTE);
                String plot = currentMovie.getString(MV_PLOT);

                mMovieList.add(new MovieItem(id, title, poster, release, vote, plot));
            }
            return resultStrs;
        }

        // I used help from http://stackoverflow.com/a/9170457 for this progress bar code
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr = null;

            String query = "";
            if (params.length > 0) {
                query = params[0];
                Log.v("params0", params[0]);
            }

            try {
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3";
                final String MOVIE = "movie";
                final String SEARCH = "search";
                final String QUERY_KEY = "query";
                final String MOVIE_SORT_POPULAR = "popular";
                final String MOVIE_SORT_RATING = "top_rated";
                final String MOVIE_SORT_SEARCH = "movie";

                Uri builtUri;

                if (mSortOrderSearch) {
                    builtUri = Uri.parse(MOVIE_BASE_URL)
                            .buildUpon()
                            .appendPath(SEARCH)
                            .appendPath(MOVIE_SORT_SEARCH)
                            .appendQueryParameter(QUERY_KEY, query)
                            .appendQueryParameter(API_KEY_LABEL, API_KEY)
                            .build();
                } else if (mSortOrderPopular) {
                    builtUri = Uri.parse(MOVIE_BASE_URL)
                            .buildUpon()
                            .appendPath(MOVIE)
                            .appendPath(MOVIE_SORT_POPULAR)
                            .appendQueryParameter(API_KEY_LABEL, API_KEY)
                            .build();
                } else {
                    builtUri = Uri.parse(MOVIE_BASE_URL)
                            .buildUpon()
                            .appendPath(MOVIE)
                            .appendPath(MOVIE_SORT_RATING)
                            .appendQueryParameter(API_KEY_LABEL, API_KEY)
                            .build();
                }

                URL url = new URL(builtUri.toString());

                // Create the request to Google Books API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJsonStr = buffer.toString();

//                Log.v(LOG_TAG, "JSON String: " + moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mMovieAdapter.notifyDataSetChanged();
            progressDialog.dismiss();

            // If there were any movies downloaded, show the first one by default
            if (mMovieList.size() > 0) {
                ((Callback) getActivity()).moviesLoaded(mMovieList.get(0));
            }
            mSortOrderSearch = false;
        }
    }
}
