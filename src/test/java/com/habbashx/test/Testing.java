package com.habbashx.test;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.habbashx.test.KeyRegistry.KEYS;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.KeyEvent.*;

public class Testing {

    void main(String[] args) {

        try {
            Robot robot = new Robot();

            robot.mouseMove(894, 1056);
            robot.mousePress(BUTTON1_DOWN_MASK);
            robot.mouseRelease(BUTTON1_DOWN_MASK);
            Thread.sleep(1000);

            robot.mouseMove(781, 451);
            robot.mousePress(BUTTON1_DOWN_MASK);
            robot.mouseRelease(BUTTON1_DOWN_MASK);
            robot.keyPress(KEYS.get("j"));
            Thread.sleep(100);
            robot.keyPress(KEYS.get("a"));
            Thread.sleep(100);

            robot.keyPress(KEYS.get("v"));
            Thread.sleep(100);

            robot.keyPress(KEYS.get("a"));
            Thread.sleep(100);

            robot.keyPress(VK_ENTER);
            Thread.sleep(100);

            robot.mouseMove(1400, 367);
            Thread.sleep(100);
            robot.mouseMove(1514, 367);
            Thread.sleep(100);
            robot.mousePress(BUTTON1_DOWN_MASK);
            robot.mouseRelease(BUTTON1_DOWN_MASK);


        } catch (AWTException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class KeyRegistry {

    public static final Map<String, Integer> KEYS = new HashMap<>();

    static {
        KEYS.put("a", VK_A);
        KEYS.put("b", VK_B);
        KEYS.put("c", VK_C);
        KEYS.put("d", VK_D);
        KEYS.put("e", VK_E);
        KEYS.put("f", VK_F);
        KEYS.put("g", VK_G);
        KEYS.put("h", VK_H);
        KEYS.put("i", VK_I);
        KEYS.put("j", VK_J);
        KEYS.put("k", VK_K);
        KEYS.put("l", VK_L);
        KEYS.put("m", VK_M);
        KEYS.put("n", VK_N);
        KEYS.put("o", VK_O);
        KEYS.put("p", VK_B);
        KEYS.put("q", VK_Q);
        KEYS.put("r", VK_R);
        KEYS.put("s", VK_S);
        KEYS.put("t", VK_T);
        KEYS.put("u", VK_U);
        KEYS.put("v", VK_V);
        KEYS.put("w", VK_W);
        KEYS.put("x", VK_X);
        KEYS.put("y", VK_Y);
        KEYS.put("z", VK_Z);
        KEYS.put("enter", VK_ENTER);
    }
}
