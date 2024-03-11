package com.postnord.cem.convert;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

public class CsvFileReaderTest {

	@Test
	public void test1() throws Exception {
		String fileName = "data/sample.csv";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		CsvFileReader reader = new CsvFileReader(br);
		int readCsvFile = reader.readCsvFile();
		Assert.assertEquals(101, readCsvFile);
	}

	@Test
	public void test2() throws Exception {
		String fileName = "data/Kundreg.csv";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		CsvFileReader reader = new CsvFileReader(br);
		int readCsvFile = reader.readCsvFile();
		Assert.assertEquals(32, readCsvFile);
	}
}
