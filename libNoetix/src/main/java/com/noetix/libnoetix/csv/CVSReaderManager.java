package com.noetix.libnoetix.csv;

import java.util.LinkedList;
import java.util.Map;

public class CVSReaderManager implements ICSVReader{

    private final AssetsCSVReader assetsCSVReader;
    private final FileCSVReader fileCSVReader;

    private CVSReaderManager() {
        assetsCSVReader = new AssetsCSVReader();
        fileCSVReader = new FileCSVReader();
    }

    private static final class Holder{
        private static final CVSReaderManager instance = new CVSReaderManager();
    }

    public static CVSReaderManager getInstance(){
        return Holder.instance;
    }

    @Override
    public void parseCSVFiles() {
        fileCSVReader.loadCSVFiles();
        assetsCSVReader.loadCSVFile();
    }

    @Override
    public Map<String, LinkedList<Map<String, Float>>> getEmotionBlendShapes() {
        return assetsCSVReader.getCsvMap();
    }

    @Override
    public Map<String, LinkedList<Map<String, Float>>> getDefaultBlendShapes() {
        return fileCSVReader.getCsvMap();
    }
}
