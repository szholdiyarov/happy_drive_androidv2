package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.util.ArrayList;
import java.util.List;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.widget.BackgroundChangeable;
import kz.telecom.happydrive.util.GlideRoundedCornersTransformation;
import kz.telecom.happydrive.util.Utils;


/**
 * Created by Galymzhan Sh on 11/15/15.
 */
public class CatalogItemFragment extends BaseFragment {
    private ListView listView;
    private ItemAdapter adapter;
    private int categoryId;
    private String categoryName;

    private View.OnClickListener starClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = listView.getPositionForView(v);
            final Card pickedCard = adapter.data.get(position);
            new Thread() {
                @Override
                public void run() {
                    if (pickedCard.isStarred()) {
                        // remove from starred
                        ApiClient.removeStar(pickedCard.id);
                        pickedCard.setStarred(false);
                    } else {
                        ApiClient.putStar(pickedCard.id);
                        pickedCard.setStarred(true);
                    }
                    BaseActivity activity = (BaseActivity) getActivity();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetInvalidated();
                        }
                    });
                }
            }.start();
        }
    };

    private View.OnClickListener cardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = listView.getPositionForView(v);
            BaseActivity activity = (BaseActivity) getActivity();
            Card card = (Card) adapter.getItem(position);
            activity.replaceContent(CardDetailsFragment.newInstance(card),
                    true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog_item, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        categoryId = bundle.getInt("categoryId");
        categoryName = bundle.getString("categoryName");

        BaseActivity activity = (BaseActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionBar.setTitle(categoryName);

        adapter = new ItemAdapter(getContext());
        listView = (ListView) view.findViewById(R.id.cardsListView);
        listView.setAdapter(adapter);

        if (getActivity() instanceof BackgroundChangeable) {
            final ImageView backgroundImgView = ((BackgroundChangeable) getActivity())
                    .getBackgroundImageView();
            final Card card = User.currentUser().card;
            if (!Utils.isEmpty(card.getBackground())) {
                backgroundImgView.post(new Runnable() {
                    @Override
                    public void run() {
                        NetworkManager.getGlide()
                                .load(card.getBackground())
                                .signature(GlideCacheSignature.ownerBackgroundKey(card.getBackground()))
                                .override(backgroundImgView.getWidth(),
                                        backgroundImgView.getHeight())
                                .centerCrop()
                                .into(backgroundImgView);
                    }
                });
            }
        }

        loadData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getActivity().onBackPressed();
            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @MainThread
    private void disableProgressBar() {
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.fragment_catalog_progress_bar);
        progressBar.setVisibility(View.GONE);
    }

    private void loadData() {
        new Thread() {
            @Override
            public void run() {
                try {
                    final List<Card> data = ApiClient.getCategoryCards(categoryId);
                    BaseActivity activity = (BaseActivity) getActivity();
                    final View view = getView();
                    if (activity != null && view != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (Card c : data) {
                                    adapter.data.add(c);
                                }
                                adapter.notifyDataSetChanged();
                                disableProgressBar();
                            }
                        });
                    }
                } catch (final Exception e) {
                    BaseActivity activity = (BaseActivity) getActivity();
                    final View view = getView();
                    if (activity != null && view != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disableProgressBar();
                                Snackbar.make(view, R.string.no_connection, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }.start();
    }

    class ItemAdapter extends BaseAdapter {
        List<Card> data;
        private Card myCard = User.currentUser().card;

        public ItemAdapter(Context context) {
            this.data = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null)
                vi = LayoutInflater.from(getContext())
                        .inflate(R.layout.fragment_catalog_item_row, parent, false);

            TextView name = (TextView) vi.findViewById(R.id.name);
            name.setOnClickListener(cardClickListener);
            TextView description = (TextView) vi.findViewById(R.id.description);
            description.setOnClickListener(cardClickListener);

            final Card card = data.get(position);
            String fullName = card.getFirstName();
            if (!Utils.isEmpty(card.getLastName())) {
                fullName += " " + card.getLastName();
            }

            name.setText(fullName);
            description.setText(card.getPosition());

            if (card.getAvatar() != null) {
                final ImageView imageView = (ImageView) vi.findViewById(R.id.avatar);
                imageView.setOnClickListener(cardClickListener);
                if (!Utils.isEmpty(card.getAvatar())) {
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            String tempAvatarUrl = Request.DEFAULT_HOST + "/card/download/avatar/" + card.id;
                            DisplayMetrics dm = getResources().getDisplayMetrics();
                            NetworkManager.getGlide()
                                    .load(tempAvatarUrl)
                                    .signature(GlideCacheSignature
                                            .foreignCacheKey(tempAvatarUrl))
                                    .override(imageView.getWidth(),
                                            imageView.getHeight())
                                    .bitmapTransform(new CenterCrop(getContext()),
                                            new GlideRoundedCornersTransformation(getContext(),
                                                    Utils.dipToPixels(3f, dm), Utils.dipToPixels(1.5f, dm)))
                                    .error(R.drawable.user_photo)
                                    .placeholder(R.drawable.user_photo_load)
                                    .into(imageView);
                        }
                    });
                }
            }

            ImageView starView = (ImageView) vi.findViewById(R.id.star);
            if (card.compareTo(myCard) == 0) {
                starView.setVisibility(View.INVISIBLE);
            } else {
                starView.setVisibility(View.VISIBLE);
                starView.setColorFilter(card.isStarred() ? Color.BLACK : Color.WHITE);
                starView.setOnClickListener(starClickListener);
            }
            return vi;
        }
    }
}