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
package org.jvnet.argos.feedster;

import java.net.MalformedURLException;
import java.net.URL;

import org.jvnet.argos.impl.AbstractSyndFormatSearchIterator;



public class FeedsterSearchIterator extends AbstractSyndFormatSearchIterator {
	public static String DEFAULT_ENDPOINT = "http://feedster.com/search.php";
	private static final String SOURCE = "http://www.feedster.com/";
	private static final String RESULT_TYPE = "BLOGS";		
	
	public FeedsterSearchIterator(String query) {
		super(query);
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
		return new URL(getEndpoint() + "?q=" + getQuery() + "&sort=date&ie=UTF-8&type=rss&offset=" + getStartFrom() + "&limit=" + getNumPerPage());
	}
}
