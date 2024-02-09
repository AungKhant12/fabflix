package edu.uci.ics.fabflixmobile.ui.mainpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Button;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        title = binding.titleEntry;
        final Button searchButton = binding.searchButton;
        searchButton.setOnClickListener(view -> search());
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        Intent MovieListPage = new Intent(MainActivity.this, MovieListActivity.class);
        MovieListPage.putExtra("title", title.getText().toString());
        finish();
        startActivity(MovieListPage);
    }
}