package org.nuxeo.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.lib.stream.computation.AbstractComputation;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamProcessorTopology;

public class BlobReplicationSinkComputation implements StreamProcessorTopology {

	private static final Log log = LogFactory.getLog(BlobReplicationSinkComputation.class);

	public static final String COMPUTATION_NAME = "BlobReplicator";

	public static final String BATCH_SIZE_OPT = "batchSize";

	public static final String BATCH_THRESHOLD_MS_OPT = "batchThresholdMs";

	public static final int DEFAULT_BATCH_SIZE = 1;

	public static final int DEFAULT_BATCH_THRESHOLD_MS = 200;

	@Override
	public Topology getTopology(Map<String, String> options) {
		int batchSize = getOptionAsInteger(options, BATCH_SIZE_OPT, DEFAULT_BATCH_SIZE);
		int batchThresholdMs = getOptionAsInteger(options, BATCH_THRESHOLD_MS_OPT, DEFAULT_BATCH_THRESHOLD_MS);
		return Topology.builder()
				.addComputation(() -> new BlobReplicatorComputation(COMPUTATION_NAME, batchSize, batchThresholdMs),
						Collections.singletonList("i1:" + KafkaAwareBlobProviderWrapper.STREAM_NAME))
				.build();
	}

	public class BlobReplicatorComputation extends AbstractComputation {

		protected final int batchSize;

		protected final int batchThresholdMs;

		public BlobReplicatorComputation(String name, int batchSize, int batchThresholdMs) {
			super(name, 1, 0);
			this.batchSize = batchSize;
			this.batchThresholdMs = batchThresholdMs;
		}

		@Override
		public void processRecord(ComputationContext ctx, String inputStreamName, Record record) {

			try {
				String digest = new String(record.data, "UTF-8");

				Blob blob = fetchRemoteBlob(digest);

				storeBlob(blob);

			} catch (UnsupportedEncodingException e) {
				log.error("Unable to read digest from replication stream", e);
			} catch (IOException e) {
				log.error("Unable store Blob", e);
			}
		}

		protected Blob fetchRemoteBlob(String digest) {
			// XXX
			return null;
		}

		protected void storeBlob(Blob blob) throws IOException {
			BlobManager bm = Framework.getService(BlobManager.class);
			bm.getBlobProvider("default").writeBlob(blob);
		}

	}

	protected int getOptionAsInteger(Map<String, String> options, String option, int defaultValue) {
		String value = options.get(option);
		return value == null ? defaultValue : Integer.valueOf(value);
	}
}
