package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.Category;
import kz.telecom.happydrive.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Galymzhan Sh on 11/15/15.
 */
public class CatalogFragment extends BaseFragment {

    private ListView listView;
    private CatalogAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new CatalogAdapter(getContext());
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                Toast.makeText(getActivity(), "Redirect to Galym's view", Toast.LENGTH_LONG).show();
            }
        });
        loadData();
    }

    private void loadData() {
        new Thread() {
            @Override
            public void run() {
                try {
                    final List<Category> data = Category.getCategoriesListTemp();
                    BaseActivity activity = (BaseActivity) getActivity();
                    final View view = getView();
                    if (activity != null && view != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (Category c: data) {
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


    class CatalogAdapter extends BaseAdapter {

        Context context;
        List<Category> data;
        LayoutInflater inflater = null;

        public CatalogAdapter(Context context) {
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
                vi = inflater.inflate(R.layout.fragment_catalog_row, null);
            TextView text = (TextView) vi.findViewById(R.id.text);
            text.setText(data.get(position).name);
            return vi;
        }
    }


}