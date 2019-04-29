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

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity  {

    //Required for setting up the connection with the Database and authentication client
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    //Globalizing of layout elements (initializing)
    EditText mNameField;
    EditText mPassField;
    Button mFirebaseBtn;
    Drawable buttondrawable1;
    Drawable buttondrawable2;

    //Used for logging purposes
    String TAG;

    //User credentials and user class for re-login purposes
    String longitude="0";
    String latitude= "0";
    String userId = "";
    private String name;
    private String password;
    static String student_number;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase initialization and authentication client
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        //Login activity linking of widget variables to physical layout elements
        buttondrawable1 = ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshape);
        buttondrawable2 = ContextCompat.getDrawable(getApplicationContext()
                ,R.drawable.buttonshapebefore);
        mFirebaseBtn = (Button) findViewById(R.id.firebase_btn);
        mNameField = (EditText) findViewById(R.id.name_field);
        mPassField = (EditText) findViewById(R.id.pass_field);
        mFirebaseBtn.setBackground(buttondrawable2);
        mFirebaseBtn.setText("Login");

        //Layout listeners for above widgets, these listeners get called whenver login/register
        // is initiated
        editTextsListeners();
        loginButtonListener();

    }

    // Listener for the login button. If password andusername are not empty it goes to register
    // function.
    private void loginButtonListener() {
        mFirebaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = mNameField.getText().toString().trim();
                password = mPassField.getText().toString().trim();

                if(name!=null && !name.isEmpty()&& !password.isEmpty()){
                    name=name+"@random.com";
                    register(name,password);}
                    /*else if (name.charAt(0)!='s' || name.length()!=7)
                        if(password==null || password.isEmpty()){
                            mNameField.setError("Invalid studentnumber");
                            mPassField.setError("Please fill in password");
                        }
                        else{mNameField.setError("Invalid studentnumber");}
                    }*/
                // The if else statement was used in the past for detection of studentnumber for
                // logging in. This was left out for testing convenience.
            }
        });
    }

    // Tries to register by the "studentnumber" variable and "pass variable", these are the password
    // and username inputs from the edittexts as seen in the listeners. If register is successful ->
    // Gpsactivity, if not exception by firebase is handled by either logging in or showing error to
    // user by toast.
    public void register(final String studentNumber, final String pass) {
        mAuth.createUserWithEmailAndPassword(studentNumber,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // task.isSuccessful means if the creating of a new user was successful
                        if(task.isSuccessful()){
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Registration successful", Toast.LENGTH_SHORT);
                            toast.show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            userId=studentNumber.replace("@random.com", "");
                            updateDisplayName(user);
                            writeNewUser(userId,studentNumber, pass, longitude,latitude);
                            checkUserLoggedInAndGPSActivityIntent(user);
                        }

                        // !task.isSuccessful means that creating a new user was not succesful and
                        // exception is sent back (reason why) to the app
                        else if(!task.isSuccessful())
                        {
                            String temp= "";
                            try {
                                temp =  task.getException().getMessage();
                            } catch (Exception e) {}

                            //If the exception is case of user already existing
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(),"Logging in",
                                        Toast.LENGTH_SHORT).show();
                                login(studentNumber,pass);
                            }

                            //Else it is some other error and that is shown to the user
                            else{Toast.makeText(MainActivity.this, temp,
                                    Toast.LENGTH_SHORT).show();}}
                    }
                });
    }

    // Updates another Firebase variable (displayname) to the username used to login/register
    private void updateDisplayName(FirebaseUser user) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userId)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Tries to login to firebase auth client, if successful -> GPSactivity, if not error is shown.
    public void login(String name, String pass) {
        mAuth.signInWithEmailAndPassword(name,pass).addOnCompleteListener
                (this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //Handles if logging in is succesful
                        if(task.isSuccessful()){
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Log in successful", Toast.LENGTH_SHORT);
                            toast.show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("check user id", user.getDisplayName());
                            checkUserLoggedInAndGPSActivityIntent(user);
                        }

                        //Handles exception by firebase and shows to user
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
        checkUserLoggedInAndGPSActivityIntent(currentUser);}

    //Checks whether user is logged in and if so goes to GPS activity.
    private void checkUserLoggedInAndGPSActivityIntent(FirebaseUser currentUser) {
        if(currentUser!=null){
            Intent intentgps=new Intent(this, GpsActivity.class);
            intentgps.putExtra("userId",currentUser.getEmail().
                    replace("@random.com",""));
            intentgps.putExtra(" password",password);
            if(user==null){writeNewUser(currentUser.getEmail().replace("@random.com",""),currentUser.getEmail(),password,null,null);}
            intentgps.putExtra("passwordStored",user.password);
            intentgps.putExtra("usernameStored", user.studentnumber);
            startActivity(intentgps);
        }

        if (currentUser==null){
            if(user!=null){
                login(user.studentnumber,user.password);}
        }
    }



    // Writes a User class (nothing to do with firebase), this is used for staying logged in to
    // firebase this will prevent logging in again after short connection loss.
    private void writeNewUser(String userId, String studentNumber, String passWord, String Longitude
            , String Latitude){
        GoogleFusedLocations.userid = userId;
        student_number = studentNumber;
        user = new User(studentNumber,passWord, latitude, longitude);
        mDatabase.child("users").child(userId).setValue(user);
        return;
    }
    //Listener for user input into username and password
    // editTexts, changes button to other color when those two both have input, void function.
    private void editTextsListeners() {
        mPassField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override //Sets button to the nice colour after both fields have been filled in
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}

