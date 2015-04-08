/**
 * 
 * Based on Dave Johnson's Technorati wrapper, used under the Apache Licence.
 * 
 * See http://www.rollerweblogger.org/page/roller/20030517#technoratj
 */
package org.jvnet.argos.technorati;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** 
 * Simple Technorati wrapper for Java. 
 * 
 * @author David M Johnson
 * @author Nick Lothian 
 * 
 * */
public class TechnoratiWrapper {
    private String mKey = null;

    public TechnoratiWrapper(String key) {
        mKey = key;
    }

    /** Looks for key in classpath using "/technorati.license" */
    public TechnoratiWrapper() throws Exception {
        InputStream is = getClass().getResourceAsStream("/technorati.license");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        mKey = br.readLine();
    }
    
    public TechnoratiResult getSearch(String query, int start, int limit) throws Exception {
        return new TechnoratiResult("http://api.technorati.com/search",query,null, start, limit, true);
    }    

    public TechnoratiResult getLinkCosmos(String url, int start, int limit) throws Exception {
        return new TechnoratiResult("http://api.technorati.com/cosmos",url,"links", start, limit, false);
    }
    
    
    public TechnoratiResult getWeblogCosmos(String url, int start, int limit) throws Exception {
        return new TechnoratiResult("http://api.technorati.com/cosmos",url,"weblog", start, limit, false);
    }
    

    public TechnoratiResult getBloginfo(String url) throws Exception {
        return new TechnoratiResult("http://api.technorati.com/bloginfo",url,null);
    }

    public TechnoratiResult getOutbound(String url, int start, int limit) throws Exception {
        return new TechnoratiResult("http://api.technorati.com/outbound",url,null, start, limit, false);
    }

    /** Technorati result with header info and collection of weblog items */
    public class TechnoratiResult {
        private TechnoratiWeblog mWeblog = null;
        private Collection<TechnoratiWeblog> mWeblogs = new ArrayList<TechnoratiWeblog>();

        protected TechnoratiResult(String apiUrl, String url, String type) throws Exception {
            this(apiUrl, url, type, 0, 20, false);
        }
        
        protected TechnoratiResult(String apiUrl, String urlOrQuery, String type, int start, int limit, boolean isSearch) throws Exception {
            Map<String, Object> args = new HashMap<String, Object>();
            if (isSearch) {
                args.put("query", urlOrQuery);
            } else {
                args.put("url", urlOrQuery);
            }
            
            args.put("type", type);
            args.put("format", "xml");
            args.put("key", mKey);
            args.put("start",new Integer(start));
            args.put("limit",new Integer(limit));
            
            XPath itemsPath = XPath.newInstance("/tapi/document/item/weblog");

            Document doc = getRawResults(apiUrl,args);
            Element elem = doc.getRootElement();
            String error = getString(elem,"/tapi/document/result/error");
            if ( error != null ) throw new Exception(error);
            if (mWeblog == null) {
                XPath p = XPath.newInstance("/tapi/document/result/weblog");
                Element w = (Element) p.selectSingleNode(doc);
                mWeblog = new TechnoratiWeblog(w);
            }
            int count=0;
            Iterator iter = itemsPath.selectNodes(doc).iterator();
            while (iter.hasNext()) {
                Element element = (Element) iter.next();
                TechnoratiWeblog w = new TechnoratiWeblog(element);
                mWeblogs.add(w);
                count++;
            }            
        }
        

        public TechnoratiWeblog getWeblog() {return mWeblog;}
        public Collection<TechnoratiWeblog> getWeblogs() {return mWeblogs;}
    }

    /** Technorati weblog representation */
    public class TechnoratiWeblog {
        private String mName = null;
        private String mUrl = null;
        private String mRssurl = null;
        private String mLastupdate = null;
        private String mNearestpermalink = null;
        private String mExcerpt = null;
        private int mInboundlinks = 0;
        private int mInboundblogs = 0;

        public TechnoratiWeblog(Element elem) throws Exception {
            mName = getString(elem,"name");
            mUrl = getString(elem,"url");
            mRssurl = getString(elem,"rssurl");
            mLastupdate = getString(elem,"lastupdate");
            mNearestpermalink = getString(elem,"nearestpermalink");
            mExcerpt = getString(elem,"excerpt");
            mInboundlinks = getInt(elem,"inboundlinks");
            mInboundblogs = getInt(elem,"inboundblogs");
        }

        public String getName() {return mName;}
        public String getUrl() {return mUrl;}
        public String getRssurl() {return mRssurl;}
        public int getInboundblogs() {return mInboundblogs;}
        public int getInboundlinks() {return mInboundlinks;}
        public String getLastupdate() {return mLastupdate;}
        public String getNearestpermalink() {return mNearestpermalink;}
        public String getExcerpt() {return mExcerpt;}
    }

    public class TechnoratiEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
            return new InputSource(getClass().getResourceAsStream("tapi-002.xml"));
        }
    }

    protected Document getRawResults(String urlString, Map args) throws Exception {
        int count = 0;
        Iterator keys = args.keySet().iterator();
        while (keys.hasNext()) {
            String sep = count++==0 ? "?" : "&";
            String name = (String)keys.next();
            if ( args.get(name) != null ) {
                urlString += sep + name + "=" + args.get(name);
            }
        }
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.connect();        
        SAXBuilder builder = new SAXBuilder();
        
        builder.setEntityResolver(new TechnoratiEntityResolver());
                
        InputStream in =  new BufferedInputStream(conn.getInputStream());
        
        return builder.build(in);
    }

    protected String getString(Element elem, String path) throws Exception {
        XPath xpath = XPath.newInstance(path);
        Element e = (Element)xpath.selectSingleNode(elem);
        return e!=null ? e.getText() : null;
    }

    protected int getInt(Element elem, String path) throws Exception {
        XPath xpath = XPath.newInstance(path);
        Element e = (Element)xpath.selectSingleNode(elem);
        if (e != null) {
            try {
                return Integer.parseInt(e.getTextTrim());
            } catch (NumberFormatException e1) {
                //System.err.println("Error parsing " + e.getTextTrim() + " as an int");                
                return 0;
            }
        } else {
            return 0;
        }
        
    }
}
