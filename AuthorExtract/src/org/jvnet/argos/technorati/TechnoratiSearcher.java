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

import java.util.Iterator;

import org.jvnet.argos.InvalidConfigurationException;
import org.jvnet.argos.SearchResult;
import org.jvnet.argos.impl.AbstractSearcher;

/**
 * @author Nick Lothian
 *
 */
public class TechnoratiSearcher extends AbstractSearcher {
    //public static enum SearchType {CosmosQuery, WeblogCosmosQuery, SearchQuery, GetInfoQuery, OutboundQuery, BlogInfoQuery};
    public static enum SearchType {CosmosQuery, WeblogCosmosQuery, OutboundQuery, SearchQuery};

    private SearchType searchType;
    private int numPerPage = 100;
    
    public int getNumPerPage() {
        return numPerPage;
    }
    


    public void setNumPerPage(int numPerPage) {
        this.numPerPage = numPerPage;
    }
    


    public SearchType getSearchType() {
        return searchType;
    }
    

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }
    

    public TechnoratiSearcher(String appKey, SearchType st) {
        setAppKey(appKey);
        setSearchType(st);
    }

    public TechnoratiSearcher(String appKey, SearchType st, int numPerPage) {
       this(appKey, st);
       setNumPerPage(numPerPage);
    }    
    
    public Iterator<SearchResult> search(String query) {
        if (getAppKey() == null) {
            throw new InvalidConfigurationException("No application key supplied");
        }        
        return new TechnoratiSearchIterator(getAppKey(), getQueryParser().parse(query), getNumPerPage(), getSearchType());
    }    
}
