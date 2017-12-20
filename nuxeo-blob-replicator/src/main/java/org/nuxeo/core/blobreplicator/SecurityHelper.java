package org.nuxeo.core.blobreplicator;

import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kv.KeyValueService;
import org.nuxeo.runtime.kv.KeyValueStore;

public class SecurityHelper {

	public static final String STORE = "blobReplicatorKV";

	public static final String SECURITY_PARAM = "org.nuxeo.blobreplicator.security.enabled";

	public static boolean isSecurityEnabled() {		
		return Boolean.parseBoolean(Framework.getProperty(SECURITY_PARAM, "false"));
	}
	
	protected static KeyValueStore getStore() {
		KeyValueService kvs = Framework.getService(KeyValueService.class);
		return kvs.getKeyValueStore(STORE);
	}

	public static void storeTuple(String key, String digest) {
		getStore().put(key, digest);
	}

	public static String getDigest(String key) {
		String digest = getStore().getString(key);
		getStore().put(key, (String)null); 
		return digest;
	}

}
