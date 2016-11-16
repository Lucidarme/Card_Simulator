package com.visualisation.card_simulator;

import android.widget.ImageView;

/**
 * Created by lucidarme on 26/10/16.
 */

public class Carte {
    public Card_states state;
    public int ID;
    public ImageView imageView;

    public Carte(int ID, ImageView im){
        state = Card_states.PIOCHE;
        this.ID = ID;
        imageView = im;
    }

}
