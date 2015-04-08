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

import org.jvnet.argos.msn.MSNWebSearcher;


public class CommandLineSearcher {

	public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java " + CommandLineSearcher.class.getName() + " query");
            System.exit(-1);
        }
        
		try {
            long start = System.currentTimeMillis();
            Searcher searcher = new MSNWebSearcher();
            Iterator<SearchResult> it = searcher.search(args[0]);
            int count = 0;
            while (it.hasNext()) {
            	SearchResult result = it.next();
            	count++;
            	if (count % 500 == 0) {
            		System.out.println(count);
            	}
            	System.out.println(result.getTitle() + " Address: " + result.getAddress());
            }
            long end = System.currentTimeMillis();
            System.out.println(count + " items in " + (end - start) + " ms");
        } catch (SearchServiceException e) {
            e.printStackTrace();
        }
	}

}
