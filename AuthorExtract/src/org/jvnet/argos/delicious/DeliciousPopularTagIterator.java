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
package org.jvnet.argos.delicious;


public class DeliciousPopularTagIterator extends DeliciousRecentTagIterator {
	public static final String DEFAULT_ENDPOINT = "http://del.icio.us/rss/popular/"; 
	private static final String SOURCE = "http://del.ico.us/popular";
	private static final String RESULT_TYPE = "POPULAR BOOKMARKS";	
	
	public DeliciousPopularTagIterator(String query) {
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
	
}