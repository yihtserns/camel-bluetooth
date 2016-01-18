/*
 * Copyright 2016 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.camel.bluetooth;

import com.github.yihtserns.camel.bluetooth.testutil.UseBluetoothEmulator;
import static com.intel.bluetooth.EmulatorTestsHelper.runNewEmulatorStack;
import org.junit.Test;
import org.junit.Rule;
import static com.intel.bluetooth.EmulatorTestsHelper.useThreadLocalEmulator;
import javax.bluetooth.RemoteDevice;
import org.apache.camel.impl.DefaultCamelContext;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author yihtserns
 */
public class ScanEndpointTest {

    @Rule
    public UseBluetoothEmulator btEmulator = new UseBluetoothEmulator();

    @Test
    public void canFindAllBluetoothDevices() throws Exception {
        Runnable doNothing = new Runnable() {

            public void run() {
            }
        };

        runNewEmulatorStack(doNothing);
        runNewEmulatorStack(doNothing);
        runNewEmulatorStack(doNothing);

        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.disableJMX();
        camelContext.start();

        useThreadLocalEmulator();
        RemoteDevice[] devices = camelContext.createProducerTemplate().requestBody("bt:scan", (Object) null, RemoteDevice[].class);

        int expectedBluetoothDeviceCount = 3;
        assertThat(devices, is(arrayWithSize(expectedBluetoothDeviceCount)));
    }
}
