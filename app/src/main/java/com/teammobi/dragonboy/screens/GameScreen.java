package com.teammobi.dragonboy.screens;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.teammobi.dragonboy.engine.AssetManager;
import com.teammobi.dragonboy.engine.GameEngine;
import com.teammobi.dragonboy.engine.InputHandler;
import com.teammobi.dragonboy.engine.SoundManager;
import com.teammobi.dragonboy.entities.Enemy;
import com.teammobi.dragonboy.entities.Hero;
import com.teammobi.dragonboy.entities.Item;
import com.teammobi.dragonboy.entities.Platform;
import com.teammobi.dragonboy.entities.Projectile;
import com.teammobi.dragonboy.utils.HUD;
import com.teammobi.dragonboy.utils.LevelBuilder;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private GameEngine   engine;
    private AssetManager assets;
    private SoundManager sound;
    private InputHandler input;
    private int W, H, level;

    // Game objects
    private Hero   hero;
    private List<Platform> platforms;
    private List<Enemy>    enemies;
    private List<Item>     items;

    // Camera
    private float camX = 0;
    private float levelWidth;

    // HUD
    private HUD hud;

    // State
    private enum State { PLAYING, DEAD, WIN }
    private State state = State.PLAYING;
    private int   stateTimer = 0;

    // Background parallax layers
    private Paint bgPaint;

    // Goal portal
    private float goalX, goalY;
    private int   goalAnim = 0;

    public GameScreen(GameEngine engine, AssetManager assets, SoundManager sound,
                      InputHandler input, int W, int H, int level) {
        this.engine = engine;
        this.assets = assets;
        this.sound  = sound;
        this.input  = input;
        this.W      = W;
        this.H      = H;
        this.level  = level;

        bgPaint = new Paint();

        hud = new HUD(W, H, input);

        buildLevel();
    }

    private void buildLevel() {
        LevelBuilder.LevelData data = LevelBuilder.build(level, W, H, assets);

        platforms  = data.platforms;
        enemies    = data.enemies;
        items      = data.items;
        levelWidth = data.levelWidth;
        goalX      = data.goalX;
        goalY      = data.goalY;

        hero = new Hero(data.heroStartX, data.heroStartY, assets, input);
    }

    @Override
    public void update() {
        goalAnim++;

        if (state == State.PLAYING) {
            updatePlaying();
        } else {
            stateTimer++;
            if (stateTimer > 120) {
                checkStateTransition();
            }
        }
    }

    private void updatePlaying() {
        hero.update(platforms, enemies);

        // Update enemies
        for (Enemy e : enemies) {
            e.update(hero, platforms);
        }

        // Update items
        float groundY = H - 100f;
        for (Item item : items) {
            item.update(groundY);
            item.checkCollect(hero);
        }

        // Update hero projectiles (already in Hero.update, but render from here)
        // (Projectiles updated inside Hero.update)

        // Camera follow hero
        camX = hero.x - W * 0.35f;
        if (camX < 0) camX = 0;
        if (camX > levelWidth - W) camX = levelWidth - W;

        // Check death
        if (hero.isDead() || hero.y > H + 100) {
            state      = State.DEAD;
            stateTimer = 0;
        }

        // Check spike collision
        for (Platform p : platforms) {
            if (p.isDeadly() && p.getBounds().intersect(hero.getBounds())) {
                hero.takeDamage(9999);
            }
        }

        // Check goal
        RectF goalRect = new RectF(goalX, goalY, goalX + 60, goalY + 80);
        if (hero.getBounds().intersect(goalRect)) {
            state      = State.WIN;
            stateTimer = 0;
        }
    }

    private void checkStateTransition() {
        if (state == State.DEAD) {
            // Tap to restart same level
            // (Tap detected via input — any button = restart)
            if (anyInput()) {
                engine.switchToGame(level);
            }
        } else if (state == State.WIN) {
            if (anyInput()) {
                int nextLevel = level < 3 ? level + 1 : 1;
                engine.switchToGame(nextLevel);
            }
        }
    }

    private boolean anyInput() {
        return input.isHeld(InputHandler.BTN_JUMP)
            || input.isHeld(InputHandler.BTN_ATTACK)
            || input.isHeld(InputHandler.BTN_SKILL);
    }

    @Override
    public void render(Canvas canvas) {
        renderBackground(canvas);
        renderPlatforms(canvas);
        renderItems(canvas);
        renderEnemies(canvas);
        renderProjectiles(canvas);
        renderHero(canvas);
        renderGoal(canvas);
        renderHUD(canvas);
    }

    private void renderBackground(Canvas canvas) {
        // Sky gradient
        Paint g = new Paint();
        LinearGradient sky;
        if (level == 3) {
            sky = new LinearGradient(0, 0, 0, H,
                new int[]{0xFF1A0010, 0xFF440022, 0xFF220011}, null, Shader.TileMode.CLAMP);
        } else if (level == 2) {
            sky = new LinearGradient(0, 0, 0, H,
                new int[]{0xFF0A1A2A, 0xFF1A3A1A, 0xFF2A1A00}, null, Shader.TileMode.CLAMP);
        } else {
            sky = new LinearGradient(0, 0, 0, H,
                new int[]{0xFF1A3A6A, 0xFF3A6AAA, 0xFF6A9ACA}, null, Shader.TileMode.CLAMP);
        }
        g.setShader(sky);
        canvas.drawRect(0, 0, W, H, g);

        // Parallax background elements (simple mountains/trees)
        renderParallax(canvas);
    }

    private void renderParallax(Canvas canvas) {
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float parallax = camX * 0.3f;

        // Far mountains
        mPaint.setColor(level == 3 ? 0xFF330011 : 0xFF1A4A6A);
        for (int i = 0; i < 6; i++) {
            float mx = i * 300 - parallax % 300;
            float mh = 150 + (i % 3) * 60;
            drawTriangle(canvas, mx, H - 100, mx + 150, H - 100 - mh, mx + 300, H - 100, mPaint);
        }

        // Mid trees
        mPaint.setColor(level == 3 ? 0xFF550022 : 0xFF0A3A0A);
        for (int i = 0; i < 10; i++) {
            float tx = i * 180 - (parallax * 0.6f) % 180;
            float th = 80 + (i % 3) * 30;
            float tw = 40;
            canvas.drawRect(tx + tw * 0.4f, H - 100 - th, tx + tw * 0.6f, H - 100, mPaint);
            drawTriangle(canvas, tx, H - 100 - th * 0.6f,
                tx + tw / 2, H - 100 - th, tx + tw, H - 100 - th * 0.6f, mPaint);
        }
    }

    private void drawTriangle(Canvas canvas, float x1, float y1, float x2, float y2,
                              float x3, float y3, Paint paint) {
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void renderPlatforms(Canvas canvas) {
        for (Platform p : platforms) {
            p.render(canvas, camX);
        }
    }

    private void renderItems(Canvas canvas) {
        for (Item item : items) {
            item.render(canvas, camX);
        }
    }

    private void renderEnemies(Canvas canvas) {
        for (Enemy e : enemies) {
            e.render(canvas, camX);
        }
    }

    private void renderProjectiles(Canvas canvas) {
        for (Projectile p : hero.projectiles) {
            p.render(canvas, camX);
        }
    }

    private void renderHero(Canvas canvas) {
        hero.render(canvas, camX);
    }

    private void renderGoal(Canvas canvas) {
        float gx = goalX - camX;
        // Animated portal
        Paint portalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float pulse = 1f + 0.1f * (float)Math.sin(goalAnim * 0.1);

        portalPaint.setColor(0xAAFFDD00);
        canvas.drawOval(new RectF(gx, goalY, gx + 60, goalY + 80), portalPaint);
        portalPaint.setColor(0x66FF8800);
        canvas.drawOval(new RectF(gx - 10*pulse, goalY - 15*pulse,
                                  gx + 70*pulse, goalY + 95*pulse), portalPaint);

        Paint lbl = new Paint(Paint.ANTI_ALIAS_FLAG);
        lbl.setColor(Color.YELLOW);
        lbl.setTextAlign(Paint.Align.CENTER);
        lbl.setTextSize(18);
        canvas.drawText("GOAL", gx + 30, goalY - 10, lbl);
    }

    private void renderHUD(Canvas canvas) {
        if (state == State.PLAYING) {
            hud.render(canvas, hero, level, hero.coins);
        } else if (state == State.DEAD) {
            hud.render(canvas, hero, level, hero.coins);
            hud.renderGameOver(canvas, hero.coins);
        } else if (state == State.WIN) {
            hud.render(canvas, hero, level, hero.coins);
            hud.renderWin(canvas, level, hero.coins);
        }
    }
}
