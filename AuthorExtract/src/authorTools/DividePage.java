package authorTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html2.HTMLElement;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;

/**
 * Parcourir l'arbre DOM d'une page.
 * 
 * 
 * Diviser la page en block suivant les balise de séparation : div, hr ...
 * 
 * @author changuel
 * 
 */
public class DividePage {

	String adress;
	private String MIMEtype = null;
	private String charset = "ISO-8859-1";

	public DividePage(String pageAdress) {
		setAdress(pageAdress);

	}

	void connectAndparse() {
		String ligne;
		FileOutputStream out;
		Tidy tidy = new Tidy();

		URL url;
		InputStream in;

		try {
			/** Connexion à l'URL */
			url = new URL(adress);
			URLConnection urlConn = url.openConnection();
			urlConn.setConnectTimeout(5000);
			urlConn.setReadTimeout(5000);
			urlConn.connect();
			String type = urlConn.getContentType();

			if (type != null) {
				final String[] parts = type.split(";");
				setMIMEtype(parts[0].trim());
			}
			if (MIMEtype != null && MIMEtype.equals("text/html")) {
				in = urlConn.getInputStream();
				if (in != null) {

					/** Nettoyer le code html avec Jtidy */
					tidy.setXHTML(true);
					tidy.setShowWarnings(false);
					tidy.setQuiet(true);
					out = new FileOutputStream("testX.html");
					tidy.parse(in, out);

					/** rechercher la valeur de charset dans le fichier */
					BufferedReader lecteurAvecBuffer = new BufferedReader(
							new FileReader("testX.html"));
					while ((ligne = lecteurAvecBuffer.readLine()) != null) {
						int index = ligne.indexOf("charset=");
						if (index != -1) {
							if (ligne.trim().toLowerCase().startsWith("utf-8",
									index + 8))
								setCharset("utf-8");
							else
								setCharset("ISO-8859-1");
							break;
						}
					}
					lecteurAvecBuffer.close();
					parsePage();
				}
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException ");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	void parsePage() {
		String lang = "en";
		String TEST_URI = "http://clg-beaumarchais.scola.ac-paris.fr/MATH/BM_MATH.HTM";
		UserAgentContext uacontext = new SimpleUserAgentContext();
		DocumentBuilderImpl builder = new DocumentBuilderImpl(uacontext);
		long begin = System.currentTimeMillis();
		HTMLElement body;

		/** Récupération de l'arbre DOM */
		Reader reader;
		try {
			reader = new InputStreamReader(new FileInputStream("testX.html"),
					getCharset());
			InputSourceImpl inputSource = new InputSourceImpl(reader, TEST_URI);
			Document d = builder.parse(inputSource, begin);
			HTMLDocumentImpl document = (HTMLDocumentImpl) d;

			body = document.getBody();

			if (body != null) {
				/** Pour construire le corpus **/
				dividePage(body);

			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
		} catch (SAXException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	String allTaggedTxt = "";

	void dividePage(HTMLElement elem) {

		NodeList children = elem.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {

			Node e = children.item(i);
			Node parent = e.getParentNode();
			HTMLElementImpl elementParent = (HTMLElementImpl) parent;
			String nodeName = elementParent.getNodeName().trim().toLowerCase();

			if (e.getNodeName().equals("div"))
				System.out
						.println("***************************** nouveau bloc ");

			if (e.getNodeName().equals("hr"))
				System.out
						.println("***************************** nouvelle ligne ");

			if (e.getNodeType() == Node.TEXT_NODE && !nodeName.equals("script")) {
				String text = e.getTextContent().trim();
				while (text.startsWith("&nbsp;"))
					text = text.substring(6);

				boolean hiddenType = false;

				/**
				 * Hidden text
				 */
				if (elementParent.getAttribute("type") != null)
					if (elementParent.getAttribute("type").toLowerCase()
							.equals("hidden"))
						hiddenType = true;
				if (elementParent.getAttribute("style") != null) {
					if (elementParent.getAttribute("style").toLowerCase()
							.equals("display: none;")
							|| elementParent.getAttribute("style")
									.toLowerCase().equals("display: none"))
						hiddenType = true;
				}
				if (elementParent.getAncestor("div") != null) {
					HTMLElementImpl divElem = elementParent.getAncestor("div");
					if (divElem.getAttribute("style") != null) {
						if (divElem.getAttribute("style").toLowerCase().equals(
								"display: none;")) {
							hiddenType = true;
						}
					}
				}

				// System.out.println("<" + nodeName + ">");

				// if (text.equals(""))
				// System.out.println("<" + nodeName);
				if ((nodeName.equals("tr") || nodeName.equals("br"))
						|| nodeName.equals("br/") || nodeName.equals("p")) {
					System.out.println();
					System.out.println("-------------------");
				}

				if (!text.equals("") && text.length() > 1
						&& !nodeName.equals("option")
						&& !nodeName.equals("script")
						&& !nodeName.equals("style")
						&& !nodeName.equals("form") && !nodeName.equals("img")
						&& hiddenType == false && !text.startsWith("<!--")
						&& !text.startsWith("function")
						&& !text.startsWith("&gt;")) {

					String cleanedText = nettoyeText(text);
					System.out.print("--" + cleanedText);

					/**
					 * tag avec POS et extraire les entités nommées
					 */

				}
			}
			NodeList liste = e.getChildNodes();
			if (liste.getLength() > 0) {
				dividePage((HTMLElementImpl) e);
			}
		}
	}

	/**
	 * remplacer les code d'ecriture html par leurs équivalents en texte
	 * 
	 * @param text
	 * @return
	 */
	String nettoyeText(String text) {

		text = text.replaceAll("Ã©", "é");
		text = text.replaceAll("â€™", "'");
		text = text.replaceAll("Ã¨", "è");
		text = text.replaceAll("Ã", "à");
		text = text.replaceAll("Â©", "©");
		text = text.replaceAll("â€“", "-");
		text = text.replaceAll("à¼", "ü");
		text = text.replaceAll("à¼", "ü");
		text = text.replaceAll("à¤", "ä");
		text = text.replaceAll("Å“", "oe");
		text = text.replaceAll("\\s", " ");

		return text;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public String getMIMEtype() {
		return MIMEtype;
	}

	public void setMIMEtype(String etype) {
		MIMEtype = etype;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getAllTaggedTxt() {
		return allTaggedTxt;
	}

	public void setAllTaggedTxt(String allTaggedTxt) {
		this.allTaggedTxt = allTaggedTxt;
	}

}
