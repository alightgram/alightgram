package org.telegram.owpengram;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

/**
 * «Кастомные функции» screen with two tabs:
 *  - «Смена рамки»: profile frame shape with a live avatar preview and a slider;
 *  - «Снежинки»: snowfall switch, size and speed sliders.
 */
public class CustomFunctionsActivity extends BaseFragment {

    private static final int SHAPE_CIRCLE = 100;
    private static final int SHAPE_SQUARE = 0;

    private static final int TAB_FRAME = 0;
    private static final int TAB_SNOW = 1;

    private TextView tabFrameButton;
    private TextView tabSnowButton;
    private FrameLayout panelFrame;
    private FrameLayout panelSnow;

    private AvatarPreviewView avatarPreview;
    private CustomSliderView shapeSlider;
    private CustomSliderView snowSizeSlider;
    private CustomSliderView snowSpeedSlider;
    private TextCheckCell snowToggleCell;
    private TextView shapeStateText;

    private int accentColor;
    private int whiteBg;

    @Override
    public View createView(Context context) {
        CustomFeatures.init(context);

        actionBar = createActionBar(context);
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle("Кастомные функции");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        int grayBg = getThemedColor(Theme.key_windowBackgroundGray);
        whiteBg = getThemedColor(Theme.key_windowBackgroundWhite);
        accentColor = getThemedColor(Theme.key_windowBackgroundWhiteBlueText);
        int trackGray = getThemedColor(Theme.key_switch2Track);
        int blackText = getThemedColor(Theme.key_windowBackgroundWhiteBlackText);
        int grayText = getThemedColor(Theme.key_windowBackgroundWhiteGrayText);

        FrameLayout rootFrame = new FrameLayout(context);
        rootFrame.setBackgroundColor(grayBg);

        ScrollView scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);
        rootFrame.addView(scrollView, LayoutHelper.createFrame(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ============ Tabs: «Смена рамки» / «Снежинки» ============
        LinearLayout segmented = new LinearLayout(context);
        segmented.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable segmentedBg = new GradientDrawable();
        segmentedBg.setCornerRadius(AndroidUtilities.dp(14));
        segmentedBg.setColor(whiteBg);
        segmented.setBackground(segmentedBg);
        segmented.setPadding(AndroidUtilities.dp(3), AndroidUtilities.dp(3), AndroidUtilities.dp(3), AndroidUtilities.dp(3));

        tabFrameButton = makeTabButton(context, "Смена рамки");
        tabSnowButton = makeTabButton(context, "Снежинки");
        segmented.addView(tabFrameButton, LayoutHelper.createLinear(0, AndroidUtilities.dp(42), 1.0f));
        segmented.addView(tabSnowButton, LayoutHelper.createLinear(0, AndroidUtilities.dp(42), 1.0f));
        content.addView(segmented, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 12, 8, 12, 8));

        tabFrameButton.setOnClickListener(v -> selectTab(TAB_FRAME));
        tabSnowButton.setOnClickListener(v -> selectTab(TAB_SNOW));

        // ============ Panels ============
        FrameLayout panelsHolder = new FrameLayout(context);
        panelsHolder.setBackgroundColor(Color.TRANSPARENT);
        content.addView(panelsHolder, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- Panel 1: frame shape ----
        panelFrame = new FrameLayout(context);
        LinearLayout frameContent = new LinearLayout(context);
        frameContent.setOrientation(LinearLayout.VERTICAL);
        panelFrame.addView(frameContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout cardFrame = makeCard(context, whiteBg);
        frameContent.addView(cardFrame, makeCardLp());
        frameContent.addView(makeSpacing(context, 8));

        TextView frameHint = new TextView(context);
        frameHint.setText("Форма применяется ко всем аватаркам: в чатах, в списке чатов и в профиле.");
        frameHint.setTextSize(13);
        frameHint.setTextColor(grayText);
        frameHint.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(10), AndroidUtilities.dp(20), 0);
        cardFrame.addView(frameHint, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout previewHolder = new FrameLayout(context);
        avatarPreview = new AvatarPreviewView(context, currentAccount);
        previewHolder.addView(avatarPreview, LayoutHelper.createFrame(120, 120, Gravity.CENTER_HORIZONTAL, 0, 24, 0, 24));
        cardFrame.addView(previewHolder, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        shapeStateText = new TextView(context);
        shapeStateText.setTextSize(14);
        shapeStateText.setTextColor(accentColor);
        shapeStateText.setGravity(Gravity.CENTER);
        shapeStateText.setText(describeShape(CustomFeatures.frameShape));
        shapeStateText.setPadding(0, 0, 0, AndroidUtilities.dp(6));
        cardFrame.addView(shapeStateText, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        shapeSlider = new CustomSliderView(context);
        shapeSlider.setTitle("Форма рамки");
        shapeSlider.setColors(trackGray, accentColor, whiteBg, blackText, accentColor);
        shapeSlider.setValue(CustomFeatures.frameShape, false);
        shapeSlider.setValueText(shapePercentText(CustomFeatures.frameShape));
        shapeSlider.setCallback((view, value, fromUser, finalValue) -> {
            CustomFeatures.setFrameShape(value);
            shapeStateText.setText(describeShape(value));
            shapeSlider.setValueText(shapePercentText(value));
            avatarPreview.setShapePercent(value, fromUser);
        });
        cardFrame.addView(shapeSlider, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 10));

        panelsHolder.addView(panelFrame, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- Panel 2: snowflakes ----
        panelSnow = new FrameLayout(context);
        LinearLayout snowContent = new LinearLayout(context);
        snowContent.setOrientation(LinearLayout.VERTICAL);
        panelSnow.addView(snowContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout cardSnow = makeCard(context, whiteBg);
        snowContent.addView(cardSnow, makeCardLp());
        snowContent.addView(makeSpacing(context, 8));

        snowToggleCell = new TextCheckCell(context);
        snowToggleCell.setHeight(AndroidUtilities.dp(50));
        snowToggleCell.setTextAndCheck("Включить снежинки", CustomFeatures.snowEnabled, false);
        snowToggleCell.setBackgroundColor(Color.TRANSPARENT);
        snowToggleCell.setOnClickListener(v -> {
            boolean newValue = !CustomFeatures.snowEnabled;
            CustomFeatures.setSnowEnabled(newValue);
            snowToggleCell.setChecked(newValue);
            notifyOverlay();
        });
        cardSnow.addView(snowToggleCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(50)));

        snowSizeSlider = new CustomSliderView(context);
        snowSizeSlider.setTitle("Размер снежинок");
        snowSizeSlider.setColors(trackGray, accentColor, whiteBg, blackText, accentColor);
        snowSizeSlider.setValue(CustomFeatures.snowSizeToPosition(CustomFeatures.snowSize), false);
        snowSizeSlider.setValueText(snowFactorText(CustomFeatures.snowSize));
        snowSizeSlider.setCallback((view, value, fromUser, finalValue) -> {
            CustomFeatures.setSnowSize(CustomFeatures.sliderToSnowSize(value));
            snowSizeSlider.setValueText(snowFactorText(CustomFeatures.snowSize));
            notifyOverlay();
        });
        cardSnow.addView(snowSizeSlider, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        snowSpeedSlider = new CustomSliderView(context);
        snowSpeedSlider.setTitle("Скорость снежинок");
        snowSpeedSlider.setColors(trackGray, accentColor, whiteBg, blackText, accentColor);
        snowSpeedSlider.setValue(CustomFeatures.snowSpeedToPosition(CustomFeatures.snowSpeed), false);
        snowSpeedSlider.setValueText(snowFactorText(CustomFeatures.snowSpeed));
        snowSpeedSlider.setCallback((view, value, fromUser, finalValue) -> {
            CustomFeatures.setSnowSpeed(CustomFeatures.sliderToSnowSpeed(value));
            snowSpeedSlider.setValueText(snowFactorText(CustomFeatures.snowSpeed));
            notifyOverlay();
        });
        cardSnow.addView(snowSpeedSlider, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView snowInfo = new TextView(context);
        snowInfo.setText("Падающие белые снежинки работают во всех экранах приложения: чаты, настройки, галерея и другие.");
        snowInfo.setTextSize(13);
        snowInfo.setTextColor(grayText);
        snowInfo.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(2), AndroidUtilities.dp(20), AndroidUtilities.dp(16));
        cardSnow.addView(snowInfo, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        panelsHolder.addView(panelSnow, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        selectTab(TAB_FRAME);

        fragmentView = rootFrame;
        return rootFrame;
    }

    private TextView makeTabButton(Context context, String title) {
        TextView b = new TextView(context);
        b.setText(title);
        b.setTextSize(14);
        b.setGravity(Gravity.CENTER);
        b.setAllCaps(false);
        b.setSingleLine(true);
        return b;
    }

    private void selectTab(int tab) {
        GradientDrawable active = new GradientDrawable();
        active.setCornerRadius(AndroidUtilities.dp(12));
        active.setColor(accentColor);
        GradientDrawable idle = new GradientDrawable();
        idle.setCornerRadius(AndroidUtilities.dp(12));
        idle.setColor(Color.TRANSPARENT);

        if (tab == TAB_FRAME) {
            tabFrameButton.setBackground(active);
            tabFrameButton.setTextColor(Color.WHITE);
            tabSnowButton.setBackground(idle);
            tabSnowButton.setTextColor(accentColor);
            panelFrame.setVisibility(View.VISIBLE);
            panelSnow.setVisibility(View.GONE);
        } else {
            tabSnowButton.setBackground(active);
            tabSnowButton.setTextColor(Color.WHITE);
            tabFrameButton.setBackground(idle);
            tabFrameButton.setTextColor(accentColor);
            panelFrame.setVisibility(View.GONE);
            panelSnow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Keep values fresh if they were changed elsewhere
        if (snowToggleCell != null) {
            snowToggleCell.setChecked(CustomFeatures.snowEnabled);
        }
        if (shapeSlider != null) {
            shapeSlider.setValue(CustomFeatures.frameShape, false);
            shapeSlider.setValueText(shapePercentText(CustomFeatures.frameShape));
        }
        if (shapeStateText != null) {
            shapeStateText.setText(describeShape(CustomFeatures.frameShape));
        }
        if (snowSizeSlider != null) {
            snowSizeSlider.setValue(CustomFeatures.snowSizeToPosition(CustomFeatures.snowSize), false);
            snowSizeSlider.setValueText(snowFactorText(CustomFeatures.snowSize));
        }
        if (snowSpeedSlider != null) {
            snowSpeedSlider.setValue(CustomFeatures.snowSpeedToPosition(CustomFeatures.snowSpeed), false);
            snowSpeedSlider.setValueText(snowFactorText(CustomFeatures.snowSpeed));
        }
    }

    private void notifyOverlay() {
        try {
            android.app.Activity activity = getParentActivity();
            if (activity != null && activity.getWindow() != null) {
                android.view.ViewGroup decor = (android.view.ViewGroup) activity.getWindow().getDecorView();
                for (int i = 0; i < decor.getChildCount(); i++) {
                    View child = decor.getChildAt(i);
                    if (child instanceof SnowflakeOverlayView) {
                        ((SnowflakeOverlayView) child).onSettingsChanged();
                    }
                }
            }
        } catch (Throwable ignore) {
            // overlay may be absent — safe to ignore
        }
    }

    private LinearLayout makeCard(Context context, int color) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(AndroidUtilities.dp(16));
        bg.setColor(color);
        card.setBackground(bg);
        return card;
    }

    private LinearLayout.LayoutParams makeCardLp() {
        return LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 12, 4, 12, 0);
    }

    private View makeSpacing(Context context, int heightDp) {
        View v = new View(context);
        v.setBackgroundColor(Color.TRANSPARENT);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(heightDp)));
        return v;
    }

    private static String describeShape(int value) {
        if (value >= SHAPE_CIRCLE) {
            return "Круглая (по умолчанию)";
        }
        if (value <= SHAPE_SQUARE) {
            return "Квадратная";
        }
        return "Скругление " + value + "%";
    }

    private static String shapePercentText(int value) {
        if (value >= SHAPE_CIRCLE) {
            return "Круг";
        }
        if (value <= SHAPE_SQUARE) {
            return "Квадрат";
        }
        return value + "%";
    }

    private static String snowFactorText(float factor) {
        return "×" + String.format(java.util.Locale.US, "%.2f", factor);
    }

    @Override
    public boolean isLightStatusBar() {
        return false;
    }
}
