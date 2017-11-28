Brooklyn JMeter
===

An [Apache Brooklyn](https://brooklyn.apache.org/) entity that invokes 
[Apache JMeter](http://jmeter.apache.org/) against an endpoint.  

Throughput can be reconfigured post-deployment.

## Building

From the top level of the project:
 
    mvn clean install


## Installing

Add the bundle to a running Brooklyn or AMP server:

    br catalog add target/brooklyn-jmeter-1.0.0-SNAPSHOT.jar 


## Example Usage

Deploy the jmeter entity, configuring it with the target IP (or hostname) and the port. For example:

    name: jmeter
    location: aws-ec2:us-east-1
    services:
      - type: jmeter
        brooklyn.config:
          target: 34.240.101.234
          port: '8000'

## Releasing

```
mvn -Psonatype-oss-release release:clean release:prepare release:perform
```


