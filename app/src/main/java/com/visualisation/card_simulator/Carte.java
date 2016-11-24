package com.visualisation.card_simulator;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by lucidarme on 26/10/16.
 */

public class Carte {
    public Card_states state;
    public int ID;
    public ImageView imageView;
    public Drawable image;
    public boolean isSelected;
    private boolean isFront;

    public Carte(int ID, ImageView im){
        state = Card_states.PIOCHE;
        this.ID = ID;
        imageView = im;
        isSelected = false;
        isFront = true;
    }

    public void setFront(){
        imageView.setImageDrawable(image);
        isFront = true;
    }

    public void setBack(){
        imageView.setImageResource(R.drawable.card_back);
        isFront = false;
    }

    public boolean isFront(){
        return isFront;
    }
}
