package com.noetix.libnoetix.csv;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractReader {

    protected static final String TAG = "nx_app";
    protected Map<String, LinkedList<Map<String, Float>>> csvMap;

    protected abstract void init();

    protected abstract LinkedList<Map<String,Float>> parseCSV(String path);

    protected LinkedList<Map<String, Float>> csvParserToList(CSVParser parser) throws IOException {

        LinkedList<Map<String, Float>> blendShapeList = new LinkedList<>();

        @SuppressWarnings("unused")
        Map<String, Integer> headers = parser.getHeaderMap();
        List<String> keys = parser.getHeaderNames();
        List<CSVRecord> records = parser.getRecords();
        int recordsSize = records.size();
        int keySize = keys.size();

        for (int i = 0; i < recordsSize; i++) {
            CSVRecord record = records.get(i);
            Map<String, Float> blendShape = new HashMap<>(recordsSize);
            for (int j = 1; j < keySize; j++) {
                String key = keys.get(j);
                Float value = Float.valueOf(record.get(key));
                blendShape.put(key, value);
            }
            blendShapeList.addLast(blendShape);
        }
        return blendShapeList;
    }

}
