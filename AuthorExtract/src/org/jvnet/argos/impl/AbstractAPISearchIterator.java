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
package org.jvnet.argos.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jvnet.argos.SearchResult;
import org.jvnet.argos.SearchServiceException;


/**
 * @author nlothian
 *
 */
public abstract class AbstractAPISearchIterator extends AbstractSearchIterator {
	private String apiKey;	

	public AbstractAPISearchIterator(String query) {
		super(query);	
		setSource(getDefaultSource());
		setResultType(getDefaultResultType());
	}		
		
	public String getApiKey() {
		return apiKey;
	}	

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	protected abstract String getDefaultSource();

	protected abstract String getDefaultResultType();
	
	
	/**
	 * @see org.jvnet.argos.impl.AbstractSearchIterator#newSearchIterator()
	 */
	@Override
	protected abstract Iterator<SearchResult> newSearchIterator() throws SearchServiceException;

	protected SearchResult processResult(Object result, int pageNum, int itemNum) {
		SearchResult searchResult = new SearchResult();		
		searchResult.setResultType(getResultType());
		searchResult.setSource(getSource());
		searchResult.setPageNumber(pageNum);
		searchResult.setResultNumberOnPage(itemNum);
		
		return searchResult; 
	}

	protected void checkIfFinalPage(Object[] elements) {
		setFinalPage(elements.length < getNumPerPage());
	}

	protected Iterator<SearchResult> processResults(Object[] elements) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		int elementNum = 1;
		for (Object element : elements) {			
			results.add(processResult(element, getPageNum(), elementNum));
			elementNum++;
		}
		return results.iterator();		
	}	
	
}
