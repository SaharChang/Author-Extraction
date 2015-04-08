package authorExtract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.ParserException;
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

import ca.uottawa.balie.Balie;
import ca.uottawa.balie.DisambiguationRulesNerf;
import ca.uottawa.balie.LexiconOnDisk;
import ca.uottawa.balie.LexiconOnDiskI;
import ca.uottawa.balie.NamedEntityRecognitionNerf;
import ca.uottawa.balie.NamedEntityTypeEnumMappingNerf;
import ca.uottawa.balie.PriorCorrectionNerf;
import ca.uottawa.balie.TokenList;
import ca.uottawa.balie.Tokenizer;

/**
 * Classe Pricipale: permet de construire les descripteurs des personnes
 * trouvées dans une page WEB.
 * 
 * Les noms de personnes sont extraits à partir d'une liste des noms.<br>
 * 
 * Classe créée pour parser les pages de facon DOM. <br>
 * 
 * La librairie Balie est utilisé pour extraires les noms des organizations.<br>
 * 
 * Aggréger les attributs d'une meme personne dans la page<br>
 * 
 * Cette classe contient une autre classe personne.
 * 
 * 
 * @author changuel
 * 
 */

public class DicoDomParse {
	private String MIMEtype = null;
	private String charset = "ISO-8859-1";

	String adress;
	String pageAuthor;

	int id = 0;
	int numLigne = 0;
	float maxAvg;
	boolean hasAuthor;
	int preceedHr = 0;
	boolean authorPageFound;
	float minRelatifNumLine = (float) 1.0;
	float maxRelatifNumLine = (float) 0.0;
	float maxAvgAuthor = (float) 0.0;
	boolean hasTopEndAuthor;
	int nbHasTopEndAuthor = 0;
	int nbAuthorsByPage;
	int smallestSize = 1000;
	static int countNbAuthorSup1 = 0;
	int nbMetaAuthor = 0;
	int nbPageWithAuthor = 0;
	int nbPageWithoutAuthor = 0;

	final int windowSize = 15;
	final static float topPosition = (float) 0.2;
	final static float endPosition = (float) 0.8;

	String dicoFile = "dico";
	String attribFile = "authorsAttrib";
	final static String personFileName = "authorsAttrib";// "googleAttribs";//

	ArrayList<TextNode> listAuthorNodes = new ArrayList<TextNode>();
	ArrayList<String> dicoList;
	public List<String> fileNameList;
	public List<String> authorList;
	List<String> allPersonList;

	List<String> createdByList = new ArrayList<String>(Arrays
			.asList(AuthorTools.createdByPatterns));
	List<String> authorPatternsList = new ArrayList<String>(Arrays
			.asList(AuthorTools.authorPatterns));

	List<String> blockSeparationList = new ArrayList<String>(Arrays
			.asList(AuthorTools.blockSeparation));
	List<String> falsePersonList = new ArrayList<String>(Arrays
			.asList(AuthorTools.falsePerson));
	Map<String, List<Person>> personMap;

	ArrayList<String> maleFirstList = new ArrayList<String>();
	ArrayList<String> femaleFirstList = new ArrayList<String>();
	ArrayList<String> lastList = new ArrayList<String>();
	String femaleFirstFile = "person//femaleFirst";
	String maleFirstFile = "person//maleFirst";
	String lastNamesFile = "person//lastAll";

	String pageAuthorFound;
	// BALIE NE
	Tokenizer tokenizer;
	LexiconOnDiskI lexicon;
	DisambiguationRulesNerf disambiguationRules;
	boolean pTag = false;

	public DicoDomParse() {
		super();
		// BALIE
		tokenizer = new Tokenizer(Balie.LANGUAGE_ENGLISH, true);
		lexicon = new LexiconOnDisk(LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON);
		disambiguationRules = DisambiguationRulesNerf.Load();
	}

	/**
	 * parcourir fileNameList et authorList, extraire le nom de chaque fichier
	 * et son auteur. Traiter chaque fichier en lui instanciant une classe
	 * WebFile
	 */
	public void parsingAuthorLists() {

		for (int i = 0; i < fileNameList.size(); i++) {
			pageAuthorFound = "";
			authorPageFound = false;
			hasTopEndAuthor = false;
			nbAuthorsByPage = 0;
			smallestSize = 1000;
			maxAvgAuthor = (float) 0.0;
			allPersonList = new ArrayList<String>();
			personMap = new HashMap<String, List<Person>>();
			String fullNameFile = fileNameList.get(i);
			String pageAuthor = authorList.get(i);
			if ((pageAuthor != null && !pageAuthor.trim().equals(""))) {
				System.out.println("------- " + fullNameFile + " - "
						+ pageAuthor);
				this.adress = fullNameFile;
				this.pageAuthor = pageAuthor;
				this.connectAndparse();
				// extractMetaAuthor();
			}
		}
		System.out.println("nb fies= " + fileNameList.size());
		System.out.println("nbre de fichiers ayant un auteur correct: " + id);
		System.out.println("nbre de fichiers ayant un meta auteur : "
				+ nbMetaAuthor);
		System.out.println("Nb pages having more than one author : "
				+ countNbAuthorSup1);
		System.out.println("nb pages having TopEndAuthor : "
				+ nbHasTopEndAuthor);
	}

	/**
	 * parcourir la page et extraire ses attribut pour la construction du corpus
	 */
	void parsingPage() {

		for (int i = 0; i < fileNameList.size(); i++) {
			authorPageFound = false;
			pageAuthorFound = "";
			hasTopEndAuthor = false;
			nbAuthorsByPage = 0;
			smallestSize = 1000;
			maxAvgAuthor = (float) 0.0;
			allPersonList = new ArrayList<String>();
			personMap = new HashMap<String, List<Person>>();
			String fullNameFile = fileNameList.get(i);
			String pageAuthor = authorList.get(i);
			if ((pageAuthor != null && !pageAuthor.trim().equals(""))) {
				this.adress = fullNameFile;
				this.pageAuthor = pageAuthor;
				this.connectAndparse();
			}
		}
		System.out.println("nb fies= " + fileNameList.size());
		System.out.println("nbre de fichiers ayant un titre: " + id);
		System.out.println("Nb pages without author : "
				+ nbPageWithoutAuthor);
		System.out.println("nb pages having TopEndAuthor : "
				+ nbHasTopEndAuthor);
	}

	void connectAndparse() {
		String ligne;
		FileOutputStream out;
		Tidy tidy = new Tidy();
		URL url;
		InputStream in;
		try {
			// ** Connexion à l'URL
			url = new URL(adress);
			URLConnection urlConn = url.openConnection();
			urlConn.setConnectTimeout(5000);
			urlConn.setReadTimeout(5000);
			urlConn.connect();
			String type = urlConn.getContentType();

			if (type != null) {
				final String[] parts = type.split(";");
				this.MIMEtype = parts[0].trim();
			}
			if (MIMEtype != null && MIMEtype.equals("text/html")) {
				in = urlConn.getInputStream();
				if (in != null) {

					// ** Nettoyer le code html avec Jtidy
					tidy.setXHTML(true);
					tidy.setShowWarnings(false);
					tidy.setQuiet(true);
					out = new FileOutputStream("testX.html");
					tidy.parse(in, out);

					// ** Rechercher la valeur de charset dans le fichier
					BufferedReader lecteurAvecBuffer = new BufferedReader(
							new FileReader("testX.html"));
					while ((ligne = lecteurAvecBuffer.readLine()) != null) {
						int index = ligne.indexOf("charset=");
						if (index != -1) {
							if (ligne.trim().toLowerCase().startsWith("utf-8",
									index + 8))
								this.charset = "utf-8";
							else
								this.charset = "ISO-8859-1";
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

	/**
	 * Extraire l'arbre DOM de la page <br>
	 * Extraire les attributs de la page.
	 */
	void parsePage() {
		String TEST_URI = "http://clg-beaumarchais.scola.ac-paris.fr/MATH/BM_MATH.HTM";
		UserAgentContext uacontext = new SimpleUserAgentContext();
		DocumentBuilderImpl builder = new DocumentBuilderImpl(uacontext);
		long begin = System.currentTimeMillis();
		HTMLElement body = null;

		/** Récupération de l'arbre DOM */
		Reader reader;
		try {
			reader = new InputStreamReader(new FileInputStream("testX.html"),
					this.charset);
			InputSourceImpl inputSource = new InputSourceImpl(reader, TEST_URI);
			Document d = builder.parse(inputSource);
			HTMLDocumentImpl document = (HTMLDocumentImpl) d;
			body = document.getBody();
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		if (body != null) {
			/** Pour construire le corpus * */
			// extractAuthor(body);
			/** pour extraire les POS */
			// tagText(body);
			/** Extraire le texte de la page */
			// extractText(body);
			/** pour extraire les attributs de l'auteur */
			extractAuthorAttributes(body);
			percentChange();
			// saveAttributes(personFileName);
			saveMap(personFileName);

			// savelistPersons(personFileName);
			// treeTagg(listAuthorNodes);
		}
	}

	/**
	 * Parcourir l'arbre DOM de la pages. <br>
	 * Extraire les entités nommées et leurs propriétés
	 * 
	 * @param elem
	 */
	void extractAuthorAttributes(HTMLElement elem) {

		NodeList children = elem.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node e = children.item(i);
			Node parent = e.getParentNode();
			HTMLElementImpl elementParent = (HTMLElementImpl) parent;
			String nodeName = elementParent.getNodeName().trim().toLowerCase();

			authorTreatment(e, elementParent, parent);

			NodeList liste = e.getChildNodes();
			if (liste.getLength() > 0) {
				extractAuthorAttributes((HTMLElementImpl) e);
			}
		}
	}

	public void authorTreatment(Node e, HTMLElementImpl elementParent,
			Node parent) {
		String nodeName = elementParent.getNodeName().trim().toLowerCase();
		// Balise de séparation
		String currentNodeName = e.getNodeName().toLowerCase();

		if (blockSeparationList.contains(currentNodeName))
			preceedHr = 1;
		// quand on a 2 balises <p> qui se suivent alors c'est un nouveau
		// block
		if (currentNodeName.equals("p") && pTag == true)
			preceedHr = 1;
		if (currentNodeName.equals("p")) {
			pTag = true;
		} else
			pTag = false;

		if (e.getNodeType() == Node.TEXT_NODE && !nodeName.equals("script")) {
			String text = e.getTextContent().trim();
			while (text.startsWith("&nbsp;"))
				text = text.substring(6);
			// Eliminer Hidden Text
			boolean hiddenType = false;
			if (elementParent.getAttribute("type") != null)
				if (elementParent.getAttribute("type").toLowerCase().equals(
						"hidden"))
					hiddenType = true;
			if (elementParent.getAttribute("style") != null) {
				if (elementParent.getAttribute("style").toLowerCase().equals(
						"display: none;")
						|| elementParent.getAttribute("style").toLowerCase()
								.equals("display: none"))
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
			// TRAITER LE TEXTE
			if (!text.equals("") && text.length() > 1
					&& !nodeName.equals("option") && !nodeName.equals("script")
					&& !nodeName.equals("style") && !nodeName.equals("form")
					&& !nodeName.equals("img") && hiddenType == false
					&& !text.startsWith("<!--") && !text.startsWith("function")
					&& !text.startsWith("&gt;")) {
				String cleanedText = AuthorTools.nettoyeText(text);
				TextNode txtNode = new TextNode();
				// String textToadd = cleanedText.replaceAll("\\'", " ");
				txtNode.sentence = cleanedText;

				// PRECED HR
				if (preceedHr == 1) {
					txtNode.precedSeparation = true;
				}
				// MAIL
				if (elementParent.getAttribute("href") != null)
					if (elementParent.getAttribute("href").contains("mailto")) {
						txtNode.hasMail = 1;
					}
				if (AuthorTools.domEmailDetector(cleanedText))
					txtNode.hasMail = 1;

				// LIEN
				if (elementParent.getAttribute("href") != null)
					txtNode.hasLink = 1;

				// NUM NODE
				numLigne++;
				txtNode.numNode = numLigne;

				// Size
				int policeSize = elementParent.getRenderState().getFont()
						.getSize();
				txtNode.policeSize = policeSize;
				if (policeSize < smallestSize)
					smallestSize = policeSize;

				// POS ET NE
				txtNode.analyseNode();
				listAuthorNodes.add(txtNode);
				preceedHr = 0;
			}
		}

	}

	/**
	 * Parcourir la liste des personnes et analyser leurs fenetres.<br>
	 * Chercher la date, le mail dans la fenetre.<br>
	 * Trouver le numéro relatif du noeud par rapport à toute la page <br>
	 * 
	 * quand un noeud est precédé par une balise de separation ne pas prendre
	 * les noeuds qui le precedent
	 * 
	 * Metrre toutes les personnes dans une map, regrouper les info eds
	 * personnes ayant le même nom.
	 */
	void percentChange() {

		System.out.println("** Parcour de la liste à nouveau ***");

		int count;
		boolean precedSeparation;
		int nbWords = 0;

		for (int i = 0; i < listAuthorNodes.size(); i++) {

			count = 1;
			precedSeparation = false;
			TextNode txtNode = listAuthorNodes.get(i);
			precedSeparation = txtNode.precedSeparation;

			if (txtNode.containPerson == true) {
				List<Person> personList = txtNode.personList;

				// PERSON LIST
				for (int j = 0; j < personList.size(); j++) {
					nbWords = 0;
					Person person = personList.get(j);
					person.precedSeparation = precedSeparation;
					int nbWordBefore = person.nbWordsBefore;
					int originalNbWordsBefore = nbWordBefore;

					// Construire WINDOW BEFORE
					String allBeforeWindow = person.windowBefore.toString();

					// if (nbWordBefore < windowSize) {
					// // lire les tokens des noeuds précédents
					// while (nbWordBefore < windowSize && i > (count - 1)
					// && precedSeparation == false) {
					// // nbre tokens a prendre des noeuds precedents
					// int nbRestWindowBefore = windowSize - nbWordBefore;
					// TextNode nodeBefore = listAuthorNodes
					// .get(i - count);
					// // MAIL
					// if (nodeBefore.hasMail == 1)
					// person.hasMail = true;
					// // WINDOW BEFORE
					// precedSeparation = nodeBefore.precedSeparation;
					// int start;
					// int end = nodeBefore.alTokenList.size();
					// // Si noeud precedent est grand prendre une partie
					// if (end > nbRestWindowBefore) {
					// nbWordBefore += nbRestWindowBefore;
					// start = end - nbRestWindowBefore;
					// } else {
					// // sinon prendre tout le noeud
					// start = 0;
					// nbWordBefore += nodeBefore.wordNumber;
					// }
					// List<String> listTokensBefore = nodeBefore.alTokenList
					// .subList(start, end);
					// String windowBefore = "";
					//
					// // Fenetre d'un noeud precedent
					// for (int k = 0; k < listTokensBefore.size(); k++) {
					// windowBefore = windowBefore
					// + listTokensBefore.get(k) + " ";
					// // AUTHOR BEFORE
					// if (originalNbWordsBefore < 2) {
					// int last = listTokensBefore.size() - 1;
					// int last2 = listTokensBefore.size() - 2;
					// if (k == last || k == last2) {
					// if (authorPatternsList
					// .contains(listTokensBefore.get(
					// k).toLowerCase())) {
					// person.precededByAuthorTag = true;
					// }
					// }
					// // CREATED BY
					// if (k == last) {
					// if (listTokensBefore.get(k)
					// .toLowerCase().equals("by"))
					// person.createdBy = true;
					// }
					// }
					// }
					// // LOCATION
					// List<String> locationList = nodeBefore.locationList;
					// for (String location : locationList) {
					// if (listTokensBefore.contains(location)) {
					// person.hasLocation = true;
					// break;
					// }
					// }
					// // Organisation
					// List<String> orgList = nodeBefore.orgList;
					// for (String organisation : orgList) {
					// if (listTokensBefore.contains(organisation)) {
					// person.hasOrg = true;
					// break;
					// }
					// }
					// // Personne
					// List<Person> personListBefore = nodeBefore.personList;
					// for (Person personne : personListBefore) {
					// if (listTokensBefore.contains(personne.name)) {
					// person.hasPerson = true;
					// break;
					// }
					// }
					// allBeforeWindow = windowBefore + allBeforeWindow;
					// count++;
					// }
					// }

					// WINDOW AFTER
					precedSeparation = false;
					String allAfterWindow = person.windowAfter.toString();
					int nbWordAfter = person.nbWordsAfter;
					count = 1;
					// if (nbWordAfter < windowSize) {
					// while (nbWordAfter < windowSize
					// && i < (listAuthorNodes.size() - count)) {
					// int restWindowAfter = windowSize - nbWordAfter;
					//
					// TextNode nodeAfter = listAuthorNodes.get(i + count);
					// // MAIL
					// if (nodeAfter.hasMail == 1)
					// person.hasMail = true;
					// precedSeparation = nodeAfter.precedSeparation;
					// if (precedSeparation == false) {
					// // WINDOW AFTER
					// int start = 0;
					// int end = nodeAfter.alTokenList.size();
					// if (end > restWindowAfter) {
					// nbWordAfter += restWindowAfter;
					// end = restWindowAfter;
					// } else {
					// nbWordAfter += nodeAfter.wordNumber;
					// }
					// List<String> listTokensAfter = nodeAfter.alTokenList
					// .subList(start, end);
					// String windowAfter = "";
					// for (int k = 0; k < listTokensAfter.size(); k++) {
					// windowAfter = windowAfter + " "
					// + listTokensAfter.get(k);
					// }
					// // LOCATION
					// List<String> locationList = nodeAfter.locationList;
					// for (String location : locationList) {
					// if (listTokensAfter.contains(location)) {
					// person.hasLocation = true;
					// break;
					// }
					// }
					// // Organisation
					// List<String> orgList = nodeAfter.orgList;
					// for (String organisation : orgList) {
					// if (listTokensAfter.contains(organisation)) {
					// person.hasOrg = true;
					// break;
					// }
					// }
					// // Personne
					// List<Person> personListBefore = nodeAfter.personList;
					// for (Person personne : personListBefore) {
					// if (listTokensAfter.contains(personne.name)) {
					// person.hasPerson = true;
					// break;
					// }
					// }
					// allAfterWindow = allAfterWindow + " "
					// + windowAfter;
					// }
					// count++;
					// }
					// }
					// NB WORDS
					nbWords = nbWordBefore + nbWordAfter;

					// TOTAL WINDOW
					String window = allBeforeWindow.trim() + " "
							+ allAfterWindow.trim();

					// SIZE
					// int size = txtNode.policeSize;
					// float relatifSize = smallestSize / size;
					// person.policeSize = size;
					// person.relatifSize = relatifSize;
					//
					// // Dictionnaire
					// for (String dicoWord : dicoList) {
					// if (window.toString().toLowerCase().contains(dicoWord)) {
					// person.hasDicoTocken = true;
					// break;
					// }
					// }
					// // MAIL
					// if (window.toString().toLowerCase().contains("mail"))
					// person.hasMail = true;
					//
					// // Date
					// boolean hasDate = AuthorTools
					// .containDate(window.toString());
					// if (hasDate == true)
					// person.hasDate = hasDate;

					// // POSITION
					// float distance;
					// float relatifNumLigne = (float) person.numLine /
					// numLigne;
					// person.relatifNumLine = relatifNumLigne;
					// if (relatifNumLigne < topPosition) {
					// person.topPosition = 1;
					// distance = topPosition - relatifNumLigne;
					// } else if (relatifNumLigne > endPosition) {
					// person.endPosition = 1;
					// distance = relatifNumLigne - endPosition;
					// } else
					// distance = 0;
					// person.distanceToContent = distance;
					//
					// // quand on teste sur des pages non annoté de Google
					// // eliminer le test person.isAutor
					// if (person.isAutor) {
					// if (relatifNumLigne > maxRelatifNumLine)
					// maxRelatifNumLine = relatifNumLigne;
					// if (relatifNumLigne < minRelatifNumLine)
					// minRelatifNumLine = relatifNumLigne;
					// }
					// if (person.isAutor) {
					// if (person.topPosition == 1 || person.endPosition == 1)
					// hasTopEndAuthor = true;
					// }
					// person.precedSeparation = precedSeparation;

					List<Person> localPersonList;
					String authorName = person.isAutor ? "author" : person.name;

					if (personMap.containsKey(authorName)) {
						localPersonList = personMap.get(authorName);
					} else {
						localPersonList = new ArrayList<Person>();
					}
					localPersonList.add(person);

					// pour chaque personne mettre toutes ses occurences dans la
					// page dans personMap, si c'est l'auteur son devient author
					personMap.put(authorName, localPersonList);
				}
			}
		}
	}

	/**
	 * aggregation des attributs des même personnes avec l'opérateur OR
	 */
	void saveMap(String FileName) {
		System.out.println("SAVING MAP...");
		List<Person> personList = new ArrayList<Person>();
		Set cles = personMap.keySet();
		Iterator it = cles.iterator();

		if (authorPageFound) {
			nbPageWithAuthor++;
			// activer s'il s'agit de parcourir des pages non annotées
			// if (personMap.size() < 20) {
			// l'id de la page courante
			id++;
			try {
				BufferedWriter sortie = new BufferedWriter(new FileWriter(
						FileName, true));

				while (it.hasNext()) {
					String cle = (String) it.next();
					List<Person> localPersonList = personMap.get(cle);
					Person person = new Person();
					float distanceToContent = (float) 0.0;
					// Combiner les attributs
					for (Person localPerson : localPersonList) {
						person.name = localPerson.name;
						person.totalName = localPerson.totalName;

						if (localPerson.distanceToContent > distanceToContent)
							distanceToContent = localPerson.distanceToContent;

						if (localPerson.isAutor)
							person.isAutor = true;
						if (localPerson.createdBy)
							person.createdBy = true;
						if (localPerson.endPosition == 1)
							person.endPosition = 1;
						if (localPerson.hasDate)
							person.hasDate = true;
						if (localPerson.hasDicoTocken)
							person.hasDicoTocken = true;
						if (localPerson.hasLocation)
							person.hasLocation = true;
						if (localPerson.hasMail)
							person.hasMail = true;
						if (localPerson.hasOrg)
							person.hasOrg = true;
						if (localPerson.hasPerson)
							person.hasPerson = true;
						if (localPerson.precededByAuthorTag)
							person.precededByAuthorTag = true;
						if (localPerson.precedSeparation)
							person.precedSeparation = true;
						if (localPerson.topPosition == 1)
							person.topPosition = 1;
						if (localPerson.relatifNumLine == maxRelatifNumLine)
							person.maxRelatifNumLine = true;
						if (localPerson.relatifNumLine == minRelatifNumLine)
							person.minRelatifNumLine = true;
					}
					person.distanceToContent = distanceToContent;
					if (distanceToContent > 0)
						person.outOfContent = true;
					personList.add(person);

					sortie.write(id + "," + "'" + adress + "'" + "," + "'"
							+ person.totalName + "'" + ","
							+ AuthorTools.boolToInt(person.hasDate) + ","
							+ AuthorTools.boolToInt(person.hasDicoTocken) + ","
							+ AuthorTools.boolToInt(person.hasMail) + ","
							+ AuthorTools.boolToInt(person.maxRelatifNumLine)
							+ ","
							+ AuthorTools.boolToInt(person.minRelatifNumLine)
							+ "," + person.topPosition + ","
							+ person.endPosition + ","
							+ AuthorTools.boolToInt(person.precededByAuthorTag)
							+ "," + AuthorTools.boolToInt(person.createdBy)
							+ ","
							// + AuthorTools.boolToInt(person.hasPerson) + ","
							// + AuthorTools.boolToInt(person.hasLocation) + ","
							+ AuthorTools.boolToInt(person.hasOrg) + ","
							// + AuthorTools.boolToInt(person.precedSeparation)
							// + ","
							// + person.hasLink + ","
							// + AuthorTools.boolToInt(person.outOfContent) +
							// ","
							// + person.distanceToContent + ","
							+ AuthorTools.boolToInt(person.isAutor) + "\n");

				}
				sortie.flush();
				sortie.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// }
		} else {
			nbPageWithoutAuthor++;
			try {
				BufferedWriter sortie2 = new BufferedWriter(new FileWriter(
						"withoutAuthor.txt", true));
				sortie2.write(adress + "--" + pageAuthor + "-- "
						+ pageAuthorFound + "\n");
				sortie2.flush();
				sortie2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Enregistrer les attributs des personnes sans regrouper les differentes
	 * occurencezs d'une personne
	 */
	void savelistPersons(String FileName) {
		Set cles = personMap.keySet();
		Iterator it = cles.iterator();

		if (authorPageFound) {
			id++;
			try {
				BufferedWriter sortie = new BufferedWriter(new FileWriter(
						FileName, true));
				while (it.hasNext()) {
					String cle = (String) it.next();
					List<Person> localPersonList = personMap.get(cle);
					for (Person localPerson : localPersonList) {
						if (localPerson.distanceToContent > 0)
							localPerson.outOfContent = true;
						sortie
								.write(id
										+ ","
										+ "'"
										+ adress
										+ "'"
										+ ","
										+ "'"
										+ localPerson.name
										+ "'"
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.hasDate)
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.hasDicoTocken)
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.hasMail)
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.maxRelatifNumLine)
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.minRelatifNumLine)
										+ ","
										+ localPerson.topPosition
										+ ","
										+ localPerson.endPosition
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.precededByAuthorTag)
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.createdBy)
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.hasOrg)
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.precedSeparation)
										+ ","
										+ localPerson.hasLink
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.outOfContent)
										+ ","
										+ localPerson.distanceToContent
										+ ","
										+ AuthorTools
												.boolToInt(localPerson.isAutor)
										+ "\n");
					}
				}
				sortie.flush();
				sortie.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Comparer la valeur du meta auteur avec la valeur de l'etiquette auteur
	 * 
	 * @return
	 */
	String extractMetaAuthor() {
		String authorName = null;

		URL url;
		try {
			url = new URL(adress);

			Parser parser = new Parser(url.toString());
			org.htmlparser.util.NodeList list = parser.parse(new TagNameFilter(
					"meta"));
			// auteur : ="Author" , ="Publisher"
			for (int i = 0; i < list.size(); i++) {
				MetaTag tag = (MetaTag) list.elementAt(i);
				String meta = tag.getMetaTagName();
				String metaValue = tag.getMetaContent();
				if (meta != null) {
					meta = meta.toLowerCase();
					if (meta.equals("author") && metaValue != null
							&& !metaValue.equals("")) {
						authorName = metaValue.toLowerCase();
						nbMetaAuthor++;
						float distance = AuthorTools.distanceBetweenTexts(
								authorName, pageAuthor);
						if (distance >= 0.5) {
							id++;
							System.out.println(id + " " + adress + "**"
									+ pageAuthor);
						}
					}
				}
			}
			parser.reset();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (ParserException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return authorName;
	}

	/**
	 * remplir dicoList avec les mots qui se trouvent dans le fichier
	 * dictionnaire
	 */
	public void loadDicoList() {
		dicoList = new ArrayList<String>();
		BufferedReader ficTexte;
		String ligne = "";
		try {
			ficTexte = new BufferedReader(new FileReader(new File(dicoFile)));
			if (ficTexte == null) {
				throw new FileNotFoundException("Fichier non trouvé: "
						+ dicoFile);
			}
			while ((ligne = ficTexte.readLine()) != null) {
				if (ligne != null) {
					dicoList.add(ligne);
				}
			}
			ficTexte.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Extraire a partir du fichier la liste des url et de leurs auteurs
	 * associés. Les mettre dans fileNameList et dans authorList <br>
	 * Nous traitons que les pages en Englais
	 */
	void getAuthorsFromFiles() {

		fileNameList = new ArrayList<String>();
		authorList = new ArrayList<String>();
		String ligne = "";
		String fichier = "allEnglishPages.txt";// "allPagesWithAuthors.txt";

		BufferedReader ficTexte;
		try {
			ficTexte = new BufferedReader(new FileReader(new File(fichier)));
			if (ficTexte == null) {
				throw new FileNotFoundException("Fichier non trouvé: "
						+ fichier);
			}
			while ((ligne = ficTexte.readLine()) != null) {
				if (ligne != null) {
					String[] tmp = ligne.split("--");
					if (tmp.length > 1) {
						fileNameList.add(tmp[0].trim());
						authorList.add(tmp[1].trim());
					}
				}
			}
			ficTexte.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void loadPersonNamesDictionnaries() {
		BufferedReader ficTexte;
		String ligne = "";
		try {
			// Female First File
			ficTexte = new BufferedReader(new FileReader(new File(
					femaleFirstFile)));
			if (ficTexte == null) {
				throw new FileNotFoundException("Fichier non trouvé: "
						+ femaleFirstFile);
			}
			while ((ligne = ficTexte.readLine()) != null) {
				if (ligne != null) {
					femaleFirstList.add(ligne.toLowerCase());
				}
			}
			ficTexte.close();

			// Male First File
			ficTexte = new BufferedReader(new FileReader(
					new File(maleFirstFile)));
			if (ficTexte == null) {
				throw new FileNotFoundException("Fichier non trouvé: "
						+ maleFirstFile);
			}
			while ((ligne = ficTexte.readLine()) != null) {
				if (ligne != null) {
					maleFirstList.add(ligne.toLowerCase());
				}
			}
			ficTexte.close();

			// Last names File

			// ficTexte = new BufferedReader(new FileReader(
			// new File(lastNamesFile)));
			// if (ficTexte == null) {
			// throw new FileNotFoundException("Fichier non trouvé: "
			// + lastNamesFile);
			// }
			// while ((ligne = ficTexte.readLine()) != null) {
			// if (ligne != null) {
			// lastList.add(ligne.toLowerCase());
			// }
			// }
			// ficTexte.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		double before = System.currentTimeMillis();
		DicoDomParse domParse = new DicoDomParse();
		domParse.loadPersonNamesDictionnaries();
		double time = (System.currentTimeMillis() - before) / 1000;
		System.out.println("Dictionnaires charges :" + time + " s");
		domParse.loadDicoList();
		domParse.getAuthorsFromFiles();
		domParse.parsingAuthorLists();

		double time2 = (System.currentTimeMillis() - time) / 1000;
		System.out.println("Dictionnaires charges :" + time + " s");
	}

	/**
	 * 
	 * Classe qui caracterise un noeud textuel
	 * 
	 */
	public class TextNode {
		ArrayList<String> alTokenList;

		TokenList alTokenListBalie;

		private List<String> locationList = new ArrayList<String>();

		private List<String> orgList = new ArrayList<String>();

		int numNode;

		int hasMail = 0;

		boolean precedSeparation = false;

		String sentence;

		boolean containPerson;

		List<Person> personList = new ArrayList<Person>();

		int wordNumber = 0;

		int hasLink = 0;
		int policeSize;

		/**
		 * Analyser le contenu textuel du noeud. <br>
		 * Extraire les organizations. <br>
		 * Tokenizer, chercher si les tokens appartiennent à la liste des noms
		 * propores. <br>
		 */
		public void analyseNode() {

			int index = 0;// localisation de la personne
			ArrayList<Integer> personIndexes = new ArrayList<Integer>();
			alTokenList = new ArrayList<String>();
			String PUNCTUATION_REGEX = "\\?|\\'|\\-|<|>|\\[|\\]|\\{|\\}|\\/|\\;|\"|\\(|\\)|\\:|\\!|\\*";
			sentence = sentence.replaceAll(PUNCTUATION_REGEX, "").trim();

			// ORGANIZATION: avec Balie
			// tokenizer.Reset();
			// tokenizer.Tokenize(sentence.toString());
			// alTokenListBalie = tokenizer.GetTokenList();
			// NamedEntityRecognitionNerf ner = new NamedEntityRecognitionNerf(
			// alTokenListBalie, lexicon, disambiguationRules,
			// new PriorCorrectionNerf(), NamedEntityTypeEnumMappingNerf
			// .values(), true);
			// ner.RecognizeEntities();
			// for (int i = 0; i < alTokenListBalie.Size(); i++) {
			// String namedEntity = alTokenListBalie.Get(i).EntityType()
			// .GetLabel(NamedEntityTypeEnumMappingNerf.values());
			// String name = alTokenListBalie.Get(i).Raw();
			// if (namedEntity != null) {
			// if (namedEntity.equals("ORGANIZATION")) {
			// orgList.add(name);
			// }
			// }
			// }

			// Tokenize
			System.out.println("-" + sentence);
			StringTokenizer st = new StringTokenizer(sentence, " ");

			// Person: utiliser la liste des noms
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				alTokenList.add(token);
				if (Character.isUpperCase(token.charAt(0))
						&& token.length() > 0) {
					String lowerToken = token.toLowerCase().replaceAll(
							"\\,|\\.", " ").trim();

					System.out.println(lowerToken);
					if (femaleFirstList.contains(lowerToken)
							|| maleFirstList.contains(lowerToken)
							|| lastList.contains(lowerToken)) {
						personIndexes.add(index);
						containPerson = true;
					}
				}
				index++;
			}
			ArrayList<String> currentPersons = new ArrayList<String>();
			// Merge Person ou cas ou 2 nom se suivent alors ils sont pour la
			// meme personne
			for (int i = 0; i < personIndexes.size(); i++) {
				boolean addToList = true;
				int start = personIndexes.get(i);
				int end = start;
				int j;
				String namePerson = alTokenList.get(start);
				int nameSize = 1;
				j = start;
				String nextName = "", beforeName = "";
				// Next
				if (!namePerson.endsWith(",")) {
					while (j < alTokenList.size() - 1) {
						String nextToken = alTokenList.get(j + 1);
						if (Character.isUpperCase(nextToken.charAt(0))
								&& nextToken.length() > 0
								&& !nextName.endsWith(",")) {
							nextName += " " + nextToken;
							end = end + 1;
							j++;
							nameSize++;
						} else
							break;
					}
				}

				// Before
				j = start;
				String beforeToken = "";
				while (j > 0 && !beforeToken.endsWith(",")) {
					beforeToken = alTokenList.get(j - 1);
					if (Character.isUpperCase(beforeToken.charAt(0))
							&& beforeToken.length() > 0
							&& !beforeToken.endsWith(",")) {
						beforeName = beforeToken + " " + beforeName;
						start = start - 1;
						j--;
						nameSize++;
					} else
						break;
				}
				String totalName = beforeName + namePerson + nextName;

				totalName = totalName.replaceAll("\\,|\\.", " ").trim();
				namePerson = namePerson.replaceAll("\\,|\\.", " ").trim();
				System.out.println("-------------" + totalName + " - "
						+ pageAuthor);
				if (!currentPersons.contains(totalName) && nameSize > 0) {

					currentPersons.add(totalName);

					// Construct Person
					Person person = new Person(namePerson, start, end);
					person.totalName = totalName;
					person.hasLink = this.hasLink;
					float distance = AuthorTools.distanceBetweenTexts(
							person.totalName, pageAuthor);
					if (distance > maxAvgAuthor)
						maxAvgAuthor = distance;
					person.avgAuthor = distance;

					if (distance == 1) {
						System.out.println(totalName + " - " + distance);
						person.isAutor = true;
						authorPageFound = true;
						nbAuthorsByPage++;
						pageAuthorFound = person.totalName;
					} else if (allPersonList.contains(namePerson)) {
						addToList = false;
					}
					if (addToList) {
						personList.add(person);
						allPersonList.add(namePerson);
					}
				}
			}
			this.wordNumber = alTokenList.size();
			checkWindow();
		}

		/**
		 * Chercher des information dans la fenetre de contexte
		 */
		void checkWindow() {

			for (int i = 0; i < personList.size(); i++) {
				Person person = personList.get(i);

				// MAIL
				if (this.hasMail == 1)
					person.hasMail = true;

				// NUM LIGNE
				person.numLine = this.numNode;

				// WINDOW BEFORE
				String windowBefore = "";
				int startPersonPosition = person.start;
				int nbWordsBefore = 0;
				int allNbWordsBefore = 0;
				int indexBefore;

				if (startPersonPosition > 0) {
					indexBefore = startPersonPosition - 1;
					while (indexBefore >= 0) {
						if (!alTokenList.get(indexBefore).matches(
								AuthorTools.PUNCTUATION_REGEX)) {
							allNbWordsBefore++;
							String token = alTokenList.get(indexBefore);

							if (nbWordsBefore < windowSize) {
								nbWordsBefore++;
								windowBefore = token + " " + windowBefore;
								// CREATED BY
								if (indexBefore >= 0
										&& createdByList.contains(token
												.toLowerCase())) {
									String nextToken = alTokenList
											.get(indexBefore + 1);
									if (nextToken.toLowerCase().equals("by"))
										person.createdBy = true;
								}
								// BY
								if (nbWordsBefore == 1
										&& token.toLowerCase().equals("by")) {
									person.createdBy = true;
								}
								// author Patterns List
								if (nbWordsBefore == 1 || nbWordsBefore == 2) {
									if (authorPatternsList.contains(token
											.toLowerCase())) {
										person.precededByAuthorTag = true;
									}
								}
							}
						}
						indexBefore--;
					}
				}
				person.nbWordsBefore = nbWordsBefore;
				person.windowBefore = windowBefore;

				// WINDOW AFTER
				String windowAfter = "";
				int endPersonPosition = person.end;
				int indexAfter;
				int nbWordsAfter = 0;
				int allNbWordsAfter = 0;
				// construire la fenetre
				if (endPersonPosition < alTokenList.size()) {
					indexAfter = endPersonPosition + 1;
					while (indexAfter < alTokenList.size()) {
						String token = alTokenList.get(indexAfter);
						if (!token.matches(AuthorTools.PUNCTUATION_REGEX)) {
							allNbWordsAfter++;
							if (nbWordsAfter < windowSize) {
								nbWordsAfter++;
								windowAfter = windowAfter + " " + token;
							}
						}
						indexAfter++;
					}
				}
				person.nbWordsAfter = nbWordsAfter;
				person.baliseNbWords = allNbWordsBefore + allNbWordsAfter;
				person.windowAfter = windowAfter;

			}
		}
	}

	public class Person {
		String name;
		String totalName;

		int start;

		int end;

		int baliseNbWords;

		int numTag;

		int numLine;

		int hasLink = 0;

		int nbWordsBefore;

		int nbWordsAfter;

		// Window
		String windowBefore;

		String windowAfter;

		String window;

		// Date
		boolean hasDate;

		// Mots du dictionnaire
		boolean hasDicoTocken;

		// Personne
		boolean hasPerson;

		// Lieu
		boolean hasLocation;

		// Organisation
		boolean hasOrg;

		// Mail
		boolean hasMail;

		// PrecedByAuthor
		boolean precededByAuthorTag;

		// Author
		boolean isAutor;
		float avgAuthor;

		// createdBy
		boolean createdBy;

		// SIZE
		float relatifSize;
		int policeSize;

		// position
		int topPosition = 0;
		int endPosition = 0;
		boolean maxRelatifNumLine;
		boolean minRelatifNumLine;
		float distanceToContent;
		float relatifNumLine;
		boolean outOfContent;

		// Separation
		boolean precedSeparation;

		public Person(String name, int start, int end) {
			super();
			this.name = name;
			this.start = start;
			this.end = end;
		}

		public Person(String name, int tag) {
			super();
			this.name = name;
			this.numTag = tag;
		}

		public Person() {
			// TODO Auto-generated constructor stub
		}

	}
}
