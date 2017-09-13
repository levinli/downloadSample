package com.example.download.demo.download;

/**
 * Created by levin on 2017/9/12.
 */

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
