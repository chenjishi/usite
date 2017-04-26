package com.chenjishi.u148.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-15
 * Time: 下午7:39
 * To change this template use File | Settings | File Templates.
 */
public class Rocket {
    public boolean sleep = true;

    private float energy, length, mx, my, gravity, ox, oy, x, y, t;
    private float vx[], vy[];
    private int patch, red, green, blue;
    private Random random;
    private float density;

    public Rocket(int a, int b, int g, float d) {
        mx = a;
        my = b;
        gravity = g;
        density = d;
    }

    public void init(int e, int p, int l, long seed) {
        energy = e;
        patch = p + 20;
        length = l;

        random = new Random(seed);

        vx = new float[patch];
        vy = new float[patch];

        red = (int) (random.nextFloat() * 128) + 128;
        blue = (int) (random.nextFloat() * 128) + 128;
        green = (int) (random.nextFloat() * 128) + 128;

        ox = (random.nextFloat() * mx / 2) + mx / 4;
        oy = (random.nextFloat() * my / 2) + my / 4;

        for (int i = 0; i < patch; ++i) {
            vx[i] = ((random.nextFloat() + random.nextFloat() / 2) * energy) - energy / (random.nextInt(2) + 1);
            vy[i] = ((random.nextFloat() + random.nextFloat() / 2) * energy * 7 / 8) - energy / (random.nextInt(5) + 4);
        }
    }

    public void start() {
        t = 0;
        sleep = false;
    }

    private int getColor() {
//        int cr, cg, cb;

       int _blue = (int) (random.nextFloat() * 255);

//        cr = (int) (random.nextDouble() * 64) - 32 + red;
//        cg = (int) (random.nextDouble() * 64) - 32 + green;
//        cb = (int) (random.nextDouble() * 64) - 32 + blue;
//
//        if (cr >= 0 && cr <= 256)
//            red = cr;
//        if (cg >= 0 && cg <= 256)
//            green = cg;
//        if (cb >= 0 && cb <= 256)
//            blue = cb;
//
//        int _red = red == 256 ? 255 : red;
//        int _green = green == 256 ? 255 : green;
//        int _blue = blue == 256 ? 255 : blue;

        return Color.rgb(255, 255, _blue);
    }

    public void doDraw(Canvas canvas, Paint paint) {
        if (!sleep) {
            if (t < length) {
                int i;
                double s;

                int color = getColor();
                paint.setColor(color);

                for (i = 0; i < patch; ++i) {
                    s = (double) t / 100;
                    x = (int) (vx[i] * s);
                    y = (int) (vy[i] * s - gravity * s * s);

                    float r = random.nextFloat() * density * 2 + 2.0f;
                    canvas.drawCircle(ox + x, oy - y, r, paint);
                }

                color = getColor();
                paint.setColor(color);

                for (i = 0; i < patch; ++i) {
                    if (t >= length / 2) {
                        for (int j = 0; j < 2; ++j) {
                            s = (double) ((t - length / 2) * 2 + j) / 100;
                            x = (int) (vx[i] * s);
                            y = (int) (vy[i] * s - gravity * s * s);

                            float r = random.nextFloat() * density * 2 + 2.0f;
                            canvas.drawCircle(ox + x, oy - y, r, paint);
                        }
                    }
                }

                ++t;
            } else
                sleep = true;
        }
    }
}
