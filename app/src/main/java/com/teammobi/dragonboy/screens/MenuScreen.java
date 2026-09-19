package com.teammobi.dragonboy.screens;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.teammobi.dragonboy.engine.AssetManager;
import com.teammobi.dragonboy.engine.GameEngine;
import com.teammobi.dragonboy.engine.InputHandler;

public class MenuScreen implements Screen {

    private GameEngine engine;
    private AssetManager assets;
    private InputHandler input;
    private int W, H;

    private Paint bgPaint, titlePaint, btnPaint, btnTextPaint, subPaint;
    private RectF btnPlay, btnExit;
    private float titleY;
    private int animTick = 0;

    // Floating particles
    private float[] px, py, pvy;
    private int[] pColor;
    private static final int PARTICLE_COUNT = 30;

    public MenuScreen(GameEngine engine, AssetManager assets, InputHandler input, int W, int H) {
        this.engine = engine;
        this.assets  = assets;
        this.input   = input;
        this.W = W;
        this.H = H;

        initPaints();
        initButtons();
        initParticles();
    }

    private void initPaints() {
        bgPaint = new Paint();

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(H * 0.09f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.YELLOW);
        titlePaint.setShadowLayer(8, 3, 3, Color.RED);

        subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setTextSize(H * 0.03f);
        subPaint.setTextAlign(Paint.Align.CENTER);
        subPaint.setColor(Color.CYAN);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(0xDD223388);

        btnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnTextPaint.setTextSize(H * 0.05f);
        btnTextPaint.setFakeBoldText(true);
        btnTextPaint.setTextAlign(Paint.Align.CENTER);
        btnTextPaint.setColor(Color.WHITE);
    }

    private void initButtons() {
        float bw = W * 0.55f;
        float bh = H * 0.09f;
        float cx = W / 2f;

        btnPlay = new RectF(cx - bw/2, H * 0.55f, cx + bw/2, H * 0.55f + bh);
        btnExit = new RectF(cx - bw/2, H * 0.67f, cx + bw/2, H * 0.67f + bh);
    }

    private void initParticles() {
        px    = new float[PARTICLE_COUNT];
        py    = new float[PARTICLE_COUNT];
        pvy   = new float[PARTICLE_COUNT];
        pColor = new int[PARTICLE_COUNT];
        int[] colors = {Color.YELLOW, Color.CYAN, Color.RED, Color.GREEN, Color.MAGENTA};
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i]    = (float)(Math.random() * W);
            py[i]    = (float)(Math.random() * H);
            pvy[i]   = -(float)(Math.random() * 1.5f + 0.5f);
            pColor[i] = colors[i % colors.length];
        }
    }

    @Override
    public void update() {
        animTick++;

        // Update particles
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            py[i] += pvy[i];
            if (py[i] < 0) py[i] = H;
        }

        // Title bounce
        titleY = H * 0.3f + (float)(Math.sin(animTick * 0.05) * 8);

        // Check touch on buttons
        // (Input handling via pressed zones not set yet — use simple tap detection)
    }

    @Override
    public void render(Canvas canvas) {
        // Gradient background
        Paint gradPaint = new Paint();
        LinearGradient grad = new LinearGradient(
            0, 0, 0, H,
            new int[]{0xFF000033, 0xFF001166, 0xFF000022},
            null, Shader.TileMode.CLAMP
        );
        gradPaint.setShader(grad);
        canvas.drawRect(0, 0, W, H, gradPaint);

        // Particles (stars)
        Paint pPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pPaint.setColor(pColor[i]);
            pPaint.setAlpha(100 + (int)(Math.abs(Math.sin(animTick * 0.05 + i)) * 155));
            canvas.drawCircle(px[i], py[i], 3, pPaint);
        }

        // Title shadow
        canvas.drawText("DRAGON BOY", W / 2f, titleY, titlePaint);
        // Subtitle
        canvas.drawText("TeamMobi Edition", W / 2f, titleY + H * 0.07f, subPaint);

        // Draw hero sprite preview
        if (assets.get("hero_idle_" + ((animTick / 10) % 4)) != null) {
            android.graphics.Bitmap heroSprite = assets.get("hero_idle_" + ((animTick / 10) % 4));
            float heroX = W / 2f - heroSprite.getWidth();
            float heroY = H * 0.38f;
            canvas.drawBitmap(heroSprite, null,
                new RectF(heroX, heroY, heroX + heroSprite.getWidth() * 2, heroY + heroSprite.getHeight() * 2),
                null);
        }

        // Buttons
        drawButton(canvas, btnPlay, "PLAY", 0xFF0044DD);
        drawButton(canvas, btnExit, "EXIT", 0xFF880022);

        // Version
        Paint verPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        verPaint.setColor(0xFF888888);
        verPaint.setTextSize(H * 0.022f);
        verPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("v1.0 © TeamMobi", W / 2f, H * 0.95f, verPaint);
    }

    private void drawButton(Canvas canvas, RectF rect, String text, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Shadow
        p.setColor(0x66000000);
        canvas.drawRoundRect(new RectF(rect.left+4, rect.top+4, rect.right+4, rect.bottom+4), 16, 16, p);
        // Button bg
        p.setColor(color);
        canvas.drawRoundRect(rect, 16, 16, p);
        // Border
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        canvas.drawRoundRect(rect, 16, 16, p);
        p.setStyle(Paint.Style.FILL);
        // Text
        btnTextPaint.setColor(Color.WHITE);
        canvas.drawText(text, rect.centerX(), rect.centerY() + btnTextPaint.getTextSize() * 0.35f, btnTextPaint);
    }

    // Called by GameEngine to check button tap
    public void onTap(float x, float y) {
        if (btnPlay.contains(x, y)) {
            engine.switchToGame(1);
        } else if (btnExit.contains(x, y)) {
            System.exit(0);
        }
    }
}
