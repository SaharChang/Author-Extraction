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
package org.jvnet.argos.technorati;

import java.util.Collection;
import java.util.Iterator;

import org.jvnet.argos.SearchResult;
import org.jvnet.argos.SearchServiceException;
import org.jvnet.argos.impl.AbstractAPISearchIterator;
import org.jvnet.argos.impl.StandardResultTypes;

/**
 * @author Nick Lothian
 *
 */
public class TechnoratiSearchIterator extends AbstractAPISearchIterator  {
    public static final String SOURCE = "http://www.technorati.com/";
    
    private TechnoratiSearcher.SearchType searchType;

    public TechnoratiSearcher.SearchType getSearchType() {
        return searchType;
    }
    

    public void setSearchType(TechnoratiSearcher.SearchType searchType) {
        this.searchType = searchType;
    }    

    public TechnoratiSearchIterator(String apiKey, String query, int numPerPage, TechnoratiSearcher.SearchType st) {
        super(query);
        setApiKey(apiKey);
        setSearchType(st); 
        setNumPerPage(numPerPage);
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
    protected Iterator<SearchResult> newSearchIterator() throws SearchServiceException {
        TechnoratiWrapper wrapper = new TechnoratiWrapper(getApiKey());
        try {
            TechnoratiWrapper.TechnoratiResult techResult = null;
            if (searchType == TechnoratiSearcher.SearchType.CosmosQuery) {
                techResult = wrapper.getLinkCosmos(getQuery(), getStartFrom(), getNumPerPage());
            } else if (searchType == TechnoratiSearcher.SearchType.WeblogCosmosQuery) {
                techResult = wrapper.getWeblogCosmos(getQuery(), getStartFrom(), getNumPerPage());
            } else if (searchType == TechnoratiSearcher.SearchType.OutboundQuery) {
                techResult = wrapper.getOutbound(getQuery(), getStartFrom(), getNumPerPage());
            } else if (searchType == TechnoratiSearcher.SearchType.SearchQuery) {
                techResult = wrapper.getSearch(getQuery(), getStartFrom(), getNumPerPage());                
            } else {                
                throw new UnsupportedOperationException("Search type unimplemented");
            }
            Collection<TechnoratiWrapper.TechnoratiWeblog> rawResults = techResult.getWeblogs();
            TechnoratiWrapper.TechnoratiWeblog[] elements = (TechnoratiWrapper.TechnoratiWeblog[]) rawResults.toArray(new TechnoratiWrapper.TechnoratiWeblog[rawResults.size()]);
            checkIfFinalPage(elements);
            
            return processResults(elements);
            
        } catch (Exception e) {
            SearchServiceException sse = new SearchServiceException(e);
            //sse.initCause(e);
            throw sse;
        }
        
    }

    @Override
    protected SearchResult processResult(Object result, int pageNum, int itemNum) {
        SearchResult searchResult = super.processResult(result, pageNum, itemNum);
        TechnoratiWrapper.TechnoratiWeblog element = (TechnoratiWrapper.TechnoratiWeblog)result;
                
        String url = element.getNearestpermalink();
        if (url == null || "".equals(url.trim())) {
            url = element.getUrl();
        }
        searchResult.setAddress(url);
        
        searchResult.setDescription(element.getExcerpt());
        searchResult.setTitle(element.getName());
        
        return searchResult;        
    }

}
