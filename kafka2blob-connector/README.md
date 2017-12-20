# About 

This module provide a Kafka Connector that uses the Nuxeo BlobManager filesystem as a sink.


# Status

Basic implementation: should be ready for a real life test.

# Requirements

This module requires Java 8 and Maven 3.

# Building

Run the Maven build:

   mvn clean install

# Configuration

**Configure Kafka Connect **

Kafka connect needed to be configured so that the nuxeo plugin will be found: this means you need to provide a value for `plugin.path`, typically in the defaul configuration file (`config/connect-standalone.properties`).

However, because Kafka is fed via `nuxeo-stream` , we can not use the standard converters and marshaller.

You can directly use the configuration file provided in `kafka2blob-connector/connect-standandalone-nuxeo.properties` (and edit the `plugin.path` accordingly).

**Deploy the  Nuxeo Kafka Connect Plugin**

Use the Uber jar locared in `target/nuxeo-kafka2blob-connect-1.0.0-SNAPSHOT-jar-with-dependencies.jar`

**Copy and edit the connector configuration**

Copy and edit the configuration file located in `kafka2blob-connector/connect-blob-sink.properties`;

You want to edit the 3 parameters to according to your source server and the target Blob Manager storage location:

    blob.root= /home/tiry/dev/runEnv/nuxeo-server-9.10-I20171210_2311-tomcat/nxserver/data/binaries/data/
    blob.depth=2
    blob.url=http://127.0.0.1/8080/nuxeo/

# Starting the connector

    bin/connect-standalone.sh config/connect-standalone-nuxeo.properties config/connect-blob-sink.properties
    
# Licensing
 
This module is licensed under the GNU Lesser General Public License (LGPL) version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).
 
# About Nuxeo
 
Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with
SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.
More information is available at [www.nuxeo.com](http://www.nuxeo.com).