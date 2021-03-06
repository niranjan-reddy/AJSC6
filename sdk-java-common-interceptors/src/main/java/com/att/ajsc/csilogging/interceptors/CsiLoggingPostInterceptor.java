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
package com.att.ajsc.csilogging.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.ajsc.common.AjscPostInterceptor;
import com.att.ajsc.csilogging.common.CSILoggingJerseyUtils;
import com.att.ajsc.csilogging.common.CSILoggingUtils;
import com.att.ajsc.csilogging.common.QueueConnector;
import com.att.ajsc.csilogging.util.CommonNames;
import com.att.ajsc.logging.AjscEelfManager;
import com.att.ajsc.methodmapper.common.MethodMapperConstants;
import com.att.eelf.configuration.EELFLogger;

public class CsiLoggingPostInterceptor extends AjscPostInterceptor {

	static final EELFLogger logger = AjscEelfManager.getInstance().getLogger(CsiLoggingPostInterceptor.class);

	public CsiLoggingPostInterceptor() {
		setPosition(Integer.MAX_VALUE - 3);
	}

	private HttpServletRequest request;
	private HttpServletResponse response;

	@Autowired
	private QueueConnector connector;
	@Autowired
	private CSILoggingUtils csiLoggingUtils;

	@Override
	public boolean allowOrReject(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

		request = (HttpServletRequest) (requestContext.getProperty(CommonNames.REQUEST));
		response = (HttpServletResponse) (requestContext.getProperty(CommonNames.RESPONSE));

		request.setAttribute(CommonNames.RESPONSE_ENTITY, responseContext.getEntity());
		CSILoggingJerseyUtils.setRequestAttributesFromResponseContext(responseContext, request);
		try {
			csiLoggingUtils.handleResponse(request, response, connector);
		}

		catch (Exception e) {
			logger.error("Error calling CsiLoggingCamelPostInterceptor: ", e);
		}

		return true;
	}

}