package org.nuxeo.kafka2blob.connect;
        
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlobSinkConnector extends SinkConnector {


	public static final String BLOB_ROOT = "blob.root";
	public static final String BLOB_FS_DEPTH = "blob.depth";
	public static final String BLOB_DOWNLOAD_URL = "blob.url";
	
	protected String fsRoot;
	protected Integer fsDepth;
	protected String downloadUrl;
	

	@Override
	public String version() {
		return AppInfoParser.getVersion();
	}

	@Override
	public void start(Map<String, String> props) {		
		fsRoot = props.get(BLOB_ROOT);
		fsDepth = Integer.parseInt(props.get(BLOB_FS_DEPTH));		
		downloadUrl = props.get(BLOB_DOWNLOAD_URL)+ "binarydup";	
	}

	@Override
	public Class<? extends Task> taskClass() {
		return BlobUploadTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {

		ArrayList<Map<String, String>> configs = new ArrayList<>();
		for (int i = 0; i < maxTasks; i++) {
			Map<String, String> config = new HashMap<>();
			config.put(BLOB_ROOT, fsRoot);
			config.put(BLOB_FS_DEPTH, fsDepth.toString());
			config.put(BLOB_DOWNLOAD_URL, downloadUrl);			
			configs.add(config);
		}
		return configs;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	@Override
	public ConfigDef config() {		
		ConfigDef config = new ConfigDef();				
		config.define(BLOB_ROOT, Type.STRING,Importance.HIGH,"BlobManager root FS");
		config.define(BLOB_FS_DEPTH, Type.INT,Importance.HIGH,"BlobManager FS depth");		
		config.define(BLOB_DOWNLOAD_URL, Type.STRING ,Importance.HIGH,"Remote BlobManager download url");		
		return config;
	}
}
