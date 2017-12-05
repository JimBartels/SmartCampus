package com.inc.bb.smartcampus;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {


    //Required for setting up the connection with the Database
    private DatabaseReference mDatabase;

   //Required for sending data to Server
    EditText mNameField;
    EditText mPassField;
    Button mFirebaseBtn;
    private FirebaseAuth mAuth;
    String TAG;
    boolean hasAccount;
    String longitude="0";
    String latitude= "0";
    String userId = "";
    Drawable buttondrawable1;
    Drawable buttondrawable2;
    Boolean isTextChanged1;
    Boolean isTextChanged2;
    private String name;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        buttondrawable1 = ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshape);
        buttondrawable2 = ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshapebefore);

        mFirebaseBtn = (Button) findViewById(R.id.firebase_btn);
        mNameField = (EditText) findViewById(R.id.name_field);
        mPassField = (EditText) findViewById(R.id.pass_field);
        mFirebaseBtn.setBackground(buttondrawable2);
        final FirebaseUser user =mAuth.getCurrentUser();
        mFirebaseBtn.setText("Login");
        buttonchanger();
        mFirebaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = mNameField.getText().toString().trim();
                password = mPassField.getText().toString().trim();

                if(true){
                    if(name!=null && !name.isEmpty()&& password!=null && !password.isEmpty() && name.charAt(0)=='s'&& name.length()== 7){
                        //Fire base doet raar met username inloggen etc. Er is eigenlijk alleen maar een optie voor email en password. Daarom heeft iedereen nu het formaat (s nummer)@random.com haha
                        name=name+"@random.com";
                        register(name,password);
                }
                else if (name.charAt(0)!='s' || name.length()!=7){ //Hier pak ik aan dat het moet beginnen met een s en 7 lang moet zijn
                        if(password==null || password.isEmpty()){
                            mNameField.setError("Invalid studentnumber");
                            mPassField.setError("Please fill in password");
                        }
                        else{mNameField.setError("Invalid studentnumber");}
                    }
                }

                /*HashMap<String,String> dataMap = new HashMap<String,String>();
                dataMap.put("Studentnumber",name);
                dataMap.put("Password",email);*/


            }
        });
    }


    public void register(final String studentNumber, final String pass) {
        mAuth.createUserWithEmailAndPassword(studentNumber,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast toast = Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT);
                            toast.show();
                            // Hier gaat die heen als de user nog niet geregistreerd is. Is die wel geregistreerd dan is de task niet succesfull, dan krijg je een exception togestuurd. Deze pak ik hieronder aan!
                            FirebaseUser user = mAuth.getCurrentUser();
                            userId=studentNumber;
                            writeNewUser(userId,studentNumber, pass, longitude,latitude);
                            updateUI(user);
                        }
                        else if(!task.isSuccessful())//Task is niet succesfull exception toegestuurd
                        {String temp= "";
                            try {
                            temp =  task.getException().getMessage();
                        } catch (Exception e) {}
                        //Hieronder als d e execption is dat de user al bestaat, dan probeer die in te loggen met de opgegeven password en naam.
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(),"Logging in",Toast.LENGTH_SHORT).show();
                                login(studentNumber,pass);
                            }else{Toast.makeText(MainActivity.this, temp,
                                    Toast.LENGTH_SHORT).show();}} //ALs de exception dus iets anders is dan krijg je hem gwn op je scherm te zienw at je fout doet. Bvb een te kort password

                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void login(String name, String pass) {
        mAuth.signInWithEmailAndPassword(name,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast toast = Toast.makeText(getApplicationContext(), "Log in successful", Toast.LENGTH_SHORT);
                    toast.show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                }
                else if(!task.isSuccessful())
                {String temp= "";
                    try {
                        temp =  task.getException().getMessage();
                    } catch (Exception e) {}
                    Toast.makeText(MainActivity.this, temp,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, " On start");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);}

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser!=null ){
            //De app kijkt altijd eerst naar onstart na dat de onclicklistener is neergezet. Als dus de Currentuser niet null is dan gaat die gelijk door naar de andere activity omdat je al ingelogd bent.Hier kom je vanuit Onstart, vanuit register en login methods.
            Intent intentgps=new Intent(this, GpsActivity.class);
            intentgps.putExtra("userId",name);
            startActivity(intentgps);
        }
        if (currentUser==null){
            //Dit hoeft niks te zijn


        }}
    private void writeNewUser(String userId, String studentNumber, String passWord, String Longitude, String Latitude){
        userId=userId.replace("@random.com","");
        studentNumber = studentNumber.replace("@random.com","");
        User user = new User(studentNumber,passWord, latitude, longitude);
        mDatabase.child("users").child(userId).setValue(user);
        return;

    }

    private void buttonchanger() {
        mPassField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(mPassField.getText().toString().length()>0){
                    if(mNameField.getText().toString().length()>0){
                        mFirebaseBtn.setBackground(buttondrawable1);
                        mFirebaseBtn.setEnabled(true);
                    }}
                if(mPassField.getText().toString().isEmpty()){
                    mFirebaseBtn.setBackground(buttondrawable2);
                    mFirebaseBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(mNameField.getText().toString().length()>0){
                    if(mPassField.getText().toString().length()>0){
                        mFirebaseBtn.setBackground(buttondrawable1);
                        mFirebaseBtn.setEnabled(true);
                    }}
                if(mNameField.getText().toString().isEmpty()){
                    mFirebaseBtn.setBackground(buttondrawable2);
                    mFirebaseBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean isStudentnumer() {
        if(mNameField.getText().toString().length()==7){
           String check = mNameField.getText().toString().trim();
            if(check.charAt(0)=='s') {
                return true;
            }else return false;
        }
        else return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}

