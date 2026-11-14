package org.telegram.ui.Alight;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

/**
 * AlightGram settings screen.
 */
public class AlightSettingsActivity extends BaseFragment {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CHECK = 1;
    private static final int TYPE_VALUE = 2;
    private static final int TYPE_INFO = 3;

    private static final int ROW_FEATURES_HEADER = 0;
    private static final int ROW_GHOST = 1;
    private static final int ROW_EDIT_HISTORY = 2;
    private static final int ROW_HIDE_SPONSORED = 3;
    private static final int ROW_FEATURES_INFO = 4;
    private static final int ROW_SERVER_HEADER = 5;
    private static final int ROW_SERVER = 6;
    private static final int ROW_DOMAIN = 7;
    private static final int ROW_VERSION = 8;
    private static final int ROW_SERVER_INFO = 9;
    private static final int ROW_COUNT = 10;

    private RecyclerListView listView;
    private ListAdapter adapter;

    @Override
    public View createView(Context context) {
        AlightConfig.ensureLoaded();

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("AlightGram");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        listView = new RecyclerListView(context);
        listView.setSections();
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position) -> onRowClicked(position));
        frameLayout.addView(listView, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
        ));
        return fragmentView;
    }

    private void onRowClicked(int position) {
        if (position == ROW_GHOST) {
            AlightConfig.setGhostMode(!AlightConfig.ghostMode);
            adapter.notifyItemChanged(position);
        } else if (position == ROW_EDIT_HISTORY) {
            AlightConfig.setEditHistory(!AlightConfig.editHistory);
            adapter.notifyItemChanged(position);
        } else if (position == ROW_HIDE_SPONSORED) {
            AlightConfig.setHideSponsored(!AlightConfig.hideSponsored);
            adapter.notifyItemChanged(position);
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context context;

        private ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return ROW_COUNT;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == ROW_GHOST || position == ROW_EDIT_HISTORY || position == ROW_HIDE_SPONSORED;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == TYPE_HEADER) {
                view = new HeaderCell(context);
            } else if (viewType == TYPE_CHECK) {
                view = new TextCheckCell(context);
            } else if (viewType == TYPE_VALUE) {
                view = new TextSettingsCell(context);
            } else {
                view = new TextInfoPrivacyCell(context);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_HEADER) {
                HeaderCell cell = (HeaderCell) holder.itemView;
                cell.setText(LocaleController.getString(position == ROW_FEATURES_HEADER
                        ? R.string.AlightFeaturesHeader
                        : R.string.AlightAboutHeader));
            } else if (holder.getItemViewType() == TYPE_CHECK) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == ROW_GHOST) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.AlightGhostMode), AlightConfig.ghostMode, false);
                } else if (position == ROW_EDIT_HISTORY) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.AlightEditHistory), AlightConfig.editHistory, false);
                } else {
                    cell.setTextAndCheck(LocaleController.getString(R.string.AlightHideSponsored), AlightConfig.hideSponsored, true);
                }
            } else if (holder.getItemViewType() == TYPE_VALUE) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setEnabled(false);
                if (position == ROW_SERVER) {
                    cell.setTextAndValue(LocaleController.getString(R.string.AlightServerLabel), AlightConfig.SERVER_ENDPOINT, false);
                } else if (position == ROW_DOMAIN) {
                    cell.setTextAndValue(LocaleController.getString(R.string.AlightDomainLabel), AlightConfig.DOMAIN, false);
                } else {
                    cell.setTextAndValue(LocaleController.getString(R.string.AlightVersionLabel), BuildConfig.BUILD_VERSION_STRING + " (" + BuildConfig.VERSION_NUM + ")", true);
                }
            } else {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setText(LocaleController.getString(position == ROW_FEATURES_INFO
                        ? R.string.AlightFeaturesInfo
                        : R.string.AlightServerInfo));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == ROW_FEATURES_HEADER || position == ROW_SERVER_HEADER) {
                return TYPE_HEADER;
            }
            if (position == ROW_GHOST || position == ROW_EDIT_HISTORY || position == ROW_HIDE_SPONSORED) {
                return TYPE_CHECK;
            }
            if (position == ROW_FEATURES_INFO || position == ROW_SERVER_INFO) {
                return TYPE_INFO;
            }
            return TYPE_VALUE;
        }
    }
}
