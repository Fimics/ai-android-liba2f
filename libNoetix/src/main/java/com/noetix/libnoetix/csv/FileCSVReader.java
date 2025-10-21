package com.noetix.libnoetix.csv;

import com.noetix.libnoetix.SDKContext;
import com.noetix.utils.KLog;
import com.noetix.utils.TaskExecutors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class FileCSVReader extends AbstractReader{

    private static final String dir="/sdcard/robot_csv";

    public FileCSVReader() {
        init();
    }

    @Override
    public void init(){
        csvMap = new HashMap<String,LinkedList<Map<String,Float>>>();
        TaskExecutors.get().onIOTask(() -> createCSVDir());
    }

    private void createCSVDir(){
        File file = new File(dir);
        if (!file.exists()){
            file.mkdir();
        }
    }

    public Map<String,LinkedList<Map<String,Float>>> getCsvMap(){
        return csvMap;
    }

    public void loadCSVFiles(){
       TaskExecutors.get().onIOTask(() -> csvFile2Map());
    }

    private void csvFile2Map(){
        File csvDir = new File(dir);
        if (!csvDir.exists()|| csvDir.isFile()){
            KLog.d(TAG,"csv 目录为空或不是目录...");
            return;
        }

        String[] names =csvDir.list();
        File [] files =csvDir.listFiles();
        int fileCount = names.length;

        for (int k=0;k<fileCount;k++){
            String path = files[k].getPath();
            LinkedList<Map<String,Float>> fileBlendShapes = parseCSV(path);
            csvMap.put(path,fileBlendShapes);
        }
        KLog.d(TAG,"loadCSVFiles finish...");
    }

    @Override
    public LinkedList<Map<String,Float>> parseCSV(String path){
        try {
            FileReader reader = new FileReader(path);
            CSVParser parser = new CSVParser(reader,CSVFormat.DEFAULT.withFirstRecordAsHeader());
            return csvParserToList(parser);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
