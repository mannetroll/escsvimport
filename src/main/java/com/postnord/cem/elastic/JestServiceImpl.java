package com.postnord.cem.elastic;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.BulkResult;
import io.searchbox.core.BulkResult.BulkResultItem;
import io.searchbox.core.Index;

/**
 * @author mannetroll
 */
public class JestServiceImpl {
	private static Logger LOG = LoggerFactory.getLogger(JestServiceImpl.class);
	private JestClient jestClient;
	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	public JestServiceImpl(JestClient jestClient) {
		this.jestClient = jestClient;
	}

	public String toJson(Object object) {
		try {
			StringWriter sw = new StringWriter();
			mapper.writeValue(sw, object);
			return sw.toString();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return "{\"message\": \"toJson failed\"}";
		}
	}

	public void toElasticSearchBulk(List<IndexDocument> bulk) {
		try {
			Builder bulkRequest = new Bulk.Builder();
			for (IndexDocument doc : bulk) {
				bulkRequest.addAction(
						new Index.Builder(toJson(doc.getSource())).index(doc.getIndex()).id(doc.getId()).build());
			}
			BulkResult execute = jestClient.execute(bulkRequest.build());
			String errorMessage = execute.getErrorMessage();
			if (errorMessage != null) {
				LOG.info("errorMessage: " + errorMessage);
				List<BulkResultItem> failedItems = execute.getFailedItems();
				LOG.info("FailedItems: " + execute.getFailedItems().size());
				for (BulkResultItem bulkResultItem : failedItems) {
					LOG.info("bulkResultItem: " + bulkResultItem.errorReason);
				}
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
