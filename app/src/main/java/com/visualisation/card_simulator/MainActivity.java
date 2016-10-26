package com.visualisation.card_simulator;


import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;




    HashMap<ImageView, Float> hash_pos_precX = new HashMap<ImageView, Float>();
    HashMap<ImageView, Float> hash_pos_precY = new HashMap<ImageView, Float>();
    private GoogleApiClient mGoogleApiClient;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInClicked();
            }
        });
        findViewById(R.id.sign_out_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutclicked();
            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        Log.d("COUCOU","GoogleApiCLient.builder");



        ArrayList<ImageView> tab_carte = new ArrayList<ImageView>();
        final ArrayList<LinearLayout> tab_player = new ArrayList<LinearLayout>();

        /***********************************************************************/
        /* TO ADD A CARD, YOU JUST HAVE TO PUT A NEW LINE JUST AFTER AND IT WILL
        /* AUTOMATICALY ADD THE LISTENER.
        /*  Of course you also have to add a card in activity_main.xml
        /***********************************************************************/

        tab_carte.add((ImageView) (findViewById(R.id.carte)));
        tab_carte.add((ImageView) (findViewById(R.id.carte2)));
        tab_carte.add((ImageView) (findViewById(R.id.carte3)));

        /***********************************************************************/
        tab_player.add((LinearLayout)(findViewById(R.id.player1)));
        tab_player.add((LinearLayout)(findViewById(R.id.player2)));
        tab_player.add((LinearLayout)(findViewById(R.id.player3)));



        for(final ImageView carte : tab_carte){

            carte.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:

                            carte.bringToFront();
                            hash_pos_precX.put(carte,event.getRawX());
                            hash_pos_precY.put(carte,event.getRawY());

                            break;
                        case MotionEvent.ACTION_MOVE:
                            float x_cord = event.getRawX();
                            float y_cord = event.getRawY();


                            float deplX = x_cord - hash_pos_precX.get(carte);
                            float deplY = y_cord - hash_pos_precY.get(carte);


                            hash_pos_precX.put(carte, x_cord);
                            hash_pos_precY.put(carte, y_cord);


                            carte.setX(carte.getX() + deplX);
                            carte.setY(carte.getY() + deplY);


                            for(final LinearLayout player : tab_player){
                                if(carte.getX() <= (player.getX() + player.getWidth())
                                        && carte.getX() >= (player.getX() - carte.getWidth())
                                        && carte.getY() <= (player.getY() + player.getHeight())
                                        && carte.getY() >= (player.getY() - carte.getHeight())){

                                    collision(player);

                                }
                                else{
                                    ImageView i = (ImageView)player.getChildAt(0);
                                    i.setColorFilter(Color.TRANSPARENT);
                                }
                            }



                            break;

                        case MotionEvent.ACTION_UP:

                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });

        }

    }

    public void collision(LinearLayout player){
        ImageView i = (ImageView)player.getChildAt(0);
        i.setColorFilter(Color.BLUE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("COUCOU","onSTart");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d("COUCOU","onCOnnected");

        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "connection Suspended", Toast.LENGTH_SHORT).show();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "signin_other_error")) {
                mResolvingConnectionFailure = false;
            }
        }
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        Log.d("COUCOU", "ConnectionFailed");
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }

    }

    // Call when the sign-in button is clicked
    private void signInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    // Call when the sign-out button is clicked
    private void signOutclicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        Toast.makeText(this, "disconnected from Google Play Games", Toast.LENGTH_SHORT).show();

        // show sign-in button, hide the sign-out button
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
    }

}
