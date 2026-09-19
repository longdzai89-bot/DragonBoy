package com.teammobi.dragonboy.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * AssetManager - Load ảnh thật từ assets/sprites/
 * Sprite sheet nằm ngang: các frame xếp từ trái sang phải.
 * Nếu không có file ảnh → tự vẽ placeholder bằng Canvas Android.
 */
public class AssetManager {

    private static final String TAG = "AssetManager";
    private final Context context;
    private final Map<String, Bitmap> bitmaps = new HashMap<>();

    public static final int HERO_W = 48;
    public static final int HERO_H = 48;

    public AssetManager(Context context) {
        this.context = context;
    }

    public void loadAll() {
        loadPlaceholders();   // Luôn load placeholder trước
        loadRealSprites();    // Override bằng ảnh thật nếu có
    }

    // ─── LOAD ẢNH THẬT TỪ assets/sprites/ ───────────────────────────────────
    private void loadRealSprites() {
        // Hero
        loadSheet("sprites/hero_idle.png",    4, HERO_W, HERO_H, "hero_idle_");
        loadSheet("sprites/hero_run.png",     6, HERO_W, HERO_H, "hero_run_");
        loadSheet("sprites/hero_jump.png",    2, HERO_W, HERO_H, "hero_jump_");
        loadSheet("sprites/hero_attack.png",  4, HERO_W, HERO_H, "hero_attack_");
        loadSheet("sprites/hero_skill.png",   5, HERO_W, HERO_H, "hero_skill_");
        loadSingle("sprites/hero_hurt.png", "hero_hurt");
        loadSingle("sprites/hero_die.png",  "hero_die");

        // Enemies
        loadSheet("sprites/slime.png",     4, 32, 32, "slime_");
        loadSheet("sprites/skeleton.png",  4, 40, 56, "skel_");
        loadSheet("sprites/boss.png",      6, 96, 96, "boss_");

        // Items
        loadSingle("sprites/coin.png",     "coin");
        loadSingle("sprites/hp_orb.png",   "hp_orb");
        loadSingle("sprites/gem.png",      "gem");
        loadSingle("sprites/fireball.png", "fireball");

        // Tiles
        loadSingle("sprites/tile_ground.png", "tile_ground");
        loadSingle("sprites/tile_grass.png",  "tile_grass");
        loadSingle("sprites/tile_brick.png",  "tile_brick");
        loadSingle("sprites/tile_spike.png",  "tile_spike");
    }

    /**
     * Load sprite sheet nằm ngang → tách thành các frame riêng.
     * @param assetPath  đường dẫn trong assets/
     * @param frames     số frame
     * @param fw         chiều rộng 1 frame (px)
     * @param fh         chiều cao 1 frame (px)
     * @param prefix     prefix key: "hero_idle_" → hero_idle_0, hero_idle_1, ...
     */
    private void loadSheet(String assetPath, int frames, int fw, int fh, String prefix) {
        try {
            InputStream is = context.getAssets().open(assetPath);
            Bitmap sheet = BitmapFactory.decodeStream(is);
            is.close();
            if (sheet == null) return;


            int actualFW = sheet.getWidth() / frames;

            for (int i = 0; i < frames; i++) {
                int srcX = i * actualFW;
                if (srcX + actualFW > sheet.getWidth()) break;
                Bitmap frame = Bitmap.createBitmap(sheet, srcX, 0, actualFW, sheet.getHeight());
                // Scale nếu cần
                if (frame.getWidth() != fw || frame.getHeight() != fh) {
                    frame = Bitmap.createScaledBitmap(frame, fw, fh, true);
                }
                put(prefix + i, frame);
            }
            Log.d(TAG, "Loaded sheet: " + assetPath + " (" + frames + " frames)");
        } catch (Exception e) {
            Log.d(TAG, "No asset: " + assetPath + " (using placeholder)");
        }
    }

    private void loadSingle(String assetPath, String key) {
        try {
            InputStream is = context.getAssets().open(assetPath);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            if (bmp != null) {
                put(key, bmp);
                Log.d(TAG, "Loaded: " + assetPath);
            }
        } catch (Exception e) {
            Log.d(TAG, "No asset: " + assetPath + " (using placeholder)");
        }
    }

    // ─── PLACEHOLDER (Canvas Android) ────────────────────────────────────────
    private void loadPlaceholders() {
        // Hero idle
        for (int i = 0; i < 4; i++)
            put("hero_idle_" + i,   makeHero(0xFF2288FF, "IDLE", i));
        // Hero run
        for (int i = 0; i < 6; i++)
            put("hero_run_" + i,    makeHero(0xFF22CC44, "RUN", i));
        // Hero jump/fall
        put("hero_jump_0",          makeHero(0xFFFFDD00, "JUMP", 0));
        put("hero_jump_1",          makeHero(0xFFFFAA00, "FALL", 1));
        // Hero attack
        for (int i = 0; i < 4; i++)
            put("hero_attack_" + i, makeHero(0xFFFF4422, "ATK", i));
        // Hero skill
        for (int i = 0; i < 5; i++)
            put("hero_skill_" + i,  makeHero(0xFFFF00FF, "SKL", i));
        // Hero states
        put("hero_hurt",            makeHero(0xFFFFFFFF, "HRT", 0));
        put("hero_die",             makeHero(0xFF888888, "DIE", 0));

        // Enemies
        for (int i = 0; i < 4; i++) put("slime_" + i, makeEnemy(32, 32, 0xFF44BB44, i));
        for (int i = 0; i < 4; i++) put("skel_"  + i, makeEnemy(40, 56, 0xFFCCCCCC, i));
        for (int i = 0; i < 6; i++) put("boss_"  + i, makeEnemy(96, 96, 0xFFCC2200, i));

        // Projectile
        put("fireball", makeFireball());

        // Tiles
        put("tile_ground", makeTile(0xFF5D4037, 0xFF795548));
        put("tile_grass",  makeTile(0xFF388E3C, 0xFF4CAF50));
        put("tile_brick",  makeTile(0xFF8D6E63, 0xFFA1887F));
        put("tile_spike",  makeSpike());

        // Items
        put("coin",   makeCoin());
        put("hp_orb", makeOrb(0xFFFF3333));
        put("gem",    makeGem());

        // Buttons
        put("btn_jump",   makeBtn(0xBB0055DD, "↑"));
        put("btn_attack", makeBtn(0xBBCC2200, "A"));
        put("btn_skill",  makeBtn(0xBB880099, "S"));
    }

    // ─── PLACEHOLDER DRAWERS ─────────────────────────────────────────────────
    private Bitmap makeHero(int color, String label, int frame) {
        int w = HERO_W, h = HERO_H;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Body
        p.setColor(color);
        c.drawRoundRect(4, h*.3f, w-4, h*.95f, 6, 6, p);
        // Head
        p.setColor(0xFFFFDDB3);
        c.drawCircle(w/2f, h*.22f, h*.18f, p);
        // Eyes
        p.setColor(0xFF333333);
        c.drawCircle(w*.38f, h*.2f, 2.5f, p);
        c.drawCircle(w*.62f, h*.2f, 2.5f, p);
        // Wings
        p.setColor(0xAAFF6600);
        c.drawOval(0, h*.35f, w*.25f, h*.55f, p);
        c.drawOval(w*.75f, h*.35f, w, h*.55f, p);
        // Legs
        p.setColor(Color.rgb(80, 50, 20));
        float off = (frame % 2 == 0) ? 3 : -3;
        if (label.equals("RUN")) {
            c.drawRoundRect(4, h*.82f + off, w/2f-2, h, 4, 4, p);
            c.drawRoundRect(w/2f+2, h*.82f - off, w-4, h, 4, 4, p);
        } else {
            c.drawRoundRect(4, h*.82f, w/2f-2, h, 4, 4, p);
            c.drawRoundRect(w/2f+2, h*.82f, w-4, h, 4, 4, p);
        }
        // Attack arc
        if (label.equals("ATK")) {
            p.setColor(0x88FFFF00);
            c.drawOval(w*.6f, h*.3f, w*1.3f, h*.7f, p);
        }
        return b;
    }

    private Bitmap makeEnemy(int w, int h, int color, int frame) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawRoundRect(2, 2, w-2, h-2, 8, 8, p);
        p.setColor(Color.WHITE);
        c.drawCircle(w*.35f, h*.4f, 4, p);
        c.drawCircle(w*.65f, h*.4f, 4, p);
        p.setColor(Color.BLACK);
        float ex = (frame%2==0)?1:-1;
        c.drawCircle(w*.35f+ex, h*.4f+1, 2, p);
        c.drawCircle(w*.65f+ex, h*.4f+1, 2, p);
        return b;
    }

    private Bitmap makeFireball() {
        Bitmap b = Bitmap.createBitmap(32, 20, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xAAFF6600); c.drawOval(0,0,32,20,p);
        p.setColor(0xFFFFDD00); c.drawOval(4,4,24,16,p);
        p.setColor(0xFFFFFFAA); c.drawOval(8,6,16,14,p);
        return b;
    }

    private Bitmap makeTile(int dark, int light) {
        Bitmap b = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint();
        p.setColor(light); c.drawRect(0,0,32,32,p);
        p.setColor(0x33FFFFFF); c.drawRect(0,0,32,4,p);
        p.setColor(0x55000000); c.drawRect(0,28,32,32,p);
        p.setColor(0x33000000);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1);
        c.drawRect(1,1,31,31,p);
        return b;
    }

    private Bitmap makeSpike() {
        Bitmap b = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFF888888); c.drawRect(0,19,32,32,p);
        p.setColor(0xFFAAAAAA);
        android.graphics.Path path = new android.graphics.Path();
        for (int i=0;i<4;i++){
            float bx=i*8f;
            path.moveTo(bx,19); path.lineTo(bx+4,0); path.lineTo(bx+8,19); path.close();
        }
        c.drawPath(path,p);
        return b;
    }

    private Bitmap makeCoin() {
        Bitmap b = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFFFCC00); c.drawCircle(10,10,9,p);
        p.setColor(0x66FFFFFF); c.drawCircle(7,7,4,p);
        p.setColor(0xFFAA8800); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.5f);
        c.drawCircle(10,10,8.5f,p);
        return b;
    }

    private Bitmap makeOrb(int color) {
        Bitmap b = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color); c.drawCircle(10,10,9,p);
        p.setColor(0x66FFFFFF); c.drawCircle(7,6,4,p);
        return b;
    }

    private Bitmap makeGem() {
        Bitmap b = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(10,1); path.lineTo(18,8); path.lineTo(10,19); path.lineTo(2,8); path.close();
        p.setColor(0xFF00DDFF); c.drawPath(path, p);
        p.setColor(0x55FFFFFF); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.5f);
        c.drawLine(10,1,10,19,p); c.drawLine(2,8,18,8,p);
        return b;
    }

    private Bitmap makeBtn(int color, String label) {
        Bitmap b = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color); c.drawCircle(40,40,38,p);
        p.setColor(0x55FFFFFF); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3);
        c.drawCircle(40,40,36,p); p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE); p.setTextSize(28); p.setTextAlign(Paint.Align.CENTER);
        c.drawText(label,40,50,p);
        return b;
    }

    // ─── API ─────────────────────────────────────────────────────────────────
    public synchronized void put(String key, Bitmap bmp) {
        if (bmp != null) bitmaps.put(key, bmp);
    }

    public synchronized Bitmap get(String key) {
        return bitmaps.get(key);
    }
}
