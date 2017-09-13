package com.example.download.demo.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.download.demo.R;
import com.example.download.demo.service.DownloadService;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 下载demo
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    @BindView(R.id.start_btn)
    Button startBtn;
    @BindView(R.id.cancel_btn)
    Button cancelBtn;
    @BindView(R.id.pause_btn)
    Button pauseBtn;

    private DownloadService.DownloadBinder binder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (DownloadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        startBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);

        Intent intent = new Intent(this,DownloadService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_btn:
                String url = "http://sw.bos.baidu.com/sw-search-sp/software/d2831b71fc397/eclipse_v4.7.0.exe";
                binder.startDownlaod(url);
                break;
            case R.id.pause_btn:
                binder.pauseDownload();
                break;
            case R.id.cancel_btn:
                binder.cancelDownload();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
