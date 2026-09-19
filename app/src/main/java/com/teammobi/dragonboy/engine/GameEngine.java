package com.teammobi.dragonboy.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.teammobi.dragonboy.screens.GameScreen;
import com.teammobi.dragonboy.screens.MenuScreen;
import com.teammobi.dragonboy.screens.Screen;

public class GameEngine {

    public enum State { MENU, PLAYING, GAME_OVER, WIN }

    private Context context;
    private InputHandler inputHandler;
    private AssetManager assetManager;
    private SoundManager soundManager;

    private Screen currentScreen;
    private MenuScreen menuScreen;
    private State state = State.MENU;

    private int screenWidth, screenHeight;
    private Paint bgPaint;

    public GameEngine(Context context, InputHandler inputHandler) {
        this.context = context;
        this.inputHandler = inputHandler;
        this.assetManager = new AssetManager(context);
        this.soundManager = new SoundManager(context);

        bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
    }

    public void init(int width, int height) {
        this.screenWidth  = width;
        this.screenHeight = height;
        assetManager.loadAll();
        switchToMenu();
    }

    public void onSizeChanged(int w, int h) {
        this.screenWidth  = w;
        this.screenHeight = h;
    }

    public void switchToMenu() {
        state = State.MENU;
        menuScreen = new MenuScreen(this, assetManager, inputHandler, screenWidth, screenHeight);
        currentScreen = menuScreen;
    }

    public void switchToGame(int level) {
        state = State.PLAYING;
        currentScreen = new GameScreen(this, assetManager, soundManager,
                inputHandler, screenWidth, screenHeight, level);
    }

    public void setGameOver() { state = State.GAME_OVER; }
    public void setWin()      { state = State.WIN; }

    /** Called from GameView on finger-up — for menu button taps */
    public void onTap(float x, float y) {
        if (state == State.MENU && menuScreen != null) {
            menuScreen.onTap(x, y);
        }
    }

    public void update() {
        if (currentScreen != null) currentScreen.update();
        inputHandler.clearPressed();
    }

    public void render(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, screenHeight, bgPaint);
        if (currentScreen != null) currentScreen.render(canvas);
    }

    public Context getContext()      { return context; }
    public int getScreenWidth()      { return screenWidth; }
    public int getScreenHeight()     { return screenHeight; }
    public State getState()          { return state; }
    public SoundManager getSoundManager() { return soundManager; }
}
