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
package com.github.yihtserns.camel.bluetooth.testutil;

import com.intel.bluetooth.EmulatorTestsHelper;
import org.junit.rules.ExternalResource;

/**
 * @author yihtserns
 */
public class UseBluetoothEmulator extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        EmulatorTestsHelper.startInProcessServer();
    }

    @Override
    protected void after() {
        EmulatorTestsHelper.stopInProcessServer();
    }
}
