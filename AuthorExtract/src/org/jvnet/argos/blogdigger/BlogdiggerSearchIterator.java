/**
 * Copyright 2005 Nick Lothian
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
package org.jvnet.argos.blogdigger;

import java.net.MalformedURLException;
import java.net.URL;

import org.jvnet.argos.impl.AbstractSyndFormatSearchIterator;

public class BlogdiggerSearchIterator extends AbstractSyndFormatSearchIterator {
	public static final String DEFAULT_ENDPOINT="http://www.blogdigger.com/rss.jsp";
	public static final String SOURCE = "http://www.blogdigger.com/";
	public static final String RESULT_TYPE = "BLOG";

	public BlogdiggerSearchIterator(String query) {
		super(query);
		setStartFrom(1); // blogdigger index starts from 1				
	}	

	@Override
	protected String getDefaultEndpoint() {
		return DEFAULT_ENDPOINT;
	}

	@Override
	protected String getDefaultSource() {		
		return SOURCE;
	}

	@Override
	protected String getDefaultResultType() {	
		return RESULT_TYPE;
	}	
	
	@Override
	protected URL obtainURL() throws MalformedURLException {
		return new URL(getEndpoint() + "?sortby=date&q=" + getQuery() + "&si=" + getStartFrom() + "&pp=" + getNumPerPage());		
	}

}