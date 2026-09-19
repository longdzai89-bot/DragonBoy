package com.teammobi.dragonboy.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.teammobi.dragonboy.engine.AssetManager;

public class Platform {

    public enum Type { GROUND, BRICK, SPIKE }

    private float x, y, width, height;
    private Type  type;
    private AssetManager assets;
    private String tileKey;
    private boolean deadly;

    public Platform(float x, float y, float width, float height, Type type, AssetManager assets) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.type   = type;
        this.assets = assets;

        switch (type) {
            case GROUND: tileKey = "tile_ground"; deadly = false; break;
            case BRICK:  tileKey = "tile_brick";  deadly = false; break;
            case SPIKE:  tileKey = "tile_spike";  deadly = true;  break;
        }
    }

    public void render(Canvas canvas, float camX) {
        Bitmap tile = assets.get(tileKey);
        if (tile == null) return;

        int tileW = tile.getWidth();
        int tileH = tile.getHeight();
        float startX = x - camX;

        for (float tx = startX; tx < startX + width; tx += tileW) {
            for (float ty = y; ty < y + height; ty += tileH) {
                canvas.drawBitmap(tile, tx, ty, null);
            }
        }
    }

    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public boolean isDeadly() { return deadly; }
    public Type    getType()  { return type; }
    public float   getX()     { return x; }
    public float   getY()     { return y; }
    public float   getW()     { return width; }
    public float   getH()     { return height; }
}
