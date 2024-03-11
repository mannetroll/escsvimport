package com.postnord.cem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.postnord.cem.convert.Converter;
import com.postnord.cem.convert.CsvFileReader;
import com.postnord.cem.convert.KundRegister;
import com.postnord.cem.elastic.IndexDocument;
import com.postnord.cem.elastic.JestServiceImpl;
import com.postnord.cem.util.ServicePointUtil2;

import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.config.HttpClientConfig.Builder;
import io.searchbox.client.http.JestHttpClient;

@SpringBootApplication
public class ElasticBulkImporterApp implements CommandLineRunner {
	private final static Logger LOG = LogManager.getLogger(ServicePointUtil2.class);
	private static ObjectMapper reader;
	private static ConfigurableApplicationContext ctx;

	static {
		reader = new ObjectMapper();
		reader.registerModule(new JodaModule());
		reader.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Value("${ece.shield}")
	private String shield;

	@Value("${spring.data.jest.read-timeout}")
	private Integer readtimeout;
	@Value("${spring.data.jest.uri}")
	private String eshost;

	@Value("${import.gzip}")
	private String gzip;
	@Value("${import.rows}")
	private Integer rows;
	@Value("${import.bulkSize}")
	private Integer bulkSize;

	@Bean
	JestServiceImpl searchService() {
		LOG.info("eshost: " + eshost);
		List<Header> headers = new ArrayList<Header>();
		String basic = new String(Base64.encodeBase64(shield.getBytes()));
		headers.add(new BasicHeader("Authorization", "Basic " + basic));

		JestClientFactory factory = new JestClientFactory() {
			@Override
			protected HttpClientBuilder configureHttpClient(HttpClientBuilder builder) {
				return builder.setDefaultHeaders(headers);
			}
		};

		Builder builder = new HttpClientConfig.Builder(eshost).multiThreaded(true).discoveryEnabled(false)
				.connTimeout(1000).readTimeout(readtimeout);
		factory.setHttpClientConfig(builder.build());
		return new JestServiceImpl((JestHttpClient) factory.getObject());
	}

	@Override
	public void run(String... args) throws Exception {
		long row = 0;
		LOG.info("Read: " + row);
		//
		// Bulk Import
		//
		row = 0;
		KundRegister kundRegister = new KundRegister();
		Converter converter = new Converter();
		try {
			LOG.info("Importing " + rows + " rows from " + gzip);
			Reader reader = null;
			if (gzip.endsWith("gz")) {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gzip))));
			} else {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(gzip)));
			}

			CsvFileReader csvFileReader = new CsvFileReader(reader);
			int readCsvFile = csvFileReader.readCsvFile();
			LOG.info(gzip + ": " + readCsvFile);

			long start = System.currentTimeMillis();

			List<IndexDocument> bulk = new ArrayList<IndexDocument>(bulkSize);
			List<Map<String, Object>> listMap = csvFileReader.getListMap();
			for (Map<String, Object> map : listMap) {
				//
				// add kundRegister
				//
				Object kundnummer = map.get("Kundnummer_uppdragsgivare");
				if (kundnummer != null) {
					Map<String, Object> kund = kundRegister.getKund((String) kundnummer);
					for (String param : kund.keySet()) {
						map.put("KundRegister_" + param, kund.get(param));
					}
				}
				//
				// add GeoPos
				//
				converter.setPostnummerGeoPos(map);

				row++;
				IndexDocument doc = new IndexDocument();
				doc.setId((String) map.get("Kollinummer"));
				doc.setIndex("sass");
				doc.setSource(map);
				//
				bulk.add(doc);
				if ((row % bulkSize) == 0) {
					long elapsed = System.currentTimeMillis() - start;
					LOG.info((row) + ": " + (1000 * row) / elapsed + " fps,  " + (100 * row) / rows + " %");
					searchService().toElasticSearchBulk(bulk);
					bulk.clear();
				}
				if (row > rows) {
					break;
				}
			}
			reader.close();
			// rest request
			if (bulk.size() > 0) {
				LOG.info("rest: " + bulk.size());
				searchService().toElasticSearchBulk(bulk);
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		LOG.info("processed: " + row);
	}

	public static IndexDocument parseIndexDocument(String json) {
		try {
			return reader.readValue(json, IndexDocument.class);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) throws InterruptedException {
		ctx = SpringApplication.run(ElasticBulkImporterApp.class, args);
		LOG.info("Done!");
		ctx.close();
	}

}
