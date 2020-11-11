/**
 * Copyright © 2008-2019, Province of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.bc.gov.ols.geocoder.data.indexing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.util.StopWatch;

public class LuceneWordMap implements WordMap {
	private static final Logger logger = LoggerFactory.getLogger(//GeocoderConfig.LOGGER_PREFIX +
			LuceneWordMap.class.getCanonicalName());
	
	private List<Word> words;
	private Analyzer analyzer;
	private Directory luceneIndex;
	private final Path indexPath = Path.of("C:/apps/bgeo/index/wordmap/");
	DirectoryReader ireader;
    IndexSearcher isearcher;
    QueryParser parser;	
	
	public LuceneWordMap(Map<String, Word> wordList, Map<String, Set<Word>> wordMap) {
		// build an arrayList of words, and a temporary hashmap reverse index
		words = new ArrayList<Word>();
		Map<String, Integer> wordId = new HashMap<String, Integer>();
		for(Word word : wordList.values()) {
			words.add(word);
			wordId.put(word.getWord(), words.size()-1);
		}
		try {
			logger.info("Lucene Word Map Index build starting...");
			StopWatch sw = new StopWatch();
			sw.start();
			analyzer = new StandardAnalyzer();
			IOUtils.rm(indexPath);
			luceneIndex = new MMapDirectory(indexPath);
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
	
			try(IndexWriter w = new IndexWriter(luceneIndex, config)) {
				for(Entry<String, Set<Word>> entry : wordMap.entrySet()) {
					for(Word word : entry.getValue()) {
						Document doc = new Document();
					    doc.add(new Field("word", word.getWord(), TextField.TYPE_STORED));
					    doc.add(new StoredField("id", wordId.get(word.getWord())));
					    w.addDocument(doc);
					}
				}
				w.close();
			}
			
			sw.stop();
			logger.info("Lucene Index built in: " + sw.getElapsedTimeSecs() + "s");
			ireader = DirectoryReader.open(luceneIndex);
		    isearcher = new IndexSearcher(ireader);
		    parser = new QueryParser("word", analyzer);
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		} 
	}
	
	@Override
	public List<MisspellingOf<Word>> mapWord(String fromWord, boolean allowMisspellings) {
		try {
		    Query query = parser.parse(fromWord);
		    TopDocs topDocs = isearcher.search(query, 10);
		    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		    float topScore = 0;
		    if(scoreDocs.length > 0) {
		    	topScore = scoreDocs[0].score;
		    }
		    List<MisspellingOf<Word>> results = new ArrayList<MisspellingOf<Word>>(scoreDocs.length);
		    // Iterate through the results:
		    for (int i = 0; i < scoreDocs.length; i++) {
		    	Document hitDoc = isearcher.doc(scoreDocs[i].doc);
		    	int error = Math.round(100 * (topScore - scoreDocs[i].score) / topScore);
		    	Word word = words.get((int) hitDoc.getField("id").numericValue());
		    	results.add(new MisspellingOf<Word>(word, error, fromWord));
		    }
		    return results;
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
	}

	@Override
	public List<MisspellingOf<Word>> mapWord(String fromWord, boolean allowMisspellings, boolean autoComplete) {
		return mapWord(fromWord, allowMisspellings);
	}

}
