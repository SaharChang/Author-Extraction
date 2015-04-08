package authorTools;
import java.net.MalformedURLException;
import java.net.URL;

import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * C'est une classe qui utilise la librairie htmlparser pour parser les fichiers
 * html probleme:
 * 
 * @author Sahar Changuel
 * 
 */
public class ParsingClass {

	/**
	 * Utiliser la librairie htmlParser pour extraire les balises on n'obtient
	 * pas exactement la valeur du tag cherché, mais on obtient toute la ligne
	 * du tag
	 * 
	 * @param adress
	 */
	static void parsing(String adress) {
		try {
			Parser parser = new Parser(adress, new DefaultParserFeedback(
					DefaultParserFeedback.QUIET));

			System.out.println("--------------------Meta--------------------");
			// Chercher les tags Meta
			NodeList nodes = parser.extractAllNodesThatMatch(new TagNameFilter(
					"meta"));
			// Afficher le contenu des tags Meta
			if (nodes.size() > 0) {
				for (int i = 0; i < nodes.size(); i++) {
					System.out.println(nodes.elementAt(i).toHtml());
				}
			}
			// Reset the parser to start from the beginning again.
			parser.reset();
			System.out.println("--------------------Title--------------------");
			// chercher les balises Title
			NodeList nodes1 = parser
					.extractAllNodesThatMatch(new TagNameFilter("title"));
			if (nodes1.size() > 0) {
				for (int i = 0; i < nodes.size(); i++) {
					System.out.println(nodes1.elementAt(i).toHtml());
				}
			}

			// Reset the parser to start from the beginning again.
			parser.reset();
			// Extraire les valeurs de balises ayant tel attribut avec telle
			// valeur
			System.out
					.println("------------------extract attribute----------------------");

			// HasAttributeFilter:This class accepts all tags that have a
			// certain attribute, and optionally, with a certain value.
			NodeList nodes2 = parser
					.extractAllNodesThatMatch(new HasAttributeFilter("name",
							"keywords"));
			if (nodes2.size() > 0) {
				System.out.println(nodes2.size() + " "
						+ nodes2.elementAt(0).toHtml());
			}
			parser.reset();
		} catch (ParserException e) {
			System.out.println("ERROR " + e.toString());
		}
	}

	/**
	 * Transformer du code html en un texte contenant uniquement le contenu
	 * utilisant la librairie htmlparser
	 * 
	 * @param adress
	 */
	static void htmlToText(String adress) {
		StringBuilder text = new StringBuilder();
		try {
			Parser parser = new Parser(adress);
			NodeIterator i = parser.elements();

			while (i.hasMoreNodes())
				text.append(i.nextNode().toPlainTextString());
			System.out.println(text);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	static void extractBody(URL url) {
		try {
			// extract links
			Parser parser = new Parser(url.toString());
			NodeList list = parser.parse(new NodeClassFilter(BodyTag.class));
			for (int i = 0; i < list.size(); i++) {
				BodyTag tag = (BodyTag) list.elementAt(i);
				
				System.out.println("Body = " + tag.getBody());
			}
			parser.reset();
		} catch (ParserException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	/**
	 * extraire le titre
	 * 
	 * @param url
	 */
	public static void extractTitle(URL url) {
		try {
			// extract links
			Parser parser = new Parser(url.toString());
			NodeList list = parser.parse(new NodeClassFilter(TitleTag.class));

			System.out.println("Number of Title =" + list.size());

			for (int i = 0; i < list.size(); i++) {
				TitleTag tag = (TitleTag) list.elementAt(i);
				System.out.println("Title of the page "
						+ tag.getPage().getUrl());
				String title = tag.getTitle();
				System.out.println("is" + title);
			}
			parser.reset();
		} catch (ParserException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	/**
	 * extraire le titre
	 * 
	 * @param url
	 */
	public static void extractTitleFromFile(String fileName) {
		try {
			// extract links
			Parser parser = new Parser(fileName);
			NodeList list = parser.parse(new NodeClassFilter(TitleTag.class));

			System.out.println("Number of Title =" + list.size());

			for (int i = 0; i < list.size(); i++) {
				TitleTag tag = (TitleTag) list.elementAt(i);
				System.out.println("Title of the page "
						+ tag.getPage().getUrl());
				String title = tag.getTitle();
				System.out.println("is" + title);
			}
			parser.reset();
		} catch (ParserException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	/**
	 * Extraire les Metad Tags
	 * 
	 * @param url
	 */
	public static void extractMeta(URL url) {

		try {
			// extract links
			Parser parser = new Parser(url.toString());
			NodeList list = parser.parse(new TagNameFilter("meta"));// ou
			// parser.parse(new
			// NodeClassFilter(MetaTag.class));

			System.out.println("Number of Meta tags =" + list.size());
			// auteur : ="Author" , ="Publisher"
			for (int i = 0; i < list.size(); i++) {
				MetaTag tag = (MetaTag) list.elementAt(i);
				String meta = tag.getMetaTagName();
				String metaValue = tag.getMetaContent();
				System.out.println(meta + " =  " + metaValue);
			}
			parser.reset();
		} catch (ParserException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	public static void main(String[] args) {
		String startUrlString = "http://farside.ph.utexas.edu/teaching/em/em.html";
		// extractTitleFromFile(fileName);
		try {
			URL url = new URL(startUrlString);
			 extractTitle(url);
			//extractMeta(url);
		} catch (MalformedURLException e) {
			System.err.println("invalid url : " + startUrlString);
		}
	}
}
