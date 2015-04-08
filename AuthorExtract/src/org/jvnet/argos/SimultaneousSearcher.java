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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jvnet.argos.blogdigger.BlogdiggerWebSearcher;
import org.jvnet.argos.impl.AbstractSearcher;
import org.jvnet.argos.impl.SearcherCallable;
import org.jvnet.argos.msn.MSNWebSearcher;

public class SimultaneousSearcher extends AbstractSearcher {
	public static final String RESULT_TYPE = "MULTIPLE";

	private List<Searcher> searcherList;
	private int resultsLimit = 50;

	public SimultaneousSearcher(List<Searcher> searcherList) {
		this.searcherList = searcherList;
	}

	public SimultaneousSearcher(List<Searcher> searcherList, int resultsLimit) {
		this(searcherList);
		this.resultsLimit = resultsLimit;
	}

	protected int getResultsLimit() {
		return resultsLimit;
	}

	protected void setResultsLimit(int resultsLimit) {
		this.resultsLimit = resultsLimit;
	}

	public Iterator<SearchResult> search(String query) {
		Collection<SearchResult> col = searchCollection(query);
		return col.iterator();
	}

	public Collection<SearchResult> searchCollection(String query) {
		List<Callable<List<SearchResult>>> callableList = new LinkedList<Callable<List<SearchResult>>>();
		for (Searcher searcher : searcherList) {
			Callable<List<SearchResult>> callable = new SearcherCallable(
					searcher.getQueryParser().parse(query), searcher,
					resultsLimit);
			callableList.add(callable);
		}

		ExecutorService executor = Executors.newFixedThreadPool(callableList
				.size());
		try {
			List<Future<List<SearchResult>>> futureList = new LinkedList<Future<List<SearchResult>>>();
			for (Callable<List<SearchResult>> callable : callableList) {
				Future<List<SearchResult>> future = executor.submit(callable);
				futureList.add(future);
			}

			// wait for all searches to complete
			int numComplete = 0;
			while (numComplete < futureList.size()) {
				System.out.println("waiting.." + numComplete + " completed.");
				Thread.sleep(500);
				numComplete = 0;
				for (Future<java.util.List<org.jvnet.argos.SearchResult>> future : futureList) {
					if (future.isDone()) {
						numComplete++;
					}
				}
			}
			Map<String, SearchResult> resultsMap = Collections
					.synchronizedMap(new HashMap<String, SearchResult>());

			for (Future<java.util.List<org.jvnet.argos.SearchResult>> future : futureList) {

				List<SearchResult> resultsList = future.get();
				System.out.println(future.get().size());
				for (SearchResult result : resultsList) {
					String address = result.getAddress().trim();
					if (!resultsMap.containsKey(address)) {
						resultsMap.put(address, result);
					} else {
						SearchResult existingResult = resultsMap.get(address);
						int page = existingResult.getPageNumber();
						if (page > result.getPageNumber()) {
							existingResult
									.setPageNumber(result.getPageNumber());
						}
						int itemNum = existingResult.getResultNumberOnPage();
						if (itemNum > result.getResultNumberOnPage()) {
							existingResult.setResultNumberOnPage(result
									.getResultNumberOnPage());
						}
						existingResult.setResultType(RESULT_TYPE);
						existingResult.setSource(existingResult.getSource()
								+ ", " + result.getSource());
					}
				}
			}

			List<SearchResult> resultsList = new ArrayList<SearchResult>(
					resultsMap.values());

			Collections.sort(resultsList);

			return resultsList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SearchServiceException(e);

		} finally {
			executor.shutdown();
		}
	}

	public static void main(String[] args) {
		
		 List<Searcher> searcherList = new LinkedList<Searcher>();
		 searcherList.add(new MSNWebSearcher());
		 searcherList.add(new BlogdiggerWebSearcher());
		
		 SimultaneousSearcher searcher = new
		 SimultaneousSearcher(searcherList);
		
		 Collection<SearchResult> results = searcher.searchCollection("java");
		 System.out.println(results.size() + " items returned.");
		 for (SearchResult result : results) {
		 System.out.println(result.getAddress() + " page = " +
		 result.getPageNumber() + " item = " + result.getResultNumberOnPage()
		 + " source = " + result.getSource());
		 }
	}
}
