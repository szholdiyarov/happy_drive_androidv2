package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.Category;

import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.CardEditActivity;
import kz.telecom.happydrive.util.Utils;

import java.util.List;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardEditParamsMainFragment extends BaseFragment implements CardEditActivity.IsSavable {
    private static final String EXTRA_CARD = "extra:card";

    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mPhoneNumberEditText;
    private Spinner mCategorySpinner;
    private EditText mCompanyNameEditText;
    private EditText mPositionEditText;
    private ContentLoadingProgressBar mProgressBar;
    private ScrollView mScrollView;

    private Card mCard;
    private ArrayAdapter<Category> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static BaseFragment newInstance(Card card) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);
        BaseFragment fragment = new CardEditParamsMainFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_edit_params_main, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCard = getArguments().getParcelable(EXTRA_CARD);

        mScrollView = (ScrollView) view.findViewById(R.id.fragment_card_edit_main_scroller);
        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.progress_bar);
        mFirstNameEditText = (EditText) view.findViewById(R.id.fragment_card_edit_main_et_first_name);
        mLastNameEditText = (EditText) view.findViewById(R.id.fragment_card_edit_main_et_last_name);
        mPhoneNumberEditText = (EditText) view.findViewById(R.id.fragment_card_edit_main_et_phone_number);
        mCategorySpinner = (Spinner) view.findViewById(R.id.fragment_card_edit_main_spinner_category);
        mCompanyNameEditText = (EditText) view.findViewById(R.id.fragment_card_edit_main_et_company_name);
        mPositionEditText = (EditText) view.findViewById(R.id.fragment_card_edit_main_et_position);

        mProgressBar.hide();
        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            loadCategories();
        }

        mCategorySpinner.setAdapter(mAdapter);
        mFirstNameEditText.setText(mCard.getFirstName());
        mLastNameEditText.setText(mCard.getLastName());
        mPhoneNumberEditText.setText(mCard.getPhone());
        mCompanyNameEditText.setText(mCard.getWorkPlace());
        mPositionEditText.setText(mCard.getPosition());
    }

    private void loadCategories() {
        mScrollView.setVisibility(View.INVISIBLE);
        mProgressBar.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    final List<Category> data = Category.getCategories();
                    if (getActivity() != null && getView() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.clear();
                                mAdapter.add(new Category(0, "Не выбрана"));
                                for (Category c : data) {
                                    mAdapter.add(c);
                                }

                                mProgressBar.hide();
                                mScrollView.setVisibility(View.VISIBLE);
                                mAdapter.notifyDataSetChanged();
                                for (int i = 0; i < mAdapter.getCount(); i++) {
                                    Category cat = mAdapter.getItem(i);
                                    if (cat.id == mCard.getCategoryId()) {
                                        mCategorySpinner.setSelection(i);
                                        break;
                                    }
                                }
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
                                Snackbar.make(view, R.string.no_connection, Snackbar.LENGTH_LONG)
                                        .setAction("Повторить", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                loadCategories();
                                            }
                                        }).show();
                            }
                        });
                    }
                }
            }
        }.start();
    }

    @Override
    public boolean readyForSave() {
        if (getView() == null) {
            return false;
        }

        final String firstName = mFirstNameEditText.getText().toString();
        if (Utils.isEmpty(firstName)) {
            return false;
        }

        final String phoneNumber = mPhoneNumberEditText.getText().toString();
        if (Utils.isEmpty(phoneNumber)) {
            return false;
        }

        final int categoryPos = mCategorySpinner.getSelectedItemPosition();
        if (categoryPos <= 0) {
            return false;
        }

        final String position = mPositionEditText.getText().toString();
        if (Utils.isEmpty(position)) {
            return false;
        }

        mCard.setFirstName(firstName);
        mCard.setLastName(mLastNameEditText.getText().toString());
        mCard.setPhone(phoneNumber);
        mCard.setCategoryId(mAdapter.getItem(categoryPos).id);
        mCard.setWorkPlace(mCompanyNameEditText.getText().toString());
        mCard.setPosition(position);

        return true;
    }
}
