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