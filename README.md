Brooklyn JMeter
===

An [Apache Brooklyn](https://brooklyn.apache.org/) entity that invokes 
[Apache JMeter](http://jmeter.apache.org/) against an endpoint.  

Throughput can be reconfigured post-deployment.

Releasing
---

```
mvn -Psonatype-oss-release release:clean release:prepare release:perform 
```

