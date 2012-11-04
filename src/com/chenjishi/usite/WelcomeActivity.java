package com.chenjishi.usite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.chenjishi.usite.activity.HomeActivity;

public class WelcomeActivity extends Activity implements View.OnClickListener {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.to_home).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
