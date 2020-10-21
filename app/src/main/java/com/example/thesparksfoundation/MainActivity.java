package com.example.thesparksfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;

    private TextView textView;
    private TextView textView2;
    private ImageView imageView;
    private LoginButton loginButton;
    private static final String TAG="FacebookAuthentication";

    private FrameLayout frame3;

    private SignInButton signInButton;
    private GoogleSignInClient googleSignInClient;
    private String TAG2="MainActivity";
    private Button btnSignOut;
    private int RC_SIGN_IN=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth= FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);

        textView=findViewById(R.id.name);
        imageView=findViewById(R.id.profile_image);
//        textView2=findViewById(R.id.email);
       frame3=findViewById(R.id.frame3);
        loginButton=findViewById(R.id.btnlogin);
        loginButton.setReadPermissions("email","public_profile");
        callbackManager= CallbackManager.Factory.create();

        textView.setText("");
//        textView2.setText("");
        imageView.setImageResource(R.drawable.profile);

        signInButton=findViewById(R.id.Gsign_in);
        btnSignOut=findViewById(R.id.Gsign_out);

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleSignInClient= GoogleSignIn.getClient(this,gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignInClient.signOut();
                Toast.makeText(MainActivity.this,"You are Logged Out",Toast.LENGTH_LONG).show();
                textView.setText("");
                imageView.setImageResource(R.drawable.profile);
                btnSignOut.setVisibility(View.INVISIBLE);
                frame3.setVisibility(View.INVISIBLE);
            }
        });

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"onSuccess"+loginResult);
                frame3.setVisibility(View.VISIBLE);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"onError"+error);
            }
        });
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user!=null)
                {
                    updateUI(user);
                }
                else
                {
                    updateUI(null);
                }
            }
        };
        accessTokenTracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null)
                {

                    firebaseAuth.signOut();
                    frame3.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    private void signIn()
    {
        Intent signInIntent=googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override

    protected void  onActivityResult(int requestCode,int resultCode,@Nullable Intent data)
    {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode==RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try{
            GoogleSignInAccount acc=completedTask.getResult(ApiException.class);
//            Toast.makeText(MainActivity.this,"Successfully signed In with Google",Toast.LENGTH_LONG).show();
            FirebaseGoogleAuth(acc);
        }
        catch(ApiException e){
            Toast.makeText(MainActivity.this,"Signed In Failed with Google",Toast.LENGTH_LONG).show();
            FirebaseGoogleAuth(null);
    }
    }
    private void FirebaseGoogleAuth(GoogleSignInAccount acct){
     AuthCredential authCredential= GoogleAuthProvider.getCredential(acct.getIdToken(),null);
     firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
         @Override
         public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful())
            {
     Toast.makeText(MainActivity.this,"Google Sign-in Successful",Toast.LENGTH_LONG).show();
                FirebaseUser user=firebaseAuth.getCurrentUser();
                updateGoogleUI(user);
            }
            else
            {
                Toast.makeText(MainActivity.this,"Google Sign-in Failed",Toast.LENGTH_LONG).show();
                updateGoogleUI(null);
            }
         }
     });
    }
    private void updateGoogleUI(FirebaseUser user)
    {
        btnSignOut.setVisibility(View.VISIBLE);
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account!=null)
        {
            String personName=account.getDisplayName();
            String personGivenName=account.getGivenName();
            String personFamilyName=account.getFamilyName();
            String personEmail=account.getEmail();
            String personId=account.getId();
            Uri personphoto=account.getPhotoUrl();
//            Toast.makeText(MainActivity.this,personName+personEmail,Toast.LENGTH_LONG).show();

            frame3.setVisibility(View.VISIBLE);
            textView.setText(personName);
//            textView2.setText(personEmail);


        }
    };

    private void handleFacebookAccessToken(AccessToken token)
    {
        Log.d(TAG,"handleFacebookAccessToken"+token);

        AuthCredential authCredential= FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Log.d(TAG,"Sign in with credentials is Successful");
                    FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
                    updateUI(firebaseUser);}
                else
                {
                    Log.d(TAG,"Sign in with credentials is Unsuccessful"+task.getException());
                    Toast.makeText(MainActivity.this,"Authentication Failed",Toast.LENGTH_LONG).show();
                    updateUI(null);
                }
            }
        });
    }
    private void updateUI(FirebaseUser user)
    {
        if(user!=null)
        {
            textView.setText(user.getDisplayName());
            if(user.getPhotoUrl()!=null)
            {
                String photourl=user.getPhotoUrl().toString();
                photourl=photourl+"?type=large";
                Picasso.get().load(photourl).into(imageView);
            }
            else
            {
                textView.setText("");
                imageView.setImageResource(R.drawable.profile);
            }
        }
        else
        {
            textView.setText("");
            imageView.setImageResource(R.drawable.profile);
        }
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }


}

