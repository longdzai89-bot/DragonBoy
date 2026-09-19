package com.teammobi.dragonboy.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.teammobi.dragonboy.engine.AssetManager;

import java.util.List;

public class Projectile {

    private float x, y;
    private float vx;
    private int   dmg;
    private boolean dead = false;
    private AssetManager assets;

    private static final float SPEED  = 10f;
    private static final float WIDTH  = 24f;
    private static final float HEIGHT = 16f;
    private static final int   MAX_TICKS = 90;
    private int ticks = 0;

    public Projectile(float x, float y, boolean facingRight, int dmg, AssetManager assets) {
        this.x      = x;
        this.y      = y;
        this.vx     = facingRight ? SPEED : -SPEED;
        this.dmg    = dmg;
        this.assets = assets;
    }

    public void update(List<Platform> platforms, List<Enemy> enemies) {
        if (dead) return;

        x += vx;
        ticks++;
        if (ticks > MAX_TICKS) { dead = true; return; }

        RectF myRect = getBounds();

        // Hit platform
        for (Platform p : platforms) {
            if (myRect.intersect(p.getBounds())) {
                dead = true;
                return;
            }
        }

        // Hit enemy
        for (Enemy e : enemies) {
            if (!e.isDead() && myRect.intersect(e.getBounds())) {
                e.takeDamage(dmg);
                dead = true;
                return;
            }
        }
    }

    public void render(Canvas canvas, float camX) {
        if (dead) return;
        Bitmap bmp = assets.get("fireball");
        if (bmp != null) {
            canvas.drawBitmap(bmp, null,
                new RectF(x - camX, y, x - camX + WIDTH, y + HEIGHT), null);
        }
    }

    public RectF getBounds() {
        return new RectF(x, y, x + WIDTH, y + HEIGHT);
    }

    public boolean isDead() { return dead; }
}
