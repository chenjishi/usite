package com.chenjishi.u148.view;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-15
 * Time: 下午7:40
 * To change this template use File | Settings | File Templates.
 */
public class Fireworks {
    /**
     * Maximum number of rockets.
     */
    public int MaxRocketNumber = 9;
    /**
     * Controls "energy" of firwork explosion. Default value 850.
     */
    public int MaxRocketExplosionEnergy = 950;
    /**
     * Controls the density of the firework burst. Larger numbers give higher density.
     * Default value 90.
     */
    public int MaxRocketPatchNumber = 60;
    /**
     * Controls the radius of the firework burst. Larger numbers give larger radius.
     * Default value 68.
     */
    public int MaxRocketPatchLength = 68 * 2;

    /**
     * Controls gravity of the firework simulation.
     * Default value 400.
     */
    public int Gravity = 400;

    transient private Rocket rocket[];
    transient private boolean rocketsCreated = false;

    private int width;
    private int height;
    private float screenDesity;

    Fireworks(int width, int height, float screenDesity) {
        this.width = width;
        this.height = height;
        this.screenDesity = screenDesity;
    }

    void createRockets() {
        rocketsCreated = true;

        Rocket tempRocket[] = new Rocket[MaxRocketNumber];

        for (int i = 0; i < MaxRocketNumber; i++)
            tempRocket[i] = new Rocket(width, height, Gravity, screenDesity);

        rocket = tempRocket;
    }

    public synchronized void reshape(int width, int height) {
        this.width = width;
        this.height = height;

        rocketsCreated = false;
    }

    public void doDraw(Canvas canvas, Paint paint) {
        int i, e, p, l;
        long s;

        boolean sleep;

        if (!rocketsCreated) {
            createRockets();
        }

        if (rocketsCreated) {
            sleep = true;

            for (i = 0; i < MaxRocketNumber; i++)
                sleep = sleep && rocket[i].sleep;

            for (i = 0; i < MaxRocketNumber; ++i) {
                e = (int) (Math.random() * MaxRocketExplosionEnergy * 3 / 4) + MaxRocketExplosionEnergy / 4 + 1;
                p = (int) (Math.random() * MaxRocketPatchNumber * 3 / 4) + MaxRocketPatchNumber / 4 + 1;
                l = (int) (Math.random() * MaxRocketPatchLength * 3 / 4) + MaxRocketPatchLength / 4 + 1;
                s = (long) (Math.random() * 10000);

                Rocket r = rocket[i];
                if (r.sleep && Math.random() * MaxRocketNumber * l < 2) {
                    r.init(e, p, l, s);
                    r.start();
                }

                if (rocketsCreated)
                    r.doDraw(canvas, paint);
            }
        }
    }
}
