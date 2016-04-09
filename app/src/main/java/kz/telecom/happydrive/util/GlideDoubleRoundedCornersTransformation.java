package kz.telecom.happydrive.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

/**
 * Created by shgalym on 11/27/15.
 */
public class GlideDoubleRoundedCornersTransformation implements Transformation<Bitmap> {

    private BitmapPool mBitmapPool;
    private int mRadius;
    private int mBorder1;
    private int mBorder2;
    private int mBorder1Color;
    private int mBorder2Color;

    // todo refactor arguments
    public GlideDoubleRoundedCornersTransformation(Context context, int radius, int border1, int border1Color,
                                                   int border2, int border2COlor) {
        mBitmapPool = Glide.get(context).getBitmapPool();
        mRadius = radius;
        mBorder1 = border1;
        mBorder2 = border2;

        mBorder1Color = border1Color;
        mBorder2Color = border2COlor;
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();

        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap bitmap = mBitmapPool.get(width, height, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(mBorder1Color);
        canvas.drawRoundRect(new RectF(0, 0, width, height), mRadius, mRadius, paint);
        paint.setColor(mBorder2Color);
        canvas.drawRoundRect(new RectF(mBorder1, mBorder1, width - mBorder1, height - mBorder1),
                mRadius, mRadius, paint);

        int border = mBorder1 + mBorder2;
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(border, border, width - border, height - border),
                mRadius, mRadius, paint);

        return BitmapResource.obtain(bitmap, mBitmapPool);
    }

    // TODO add colors to signatures
    @Override
    public String getId() {
        return "RoundedTransformation(radius=" + mRadius + ", border1=" + mBorder1 + "," +
                " border2=" + mBorder2 + ")";
    }
}
