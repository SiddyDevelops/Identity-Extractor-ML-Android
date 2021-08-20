package com.siddydevelops.identityextractor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openRelevantActivity(View view)
    {
        int id = view.getId();
        switch(id)
        {
            case R.id.aadhaarCardView:
                Intent aIntent = new Intent(getApplicationContext(),AadhaarCardActivity.class);
                startActivity(aIntent);
                break;

            case R.id.panCardView:
                Intent pIntent = new Intent(getApplicationContext(),PanCardActivity.class);
                startActivity(pIntent);
                break;

            case R.id.idCardView:
                Intent iIntent = new Intent(getApplicationContext(),GenericIDCardActivity.class);
                startActivity(iIntent);
                break;
        }
    }

}