package kz.telecom.happydrive.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.Category;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.MainActivity;

import java.util.List;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardEditParamsFragment extends BaseFragment {
    private EditText mFirstName, mLastName, mMiddleName,
            mPosition, mCompanyName, mPhoneNumber, mEmailAddress, mWebsite,
            mCompanyAddress, mAbout;
    private Spinner mCategory;
    private Card mCard;
    private ArrayAdapter<Category> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_edit_params, parent, false);
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
                                    adapter.add(c);
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFirstName = (EditText) view.findViewById(R.id.input_first_name);
        mLastName = (EditText) view.findViewById(R.id.input_last_name);
        mMiddleName = (EditText) view.findViewById(R.id.input_middle_name);
        mPosition = (EditText) view.findViewById(R.id.input_position);
        mCompanyName = (EditText) view.findViewById(R.id.input_company_name);
        mPhoneNumber = (EditText) view.findViewById(R.id.input_phone);
        mEmailAddress = (EditText) view.findViewById(R.id.input_email);
        mWebsite = (EditText) view.findViewById(R.id.input_website);
        mCompanyAddress = (EditText) view.findViewById(R.id.input_company_address);
        mAbout = (EditText) view.findViewById(R.id.input_about);

        mCategory = (Spinner) view.findViewById(R.id.select_category);
        // TODO: Wait until loads catalog data.
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategory.setAdapter(adapter);
        loadData();
        mCard = Card.getUserCard(getContext());
        if (mCard != null) {
            mFirstName.setText(mCard.firstName);
            mLastName.setText(mCard.lastName);
            mMiddleName.setText(mCard.middleName);
            mPosition.setText(mCard.position);
            mCompanyName.setText(mCard.companyName);
            mPhoneNumber.setText(mCard.phoneNumber);
            mEmailAddress.setText(mCard.email);
            mWebsite.setText(mCard.website);
            mCompanyAddress.setText(mCard.workAddress);
            mAbout.setText(mCard.about);

            for (int i = 0; i < adapter.getCount(); i++) {
                Category cat = adapter.getItem(i);
                if (cat.id == mCard.catId) {
                    mCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_card_edit_params, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            final String fn = mFirstName.getText().toString();
            if (TextUtils.isEmpty(fn)) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (mCard == null) {
                mCard = new Card();
            }

            final String position = mPosition.getText().toString();
            if (TextUtils.isEmpty(position)) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return true;
            }

            final String phoneNumber = mPhoneNumber.getText().toString();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (mCategory.getSelectedItemPosition() == 0) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return true;
            }

            mCard.firstName = fn;
            mCard.lastName = mLastName.getText().toString();
            mCard.middleName = mMiddleName.getText().toString();
            mCard.position = position;
            mCard.companyName = mCompanyName.getText().toString();
            mCard.phoneNumber = phoneNumber;
            mCard.email = mEmailAddress.getText().toString();
            mCard.website = mWebsite.getText().toString();
            mCard.catId = ((ArrayAdapter<Category>) mCategory.getAdapter())
                    .getItem(mCategory.getSelectedItemPosition()).id;
            mCard.workAddress = mCompanyAddress.getText().toString();
            mCard.about = mAbout.getText().toString();

            mCard.save(getContext());
            getActivity().onBackPressed();
            DataManager.getInstance().bus.post(new Card.OnCardUpdateEvent());
            return true;
        }

        return false;
    }
}
