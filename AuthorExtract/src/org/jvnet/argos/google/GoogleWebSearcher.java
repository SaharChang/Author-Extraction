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

import java.util.Iterator;

import org.jvnet.argos.InvalidConfigurationException;
import org.jvnet.argos.SearchResult;
import org.jvnet.argos.impl.AbstractSearcher;



/**
 * <p>Search the web using the <a href="http://www.google.com/apis/">Google APIs</a></p>
 * 
 * @author nicklothian
 */
public class GoogleWebSearcher extends AbstractSearcher {	

	public GoogleWebSearcher() {

	}

	public GoogleWebSearcher(String appKey) {
        setAppKey(appKey);
	}

	public Iterator<SearchResult> search(String query) {
		if (getAppKey() == null) {
			throw new InvalidConfigurationException("No application key supplied");
		}
		return new GoogleWebSearchIterator(getAppKey(), getQueryParser().parse(query));
	}

}
