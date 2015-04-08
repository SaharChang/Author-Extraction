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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jvnet.argos.SearchResult;
import org.jvnet.argos.SearchServiceException;




import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public abstract class AbstractSyndFormatSearchIterator extends AbstractSearchIterator {
	
	public AbstractSyndFormatSearchIterator(String query) {
		super(query);

		setEndpoint(getDefaultEndpoint());
		setSource(getDefaultSource());
		setResultType(getDefaultResultType());
	}
	
	protected abstract String getDefaultEndpoint();

	protected abstract String getDefaultSource();

	protected abstract String getDefaultResultType();

	protected abstract URL obtainURL() throws MalformedURLException;
	
	@Override
	protected Iterator<SearchResult> newSearchIterator() throws SearchServiceException {
		try {
			URL url = obtainURL();
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(url));
			
			List entriesList = feed.getEntries();			
			setFinalPage(entriesList.size() < getNumPerPage());			
			List<SearchResult> results = new LinkedList<SearchResult>();
			int itemNum = 1;
			for (Iterator iter = entriesList.iterator(); iter.hasNext();) {
				SyndEntry element = (SyndEntry) iter.next();
				
				SearchResult searchResult = new SearchResult();
				searchResult.setAddress(element.getLink());
				SyndContent content = element.getDescription();
				if (content != null) {
					searchResult.setDescription(content.getValue());
				} else {
					searchResult.setDescription("");
				}
				searchResult.setTitle(element.getTitle());
				searchResult.setResultType(getResultType());
				searchResult.setSource(getSource());
				searchResult.setPageNumber(getPageNum());
				searchResult.setResultNumberOnPage(itemNum);
				
				itemNum++;
				
				results.add(searchResult);
			}
			return results.iterator();
		} catch (Exception e) {
			throw new SearchServiceException(e);
		}		
	}
		
}
