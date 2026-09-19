package com.teammobi.dragonboy.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.teammobi.dragonboy.engine.AssetManager;
import com.teammobi.dragonboy.engine.InputHandler;

import java.util.ArrayList;
import java.util.List;

public class Hero {

    public enum State {
        IDLE, RUN, JUMP, FALL, ATTACK, SKILL, HURT, DIE
    }

    // Position & physics
    public float x, y;
    public float vx, vy;
    public float width  = 40f;
    public float height = 60f;
    public boolean onGround = false;
    public boolean facingRight = true;

    // Stats
    public int hp     = 100;
    public int maxHp  = 100;
    public int mp     = 50;
    public int maxMp  = 50;
    public int level  = 1;
    public int exp    = 0;
    public int coins  = 0;
    public int atk    = 20;

    // Physics constants
    private static final float GRAVITY    =  0.5f;
    private static final float JUMP_FORCE = -14f;
    private static final float MOVE_SPEED =  4.5f;
    private static final float MAX_FALL   =  18f;

    // State & animation
    private State state        = State.IDLE;
    private int   animFrame    = 0;
    private int   animTick     = 0;
    private int   animSpeed    = 8;

    // Hurt invincibility
    private int invincibleTick = 0;
    private static final int INVINCIBLE_DURATION = 60;

    // Attack hitbox active
    private int attackTick = 0;
    private boolean hitboxActive = false;

    // Skill
    private int skillCooldown = 0;
    private static final int SKILL_COST = 20;

    private AssetManager assets;
    private InputHandler  input;
    private Paint         hpPaint, mpPaint, bgPaint, outlinePaint;

    // Projectiles created by this hero
    public List<Projectile> projectiles = new ArrayList<>();

    public Hero(float x, float y, AssetManager assets, InputHandler input) {
        this.x = x;
        this.y = y;
        this.assets = assets;
        this.input  = input;

        hpPaint = new Paint();
        hpPaint.setColor(Color.RED);
        mpPaint = new Paint();
        mpPaint.setColor(Color.BLUE);
        bgPaint = new Paint();
        bgPaint.setColor(Color.DKGRAY);
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(1f);
    }

    public void update(List<Platform> platforms, List<Enemy> enemies) {
        handleInput();
        applyPhysics(platforms);
        updateAnimation();
        updateSkillCooldown();
        updateInvincibility();
        updateAttack(enemies);
        updateProjectiles(platforms, enemies);
    }

    private void handleInput() {
        if (state == State.DIE || state == State.HURT) return;

        // Horizontal movement
        if (input.isHeld(InputHandler.BTN_LEFT)) {
            vx = -MOVE_SPEED;
            facingRight = false;
            if (onGround && state != State.ATTACK) setState(State.RUN);
        } else if (input.isHeld(InputHandler.BTN_RIGHT)) {
            vx = MOVE_SPEED;
            facingRight = true;
            if (onGround && state != State.ATTACK) setState(State.RUN);
        } else {
            vx *= 0.7f;
            if (Math.abs(vx) < 0.1f) vx = 0;
            if (onGround && state != State.ATTACK && state != State.SKILL) setState(State.IDLE);
        }

        // Jump
        if (input.isPressed(InputHandler.BTN_JUMP) && onGround) {
            vy = JUMP_FORCE;
            onGround = false;
            setState(State.JUMP);
        }

        // Attack
        if (input.isPressed(InputHandler.BTN_ATTACK) && state != State.ATTACK && state != State.SKILL) {
            startAttack();
        }

        // Skill (Dragon Fire)
        if (input.isPressed(InputHandler.BTN_SKILL)
                && skillCooldown == 0
                && mp >= SKILL_COST
                && state != State.ATTACK
                && state != State.SKILL) {
            startSkill();
        }
    }

    private void startAttack() {
        setState(State.ATTACK);
        animFrame   = 0;
        attackTick  = 0;
        hitboxActive = true;
    }

    private void startSkill() {
        setState(State.SKILL);
        animFrame  = 0;
        attackTick = 0;
        mp -= SKILL_COST;
        skillCooldown = 180;
        // Spawn fireball
        float fbX = facingRight ? x + width : x - 24;
        projectiles.add(new Projectile(fbX, y + height / 2 - 8, facingRight, 10, assets));
    }

    private void applyPhysics(List<Platform> platforms) {
        // Gravity
        vy += GRAVITY;
        if (vy > MAX_FALL) vy = MAX_FALL;

        x += vx;
        y += vy;

        // Platform collision
        onGround = false;
        RectF heroRect = getBounds();
        for (Platform p : platforms) {
            RectF pRect = p.getBounds();
            if (heroRect.intersect(pRect)) {
                // Restore before resolving
                heroRect = getBounds();
                if (!heroRect.intersect(pRect)) continue;

                float overlapLeft   = heroRect.right - pRect.left;
                float overlapRight  = pRect.right  - heroRect.left;
                float overlapTop    = heroRect.bottom - pRect.top;
                float overlapBottom = pRect.bottom - heroRect.top;

                float minH = Math.min(overlapLeft, overlapRight);
                float minV = Math.min(overlapTop,  overlapBottom);

                if (minV < minH) {
                    if (overlapTop < overlapBottom) {
                        y -= overlapTop;
                        if (vy > 0) {
                            vy = 0;
                            onGround = true;
                        }
                    } else {
                        y += overlapBottom;
                        if (vy < 0) vy = 0;
                    }
                } else {
                    if (overlapLeft < overlapRight) {
                        x -= overlapLeft;
                    } else {
                        x += overlapRight;
                    }
                }
            }
        }

        // Update fall/jump state
        if (!onGround) {
            if (vy < 0 && state != State.ATTACK && state != State.SKILL) setState(State.JUMP);
            else if (vy > 1 && state != State.ATTACK && state != State.SKILL) setState(State.FALL);
        }
    }

    private void updateAttack(List<Enemy> enemies) {
        if (state == State.ATTACK) {
            attackTick++;
            hitboxActive = attackTick >= 4 && attackTick <= 12;

            if (hitboxActive) {
                RectF hitbox = getAttackHitbox();
                for (Enemy e : enemies) {
                    if (!e.isDead() && hitbox.intersect(e.getBounds())) {
                        e.takeDamage(atk);
                        hitboxActive = false; // Hit once per swing
                        break;
                    }
                }
            }

            if (attackTick >= 24) {
                setState(onGround ? State.IDLE : State.FALL);
                hitboxActive = false;
                attackTick   = 0;
            }
        }

        if (state == State.SKILL) {
            attackTick++;
            if (attackTick >= 30) {
                setState(onGround ? State.IDLE : State.FALL);
                attackTick = 0;
            }
        }
    }

    private void updateProjectiles(List<Platform> platforms, List<Enemy> enemies) {
        List<Projectile> dead = new ArrayList<>();
        for (Projectile p : projectiles) {
            p.update(platforms, enemies);
            if (p.isDead()) dead.add(p);
        }
        projectiles.removeAll(dead);
    }

    private void updateSkillCooldown() {
        if (skillCooldown > 0) skillCooldown--;
        // Slow MP regen
        if (mp < maxMp && animTick % 90 == 0) mp++;
    }

    private void updateInvincibility() {
        if (invincibleTick > 0) invincibleTick--;
    }

    private void updateAnimation() {
        animTick++;
        int frames = getFrameCount();
        if (animTick >= animSpeed) {
            animTick = 0;
            animFrame = (animFrame + 1) % frames;
        }
    }

    private int getFrameCount() {
        switch (state) {
            case IDLE:   return 4;
            case RUN:    return 6;
            case JUMP:
            case FALL:   return 2;
            case ATTACK: return 4;
            case SKILL:  return 5;
            default:     return 1;
        }
    }

    public void takeDamage(int dmg) {
        if (invincibleTick > 0 || state == State.DIE) return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            setState(State.DIE);
        } else {
            setState(State.HURT);
            invincibleTick = INVINCIBLE_DURATION;
            // Knockback
            vy = -6f;
            vx = facingRight ? -4f : 4f;
        }
    }

    private void setState(State s) {
        if (state == s) return;
        state     = s;
        animFrame = 0;
        animTick  = 0;
        animSpeed = s == State.RUN ? 6 : (s == State.ATTACK || s == State.SKILL ? 5 : 8);
    }

    public void render(Canvas canvas, float camX) {
        boolean blink = invincibleTick > 0 && (invincibleTick / 4) % 2 == 0;
        if (blink) return;

        String key = getSpriteKey();
        Bitmap bmp = assets.get(key);

        float drawX = x - camX;

        if (bmp != null) {
            Matrix m = new Matrix();
            if (!facingRight) {
                m.setScale(-1, 1);
                m.postTranslate(bmp.getWidth(), 0);
            }
            m.postScale(width / bmp.getWidth(), height / bmp.getHeight());
            m.postTranslate(drawX, y);
            canvas.drawBitmap(bmp, m, null);
        }

        // Attack hitbox debug (optional - remove in release)
        if (hitboxActive) {
            Paint dbg = new Paint();
            dbg.setColor(0x44FF0000);
            RectF hb = getAttackHitbox();
            hb.offset(-camX, 0);
            canvas.drawRect(hb, dbg);
        }
    }

    private String getSpriteKey() {
        switch (state) {
            case IDLE:    return "hero_idle_"   + animFrame;
            case RUN:     return "hero_run_"    + animFrame;
            case JUMP:    return "hero_jump_0";
            case FALL:    return "hero_jump_1";
            case ATTACK:  return "hero_attack_" + Math.min(animFrame, 3);
            case SKILL:   return "hero_skill_"  + Math.min(animFrame, 4);
            case HURT:    return "hero_hurt";
            case DIE:     return "hero_die";
            default:      return "hero_idle_0";
        }
    }

    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public RectF getAttackHitbox() {
        float hw = 55f;
        float hh = 40f;
        float hx = facingRight ? x + width : x - hw;
        float hy = y + height * 0.2f;
        return new RectF(hx, hy, hx + hw, hy + hh);
    }

    public boolean isDead()            { return state == State.DIE; }
    public State   getState()          { return state; }
    public int     getSkillCooldown()  { return skillCooldown; }
    public int     getMp()             { return mp; }
    public int     getMaxMp()          { return maxMp; }
    public boolean isHurtState()       { return state == State.HURT; }
}
