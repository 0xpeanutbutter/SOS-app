package com.example.sos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddContacts extends AppCompatActivity {


    EditText phone1,phone2,phone3;
    Button save;
    public static SharedPreferences sharedPreferences;
    public static final String myPreferences = "PhoneNumber";
    public static final String phoneNumber1 = "First";
    public static final String phoneNumber2 = "Second";
    public static final String phoneNumber3= "Third";
    public String input1;
    public String input2;
    public String input3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        phone1 = findViewById(R.id.contact1);
        phone2 = findViewById(R.id.contact2);
        phone3 = findViewById(R.id.contact3);
        save = findViewById(R.id.svBtn);

        sharedPreferences = getSharedPreferences(myPreferences,Context.MODE_PRIVATE);
        save.setOnClickListener(v -> {
            input1 = phone1.getText().toString();
            input2 = phone2.getText().toString();
            input3 = phone3.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(phoneNumber1,input1);
                editor.putString(phoneNumber2,input2);
                editor.putString(phoneNumber3,input3);
                editor.apply();
            Toast.makeText(AddContacts.this,"Contacts saved",Toast.LENGTH_SHORT).show();
        });
    }
}