package kz.telecom.happydrive.ui.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.data.network.NetworkManager;

/**
 * Created by shgalym on 25.12.2015.
 */
public class PhotoAdapter extends StorageAdapter {
    public PhotoAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder(inflater.inflate(R.layout.layout_storage_photo, parent, false));
    }

    private class PhotoViewHolder extends ViewHolder {
        private final ImageView imageView;
        private final TextView textView;

        private ApiObject mObject;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchItemClick(mObject);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dispatchLongItemClick(mObject);
                    return true;
                }
            });

            imageView = (ImageView) itemView.findViewById(R.id.layout_storage_photo_image_view);
            textView = (TextView) itemView.findViewById(R.id.layout_storage_photo_tv_title);
        }

        @Override
        public void onBind(ApiObject object) {
            mObject = object;
            if (object.isFolder()) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(((FolderObject) object).name);
                imageView.setImageDrawable(new ColorDrawable(0xee141631));
                return;
            } else {
                textView.setVisibility(View.GONE);
            }

            FileObject fileObject = (FileObject) object;
            final int type = fileObject.getType();
            switch (type) {
                case ApiObject.TYPE_FILE_VIDEO:
                    imageView.setImageResource(R.drawable.ic_storage_photo_video);
                    break;
                case ApiObject.TYPE_FILE_MUSIC:
                    imageView.setImageResource(R.drawable.ic_storage_photo_music);
                    break;
                case ApiObject.TYPE_FILE_DOCUMENT:
                    imageView.setImageResource(R.drawable.ic_storage_photo_doc);
                    break;
                case ApiObject.TYPE_FILE_PHOTO:
                default:
                    NetworkManager.getGlide()
                            .load(fileObject.url)
                            .placeholder(R.drawable.ic_storage_photo_load)
                            .error(R.drawable.ic_storage_photo_doc)
                            .into(imageView);
            }
        }
    }
}
