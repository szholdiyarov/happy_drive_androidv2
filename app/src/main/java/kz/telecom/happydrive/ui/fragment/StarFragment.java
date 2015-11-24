package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darkhan on 24.11.15.
 */
public class StarFragment extends BaseFragment {
    private ListView listView;
    private ItemAdapter adapter;

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
        BaseActivity activity = (BaseActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.action_favourite);
        actionBar.setDisplayHomeAsUpEnabled(true);

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
                                for (Card c: data) {
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
            Card card = data.get(position);
            String fullName = card.getFirstName();
            if (!Utils.isEmpty(card.getLastName())) {
                fullName += " " + card.getLastName();
            }

            if (card.getAvatar() != null) {
                ImageView imageView = (ImageView) vi.findViewById(R.id.avatar);
                imageView.setOnClickListener(cardClickListener);
                String tempAvatarUrl = "http://hd.todo.kz/card/download/avatar/" + Integer.toString(card.id);
                NetworkManager.getPicasso().load(tempAvatarUrl)
                        .fit().centerCrop()
                        .error(R.drawable.user_photo)
                        .placeholder(R.drawable.user_photo)
                        .into(imageView);

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
            BaseActivity activity = (BaseActivity) getActivity();
            Card card = (Card) adapter.getItem(position);
            activity.replaceContent(CardDetailsFragment.newInstance(card), true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
    };




}