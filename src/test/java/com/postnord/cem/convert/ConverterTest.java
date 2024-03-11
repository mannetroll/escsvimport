package com.postnord.cem.convert;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ConverterTest {

    @Test
    public void test() {
        Converter converter = new Converter();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Till_Postnummer", "11436");
        converter.setPostnummerGeoPos(map);
        System.out.println("map: " + map);
    }

}
