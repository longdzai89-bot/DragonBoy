package com.teammobi.dragonboy.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.teammobi.dragonboy.engine.AssetManager;

import java.util.List;

public class Enemy {

    public enum Type { SLIME, SKELETON, BOSS }

    protected float x, y;
    protected float width, height;
    protected float vx, vy;
    protected boolean onGround = false;
    protected boolean facingRight = false;

    protected int hp, maxHp, atk;
    protected Type type;
    protected String spritePrefix;

    protected int animFrame = 0;
    protected int animTick  = 0;
    protected int animFrames = 4;

    protected boolean dead      = false;
    protected int     hurtTick  = 0;
    protected float   aggroRange = 250f;
    protected float   attackRange = 50f;
    protected int     attackCooldown = 0;

    private static final float GRAVITY   = 0.5f;
    private static final float MAX_FALL  = 15f;
    protected float moveSpeed  = 2f;

    protected AssetManager assets;
    protected Paint hpPaint, hpBgPaint;

    // Drop
    public int coinDrop = 5;
    public int expDrop  = 10;

    public Enemy(float x, float y, Type type, AssetManager assets) {
        this.x     = x;
        this.y     = y;
        this.type  = type;
        this.assets = assets;

        switch (type) {
            case SLIME:
                width = 32; height = 32; hp = 40; maxHp = 40; atk = 8;
                spritePrefix = "slime_"; moveSpeed = 1.5f; coinDrop = 3; expDrop = 5;
                break;
            case SKELETON:
                width = 40; height = 56; hp = 80; maxHp = 80; atk = 15;
                spritePrefix = "skel_"; moveSpeed = 2.5f; coinDrop = 8; expDrop = 15;
                break;
            case BOSS:
                width = 96; height = 96; hp = 500; maxHp = 500; atk = 30;
                spritePrefix = "boss_"; moveSpeed = 1.5f; aggroRange = 500f;
                attackRange = 100f; coinDrop = 50; expDrop = 200; animFrames = 6;
                break;
        }

        hpPaint = new Paint();
        hpPaint.setColor(Color.RED);
        hpBgPaint = new Paint();
        hpBgPaint.setColor(Color.DKGRAY);
    }

    public void update(Hero hero, List<Platform> platforms) {
        if (dead) return;

        applyPhysics(platforms);
        updateAI(hero);
        updateAnimation();
        if (attackCooldown > 0) attackCooldown--;
        if (hurtTick > 0) hurtTick--;
    }

    protected void updateAI(Hero hero) {
        float dx = hero.x - x;
        float dist = Math.abs(dx);

        if (dist < aggroRange) {
            // Chase
            if (dist > attackRange) {
                facingRight = dx > 0;
                vx = facingRight ? moveSpeed : -moveSpeed;
            } else {
                vx *= 0.8f;
                // Attack hero
                if (attackCooldown == 0) {
                    attackCooldown = 90;
                    if (getBounds().intersect(hero.getBounds())) {
                        hero.takeDamage(atk);
                    }
                }
            }
        } else {
            // Patrol
            vx *= 0.9f;
        }
    }

    private void applyPhysics(List<Platform> platforms) {
        vy += GRAVITY;
        if (vy > MAX_FALL) vy = MAX_FALL;

        x += vx;
        y += vy;
        onGround = false;

        RectF myRect = getBounds();
        for (Platform p : platforms) {
            RectF pRect = p.getBounds();
            if (myRect.intersect(pRect)) {
                myRect = getBounds();
                if (!myRect.intersect(pRect)) continue;

                float oTop    = myRect.bottom - pRect.top;
                float oBottom = pRect.bottom  - myRect.top;
                float oLeft   = myRect.right  - pRect.left;
                float oRight  = pRect.right   - myRect.left;

                float minH = Math.min(oLeft, oRight);
                float minV = Math.min(oTop,  oBottom);

                if (minV < minH) {
                    if (oTop < oBottom) {
                        y -= oTop;
                        vy = 0;
                        onGround = true;
                    } else {
                        y += oBottom;
                        vy = 0;
                    }
                } else {
                    if (oLeft < oRight) { x -= oLeft; vx = 0; }
                    else { x += oRight; vx = 0; }
                }
            }
        }
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= 8) {
            animTick = 0;
            animFrame = (animFrame + 1) % animFrames;
        }
    }

    public void takeDamage(int dmg) {
        if (dead) return;
        hp -= dmg;
        hurtTick = 10;
        if (hp <= 0) {
            hp   = 0;
            dead = true;
        }
        // Knockback
        vx = facingRight ? -2f : 2f;
        vy = -3f;
    }

    public void render(Canvas canvas, float camX) {
        if (dead) return;

        Bitmap bmp = assets.get(spritePrefix + animFrame);
        float drawX = x - camX;

        if (bmp != null) {
            Matrix m = new Matrix();
            if (!facingRight) {
                m.setScale(-1, 1);
                m.postTranslate(bmp.getWidth(), 0);
            }
            m.postScale(width / bmp.getWidth(), height / bmp.getHeight());
            m.postTranslate(drawX, y);

            Paint p = new Paint();
            if (hurtTick > 0) {
                android.graphics.ColorMatrixColorFilter cf =
                    new android.graphics.ColorMatrixColorFilter(new android.graphics.ColorMatrix(new float[]{
                        1,0,0,0,100, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,1,0
                    }));
                p.setColorFilter(cf);
            }
            canvas.drawBitmap(bmp, m, p);
        }

        renderHpBar(canvas, drawX);
    }

    private void renderHpBar(Canvas canvas, float drawX) {
        float barW = width;
        float barH = 6;
        float barX = drawX;
        float barY = y - 12;

        canvas.drawRect(barX, barY, barX + barW, barY + barH, hpBgPaint);
        float ratio = (float) hp / maxHp;
        hpPaint.setColor(ratio > 0.5f ? Color.GREEN : ratio > 0.25f ? Color.YELLOW : Color.RED);
        canvas.drawRect(barX, barY, barX + barW * ratio, barY + barH, hpPaint);
    }

    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public boolean isDead()  { return dead; }
    public Type    getType() { return type; }
    public float   getX()    { return x; }
    public float   getY()    { return y; }
}
