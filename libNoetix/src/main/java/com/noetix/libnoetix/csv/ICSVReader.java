package com.noetix.libnoetix.csv;

import java.util.LinkedList;
import java.util.Map;

public interface ICSVReader {

    void parseCSVFiles();
    Map<String, LinkedList<Map<String, Float>>> getEmotionBlendShapes();
    Map<String, LinkedList<Map<String, Float>>> getDefaultBlendShapes();

    static ICSVReader getInstance(){
        return CVSReaderManager.getInstance();
    }
}
