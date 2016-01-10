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

import com.github.yihtserns.camel.bluetooth.testutil.DelegatingExecutorService;
import com.github.yihtserns.camel.bluetooth.testutil.DelegatingExecutorServiceStrategy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.bluetooth.LocalDevice;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.intel.bluetooth.EmulatorTestsHelper.startInProcessServer;
import static com.intel.bluetooth.EmulatorTestsHelper.stopInProcessServer;
import static com.intel.bluetooth.EmulatorTestsHelper.useThreadLocalEmulator;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @author yihtserns
 */
public class ObexObjectPushProfileEndpointTest {

    private DefaultCamelContext serverCamelContext = newCamelContext("Server");
    private String serverBluetoothAddress;

    @Before
    public void startServers() throws Exception {
        startInProcessServer();
        startServerCamelContext();
        useThreadLocalEmulator();
    }

    private void startServerCamelContext() throws Exception {
        serverCamelContext.setExecutorServiceStrategy(new DelegatingExecutorServiceStrategy(serverCamelContext.getExecutorServiceStrategy()) {

            @Override
            public ExecutorService newSingleThreadExecutor(Object source, String name) {
                return new DelegatingExecutorService(super.newSingleThreadExecutor(source, name)) {

                    @Override
                    public <T> Future<T> submit(final Callable<T> callable) {
                        return super.submit(new Callable<T>() {

                            @Override
                            public T call() throws Exception {
                                useThreadLocalEmulator();
                                serverBluetoothAddress = LocalDevice.getLocalDevice().getBluetoothAddress();

                                return callable.call();
                            }
                        });
                    }
                };
            }
        });

        serverCamelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("bt:opp").transform().body(String.class).to("mock:mock");
            }
        });
        serverCamelContext.start();
    }

    @After
    public void stopServers() throws Exception {
        try {
            serverCamelContext.stop();
        } finally {
            stopInProcessServer();
        }
    }

    @Test
    public void canSendAndReceiveViaBluetooth() throws Exception {
        DefaultCamelContext clientCamelContext = newCamelContext("Client");
        clientCamelContext.start();

        try {
            String someBody = "Expected Body";

            MockEndpoint mock = serverCamelContext.getEndpoint("mock:mock", MockEndpoint.class);
            mock.expectedBodiesReceived(someBody);

            clientCamelContext.createProducerTemplate().sendBody("bt:opp/" + serverBluetoothAddress, someBody);

            mock.assertIsSatisfied();
        } finally {
            clientCamelContext.stop();
        }
    }

    private static DefaultCamelContext newCamelContext(String name) {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setName(name);
        camelContext.disableJMX();

        return camelContext;
    }
}
