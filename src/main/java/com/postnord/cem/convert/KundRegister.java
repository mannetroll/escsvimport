package com.postnord.cem.convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KundRegister {
    private static final Logger LOG = LoggerFactory.getLogger(KundRegister.class);
    private static Map<String, Map<String, Object>> kundMap = new HashMap<String, Map<String, Object>>();

    public KundRegister() {
        try {
            URL file = KundRegister.class.getClassLoader().getResource("Kundreg.csv");
            BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
            CsvFileReader reader = new CsvFileReader(in);
            int readCsvFile = reader.readCsvFile();
            LOG.info("KundRegister: " + readCsvFile);
            List<Map<String, Object>> listMap = reader.getListMap();
            for (Map<String, Object> map : listMap) {
                kundMap.put((String) map.get("Kundnummer"), map);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public Map<String, Object> getKund(String number) {
        return kundMap.get(number);
    }

}
