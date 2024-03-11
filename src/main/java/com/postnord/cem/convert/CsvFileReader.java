package com.postnord.cem.convert;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @author mannetroll
 *
 */
public class CsvFileReader {
    private List<CSVRecord> list;
    private Reader reader;
    private static final char DELIMITER = ',';

    public CsvFileReader(Reader reader) {
        this.reader = reader;
    }

    String[] keys = { "Kollinummer", "Kundnummer_uppdragsgivare", "Referensdokumentnummer", "Forsaljningsdokument",
            "s_ord_item", "Material", "Rakenskapsarperiod", "Till_Postnummer", "Fran_Postnummer", "Inlamningsperiod",
            "Kundnummer", "Tax_Number_2", "Box" };

    public List<Map<String, Object>> getListMap() {
        Set<String> set = new HashSet<String>(Arrays.asList(keys));
        if (list.size() > 0) {
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            CSVRecord headersSE = list.get(0);
            //
            // transform to Kibana fields
            //
            List<String> headers = new ArrayList<String>(headersSE.size());
            for (int i = 0; i < headersSE.size(); i++) {
                String header = headersSE.get(i);
                headers.add(replace(header));
            }
            for (int i = 1; i < list.size(); i++) {
                CSVRecord record = list.get(i);
                Map<String, Object> map = new HashMap<String, Object>();
                for (int j = 0; j < headers.size(); j++) {
                    String string = record.get(j);
                    String key = headers.get(j);
                    if (set.contains(key)) {
                        map.put(key, string);
                    } else if (isLong(string)) {
                        map.put(key, Long.parseLong(string));
                    } else if (isDouble(string)) {
                        map.put(key, Double.parseDouble(string));
                    } else {
                        map.put(key, string);
                    }
                }
                result.add(map);
            }
            return result;
        }
        return Collections.emptyList();
    }

    private String replace(String header) {
        header = header.replace(' ', '_').replace("(", "").replace(")", "").replace(":", "").replace("/", "");
        header = header.replace('.', '_').replace('-', '_').replace("%", "procent").replace("&", "och");
        header = header.replace('å', 'a').replace('ä', 'a').replace('ö', 'o');
        header = header.replace('Å', 'A').replace('Ä', 'A').replace('Ö', 'O');
        header = header.replace("___", "_").replace("__", "_").replace(",", "");
        return header;
    }

    public int readCsvFile() throws IOException {
        CSVParser csvFileParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(DELIMITER));
        try {
            //Get a list of CSV file records
            list = csvFileParser.getRecords();
            return list.size();
        } finally {
            csvFileParser.close();
        }
    }

    public static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isLong(String string) {
        if (string != null && string.startsWith("0")) {
            return false;
        }
        try {
            Long.parseLong(string);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}