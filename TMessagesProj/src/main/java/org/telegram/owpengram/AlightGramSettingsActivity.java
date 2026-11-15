package org.telegram.owpengram;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

/**
 * «Настройки AlightGram» — placeholder screen, will be built out from scratch.
 */
public class AlightGramSettingsActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar = createActionBar(context);
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle("Настройки AlightGram");
        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        fragmentView = root;
        return root;
    }

    @Override
    public boolean isLightStatusBar() {
        return false;
    }
}
