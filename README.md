# About 

The goal is to build a pipeline to replicate blobs indexes between 2 remote Nuxeo instances.

The pipe uses Kafka as vehicle to transmit digests of the blobs between the source and the destination Nuxeo instance.

This pipe is composed of 2 parts:

 - a part that logs Blobs digest related operation inside Kafka
 	- this part is a Nuxeo Plugin that wraps the BlobProvider
 - a part that gets the digest from Kafka, download the blobs and copy it at the right location
 	- this part if a custom kafka connect 


# Licensing
 
This module is licensed under the GNU Lesser General Public License (LGPL) version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html).
 
# About Nuxeo
 
Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with
SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.
More information is available at [www.nuxeo.com](http://www.nuxeo.com).