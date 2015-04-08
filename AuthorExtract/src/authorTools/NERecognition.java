package authorTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;

import qtag.Tagger;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.chunk.NBestChunker;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.parsers.MedPostPosParser;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;
import com.aliasi.xml.SimpleElementHandler;

import ca.uottawa.balie.Balie;
import ca.uottawa.balie.DisambiguationRulesNerf;
import ca.uottawa.balie.LexiconOnDisk;
import ca.uottawa.balie.LexiconOnDiskI;
import ca.uottawa.balie.NamedEntityRecognitionNerf;
import ca.uottawa.balie.NamedEntityTypeEnumMappingNerf;
import ca.uottawa.balie.PriorCorrectionNerf;
import ca.uottawa.balie.Token;
import ca.uottawa.balie.TokenList;
import ca.uottawa.balie.Tokenizer;

public class NERecognition {
	/**
	 * Plusieurs de plusieurs librairies d'extraction d'entitées nommées
	 * 
	 * @param strText
	 */

	public static void balieNE(String strText) {

		{
			ArrayList<String> listNE = new ArrayList<String>();

			Tokenizer tokenizer = new Tokenizer(Balie.LANGUAGE_ENGLISH, true);

			LexiconOnDiskI lexicon = new LexiconOnDisk(
					LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON);
			DisambiguationRulesNerf disambiguationRules = DisambiguationRulesNerf
					.Load();
			tokenizer.Reset();
			// TRES lent
			tokenizer.Tokenize(strText);
			TokenList alTokenList = tokenizer.GetTokenList();

			NamedEntityRecognitionNerf ner = new NamedEntityRecognitionNerf(
					alTokenList, lexicon, disambiguationRules,
					new PriorCorrectionNerf(), NamedEntityTypeEnumMappingNerf
							.values(), true);
			ner.RecognizeEntities();

			alTokenList = ner.GetTokenList();

			int nbPerson = 0;
			String precedPerson;
			int precedBy = 0;

			// words feature creation
			for (int i = 0; i < alTokenList.Size(); i++) {
				Token curTok = alTokenList.Get(i);
				Token nextTok = alTokenList.Get(i + 1);

				String namedEntity = alTokenList.Get(i).EntityType().GetLabel(
						NamedEntityTypeEnumMappingNerf.values());
				if (namedEntity != null)
					System.out.println(alTokenList.Get(i).Raw() + " - NE= "
							+ namedEntity + " Start = "
							+ alTokenList.Get(i).EntityType().IsStart()
							+ " IsEnd + "
							+ alTokenList.Get(i).EntityType().IsEnd());

				// if (alTokenList.Get(i).EntityType().IsStart()) {
				// System.out.print("Sahar Adjacent entities: " + curTok.Raw()
				// + " | " + nextTok.Raw());
				// }
			}

			// System.out.println("precedBy " + precedBy);
		}
	}

	static void lingpipeNE(String txt) {

		String model = "models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
		File modelFile = new File(model);

		System.out.println("Reading chunker from file=" + modelFile);
		Chunker chunker;
		try {
			chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
			Chunking chunking = chunker.chunk(txt);
			System.out.println("Chunking=" + chunking);
			Set chunkSet = chunking.chunkSet();
			Iterator it = chunkSet.iterator();
			while (it.hasNext()) {
				Chunk chunk = (Chunk) it.next();
				int start = chunk.start();
				int end = chunk.end();
				String phrase = txt.substring(start, end);
				String type = chunk.type();
				double conf = Math.pow(2.0, chunk.score());
				System.out.println("score =" + conf);

				System.out.println("     chunk=" + type + "  :  " + phrase);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	static void lingpipeNeNbest(String txt) {
		int MAX_N_BEST_CHUNKS = 7;
		String model = "models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
		File modelFile = new File(model);
		HashMap<String, Double> persons = new HashMap<String, Double>();

		try {
			ConfidenceChunker chunker = (ConfidenceChunker) AbstractExternalizable
					.readObject(modelFile);

			System.out.println("Reading chunker from file=" + modelFile);

			// for (int i = 1; i < tokens.length; ++i) {
			char[] cs = txt.toCharArray();
			Iterator it = chunker.nBestChunks(cs, 0, cs.length,
					MAX_N_BEST_CHUNKS);
			for (int n = 0; it.hasNext(); ++n) {
				Chunk chunk = (Chunk) it.next();

				double conf = Math.pow(2.0, chunk.score());
				int start = chunk.start();
				int end = chunk.end();
				String phrase = txt.substring(start, end);
				String type = chunk.type();

				double score;
				double diff;
				if (type.equals("PERSON")) {
					persons.put(phrase, conf);
				} else if (persons.containsKey(phrase)) {
					score = persons.get(phrase);
					if (score > conf) {
						diff = score - conf;
						System.out.println("diff = " + diff);
						if (diff < 0.4)
							persons.remove(phrase);
					}

				}

				System.out.println(n + " "
						+ Strings.decimalFormat(conf, "0.0000", 12) + " ("
						+ start + ", " + end + ") " + chunk.type() + " "
						+ phrase + " - " + chunk.score());
			}

			System.out.println(" ********* personnes ****************");
			Set cles = persons.keySet();
			Iterator it2 = cles.iterator();
			while (it2.hasNext()) {
				Object cle = it2.next(); // tu peux typer plus finement ici
				Object valeur = persons.get(cle); // tu peux typer plus
				// finement
				// ici
				System.out.println(cle);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	static String[] tokenizeLingpipe(String text) {
		String[] tokens = null;
		/** detection de la langue avec lingpipe * */
		TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory(
				"(-|'|\\d|\\p{L})+|\\S");
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(
					"models/pos-en-general-brown.HiddenMarkovModel");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
			Streams.closeInputStream(objIn);
			HmmDecoder decoder = new HmmDecoder(hmm);
			String line = text;
			char[] cs = line.toCharArray();
			com.aliasi.tokenizer.Tokenizer tokenizer = TOKENIZER_FACTORY
					.tokenizer(cs, 0, cs.length);
			tokens = tokenizer.tokenize();

			String[] tags = decoder.firstBest(tokens);
			for (int i = 0; i < tokens.length; ++i)
				System.out.print(tokens[i] + "_" + tags[i] + " | ");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tokens;
	}

	public static void main(String[] args) {

		String strText = "Copyright © 2001   by Lawrence Sklar <mail mail >.  ";

		// tokenizeLingpipe(strText);
		double before = System.currentTimeMillis();

		// System.out.println("------------ LINGPIPE --------------------");
		// lingpipeNE(strText);
		// tokenizeLingpipe(strText);
		//
		// double time = (System.currentTimeMillis() - before) / 1000;
		// System.out.println();
		// System.out.println("Temps d'exécution = " + time + "s");

		System.out.println("------------ BALIE ------------------------");
		System.out.println();
		before = System.currentTimeMillis();
		balieNE(strText);
		double time2 = (System.currentTimeMillis() - before) / 1000;
		System.out.println();
		System.out.println("Temps d'exécution = " + time2 + "s");
	}
}
