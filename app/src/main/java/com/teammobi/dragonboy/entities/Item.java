package com.teammobi.dragonboy.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.teammobi.dragonboy.engine.AssetManager;

public class Item {

    public enum Type { COIN, HP_ORB, GEM }

    private float x, y;
    private float vy = 0;
    private boolean collected = false;
    private Type type;
    private AssetManager assets;
    private float bobOffset = 0;
    private int tick = 0;

    private static final float SIZE    = 20f;
    private static final float GRAVITY = 0.3f;

    public Item(float x, float y, Type type, AssetManager assets) {
        this.x     = x;
        this.y     = y;
        this.type  = type;
        this.assets = assets;
    }

    public void update(float groundY) {
        if (collected) return;
        tick++;
        bobOffset = (float)(Math.sin(tick * 0.1) * 4);
        if (y + SIZE < groundY) {
            vy += GRAVITY;
            y  += vy;
        } else {
            vy = 0;
        }
    }

    public boolean checkCollect(Hero hero) {
        if (collected) return false;
        if (getBounds().intersect(hero.getBounds())) {
            collected = true;
            applyEffect(hero);
            return true;
        }
        return false;
    }

    private void applyEffect(Hero hero) {
        switch (type) {
            case COIN:   hero.coins += 1; break;
            case HP_ORB: hero.hp = Math.min(hero.hp + 20, hero.maxHp); break;
            case GEM:    hero.coins += 5; break;
        }
    }

    public void render(Canvas canvas, float camX) {
        if (collected) return;
        String key = type == Type.COIN ? "coin"
                   : type == Type.HP_ORB ? "hp_orb" : "gem";
        Bitmap bmp = assets.get(key);
        float drawX = x - camX;
        float drawY = y + bobOffset;
        if (bmp != null) {
            canvas.drawBitmap(bmp, null,
                new RectF(drawX, drawY, drawX + SIZE, drawY + SIZE), null);
        }
    }

    public RectF getBounds() {
        return new RectF(x, y, x + SIZE, y + SIZE);
    }

    public boolean isCollected() { return collected; }
}
