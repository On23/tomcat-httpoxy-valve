/*
 * Copyright 2010 Leonard Axelsson
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
 * 
 */
package ru.on23.tomcat.valve.httproxy;

import java.io.IOException;
import javax.servlet.ServletException;

import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 *
 * See https://www.apache.org/security/asf-httpoxy-response.txt
 *
 * Add a global valve to reject requests with PROXY header, create a
 * PoxyValve.java with below content, compile it and put it in a jar and
 * put the jar in the lib installation of your tomcat. Add the line
 * <code>&lt;Valve className="PoxyValve" /&gt;</code> in conf/server.xml
 * (like after the AccessLogValve) and restart Tomcat
 *
 */
public class PoxyValve extends ValveBase {

    //private Logger logger = Logger.getLogger(this.getClass().getName());

    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        String poxy = request.getHeader("Proxy");
        if (poxy != null) {
            response.sendError(400);
            return;
        }
        getNext().invoke(request, response);
    }

}
