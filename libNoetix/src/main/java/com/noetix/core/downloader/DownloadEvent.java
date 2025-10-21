package com.noetix.core.downloader;

import com.jeremyliao.liveeventbus.core.LiveEvent;
import com.tonyodev.fetch2.Request;

import java.util.HashMap;
import java.util.Map;

public class DownloadEvent implements LiveEvent {
    public static final String EVENT_ITEM_PROGRESS="event_item_progress";
    public static final String EVENT_ITEM_COMPLETED="event_item_completed";
    public static final String EVENT_ALL_COMPLETED="event_all_completed";
    public static final String EVENT_ITEM_FAILED="event_item_failed";

    public static final HashMap<String,String> map = new HashMap(4);

    static {
        map.put(EVENT_ITEM_PROGRESS,"下载中");
        map.put(EVENT_ITEM_COMPLETED,"下载完成");
        map.put(EVENT_ITEM_FAILED,"下载失败");
        map.put(EVENT_ALL_COMPLETED,"全部下载完成");
    }

    public Request request;
    public int progress;
    public String  event = EVENT_ITEM_PROGRESS;
    public int index;
    public int totalSize;

    public DownloadEvent(Request request, int progress, String event, int index, int totalSize) {
        this.request = request;
        this.progress = progress;
        this.event = event;
        this.index = index;
        this.totalSize = totalSize;
    }


    @Override
    public String toString() {
        return "DownloadEvent{" +
                "request=" + request +
                ", progress=" + progress +
                ", event='" + event + '\'' +
                ", index=" + index +
                ", totalSize=" + totalSize +
                '}';
    }

}
