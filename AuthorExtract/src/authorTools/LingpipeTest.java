package authorTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Set;

import org.xml.sax.Attributes;

import lipgpipeDemo.EmailRegExChunker;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.chunk.NBestChunker;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.aliasi.xml.SimpleElementHandler;

public class LingpipeTest {
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
				System.out.println("     chunk=" + type + "  :  " + phrase
						+ " score = " + conf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	static void nBestChunker(String sentenceText) {
		int MAX_N_BEST = 3;
		String model = "models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
		File modelFile = new File(model);
		try {
			Chunker chunker = (Chunker) AbstractExternalizable
					.readObject(modelFile);

			NBestChunker nBestChunker = (NBestChunker) chunker;
			char[] cs = sentenceText.toCharArray();
			Iterator chunkingIt = nBestChunker.nBest(cs, 0, cs.length,
					MAX_N_BEST);
			for (int i = 0; i < MAX_N_BEST && chunkingIt.hasNext(); ++i) {
				ScoredObject so = (ScoredObject) chunkingIt.next();
				double log2P = so.score();
				Chunking chunking = (Chunking) so.getObject();

				System.out.println("rank: " + Integer.toString(i)
						+ " | jointLog2P : " + Double.toString(log2P) + " "
						+ chunking.chunkSet());

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void confidenceChunker(String text) {

		int MAX_CONF = 16;
		String model = "models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
		File modelFile = new File(model);
		try {
			Chunker chunker = (Chunker) AbstractExternalizable
					.readObject(modelFile);

			char[] cs = text.toCharArray();
			ConfidenceChunker confChunker = (ConfidenceChunker) chunker;
			Iterator it = confChunker.nBestChunks(cs, 0, cs.length, MAX_CONF);

			for (int i = 0; i < MAX_CONF && it.hasNext(); ++i) {
				Chunk chunk = (Chunk) it.next();
				int start = chunk.start();
				int end = chunk.end();
				String type = chunk.type();
				String mentionText = text.substring(start, end);
				double score = chunk.score();
				double condProb = java.lang.Math.pow(2.0, score);

				System.out.println(" TEXT : " + mentionText + " | " + type
						+ " | condProb " + Double.toString(condProb) + " RANK "
						+ Integer.toString(i));

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param text
	 */
	static void tokenizeLingpipe(String text) {
		/** detection de la langue avec lingpipe **/
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
			String[] tokens = tokenizer.tokenize();
			System.out.println(tokens.length);
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

	}

	static void emailDetector(String text) {

		boolean isMail = false;
		Chunker chunker = new EmailRegExChunker();
		Chunking chunking = chunker.chunk(text);
		Set chunkSet = chunking.chunkSet();
		Iterator it = chunkSet.iterator();
		
		if (it.hasNext()) {
			isMail = true;
			Chunk chunk = (Chunk) it.next();
			int start = chunk.start();
			int end = chunk.end();
			String txt = text.substring(start, end);
			System.out.println("     chunk=" + chunk + "  text=" + txt);
		}
		System.out.println("isMail " + isMail);
	}

	public static void main(String[] args) {

		String strText = "Copyright © 2000 by Howard Tolley, Jr., with grant support from the U.S. Institute of Peace. All Rights Reserved. THRO Case No. 200-3, ISSN 1529-2215.";
		System.out.println(strText);
		// // tokenizeLingpipe(strText);
		// double before = System.currentTimeMillis();
		// System.out.println("------------ LINGPIPE --------------------");
		// System.out.println();
		// lingpipeNE(strText);
		//
		// System.out
		// .println("------------ CONFIDENCE FACTOR --------------------");
		// // tokenizeLingpipe(strText);
		// confidenceChunker(strText);

		// System.out.println("------------ N BEST CHUNK--------------------");
		// nBestChunker(strText);
		// double time = (System.currentTimeMillis() - before) / 1000;
		// System.out.println();
		// System.out.println("Temps d'exécution = " + time + "s");

		// Find email:
		String txt = "John's email is john@his.company.com and his friend's is foo.bar@123.foo.ca";
		emailDetector(txt);
	}
}
