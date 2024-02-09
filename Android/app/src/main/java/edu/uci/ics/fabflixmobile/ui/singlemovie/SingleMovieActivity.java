package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivitySingleMovieBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends AppCompatActivity {
    private TextView title;
    private TextView year;
    private TextView director;
    private TextView genres;
    private TextView stars;
    private String movieid;
    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "3.141.4.249";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySingleMovieBinding binding = ActivitySingleMovieBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        movieid = extras.getString("id");

        title = binding.singleMovieTitle;
        year = binding.singleMovieYear;
        director = binding.singleMovieDirector;
        genres = binding.singleMovieGenres;
        stars = binding.singleMovieStars;

        populateSingleMoviePage();
    }

    private void populateSingleMoviePage() {
        Log.d("singlemovie", "entered populating");
        Log.d("singlemovie", movieid);
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + movieid,
                response -> {
                    Log.d("singlemovie", "got response back");
                    Log.d("singlemovie", response);
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONArray(response).getJSONObject(0);
                        Log.d("singlemovie", "Parsed response to json object");
                        title.setText(jsonResponse.getString("movie_title"));
                        year.setText(jsonResponse.getString("movie_year"));
                        director.setText(jsonResponse.getString("movie_director"));
                        genres.setText(jsonResponse.getString("genre_name_list"));
                        stars.setText(jsonResponse.getString("star_name_list"));
                    } catch (Throwable t) {
                        Log.e("singlemovie", "Could not parse malformed JSON");
                    }

                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {};
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}