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
 * les personnes sont detectées en tant qu'entitées nommées avec Balie.
 * 
 * Prendre en compte les POS tags.
 * 
 * @author changuel
 * 
 */
public class DomParseBalie {
	private String MIMEtype = null;
	private String charset = "ISO-8859-1";

	private String adress;
	private String pageAuthor;

	int id = 0;
	int numLigne = 0;
	float maxAvg;
	boolean hasAuthor;
	int preceedHr = 0;
	final int windowSize = 15;
	boolean authorPageFound;
	int nbHasTitles = 0;
	float minRelatifNumLine = (float) 1.0;
	float maxRelatifNumLine = (float) 0.0;
	float maxAvgAuthor = (float) 0.0;
	boolean hasTopEndAuthor;
	int nbHasTopEndAuthor = 0;
	int nbAuthorsByPage;
	int smallestSize = 1000;
	static int countNbAuthorSup1 = 0;
	float topPosition = (float) 0.2;
	float endPosition = (float) 0.9;

	String dicoFile = "dico";
	String attribFile = "authorsAttrib";
	final static String personFileName = "authorsAttrib";

	ArrayList<TextNode> listAuthorNodes;
	ArrayList<String> dicoList;
	List<String> fileNameList;
	List<String> authorList;
	List<String> allPersonList;

	List<String> createdByList = new ArrayList<String>(Arrays
			.asList(AuthorTools.createdByPatterns));
	List<String> authorPatternsList = new ArrayList<String>(Arrays
			.asList(AuthorTools.authorPatterns));

	List<String> nounPosList = new ArrayList<String>(Arrays
			.asList(AuthorTools.nounPos));
	List<String> verbPosList = new ArrayList<String>(Arrays
			.asList(AuthorTools.verbPos));
	List<String> blockSeparationList = new ArrayList<String>(Arrays
			.asList(AuthorTools.blockSeparation));
	List<String> falsePersonList = new ArrayList<String>(Arrays
			.asList(AuthorTools.falsePerson));
	Map<String, List<Person>> personMap;

	// BALIE NE
	Tokenizer tokenizer;
	LexiconOnDiskI lexicon;
	DisambiguationRulesNerf disambiguationRules;

	public DomParseBalie() {
		super();
		// BALIE
		tokenizer = new Tokenizer(Balie.LANGUAGE_ENGLISH, true);
		lexicon = new LexiconOnDisk(LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON);
		disambiguationRules = DisambiguationRulesNerf.Load();
	}

	/**
	 * ******************** MAIN ****************************
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		double before = System.currentTimeMillis();

		DomParseBalie domParse = new DomParseBalie();
		domParse.loadDicoList();
		domParse.getAuthorsFromFiles();
		domParse.parsingAuthorLists();

		double time = (System.currentTimeMillis() - before) / 1000;
		System.out.println("\nTemps d'exécution = " + time + "s");

	}

	/**
	 * remplir dicoList avec les mots qui se trouvent dans le fichier
	 * dictionnaire
	 */
	void loadDicoList() {
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
					// System.out.println(ligne);
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
	 * associés. Les mettre dans fileNameList et dans titleList <br>
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
						System.out.println(tmp[0].trim() + "-" + tmp[1].trim());
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

	/**
	 * parcourir fileNameList et authorList, extraire le nom de chaque fichier
	 * et son auteur.
	 */
	void parsingAuthorLists() {

		for (int i = 0; i < fileNameList.size(); i++) {
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
			}
		}
		System.out.println("nb fies= " + fileNameList.size());
		System.out.println("nbre de fichiers ayant un titre: " + id);
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
			}
		}
		System.out.println("nb fies= " + fileNameList.size());
		System.out.println("nbre de fichiers ayant un titre: " + id);
		System.out.println("Nb pages having more than one author : "
				+ countNbAuthorSup1);
		System.out.println("nb pages having TopEndAuthor : "
				+ nbHasTopEndAuthor);
	}

	/**
	 * *************************************************************************
	 * ****************************
	 */

	void connectAndparse() {
		this.hasAuthor = false;
		listAuthorNodes = new ArrayList<TextNode>();
		this.maxAvg = 0;
		numLigne = 0;

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
				this.MIMEtype = parts[0].trim();
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

	void parsePage() {
		String TEST_URI = "http://clg-beaumarchais.scola.ac-paris.fr/MATH/BM_MATH.HTM";
		UserAgentContext uacontext = new SimpleUserAgentContext();
		DocumentBuilderImpl builder = new DocumentBuilderImpl(uacontext);
		long begin = System.currentTimeMillis();
		HTMLElement body;

		/** Récupération de l'arbre DOM */
		Reader reader;
		try {
			reader = new InputStreamReader(new FileInputStream("testX.html"),
					this.charset);
			InputSourceImpl inputSource = new InputSourceImpl(reader, TEST_URI);
			Document d = builder.parse(inputSource, begin);
			HTMLDocumentImpl document = (HTMLDocumentImpl) d;
			body = document.getBody();
			System.out.println("here");
			// System.out.println(document.getTextContent());
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
				// treeTagg(listAuthorNodes);
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

	boolean pTag = false;

	/**
	 * Parcourir l'arbre DOM de la pages. <br>
	 * Extraire les entités nommées et leurs propriétés
	 * 
	 * @param elem
	 */
	void extractAuthorAttributes(HTMLElement elem) {
		System.out.println("extractAuthorAttributes .......");

		NodeList children = elem.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node e = children.item(i);
			Node parent = e.getParentNode();
			HTMLElementImpl elementParent = (HTMLElementImpl) parent;
			String nodeName = elementParent.getNodeName().trim().toLowerCase();

			// Balise de séparation
			String currentNodeName = e.getNodeName().toLowerCase();

			if (blockSeparationList.contains(currentNodeName))
				preceedHr = 1;
			if (currentNodeName.equals("p") && pTag == true)
				preceedHr = 1;
			if (currentNodeName.equals("p")) {
				pTag = true;
			} else
				pTag = false;

			// if (e.getNodeName().equals("ul"))
			// preceedHr = 1;

			if (e.getNodeType() == Node.TEXT_NODE && !nodeName.equals("script")) {
				String text = e.getTextContent().trim();
				while (text.startsWith("&nbsp;"))
					text = text.substring(6);
				// Eliminer Hidden Text
				boolean hiddenType = false;
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
				// TRAITER LE TEXTE
				if (!text.equals("") && text.length() > 1
						&& !nodeName.equals("option")
						&& !nodeName.equals("script")
						&& !nodeName.equals("style")
						&& !nodeName.equals("form") && !nodeName.equals("img")
						&& hiddenType == false && !text.startsWith("<!--")
						&& !text.startsWith("function")
						&& !text.startsWith("&gt;")) {
					String cleanedText = nettoyeText(text);
					TextNode txtNode = new TextNode();
					// String textToadd = cleanedText.replaceAll("\\'", " ");
					txtNode.sentence = cleanedText;

					// PRECED HR
					if (preceedHr == 1) {
						txtNode.precedSeparation = true;
					}
					// MAIL
					if (elementParent.getAttribute("href") != null)
						if (elementParent.getAttribute("href").contains(
								"mailto")) {
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
					System.out.println(numLigne + "---" + text);

					// Size
					int policeSize = elementParent.getRenderState().getFont()
							.getSize();
					txtNode.policeSize = policeSize;
					if (policeSize < smallestSize)
						smallestSize = policeSize;

					// POS ET NE
					txtNode.balieAnalyse();

					listAuthorNodes.add(txtNode);
					preceedHr = 0;
				}
			}

			NodeList liste = e.getChildNodes();
			if (liste.getLength() > 0) {
				extractAuthorAttributes((HTMLElementImpl) e);
			}
		}
	}

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

	/**
	 * Réciprer pour chaque élément de la liste les propriétés de deux avants et
	 * deux noeuds après : copyright, dico, date, email.<br>
	 * Trouver le numéro relatif du noeud par rapport à toute la page <br>
	 * 
	 * quand un noeud est precédé par la balise div ou hr je ne regarde pas les
	 * noeuds textuels qui le precedent (puisqu'on est dans un nourveau bloc)
	 * 
	 */
	void percentChange() {

		System.out.println("** Parcour de la liste à nouveau ***");

		int count;
		boolean precedSeparation;
		int nbVerbs = 0;
		int nbNoun = 0;
		int nbWords = 0;
		// if (authorPageFound) {

		for (int i = 0; i < listAuthorNodes.size(); i++) {

			count = 1;
			precedSeparation = false;
			TextNode txtNode = listAuthorNodes.get(i);
			precedSeparation = txtNode.precedSeparation;
			List<String> listTags = new ArrayList<String>();

			if (txtNode.containPerson == true) {
				List<Person> personList = txtNode.personList;

				// PERSON LIST
				for (int j = 0; j < personList.size(); j++) {
					nbWords = 0;
					Person person = personList.get(j);
					person.precedSeparation = precedSeparation;
					System.out.println("PERSON : " + person.name);
					listTags = new ArrayList<String>();
					nbVerbs = person.nbVerbs;
					nbNoun = person.nbNoun;
					int nbWordBefore = person.nbWordsBefore;
					int originalNbBefore = nbWordBefore;

					// Node BEFORE
					String beforeWindow = person.windowBefore.toString();
					if (nbWordBefore < windowSize) {
						while (nbWordBefore < windowSize && i > (count - 1)
								&& precedSeparation == false) {
							int restWindowBefore = windowSize - nbWordBefore;
							TextNode nodeBefore = listAuthorNodes
									.get(i - count);
							// MAIL
							if (nodeBefore.hasMail == 1)
								person.hasMail = true;

							// WINDOW BEFORE
							precedSeparation = nodeBefore.precedSeparation;
							int start;
							int end = nodeBefore.ListTokens.size();
							if (end > restWindowBefore) {
								nbWordBefore += restWindowBefore;
								start = end - restWindowBefore;
							} else {
								start = 0;
								nbWordBefore += nodeBefore.wordNumber;
							}
							List<String> listTokensBefore = nodeBefore.ListTokens
									.subList(start, end);
							listTags.addAll(nodeBefore.ListTags.subList(start,
									end));
							String windowBefore = "";
							for (int k = 0; k < listTokensBefore.size(); k++) {
								windowBefore = windowBefore
										+ listTokensBefore.get(k) + " ";
								// AUTHOR BEFORE
								if (originalNbBefore < 2) {
									int last = listTokensBefore.size() - 1;
									int last2 = listTokensBefore.size() - 2;
									if (k == last || k == last2) {
										if (authorPatternsList
												.contains(listTokensBefore.get(
														k).toLowerCase())) {
											person.precededByAuthorTag = true;
										}
									}
									// CREATED BY
									if (k == last) {
										if (listTokensBefore.get(k)
												.toLowerCase().equals("by"))
											person.createdBy = true;
									}
								}
							}

							beforeWindow = windowBefore + beforeWindow;

							// LOCATION
							List<String> locationList = nodeBefore.locationList;
							for (String location : locationList) {
								if (listTokensBefore.contains(location)) {
									person.hasLocation = true;
									break;
								}
							}
							// Organisation
							List<String> orgList = nodeBefore.orgList;
							for (String organisation : orgList) {
								if (listTokensBefore.contains(organisation)) {
									person.hasOrg = true;
									break;
								}
							}
							// Personne
							List<Person> personListBefore = nodeBefore.personList;
							for (Person personne : personListBefore) {
								if (listTokensBefore.contains(personne.name)) {
									person.hasPerson = true;
									break;
								}
							}
							count++;
						}
					}

					// WINDOW AFTER
					precedSeparation = false;
					String afterWindow = person.windowAfter.toString();
					int nbWordAfter = person.nbWordsAfter;
					count = 1;
					if (nbWordAfter < windowSize) {
						while (nbWordAfter < windowSize
								&& i < (listAuthorNodes.size() - count)
								&& precedSeparation == false) {
							int restWindowAfter = windowSize - nbWordAfter;

							TextNode nodeAfter = listAuthorNodes.get(i + count);
							// MAIL
							if (nodeAfter.hasMail == 1)
								person.hasMail = true;

							precedSeparation = nodeAfter.precedSeparation;
							if (precedSeparation == false) {
								// WINDOW AFTER
								int start = 0;
								int end = nodeAfter.ListTokens.size();
								if (end > restWindowAfter) {
									nbWordAfter += restWindowAfter;
									end = restWindowAfter;
								} else {
									nbWordAfter += nodeAfter.wordNumber;
								}

								List<String> listTokensAfter = nodeAfter.ListTokens
										.subList(start, end);
								listTags.addAll(nodeAfter.ListTags.subList(
										start, end));
								String windowAfter = "";
								for (int k = 0; k < listTokensAfter.size(); k++) {
									windowAfter = windowAfter + " "
											+ listTokensAfter.get(k);
								}
								afterWindow = afterWindow + " " + windowAfter;

								// LOCATION
								List<String> locationList = nodeAfter.locationList;
								for (String location : locationList) {
									if (listTokensAfter.contains(location)) {
										person.hasLocation = true;
										break;
									}
								}
								// Organisation
								List<String> orgList = nodeAfter.orgList;
								for (String organisation : orgList) {
									if (listTokensAfter.contains(organisation)) {
										person.hasOrg = true;
										break;
									}
								}
								// Personne
								List<Person> personListBefore = nodeAfter.personList;
								for (Person personne : personListBefore) {
									if (listTokensAfter.contains(personne.name)) {
										person.hasPerson = true;
										break;
									}
								}
							}
							count++;
						}
					}
					// NB WORDS
					nbWords = nbWordBefore + nbWordAfter;

					// TOTAL WINDOW
					String window = beforeWindow.trim() + " "
							+ afterWindow.trim();

					// SIZE
					int size = txtNode.policeSize;
					float relatifSize = smallestSize / size;
					person.policeSize = size;
					person.relatifSize = relatifSize;

					// POS
					for (int k = 0; k < listTags.size(); k++) {
						String tag = listTags.get(k).toLowerCase();
						if (verbPosList.contains(tag)) {
							nbVerbs++;
						}
						if (nounPosList.contains(tag)) {
							nbNoun++;
						}
					}
					person.nbVerbs = nbVerbs;
					person.nbNoun = nbNoun;
					person.relatifNbVerbs = nbWords != 0 ? (float) nbVerbs
							/ nbWords : 0;
					person.relatifNbNoun = nbWords != 0 ? (float) nbNoun
							/ nbWords : 0;

					// Dictionnaire
					for (String dicoWord : dicoList) {
						if (window.toString().toLowerCase().contains(dicoWord)) {
							person.hasDicoTocken = true;
							break;
						}
					}

					// MAIL
					if (window.toString().toLowerCase().contains("mail"))
						person.hasMail = true;

					// Date
					boolean hasDate = AuthorTools.containDate(window.toString());
					if (hasDate == true)
						person.hasDate = hasDate;

					// POSITION
					float distance;
					float relatifNumLigne = (float) person.numLine / numLigne;
					person.relatifNumLine = relatifNumLigne;
					if (relatifNumLigne < topPosition) {
						person.topPosition = 1;
						distance = topPosition - relatifNumLigne;
					} else if (relatifNumLigne > endPosition) {
						person.endPosition = 1;
						distance = relatifNumLigne - endPosition;
					} else
						distance = 0;
					person.distanceToContent = distance;

					// TODO google
					// if (person.isAutor) {
					if (relatifNumLigne > maxRelatifNumLine)
						maxRelatifNumLine = relatifNumLigne;
					if (relatifNumLigne < minRelatifNumLine)
						minRelatifNumLine = relatifNumLigne;
					// }

					if (person.isAutor)
						if (person.topPosition == 1 || person.endPosition == 1)
							hasTopEndAuthor = true;

					person.precedSeparation = precedSeparation;
					boolean saveIt = true;

					// Ajouter au MAP
					if (person.name.length() < 3
							|| person.name.contains("@")
							|| person.name.contains("=")
							|| person.name.toLowerCase().contains(
									"introduction")
							|| person.name.toLowerCase().contains("link")
							|| person.name.toLowerCase().contains("page")
							|| person.name.toLowerCase().contains("search")
							|| person.name.toLowerCase().contains("content"))
						saveIt = false;

					List<Person> localPersonList;
					if (saveIt) {
						String authorName = person.isAutor ? "author"
								: person.name;
						if (personMap.containsKey(authorName)) {
							localPersonList = personMap.get(authorName);
						} else {
							localPersonList = new ArrayList<Person>();
						}
						localPersonList.add(person);

						System.out.println("--PERSON :" + person.name
								+ " IsAuthor : " + person.isAutor
								+ " distanceToContent : "
								+ person.distanceToContent);

						System.out.println();
						System.out.println();
						personMap.put(authorName, localPersonList);

					}
				}
			}
		}

		// }
	}

	/**
	 * Chaque personne est enregistrée une seule fois, ses attributs regroupe
	 * l'ensemble des attributs relatifs à chaque occurence dans la page
	 */
	void saveMap(String FileName) {
		List<Person> personList = new ArrayList<Person>();
		Set cles = personMap.keySet();
		Iterator it = cles.iterator();
		// TODO google
		// if (authorPageFound) {
		if (personMap.size() < 20) {
			id++;
			try {
				BufferedWriter sortie = new BufferedWriter(new FileWriter(
						FileName, true));

				while (it.hasNext()) {
					String cle = (String) it.next();
					List<Person> localPersonList = personMap.get(cle);
					Person person = new Person();
					float distanceToContent = (float) 0.0;
					for (Person localPerson : localPersonList) {
						person.name = localPerson.name;
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

					// SVMLib
					// sortieAuteur.write(Tools.boolToInt(person.isAutor) +
					// " 1:"
					// + Tools.boolToInt(person.hasDate) + " 2:"
					// + Tools.boolToInt(person.hasDicoTocken) + " 3:"
					// + Tools.boolToInt(person.hasMail) + " 4:"
					// + Tools.boolToInt(person.maxRelatifNumLine) + " 5:"
					// + Tools.boolToInt(person.minRelatifNumLine) + " 6:"
					// + Tools.boolToInt(person.precededByAuthorTag)
					// + " 7:" + Tools.boolToInt(person.createdBy) + " 8:"
					// + Tools.boolToInt(person.hasOrg) + " 9:"
					// + Tools.boolToInt(person.precedSeparation) + " 10:"
					// + Tools.boolToInt(person.outOfContent) + "\n");

					sortie.write(id + "," + "'" + adress + "'" + "," + "'"
							+ person.name + "'" + ","
							+ AuthorTools.boolToInt(person.hasDate) + ","
							+ AuthorTools.boolToInt(person.hasDicoTocken) + ","
							+ AuthorTools.boolToInt(person.hasMail)
							+ ","
							+ AuthorTools.boolToInt(person.maxRelatifNumLine)
							+ ","
							+ AuthorTools.boolToInt(person.minRelatifNumLine)
							+ ","
							// + person.topPosition + "," + person.endPosition +
							// ","
							+ AuthorTools.boolToInt(person.precededByAuthorTag) + ","
							+ AuthorTools.boolToInt(person.createdBy)
							+ ","
							// + Tools.boolToInt(person.hasPerson) + ","
							// + Tools.boolToInt(person.hasLocation) + ","
							+ AuthorTools.boolToInt(person.hasOrg)
							+ ","
							// + Tools.boolToInt(person.precedSeparation) + ","
							// + person.hasLink + ","
							+ AuthorTools.boolToInt(person.outOfContent) + ","
							+ AuthorTools.boolToInt(person.isAutor) + "\n");

					System.out.println("--PERSON :" + person.name
							+ " distance: " + person.distanceToContent
							+ " IsAuthor :" + person.isAutor);

				}
				sortie.flush();
				sortie.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void saveAttributes(String fileName) {
		List<Person> personList;
		boolean savePerson;
		if (authorPageFound) {
			int countToSave = 0;

			if (nbAuthorsByPage > 1)
				countNbAuthorSup1++;
			if (hasTopEndAuthor)
				nbHasTopEndAuthor++;
			id++;
			try {
				BufferedWriter sortie = new BufferedWriter(new FileWriter(
						fileName, true));
				for (TextNode textNode : listAuthorNodes) {

					personList = textNode.personList;

					for (Person person : personList) {
						savePerson = true;
						String personName = person.name.trim().toLowerCase();
						// if (falsePersonList.contains(personName)
						// || personName.length() < 3
						// || personName.contains("@")
						// || personName.contains("=")
						// || personName.contains("introduction")
						// || personName.contains("search")
						// || personName.contains("page")
						// || personName.contains("page"))
						// savePerson = false;

						// if (nbAuthorsByPage > 1) {
						// // if (person.isAutor
						// // && person.avgAuthor < maxAvgAuthor) {
						// // person.isAutor = false;
						// // savePerson = false;
						// // }
						// if (person.isAutor) {
						// if (hasTopEndAuthor) {
						// if (person.relatifNumLine > HtmlParse.topPosition
						// && person.relatifNumLine < HtmlParse.endPosition) {
						// nbAuthorsByPage--;
						// person.isAutor = false;
						// // savePerson = false;
						// }
						// } else if (person.relatifNumLine != maxRelatifNumLine
						// && person.relatifNumLine != minRelatifNumLine) {
						// nbAuthorsByPage--;
						// person.isAutor = false;
						// // savePerson = false;
						// }
						// }
						//
						// // else if (hasTopEndAuthor) {
						// // person.isAutor = false;
						// // // savePerson = false;
						// // }
						// }

						// if (person.relatifNumLine > 0.4
						// && person.relatifNumLine < 0.8)
						// savePerson = false;

						if (person.relatifNumLine == maxRelatifNumLine)
							person.maxRelatifNumLine = true;
						if (person.relatifNumLine == minRelatifNumLine)
							person.minRelatifNumLine = true;

						if (savePerson)
							countToSave++;

						// if (countToSave>9 && !person.isAutor)
						// savePerson = false;

						if (savePerson)
							sortie
									.write(id
											+ ","
											+ "'"
											+ adress
											+ "'"
											+ ","
											+ "'"
											+ person.name
											+ "'"
											+ ","
											+ AuthorTools.boolToInt(person.hasDate)
											+ ","
											+ AuthorTools
													.boolToInt(person.hasDicoTocken)
											+ ","
											+ AuthorTools.boolToInt(person.hasMail)
											+ ","
											+ AuthorTools
													.boolToInt(person.maxRelatifNumLine)
											+ ","
											+ AuthorTools
													.boolToInt(person.minRelatifNumLine)
											+ ","
											+ person.topPosition
											+ ","
											+ person.endPosition
											+ ","
											+ AuthorTools
													.boolToInt(person.precededByAuthorTag)
											+ ","
											+ AuthorTools.boolToInt(person.createdBy)
											+ ","
											+ AuthorTools.boolToInt(person.hasPerson)
											+ ","
											+ AuthorTools
													.boolToInt(person.hasLocation)
											+ ","
											+ AuthorTools.boolToInt(person.hasOrg)
											+ ","
											+ AuthorTools
													.boolToInt(person.precedSeparation)
											+ ","
											+ person.hasLink
											+ ","
											+ AuthorTools
													.boolToInt(person.outOfContent)
											+ ","
											+ AuthorTools.boolToInt(person.isAutor)
											+ "\n");
						// sortie
						// .write(id
						// + ","
						// + "'"
						// + adress
						// + "'"
						// + ","
						// + "'"
						// + person.name
						// + "'"
						// + ","
						// + Tools.boolToInt(person.hasDate)
						// + ","
						// + Tools
						// .boolToInt(person.hasDicoTocken)
						// + ","
						// + person.hasMail
						// + ","
						// + person.relatifNumLine
						// + ","
						// + person.maxRelatifNumLine
						// + ","
						// + person.minRelatifNumLine
						// + ","
						// + person.topPosition
						// + ","
						// + person.endPosition
						// + ","
						// + person.distanceToContent
						// + ","
						// + person.nbVerbs
						// + ","
						// + person.nbNoun
						// + ","
						// + person.relatifNbNoun
						// + ","
						// + person.relatifNbVerbs
						// + ","
						// + person.baliseNbWords
						// + ","
						// + Tools
						// .boolToInt(person.precededByAuthorTag)
						// + ","
						// + Tools.boolToInt(person.createdBy)
						// + ","
						// + Tools.boolToInt(person.hasPerson)
						// + ","
						// + Tools
						// .boolToInt(person.hasLocation)
						// + ","
						// + Tools.boolToInt(person.hasOrg)
						// + "," + person.precedSeparation
						// + "," + person.hasLink + ","
						// + person.relatifSize + ","
						// + Tools.boolToInt(person.isAutor)
						// + "\n");

						System.out.println("--PERSON :" + person.name
								+ " numLine: " + person.relatifNumLine
								+ " Words :" + person.baliseNbWords
								+ " Verbes :" + person.nbVerbs
								+ " properNoun : " + person.nbNoun + " Date : "
								+ person.hasDate + " Email : " + person.hasMail
								+ " IsAuthor :" + person.isAutor);

					}
				}
				sortie.flush();
				sortie.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {

			try {
				BufferedWriter sortie2 = new BufferedWriter(new FileWriter(
						"allPagesWithAuthors.txt", true));
				sortie2.write("-" + adress + "\n");
				sortie2.flush();
				sortie2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public class TextNode {
		TokenList alTokenList;

		private List<String> locationList = new ArrayList<String>();

		private List<String> orgList = new ArrayList<String>();

		int numNode;

		int hasMail = 0;

		boolean precedSeparation = false;

		String sentence;

		boolean containPerson;

		List<Person> personList = new ArrayList<Person>();

		int wordNumber = 0;

		List<String> ListTags = new ArrayList<String>();

		List<String> ListTokens = new ArrayList<String>();

		int hasLink = 0;
		int policeSize;

		public void balieAnalyse() {
			{
				// NE
				tokenizer.Reset();
				tokenizer.Tokenize(sentence.toString());
				alTokenList = tokenizer.GetTokenList();
				NamedEntityRecognitionNerf ner = new NamedEntityRecognitionNerf(
						alTokenList, lexicon, disambiguationRules,
						new PriorCorrectionNerf(),
						NamedEntityTypeEnumMappingNerf.values(), true);
				ner.RecognizeEntities();
				alTokenList = ner.GetTokenList();

				String namePerson = "";
				int start = 0;
				int end = 0;
				boolean isEnd = false;
				int nbWords = 0;
				StringBuffer text = new StringBuffer();
				text.append("");

				System.out.println();
				// words feature creation
				for (int i = 0; i < alTokenList.Size(); i++) {
					boolean addToList = true;
					if (isEnd == true)
						namePerson = "";
					isEnd = false;
					String namedEntity = alTokenList.Get(i).EntityType()
							.GetLabel(NamedEntityTypeEnumMappingNerf.values());
					String name = alTokenList.Get(i).Raw();
					String tag = alTokenList.Get(i).PartOfSpeech();
					if (!name.matches(AuthorTools.PUNCTUATION_REGEX)) {
						text.append(name).append(" ");
						nbWords++;
						ListTokens.add(name);
						ListTags.add(tag);
					}

					// PERSON
					if (namedEntity != null) {
						if (namedEntity.equals("PERSON")) {
							containPerson = true;
							namePerson += name + " ";
							if (alTokenList.Get(i).EntityType().IsStart() == true) {
								start = i;
							}
							if (alTokenList.Get(i).EntityType().IsEnd() == true) {
								end = i;
								isEnd = true;
							}
							if (isEnd == true) {
								Person person = new Person(namePerson, start,
										end);
								namePerson = namePerson.replaceAll(
										AuthorTools.PUNCTUATION_REGEX, " ").trim();
								person.name = namePerson;

								person.hasLink = this.hasLink;
								// Verifier si c'est l'AUTEUR
								float distance = AuthorTools.distanceBetweenTexts(
										person.name, pageAuthor);
								if (distance > maxAvgAuthor)
									maxAvgAuthor = distance;
								person.avgAuthor = distance;
								if (distance >= 0.5) {
									person.isAutor = true;
									authorPageFound = true;
									nbAuthorsByPage++;
								} else if (allPersonList.contains(namePerson)) {
									addToList = false;
								}
								if (addToList) {
									personList.add(person);
									allPersonList.add(namePerson);
								}
							}
						} else if (namedEntity.equals("LOCATION")) {
							locationList.add(name);
						} else if (namedEntity.equals("ORGANIZATION")) {
							orgList.add(name);
						}
					}
				}
				this.sentence = text.toString();
				this.wordNumber = nbWords;
				checkWindow();
			}
		}

		void checkWindow() {

			for (int i = 0; i < personList.size(); i++) {
				Person person = personList.get(i);

				int verbNumber = 0;
				int nounNumber = 0;

				// MAIL
				if (this.hasMail == 1)
					person.hasMail = true;

				// NUM LIGNE
				person.numLine = this.numNode;

				// WINDOW BEFORE
				int startPersonPosition = person.start;

				String windowBefore = "";
				// Nbre de mots
				int nbWordsBefore = 0;
				int allNbWordsBefore = 0;
				int indexBefore;
				if (startPersonPosition > 0) {
					indexBefore = startPersonPosition - 1;
					while (indexBefore >= 0) {
						if (!alTokenList.Get(indexBefore).Raw().matches(
								AuthorTools.PUNCTUATION_REGEX)) {
							allNbWordsBefore++;
							String token = alTokenList.Get(indexBefore).Raw();
							String tag = alTokenList.Get(indexBefore)
									.PartOfSpeech().toLowerCase();
							if (nbWordsBefore < windowSize) {
								nbWordsBefore++;
								windowBefore = token + " " + windowBefore;
								if (verbPosList.contains(tag)) {
									verbNumber++;
								}
								if (nounPosList.contains(tag)) {
									nounNumber++;
								}
								// Created By
								System.out.println(indexBefore
										+ " CREATED BY "
										+ createdByList.contains(token
												.toLowerCase()));

								if (indexBefore >= 0
										&& createdByList.contains(token
												.toLowerCase())) {

									String nextToken = alTokenList.Get(
											indexBefore + 1).Raw();
									if (nextToken.toLowerCase().equals("by"))
										person.createdBy = true;
								}
								if (nbWordsBefore == 1
										&& token.toLowerCase().equals("by")) {
									person.createdBy = true;
								}
								// PrecedByAuthor
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
				// Nbre de mots après personne
				int nbWordsAfter = 0;
				int allNbWordsAfter = 0;
				if (endPersonPosition < alTokenList.Size()) {
					indexAfter = endPersonPosition + 1;
					while (indexAfter < alTokenList.Size()) {
						String token = alTokenList.Get(indexAfter).Raw();
						String tag = alTokenList.Get(indexAfter).PartOfSpeech()
								.toLowerCase();
						if (!token.matches(AuthorTools.PUNCTUATION_REGEX)) {
							allNbWordsAfter++;
							if (nbWordsAfter < windowSize) {
								nbWordsAfter++;
								windowAfter = windowAfter + " " + token;
								if (verbPosList.contains(tag)) {
									verbNumber++;
								}
								if (nounPosList.contains(tag)) {
									nounNumber++;
								}
							}
						}
						indexAfter++;
					}
					System.out.println();
				}
				person.nbWordsAfter = nbWordsAfter;
				person.nbNoun = nounNumber;
				person.nbVerbs = verbNumber;
				person.baliseNbWords = allNbWordsBefore + allNbWordsAfter;
				StringBuffer window = new StringBuffer();
				person.windowAfter = windowAfter;

			}
		}
	}

	public class Person {
		String name;

		int start;

		int end;

		int baliseNbWords;

		float relatifNbVerbs;

		float relatifNbNoun;

		int nbVerbs;

		int nbNoun;

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
