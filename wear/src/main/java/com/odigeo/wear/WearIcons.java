package com.odigeo.wear;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by javier.rebollo on 1/7/16.
 */
public class WearIcons extends TextView {
    private String[] arrayIcons = {
            "&#xe600", "&#xe601", "&#xe602", "&#xe613", "&#xe614", "&#xe615", "&#xe616",
            "&#xe617", "&#xe618", "&#xe619", "&#xe61a", "&#xe61b", "&#xe61c", "&#xe61d",
            "&#xe61e", "&#xe61f", "&#xe620", "&#xe621", "&#xe622", "&#xe623", "&#xe624",
            "&#xe625", "&#xe626"
    };

    public WearIcons(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public WearIcons(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WearIcons,
                0, 0);
        int icon = attributes.getInt(R.styleable.WearIcons_icon, 0);

        setText(Html.fromHtml(arrayIcons[icon]));
    }

    public WearIcons(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), "wear_icons.ttf");
        setTypeface(customFont);
    }

    public void setIcon(String icon){

    }
}
