package com.teammobi.dragonboy;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.teammobi.dragonboy.engine.GameEngine;
import com.teammobi.dragonboy.engine.InputHandler;
import com.teammobi.dragonboy.screens.MenuScreen;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private GameEngine gameEngine;
    private InputHandler inputHandler;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        inputHandler = new InputHandler();
        gameEngine = new GameEngine(context, inputHandler);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameEngine.init(getWidth(), getHeight());
        gameThread = new GameThread(getHolder(), this);
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        gameEngine.onSizeChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Forward touch to input handler (for game controls)
        inputHandler.handleTouch(event);

        // Also forward tap to menu screen for Play/Exit buttons
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            int idx = event.getActionIndex();
            float x = event.getX(idx);
            float y = event.getY(idx);
            gameEngine.onTap(x, y);
        }
        return true;
    }

    public void update() {
        gameEngine.update();
    }

    public void render(Canvas canvas) {
        gameEngine.render(canvas);
    }

    public void resume() {
        if (gameThread != null) gameThread.setRunning(true);
    }

    public void pause() {
        if (gameThread != null) gameThread.setRunning(false);
    }
}
