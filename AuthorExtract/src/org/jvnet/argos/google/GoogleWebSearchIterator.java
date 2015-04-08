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
package org.jvnet.argos.google;

import java.util.Iterator;

import org.jvnet.argos.SearchResult;
import org.jvnet.argos.SearchServiceException;
import org.jvnet.argos.impl.AbstractAPISearchIterator;
import org.jvnet.argos.impl.StandardResultTypes;


import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResultElement;

public class GoogleWebSearchIterator extends AbstractAPISearchIterator  {
	public static final String SOURCE = "http://www.google.com/";

	public GoogleWebSearchIterator(String apiKey, String query) {
		super(query);
		setApiKey(apiKey);
		setNumPerPage(10); // 10 is the max for google
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
	protected Iterator<SearchResult> newSearchIterator() {
		GoogleSearch s = new GoogleSearch();
		s.setKey(getApiKey());
		s.setQueryString(getQuery());
		s.setStartResult(getStartFrom());
		s.setMaxResults(getNumPerPage());
		s.setFilter(false);
		
		try {
			GoogleSearchResultElement[] elements = s.doSearch().getResultElements();
			
			checkIfFinalPage(elements);			

			return processResults(elements);
		} catch (GoogleSearchFault e) {
			throw new SearchServiceException(e);
		}
	}

	@Override
	protected SearchResult processResult(Object result, int pageNum, int itemNum) {
		SearchResult searchResult = super.processResult(result, pageNum, itemNum);
		GoogleSearchResultElement element = (GoogleSearchResultElement)result;
		searchResult.setAddress(element.getURL());
		searchResult.setDescription(element.getSnippet());
		searchResult.setTitle(element.getTitle());
		
		return searchResult;		
	}
	
	
}
