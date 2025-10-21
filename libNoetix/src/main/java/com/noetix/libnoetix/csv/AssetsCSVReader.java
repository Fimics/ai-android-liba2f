package com.noetix.libnoetix.csv;

import android.content.Context;
import android.content.res.AssetManager;

import com.noetix.utils.AppGlobals;
import com.noetix.utils.KLog;
import com.noetix.utils.TaskExecutors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AssetsCSVReader extends AbstractReader {

    private static final String TAG = "nx_app";
    public static final String ASSETS_CSV_PATH = "csv"; // 指定 assets 目录下的文件路径

    private final Context context; // 需要 Context 来访问 assets

    public AssetsCSVReader() {
        this.context = AppGlobals.getApplication();
        csvMap = new HashMap<String, LinkedList<Map<String, Float>>>();
        init();
    }

    @Override
    public void init() {
        csvMap = new HashMap<>();
        TaskExecutors.get().onIOTask(this::loadCSVFile);
    }


    public Map<String, LinkedList<Map<String, Float>>> getCsvMap() {
        return csvMap;
    }

    public void loadCSVFile() {
        TaskExecutors.get().onIOTask(() -> csvFile2Map());
    }

    public void csvFile2Map() {
        String[] files = null;
        try {
            files = context.getAssets().list(ASSETS_CSV_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (files == null || files.length == 0) {
            KLog.e(TAG, "Error reading CSV file from assets: " + ASSETS_CSV_PATH);
            return;
        }

        for (String file : files) {
            String path = ASSETS_CSV_PATH + "/" + file;
            LinkedList<Map<String, Float>> fileBlendShapes = parseCSV(path);
            csvMap.put(file, fileBlendShapes);
        }
    }

    @Override
    protected LinkedList<Map<String, Float>> parseCSV(String path) {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            return csvParserToList(parser);
        } catch (IOException e) {
            KLog.e(TAG, "Error reading CSV file from assets: " + e.getMessage());
        }
        return null;
    }

}
