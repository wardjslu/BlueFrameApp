package rydberg.blueframe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import rydberg.blueframe.connect_activities.G_MainActivity;
import rydberg.blueframe.connect_activities.T_MainActivity;

public class WelcomeActivity extends AppCompatActivity {

    public void Graph(View view) {

        Intent intent = new Intent(this, G_MainActivity.class);

        startActivity(intent);
    }

    public void Terminal(View view) {

        Intent intent = new Intent(this, T_MainActivity.class);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

}
