package authorExtract;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qtag.Tagger;

import lipgpipeDemo.EmailRegExChunker;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.util.AbstractExternalizable;

public class AuthorTools {
	final static String EMAIL_TAG = "mail";
	final static String PUNCTUATION_REGEX = "\\?|\\'|\\-|<|>|\\[|\\]|\\{|\\}|\\/|\\,|\\;|\\.|\"|\\(|\\)|\\:|\\!|\\*";

	final static String[] authorPatterns = new String[] { "author", "creator",
			"authors", "creators", "designer", "contributor", "writer",
			"designers", "contributors", "writers", "source" };

	final static String[] createdByPatterns = new String[] { "design by",
			"designed", "created", "realised", "revised", "updated",
			"modified", "founded", "provided", "powered", "maintained",
			"supported", "written", "founded", "revised", "composed",
			"content", "posted" };

	final static String[] verbPos = new String[] { "vb", "vbb", "vbd", "vbi",
			"vbn", "vbg", "vbz", "vdb", "vdd", "vdg", "vdi", "vdn", "vdz",
			"vhb", "vhd", "vhg", "vhi", "vhn", "vhz", "vmo", "vvb", "vvd",
			"vvg", "vvi", "vvn", "vvz", "md", "do", "hvz", "bez" };

	final static String[] blockSeparation = new String[] { "hr", "table", "h1",
			"h2", "h3" };// "div",

	final static String[] falsePerson = new String[] { "introduction" };

	final static String[] nounPos = { "np" };

	/**
	 * Utliser Lingpipe pour detecter le mail dans un texte
	 * 
	 * @param text
	 * @return
	 */
	static String emailDetector(String text) {

		boolean isMail = false;
		Chunker chunker = new EmailRegExChunker();
		Chunking chunking;

		try {
			chunking = chunker.chunk(text);
			Set chunkSet = chunking.chunkSet();
			Iterator it = chunkSet.iterator();
			if (it.hasNext()) {
				Chunk chunk = (Chunk) it.next();
				int start = chunk.start();
				int end = chunk.end();
				String txt = text.substring(start, end);
				text = text.replace(txt, EMAIL_TAG);
				// System.out.println("txt " + txt);
				isMail = true;
			}
		} catch (Exception e) {
			System.out.println("unable");
			e.printStackTrace();
		}

		return text;

	}

	static boolean domEmailDetector(String text) {

		boolean isMail = false;
		Chunker chunker = new EmailRegExChunker();
		Chunking chunking;

		try {
			chunking = chunker.chunk(text);
			Set chunkSet = chunking.chunkSet();
			Iterator it = chunkSet.iterator();
			if (it.hasNext()) {
				Chunk chunk = (Chunk) it.next();
				isMail = true;
			}
		} catch (Exception e) {
			System.out.println("unable");
			e.printStackTrace();
		}

		return isMail;

	}

	static int boolToInt(boolean iVal) {
		int b = 0;
		if (iVal == true)
			b = 1;
		return b;
	}

	// b c'est l'auteur
	static float distanceBetweenTexts(String text, String author) {
		int nbExist = 0;
		float avg;
		text = text.replaceAll(PUNCTUATION_REGEX, " ");
		author = author.replaceAll(PUNCTUATION_REGEX, " ");
		text = text.trim();
		author = author.trim();
		text = text.toLowerCase();
		author = author.toLowerCase();
		
		String[] textTokens = text.split("\\s+");
		List<String> listTextTokens = new ArrayList<String>(Arrays.asList(textTokens));

		String[] authorTokens = author.split("\\s+");

		for (int i = 0; i < authorTokens.length; i++) {
			String word = authorTokens[i].trim();
			if (listTextTokens.contains(word))
				nbExist++;
		}
		avg = ((float) nbExist / authorTokens.length);

		return avg;
	}

	/**
	 * distance d'édition
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	static float distanceEdition(String a, String b) {
		a = a.replaceAll(PUNCTUATION_REGEX, " ");
		b = b.replaceAll(PUNCTUATION_REGEX, " ");
		a = a.trim();
		b = b.trim();
		System.out.println("a= " + a + " b= " + b);
		int distanceEdition;
		float score;
		int n = a.length(), m = b.length();

		int[][] matrice = new int[n + 1][m + 1];
		for (int i = 0; i < n; i++) {
			matrice[i][0] = i;
		}
		for (int j = 0; j < m; j++) {
			matrice[0][j] = j;
		}
		for (int i = 1; i < n; i++) {
			for (int j = 1; j < m; j++) {
				int cout = (a.charAt(i) == b.charAt(j)) ? 0 : 1;
				matrice[i][j] = minimum(1 + matrice[i][j - 1],
						1 + matrice[i - 1][j], cout + matrice[i - 1][j - 1]);
			}
		}
		distanceEdition = matrice[n - 1][m - 1];
		System.out.println("distanceEdition = " + distanceEdition + " max = "
				+ Math.max(n, m));
		score = (float) distanceEdition / Math.max(a.length(), b.length());
		return score;
	}

	/**
	 * verifier si chaine contient une date ou pas. verifier si chaine est sous
	 * la forme
	 * 
	 * @param chaine
	 *            1982 05
	 * @return
	 */
	public static boolean containDate(String chaine) {
		boolean contain = false;
		String eng = "([0-1]?[0-9][\\.\\-\\/][0-3][0-9][\\.\\-\\/](19[0-9]{2}|20[0-9]{2}|[0-9][0-9]))";// 11.24
		// .08
		String fr = "([0-3][0-9][\\.\\-\\/][0-1][0-9][\\.\\-\\/](19[0-9]{2}|20[0-9]{2}|[0-9][0-9]))";// 11
		// -
		// 05
		// -
		// 2008

		String annee = "(19[0-9]{2}|20[0-9]{2})";

		String modele = "\\w*\\s*(" + annee + "|" + fr + "|" + eng + ").*";
		Pattern pattern = Pattern.compile(modele);
		Matcher matcher = pattern.matcher(chaine);
		while (matcher.find()) {
			contain = true;
			String texte = matcher.group(2);
		}
		return contain;
	}

	static int minimum(int el1, int el2, int el3) {
		int min;
		if (el1 < el2)
			min = el1;
		else
			min = el2;

		if (min > el3)
			min = el3;

		return min;

	}

	public static void main(String[] args) {
		String text = "    * Aug 02, 2007 9:43 AM PST";
		String auteur = "Sahar Tolley Howard";
		System.out.println(containDate(text));
	}

	/*********************************************** Non utilisés ***********************************************/

	/**
	 * tagger chaque token de la liste des tokens avec Tree Tagger
	 * 
	 * @param listTokens
	 *            liste des token
	 * @return retourne un texte taggué sous la forme : mot1[tag1] mot2[tag2]...
	 */
	List<String> tagList(List<String> listTokens, String lang) {
		List<String> listTags = new ArrayList<String>();
		String[] tags = null;
		String typeTag = "qtag-eng";
		if (lang.equals("eng"))
			typeTag = "qtag-eng";
		else if (lang.equals("fr"))
			typeTag = "qtag-fr";
		try {
			// Tagger le texte initial
			Tagger tagger = new Tagger(typeTag);
			tags = tagger.tag(listTokens); // tableau des tags
			for (int i = 0; i < tags.length; i++) {
				listTags.add(tags[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listTags;
	}

	/**
	 * Stem a word (en anglais)
	 * 
	 * @param word
	 * @return the word stemmed
	 */
	String stemWord(String word) {
		authorTools.Stemmer stem = new authorTools.Stemmer();
		for (int i = 0; i < word.length(); i++) {
			// System.out.println("-");
			char ch = word.charAt(i);
			if (Character.isLetter((char) ch)) {
				ch = Character.toLowerCase((char) ch);
				stem.add(ch);
			}
		}
		stem.stem();
		String stemmedText = stem.toString();
		return stemmedText;
	}

	/**
	 * Recuperer tout le texte du document, trouver sa langue et suivant la
	 * langue tagger le texte avec treeTagger, pour cela on enregistre le texte
	 * dans un fichier et on execute la commande du treeTagger avec le nom de
	 * fichier en paramètre.
	 * 
	 * @param ListAuthors
	 */
	void treeTagg(String text) {
		// allText = allText.substring(0, allText.length() - 4);
		String lang = null;
		String cmdLang = "tag-english";
		int numTxt = 0; // numero du noeud texte
		int nbVerb = 0;// nbre de verbes
		int nbNoun = 0;// nbre de noms
		int nbWords = 0;// nbre de mots
		int nbPunctuation = 0;// nbre de ponctuations

		/** detection de la langue avec lingpipe **/
		File modelFile = new File("models/langid-leipzig.classifier");
		Classifier classifier;
		try {
			classifier = (Classifier) AbstractExternalizable
					.readObject(modelFile);
			Classification classification = classifier.classify(text);
			if (classification.bestCategory() != null)
				lang = classification.bestCategory();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("language = " + lang);
		/**
		 * Mettre le texte dans un fichier et le tagger avec tree tagger
		 **/
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("text.txt"));
			out.write(text);
			out.flush();
			out.close();
		} catch (IOException e) {
		}

		if (lang.equals("fr"))
			cmdLang = "tag-french";
		// else if (getPageLang().equals("gr"))
		// cmdLang = "tag-german";
		// else if (getPageLang().equals("it"))
		// cmdLang = "tag-italian";
		// else if (getPageLang().equals("sp"))
		// cmdLang = "tag-spanish";

		String cmdline = "cmd.exe /c " + cmdLang + " text.txt";

		try {
			Process p = Runtime.getRuntime().exec(cmdline);
			// get its tree tagger output (gate input)
			BufferedReader input = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			/*
			 * Chaque ligne taggée va etre inseree dans wordsList est une list
			 * de mots avec leurs tags
			 */
			ArrayList<String[]> wordsList = new ArrayList<String[]>();
			String line;
			// Chaque ligne caracterise un mot
			while ((line = input.readLine()) != null) {
				if (line.split("\t").length > 1) {
					String[] elems = line.split("\t");

					// les noeuds texte sont séparerf par -- dans le fichier
					if (!elems[0].equals("---")) {

						// verifier si le texte contient un nom propre
						if (elems[1].equals("NAM") || elems[1].contains("NP"))

							if (elems[1].equals("NOM")
									|| elems[1].equals("NAM")
									|| elems[1].contains("NN")) {
								nbNoun++;
							}
						if (elems[1].contains("VER") || elems[1].contains("VB"))
							nbVerb++;
						if (elems[0]
								.matches("\\?|\\'|\\-|<|>|\\[|\\]|\\{|\\}|\\/|\\,|\\;|\\.|\"|\\(|\\)|\\:|\\!")) {
							nbPunctuation++;
						}
						wordsList.add(elems);
						nbWords++;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	static String nettoyeText(String text) {
		while (text.startsWith("&nbsp;"))
			text = text.substring(6);

		text = text.replaceAll(
				"\\'|-|<|>|:|\\[|\\]|\\{|\\}|\\/|,|;|\\.|\"|\\(|\\)|\\!|\\s",
				" ");
		text = text.replaceAll("&nbsp;", " ");
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
		text = text.replaceAll("Ãª", "é");
		text = text.replaceAll("\\'", " ");
		return text;
	}

}
