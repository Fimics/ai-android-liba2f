package com.noetix.core.downloader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Observer;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.noetix.R;
import com.noetix.utils.AppGlobals;
import com.noetix.utils.KLog;
import com.tonyodev.fetch2.Request;

import java.io.File;

public class DownloadActivity extends AppCompatActivity {
    private static final String TAG ="DownloadActivity";
    private TextView textView;
    private TextView tv;

    public static void start(Context context){
        Intent i = new Intent(AppGlobals.getApplication(), DownloadActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppGlobals.getApplication().startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        textView = this.findViewById(R.id.tvName);
        tv = this.findViewById(R.id.tv);
        // 全屏沉浸
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

      eventObserver();
    }


    private void eventObserver(){
        LiveEventBus.get(DownloadEvent.class).observe(this, new Observer<DownloadEvent>() {
            @Override
            public void onChanged(DownloadEvent e) {

                try {
                    KLog.d(TAG,"e ---------------------------->"+e.toString());

                    String fileName ="";
                    String fileProgress;

                    Request request =e.request;
                    String event =e.event;
                    int progress =e.progress;
                    int index=e.index;
                    int total =e.totalSize;

                    if (request==null){
                        fileName="全部文件";
                        fileProgress="("+index+"/"+total+")";
                        tv.setText("配置文件更新完成");
                        showLines(textView,
                                "文件名：" + fileName,
                                "文件序号：" + fileProgress,
                                "进度：" + progress + "%",
                                "状态：" + DownloadEvent.map.get(event));
                        finish();
                    }else {
                        String file = request.getFile();
                        KLog.d(TAG," file --->"+file);

                        int last = file.lastIndexOf("/");
                        fileName = (last >= 0 && last < file.length() - 1)
                                ? file.substring(last + 1)
                                : file;
                        index=index+1;
                        fileProgress="("+index+"/"+total+")";
                        showLines(textView,
                                "文件名：" + fileName,
                                "文件序号：" + fileProgress,
                                "进度：" + progress + "%",
                                "状态：" + DownloadEvent.map.get(event));
                    }



                } catch (Exception ex) {
                    KLog.d(TAG,ex.getMessage());
                }


            }
        });
    }


    public static void showLines(TextView tv, String... lines) {
        tv.setText(TextUtils.join("\n", lines));
    }

}
