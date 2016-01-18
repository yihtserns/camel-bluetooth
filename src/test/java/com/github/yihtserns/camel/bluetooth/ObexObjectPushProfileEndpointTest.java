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
import com.github.yihtserns.camel.bluetooth.testutil.UseBluetoothEmulator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.bluetooth.LocalDevice;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.intel.bluetooth.EmulatorTestsHelper.useThreadLocalEmulator;
import java.util.ArrayList;
import java.util.List;
import javax.bluetooth.BluetoothStateException;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * @author yihtserns
 */
public class ObexObjectPushProfileEndpointTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public UseBluetoothEmulator btEmulator = new UseBluetoothEmulator();
    private List<CamelContext> camelContexts = new ArrayList<CamelContext>();

    @Before
    public void initBluetoothEmulatorForCurrentThread() throws Exception {
        useThreadLocalEmulator();
    }

    @After
    public void stopCamelContexts() {
        for (CamelContext camelContext : camelContexts) {
            try {
                camelContext.stop();
            } catch (Exception ex) {
                System.err.println(ex.toString());
                ex.printStackTrace(System.err);
            }
        }
    }

    @Test
    public void canSendAndReceiveViaBluetooth() throws Exception {
        final Container<String> serverBluetoothAddress = new Container<String>();
        DefaultCamelContext serverCamelContext = newCamelContext("Server");
        DefaultCamelContext clientCamelContext = newCamelContext("Client");

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
                                serverBluetoothAddress.set(LocalDevice.getLocalDevice().getBluetoothAddress());

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
        clientCamelContext.start();
        MockEndpoint mock = serverCamelContext.getEndpoint("mock:mock", MockEndpoint.class);

        String someBody = "Expected Body";

        mock.expectedBodiesReceived(someBody);
        clientCamelContext.createProducerTemplate().sendBody("bt:opp/" + serverBluetoothAddress.get(), someBody);
        mock.assertIsSatisfied();
    }

    @Test
    public void shouldThrowWhenCannotStartBluetoothConsumer() throws Exception {
        DefaultCamelContext serverCamelContext = newCamelContext("Server");
        serverCamelContext.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("bt:opp").to("mock:mock");
            }
        });

        try {
            serverCamelContext.start();
            fail("Should throw exception");
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            assertThat(cause, is(instanceOf(BluetoothStateException.class)));
            assertThat(cause.getMessage(), is("No BluetoothStack or Adapter for current thread"));
        }
    }

    private DefaultCamelContext newCamelContext(String name) {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setName(name);
        camelContext.disableJMX();

        camelContexts.add(camelContext);

        return camelContext;
    }

    private static final class Container<T> {

        private T obj;

        public void set(T obj) {
            this.obj = obj;
        }

        public T get() {
            return obj;
        }
    }
}
