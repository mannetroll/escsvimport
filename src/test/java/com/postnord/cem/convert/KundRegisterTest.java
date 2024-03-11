package com.postnord.cem.convert;

import java.util.Map;

import org.junit.Test;

public class KundRegisterTest {

    @Test
    public void test() {
        KundRegister kundRegister = new KundRegister();
        Map<String, Object> kund = kundRegister.getKund("0020072020");
        System.out.println("0020072020: " + kund);
    }

}
