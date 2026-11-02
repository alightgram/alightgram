package org.telegram.owpengram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

/**
 * «Кастомные функции» screen:
 *  - Card 1: profile frame shape with a live avatar preview and a slider;
 *  - Card 2: snowflakes switch, size and speed sliders.
 */
public class CustomFunctionsActivity extends BaseFragment {

    private static final int SHAPE_CIRCLE = 100;
    private static final int SHAPE_SQUARE = 0;

    private AvatarPreviewView avatarPreview;
    private CustomSliderView shapeSlider;
    private CustomSliderView snowSizeSlider;
    private CustomSliderView snowSpeedSlider;
    private TextCheckCell snowToggleCell;
    private TextView shapeStateText;

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
        int whiteBg = getThemedColor(Theme.key_windowBackgroundWhite);
        int accent = getThemedColor(Theme.key_windowBackgroundWhiteBlueText);
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

        // ============ Card 1: «Кастомные функции» — profile frame ============
        LinearLayout card1 = makeCard(context, whiteBg);
        content.addView(card1, makeCardLp());
        content.addView(makeSpacing(context));

        TextView cardTitle = new TextView(context);
        cardTitle.setText("Кастомные функции");
        cardTitle.setTextSize(18);
        cardTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        cardTitle.setTextColor(blackText);
        cardTitle.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(14), AndroidUtilities.dp(20), 0);
        card1.addView(cardTitle, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView cardSubtitle = new TextView(context);
        cardSubtitle.setText("Настройте внешний вид своего профиля");
        cardSubtitle.setTextSize(13);
        cardSubtitle.setTextColor(grayText);
        cardSubtitle.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(4), AndroidUtilities.dp(20), 0);
        card1.addView(cardSubtitle, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout previewHolder = new FrameLayout(context);
        avatarPreview = new AvatarPreviewView(context, currentAccount);
        previewHolder.addView(avatarPreview, LayoutHelper.createFrame(120, 120, Gravity.CENTER_HORIZONTAL, 0, 24, 0, 24));
        card1.addView(previewHolder, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        shapeStateText = new TextView(context);
        shapeStateText.setTextSize(14);
        shapeStateText.setTextColor(accent);
        shapeStateText.setGravity(Gravity.CENTER);
        shapeStateText.setText(describeShape(CustomFeatures.frameShape));
        shapeStateText.setPadding(0, 0, 0, AndroidUtilities.dp(6));
        card1.addView(shapeStateText, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        shapeSlider = new CustomSliderView(context);
        shapeSlider.setTitle("Форма рамки");
        shapeSlider.setColors(trackGray, accent, whiteBg, blackText, accent);
        shapeSlider.setValue(CustomFeatures.frameShape, false);
        shapeSlider.setValueText(shapePercentText(CustomFeatures.frameShape));
        shapeSlider.setCallback((view, value, fromUser, finalValue) -> {
            CustomFeatures.setFrameShape(value);
            shapeStateText.setText(describeShape(value));
            shapeSlider.setValueText(shapePercentText(value));
            avatarPreview.setShapePercent(value, fromUser);
        });
        card1.addView(shapeSlider, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 10));

        // ============ Snowflakes header ============
        HeaderCell snowHeader = new HeaderCell(context);
        snowHeader.setText("Снежинки");
        content.addView(snowHeader, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 12, 0, 12, 0));

        // ============ Card 2: snowflakes ============
        LinearLayout card2 = makeCard(context, whiteBg);
        content.addView(card2, makeCardLp());
        content.addView(makeSpacing(context));

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
        card2.addView(snowToggleCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(50)));

        snowSizeSlider = new CustomSliderView(context);
        snowSizeSlider.setTitle("Размер снежинок");
        snowSizeSlider.setColors(trackGray, accent, whiteBg, blackText, accent);
        snowSizeSlider.setValue(CustomFeatures.snowSizeToPosition(CustomFeatures.snowSize), false);
        snowSizeSlider.setValueText(snowFactorText(CustomFeatures.snowSize));
        snowSizeSlider.setCallback((view, value, fromUser, finalValue) -> {
            CustomFeatures.setSnowSize(CustomFeatures.sliderToSnowSize(value));
            snowSizeSlider.setValueText(snowFactorText(CustomFeatures.snowSize));
            notifyOverlay();
        });
        card2.addView(snowSizeSlider, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        snowSpeedSlider = new CustomSliderView(context);
        snowSpeedSlider.setTitle("Скорость снежинок");
        snowSpeedSlider.setColors(trackGray, accent, whiteBg, blackText, accent);
        snowSpeedSlider.setValue(CustomFeatures.snowSpeedToPosition(CustomFeatures.snowSpeed), false);
        snowSpeedSlider.setValueText(snowFactorText(CustomFeatures.snowSpeed));
        snowSpeedSlider.setCallback((view, value, fromUser, finalValue) -> {
            CustomFeatures.setSnowSpeed(CustomFeatures.sliderToSnowSpeed(value));
            snowSpeedSlider.setValueText(snowFactorText(CustomFeatures.snowSpeed));
            notifyOverlay();
        });
        card2.addView(snowSpeedSlider, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView snowInfo = new TextView(context);
        snowInfo.setText("Падающие белые точки работают во всех экранах приложения: чаты, настройки, галерея и другие.");
        snowInfo.setTextSize(13);
        snowInfo.setTextColor(grayText);
        snowInfo.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(2), AndroidUtilities.dp(20), AndroidUtilities.dp(16));
        card2.addView(snowInfo, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        fragmentView = rootFrame;
        return rootFrame;
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

    private View makeSpacing(Context context) {
        View v = new View(context);
        v.setBackgroundColor(Color.TRANSPARENT);
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
