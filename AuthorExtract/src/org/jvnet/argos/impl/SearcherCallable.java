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
import java.util.concurrent.Callable;

import org.jvnet.argos.SearchResult;
import org.jvnet.argos.Searcher;


public class SearcherCallable implements Callable<List<SearchResult>> {
	private Searcher searcher;
	private String query;
	private int resultLimit;

	public SearcherCallable(String query, Searcher searcher, int resultLimit) {
		this.query = query;
		this.searcher = searcher;
		this.resultLimit = resultLimit;
	}
	
	public List<SearchResult> call() throws Exception {
		List<SearchResult> results = new LinkedList<SearchResult>();
		
		Iterator<SearchResult> iterator = searcher.search(query);
		int count = 0;
		while (iterator.hasNext()) {			
			SearchResult result = iterator.next();
			results.add(result);
			count++;
			if (count >= resultLimit) {
				break;
			}
		}
		return results;
	}


}
