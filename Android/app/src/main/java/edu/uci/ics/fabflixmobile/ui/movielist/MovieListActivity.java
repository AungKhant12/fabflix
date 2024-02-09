package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.data.model.SearchParams;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "3.141.4.249";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private SearchParams searchparams = new SearchParams();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);

        Bundle extras = getIntent().getExtras();
        Log.d("movielist", extras.getString("title"));
        searchparams.setTitle(extras.getString("title"));
        Log.d("movielist", searchparams.getTitle());

//        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        final Button nextButton = (Button) findViewById(R.id.next);
        final Button prevButton = (Button) findViewById(R.id.prev);

        nextButton.setOnClickListener(view -> nextPage());
        prevButton.setOnClickListener(view -> prevPage());

        populateMovieList();
    }

    private void prevPage() {
        searchparams.decrementPageNum();
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(null);
        populateMovieList();
    }
    private void nextPage() {
        searchparams.incrementPageNum();
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(null);
        populateMovieList();
    }
    private void populateMovieList() {
        Log.d("movielist", "entered populateMovieList");
        Log.d("movielist", searchparams.getPageNum());
        RequestQueue queue = NetworkManager.sharedManager(this).queue;
        StringRequest movieRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/fulltextsearch?page=" + searchparams.getPageNum() + "&limit=10&sortPriority=rating&ratingOrder=desc&titleOrder=asc&query=" + searchparams.getTitle(),
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("movielist", "got response back from servlet");
                    Log.d("movielist", response);
                    JSONArray jsonResponseArray = null;
                    try {
                        jsonResponseArray = new JSONArray(response);
                        Log.d("movielist", "Parsed response to jsonArray");
                        final ArrayList<Movie> movies = new ArrayList<>();

                        for (int i = 0; i < jsonResponseArray.length(); i++) {
                            JSONObject responsemovie = jsonResponseArray.getJSONObject(i);
                            movies.add(new Movie(responsemovie.getString("movie_title"), responsemovie.getString("movie_year"), responsemovie.getString("movie_director"), responsemovie.getString("genre_name_list"), responsemovie.getString("star_name_list"), responsemovie.getString("movie_id")));
                        }
                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Log.d("movielist", "item clicked on");
                            Movie movie = movies.get(position);
                            Log.d("movielist", "got movie with position" + position);
                            Log.d("movielist", "this is movie id: " + movie.getId() + " for movie: " + movie.getName());
                            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            SingleMoviePage.putExtra("id", movie.getId());
                            finish();
                            startActivity(SingleMoviePage);
//                            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
//                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        });
                    } catch (Throwable t) {
                        Log.e("movielist", "Could not parse malformed JSON");
                    }

                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {};
        // important: queue.add is where the login request is actually sent
        Log.d("movielist", String.valueOf(movieRequest));
        queue.add(movieRequest);
    }
}