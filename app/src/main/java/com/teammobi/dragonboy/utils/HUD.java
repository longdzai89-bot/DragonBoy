package com.teammobi.dragonboy.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.teammobi.dragonboy.engine.InputHandler;
import com.teammobi.dragonboy.entities.Hero;

public class HUD {

    private int W, H;
    private InputHandler input;

    private Paint barBgPaint, hpPaint, mpPaint, textPaint, joyPaint,
                  btnPaint, btnTextPaint, overlayPaint, skillPaint;

    // Button zones
    private RectF zoneJump, zoneAttack, zoneSkill;

    // Joystick visual
    private float joyRadius   = 70f;
    private float joyStickRad = 30f;
    private PointF joyCenter;

    public HUD(int W, int H, InputHandler input) {
        this.W     = W;
        this.H     = H;
        this.input = input;
        initPaints();
        setupZones();
    }

    private void initPaints() {
        barBgPaint = new Paint();
        barBgPaint.setColor(0xAA000000);

        hpPaint = new Paint();
        hpPaint.setColor(Color.RED);

        mpPaint = new Paint();
        mpPaint.setColor(0xFF4488FF);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(H * 0.025f);
        textPaint.setFakeBoldText(true);

        joyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        btnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnTextPaint.setColor(Color.WHITE);
        btnTextPaint.setTextSize(H * 0.022f);
        btnTextPaint.setTextAlign(Paint.Align.CENTER);
        btnTextPaint.setFakeBoldText(true);

        overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        skillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skillPaint.setColor(0xAA000000);
    }

    private void setupZones() {
        float margin = 20;
        float btnSize = H * 0.14f;

        // Right side buttons
        float rightX = W - btnSize - margin;
        float midX   = W - btnSize * 2 - margin * 2;

        // Attack (A) - right bottom
        zoneAttack = new RectF(rightX, H - btnSize - margin,
                               rightX + btnSize, H - margin);
        // Jump (B) - middle right
        zoneJump   = new RectF(midX, H - btnSize - margin,
                               midX + btnSize, H - margin);
        // Skill (S) - above attack
        zoneSkill  = new RectF(rightX, H - btnSize * 2 - margin * 2,
                               rightX + btnSize, H - btnSize - margin * 2);

        // Register zones with input handler
        input.setButtonZone(InputHandler.BTN_JUMP,   zoneJump);
        input.setButtonZone(InputHandler.BTN_ATTACK, zoneAttack);
        input.setButtonZone(InputHandler.BTN_SKILL,  zoneSkill);

        // Joystick center (bottom-left)
        joyCenter = new PointF(joyRadius + margin * 2, H - joyRadius - margin * 2);
        input.setJoystickArea(joyCenter, joyRadius);
    }

    public void render(Canvas canvas, Hero hero, int level, int coins) {
        renderHpBar(canvas, hero);
        renderMpBar(canvas, hero);
        renderStats(canvas, hero, level, coins);
        renderJoystick(canvas);
        renderButtons(canvas, hero);
    }

    private void renderHpBar(Canvas canvas, Hero hero) {
        float barX = W * 0.04f;
        float barY = H * 0.04f;
        float barW = W * 0.38f;
        float barH = H * 0.035f;

        // Background
        canvas.drawRoundRect(new RectF(barX, barY, barX + barW, barY + barH), 8, 8, barBgPaint);
        // HP fill
        float ratio = (float) hero.hp / hero.maxHp;
        hpPaint.setColor(ratio > 0.5f ? Color.RED
                       : ratio > 0.25f ? Color.rgb(255, 140, 0)
                       : Color.rgb(255, 50, 50));
        canvas.drawRoundRect(
            new RectF(barX, barY, barX + barW * ratio, barY + barH), 8, 8, hpPaint);
        // Label
        canvas.drawText("HP  " + hero.hp + "/" + hero.maxHp,
            barX + 6, barY + barH - 4, textPaint);
    }

    private void renderMpBar(Canvas canvas, Hero hero) {
        float barX = W * 0.04f;
        float barY = H * 0.09f;
        float barW = W * 0.25f;
        float barH = H * 0.025f;

        canvas.drawRoundRect(new RectF(barX, barY, barX + barW, barY + barH), 6, 6, barBgPaint);
        float ratio = (float) hero.getMp() / hero.getMaxMp();
        canvas.drawRoundRect(
            new RectF(barX, barY, barX + barW * ratio, barY + barH), 6, 6, mpPaint);

        Paint mpTxt = new Paint(textPaint);
        mpTxt.setTextSize(H * 0.019f);
        canvas.drawText("MP  " + hero.getMp() + "/" + hero.getMaxMp(),
            barX + 6, barY + barH - 2, mpTxt);
    }

    private void renderStats(Canvas canvas, Hero hero, int level, int coins) {
        Paint stat = new Paint(textPaint);
        stat.setTextSize(H * 0.023f);
        stat.setColor(Color.YELLOW);
        canvas.drawText("Lv." + level + "  💰" + coins,
            W * 0.04f, H * 0.16f, stat);
    }

    private void renderJoystick(Canvas canvas) {
        PointF center = input.isJoystickActive()
                      ? input.getJoystickCenter()
                      : joyCenter;

        // Outer ring
        joyPaint.setColor(0x55FFFFFF);
        joyPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center.x, center.y, joyRadius, joyPaint);
        joyPaint.setColor(0xAAFFFFFF);
        joyPaint.setStyle(Paint.Style.STROKE);
        joyPaint.setStrokeWidth(3);
        canvas.drawCircle(center.x, center.y, joyRadius, joyPaint);
        joyPaint.setStyle(Paint.Style.FILL);

        // Inner stick
        if (input.isJoystickActive()) {
            PointF cur = input.getJoystickCurrent();
            float dx = cur.x - center.x;
            float dy = cur.y - center.y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist > joyRadius) {
                float scale = joyRadius / dist;
                dx *= scale; dy *= scale;
            }
            joyPaint.setColor(0xDDFFFFFF);
            canvas.drawCircle(center.x + dx, center.y + dy, joyStickRad, joyPaint);
        } else {
            joyPaint.setColor(0xAAFFFFFF);
            canvas.drawCircle(center.x, center.y, joyStickRad, joyPaint);
        }
    }

    private void renderButtons(Canvas canvas, Hero hero) {
        drawCircleBtn(canvas, zoneJump,   "B",  0xAA0055CC, input.isHeld(InputHandler.BTN_JUMP));
        drawCircleBtn(canvas, zoneAttack, "A",  0xAACC2200, input.isHeld(InputHandler.BTN_ATTACK));

        // Skill button with cooldown overlay
        int cd = hero.getSkillCooldown();
        drawCircleBtn(canvas, zoneSkill, "S", 0xAA880099, input.isHeld(InputHandler.BTN_SKILL));
        if (cd > 0) {
            // Darken + show timer
            float sweep = 360f * cd / 180f;
            skillPaint.setColor(0xAA000000);
            skillPaint.setStyle(Paint.Style.FILL);
            // Simple arc overlay
            canvas.drawArc(
                new android.graphics.RectF(
                    zoneSkill.left + 4, zoneSkill.top + 4,
                    zoneSkill.right - 4, zoneSkill.bottom - 4),
                -90, sweep, true, skillPaint);
            skillPaint.setStyle(Paint.Style.FILL);
            Paint cdTxt = new Paint(btnTextPaint);
            cdTxt.setTextSize(H * 0.018f);
            cdTxt.setColor(Color.WHITE);
            canvas.drawText((cd / 60 + 1) + "s",
                zoneSkill.centerX(), zoneSkill.centerY() + 6, cdTxt);
        }
    }

    private void drawCircleBtn(Canvas canvas, RectF zone, String label, int color, boolean pressed) {
        float cx = zone.centerX();
        float cy = zone.centerY();
        float r  = Math.min(zone.width(), zone.height()) / 2f;

        btnPaint.setColor(pressed ? (color | 0xFF000000) : color);
        canvas.drawCircle(cx, cy, r, btnPaint);

        btnPaint.setColor(0xAAFFFFFF);
        btnPaint.setStyle(Paint.Style.STROKE);
        btnPaint.setStrokeWidth(3);
        canvas.drawCircle(cx, cy, r, btnPaint);
        btnPaint.setStyle(Paint.Style.FILL);

        canvas.drawText(label, cx, cy + btnTextPaint.getTextSize() * 0.35f, btnTextPaint);
    }

    public void renderGameOver(Canvas canvas, int coins) {
        overlayPaint.setColor(0xCC000000);
        canvas.drawRect(0, 0, W, H, overlayPaint);

        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        title.setColor(Color.RED);
        title.setTextSize(H * 0.1f);
        title.setFakeBoldText(true);
        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME OVER", W / 2f, H * 0.4f, title);

        Paint sub = new Paint(Paint.ANTI_ALIAS_FLAG);
        sub.setColor(Color.WHITE);
        sub.setTextSize(H * 0.04f);
        sub.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Coins: " + coins, W / 2f, H * 0.55f, sub);
        canvas.drawText("Tap to restart", W / 2f, H * 0.65f, sub);
    }

    public void renderWin(Canvas canvas, int level, int coins) {
        overlayPaint.setColor(0xCC001133);
        canvas.drawRect(0, 0, W, H, overlayPaint);

        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        title.setColor(Color.YELLOW);
        title.setTextSize(H * 0.08f);
        title.setFakeBoldText(true);
        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("LEVEL " + level + " CLEAR!", W / 2f, H * 0.38f, title);

        Paint sub = new Paint(Paint.ANTI_ALIAS_FLAG);
        sub.setColor(Color.WHITE);
        sub.setTextSize(H * 0.038f);
        sub.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Coins: " + coins, W / 2f, H * 0.52f, sub);
        canvas.drawText("Tap for next level", W / 2f, H * 0.62f, sub);
    }
}
