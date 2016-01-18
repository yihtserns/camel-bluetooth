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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultProducer;

/**
 *
 * @author yihtserns
 */
public class ScanEndpoint extends DefaultEndpoint {

    public ScanEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    public Producer createProducer() throws Exception {
        return new DefaultProducer(this) {

            public void process(Exchange exchange) throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                final List<RemoteDevice> devices = new ArrayList<RemoteDevice>();

                DiscoveryListener collectDevices = new DiscoveryListener() {

                    public void deviceDiscovered(RemoteDevice rd, DeviceClass dc) {
                        devices.add(rd);
                    }

                    public void inquiryCompleted(int i) {
                        latch.countDown();
                    }

                    public void servicesDiscovered(int i, ServiceRecord[] srs) {
                    }

                    public void serviceSearchCompleted(int i, int i1) {
                    }
                };

                DiscoveryAgent discoveryAgent = LocalDevice.getLocalDevice().getDiscoveryAgent();

                boolean started = discoveryAgent.startInquiry(DiscoveryAgent.GIAC, collectDevices);
                if (!started) {
                    throw new IllegalStateException("Unable to start scanning for devices");
                }

                boolean completed = latch.await(20000, TimeUnit.MILLISECONDS);
                if (!completed) {
                    discoveryAgent.cancelInquiry(collectDevices);
                }

                exchange.getIn().setBody(devices);
            }
        };
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isSingleton() {
        return true;
    }
}
