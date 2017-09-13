package com.example.download.demo.download;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.renderscript.Int2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by levin on 2017/9/12.
 * 异步下载任务
 */

public class DownloadTask extends AsyncTask <String,Integer,Integer>{

    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;

    private DownloadListener listener;

    private boolean isCanceled = false;
    private boolean isPaused;
    private int lastProgress;

    public DownloadTask(DownloadListener listener){
        this.listener = listener;
    }


    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile randomAccessFile = null;
        File file = null;
        long downloadedLength = 0;
        String downloadUrl = params[0];
        String fileName = downloadUrl.hashCode()+"";
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        file = new File(directory+fileName);
        if(file.exists()){
            downloadedLength = file.length();
        }
        try {
            long contentLength = getContentLength(downloadUrl);
            if(contentLength ==0){
                return TYPE_FAILED;
            }else if(contentLength == downloadedLength){
                return TYPE_SUCCESS;
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes=" + downloadedLength +"-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if(response!=null){
                is = response.body().byteStream();
                randomAccessFile = new RandomAccessFile(file,"rw");
                randomAccessFile.seek(downloadedLength);
                byte [] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b))!=-1){
                    if(isCanceled){
                        return TYPE_CANCELED;
                    }else if(isPaused){
                        return TYPE_PAUSED;
                    }else {
                        total += len;
                        randomAccessFile.write(b,0,len);
                        int progress = (int) ((total + downloadedLength) *100/contentLength);
                        publishProgress(progress);
                    }

                }
                response.body().close();
                return TYPE_SUCCESS;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {

                if(is!=null){
                    is.close();
                }

                if(randomAccessFile!=null){
                    randomAccessFile.close();
                }

                if(isCanceled && file!=null){
                    file.delete();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return TYPE_FAILED;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = client.newCall(request).execute();
        if(response!=null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if(progress>lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status){
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
        }
    }

    public void pauseDownlaod(){
        isPaused = true;
    }

    public  void cancelDownload(){
        isCanceled = true;
    }

}
