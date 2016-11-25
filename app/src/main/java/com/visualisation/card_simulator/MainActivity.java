package com.visualisation.card_simulator;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoomUpdateListener, RealTimeMessageReceivedListener,RoomStatusUpdateListener, SensorEventListener {

    private static float dropOnPLayer = 5555f;
    private static float dropOnHand = 6666f;
    private static float dropOnPioche = 7777f;
    private static float dropOnTapis = 8888f;
    private static float mix = 4444f;
    private static float returncard = 3333f;

    private static int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_WAITING_ROOM = 10002;

    SensorManager sensorManager;
    Sensor accelerometer;

    boolean mPlaying = false;
    final static int MIN_PLAYERS = 2;
    String mRoomId;
    Room mRoom;
    ArrayList<Participant> mParticipants;
    String mMyId;




    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;




    HashMap<ImageView, Float> hash_pos_precX = new HashMap<ImageView, Float>();
    HashMap<ImageView, Float> hash_pos_precY = new HashMap<ImageView, Float>();
    ArrayList<Carte> tab_carte;
    Button retourner;

    private GoogleApiClient mGoogleApiClient;
    ArrayList<LinearLayout> tab_player;


    float Height, Width;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Height = displayMetrics.heightPixels;
        Width = displayMetrics.widthPixels;




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
        findViewById(R.id.quick_match).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuickGame();
            }
        });

        findViewById(R.id.leave_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab_carte.clear();
                startGame();
                for(Carte c : tab_carte){
                    dropOnPioche(c, false);
                }

            }
        });



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        Log.d("COUCOU", "GoogleApiCLient.builder");


        tab_carte = new ArrayList<Carte>();
        tab_player = new ArrayList<LinearLayout>();

        /***********************************************************************/
        /* TO ADD A CARD, YOU JUST HAVE TO PUT A NEW LINE JUST AFTER AND IT WILL
        /* AUTOMATICALY ADD THE LISTENER.
        /*  Of course you also have to add a card in activity_main.xml
        /***********************************************************************/

        startGame();

        tab_player.add((LinearLayout) (findViewById(R.id.player2)));
        retourner = (Button) findViewById(R.id.retourner);

        for(Carte c : tab_carte){
            c.image = c.imageView.getDrawable();
            dropOnPioche(c, false);
        }

        /***********************************************************************/

        /*for(LinearLayout l : tab_player){
            l.setVisibility(View.GONE);
        }*/
        for (final Carte carte : tab_carte) {
            setCardListener(carte);
        }

        for(final LinearLayout player : tab_player){
            player.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(Carte c : tab_carte){
                        if(c.isSelected){
                            dropOnPLayer(player, c, false);
                            c.isSelected = false;
                            c.imageView.setColorFilter(Color.TRANSPARENT);
                            retourner.setVisibility(View.GONE);

                        }
                    }
                }
            });
        }

        RelativeLayout plateau = (RelativeLayout) findViewById(R.id.plateau_carte);
        plateau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Carte c : tab_carte){
                    if(c.isSelected){
                        dropOnHand(c, false);
                        c.isSelected = false;
                        c.imageView.setColorFilter(Color.TRANSPARENT);
                        retourner.setVisibility(View.GONE);

                    }

                }
            }
        });

        RelativeLayout pioche = (RelativeLayout) findViewById(R.id.pioche);
        pioche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Carte c : tab_carte){
                    if(c.isSelected){
                        dropOnPioche(c, false);
                        c.isSelected = false;
                        c.imageView.setColorFilter(Color.TRANSPARENT);
                        retourner.setVisibility(View.GONE);

                    }
                }
            }
        });

        RelativeLayout tapis = (RelativeLayout) findViewById(R.id.tapis);
        tapis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Carte c : tab_carte){
                    if(c.isSelected){
                        dropOnTapis(c, false);
                        c.isSelected = false;
                        c.imageView.setColorFilter(Color.TRANSPARENT);
                        retourner.setVisibility(View.GONE);

                    }
                }
            }
        });


        retourner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Carte c : tab_carte){
                    if(c.isSelected && c.state != Card_states.PIOCHE)
                    returncard(c, false);

                }
            }
        });
        retourner.setVisibility(View.GONE);

        Button melanger = (Button) findViewById(R.id.melanger);
        melanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixCard(false);
            }
        });
    }

    public void startGame(){
        tab_carte.add(new Carte(0,(ImageView) (findViewById(R.id.c1))));
        tab_carte.add(new Carte(1,(ImageView) (findViewById(R.id.d1))));
        tab_carte.add(new Carte(2,(ImageView) (findViewById(R.id.h1))));
        tab_carte.add(new Carte(3,(ImageView) (findViewById(R.id.s1))));
        tab_carte.add(new Carte(4,(ImageView) (findViewById(R.id.c2))));
        tab_carte.add(new Carte(5,(ImageView) (findViewById(R.id.d2))));
        tab_carte.add(new Carte(6,(ImageView) (findViewById(R.id.h2))));
        tab_carte.add(new Carte(7,(ImageView) (findViewById(R.id.s2))));
        tab_carte.add(new Carte(8,(ImageView) (findViewById(R.id.c3))));
        tab_carte.add(new Carte(9,(ImageView) (findViewById(R.id.d3))));
        tab_carte.add(new Carte(10,(ImageView) (findViewById(R.id.h3))));
        tab_carte.add(new Carte(11,(ImageView) (findViewById(R.id.s3))));
        tab_carte.add(new Carte(12,(ImageView) (findViewById(R.id.c4))));
        tab_carte.add(new Carte(13,(ImageView) (findViewById(R.id.d4))));
        tab_carte.add(new Carte(14,(ImageView) (findViewById(R.id.h4))));
        tab_carte.add(new Carte(15,(ImageView) (findViewById(R.id.s4))));
        tab_carte.add(new Carte(16,(ImageView) (findViewById(R.id.c5))));
        tab_carte.add(new Carte(17,(ImageView) (findViewById(R.id.d5))));
        tab_carte.add(new Carte(18,(ImageView) (findViewById(R.id.h5))));
        tab_carte.add(new Carte(19,(ImageView) (findViewById(R.id.s5))));
        tab_carte.add(new Carte(20,(ImageView) (findViewById(R.id.c6))));
        tab_carte.add(new Carte(21,(ImageView) (findViewById(R.id.d6))));
        tab_carte.add(new Carte(22,(ImageView) (findViewById(R.id.h6))));
        tab_carte.add(new Carte(23,(ImageView) (findViewById(R.id.s6))));
        tab_carte.add(new Carte(24,(ImageView) (findViewById(R.id.c7))));
        tab_carte.add(new Carte(25,(ImageView) (findViewById(R.id.d7))));
        tab_carte.add(new Carte(26,(ImageView) (findViewById(R.id.h7))));
        tab_carte.add(new Carte(27,(ImageView) (findViewById(R.id.s7))));
        tab_carte.add(new Carte(28,(ImageView) (findViewById(R.id.c8))));
        tab_carte.add(new Carte(29,(ImageView) (findViewById(R.id.d8))));
        tab_carte.add(new Carte(30,(ImageView) (findViewById(R.id.h8))));
        tab_carte.add(new Carte(31,(ImageView) (findViewById(R.id.s8))));
        tab_carte.add(new Carte(32,(ImageView) (findViewById(R.id.c9))));
        tab_carte.add(new Carte(33,(ImageView) (findViewById(R.id.d9))));
        tab_carte.add(new Carte(34,(ImageView) (findViewById(R.id.h9))));
        tab_carte.add(new Carte(35,(ImageView) (findViewById(R.id.s9))));
        tab_carte.add(new Carte(36,(ImageView) (findViewById(R.id.c10))));
        tab_carte.add(new Carte(37,(ImageView) (findViewById(R.id.d10))));
        tab_carte.add(new Carte(38,(ImageView) (findViewById(R.id.h10))));
        tab_carte.add(new Carte(39,(ImageView) (findViewById(R.id.s10))));
        tab_carte.add(new Carte(40,(ImageView) (findViewById(R.id.c11))));
        tab_carte.add(new Carte(41,(ImageView) (findViewById(R.id.d11))));
        tab_carte.add(new Carte(42,(ImageView) (findViewById(R.id.h11))));
        tab_carte.add(new Carte(43,(ImageView) (findViewById(R.id.s11))));
        tab_carte.add(new Carte(44,(ImageView) (findViewById(R.id.c12))));
        tab_carte.add(new Carte(45,(ImageView) (findViewById(R.id.d12))));
        tab_carte.add(new Carte(46,(ImageView) (findViewById(R.id.h12))));
        tab_carte.add(new Carte(47,(ImageView) (findViewById(R.id.s12))));
        tab_carte.add(new Carte(48,(ImageView) (findViewById(R.id.c13))));
        tab_carte.add(new Carte(49,(ImageView) (findViewById(R.id.d13))));
        tab_carte.add(new Carte(50,(ImageView) (findViewById(R.id.h13))));
        tab_carte.add(new Carte(51,(ImageView) (findViewById(R.id.s13))));
    }

    public void setCardListener(final Carte carte){
        carte.imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ImageView image_carte = carte.imageView;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        image_carte.bringToFront();
                        hash_pos_precX.put(image_carte, event.getRawX());
                        hash_pos_precY.put(image_carte, event.getRawY());


                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x_cord = event.getRawX();
                        float y_cord = event.getRawY();


                        float deplX = x_cord - hash_pos_precX.get(image_carte);
                        float deplY = y_cord - hash_pos_precY.get(image_carte);


                        hash_pos_precX.put(image_carte, x_cord);
                        hash_pos_precY.put(image_carte, y_cord);


                        image_carte.setX(image_carte.getX() + deplX);
                        image_carte.setY(image_carte.getY() + deplY);

                        sendCardPos(image_carte.getX(), image_carte.getY(), carte.ID);
                        for (final LinearLayout player : tab_player) {
                            if (image_carte.getX() <= (player.getX() + player.getWidth())
                                    && image_carte.getX() >= (player.getX() - image_carte.getWidth())
                                    && image_carte.getY() <= (player.getY() + player.getHeight())
                                    && image_carte.getY() >= (player.getY() - image_carte.getHeight())) {

                                collisionWithPlayer(player);

                            } else {
                                ImageView i = (ImageView) player.getChildAt(0);
                                i.setColorFilter(Color.TRANSPARENT);
                            }
                        }


                        break;

                    case MotionEvent.ACTION_UP:
                        RelativeLayout plateau = (RelativeLayout) findViewById(R.id.plateau_carte);
                        RelativeLayout pioche = (RelativeLayout) findViewById(R.id.pioche);
                        if (carte.isSelected) {
                            for (Carte c : tab_carte) {
                                c.isSelected = false;
                                c.imageView.setColorFilter(Color.TRANSPARENT);
                            }
                        }else{
                            for(Carte c : tab_carte){
                                c.isSelected = false;
                                c.imageView.setColorFilter(Color.TRANSPARENT);
                            }
                            carte.isSelected = true;
                            carte.imageView.setColorFilter(Color.argb(75,0,0,255));

                        }


                        for (final LinearLayout player : tab_player) {
                            if (image_carte.getX() <= (player.getX() + player.getWidth())
                                    && image_carte.getX() >= (player.getX() - image_carte.getWidth())
                                    && image_carte.getY() <= (player.getY() + player.getHeight())
                                    && image_carte.getY() >= (player.getY() - image_carte.getHeight())) {
                                if(carte.state != Card_states.AUTRES){
                                    carte.imageView.setColorFilter(Color.TRANSPARENT);
                                    carte.isSelected = false;
                                }
                                dropOnPLayer(player, carte, false);

                            }else if(image_carte.getX() <= (plateau.getX() + plateau.getWidth())
                                    && image_carte.getX() >= (plateau.getX() - image_carte.getWidth())
                                    && image_carte.getY() <= (plateau.getY() + plateau.getHeight())
                                    && image_carte.getY() >= (plateau.getY() - image_carte.getHeight())){
                                if(carte.state != Card_states.MOI){
                                    carte.imageView.setColorFilter(Color.TRANSPARENT);
                                    carte.isSelected = false;

                                }
                                dropOnHand(carte, false);
                            }else if(image_carte.getX() <= (pioche.getX() + pioche.getWidth())
                                    && image_carte.getX() >= (pioche.getX() - image_carte.getWidth())
                                    && image_carte.getY() <= (pioche.getY() + pioche.getHeight())
                                    && image_carte.getY() >= (pioche.getY() - image_carte.getHeight())){
                                if(carte.state != Card_states.PIOCHE){
                                    carte.imageView.setColorFilter(Color.TRANSPARENT);
                                    carte.isSelected = false;
                                }
                                dropOnPioche(carte, false);

                            }else{
                                if(carte.state != Card_states.TAPIS){
                                    carte.imageView.setColorFilter(Color.TRANSPARENT);
                                    carte.isSelected = false;
                                }
                                dropOnTapis(carte, false);
                            }


                        }
                        break;
                    default:
                        break;

                }

                if(carte.isSelected && carte.state != Card_states.PIOCHE){
                    retourner.setVisibility(View.VISIBLE);
                }
                else{
                    retourner.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    public void collisionWithPlayer(LinearLayout player){
        ImageView i = (ImageView)player.getChildAt(0);
        i.setColorFilter(Color.BLUE);
    }

    public void dropOnPLayer(LinearLayout player, Carte carte, Boolean isReceived){
        ImageView i = (ImageView)player.getChildAt(0);

        i.setColorFilter(Color.TRANSPARENT);
        Card_states oldState = carte.state;
        carte.state = Card_states.AUTRES;
        carte.imageView.setScaleX(0.5f);
        carte.imageView.setScaleY(0.5f);
        carte.setBack();
        carte.imageView.setOnTouchListener(null);

        organizedCard();

        if(oldState == Card_states.PIOCHE)
            setPiocheCompteur();
        if(!isReceived){
            try{
                byte[] message;
                message = ByteBuffer.allocate(12).putFloat(dropOnHand).putFloat(0f).putInt(carte.ID).array();
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);

            }catch(Exception e){

            }
        }

    }

    public void dropOnHand(Carte carte, Boolean isReceived){

        if(carte.state == Card_states.AUTRES){
            setCardListener(carte);
        }
        Card_states oldState = carte.state;


        carte.state = Card_states.MOI;
        carte.imageView.setScaleX(0.75f);
        carte.imageView.setScaleY(0.75f);
        //carte.setFront();

        organizedCard();


        if(oldState == Card_states.PIOCHE)
            setPiocheCompteur();

        if(!isReceived){
            try{
                byte[] message;
                message = ByteBuffer.allocate(12).putFloat(dropOnPLayer).putFloat(0f).putInt(carte.ID).array();
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);

            }catch(Exception e){

            }
        }

    }



    public void dropOnTapis(Carte carte, Boolean isReceived){
        Card_states oldState = carte.state;

        carte.imageView.setScaleX(1);
        carte.imageView.setScaleY(1);
        carte.state = Card_states.TAPIS;
        if(oldState != Card_states.TAPIS)
            carte.setBack();
        RelativeLayout tapis = (RelativeLayout) findViewById(R.id.tapis);
        ImageView image_carte = carte.imageView;
        if(!(image_carte.getX() <= (tapis.getX() + tapis.getWidth())
                && image_carte.getX() >= (tapis.getX() - image_carte.getWidth())
                && image_carte.getY() <= (tapis.getY() + tapis.getHeight())
                && image_carte.getY() >= (tapis.getY() - image_carte.getHeight()))){
            carte.imageView.setX(tapis.getX() + tapis.getWidth()/2 - carte.imageView.getWidth()/2);
            carte.imageView.setY(tapis.getY() + tapis.getHeight()/2 - carte.imageView.getHeight()/2);
        }

        organizedCard();

        if(oldState == Card_states.PIOCHE)
            setPiocheCompteur();
        if(oldState == Card_states.AUTRES)
            setCardListener(carte);

        if(!isReceived){
            try{
                byte[] message;
                message = ByteBuffer.allocate(12).putFloat(dropOnTapis).putFloat(0f).putInt(carte.ID).array();
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);

            }catch(Exception e){

            }
        }
        else{
            carte.imageView.bringToFront();
        }
    }

    public void dropOnPioche(Carte carte, Boolean isReceived){
        RelativeLayout pioche = (RelativeLayout) findViewById(R.id.pioche);
        carte.imageView.setX(pioche.getX());
        carte.imageView.setY(pioche.getY());
        carte.imageView.setScaleX(0.75f);
        carte.imageView.setScaleY(0.75f);
        if(carte.state == Card_states.AUTRES)
            setCardListener(carte);
        carte.state = Card_states.PIOCHE;
        carte.setBack();
        for(Carte c : tab_carte){
            if(!(c == carte)){
                c.imageView.bringToFront();
            }
        }
        setPiocheCompteur();
        organizedCard();

        if(!isReceived){
            try{
                byte[] message;
                message = ByteBuffer.allocate(12).putFloat(dropOnPioche).putFloat(0f).putInt(carte.ID).array();
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);

            }catch(Exception e){

            }
        }
    }

    public void setPiocheCompteur(){
        int compteur = 0;
        for(Carte c : tab_carte){
            if(c.state == Card_states.PIOCHE)
                compteur++;
        }
        TextView cp = (TextView) findViewById(R.id.compteurPioche);
        if(compteur == 0)
            cp.setText("");
        else
            cp.setText("" + compteur);
        RelativeLayout r = (RelativeLayout) findViewById(R.id.relat_compteur);
        r.bringToFront();
        cp.bringToFront();
    }

    public void organizedCard(){
        ImageView i = (ImageView) tab_player.get(0).getChildAt(0);
        RelativeLayout plateau = (RelativeLayout) findViewById(R.id.plateau_carte);
        int c_main = -1;
        int c_player = -1;
        int surplus = 0;
        int surplus_player = 1;
        for(Carte c : tab_carte){
            if(c.state == Card_states.MOI){
                c_main ++;
                if(c_main*c.imageView.getWidth()/2 + c.imageView.getWidth() >= Width){
                    surplus ++;
                    c_main = 0;
                }

                c.imageView.setX(c_main*c.imageView.getWidth()/2);
                //c.imageView.setY((plateau.getY()-plateau.getHeight()/2) + (c.imageView.getHeight()/(2))*surplus);
                c.imageView.setY((plateau.getY()) + (c.imageView.getHeight()/(2))*surplus);

                c.imageView.bringToFront();


            }
            else if(c.state == Card_states.AUTRES){
                c_player++;
                if(i.getX() + c.imageView.getWidth() + c_player*c.imageView.getWidth()/2 + c.imageView.getWidth() >= Width){
                    surplus_player ++;
                    c_player = 0;
                }

                c.imageView.setX(i.getX() + c.imageView.getWidth() + c_player*c.imageView.getWidth()/2);
                c.imageView.setY(i.getY() + (c.imageView.getHeight()/2)*surplus_player);
                c.imageView.bringToFront();
            }
        }
    }



    public void sendCardPos(float x,float y, int carteID){
        try{
            byte[] message;
            message = ByteBuffer.allocate(12).putFloat(dpToPercentWidth(x)).putFloat(dpToPercentHeight(y)).putInt(carteID).array();
            Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);
        }catch(Exception e){

        }
    }

    public void returncard(Carte carte, boolean isReceived){
        if(carte.state != Card_states.AUTRES){
            if(carte.isFront())
                carte.setBack();
            else
                carte.setFront();
        }
        carte.isSelected = false;
        carte.imageView.setColorFilter(Color.TRANSPARENT);
        if(!isReceived && carte.state != Card_states.MOI){
            try{
                byte[] message;
                message = ByteBuffer.allocate(12).putFloat(returncard).putFloat(0f).putInt(carte.ID).array();
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);
            }catch(Exception e){

            }
        }

    }

    public void mixCard(boolean isReceived){
        List<Carte> cartePioche = new ArrayList<>();
        for(Carte c : tab_carte){
            if(c.state == Card_states.PIOCHE)
                cartePioche.add(c);
        }
        Collections.shuffle(cartePioche);
        for(Carte c : cartePioche){
            c.imageView.bringToFront();
        }
        setPiocheCompteur();
        if(!isReceived){

            try{
                byte[] message;
                message = ByteBuffer.allocate(12).putFloat(mix).putFloat(0f).putInt(0).array();
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);
            }catch(Exception e){

            }
        }


        Toast.makeText(this, "La pioche a été mélangé !" , Toast.LENGTH_LONG).show();
    }

    public float dpToPercentWidth(float x){
        return x/Width;
    }

    public float dpToPercentHeight(float y){
        return (y/Height + 0.3f);
    }

    public float percentToDpWidth(float x){
        return x * Width;
    }

    public float percentToDpHeight(float y){
        return y * Height;
    }
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

        float x, y;
        int carteID;
        byte[] b = realTimeMessage.getMessageData();
        ByteBuffer bf = ByteBuffer.wrap(b);
        x = bf.getFloat();
        y = bf.getFloat();
        carteID = bf.getInt();

        Carte carte = tab_carte.get(1);
        for(Carte c : tab_carte){
            if(c.ID == carteID){
                carte = c;
                break;
            }
        }

        if(x == dropOnPLayer){
            dropOnPLayer(tab_player.get(0), carte, true);
        }else if(x == dropOnHand) {
            dropOnHand(carte, true);
        }else if(x == dropOnPioche){
            dropOnPioche(carte, true);
        }else if(x == dropOnTapis){
            dropOnTapis(carte, true);
        } else if(x == mix){
            mixCard(true);
        }else if(x == returncard){
            returncard(carte, true);
        }else {
            carte.imageView.setX(percentToDpWidth(x));
            carte.imageView.setY(Height - percentToDpHeight(y));
        }


    }

    /**
     *  GOOGLE PLAY GAMES API
     */



    private void startMultiplayerGame(){
        Toast.makeText(this, "Le jeu peut commencer !", Toast.LENGTH_SHORT).show();
        findViewById(R.id.quick_match).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        findViewById(R.id.leave_game).setVisibility(View.VISIBLE);
        for(LinearLayout l : tab_player){
            l.setVisibility(View.VISIBLE);
        }


    }

    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen


    }





















    @Override
    protected void onStart() {
        super.onStart();
        Log.d("COUCOU", "onSTart");
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        //mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d("COUCOU","onCOnnected");

        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        findViewById((R.id.quick_match)).setVisibility(View.VISIBLE);

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
        findViewById(R.id.quick_match).setVisibility(View.GONE);

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
        if (requestCode == RC_SELECT_PLAYERS) {
            if (resultCode != RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            Bundle extras = intent.getExtras();
            final ArrayList<String> invitees =
                    intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get auto-match criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers =
                    intent.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers =
                    intent.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (requestCode == RC_WAITING_ROOM) {
            if (resultCode == Activity.RESULT_OK) {
                startMultiplayerGame();
            }
            else {
                Log.d("Waiting room", "problème a la waiting room");
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
        findViewById(R.id.quick_match).setVisibility(View.GONE);
    }


    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }


    @Override
    public void onRoomConnecting(Room room) {
        mRoom = room;
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
// peer declined invitation -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, room.getRoomId());
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
// peer left -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, room.getRoomId());
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }
    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        return false;
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        if (mPlaying) {
            // add new player to an ongoing game
        } else if (shouldStartGame(room)) {
            // start game!
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        if (mPlaying) {
            // do game-specific handling of this -- remove player's avatar
            // from the screen, etc. If not enough players are left for
            // the game to go on, end the game and leave the room.
        } else if (shouldCancelGame(room)) {
            // cancel the game
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, room.getRoomId());
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void onRoomCreated(int i, Room room) {
        if (i != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Toast.makeText(this, "error lors de la creation de la room", Toast.LENGTH_SHORT).show();

            Log.d("onRoomCreated", "error lors de la creation de la room");
            return;
        }
        // get waiting room intent
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(intent, RC_WAITING_ROOM);

    }


    @Override
    public void onJoinedRoom(int i, Room room) {
        if (i != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Toast.makeText(this, "error lors de l'accès a la room", Toast.LENGTH_SHORT).show();
            Log.d("onJoinedRoom", "error lors de l'accès a la room");
            return;
        }

        // get waiting room intent
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(intent, RC_WAITING_ROOM);


    }

    @Override
    public void onLeftRoom(int i, String s) {
        Toast.makeText(this, "VOus avez quitté la partie", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        if (i != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            Toast.makeText(this, "error lors de la connexion a la room", Toast.LENGTH_SHORT).show();
            Log.d("onRoomConnected", "error lors de la connexion a la room");
        }

    }


    @Override
    protected void onPause() {
        // unregister the sensor (désenregistrer le capteur)
        sensorManager.unregisterListener(this, accelerometer);
        super.onPause();
    }

    @Override
    protected void onResume() {
        /* Ce qu'en dit Google&#160;dans le cas de l'accéléromètre :
         * «&#160; Ce n'est pas nécessaire d'avoir les évènements des capteurs à un rythme trop rapide.
         * En utilisant un rythme moins rapide (SENSOR_DELAY_UI), nous obtenons un filtre
         * automatique de bas-niveau qui "extrait" la gravité  de l'accélération.
         * Un autre bénéfice étant que l'on utilise moins d'énergie et de CPU.&#160;»
         */
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    /********************************************************************/
    /** SensorEventListener*************************************************/
    /********************************************************************/

    Boolean readyReverse = true;

    boolean firstStep = false;
    boolean secondStep = false;
    boolean thirdStep = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Récupérer les valeurs du capteur
        float x, y, z;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            if(y >= 9 && readyReverse){
                for(Carte c : tab_carte){
                    if(c.isSelected && c.state != Card_states.PIOCHE){
                        returncard(c, false);
                    }

                }
                readyReverse = false;
            }
            else if(y<8){
                readyReverse = true;
            }

            if( x > 7){
                firstStep = true;
            }
            if(x < -7 && firstStep){
                secondStep = true;
            }
            if( firstStep && secondStep && x > 7){
                thirdStep = true;
            }
            if(firstStep && secondStep && thirdStep && x < -7){
                mixCard(false);
                firstStep = false;
                secondStep = false;
                thirdStep = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
