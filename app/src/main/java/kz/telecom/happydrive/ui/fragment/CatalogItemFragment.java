package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.squareup.picasso.Picasso;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.Category;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Galymzhan Sh on 11/15/15.
 */
public class CatalogItemFragment extends BaseFragment {

    private ListView listView;
    private ItemAdapter adapter;
    private int categoryId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle = this.getArguments();
        categoryId = bundle.getInt("category_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog_item, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new ItemAdapter(getContext());
        listView = (ListView) view.findViewById(R.id.cardsListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                Card card = (Card) adapter.getItem(i);
                ((BaseActivity) getActivity()).replaceContent(CardDetailsFragment.newInstance(card), true,
                                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        });
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

    private void loadData() {
        new Thread() {
            @Override
            public void run() {
                try {
                    final List<Card> data = Card.getCards(categoryId);
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
                String tempAvatarUrl = "http://hd.todo.kz/card/download/avatar/" + Integer.toString(card.id);
                NetworkManager.getPicasso().load(tempAvatarUrl)
                        .fit().centerCrop()
                        .error(R.drawable.user_photo)
                        .placeholder(R.drawable.user_photo)
                        .into(imageView);

            }

            name.setText(fullName);
            description.setText(card.getPosition());
            return vi;
        }
    }

}