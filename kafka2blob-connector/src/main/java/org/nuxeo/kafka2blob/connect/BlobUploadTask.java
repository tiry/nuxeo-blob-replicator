package org.nuxeo.kafka2blob.connect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobUploadTask extends SinkTask {

	private static final Logger log = LoggerFactory.getLogger(BlobUploadTask.class);

	protected String fsRoot;
	protected Integer fsDepth;
	protected String downloadUrl;

	@Override
	public void start(Map<String, String> props) {
		downloadUrl = props.get(BlobSinkConnector.BLOB_DOWNLOAD_URL);
		fsRoot = props.get(BlobSinkConnector.BLOB_ROOT);
		fsDepth = Integer.parseInt(props.get(BlobSinkConnector.BLOB_FS_DEPTH));
	}

	@Override
	public void put(Collection<SinkRecord> records) {
		for (SinkRecord record : records) {

			String digest = decodeRecord(record);			
			digest = digest.split(" ")[1];
			log.info("Processing blob with digest " + digest);						
			
			fetchRemoteBlob(digest);
			
		}
	}

	protected void fetchRemoteBlob(String digest) {

		String urlString = downloadUrl + "?key=" + digest;

		try {
			URL url = new URL(urlString);
			
			log.info("download url  " + url);						
			
			File blobFile = getFileForDigest(digest, true);

			
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(blobFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

			// FileUtils.copyURLToFile(url, blobFile);

		} catch (MalformedURLException e) {
			log.error("bad blob download url", e);
		} catch (FileNotFoundException e) {
			log.error("target file not found", e);
		} catch (IOException e) {
			log.error("Error while transfering Blob", e);
		}
	}

	protected String decodeRecord(SinkRecord record) {
		// the serializer between nuxeo-stream (Kafka Client0 and kafka connect do not
		// match
		// therefore, the bytearry decoding contains some non ascii chars
		// => doing rough cleanup before we can fix the underlying issue
		byte[] binary = (byte[]) record.value();
		try {
			String data = "";
			int startIdx = 3;
			int idx = startIdx;
			while (idx < binary.length) {
				if (binary[idx] < 32) {
					if (idx > startIdx) {
						data = data + new String(binary, startIdx, idx - startIdx - 1, "UTF-8");
					}
					startIdx = idx + 1;
				}
				idx++;
			}
			data = data + new String(binary, startIdx, idx - startIdx - 1, "UTF-8");
			return data;
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to decode", e);
			return null;
		}

	}

	protected File getFileForDigest(String digest, boolean createDir) {
		if (digest.length() < 2 * fsDepth) {
			return null;
		}
		StringBuilder buf = new StringBuilder(3 * fsDepth - 1);
		for (int i = 0; i < fsDepth; i++) {
			if (i != 0) {
				buf.append(File.separatorChar);
			}
			buf.append(digest.substring(2 * i, 2 * i + 2));
		}
		File dir = new File(fsRoot, buf.toString());
		if (createDir) {
			dir.mkdirs();
		}
		return new File(dir, digest);
	}

	@Override
	public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
	}

	@Override
	public void stop() {
	}

	@Override
	public String version() {
		return new BlobSinkConnector().version();
	}
}
