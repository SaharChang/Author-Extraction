package authorQueryWeb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.htmlparser.Parser;
//import org.htmlparser.filters.TagNameFilter;
//import org.htmlparser.tags.MetaTag;
//import org.htmlparser.util.NodeList;
//import org.htmlparser.util.ParserException;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jvnet.argos.SearchResult;
import org.jvnet.argos.Searcher;
import org.jvnet.argos.SimultaneousSearcher;
import org.jvnet.argos.msn.MSNWebSearcher;
import org.jvnet.argos.yahoo.YahooWebSearcher;

/**
 * Classe utilisée à la fin.s
 * 
 * Poser des requêtes sur MSN.
 * 
 * @author changuel
 * 
 */
public class ConstructCorpus {
	static ArrayList<String> queryListe;
	String query;
	int nbHasAuthor = 0;

	int nbWithoutAuthor = 0;
	ArrayList<String> dicoList;
	String dicoFile = "dico";

	public static void main(String[] args) {

		String requete;
		ConstructCorpus CC = new ConstructCorpus();
		// CC.loadDicoList();
		CC.getQueryFromFile();
		for (int i = 0; i < queryListe.size(); i++) {
			requete = queryListe.get(i);
			System.out.println("requete = " + requete);
			CC.setQuery(requete);
			// CC.searcAuthorInText();
			CC.searching();
		}
	}

	/**
	 * chaque ligne du fichier est une requete <br>
	 * ajouter chaque requete a queryListe
	 */
	void getQueryFromFile() {

		queryListe = new ArrayList<String>();
		String ligne = "";
		String fichier = "queries.txt";
		BufferedReader ficTexte;
		try {
			ficTexte = new BufferedReader(new FileReader(new File(fichier)));
			if (ficTexte == null) {
				throw new FileNotFoundException("Fichier non trouvé: "
						+ fichier);
			}
			while ((ligne = ficTexte.readLine()) != null) {
				if (ligne != null) {
					queryListe.add(ligne);
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
	 * pour chaque requete de la liste interroger le moteur de recherche, 100
	 * resultats maximum
	 * 
	 * Parcourir le resultat et parser chaque page et extraire les attributs
	 */
	void searching() {

		ArrayList<String> listadress = new ArrayList<String>();
		ArrayList<String> listauthors = new ArrayList<String>();
		String adress;
		String author = null;
		List<Searcher> searcherList = new LinkedList<Searcher>();
		searcherList.add(new MSNWebSearcher());
		SimultaneousSearcher searcher = new SimultaneousSearcher(searcherList);

		/**
		 * chercher le modele appliqué a la requete dans le moteur de recherche
		 */
		Iterator<SearchResult> it = searcher.search(query);
		while (it.hasNext()) {
			SearchResult result = it.next();
			adress = result.getAddress().trim(); // url du resultat
			if (adress != null && !adress.contains(".fr")) {
				System.out.println(adress);
				listadress.add(adress);
				listauthors.add("aaa");
			}
		}
		// DomParse domParse = new DomParse();
		// domParse.loadDicoList();
		// domParse.fileNameList = listadress;
		// domParse.authorList = listauthors;
		// domParse.parsingAuthorLists();
	}

	/**
	 * recuperer le resultat des requetes
	 */
	void searcAuthorInText() {
		String modele = "[a-zA-Z]*\\[vbn\\]\\s*\\[[A-Za-z]*\\]\\[person\\]";
		ArrayList<String> listadress = new ArrayList<String>();
		String adress;
		String author = null;

		List<Searcher> searcherList = new LinkedList<Searcher>();
		searcherList.add(new MSNWebSearcher());

		SimultaneousSearcher searcher = new SimultaneousSearcher(searcherList);

		/**
		 * chercher le modele appliqué a la requete dans le moteur de recherche
		 */
		Iterator<SearchResult> it = searcher.search(query);
		while (it.hasNext()) {
			SearchResult result = it.next();
			adress = result.getAddress().trim(); // url du resultat

			System.out.println(" ---- " + adress);
			if (adress != null) {

			}
		}
	}

	/**
	 * Extraire les Metad Tags
	 * 
	 * @param url
	 * @return
	 */
	String extractAuthor(String adress) {
		String authorName = null;

		URL url;
		try {
			url = new URL(adress);

			Parser parser = new Parser(url.toString());
			NodeList list = parser.parse(new TagNameFilter("meta"));
			// auteur : ="Author" , ="Publisher"
			for (int i = 0; i < list.size(); i++) {
				MetaTag tag = (MetaTag) list.elementAt(i);
				String meta = tag.getMetaTagName();
				String metaValue = tag.getMetaContent();
				if (meta != null) {
					meta = meta.toLowerCase();
					if (meta.equals("author") && metaValue != null
							&& !metaValue.equals(""))
						authorName = metaValue.toLowerCase();
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

	public void setQuery(String query) {
		this.query = query;
	}

	public int getNbHasAuthor() {
		return nbHasAuthor;
	}

	public void setNbHasAuthor(int nbHasAuthor) {
		this.nbHasAuthor = nbHasAuthor;
	}

	public int getNbWithoutAuthor() {
		return nbWithoutAuthor;
	}

	public void setNbWithoutAuthor(int nbWithoutAuthor) {
		this.nbWithoutAuthor = nbWithoutAuthor;
	}

}
