# About Blob ES Replicator

The goal of this module is to provide a way to replicate the blobs between 2 Nuxeo clusters deployed on 2 datacenters.

## Send App level OpLog in nuxeo-stream

The ideal is to leverage Kafka and KafkaMirror to propage the digest of every new Blob between the 2 Data centers and then to have a Computation to consume the stream and fetch the blobs using http.


   BlobProvider1 ==digest==> Kafka1 ==Mirror==> Kafka2 ==digest==> Computation ==download==> BlobProvider2 
 
# Configuration

## On the Source server

**nuxeo.conf**

You need to unable Kafka integration.

    kafka.enabled=true

**config**

You need to declares a wrapper BlobProvider that writes to `nuxeo-stream` before delegating to your "real" blobmanager

You can use `src/main/resources/sample-blobprovider-source.xml` as an example

(if you copy directly the file in the `config` do not forget to rename it with a  `-config.xml` suffix).

Provided you use the default BlobProvider, this should look something like this:

    <component name="org.nuxeo.blobprovider.source.config">
      <require>default-repository-config</require>

      <extension target="org.nuxeo.ecm.core.blob.BlobManager" point="configuration">
        <blobprovider name="default">
          <class>org.nuxeo.core.blobreplicator.source.KafkaAwareBlobProviderWrapper</class>
          <property name="backend">org.nuxeo.ecm.core.blob.binary.DefaultBinaryManager</property>
          <property name="path"></property>
          <property name="key"></property>
        </blobprovider>
      </extension>
    </component>


## On the Destination / Sink server

**nuxeo.conf**

You need to unable Kafka integration.

    kafka.enabled=true

You also need to define the source server that will be used to download the blobs

    org.nuxeo.blobreplicator.server=http://sourceserver:port/nuxeo/

**config**

You need to declare a `Computation` that will consumes the `bloblog` stream, download the blobs and store then locally.

You can use `src/main/resources/sample-blobprovider-sink.xml` as an example

(if you copy directly the file in the `config` do not forget to rename it with a  `-config.xml` suffix).

Typically:

    <component name="org.nuxeo.blobprovider.source.config">     
        <require>org.nuxeo.stream.defaultConfig</require>      
        <extension target="org.nuxeo.runtime.stream.service" point="streamProcessor">
                <streamProcessor name="blobReplicator" logConfig="bloblog"
                        defaultConcurrency="1" defaultPartitions="1"
                        class="org.nuxeo.core.blobreplicator.sink.BlobReplicationSinkComputation">
                        <option name="batchSize">1</option>
                        <option name="batchTimeThresholdMs">100</option>
                </streamProcessor>
        </extension>
    </component>

# Security

## Security off

By default, there is no security: provided you know the digest you can fetch the associated blob.

## Shared Secret based security

You can activate security in `nuxeo.conf`

    org.nuxeo.blobreplicator.security.enabled=true

This settings must be consistent between the client and the server.

Security is based on a shared secret (random GUID) that is generated for each Blob and shared using Kafka:

 - for each Blob on the source
 	- generate a token (i.e. GUID)
 	- store the `(token,digest)` pair in the KVStore
 	- send the `(token,digest)` pair  via `nuxeo-stream` (token is actually the key of the record)
 - for Download
 	- the Sink/Consumer will call using the GUID/token
 	- the Download does the token=>digest translation and deletes the KV entry 	

This provide a single usage random key for each download and this is secured as long as the Kafla streams are secured.

The KV Store is by default declared in JVM memory: this means it will not work if you are using a cluster.
In that case, you probably want to redefine the KVStore to use Redis or MongoDB, typically:

    <component name="org.nuxeo.blobprovider.kv.contrib.override">
     
     <require>org.nuxeo.blobprovider.kv.contrib</require>
     
     <extension target="org.nuxeo.runtime.kv.KeyValueService" point="configuration">
      <store name="blobReplicatorKV" class="org.nuxeo.ecm.core.mongodb.kv.MongoDBKeyValueStore">
        <property name="collection">blobReplicatorKV</property>
      </store>
     </extension>
     
    </component>
  

# Requirements

This module requires Java 8 and Maven 3.

# Building
 
   mvn clean install

# Licensing
 
This module is licensed under the GNU Lesser General Public License (LGPL) version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).
 
# About Nuxeo
 
Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with
SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.
More information is available at [www.nuxeo.com](http://www.nuxeo.com).