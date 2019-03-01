package com.arllansantiagodev.dungeon;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    //Frame
    private FrameLayout gameFrame;
    private int frameHeight, frameWidth, initialFrameWidth;
    private LinearLayout startLayout;

    //Image
    private ImageView box, pink,black,orange;
    private Drawable imageBoxRight, imageBoxLeft, initialBackground;
    //Size
    private int boxSize;

    //Position
    private float boxX, boxY;
    private float blackX, blackY;
    private float pinkX, pinkY;
    private float orangeX, orangeY;

    //Score
    private TextView scoreLabel, highScoreLabel, lastScoreLabel;
    private int score, highScore,lastScore, timeCount ;
    private SharedPreferences settings;

    //Class
    private Timer timer;
    private Handler handler = new Handler();
    private SoundPlayer soundPlayer;

    //Status
    private boolean start_flg =  false;
    private boolean action_flg =  false;
    private boolean pink_flg =  false;
    private MediaPlayer playerStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundPlayer = new SoundPlayer(this);
        gameFrame = findViewById(R.id.gameFrame);
        startLayout = findViewById(R.id.startLayout);
        box = findViewById(R.id.box);
        pink = findViewById(R.id.pink);
        black = findViewById(R.id.black);
        orange = findViewById(R.id.orange);
        scoreLabel = findViewById(R.id.scoreLabel);
        highScoreLabel = findViewById(R.id.highScoreLabel);
        lastScoreLabel = findViewById(R.id.lastScoreLabel);

        imageBoxLeft = getResources().getDrawable(R.drawable.box_left);
        imageBoxRight =getResources().getDrawable(R.drawable.box_right);

        //High Score
        settings = getSharedPreferences("GAME_DATE",Context.MODE_PRIVATE);
        highScore = settings.getInt("HIGH_SCORE",0);
        highScoreLabel.setText("High Score: " + highScore);
    }

    public void changePas(){
        //Add timeCount
        timeCount += 20;

        //Orange
        orangeY +=12;

        float orangeCenterX = orangeX + orange.getWidth() / 2;
        float orangeCenterY = orangeY + orange.getHeight()/ 2;

        if (hitCheck(orangeCenterX,orangeCenterY)){
            orangeY = frameHeight + 100;
            score += 10;
            soundPlayer.playHitOrangeSound();
        }
        if (orangeY> frameHeight){
            orangeY = -100;
            orangeX = (float) Math.floor(Math.random() * (frameWidth - orange.getWidth()));
        }

        orange.setX(orangeX);
        orange.setY(orangeY);

        //Pink
        if(!pink_flg && timeCount % 10000 == 0){
            pink_flg = true;
            pinkY = -20;
            pinkX = (float) Math.floor(Math.random()* (frameWidth - pink.getWidth()));
        }

        if (pink_flg) {
            pinkY += 20;
            float pinkCenterX = pinkX + pink.getWidth() / 2;
            float pinkCenterY = pinkY + pink.getWidth() / 2;

            if(hitCheck(pinkCenterX,pinkCenterY)){
                pinkY = frameHeight + 30;
                score += 30;
                soundPlayer.playHitPinkSound();
                //Change frameWidth
                if(initialFrameWidth > frameWidth *110/100) {
                    frameWidth = frameWidth * 110/100;
                    changeFrameWidth(frameWidth);
                }
            }

            if (pinkY > frameHeight){
                pink_flg = false;
            }
            pink.setX(pinkX);
            pink.setY(pinkY);
        }

        //Black
        blackY += 30;
        float blackCenterX = blackX + black.getWidth()/2;
        float blackCenterY = blackY + black.getWidth()/2;

        if (hitCheck(blackCenterX,blackCenterY)){
            blackY = frameHeight + 100;
            soundPlayer.playHitBlackSound();
            //Change FrameWidth
            frameWidth = frameWidth * 80 /100;
            changeFrameWidth(frameWidth);
            if (frameWidth < boxSize){
                //Game Over
                gameOver();

            }
        }

        if(blackY > frameHeight){
            blackY = -100;
            blackX = (float) Math.floor(Math.random() * (frameWidth - black.getWidth()));
        }

        black.setX(blackX);
        black.setY(blackY);

        //Move box
        if(action_flg){
            //touching
            boxX += 14;
            box.setImageDrawable(imageBoxRight);
        }else{
            //Releasing
            boxX -= 14;
            box.setImageDrawable(imageBoxLeft);

        }

        //Check box position
        if (boxX < 0 ){
            boxX = 0;
            box.setImageDrawable(imageBoxRight);
        }
        if(frameWidth - boxSize < boxX){
            boxX = frameWidth - boxSize;
            box.setImageDrawable(imageBoxLeft);
        }

        box.setX(boxX);
        scoreLabel.setText("Score : "+ score);
    }
    public void gameOver(){
        //Stop timer.
        timer.cancel();
        timer = null;
        start_flg = false;
        changeFrameWidth(initialFrameWidth);
        try {
            playerStart.stop();
            MediaPlayer player = MediaPlayer.create(this,R.raw.gameover);
            player.setLooping(false);
            player.start();
            TimeUnit.SECONDS.sleep(1);

        } catch (InterruptedException e){
           e.printStackTrace();
        }
        startLayout.setVisibility(View.VISIBLE);
        box.setVisibility(View.INVISIBLE);
        orange.setVisibility(View.INVISIBLE);
        black.setVisibility(View.INVISIBLE);
        pink.setVisibility(View.INVISIBLE);
        scoreLabel.setVisibility(View.INVISIBLE);
        gameFrame.setBackgroundColor(0);
        //Update Last Score
        lastScore = score;
        lastScoreLabel.setText("Last Score: "+lastScore);


        //Update High Score
        if (score > highScore){
            highScore = score;
            highScoreLabel.setText("High Score : "+ highScore);

            SharedPreferences.Editor editor = settings.edit();

            editor.putInt("HIGH_SCORE", highScore);
            editor.commit();
        }
    }
    public boolean hitCheck(float x, float y){
        if(boxX<= x && x <= boxX + boxSize &&
                boxY <= y && y <= frameHeight){
            return true;
        }
        return false;
    }

    public void changeFrameWidth(int frameWidth){
        ViewGroup.LayoutParams params = gameFrame.getLayoutParams();
        params.width = frameWidth;
        gameFrame.setLayoutParams(params);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
       if(start_flg){
           if(event.getAction()== MotionEvent.ACTION_DOWN){
                action_flg = true;
           }else if (event.getAction() == MotionEvent.ACTION_UP) {
               action_flg = false;
           }
       }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void startGame(View view){
        start_flg = true;
        startLayout.setVisibility(View.INVISIBLE);
        playerStart = MediaPlayer.create(this,R.raw.playerstart);
        playerStart.setLooping(false);
        playerStart.start();
        if(frameHeight == 0){
            frameHeight = gameFrame.getHeight();
            frameWidth  = gameFrame.getWidth();
            initialFrameWidth = frameWidth;

            boxSize = box.getHeight();
            boxX = box.getX();
            boxY = box.getY();

        }
        frameWidth = initialFrameWidth;
        initialBackground = gameFrame.getBackground();
        gameFrame.setBackgroundColor(Color.parseColor("#99FFFFFF"));
        box.setX(0.0f);
        black.setY(3000.0f);
        orange.setY(3000.0f);
        pink.setY(3000.0f);

        blackY = black.getY();
        orangeY = orange.getY();
        pinkY =  pink.getY();

        scoreLabel.setVisibility(View.VISIBLE);
        box.setVisibility(View.VISIBLE);
        black.setVisibility(View.VISIBLE);
        orange.setVisibility(View.VISIBLE);
        pink.setVisibility(View.VISIBLE);

        timeCount = 0;
        score = 0;
        scoreLabel.setText("Score : 0");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(start_flg){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePas();
                        }
                    });
                }
            }
        }, 0,20);

    }


    public void quitGame(View view){
        finish();
        System.exit(0);
    }
}
