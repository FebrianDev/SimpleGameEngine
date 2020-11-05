package com.febrian.simplegameengine;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {
    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);

    }

    class GameView extends SurfaceView implements Runnable {
        int width, height, Xmax;

        //untuk mengakses index array bitmap
        int i = 0;
        //variable untuk set waktu saat berganti frame
        float timer;

        //variable untuk mengcek kondisi pas flip
        boolean isFlip = false;
        //variable untuk menyimpan value slip
        float flip = 1.0f;
        // This is our thread
        Thread gameThread = null;

        // This is new. We need a SurfaceHolder
        // When we use Paint and Canvas in a thread
        // We will see it in action in the draw method soon.
        SurfaceHolder ourHolder;

        // A boolean which we will set and unset
        // when the game is running- or not.
        volatile boolean playing;

        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;

        // This variable tracks the game frame rate
        long fps;

        // This is used to help calculate the fps
        private long timeThisFrame;

        // Simpan sprite pada array object bitmap
        Bitmap bitmapBob[] = {
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run0), 120, 120, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run1), 120, 120, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run2), 120, 120, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run3), 120, 120, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run4), 120, 120, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run5), 120, 120, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run6), 120, 120, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run7), 120, 120, false)
        };

        // Bob starts off not moving
        boolean isMoving = false;

        // He can walk at 150 pixels per second
        float walkSpeedPerSecond = 150;

        // He starts 10 pixels from the left
        float bobXPosition = 10;

        // When the we initialize (call new()) on gameView
        // This special constructor method runs
        public GameView(Context context) {
            // The next line of code asks the
            // SurfaceView class to set up our object.
            // How kind.
            super(context);
            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();

            // Load Bob from his .png file
            // bitmapBob[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.run0), 120, 120, false);

            // Set our boolean to true - game on!
            playing = true;

        }

        @Override
        public void run() {

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            width = size.x;
            height = size.y;
            Xmax = getResources().getDisplayMetrics().widthPixels;

            while (playing) {

                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // Update the frame
                update();

                // Draw the frame
                draw();

                // Calculate the fps this frame
                // We can then use the result to
                // time animations and more.
                timeThisFrame = System.currentTimeMillis() - startFrameTime;

                if (timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        // Everything that needs to be updated goes in here
        // In later projects we will have dozens (arrays) of objects.
        // We will also do other things like collision detection.
        public void update() {
            // If bob is moving (the player is touching the screen)
            // then move him to the right based on his target speed and the current fps.
            if (isMoving) {
                //tambahkan timer dengan fps
                timer += timeThisFrame;
                //jika timer lebih besar dari 60 milisecond
                if (timer > 60) {
                    //increment i untuk mengakses index selanjutnya dari array bitmap
                    i++;
                    //set timer menjadi 0 agar proses pergantian frame bisa berulang
                    timer = 0;
                }
                bobXPosition = bobXPosition + (walkSpeedPerSecond / fps);

                //jika posisi object/bitmap berada di batas layar kanan atau kiri set isFlip menjadi true
                if (bobXPosition >= Xmax - 80 || bobXPosition <= 0) {
                    walkSpeedPerSecond *= -1;
                    isFlip = true;
                } else {
                    isFlip = false;
                }

                //Jika i sama dengan panjang index array bitmap, maka set i menjadi 0 untuk mengulang proses animasi
                if (i == bitmapBob.length) {
                    i = 0;
                }
            }
        }

        // Draw the newly updated scene
        public void draw() {
            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255, 26, 128, 182));

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255, 249, 129, 0));

                // Make the text a bit bigger
                paint.setTextSize(45);

                // Display the current fps on the screen
                canvas.drawText("FPS:" + fps, 20, 40, paint);

                //Inisialisasi object bitmap yang menyimpan method flipImage
                Bitmap bob = flipImage(bitmapBob[i]);

                //Draw Bitmap
                canvas.drawBitmap(bob, bobXPosition, 200, paint);

                // Draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        public Bitmap flipImage(Bitmap source) {
            //deklarasi matrik untuk flip Bitmap
            Matrix matrix = new Matrix();
            //jika isFlip sama dengan true, kalikan flip dg -1
            if (isFlip)
                flip *= -1.0f;
            // set matrix prescale untuk membalikkan posisi bitmap
            matrix.preScale(flip, 1.0f);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }

        // If SimpleGameEngine Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SimpleGameEngine Activity is started then
        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        // The SurfaceView class implements onTouchListener
        // So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    // Set isMoving so Bob is moved in the update method
                    isMoving = true;

                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:

                    // Set isMoving so Bob does not move
                    isMoving = false;

                    break;
            }
            return true;
        }

    }
    // This is the end of our GameView inner class

    // More SimpleGameEngine methods will go here

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }
}