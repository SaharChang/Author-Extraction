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
import java.util.NoSuchElementException;

import org.jvnet.argos.SearchResult;
import org.jvnet.argos.SearchServiceException;




public abstract class AbstractSearchIterator implements Iterator<SearchResult> {
	public static final String UNSPECIFIED_SOURCE = "UNSPECIFIED";
	
	private boolean atEnd;
	private boolean finalPage;
	private int startFrom = 0;
	private int numPerPage = 20;
	private Iterator<SearchResult> currentPageIterator;
	private String query;
	private String endpoint;	
	private String source = UNSPECIFIED_SOURCE;		
	private String resultType = StandardResultTypes.RESULT_TYPE_UNSPECIFIED;

	private int pageNum = 1;
	
	public AbstractSearchIterator(String query) {
		super();
		setQuery(query);
	}
	
	protected int getPageNum() {
		return pageNum;
	}
	

	protected void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}	
	
	protected String getResultType() {
		return resultType;
	}	

	protected void setResultType(String resultType) {
		this.resultType = resultType;
	}
	
	protected String getEndpoint() {
		return endpoint;
	}
	

	protected void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}		
	
	protected String getSource() {
		return source;
	}

	protected void setSource(String source) {
		this.source = source;
	}	
	
	protected String getQuery() {
		return query;
	}
	

	protected void setQuery(String query) {
		this.query = query;
	}
	

	protected Iterator<SearchResult> getCurrentPageIterator() {
		return currentPageIterator;
	}

	protected void setCurrentPageIterator(Iterator<SearchResult> currentPageIterator) {
		this.currentPageIterator = currentPageIterator;
	}
	


	protected int getNumPerPage() {
		return numPerPage;
	}
	

	protected void setNumPerPage(int numPerPage) {
		this.numPerPage = numPerPage;
	}
	

	protected int getStartFrom() {
		return startFrom;
	}
	

	protected void setStartFrom(int startFrom) {
		this.startFrom = startFrom;
	}
	

	protected boolean isAtEnd() {
		return atEnd;
	}	

	protected void setAtEnd(boolean atEnd) {
		this.atEnd = atEnd;
	}
	

	protected boolean isFinalPage() {
		return finalPage;
	}
	

	protected void setFinalPage(boolean finalPage) {
		this.finalPage = finalPage;
	}
		
	protected abstract Iterator<SearchResult> newSearchIterator() throws SearchServiceException;
	
	protected Iterator<SearchResult> obtainCurrentPageIterator() throws SearchServiceException {
		if (currentPageIterator == null) {
			currentPageIterator = newSearchIterator();
		}
		return currentPageIterator;
	}
	
	public boolean hasNext() {	
		synchronized (this) {
			if (isAtEnd()) {
				return false;
			} else {
				Iterator<SearchResult> it = obtainCurrentPageIterator();
				if (!it.hasNext()) {
					if (finalPage) {
						setAtEnd(true);
						return false;
					} else {
						pageNum++;
						setStartFrom(getStartFrom() + getNumPerPage());
						currentPageIterator = null;
						it = obtainCurrentPageIterator();
						if (it.hasNext()) {
							return true;
						} else {
							setAtEnd(true);
							return false;
						}						
					}					
				} else {
					return true;
				}				
			}
		}
	}

	public SearchResult next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return obtainCurrentPageIterator().next();
	}

	public void remove() {
		throw new UnsupportedOperationException("Does not support the remove operation");
	}

	
}
