package co.example.michael.uploadpicturetest;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by Michael on 2015/1/28.
 */
class LoadImageTask extends AsyncTask<String, Integer, Bitmap>
{
    private ProgressDialog mProgressBar;

    LoadImageTask(Context context)
    {
        mProgressBar = new ProgressDialog(context);
        mProgressBar.setCancelable(true);
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressBar.setMax(100);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Log.i("Load thread", " >> doInBackground()");

        Bitmap bitmap = null;

        try{
            publishProgress(10);
            Thread.sleep(1000);

            //InputStream in = new java.net.URL(sImageUrl).openStream();
            publishProgress(60);
            Thread.sleep(1000);

            //bitmap = BitmapFactory.decodeStream(in);
            //in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        publishProgress(100);

        return bitmap;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPreExecute() {

        mProgressBar.setProgress(0);
        mProgressBar.setMessage("Image downloading ... %0");
        mProgressBar.show();

        Log.i("UI thread", " >> onPreExecute()");
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        Log.i("UI thread", " >> onPostExecute()");
        if (result != null) {
            mProgressBar.setMessage("Image downloading success!");
            //mImageView.setImageBitmap(result);
        }
        else {
            mProgressBar.setMessage("Image downloading failure!");
        }

        mProgressBar.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.i("UI thread", " >> onProgressUpdate() %" + values[0]);
        mProgressBar.setMessage("Image downloading ... %" + values[0]);
        mProgressBar.setProgress(values[0]);
    }
};
