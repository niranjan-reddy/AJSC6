/*******************************************************************************
 *   BSD License
 *    
 *   Copyright (c) 2017, AT&T Intellectual Property.  All other rights reserved.
 *    
 *   Redistribution and use in source and binary forms, with or without modification, are permitted
 *   provided that the following conditions are met:
 *    
 *   1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *      and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *      conditions and the following disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. All advertising materials mentioning features or use of this software must display the
 *      following acknowledgement:  This product includes software developed by the AT&T.
 *   4. Neither the name of AT&T nor the names of its contributors may be used to endorse or
 *      promote products derived from this software without specific prior written permission.
 *    
 *   THIS SOFTWARE IS PROVIDED BY AT&T INTELLECTUAL PROPERTY ''AS IS'' AND ANY EXPRESS OR
 *   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 *   SHALL AT&T INTELLECTUAL PROPERTY BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 *   DAMAGE.
 *******************************************************************************/
package com.att.ajsc.common.interceptors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.Response.Status;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.servlet.ServletEndpoint;
import org.apache.camel.impl.DefaultMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.att.ajsc.common.Interceptor;
import com.att.ajsc.common.PreInterceptor4;
import com.att.ajsc.common.PreInterceptor5;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PreInterceptorErrorTest.configuration.class })
public class PreInterceptorErrorTest {

	@Autowired
	private ApplicationContext context;
	private com.att.ajsc.common.Exchange exchange;
	private MockEnvironment mockEnvironment;
	private Message inMessage;

	@Configuration
	static class configuration {

		@Bean(name = "interceptor5")
		public PreInterceptor5 getPreInterceptor5() {
			PreInterceptor5 interceptor5 = spy(new PreInterceptor5());
			return interceptor5;
		}

		@Bean(name = "interceptor4")
		public PreInterceptor4 getPreInterceptor4() {
			PreInterceptor4 interceptor4 = spy(new PreInterceptor4());
			return interceptor4;
		}

	}

	@Before
	public void init() {

		mockEnvironment = new MockEnvironment();
		inMessage = new DefaultMessage();
		Message outMessage = new DefaultMessage();
		exchange = new com.att.ajsc.common.Exchange();
		inMessage.setHeader(Exchange.HTTP_URI, "/path");
		exchange.setOut(outMessage);

	}

	@Test
	public void servletEndPointFailureTest() throws Exception {

		HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(new com.att.ajsc.common.Response());
		DefaultMessage message = (DefaultMessage) spy(inMessage);
		when(message.getBody(HttpServletResponse.class)).thenReturn((HttpServletResponse) wrapper);
		exchange.setIn(message);
		exchange.setFromEndpoint(new ServletEndpoint());
		exchange.getIn().setBody(wrapper, HttpServletResponse.class);

		Interceptor interceptor = new Interceptor();
		interceptor.setContext(context);
		interceptor.setEnvironment(mockEnvironment);
		interceptor.invokePreInterceptorChain(exchange);

		((PreInterceptor4) verify(context.getBean("interceptor4"), times(1))).allowOrReject(exchange);
		((PreInterceptor5) verify(context.getBean("interceptor5"), times(0))).allowOrReject(exchange);
		assertEquals(exchange.getOut().getHeader("CamelHttpResponseCode"), Status.ACCEPTED.getStatusCode());

	}

}
