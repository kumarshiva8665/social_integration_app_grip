package com.skrp.social_media_integration_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.internal.SignInButtonImpl;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private static int RC_SIGN_IN=100;
    Button Btn2;
    SignInButtonImpl signInButton;
    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    TextView fb_nameTxt,fb_emailTxt;
    LoginButton fb_loginBtn;
    ImageView pic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Btn2 = findViewById(R.id.btn2);
        signInButton = findViewById(R.id.google_login);
        fb_emailTxt = findViewById(R.id.emailinfoTxt);
        fb_nameTxt = findViewById(R.id.nameinfoTxt);
        fb_loginBtn = findViewById(R.id.fb_login_button);
        pic = findViewById(R.id.logo);

        /* Configure sign-in to request the user's ID, email address, and basic
           profile. ID and basic profile are included in DEFAULT_SIGN_IN.*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        /* Check for existing Google Sign In account, if the user is already signed in
           the GoogleSignInAccount will be non-null.*/
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        callbackManager = CallbackManager.Factory.create();

        fb_loginBtn.setPermissions(Arrays.asList("email"));
        fb_loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken == null){
                fb_nameTxt.setText("");
                fb_emailTxt.setText("");
                pic.setImageResource(0);
                UpdateUi(false);
                Toast.makeText(MainActivity.this, "Successfully Logged Out!!", Toast.LENGTH_SHORT).show();
            }else{
                loaduserProfile(currentAccessToken);
            }
        }
    };

    private void loaduserProfile(AccessToken newAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if(object != null){
                    try {
                        String name = object.getString("name");
                        String email = object.getString("email");
                        String userId = object.getString("id");
                        fb_emailTxt.setText("Email Id: "+email);
                        fb_nameTxt.setText("Name: "+name);
                        Picasso.get().load("https://graph.facebook.com/"+ userId + "/picture?type=large").placeholder(R.drawable.default_user).into(pic);
                        Log.d("userId",userId);
                        Log.d("demo",object.toString());
                        Log.d("profilepic",String.valueOf("https://graph.facebook.com/"+ userId + "/picture?type=large"));
                        UpdateUi(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","email,id,name");
        request.setParameters(parameters);
        request.executeAsync();

    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

            }
            startActivity(new Intent(MainActivity.this,google_login_Activity.class));

            // Signed in successfully.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("message",e.toString());
        }
    }

    private void UpdateUi(boolean isSignedIn){
        if(isSignedIn){
            signInButton.setVisibility(View.GONE);
            Btn2.setVisibility(View.GONE);
        }else{
            signInButton.setVisibility(View.VISIBLE);
            Btn2.setVisibility(View.VISIBLE);
        }
    }
}