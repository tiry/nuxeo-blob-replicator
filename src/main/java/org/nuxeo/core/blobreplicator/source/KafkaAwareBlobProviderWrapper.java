package org.nuxeo.core.blobreplicator.source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.blob.BlobInfo;
import org.nuxeo.ecm.core.blob.BlobProvider;
import org.nuxeo.ecm.core.blob.binary.BinaryBlobProvider;
import org.nuxeo.ecm.core.blob.binary.BinaryManager;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamService;

public class KafkaAwareBlobProviderWrapper implements BlobProvider {

	private static final Log log = LogFactory.getLog(KafkaAwareBlobProviderWrapper.class);

	protected LogAppender<Record> appender = null;

	protected final static String BACKEND_CLASS_PROP = "backend";

	protected final static String OPLOG_CONFIG_PROP = "nuxeo.stream.blobprovider.log.config";

	protected final static String DEFAULT_OPLOG_CONFIG_PROP = "bloblog";

	public static final String STREAM_NAME = "bloblog";

	protected final AtomicLong counter = new AtomicLong();

	protected BlobProvider backend;

	protected Map<String, String> properties = new HashMap<>();

	public static String getStreamConfig() {
		return Framework.getProperty(OPLOG_CONFIG_PROP, DEFAULT_OPLOG_CONFIG_PROP);
	}

	protected LogAppender<Record> getOpLogAppender() {
		if (appender == null) {
			synchronized (this) {
				if (appender == null) {
					StreamService service = Framework.getService(StreamService.class);
					LogManager oplogManager = service.getLogManager(getStreamConfig());
					oplogManager.createIfNotExists(STREAM_NAME, 1);
					appender = oplogManager.getAppender(STREAM_NAME);
				}
			}
		}
		return appender;
	}

	protected void addOpToLog(String blobKey) {
		try {

			String key = System.currentTimeMillis() + "-" + counter.incrementAndGet();
			getOpLogAppender().append(0, Record.of(key, blobKey.getBytes(StandardCharsets.UTF_8)));

			log.debug("added to oplog:" + blobKey);
		} catch (Exception e) {
			log.error("Unable to add stream entry:" + blobKey, e);
		}
	}

	@Override
	public void close() {
		backend.close();
	}

	@Override
	public void initialize(String blobProviderId, Map<String, String> properties) throws IOException {
		this.properties.putAll(properties);

		String backendClassName = properties.get(BACKEND_CLASS_PROP);
		try {
			backend = createBackend(backendClassName);
		} catch (Exception e) {
			log.error("Unable to create backend with class " + backendClassName, e);
		}

		backend.initialize(blobProviderId + "-backend", properties);
	}

	protected BlobProvider createBackend(String backendClassName) throws Exception {

		Class<?> backendClass = this.getClass().getClassLoader().loadClass(backendClassName);

		if (BlobProvider.class.isAssignableFrom(backendClass)) {
			@SuppressWarnings("unchecked")
			Class<? extends BlobProvider> blobProviderClass = (Class<? extends BlobProvider>) backendClass;
			return blobProviderClass.newInstance();
		} else if (BinaryManager.class.isAssignableFrom(backendClass)) {
			@SuppressWarnings("unchecked")
			Class<? extends BinaryManager> binaryManagerClass = (Class<? extends BinaryManager>) backendClass;
			BinaryManager binaryManager = binaryManagerClass.newInstance();
			return new BinaryBlobProvider(binaryManager);
		} else {
			throw new Exception(backendClass + " does not implement BlobProvider or BinaryManager");
		}
	}

	@Override
	public Blob readBlob(BlobInfo blobInfo) throws IOException {
		return backend.readBlob(blobInfo);
	}

	@Override
	public boolean supportsUserUpdate() {
		return backend.supportsUserUpdate();
	}

	@Override
	public String writeBlob(Blob ablob) throws IOException {
		String key = backend.writeBlob(ablob);
		addOpToLog(key);
		return key;
	}

}
