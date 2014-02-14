package com.neffulapp.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.neffulapp.AddItemActivity;
import com.neffulapp.R;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class BitmapProcessor {

    private static Context context;
    private Bitmap placeHolderBitmap;
    private Bitmap transparentPlaceHolderBitmap;

    public BitmapProcessor(Context context) {
        BitmapProcessor.context = context;
        initTask(R.drawable.placeholder_image, placeHolderBitmap, true);
        initTask(R.drawable.placeholder_transparent, transparentPlaceHolderBitmap, true);
    }

    public void loadBitmap(int resId, String path, ImageView imageView) {
        if (cancelPotentialWork(resId, path, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(path, imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), (imageView.getDrawable() == null) ? placeHolderBitmap : transparentPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(resId);
        }
    }

    public void initTask(int resId, Bitmap bitmap, boolean isSampling) {
        SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(null, null, null, bitmap, isSampling);
        task.execute(resId);
    }

    public void initTask(String path, String filename, boolean isSampling) {
        SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(path, filename, null, null, isSampling);
        task.execute(0);
    }

    public void initTask(String path, ImageView imageView, boolean isSampling) {
        SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(path, null, imageView, null, isSampling);
        task.execute(0);
    }

    class SimpleBitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private String path;
        private boolean isSampling;
        private Bitmap bitmap;
        private ImageView imageView;
        private String filename;

        public SimpleBitmapWorkerTask(String path, String filename, ImageView imageView, Bitmap bitmap, boolean isSampling) {
            this.isSampling = isSampling;
            this.path = path;
            this.bitmap = bitmap;
            this.imageView = imageView;
            this.filename = filename;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            int resId = params[0];

            if (resId == 0 && path != null) {
                return decodeSampledBitmapFromFile(path, 100, 100, isSampling);
            } else {
                return decodeSampledBitmapFromResource(context.getResources(), resId, 80, 80, isSampling);
            }
        }

        @Override
        protected void onPostExecute(final Bitmap resultBitmap) {
            if (bitmap != null) {
                bitmap = resultBitmap;
            } else if (path != null && imageView != null) {
                imageView.setImageBitmap(resultBitmap);
            } else if (path != null && filename != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream out = new FileOutputStream(context.getFilesDir().getPath() + "/" + filename + ".png");
                            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.close();
                            ((AddItemActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Added", Toast.LENGTH_LONG).show();
                                    ((AddItemActivity) context).getProgressBar().setVisibility(View.INVISIBLE);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            ((AddItemActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Error copying file ! Please try again.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        private String path = null;

        public BitmapWorkerTask(String path, ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.path = path;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            if (data == 0) {
                return decodeSampledBitmapFromFile(path, 80, 80, true);
            } else {
                return decodeSampledBitmapFromResource(context.getResources(), data, 80, 80, true);
            }
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight, boolean isSampling) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, isSampling);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, boolean isSampling) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, isSampling);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, boolean isSampling) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        int size = height * width;

        if (size > 1000000) {
            isSampling = true;
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);
            reqWidth = displaySize.x / 2;
            reqHeight = displaySize.y / 2;
        }

        if (isSampling) {
            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
        }

        return inSampleSize;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(int data, String path, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            final String bitmapPath = bitmapWorkerTask.path;
            if (data != 0) {
                if (bitmapData != data) {
                    // Cancel previous task
                    bitmapWorkerTask.cancel(true);
                } else {
                    // The same work is already in progress
                    return false;
                }
            } else {
                if (!bitmapPath.equals(path)) {
                    // Cancel previous task
                    bitmapWorkerTask.cancel(true);
                } else {
                    // The same work is already in progress
                    return false;
                }
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}
