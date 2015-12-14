package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
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
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.CatalogItemActivity;
import kz.telecom.happydrive.util.GlideRoundedCornersTransformation;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by darkhan on 24.11.15.
 */
public class StarFragment extends BaseFragment {
    private ListView listView;
    private ItemAdapter adapter;
    private View.OnClickListener starClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = listView.getPositionForView(v);
            final ImageButton imgBtn = (ImageButton) v;
            final Card pickedCard = adapter.data.get(position);
            final boolean inStarred = adapter.data.contains(pickedCard);
            final int newImage = inStarred ? R.drawable.favorite_off_icon : R.drawable.favorite_on_icon;
            final int oldImage = inStarred ? R.drawable.favorite_on_icon : R.drawable.favorite_off_icon;
            imgBtn.setImageResource(newImage);
            new Thread() {
                @Override
                public void run() {
                    final boolean success;
                    if (inStarred) {
                        // remove from starred
                        success = ApiClient.removeStar(pickedCard.id);
                        adapter.data.remove(pickedCard);
                    } else {
                        success = ApiClient.putStar(pickedCard.id);
                        adapter.data.add(pickedCard);
                    }
                    if (!success) {
                        BaseActivity activity = (BaseActivity) getActivity();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Roll back everything if query was unsuccessful.
                                imgBtn.setImageResource(oldImage);
                                if (inStarred) {
                                    adapter.data.add(pickedCard);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    adapter.data.remove(pickedCard);
                                }
                            }
                        });
                    }
                }
            }.start();

        }
    };
    private View.OnClickListener cardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = listView.getPositionForView(v);
            Card card = (Card) adapter.getItem(position);
            Intent intent = new Intent(getContext(), CatalogItemActivity.class);
            intent.putExtra(CatalogItemActivity.EXTRA_CARD, card);
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog_item, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionBar.setTitle(R.string.action_favourite);

        adapter = new ItemAdapter(getContext());
        listView = (ListView) view.findViewById(R.id.cardsListView);
        listView.setAdapter(adapter);
        loadData();
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
                    final List<Card> data = ApiClient.getStars();
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

        Context context;
        List<Card> data;
        LayoutInflater inflater = null;

        public ItemAdapter(Context context) {
            this.context = context;
            this.data = new ArrayList<>();
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                vi = inflater.inflate(R.layout.fragment_catalog_item_row, parent, false);
            TextView name = (TextView) vi.findViewById(R.id.name);
            TextView description = (TextView) vi.findViewById(R.id.description);
            final Card card = data.get(position);
            String fullName = card.getFirstName();
            if (!Utils.isEmpty(card.getLastName())) {
                fullName += " " + card.getLastName();
            }

            if (card.getAvatar() != null) {
                final ImageView imageView = (ImageView) vi.findViewById(R.id.avatar);
                imageView.setOnClickListener(cardClickListener);
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        String tempAvatarUrl = Request.DEFAULT_HOST + "/card/download/avatar/" + Integer.toString(card.id);
                        DisplayMetrics dm = getResources().getDisplayMetrics();
                        NetworkManager.getGlide()
                                .load(tempAvatarUrl)
                                .signature(GlideCacheSignature.foreignCacheKey(tempAvatarUrl))
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

            ImageView starView = (ImageView) vi.findViewById(R.id.star);
            starView.setImageResource(R.drawable.favorite_on_icon);
            starView.setOnClickListener(starClickListener);
            name.setOnClickListener(cardClickListener);
            description.setOnClickListener(cardClickListener);
            name.setText(fullName);
            description.setText(card.getPosition());
            return vi;
        }
    }
}
