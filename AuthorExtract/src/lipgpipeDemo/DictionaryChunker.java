package lipgpipeDemo;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.RegExChunker;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.ExactDictionaryChunker;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.util.Iterator;
import java.util.Set;

public class DictionaryChunker {

    static final double CHUNK_SCORE = 1.0;

    public static void main(String[] args) {

        MapDictionary dictionary = new MapDictionary();
        dictionary.addEntry(new DictionaryEntry("50 Cent","PERSON",CHUNK_SCORE));
        dictionary.addEntry(new DictionaryEntry("XYZ120 DVD Player","DB_ID_1232",CHUNK_SCORE));
        dictionary.addEntry(new DictionaryEntry("cent","MONETARY_UNIT",CHUNK_SCORE));
        dictionary.addEntry(new DictionaryEntry("dvd player","PRODUCT",CHUNK_SCORE));


        ExactDictionaryChunker dictionaryChunkerTT
            = new ExactDictionaryChunker(dictionary,
                                         IndoEuropeanTokenizerFactory.FACTORY,
                                         true,true);

        ExactDictionaryChunker dictionaryChunkerTF
            = new ExactDictionaryChunker(dictionary,
                                         IndoEuropeanTokenizerFactory.FACTORY,
                                         true,false);

        ExactDictionaryChunker dictionaryChunkerFT
            = new ExactDictionaryChunker(dictionary,
                                         IndoEuropeanTokenizerFactory.FACTORY,
                                         false,true);

        ExactDictionaryChunker dictionaryChunkerFF
            = new ExactDictionaryChunker(dictionary,
                                         IndoEuropeanTokenizerFactory.FACTORY,
                                         false,false);



        System.out.println("\nDICTIONARY\n" + dictionary);


            String text = "50 Cent is hard to distinguish from 50 cent and just plain cent without case. My name is Jane.";
            System.out.println("\n\nTEXT=" + text);

            chunk(dictionaryChunkerTT,text);
            chunk(dictionaryChunkerTF,text);
            chunk(dictionaryChunkerFT,text);
            chunk(dictionaryChunkerFF,text);
        

    }

    static void chunk(ExactDictionaryChunker chunker, String text) {
        System.out.println("\nChunker."
                           + " All matches=" + chunker.returnAllMatches()
                           + " Case sensitive=" + chunker.caseSensitive());
        Chunking chunking = chunker.chunk(text);
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String type = chunk.type();
            double score = chunk.score();
            String phrase = text.substring(start,end);
            System.out.println("     phrase=|" + phrase + "|"
                               + " start=" + start
                               + " end=" + end
                               + " type=" + type
                               + " score=" + score);
        }
    }

}
