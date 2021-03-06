package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
            final int position = (Integer) v.getTag();
            final Card pickedCard = adapter.data.get(position);
            new Thread() {
                @Override
                public void run() {
                    if (ApiClient.removeStar(pickedCard.id)) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.data.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }.start();
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
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(getContext(), R.color.colorPrimary)));
        actionBar.setTitle(R.string.action_favourite);

        if (adapter == null) {
            adapter = new ItemAdapter();
        }

        listView = (ListView) view.findViewById(R.id.cardsListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Card card = (Card) adapter.getItem(position);
                Intent intent = new Intent(getContext(), CatalogItemActivity.class);
                intent.putExtra(CatalogItemActivity.EXTRA_CARD, card);
                startActivity(intent);
            }
        });

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
                                adapter.data.clear();
                                adapter.data.addAll(data);
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
        List<Card> data = new ArrayList<>();

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
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.fragment_catalog_item_row, parent, false);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.avatar);
                TextView titleTextView = (TextView) convertView.findViewById(R.id.name);
                TextView descTextView = (TextView) convertView.findViewById(R.id.description);
                ImageButton actionBtn = (ImageButton) convertView.findViewById(R.id.star);
                actionBtn.setOnClickListener(starClickListener);
                viewHolder = new ViewHolder(imageView, titleTextView, descTextView, actionBtn);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Card card = data.get(position);

            String lastName = card.getLastName();
            String username = card.getFirstName();
            if (!Utils.isEmpty(lastName)) {
                if (!Utils.isEmpty(username)) {
                    username += " " + lastName;
                } else {
                    username = lastName;
                }
            }

            viewHolder.titleTextView.setText(username);
            viewHolder.descTextView.setText(card.getPosition());

            if (card.getAvatar() != null) {
                if (!Utils.isEmpty(card.getAvatar())) {
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    NetworkManager.getGlide()
                            .load(card.getAvatar())
                            .signature(GlideCacheSignature
                                    .foreignCacheKey(card.getAvatar()))
                            .bitmapTransform(new CenterCrop(getContext()),
                                    new GlideRoundedCornersTransformation(getContext(),
                                            Utils.dipToPixels(3f, dm), Utils.dipToPixels(1.5f, dm)))
                            .error(R.drawable.user_photo)
                            .placeholder(R.drawable.user_photo_load)
                            .into(viewHolder.imageView);
                }
            } else {
                viewHolder.imageView.setImageResource(R.drawable.user_photo);
            }

            ImageView starView = viewHolder.actionImageBtn;
            starView.setTag(position);
            starView.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent));

            return convertView;
        }
    }

    static class ViewHolder {
        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView descTextView;
        private final ImageButton actionImageBtn;

        ViewHolder(ImageView imageView, TextView titleTextView,
                   TextView descTextView, ImageButton actionImageBtn) {
            this.imageView = imageView;
            this.titleTextView = titleTextView;
            this.descTextView = descTextView;
            this.actionImageBtn = actionImageBtn;
        }
    }
}
