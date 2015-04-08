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
package org.jvnet.argos;

/**
 * 
 * <p>Class which encapsulates a single search result from a search engine.</p>
 * 
 * @author nicklothian
 *
 */
public class SearchResult implements Comparable<SearchResult> {
	private String resultType;
	private String title;
	private String description;
	private String address;
	private String source;
	
	private int pageNumber;
	private int resultNumberOnPage;

	/**
	 * <p>source contains a URL representing the engine used for this search.</p>
	 * @return a URL as a string representing the search engine this came from.
	 */
	public String getSource() {
		return source;
	}
	

	public void setSource(String source) {
		this.source = source;
	}
	

	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * 
	 * <p>resultType allows search engines to distingush between
	 * different types of search results. For instance a search engine
	 * may allow web, image and usenet searches.</p>
	 * 
	 * @return a String representing the type of search result.
	 */
	public String getResultType() {
		return resultType;
	}
	
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String toString() {
		return this.getClass().getName() + " address: " + getAddress();
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getResultNumberOnPage() {
		return resultNumberOnPage;
	}

	public void setResultNumberOnPage(int resultNumberOnPage) {
		this.resultNumberOnPage = resultNumberOnPage;
	}


	public int compareTo(SearchResult other) {
		int result = this.getPageNumber() - other.getPageNumber();
		if (result == 0) {
			result = this.getResultNumberOnPage() - other.getResultNumberOnPage();
		}

		return result;
	}
	
}
