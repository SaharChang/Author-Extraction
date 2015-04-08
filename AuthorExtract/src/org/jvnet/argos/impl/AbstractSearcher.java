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

import org.jvnet.argos.QueryParser;
import org.jvnet.argos.Searcher;

/**
 * @author Nick Lothian
 *
 */
public abstract class AbstractSearcher implements Searcher {
	private QueryParser queryParser = new PassthroughQueryParser();
    private String appKey;

    public String getAppKey() {
        return appKey;
    }
    

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }      
	
	/**
	 * @see org.jvnet.argos.Searcher#setQueryParser(org.jvnet.argos.QueryParser)
	 */
	public void setQueryParser(QueryParser queryParser) {
		this.queryParser = queryParser;

	}

	/**
	 * @see org.jvnet.argos.Searcher#getQueryParser()
	 */
	public QueryParser getQueryParser() {
		return queryParser;
	}

}
