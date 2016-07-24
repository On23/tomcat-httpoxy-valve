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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class PoxyValveTest {
	
	private PoxyValve proxyValve;
	private InvokedValve invokedValve;
	private Request req;
	private Response resp;
	
	@Before
	public void setup() {
		proxyValve = new PoxyValve();
		invokedValve = new InvokedValve();
		
		proxyValve.setNext(invokedValve);

		req = mock(Request.class);
		resp = mock(Response.class);
	}

	@Test
	public void blockAccessWithProxyHeader() throws Exception {
		doRequestWithProxyHeader("/manager/index.html", "127.0.0.1", proxyValve);
		verify(resp).sendError(400);
		assertFalse("Should not allow access with Proxy Http Header", invokedValve.wasInvoked());
	}
	@Test
	public void allowAccessWithoutProxyHeader() throws Exception {
		doRequest("/manager/index.html", "127.0.0.1", proxyValve);
		assertTrue("Should allow access without Proxy Http Header", invokedValve.wasInvoked());
	}

	private void doRequest(String path, String remoteAddr, Valve valveToTestWith) throws Exception {
		when(req.getRemoteAddr()).thenReturn(remoteAddr);
		when(req.getRequestURI()).thenReturn(path);
		valveToTestWith.invoke(req, resp);
	}
	private void doRequestWithProxyHeader(String path, String remoteAddr, Valve valveToTestWith) throws Exception {
		when(req.getRemoteAddr()).thenReturn(remoteAddr);
		when(req.getRequestURI()).thenReturn(path);
		when(req.getHeader("Proxy")).thenReturn("yandex.ru");
		valveToTestWith.invoke(req, resp);
	}
	
	
	private class InvokedValve extends ValveBase {

		private boolean invoked = false;
		
		public boolean wasInvoked() {
			return invoked;
		}
		
		@Override
		public void invoke(Request arg0, Response arg1) throws IOException,
				ServletException {
			invoked = true;
		}
		
	}
}
