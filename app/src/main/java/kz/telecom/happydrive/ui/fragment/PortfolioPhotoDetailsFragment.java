package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;
import java.util.List;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.Comment;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.util.GlideRoundedCornersTransformation;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/26/15.
 */
public class PortfolioPhotoDetailsFragment extends BaseFragment {
    public static final String EXTRA_FILE_OBJECT = "extra:fileobj";

    private ContentLoadingProgressBar mProgressBar;
    private ListView mListView;
    private CommentAdapter mAdapter;
    private FileObject mFileObject;

    public static BaseFragment newInstance(FileObject fileObject) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_FILE_OBJECT, fileObject);

        BaseFragment fragment = new PortfolioPhotoDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio_photo_details, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mFileObject = getArguments().getParcelable(EXTRA_FILE_OBJECT);
        }

        if (mFileObject == null) {
            getActivity().onBackPressed();
            Toast.makeText(getContext(), "Передано не изображение", Toast.LENGTH_SHORT).show();
            return;
        }

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(mFileObject.name);

        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.progress_bar);
        mListView = (ListView) view.findViewById(R.id.listView);

        loadData();
    }

    private void loadData() {
        mProgressBar.show();
        mListView.setVisibility(View.INVISIBLE);
        new Thread() {
            @Override
            public void run() {
                mAdapter = new CommentAdapter();
                try {
                    mAdapter.setItems(ApiClient.getComments(mFileObject.id), false);
                } catch (Exception e) {
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.hide();
                            if (mAdapter.mItems == null) {
                                Toast.makeText(getActivity(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            mListView.setAdapter(mAdapter);
                            mListView.setVisibility(View.VISIBLE);

                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View headerView = inflater.inflate(R.layout.portfolio_photo_header, mListView, false);
                            final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)
                                    headerView.findViewById(R.id.portfolio_photo_header_image_view);
                            NetworkManager.getGlide()
                                    .load(mFileObject.url)
                                    .asBitmap()
                                    .into(new ViewTarget<SubsamplingScaleImageView, Bitmap>(imageView) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            imageView.setImage(ImageSource.bitmap(resource));
                                        }
                                    });

                            mListView.addHeaderView(headerView);

                            View footerView = inflater.inflate(R.layout.portfolio_photo_footer, mListView, false);
                            final EditText editText = (EditText) footerView.findViewById(R.id.portfolio_footer_et);
                            footerView.findViewById(R.id.portfolio_footer_btn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final String text = editText.getText().toString();
                                    if (Utils.isEmpty(text)) {
                                        Toast.makeText(getActivity(), "Напишите текст", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    writeComment(editText, text);
                                }
                            });


                            mListView.addFooterView(footerView);
                        }
                    });
                }
            }
        }.start();
    }

    private void writeComment(final EditText editText, final String text) {
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Отправка комментарий...");
        dialog.setCancelable(false);
        dialog.show();

        new Thread() {
            @Override
            public void run() {
                try {
                    ApiClient.postComment(mFileObject.id, text);
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                if (editText != null) {
                                    editText.setText("");
                                }

                                mAdapter.addItem(new Comment(-1, text, null,
                                        User.currentUser().card), true);
                            }
                        });
                    }
                } catch (final Exception e) {
                    final Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                if (e instanceof NoConnectionError) {
                                    Toast.makeText(activity, "Нет подключения к интернету",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, "Произошла ошибка при отправке комментария",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        }.start();

    }

    private class CommentAdapter extends BaseAdapter {
        private List<Comment> mItems;

        void setItems(List<Comment> items, boolean dispatchChanges) {
            mItems = items;
            if (dispatchChanges) {
                notifyDataSetChanged();
            }
        }

        void addItem(Comment comment, boolean dispatchChanges) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }

            mItems.add(comment);
            if (dispatchChanges) {
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return mItems != null ? mItems.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.layout_comment_row, parent, false);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.user_photo);
                TextView authorTextView = (TextView) convertView.findViewById(R.id.author_tv);
                TextView textView = (TextView) convertView.findViewById(R.id.text_tv);
                viewHolder = new ViewHolder(imageView, authorTextView, textView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Comment comment = (Comment) getItem(position);
            String lastName = comment.author.getLastName();
            String username = comment.author.getFirstName();
            if (!Utils.isEmpty(lastName)) {
                if (!Utils.isEmpty(username)) {
                    username += " " + lastName;
                } else {
                    username = lastName;
                }
            }

            final ImageView imageView = viewHolder.imageView;
            if (!Utils.isEmpty(comment.author.getAvatar())) {
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isOwner = User.currentUser().card.compareTo(comment.author) == 0;
                        final String tempUrl = Request.DEFAULT_HOST + "/card/download/avatar/" + comment.author.id;
                        DisplayMetrics dm = getResources().getDisplayMetrics();
                        NetworkManager.getGlide()
                                .load(tempUrl)
                                .signature(isOwner ? GlideCacheSignature.ownerAvatarKey(tempUrl)
                                        : GlideCacheSignature.foreignCacheKey(tempUrl))
                                .bitmapTransform(new CenterCrop(getContext()),
                                        new GlideRoundedCornersTransformation(getContext(),
                                                Utils.dipToPixels(3f, dm), Utils.dipToPixels(1.5f, dm)))
                                .error(R.drawable.user_photo)
                                .placeholder(R.drawable.user_photo)
                                .override(imageView.getWidth(),
                                        imageView.getHeight())
                                .into(imageView);
                    }
                });
            }

            viewHolder.authorTextView.setText(username);
            viewHolder.textView.setText(comment.text);

            return convertView;
        }
    }

    private static class ViewHolder {
        final ImageView imageView;
        final TextView authorTextView;
        final TextView textView;

        ViewHolder(ImageView imageView, TextView authorTextView, TextView textView) {
            this.imageView = imageView;
            this.authorTextView = authorTextView;
            this.textView = textView;
        }
    }
}
