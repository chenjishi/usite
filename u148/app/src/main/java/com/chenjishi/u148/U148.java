package com.chenjishi.u148;

import android.app.Application;
import com.chenjishi.u148.utils.FileUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.flurry.android.FlurryAgent;

/**
 * Created by jishichen on 2017/4/14.
 */
public class U148 extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FileUtils.init(this);
        Fresco.initialize(this);

        new FlurryAgent.Builder()
                .build(this, "YYHS4STVXPMH6Y9GJ8WD");
    }
}
