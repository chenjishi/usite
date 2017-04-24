package com.chenjishi.u148.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.utils.Constants;
import com.chenjishi.u148.utils.IntentUtils;
import com.chenjishi.u148.utils.Utils;
import com.chenjishi.u148.widget.AboutDialog;
import com.chenjishi.u148.widget.ExitDialog;
import com.chenjishi.u148.widget.LoginDialog;
import com.facebook.drawee.view.SimpleDraweeView;

import static android.widget.FrameLayout.LayoutParams.MATCH_PARENT;
import static android.widget.FrameLayout.LayoutParams.WRAP_CONTENT;
import static com.chenjishi.u148.utils.Constants.MODE_DAY;
import static com.chenjishi.u148.utils.Constants.MODE_NIGHT;

/**
 * Created by jishichen on 2017/4/18.
 */
public class MenuLayout extends FrameLayout implements View.OnClickListener,
        LoginDialog.OnLoginListener, ExitDialog.OnLogoutListener {
    private static final int TAG_SETTINGS = 233;
    private static final int TAG_MODE = 234;
    private static final int TAG_FAVORITE = 235;
    private static final int TAG_REWARD = 236;
    private static final int TAG_ABOUT = 237;
    private static final int TAG_EXIT = 238;
    private static final int TAG_LOGIN = 239;
    private static final int TAG_REGIST = 240;

    private LinearLayout container;

    private MenuListener listener;

    public MenuLayout(Context context) {
        super(context);
        init(context);
    }

    public MenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context ctx) {
        setClickable(true);
        LayoutParams lp = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        ImageView backgroundImage = new ImageView(ctx);
        backgroundImage.setImageResource(R.mipmap.road);
        backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(backgroundImage, lp);

        container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(container, lp);

        updateView();
    }

    public void setMenuListener(MenuListener listener) {
        this.listener = listener;
    }

    private void updateView() {
        container.removeAllViews();

        Context ctx = getContext();
        Resources res = getResources();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp2px(32), dp2px(32));
        lp.topMargin = dp2px(16);
        SimpleDraweeView imageView = new SimpleDraweeView(ctx);
        container.addView(imageView, lp);

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lp1.topMargin = dp2px(12);
        lp1.bottomMargin = dp2px(12);

        String[] titles = res.getStringArray(R.array.menu_item);
        int[] icons = {R.mipmap.ic_settings,
                R.mipmap.ic_bulb,
                R.mipmap.ic_favorite_menu,
                R.mipmap.ic_star,
                R.mipmap.ic_info,
                R.mipmap.ic_menu4};
        titles[1] = res.getString(Config.getThemeMode(ctx) == MODE_DAY
                ? R.string.mode_day : R.string.mode_night);
        int size = titles.length;

        if (Config.isLogin(ctx)) {
            UserInfo info = Config.getUser(ctx);

            imageView.setImageURI(Uri.parse(info.icon));
            TextView nameLabel = Utils.generateTextView(ctx, info.nickname, 0xFF3C645A, 16.f);
            container.addView(nameLabel, lp1);
        } else {
            imageView.setImageResource(R.mipmap.head);

            LinearLayout buttonLayout = new LinearLayout(ctx);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            container.addView(buttonLayout, lp1);

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            lp2.rightMargin = dp2px(16);

            TextView loginBtn = Utils.generateTextView(ctx, R.string.login, Color.TRANSPARENT,
                    14.f);
            loginBtn.setTextColor(res.getColorStateList(R.color.button_text_color));
            loginBtn.setBackgroundResource(R.drawable.login_btn);
            loginBtn.setPadding(dp2px(8), dp2px(4), dp2px(8), dp2px(4));
            loginBtn.setOnClickListener(this);
            loginBtn.setTag(TAG_LOGIN);
            buttonLayout.addView(loginBtn, lp2);

            TextView registBtn = Utils.generateTextView(ctx, R.string.menu_register, Color.TRANSPARENT,
                    14.f);
            registBtn.setTextColor(res.getColorStateList(R.color.button_text_color));
            registBtn.setPadding(dp2px(8), dp2px(4), dp2px(8), dp2px(4));
            registBtn.setBackgroundResource(R.drawable.login_btn);
            registBtn.setOnClickListener(this);
            registBtn.setTag(TAG_REGIST);
            buttonLayout.addView(registBtn);

            size = titles.length - 1;
        }

        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        for (int i = 0; i < size; i++) {
            LinearLayout view = getItemView(ctx, titles[i], icons[i]);
            view.setTag(TAG_SETTINGS + i);
            container.addView(view, lp3);
        }
    }

    private LinearLayout getItemView(Context ctx, String text, int icon) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp2px(8), dp2px(14), dp2px(8), 0);
        layout.setBackgroundResource(R.drawable.menu_list_highlight);
        layout.setOnClickListener(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        TextView textView = Utils.generateTextView(ctx, text, 0xFFEEEEEE,
                16.f);
        textView.setCompoundDrawablePadding(dp2px(8));
        textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        layout.addView(textView, lp);

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(MATCH_PARENT, 1);
        lp1.topMargin = dp2px(14);
        View divider = new View(ctx);
        divider.setBackgroundColor(0x66FFFFFF);
        layout.addView(divider, lp1);

        return layout;
    }

    @Override
    public void onClick(View v) {
        int index = (Integer) v.getTag();

        Context ctx = getContext();
        Intent intent;

        switch (index) {
            case TAG_SETTINGS:
                intent = new Intent(ctx, SettingsActivity.class);
                IntentUtils.getInstance().startActivity(ctx, intent);
                listener.onPanelClose();
                break;
            case TAG_MODE:
                Config.setThemeMode(ctx, Config.getThemeMode(ctx) == MODE_DAY
                        ? MODE_NIGHT : MODE_DAY);
                updateView();
                listener.onThemeChanged();
                break;
            case TAG_FAVORITE:
                if (Config.isLogin(ctx)) {
                    intent = new Intent(ctx, FavoriteActivity.class);
                    IntentUtils.getInstance().startActivity(ctx, intent);
                } else {
                    new LoginDialog(ctx, this).show();
                }
                listener.onPanelClose();
                break;
            case TAG_REWARD:
                Uri uri = Uri.parse("market://details?id=" + ctx.getPackageName());
                intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    ctx.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    startAppPage();
                }
                break;
            case TAG_ABOUT:
                new AboutDialog(ctx).show();
                break;
            case TAG_EXIT:
                new ExitDialog(ctx, this).show();
                break;
            case TAG_LOGIN:
                new LoginDialog(ctx, this).show();
                break;
            case TAG_REGIST:
                intent = new Intent(ctx, RegisterActivity.class);
                ctx.startActivity(intent);
                break;
        }
    }

    private void startAppPage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.XIAO_MI));
        getContext().startActivity(intent);
    }

    @Override
    public void logout() {
        Config.setUser(getContext(), null);
        updateView();
        Utils.showToast(getContext(), R.string.logout_success);
    }

    @Override
    public void onLoginSuccess() {
        updateView();
        Utils.showToast(getContext(), getResources().getString(R.string.login_success));
    }

    @Override
    public void onLoginError() {
        Utils.showToast(getContext(), getResources().getString(R.string.login_fail));
    }

    private int dp2px(int d) {
        return (int) (d * getContext().getResources().getDisplayMetrics().density + .5f);
    }

    public interface MenuListener {

        void onThemeChanged();

        void onPanelClose();

    }
}
