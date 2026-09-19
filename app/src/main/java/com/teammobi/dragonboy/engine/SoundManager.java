package com.teammobi.dragonboy.engine;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private SoundPool soundPool;
    private Map<String, Integer> soundIds = new HashMap<>();
    private Context context;
    private boolean enabled = true;

    public SoundManager(Context context) {
        this.context = context;
        AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

        soundPool = new SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build();
    }

    /**
     * Load sound từ assets/sounds/
     * Thêm file .ogg hoặc .wav vào assets/sounds/ rồi gọi hàm này
     */
    public void loadSound(String key, String assetPath) {
        try {
            android.content.res.AssetFileDescriptor afd = context.getAssets().openFd(assetPath);
            int id = soundPool.load(afd, 1);
            soundIds.put(key, id);
        } catch (Exception e) {
            // File âm thanh chưa có - bỏ qua
        }
    }

    public void play(String key) {
        if (!enabled) return;
        Integer id = soundIds.get(key);
        if (id != null) {
            soundPool.play(id, 1f, 1f, 1, 0, 1f);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void release() {
        soundPool.release();
    }
}
