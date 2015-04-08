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

/**
 * @author nicklothian
 *
 */
public class PassthroughQueryParser implements QueryParser {

	/**
	 * <p>Pass the query directly to the search engine</p>
	 * 
	 * @see org.jvnet.argos.QueryParser#parse(java.lang.String)
	 */
	public String parse(String inputQuery) {
		return inputQuery;
	}

}
