package com.teammobi.dragonboy.utils;

import com.teammobi.dragonboy.engine.AssetManager;
import com.teammobi.dragonboy.entities.Enemy;
import com.teammobi.dragonboy.entities.Item;
import com.teammobi.dragonboy.entities.Platform;

import java.util.ArrayList;
import java.util.List;

public class LevelBuilder {

    public static class LevelData {
        public List<Platform> platforms = new ArrayList<>();
        public List<Enemy>    enemies   = new ArrayList<>();
        public List<Item>     items     = new ArrayList<>();
        public float heroStartX, heroStartY;
        public float levelWidth;
        public float goalX, goalY;    // Điểm đích
        public int   level;
    }

    public static LevelData build(int level, int screenW, int screenH, AssetManager assets) {
        switch (level) {
            case 1:  return buildLevel1(screenW, screenH, assets);
            case 2:  return buildLevel2(screenW, screenH, assets);
            case 3:  return buildLevel3(screenW, screenH, assets);
            default: return buildLevel1(screenW, screenH, assets);
        }
    }

    // ─── Level 1: Tutorial - đồng bằng đơn giản ──────────────────────────────
    private static LevelData buildLevel1(int sw, int sh, AssetManager assets) {
        LevelData d = new LevelData();
        d.level       = 1;
        d.heroStartX  = 80;
        d.heroStartY  = sh - 200f;
        d.levelWidth  = sw * 5f;

        float ground = sh - 100f;
        float gh = 100f;

        // Ground tiles
        d.platforms.add(new Platform(0,       ground, 600,  gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(700,     ground, 400,  gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(1200,    ground, 500,  gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(1800,    ground, 700,  gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(2600,    ground, 400,  gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(3100,    ground, 600,  gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(3800,    ground, d.levelWidth, gh, Platform.Type.GROUND, assets));

        // Floating platforms
        float ph = ground - 140f;
        d.platforms.add(new Platform(300,  ph,       128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(550,  ph - 80,  128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(900,  ph,       128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(1300, ph - 60,  128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(1700, ph,       200, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(2100, ph - 80,  128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(2800, ph,       160, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(3300, ph - 60,  128, 24, Platform.Type.BRICK, assets));

        // Spikes
        d.platforms.add(new Platform(640, ground, 60, 24, Platform.Type.SPIKE, assets));
        d.platforms.add(new Platform(1110, ground, 90, 24, Platform.Type.SPIKE, assets));

        // Enemies - Slimes
        addEnemy(d, 500,  ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 900,  ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 1400, ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 1900, ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 2200, ground - 32, Enemy.Type.SKELETON, assets);
        addEnemy(d, 2900, ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 3500, ground - 32, Enemy.Type.SKELETON, assets);

        // Items
        addItems(d, assets, ground, ph);

        // Goal (end of level)
        d.goalX = d.levelWidth - 100;
        d.goalY = ground - 80;

        return d;
    }

    // ─── Level 2: Forest - nhiều hơn, khó hơn ────────────────────────────────
    private static LevelData buildLevel2(int sw, int sh, AssetManager assets) {
        LevelData d = new LevelData();
        d.level       = 2;
        d.heroStartX  = 80;
        d.heroStartY  = sh - 200f;
        d.levelWidth  = sw * 6f;

        float ground = sh - 100f;
        float gh     = 100f;

        d.platforms.add(new Platform(0,    ground, 400, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(500,  ground, 300, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(900,  ground, 500, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(1500, ground, 400, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(2000, ground, 600, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(2700, ground, 500, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(3300, ground, 700, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(4100, ground, 800, gh, Platform.Type.GROUND, assets));
        d.platforms.add(new Platform(5000, ground, d.levelWidth, gh, Platform.Type.GROUND, assets));

        float ph = ground - 150f;
        d.platforms.add(new Platform(200,  ph,       128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(600,  ph - 100, 128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(1100, ph,       160, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(1700, ph - 80,  128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(2200, ph,       200, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(2900, ph - 120, 128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(3400, ph,       160, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(3900, ph - 80,  128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(4500, ph,       200, 24, Platform.Type.BRICK, assets));

        // More spikes
        d.platforms.add(new Platform(400,  ground, 100, 24, Platform.Type.SPIKE, assets));
        d.platforms.add(new Platform(800,  ground, 100, 24, Platform.Type.SPIKE, assets));
        d.platforms.add(new Platform(1400, ground, 100, 24, Platform.Type.SPIKE, assets));
        d.platforms.add(new Platform(2600, ground, 100, 24, Platform.Type.SPIKE, assets));
        d.platforms.add(new Platform(3200, ground, 100, 24, Platform.Type.SPIKE, assets));

        // More enemies
        addEnemy(d, 400,  ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 700,  ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 1000, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 1300, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 1700, ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 2100, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 2400, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 2800, ground - 32, Enemy.Type.SLIME,    assets);
        addEnemy(d, 3200, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 3700, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 4200, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 4700, ground - 56, Enemy.Type.SKELETON, assets);

        addItems(d, assets, ground, ph);

        d.goalX = d.levelWidth - 100;
        d.goalY = ground - 80;
        return d;
    }

    // ─── Level 3: Boss Level ──────────────────────────────────────────────────
    private static LevelData buildLevel3(int sw, int sh, AssetManager assets) {
        LevelData d = new LevelData();
        d.level      = 3;
        d.heroStartX = 80;
        d.heroStartY = sh - 200f;
        d.levelWidth = sw * 3f;

        float ground = sh - 100f;
        float gh     = 100f;

        d.platforms.add(new Platform(0, ground, d.levelWidth + 200, gh, Platform.Type.GROUND, assets));

        float ph = ground - 150f;
        d.platforms.add(new Platform(200,  ph, 128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(500,  ph - 100, 128, 24, Platform.Type.BRICK, assets));
        d.platforms.add(new Platform(sw - 64, ph, 128, 24, Platform.Type.BRICK, assets));

        // Guards before boss
        addEnemy(d, 300, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 600, ground - 56, Enemy.Type.SKELETON, assets);
        addEnemy(d, 900, ground - 56, Enemy.Type.SKELETON, assets);

        // BOSS at end
        addEnemy(d, sw * 2f, ground - 96, Enemy.Type.BOSS, assets);

        // Items
        addItems(d, assets, ground, ph);

        d.goalX = d.levelWidth - 100;
        d.goalY = ground - 80;
        return d;
    }

    private static void addEnemy(LevelData d, float x, float y, Enemy.Type type, AssetManager assets) {
        d.enemies.add(new Enemy(x, y, type, assets));
    }

    private static void addItems(LevelData d, AssetManager assets, float ground, float ph) {
        // Coins scattered across level
        float[] coinX = {200, 400, 700, 1000, 1400, 1800, 2200, 2600, 3000, 3500, 4000};
        for (float cx : coinX) {
            if (cx < d.levelWidth) {
                d.items.add(new Item(cx,      ground - 40, Item.Type.COIN,   assets));
                d.items.add(new Item(cx + 30, ground - 40, Item.Type.COIN,   assets));
            }
        }
        // HP orbs
        d.items.add(new Item(800,  ground - 40, Item.Type.HP_ORB, assets));
        d.items.add(new Item(2000, ground - 40, Item.Type.HP_ORB, assets));
        d.items.add(new Item(3200, ground - 40, Item.Type.HP_ORB, assets));
        // Gems
        d.items.add(new Item(1200, ph - 30, Item.Type.GEM, assets));
        d.items.add(new Item(2500, ph - 30, Item.Type.GEM, assets));
    }
}
