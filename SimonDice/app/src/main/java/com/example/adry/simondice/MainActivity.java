package com.example.adry.simondice;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.adry.simondice.R;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    MediaPlayer snd_click;
    MediaPlayer snd_win;
    MediaPlayer snd_lost;

    TextView txt_repetitions;
    TextView txt_record;

    Button btn_play;
    Button btn_colors[];

    ArrayList<Integer> match = new ArrayList<>();
    ArrayList<Integer> result = new ArrayList<>();

    // Prohibit responding before giving the series of colors
    Boolean answerable = false;

    SharedPreferences myPreferences;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The sounds are initialized
        snd_click = MediaPlayer.create(this, R.raw.click);
        snd_win = MediaPlayer.create(this, R.raw.win);
        snd_lost = MediaPlayer.create(this, R.raw.lost);

        // Labels for progress information
        txt_repetitions = (TextView) findViewById(R.id.repetitions);
        txt_record = (TextView) findViewById(R.id.record);
        myPreferences=getSharedPreferences("datos", Context.MODE_PRIVATE);
        txt_record.setText(myPreferences.getString("Record","0"));

        // Start / Restart button game
        btn_play = (Button) findViewById(R.id.status);
        btn_play.setOnClickListener(this);

        // 4 Main buttons
        btn_colors = new Button[]{
                (Button) findViewById(R.id.color_0),
                (Button) findViewById(R.id.color_1),
                (Button) findViewById(R.id.color_2),
                (Button) findViewById(R.id.color_3)};
        btn_colors[0].setOnClickListener(this);
        btn_colors[1].setOnClickListener(this);
        btn_colors[2].setOnClickListener(this);
        btn_colors[3].setOnClickListener(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString("Record", txt_record.getText().toString());
        outState.putString("Repetitions",txt_repetitions.getText().toString());

        outState.putString("Play", btn_play.getText().toString());

        outState.putIntegerArrayList("Match",match);
        outState.putIntegerArrayList("Result",result);

        outState.putBoolean("Answerable", answerable);

    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        txt_record.setText(savedInstanceState.getString("Record"));
        txt_repetitions.setText(savedInstanceState.getString("Repetitions"));

        btn_play.setText(savedInstanceState.getString("Play"));

        match = savedInstanceState.getIntegerArrayList("Match");
        result = savedInstanceState.getIntegerArrayList("Result");

        answerable = savedInstanceState.getBoolean("Answerable");

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.status) {
            generate_combination();
            btn_play.setCursorVisible(false);

        } else if(answerable){

            snd_click.start();
            check_combination(view);

        }
    }

    public void generate_combination() {
        // On-Thread duration father
        int time_father = 1000;
        // Off-Thread duration child
        final int time_child = 500;

        // Is generated a color between 0 and 4
        final int color = (int) (Math.random() * 4);
        match.add(color);

        // The signs are updated
        txt_repetitions.setText(String.valueOf(match.size()));

        if(Integer.parseInt(String.valueOf(txt_repetitions.getText())) > Integer.parseInt(String.valueOf(txt_record.getText()))){
            txt_record.setText(txt_repetitions.getText());

            SharedPreferences preferencias=getSharedPreferences("datos",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putString("Record", txt_record.getText().toString());
            editor.commit();

        }


        // The start button is disabled
        btn_play.setVisibility(View.INVISIBLE);

        for (int i = 0; i < match.size(); i++) {
            final int finalI = i;

            Handler on_father = new Handler();
            final Handler off_child = new Handler();

            // ON animation
            on_father.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn_colors[match.get(finalI)].setPressed(true);
                    answerable = false;

                    // Off animation
                    off_child.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btn_colors[match.get(finalI)].setPressed(false);
                            answerable = true;
                        }
                    }, time_child);
                }
            }, time_father * i);

        }


    }

    public void check_combination(View color_click) {

        if (color_click.getId() == R.id.color_0) {
            result.add(0);
        } else if (color_click.getId() == R.id.color_1) {
            result.add(1);
        } else if (color_click.getId() == R.id.color_2) {
            result.add(2);
        } else {
            result.add(3);
        }

        if (match.size() == result.size()) {
            for (int u = 0; u < match.size(); u++) {
                if (match.get(u).equals(result.get(u))) {
                    btn_play.setText("Siguiente serie de colores");
                    if (u == match.size() - 1) {

                        Toast float_message = Toast.makeText(this,"GANASTE", Toast.LENGTH_SHORT);
                        float_message.setGravity(1,2,2);
                        float_message.show();

                        snd_win.start();
                        answerable = false;
                    }
                } else {
                    result.clear();
                    match.clear();
                    txt_repetitions.setText("0");
                    btn_play.setText("Reiniciar");

                    Toast float_message = Toast.makeText(this,"PERDISTE", Toast.LENGTH_SHORT);
                    float_message.setGravity(1,2,2);
                    float_message.show();

                    snd_lost.start();
                    answerable = false;
                }
            }
            result.clear();

            btn_play.setVisibility(View.VISIBLE);

        }
    }


}
