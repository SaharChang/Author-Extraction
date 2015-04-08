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
package org.jvnet.argos.yahoo;

import java.math.BigInteger;
import java.util.Iterator;

import org.jvnet.argos.SearchResult;
import org.jvnet.argos.SearchServiceException;
import org.jvnet.argos.impl.AbstractAPISearchIterator;
import org.jvnet.argos.impl.StandardResultTypes;


import com.yahoo.search.SearchClient;
import com.yahoo.search.WebSearchRequest;
import com.yahoo.search.WebSearchResult;

public class WebSearchIterator extends AbstractAPISearchIterator {	
	public static final String SOURCE = "http://search.yahoo.com/";	
	
	public WebSearchIterator(String appKey, String query) {
		super(query);
		setApiKey(appKey);
		setStartFrom(1); // yahoo search index starts from 1
			
	}
	
	@Override
	protected String getDefaultSource() {		
		return SOURCE;
	}

	@Override
	protected String getDefaultResultType() {	
		return StandardResultTypes.RESULT_TYPE_WEBSEARCH;
	}		
	
	@Override
	protected synchronized Iterator<SearchResult> newSearchIterator() {
		try {
			SearchClient client = new SearchClient(getApiKey());
			WebSearchRequest request = new WebSearchRequest(getQuery());
			request.setResults(getNumPerPage());
			request.setStart(new BigInteger(Integer.toString(getStartFrom())));
			
			WebSearchResult[] elements = client.webSearch(request).listResults();
			
			checkIfFinalPage(elements);
			
			return processResults(elements);			
		} catch (Exception e) {
			throw new SearchServiceException(e);
		} 
	}

	@Override
	protected SearchResult processResult(Object result, int pageNum, int itemNum) {
		SearchResult searchResult = super.processResult(result, pageNum, itemNum);
		
		WebSearchResult entry = (WebSearchResult)result;		
		searchResult.setAddress(entry.getUrl());
		searchResult.setDescription(entry.getSummary());
		searchResult.setTitle(entry.getTitle());

		return searchResult;
	}
}
