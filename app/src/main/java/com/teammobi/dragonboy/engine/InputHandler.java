package com.teammobi.dragonboy.engine;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

public class InputHandler {

    // Virtual buttons
    public static final int BTN_LEFT    = 0;
    public static final int BTN_RIGHT   = 1;
    public static final int BTN_JUMP    = 2;
    public static final int BTN_ATTACK  = 3;
    public static final int BTN_SKILL   = 4;

    private boolean[] held    = new boolean[8];
    private boolean[] pressed = new boolean[8];

    // Touch zones (set by GameScreen)
    private RectF[] btnZones = new RectF[8];

    // Joystick
    private boolean joystickActive = false;
    private PointF joystickCenter  = new PointF();
    private PointF joystickCurrent = new PointF();
    private float joystickRadius   = 80f;
    private int joystickPointer    = -1;

    // Multi-touch tracking
    private Map<Integer, Integer> pointerToBtn = new HashMap<>();

    public void setButtonZone(int btn, RectF zone) {
        btnZones[btn] = zone;
    }

    public void setJoystickArea(PointF center, float radius) {
        joystickCenter.set(center);
        joystickRadius = radius;
    }

    public void handleTouch(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Check joystick area (left half of screen)
                if (x < 400 && !joystickActive) {
                    joystickActive = true;
                    joystickPointer = pointerId;
                    joystickCenter.set(x, y);
                    joystickCurrent.set(x, y);
                    updateJoystickButtons();
                } else {
                    // Check right buttons
                    checkButtonDown(pointerId, x, y);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pid = event.getPointerId(i);
                    float px = event.getX(i);
                    float py = event.getY(i);

                    if (pid == joystickPointer) {
                        joystickCurrent.set(px, py);
                        updateJoystickButtons();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId == joystickPointer) {
                    joystickActive = false;
                    joystickPointer = -1;
                    held[BTN_LEFT]  = false;
                    held[BTN_RIGHT] = false;
                } else {
                    Integer btn = pointerToBtn.remove(pointerId);
                    if (btn != null) {
                        held[btn] = false;
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                clearAll();
                break;
        }
    }

    private void updateJoystickButtons() {
        float dx = joystickCurrent.x - joystickCenter.x;
        float threshold = joystickRadius * 0.3f;

        held[BTN_LEFT]  = dx < -threshold;
        held[BTN_RIGHT] = dx >  threshold;
    }

    private void checkButtonDown(int pointerId, float x, float y) {
        for (int i = BTN_JUMP; i <= BTN_SKILL; i++) {
            if (btnZones[i] != null && btnZones[i].contains(x, y)) {
                held[i]    = true;
                pressed[i] = true;
                pointerToBtn.put(pointerId, i);
                return;
            }
        }
    }

    public boolean isHeld(int btn)    { return held[btn]; }
    public boolean isPressed(int btn) { return pressed[btn]; }

    public void clearPressed() {
        for (int i = 0; i < pressed.length; i++) {
            pressed[i] = false;
        }
    }

    public void clearAll() {
        for (int i = 0; i < held.length; i++) {
            held[i] = false;
            pressed[i] = false;
        }
        joystickActive = false;
        joystickPointer = -1;
        pointerToBtn.clear();
    }

    public PointF getJoystickCenter()  { return joystickCenter; }
    public PointF getJoystickCurrent() { return joystickCurrent; }
    public boolean isJoystickActive()  { return joystickActive; }
    public float getJoystickRadius()   { return joystickRadius; }
}
