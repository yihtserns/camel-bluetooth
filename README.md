Camel Bluetooth Component
=========================
This component allows communication with another Bluetooth-enabled device.

URI format
----------
```
bt:<operation|profile>[?options]
```
or
```
bt://<operation|profile>[?options]
```

Examples
--------
### Accept files via Bluetooth and save them in a directory
```groovy
from("bt://opp").to("file://files");
```

### Search nearby Bluetooth devices
```groovy
import javax.bluetooth.RemoteDevice;

RemoteDevice[] devices = camelContext.createProducerTemplate().requestBody("bt://scan", null, RemoteDevice[].class);
...
```

Supported Operations
--------------------
- `scan`

Supported Profiles
------------------
- `opp` ([Object Push Profile](https://en.wikipedia.org/wiki/List_of_Bluetooth_profiles#Object_Push_Profile_.28OPP.29))

Issues
------
### To run on 64-bit computer
Using Maven:
```xml
<!-- pom.xml -->
<project ...>
    ...
    <dependencies>
        <dependency>
            <groupId>com.github.yihtserns</groupId>
            <artifactId>camel-bluetooth</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>net.sf.bluecove</groupId>
                    <artifactId>bluecove</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.ow2.chameleon.commons.bluecove</groupId>
            <artifactId>bluecove</artifactId>
            <version>2.1.1-63</version>
            <exclusions>
                <exclusion>
                    <groupId>net.sf.bluecove</groupId>
                    <artifactId>bluecove</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        ...
    </dependencies>
    ...
</project>
```