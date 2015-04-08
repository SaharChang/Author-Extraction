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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.jawin.COMException;
import org.jawin.win32.Registry;
import org.jawin.win32.RegistryConstants;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jvnet.argos.InvalidConfigurationException;
import org.jvnet.argos.SearchResult;
import org.jvnet.argos.SearchServiceException;
import org.jvnet.argos.impl.AbstractSearchIterator;
import org.jvnet.argos.impl.StandardResultTypes;
import org.xml.sax.InputSource;



public class DesktopSearchResultIterator extends AbstractSearchIterator  {
	public static final String DEFAULT_GOOGLE_REG_KEY = "Software\\Google\\Google Desktop\\API";
		
	private String endpoint;
	
	public DesktopSearchResultIterator(String query) {
		super(query);
		this.endpoint = findEndpoint();		
	}
	
	public DesktopSearchResultIterator(String searchTerm, int numPerPage) {
		this(searchTerm);
		this.setNumPerPage(numPerPage);		
	}
	
	private String findEndpoint() {
		try {
			int handle = Registry.OpenKey(RegistryConstants.HKEY_CURRENT_USER, DEFAULT_GOOGLE_REG_KEY);
			String end = Registry.QueryStringValue(handle, "search_url");
			if (end == null || "".equals(end)) {
				throw new InvalidConfigurationException("Could not establish search endpoing from value in registry key " + DEFAULT_GOOGLE_REG_KEY);
			} else {
				return end;
			}
		} catch (IOException e) {
			InvalidConfigurationException exception = new InvalidConfigurationException("Could not establish search endpoing from value in registry key " + DEFAULT_GOOGLE_REG_KEY);
			exception.initCause(exception);
			throw exception;
		} catch (COMException e) {
			InvalidConfigurationException exception = new InvalidConfigurationException("Could not establish search endpoing from value in registry key " + DEFAULT_GOOGLE_REG_KEY);
			exception.initCause(exception);
			throw exception;
		}
		
	}
	
	private SearchResult elementToResult(Element element) {		
		SearchResult result = new SearchResult();
		Element urlElement = element.getChild("url");
		if (urlElement != null) {
			result.setAddress(urlElement.getText());
		}
		Element snippetElement = element.getChild("snippet");
		if (snippetElement != null) {
			result.setDescription(urlElement.getText());
		}		
		Element categoryElement = element.getChild("category");
		if (categoryElement != null) {
			result.setResultType(categoryElement.getText());
		}		
		Element titleElement = element.getChild("title");
		if (titleElement != null) {
			result.setTitle(titleElement.getText());
		}	
		
		result.setResultType(StandardResultTypes.RESULT_TYPE_DESKTOPSEARCH);
		result.setSource("http://desktop.google.com/");
		
		return result;
	}	
	
	@Override
	protected synchronized Iterator<SearchResult> newSearchIterator() {
		try {
			URL url = new URL(endpoint +  getQuery() + "&format=xml&start=" + getStartFrom() + "&num=" + getNumPerPage() );				
			URLConnection connection = url.openConnection();
			connection.connect();
			
			InputSource inputSource = new InputSource(connection.getInputStream());
						
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(inputSource);
			
			org.jdom.xpath.XPath path = org.jdom.xpath.XPath.newInstance("/results/result");
			List elements = path.selectNodes(doc);
			
			int count = elements.size();
			setFinalPage(count < getNumPerPage());
			
			List<SearchResult> results = new LinkedList<SearchResult>();
			
			for (Iterator iter = elements.iterator(); iter.hasNext();) {
				Element element = (Element) iter.next();
				SearchResult searchResult = elementToResult(element);
				results.add(searchResult);
			}
			return results.iterator();
			
		} catch (Exception e) {		
			throw new SearchServiceException(e);
		} 
	}


}
