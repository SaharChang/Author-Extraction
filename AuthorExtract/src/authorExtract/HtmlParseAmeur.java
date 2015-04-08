package authorExtract;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.id.jericho.lib.html.MasonTagTypes;
import au.id.jericho.lib.html.MicrosoftTagTypes;
import au.id.jericho.lib.html.PHPTagTypes;
import au.id.jericho.lib.html.Source;
import ca.uottawa.balie.Balie;
import ca.uottawa.balie.DisambiguationRulesNerf;
import ca.uottawa.balie.LexiconOnDisk;
import ca.uottawa.balie.LexiconOnDiskI;
import ca.uottawa.balie.NamedEntityRecognitionNerf;
import ca.uottawa.balie.NamedEntityTypeEnumMappingNerf;
import ca.uottawa.balie.PriorCorrectionNerf;
import ca.uottawa.balie.TokenList;
import ca.uottawa.balie.Tokenizer;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;

/**
 * Extraire le texte de la page non pas avec l'arbre DOM mais avec le parseur
 * jericho.
 * 
 * Diviser chaque page en block. Il y a une classe block a la fin.
 * 
 * Methode avec Balie et methode avec Lingpipe
 * 
 * @author changuel & Ameur
 * 
 */
public class HtmlParseAmeur {
	// Ligngpipe tokeniser
	TokenizerFactory TOKENIZER_FACTORY;
	HmmDecoder decoder;
	// Lingpipe NE
	private String model;
	private Chunker chunker;
	final SentenceModel SENTENCE_MODEL;
	// BALIE NE
	Tokenizer tokenizer;
	LexiconOnDiskI lexicon;
	DisambiguationRulesNerf disambiguationRules;
	// Variables
	final static String EMAIL_TAG = "mail";
	ArrayList<String> dicoList;
	String dicoFile = "dico";
	int totalNumberOfWords = 0;
	final static String BLOCK_SEPARATOR = "------------------------------------------------------------------------";
	static int id = 0;
	final static String personFileName = "authorsAttrib";
	final static String urlFile = "allEnglishPages.txt";
	String currentUrl;
	String currentAuthor;
	boolean authorPageFound;
	static int totalNumberOfPages = 0;
	int nbAuthorsByPage;
	final int widowSize = 20;
	final int lineNumberBeforeNewBlock = 2;
	final static float topPosition = (float) 0.2;
	final static float endPosition = (float) 0.9;
	int numLine = 0;
	float minRelatifNumLine = (float) 1.0;
	float maxRelatifNumLine = (float) 0.0;
	float maxAvgAuthor = (float) 0.0;
	boolean hasTopEndAuthor;
	int maxNbWordsInBlock = 0;
	List<String> allPersonList;
	int countNbAuthorSup1 = 1;
	List<String> falsePersonList = new ArrayList<String>(Arrays
			.asList(AuthorTools.falsePerson));

	public HtmlParseAmeur() {

		super();

		// Ligngpipe tokeniser
		TOKENIZER_FACTORY = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");

		FileInputStream fileIn;
		ObjectInputStream objIn;
		HiddenMarkovModel hmm;
		try {
			fileIn = new FileInputStream(
					"models/pos-en-general-brown.HiddenMarkovModel");
			objIn = new ObjectInputStream(fileIn);
			hmm = (HiddenMarkovModel) objIn.readObject();
			Streams.closeInputStream(objIn);
			decoder = new HmmDecoder(hmm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// Lingpipe NE
		model = "models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
		SENTENCE_MODEL = new MedlineSentenceModel();
		File modelFile = new File(model);
		System.out.println("Reading chunker from file=" + modelFile);
		try {
			chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// BALIE
		tokenizer = new Tokenizer(Balie.LANGUAGE_ENGLISH, true);
		lexicon = new LexiconOnDisk(LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON);
		disambiguationRules = DisambiguationRulesNerf.Load();

	}

	/**
	 * remplir dicoList avec les mots qui se trouvent dans le fichier
	 * dictionnaire
	 * 
	 * @return
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
	void parsingUrls() {
		String ligne = "";
		BufferedReader ficTexte;
		try {
			ficTexte = new BufferedReader(new FileReader(new File(urlFile)));
			if (ficTexte == null) {
				throw new FileNotFoundException("Fichier non trouvé: "
						+ urlFile);
			}
			while ((ligne = ficTexte.readLine()) != null) {
				authorPageFound = false;
				hasTopEndAuthor = false;
				nbAuthorsByPage = 0;
				maxNbWordsInBlock = 0;
				maxAvgAuthor = (float) 0.0;
				allPersonList = new ArrayList<String>();
				if (ligne != null) {
					String[] tmp = ligne.split("--");
					if (tmp.length > 1) {
						currentUrl = tmp[0].trim();
						currentAuthor = tmp[1].trim();
						if (currentUrl != null && currentAuthor != null) {
							totalNumberOfPages++;
							System.out.println("----" + currentUrl);
							System.out
									.println("EXTRACTING BLOCKS ....... Author = "
											+ currentAuthor);
							numLine = 0;
							extractBolcs();
						}
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

	public static void main(String[] args) {
		HtmlParseAmeur hp = new HtmlParseAmeur();
		hp.loadDicoList();
		hp.parsingUrls();
		System.out.println(id + " author found over " + totalNumberOfPages
				+ " pages");
	}

	/**
	 * Extraire les blocks à partir du texte de la page Web (résultat du
	 * parseur)<br>
	 * Chaque block est séparé par celui qui le précéde par soit
	 * lineNumberBeforeNewBlock: nombre de ligne vide séparant les textes soit
	 * par "----..."
	 * 
	 * @param text
	 */
	public void extractBolcs() {

		List<Block> blockList = new ArrayList<Block>();
		String file = "test.txt";
		Map<Integer, String> linesMap = new HashMap<Integer, String>();

		try {
			InputStream ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line = null;
			boolean firstBlock = true;
			Block block = null;
			int emptyLineNumber = 0;
			// int blockNumber = 0;
			while ((line = br.readLine()) != null) {
				// Nouveau Block
				if (firstBlock) {
					block = new Block(widowSize);
					firstBlock = false;
				}
				// Tester si c'est la fin de block
				if (line.trim().equals("")
						|| line.trim().equals(BLOCK_SEPARATOR)) {
					emptyLineNumber++;
					if (emptyLineNumber >= lineNumberBeforeNewBlock) {
						firstBlock = true;
						linesMap = new HashMap<Integer, String>();
						emptyLineNumber = 0;
						if (block != null && !blockList.contains(block)
								&& !block.sentence.toString().trim().equals("")) {
							blockList.add(block);
							block.balieAnalyse();
							// block.analyse();
						}
					}
					continue;
				}
				emptyLineNumber = 0;
				// EMAIL
				if (line.contains("@"))
					line = AuthorTools.emailDetector(line);
				// Eliminer les liens
				String modele = "(<[^<>]+>)";
				Pattern pattern = Pattern.compile(modele);
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String subLine = matcher.group(1);
					if (subLine.contains(EMAIL_TAG)) {
						line = line.replace(subLine, EMAIL_TAG);
					} else {
						line = line.replace(subLine, " ");
					}
				}
				line += ". ";
				String lineWithoutPunctuation = line.replaceAll(
						AuthorTools.PUNCTUATION_REGEX, "").trim();
				// NUM LIGNE
				numLine++;
				linesMap.put(numLine, lineWithoutPunctuation);
				block.linesMap = linesMap;
				block.appendSentence(line);
			}
		} catch (FileNotFoundException e) {
			String msg = "File " + file + " not found";
			System.out.println(msg);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		extractRelatifAttributes(blockList);
		saveAttributes(blockList);
	}

	/**
	 * Données relatifs à toute la page <br>
	 * .
	 * 
	 * @param blockList
	 */
	void extractRelatifAttributes(List<Block> blockList) {
		List<Person> personList;

		boolean savePerson;
		if (authorPageFound) {

			for (int i = 0; i < blockList.size(); i++) {
				savePerson = true;
				personList = blockList.get(i).personList;
				// Données relatifs à toute la page
				for (Person person : personList) {
					person.relatifNumberWords = (float) person.nbWordsInBlock
							/ totalNumberOfWords;
					person.relatifNumBlock = (float) (i + 1) / blockList.size();

					// NUM LINE
					float relatifNumLine = (float) person.numLine / numLine;
					person.relatifNumLine = relatifNumLine;
					if (person.isAutor == 1) {
						if (relatifNumLine > maxRelatifNumLine)
							maxRelatifNumLine = relatifNumLine;
						if (relatifNumLine < minRelatifNumLine)
							minRelatifNumLine = relatifNumLine;
					}

					// NB Words
					if (person.nbWordsInBlock > maxNbWordsInBlock)
						maxNbWordsInBlock = person.nbWordsInBlock;

					// TOP & END
					if (relatifNumLine < topPosition)
						person.topPosition = true;
					if (relatifNumLine > endPosition)
						person.endPosition = true;

					if (person.isAutor == 1)
						if (person.topPosition || person.endPosition)
							hasTopEndAuthor = true;

					// Test pour SVM
					// sortie.write(Tools.boolToInt(person.isAutor)
					// + " 1:"
					// + Tools.boolToInt(person.hasDate)
					// + " 2:"
					// + Tools.boolToInt(person.hasDicoTocken)
					// + " 3:"
					// + Tools.boolToInt(person.hasEmail)
					// + " 4:"
					// // + person.nbWordsInBlock
					// // + " 5:"
					// // + person.relatifNumBlock + " 6:"
					// // + person.relatifNumLine + " 7:"
					// + Tools.boolToInt(person.topPosition)
					// + " 5:"
					// + Tools.boolToInt(person.endPosition)
					// + " 6:"
					// // + person.nbVerbs + " 10:" + person.nbNoun
					// // + " 11:"
					// + Tools.boolToInt(person.precededByAuthorTag)
					// + " 7:" + Tools.boolToInt(person.createdBy)
					// + " 8:" + Tools.boolToInt(person.hasPerson)
					// + " 9:" + Tools.boolToInt(person.hasLocation)
					// + " 10:" + Tools.boolToInt(person.hasOrg)
					//
					// + "\n");

					// }
					System.out.println();
				}
			}

		}
	}

	void saveAttributes(List<Block> blockList) {

		List<Person> personList;
		boolean savePerson;
		if (authorPageFound) {
			id++;
			if (nbAuthorsByPage > 1)
				countNbAuthorSup1++;
			try {
				BufferedWriter sortie = new BufferedWriter(new FileWriter(
						personFileName, true));
				System.out.println(" maxNbWords : " + maxNbWordsInBlock);
				for (Block block : blockList) {
					personList = block.personList;

					// Fitrage des personnes à stocker
					for (Person person : personList) {
						savePerson = true;
						String personName = person.name.trim().toLowerCase();
						if (falsePersonList.contains(personName)
								|| personName.length() < 3
								|| personName.contains("@")
								|| personName.contains("=")
								|| personName.contains("introduction")
								|| personName.contains("search")
								|| personName.contains("page")
								|| personName.contains("page"))
							savePerson = false;

						if (nbAuthorsByPage > 1) {
							// if (person.isAutor == 1
							// && person.avgAutthor < maxAvgAuthor) {
							// person.isAutor = 0;
							// savePerson = false;
							// }

							if (person.isAutor == 1) {
								if (hasTopEndAuthor) {
									if (person.relatifNumLine > topPosition
											&& person.relatifNumLine < endPosition) {
										nbAuthorsByPage--;
										person.isAutor = 0;
										// savePerson = false;
									}
								} else if (person.relatifNumLine != maxRelatifNumLine
										&& person.relatifNumLine != minRelatifNumLine) {
									nbAuthorsByPage--;
									person.isAutor = 0;
									// savePerson = false;
								}

							}
							if (person.relatifNumLine > 0.4
									&& person.relatifNumLine < 0.8)
								savePerson = false;
							// else if (hasTopEndAuthor) {
							// savePerson = false;
							// }
						}
						// MAX MIN
						if (person.relatifNumLine == maxRelatifNumLine)
							person.maxRelatifNumLine = true;
						if (person.relatifNumLine == minRelatifNumLine)
							person.minRelatifNumLine = true;
						// NB WORDS
						float relatifNbWordsInBlok = (float) person.nbWordsInBlock
								/ maxNbWordsInBlock;
						person.relatifNbWordsInBlok = relatifNbWordsInBlok;

						if (savePerson)
							sortie
									.write(id
											+ ","
											+ "'"
											+ currentUrl
											+ "'"
											+ ","
											+ "'"
											+ person.name.replaceAll(
													AuthorTools.PUNCTUATION_REGEX,
													" ")
											+ "'"
											+ ","
											+ AuthorTools.boolToInt(person.hasDate)
											+ ","
											+ AuthorTools
													.boolToInt(person.hasDicoTocken)
											+ ","
											+ AuthorTools.boolToInt(person.hasEmail)
											+ ","
											+ person.relatifNbWordsInBlok
											+ ","
											+ person.relatifNumBlock
											+ ","
											+ person.relatifNumLine
											+ ","
											+ AuthorTools
													.boolToInt(person.maxRelatifNumLine)
											+ ","
											+ AuthorTools
													.boolToInt(person.minRelatifNumLine)
											+ ","
											+ AuthorTools
													.boolToInt(person.topPosition)
											+ ","
											+ AuthorTools
													.boolToInt(person.endPosition)
											+ ","
											+ person.nbVerbs
											+ ","
											+ person.nbNoun
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
											+ AuthorTools.boolToInt(person.hasAdress)
											+ ","
											+ AuthorTools.boolToInt(person.hasPhone)
											+ "," + person.isAutor + "\n");

						System.out.println("--- Person : " + person.name + "-"
								+ id + "-" + " Email : " + person.hasEmail
								+ " Date : " + person.hasDate + " Location : "
								+ person.hasLocation + " Org : "
								+ person.hasOrg + " RelatifNumBlock : "
								+ person.relatifNumBlock + " RelatifNbWords : "
								+ person.relatifNumberWords + " nbVerbs : "
								+ person.nbVerbs + " Person : "
								+ person.hasPerson + " Dico : "
								+ person.hasDicoTocken + " AuthorBefore : "
								+ person.precededByAuthorTag + " distance : "
								+ person.avgAutthor + " IsAuthor : "
								+ person.isAutor + " authorFound : "
								+ authorPageFound + " NUMLINE : "
								+ person.numLine + " relatifNumLine : "
								+ person.relatifNumLine);
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
				sortie2.write("-" + currentUrl + "--" + currentAuthor + "\n");
				sortie2.flush();
				sortie2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Tokenize and tag the text. retoure la liste des tags des mots du texte
	 * 
	 * @param text
	 * @return
	 */
	ArrayList<String> tagWithLingpipe(String text) {
		ArrayList<String> listTags = new ArrayList<String>();

		String line = text;
		char[] cs = line.toCharArray();
		com.aliasi.tokenizer.Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(
				cs, 0, cs.length);
		String[] tokens = tokenizer.tokenize();
		String[] tags = decoder.firstBest(tokens);
		// for (int i = 0; i < tokens.length; ++i)
		// System.out.print(tokens[i] + "_" + tags[i] + " | ");

		for (int i = 0; i < tags.length; ++i)
			listTags.add(tags[i]);

		return listTags;

	}

	private Chunk[] extractByLingpipeNE(String txt, String[] patterns) {
		Chunking chunking = getChunker().chunk(txt);
		Set chunkSet = chunking.chunkSet();
		Iterator it = chunkSet.iterator();
		List<Chunk> personFoundList = new ArrayList<Chunk>();
		List<String> patternsList = new ArrayList<String>(Arrays
				.asList(patterns));
		while (it.hasNext()) {
			Chunk chunk = (Chunk) it.next();
			String type = chunk.type();
			int start = chunk.start();
			int end = chunk.end();
			String phrase = txt.substring(start, end);
			System.out.println(phrase + "_" + type);
			if (patternsList.contains(type)) {
				personFoundList.add(chunk);
			}
		}
		return (Chunk[]) personFoundList.toArray(new Chunk[personFoundList
				.size()]);

	}

	private Chunker getChunker() {
		File modelFile;
		try {
			if (chunker == null) {
				modelFile = new File(model);
				System.out.println("Reading chunker from file=" + modelFile);
				chunker = (Chunker) AbstractExternalizable
						.readObject(modelFile);
			}
			return chunker;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}

	public class Block {
		private StringBuffer sentence = new StringBuffer();
		private int wordNumber;
		public List<Person> personList = new ArrayList<Person>();
		private List<String> locationList = new ArrayList<String>();
		private List<String> orgList = new ArrayList<String>();
		Map<Integer, String> linesMap = new HashMap<Integer, String>();
		boolean analysed = false;
		TokenList alTokenList;
		// private Tokenizer tokenizer;
		int windowSize;

		public Block(int windowSize) {
			super();
			this.windowSize = windowSize;
		}

		public void appendSentence(String sentence) {
			this.sentence.append(sentence);
			analysed = false;
		}

		public void balieAnalyse() {
			{
				tokenizer.Reset();
				tokenizer.Tokenize(sentence.toString());
				alTokenList = tokenizer.GetTokenList();
				NamedEntityRecognitionNerf ner = new NamedEntityRecognitionNerf(
						alTokenList, lexicon, disambiguationRules,
						new PriorCorrectionNerf(),
						NamedEntityTypeEnumMappingNerf.values(), true);
				ner.RecognizeEntities();
				alTokenList = ner.GetTokenList();
				analysed = true;
				System.out.println("\nBlock");
				System.out.println(sentence.toString());

				String namePerson = "";
				int start = 0;
				int end = 0;
				boolean isEnd = false;
				// words feature creation
				for (int i = 0; i < alTokenList.Size(); i++) {
					boolean addToList = true;
					if (isEnd == true)
						namePerson = "";
					isEnd = false;
					String namedEntity = alTokenList.Get(i).EntityType()
							.GetLabel(NamedEntityTypeEnumMappingNerf.values());
					String name = alTokenList.Get(i).Raw();
					// System.out.println(name + "_"
					// + alTokenList.Get(i).PartOfSpeech());
					// PERSON
					if (namedEntity != null) {
						if (namedEntity.equals("PERSON")) {
							namePerson += name + " ";
							if (alTokenList.Get(i).EntityType().IsStart() == true) {
								start = i;
							}
							if (alTokenList.Get(i).EntityType().IsEnd() == true) {
								end = i;
								isEnd = true;
							}
							if (isEnd == true) {
								// System.out.println("PERSON : " + namePerson
								// + "--");
								namePerson = namePerson.replaceAll(
										AuthorTools.PUNCTUATION_REGEX, " ");
								namePerson = namePerson.replaceAll("\\s+", " ");
								namePerson = namePerson.trim();
								// System.out.println("PERSON : " + namePerson
								// + "-");
								Person person = new Person(namePerson, start,
										end);

								// Verifier si c'est l'AUTEUR
								float distance = AuthorTools.distanceBetweenTexts(
										namePerson, currentAuthor);
								if (distance > maxAvgAuthor)
									maxAvgAuthor = distance;
								person.avgAutthor = distance;
								if (distance >= 0.5) {
									person.isAutor = 1;
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
				chekBalieAttributes(allPersonList);
				if (personList.size() == 0) {
					this.wordNumber = 0;
					String subString = sentence.toString().trim();
					subString = subString.replaceAll(AuthorTools.PUNCTUATION_REGEX,
							" ");
					// Extraire les tokens
					char[] cs = subString.toCharArray();
					com.aliasi.tokenizer.Tokenizer lingpipeTokenizer = TOKENIZER_FACTORY
							.tokenizer(cs, 0, cs.length);
					String[] tokens = lingpipeTokenizer.tokenize();
					this.wordNumber = tokens.length;
				}
				totalNumberOfWords += this.wordNumber;
				System.out.println("totalNumberOfWords : " + totalNumberOfWords
						+ " wordNumber : " + this.wordNumber);
			}
		}

		/**
		 * 
		 */
		void chekBalieAttributes(List<String> allPersonList) {
			List<String> patternsList = new ArrayList<String>(Arrays
					.asList(AuthorTools.authorPatterns));
			List<String> createdByList = new ArrayList<String>(Arrays
					.asList(AuthorTools.createdByPatterns));
			List<String> nounPosList = new ArrayList<String>(Arrays
					.asList(AuthorTools.nounPos));
			List<String> verbPosList = new ArrayList<String>(Arrays
					.asList(AuthorTools.verbPos));
			int nbWords;
			this.wordNumber = 0;

			for (int i = 0; i < personList.size(); i++) {
				Person person = personList.get(i);

				// NUMLINE
				String nameWithoutPunctuation = person.name.replaceAll(
						AuthorTools.PUNCTUATION_REGEX, "").trim();
				Set cles = linesMap.keySet();
				Iterator it = cles.iterator();
				while (it.hasNext()) {
					Integer cle = (Integer) it.next();
					String phrase = linesMap.get(cle);
					if (phrase.contains(nameWithoutPunctuation)) {
						person.numLine = cle;
					}
				}
				int nounNumber = 0;
				int verbNumber = 0;
				// WINDOW BEFORE
				String windowBefore = "";
				int startPersonPosition = person.start;
				// Nbre du block avant personne
				int allNbWordsBefore = 0;
				int indexBefore;
				if (startPersonPosition > 0) {
					indexBefore = startPersonPosition - 1;
					while (indexBefore >= 0) {
						if (!alTokenList.Get(indexBefore).Raw().matches(
								AuthorTools.PUNCTUATION_REGEX)) {
							String token = alTokenList.Get(indexBefore).Raw();
							String tag = alTokenList.Get(indexBefore)
									.PartOfSpeech().toLowerCase();
							allNbWordsBefore++;
							// POS
							if (allNbWordsBefore < windowSize) {
								windowBefore = token + " " + windowBefore;
								if (verbPosList.contains(tag)) {
									verbNumber++;
								}
								if (nounPosList.contains(tag)) {
									nounNumber++;
								}
								if (token.equals(EMAIL_TAG)) {
									person.hasMail = true;
								}
								// Created By
								if (indexBefore >= 1
										&& createdByList.contains(token
												.toLowerCase())) {
									String nextToken = alTokenList.Get(
											indexBefore + 1).Raw();
									if (nextToken.toLowerCase().equals("by"))
										person.createdBy = true;
								}
								// PrecedByAuthor
								if (allNbWordsBefore == 1
										|| allNbWordsBefore == 2) {
									if (patternsList.contains(token
											.toLowerCase())) {
										person.precededByAuthorTag = true;
									}
								}
								if (allNbWordsBefore == 1
										&& token.toLowerCase().equals("by")) {
									person.createdBy = true;
								}
							}
						}
						indexBefore--;
					}
				}

				// WINDOW AFTER
				int endPersonPosition = person.end;
				String windowAfter = "";
				int indexAfter;
				// Nbre du block après personne
				int allNbWordsAfter = 0;
				if (endPersonPosition < alTokenList.Size()) {
					indexAfter = endPersonPosition + 1;
					while (indexAfter < alTokenList.Size()) {
						String token = alTokenList.Get(indexAfter).Raw();
						String tag = alTokenList.Get(indexAfter).PartOfSpeech()
								.toLowerCase();

						if (!token.matches(AuthorTools.PUNCTUATION_REGEX)) {
							allNbWordsAfter++;
							if (allNbWordsAfter < windowSize) {
								windowAfter = windowAfter + " " + token;
								if (verbPosList.contains(tag)) {
									verbNumber++;
								}
								if (nounPosList.contains(tag)) {
									nounNumber++;
								}
								if (token.equals(EMAIL_TAG)) {
									person.hasMail = true;
								}
							}
						}
						indexAfter++;
					}
				}

				person.nbNoun = nounNumber;
				person.nbVerbs = verbNumber;
				// TOTAL WINDOW
				nbWords = allNbWordsBefore + allNbWordsAfter;
				this.wordNumber = nbWords;
				StringBuffer window = new StringBuffer();
				window.append(windowBefore).append(" ").append(windowAfter);
				// Location
				for (String location : this.locationList) {
					if (window.toString().contains(location)) {
						person.hasLocation = true;
						break;
					}
				}
				// Organisation
				for (String organisation : this.orgList) {
					if (window.toString().contains(organisation)) {
						person.hasOrg = true;
						break;
					}
				}
				// PHONE
				if (window.toString().toLowerCase().contains("tel")
						|| window.toString().contains("telephone")
						|| window.toString().contains("phone"))
					person.hasPhone = true;

				// ADRESS
				if (window.toString().contains("adress"))
					person.hasAdress = true;

				// Personne
				for (Person personne : this.personList) {
					if (window.toString().contains(personne.name)) {
						person.hasPerson = true;
						break;
					}
				}
				// Dictionnaire
				for (String dicoWord : dicoList) {
					if (window.toString().toLowerCase().contains(dicoWord)) {
						person.hasDicoTocken = true;
						break;
					}
				}
				// Date
				boolean hasDate = AuthorTools.containDate(window.toString());
				if (hasDate == true)
					person.hasDate = hasDate;
				// Email
				boolean hasMail = window.toString().contains(EMAIL_TAG);
				if (hasMail == true)
					person.hasEmail = hasMail;
				// Words Number
				person.nbWordsInBlock = wordNumber;
			}
		}

		/***************************************************************************************************************************************/

		/** Méthode avec Lingpipe */

		/***************************************************************************************************************************************/

		/**
		 * Extraire les Entités Nommées, mettre les personnes dans "personList",
		 * les lieux dans "locationList" et les organisation dans "orgList".
		 */
		public void analyseWithLingpipe() {
			final String[] patterns = new String[] { "PERSON", "ORGANIZATION",
					"LOCATION" };
			Chunk[] chunks = extractByLingpipeNE(sentence.toString(), patterns);
			analysed = true;
			System.out.println("\nBlock");
			System.out.println(sentence.toString());

			for (int i = 0; i < chunks.length; i++) {
				Chunk chunk = chunks[i];
				String entityName = sentence.substring(chunk.start(), chunk
						.end());
				if (chunk.type().equals("PERSON")) {
					if (Character.isUpperCase(entityName.charAt(0))
							&& entityName.length() > 0) {
						Person person = new Person(entityName, chunk.start(),
								chunk.end());
						personList.add(person);
					}
				} else if (chunk.type().equals("LOCATION")) {
					locationList.add(entityName);
				} else if (chunk.type().equals("ORGANIZATION")) {
					orgList.add(entityName);
				}
				checkAttributesWithLingpipe();

			}

			if (personList.size() == 0) {
				this.wordNumber = 0;
				String subString = sentence.toString().trim();
				subString = subString.replaceAll(AuthorTools.PUNCTUATION_REGEX, " ");
				// Extraire les tokens
				char[] cs = subString.toCharArray();
				com.aliasi.tokenizer.Tokenizer tokenizer = TOKENIZER_FACTORY
						.tokenizer(cs, 0, cs.length);
				String[] tokens = tokenizer.tokenize();
				this.wordNumber = tokens.length;
			}
			System.out.println("WordNumber : " + this.wordNumber);
			totalNumberOfWords += this.wordNumber;
		}

		/**
		 * - Traiter la phrase précédant chaque nom de personne: extraire les
		 * Tokens, les POS, le nombre de tokens, des verbes dans la fenêtre. <br>
		 * <br>
		 * - Sélectionner dans cette phrase une fenêtre de taille windowSize.<br>
		 * <br>
		 * - Vérifier si la fenêtre contient un lieu, une organisation, une date
		 * ou un email.
		 */
		private void checkAttributesWithLingpipe() {

			for (int i = 0; i < personList.size(); i++) {
				this.wordNumber = 0;
				Person person = personList.get(i);
				// Verifier si c'est l'AUTEUR
				float distance = AuthorTools.distanceBetweenTexts(person.name,
						currentAuthor);
				person.avgAutthor = distance;
				if (distance >= 0.5) {
					person.isAutor = 1;
					authorPageFound = true;
					nbAuthorsByPage++;
				}
				/******
				 * Window Before Person
				 */
				String subStringBeforePerson = sentence.substring(0,
						person.start).trim();
				subStringBeforePerson = subStringBeforePerson.replaceAll(
						AuthorTools.PUNCTUATION_REGEX, " ");
				// Extraire les tokens
				char[] csBefore = subStringBeforePerson.toCharArray();
				com.aliasi.tokenizer.Tokenizer tokenizer = TOKENIZER_FACTORY
						.tokenizer(csBefore, 0, csBefore.length);
				String[] tokensBefore = tokenizer.tokenize();
				// Construire la fenêtre
				String[] selectedTokensBefore;
				this.wordNumber += tokensBefore.length;
				if (tokensBefore.length > windowSize) {
					selectedTokensBefore = new String[windowSize];
					int selectedTokensIndex = 0;
					int startTokensIndex = tokensBefore.length - windowSize;
					for (int j = startTokensIndex; j < tokensBefore.length; j++) {
						selectedTokensBefore[selectedTokensIndex++] = tokensBefore[j];
					}
				} else {
					selectedTokensBefore = tokensBefore;
				}
				StringBuffer windowBefore = new StringBuffer();
				// Extraire les POS de la fenêtre, compter le nombre de
				// verbes
				String[] tagsBefore = decoder.firstBest(selectedTokensBefore);
				for (int j = 0; j < tagsBefore.length; j++) {
					if (tagsBefore[j].equals("mid")
							|| tagsBefore[j].contains("vb")) {
						// this.verbNumber++;
					}
					if (selectedTokensBefore[j].equals(EMAIL_TAG)) {
						person.hasMail = true;
					}
					windowBefore.append(selectedTokensBefore[j] + " ");
				}
				// PrecedByAuthor
				String tokenBeforeAuthor = "";
				String tokenBeforeAuthor2 = "";
				if (selectedTokensBefore.length > 0)
					tokenBeforeAuthor = selectedTokensBefore[selectedTokensBefore.length - 1]
							.toLowerCase();
				if (selectedTokensBefore.length > 1)
					tokenBeforeAuthor2 = selectedTokensBefore[selectedTokensBefore.length - 2]
							.toLowerCase();
				List<String> patternsList = new ArrayList<String>(Arrays
						.asList(AuthorTools.authorPatterns));
				if (patternsList.contains(tokenBeforeAuthor)
						|| patternsList.contains(tokenBeforeAuthor2)) {
					person.precededByAuthorTag = true;
				}
				if (tokenBeforeAuthor.equals("by")) {
					person.hasDicoTocken = true;
				}
				/******
				 * Window After Person
				 */
				String subStringAfterPerson = sentence.substring(person.end,
						sentence.length()).trim();
				subStringAfterPerson = subStringAfterPerson.replaceAll(
						AuthorTools.PUNCTUATION_REGEX, " ");
				// Extraire les tokens
				char[] csAfter = subStringAfterPerson.toCharArray();
				tokenizer = TOKENIZER_FACTORY.tokenizer(csAfter, 0,
						csAfter.length);
				String[] tokensAfter = tokenizer.tokenize();
				// Construire la fenêtre
				String[] selectedTokensAfter;
				this.wordNumber += tokensAfter.length;
				if (tokensAfter.length > windowSize) {
					selectedTokensAfter = new String[windowSize];
					int selectedTokensIndex = 0;
					int endTokensIndex = windowSize;
					for (int j = 0; j < endTokensIndex; j++) {
						selectedTokensAfter[selectedTokensIndex++] = tokensAfter[j];
					}
				} else {
					selectedTokensAfter = tokensAfter;
				}
				StringBuffer windowAfter = new StringBuffer();
				// Extraire les POS de la fenêtre, compter le nombre de
				// verbes
				String[] tagsAfter = decoder.firstBest(selectedTokensAfter);
				for (int j = 0; j < tagsAfter.length; j++) {
					if (tagsAfter[j].equals("mid")
							|| tagsAfter[j].contains("vb")) {
						// this.verbNumber++;
					}
					if (selectedTokensAfter[j].equals(EMAIL_TAG)) {
						person.hasMail = true;
					}
					windowAfter.append(selectedTokensAfter[j] + " ");
				}
				/*****
				 * Fentere avant et après
				 */

				StringBuffer window = new StringBuffer();
				window.append(windowBefore).append(" ").append(windowAfter);
				// Location
				for (String location : this.locationList) {
					if (window.toString().contains(location)) {
						person.hasLocation = true;
						break;
					}
				}
				// Organisation
				for (String organisation : this.orgList) {
					if (window.toString().contains(organisation)) {
						person.hasOrg = true;
						break;
					}
				}
				// Personne
				for (Person personne : this.personList) {
					if (window.toString().contains(personne.name)) {
						person.hasPerson = true;
						break;
					}
				}
				// Dictionnaire
				for (String dicoWord : dicoList) {
					if (window.toString().toLowerCase().contains(dicoWord)) {
						person.hasDicoTocken = true;
						break;
					}
				}
				// Date
				boolean hasDate = authorExtract.AuthorTools.containDate(window
						.toString());
				if (hasDate == true)
					person.hasDate = hasDate;
				// Email
				boolean hasMail = window.toString().contains(EMAIL_TAG);
				if (hasMail == true)
					person.hasEmail = hasMail;
				// Words Number
				person.nbWordsInBlock = wordNumber;
			}

		}

	}

	public class Person {
		String name;
		int start;
		int end;
		int nbVerbs;
		int nbNoun;
		int numTag;
		int numLine;
		float relatifNumLine;

		int nbWordsInBlock;
		float relatifNumberWords;
		float relatifNumBlock;
		float relatifNbWordsInBlok;

		// Date
		boolean hasDate;

		// Mots du dictionnaire
		boolean hasDicoTocken;

		// Email
		boolean hasEmail;

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
		int isAutor = 0;
		float avgAutthor;

		// createdBy
		boolean createdBy;

		// position
		boolean topPosition;
		boolean endPosition;
		boolean maxRelatifNumLine;
		boolean minRelatifNumLine;

		// PHONE
		boolean hasPhone;

		// Adress
		boolean hasAdress;

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

	}
}
