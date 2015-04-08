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

/**
 * <p>Implement this interface to allow customization of queries to
 * optimize them for a specific search engine</p>
 * 
 * @author nicklothian
 */
public interface QueryParser {
	
	/**
	 * 
	 * @param inputQuery A query String
	 * @return A query as a String in the correct syntax for a specific search engine
	 */
	public String parse(String inputQuery);
}
