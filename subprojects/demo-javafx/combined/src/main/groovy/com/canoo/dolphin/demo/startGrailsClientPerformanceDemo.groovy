/*
 * Copyright 2012 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.canoo.dolphin.demo

import com.canoo.dolphin.LogConfig
import com.canoo.dolphin.core.client.ClientDolphin
import com.canoo.dolphin.core.client.ClientModelStore
import com.canoo.dolphin.core.client.comm.HttpClientConnector
import com.canoo.dolphin.core.client.comm.JavaFXUiThreadHandler
import com.canoo.dolphin.core.comm.JsonCodec

/*
start standalone via
gradlew demo-javafx-combined:run --stacktrace -PappProp=GrailsClientPerformance
*/

LogConfig.logCommunication()
def dolphin = new ClientDolphin()
dolphin.setClientModelStore(new ClientModelStore(dolphin))
def url = System.env.remote ?: "http://localhost:8080/dolphin-grails"
println "connecting to $url"
println "use -Dremote=... to override"
def connector = new HttpClientConnector(dolphin, url)
connector.codec = new JsonCodec()
connector.uiThreadHandler = new JavaFXUiThreadHandler()
dolphin.setClientConnector(connector)

PerformanceView.show(dolphin)
