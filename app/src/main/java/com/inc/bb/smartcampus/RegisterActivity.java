package com.inc.bb.smartcampus;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int PICK_FROM_GALLERY = 1;
    Bitmap profilePic = null;
    boolean passwordCorect, emailCorrect;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RegisterActivity mAuthTask = null;

    // UI references.
    private  EditText mEmailView;
    private EditText mPasswordView;
    private Button mRegisterNext;
    private View mProgressView;
    private View mLoginFormView;
    private Button mPictureAdd;

    Drawable editTextWrong,editTextNormal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.

        performLayoutLogic();
        //populateAutoComplete();

        mRegisterNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);

            }
        });

        mProgressView = findViewById(R.id.login_progress);
    }

    private void performLayoutLogic() {
        editTextWrong = getDrawable(R.drawable.edittextshapewrong);
        editTextNormal = getDrawable(R.drawable.edittextshape);
        mPasswordView = (EditText) findViewById(R.id.registerPassField);
        mEmailView = (EditText) findViewById(R.id.studentEmail);
        mPictureAdd = (Button) findViewById(R.id.pictureButton);
        mRegisterNext = (Button) findViewById(R.id.signUpNext);
        mRegisterNext.setBackground(getDrawable(R.drawable.buttonshapebefore));

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    mPasswordView.setError(null);
                    mPasswordView.setBackground(editTextNormal);
                    if(!TextUtils.isEmpty(mPasswordView.getText().toString())){
                    passwordCorect = isPasswordValid(mPasswordView.getText().toString());}
                    if(passwordCorect && emailCorrect){mRegisterNext.setBackground(getDrawable(R.drawable.buttonshape));
                    mRegisterNext.setClickable(true);}
                    else{mRegisterNext.setBackground(getDrawable(R.drawable.buttonshapebefore));
                    mRegisterNext.setClickable(false);}
                    return true;
                }
                return false;
            }
        });

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    mEmailView.setError(null);
                    mEmailView.setBackground(editTextNormal);
                    if(!TextUtils.isEmpty(mEmailView.getText().toString())){
                    emailCorrect = isEmailValid(mEmailView.getText().toString());
                    if(passwordCorect && emailCorrect){mRegisterNext.setBackground(getDrawable(R.drawable.buttonshape));}
                    else{mRegisterNext.setBackground(getDrawable(R.drawable.buttonshapebefore));}
                    return true;}
                }
                return false;
            }
        });

        mPictureAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mayAccesPhoto();
            }
        });

    }

   /* private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }
*/
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }



    private void mayAccesPhoto() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        } else {
            try {
                if (checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                } else {
                    selectPictureActivity();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void selectPictureActivity() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        pickIntent.setType("image/*");
        startActivityForResult(chooserIntent, PICK_FROM_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_FROM_GALLERY ){
            if(data.getExtras()!=null){
            final Bundle extras = data.getExtras();
            if (extras != null) {
                profilePic = extras.getParcelable("data");
                //TODO make pic appear
            }}
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        if(!email.contains("@student.tue.nl")){
            mEmailView.setError("Please use your student email");
            mEmailView.setBackground(editTextWrong);
        }
        return true;
    }

    private boolean isPasswordValid(String password) {
        int uppercase = 0;
        for(int k =0; k<password.length();k++){
            if(Character.isUpperCase(password.charAt(k))){uppercase++;}
        }
        if(uppercase<1){mPasswordView.setError("A minimal of 1 uppercase letter is needed");
            mPasswordView.setBackground(editTextWrong);
        return false;}
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /*private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }*/


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
   public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        FirebaseDatabase mDatabase;
        FirebaseAuth mAuth;


        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }


       /* public void register(final String studentNumber, final String pass) {
            mAuth.createUserWithEmailAndPassword(studentNumber,pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast toast = Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT);
                                toast.show();
                                // Hier gaat die heen als de user nog niet geregistreerd is. Is die wel geregistreerd dan is de task niet succesfull, dan krijg je een exception togestuurd. Deze pak ik hieronder aan!
                                FirebaseUser user = mAuth.getCurrentUser();
                                userId=studentNumber.replace("@random.com", "");
                                updateDisplayName(user);
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
        }*/

        @Override
        protected Boolean doInBackground(Void... params) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PICK_FROM_GALLERY){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPictureActivity();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

