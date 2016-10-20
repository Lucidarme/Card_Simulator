package com.visualisation.card_simulator;


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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity{


    HashMap<ImageView, Float> hash_pos_precX = new HashMap<ImageView, Float>();
    HashMap<ImageView, Float> hash_pos_precY = new HashMap<ImageView, Float>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
