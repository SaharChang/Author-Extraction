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

import java.util.Iterator;

/**
 * 
 * Interface to a search engine.
 * 
 * <p>Use the {@link search(String) search} method to run a query against a search
 * engine. Implementations may use a {@link org.jvnet.argos.QueryParser QueryParser} 
 * to optimize the query for a particular search engine.</p>
 * 
 * <p>Note that the Iterator returned by the {@link search(String) search} 
 * method may throw a 
 * {@link org.jvnet.argos.SearchServiceException SearchServiceException}</p>
 * 
 * @author Nick Lothian
 */
public interface Searcher {
	public void setQueryParser(QueryParser queryParser);
	public QueryParser getQueryParser();
	
    /**
     * Run a query against a search engine and return results. 
     * 
     * <p>Note that the returned iterator may throw a 
     * {@link org.jvnet.argos.SearchServiceException SearchServiceException}</p>
     * 
     * @param query A query to run. This query may be processed by a {@link org.jvnet.argos.QueryParser QueryParser} 
     * to optimize it for a particular search engine.
     * @return a {@link java.util.Iterator Iterator} of {@link org.jvnet.argos.SearchResult SearchResults}
     * @throws org.jvnet.argos.SearchServiceException if a search or network error occurs
     */
	public Iterator<SearchResult> search(String query);
}
