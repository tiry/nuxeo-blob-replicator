# About Blob ES Replicator

The goal of this module is to provide a way to replicate the blobs between 2 Nuxeo clusters deployed on 2 datacenters.

## Send App level OpLog in nuxeo-stream

The ideal is to leverage Kafka and KafkaMirror to propage the digest of every new Blob between the 2 Data centers and then to have a Computation to consume the stream and fetch the blobs using http.


   BlobProvider1 ==digest==> Kafka1 ==Mirror==> Kafka2 ==digest==> Computation ==download==> BlobProvider2 
 
# Configuration

## nuxeo.conf

You need to unable Kafka integration.

    kafka.enabled=true

On the source Nuxeo, copy src/main/resources/sample-blobprovider-source.xml in config (renamming as -config).

=> this declares a wrapper BlobProvider that writes to `nuxeo-stream`

On the target Nuxeo, copy src/main/resources/sample-blobprovider-sink.xml in config (renamming as -config).

=> this declares a Computation tha consumes the stream

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