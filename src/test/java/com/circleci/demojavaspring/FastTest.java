package com.circleci.demojavaspring;

import static org.junit.Assert.*;

import com.circleci.demojavaspring.model.Quote;
import com.circleci.demojavaspring.model.Snippet;
import com.circleci.demojavaspring.repository.TextFileIndexer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FastTest {

    @Test
    public void fastTest() throws InterruptedException {
        assertTrue(true);
    }

    @Test
    public void newQuotesTest() throws InterruptedException {
        Quote quote = new Quote();
        quote.setQuote("Hey");
        assertEquals(quote.getQuote(), "Hey");
    }

    @Test
    public void addExampleFile() throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Path path = Paths.get("./documents");
        Directory index = new MMapDirectory(path);

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        TextFileIndexer textFileIndexer = new TextFileIndexer();
        textFileIndexer.addExampleDoc(w, "Lucene in Action", "193398817");
        textFileIndexer.addExampleDoc(w, "Lucene for Dummies", "55320055Z");
        textFileIndexer.addExampleDoc(w, "Managing Gigabytes", "55063554A");
        textFileIndexer.addExampleDoc(w, "The Art of Computer Science", "9900333X");
        int numDocs = w.getDocStats().numDocs;
        // delete the test added document
        w.deleteAll();
        w.close();
        assertEquals(4, numDocs);
    }

    @Test
    public void searchExampleFile() throws IOException {
        // construct document
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Path path = Paths.get("./documents");
        Directory index = new MMapDirectory(path);

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        TextFileIndexer textFileIndexer = new TextFileIndexer();
        textFileIndexer.addExampleDoc(w, "Lucene in Action", "193398817");
        textFileIndexer.addExampleDoc(w, "Lucene for Dummies", "55320055Z");
        textFileIndexer.addExampleDoc(w, "Managing Gigabytes", "55063554A");
        textFileIndexer.addExampleDoc(w, "The Art of Computer Science", "9900333X");
        textFileIndexer.addExampleDoc(w, "The Art of Lacquer", "2900333X");
        int numDocs = w.getDocStats().numDocs;
        //delete all docs and close IndexWriter
        w.close();

        // search document by query
        final var queryStr = "Art";
        final int maxHits = 100;
//        Path path = Paths.get("./documents");
//        Directory index = new MMapDirectory(path);

        IndexReader indexReader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(indexReader);
//        Analyzer analyzer = new StandardAnalyzer();
        QueryBuilder builder = new QueryBuilder(analyzer);
        Query query = builder.createBooleanQuery("title", queryStr);
        TopDocs topDocs = searcher.search(query, maxHits);
        ScoreDoc[] hits = topDocs.scoreDocs;

        for (int i = 0; i < hits.length; i++) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(d.get("title") + " Score :" + hits[i].score);
        }
        System.out.println("Found " + hits.length);
        assertEquals(2, hits.length);


        // Delete all generated docs after the test is finished
        Path pathForDeleteAll = Paths.get("./documents");
        Directory indexForDeleteAll = new MMapDirectory(pathForDeleteAll);
        IndexWriterConfig configForDeleteAll = new IndexWriterConfig(analyzer);
        IndexWriter wForDeleteAll = new IndexWriter(indexForDeleteAll, configForDeleteAll);
        wForDeleteAll.deleteAll();
        wForDeleteAll.close();
    }


    @Test
    public void addSnippetsFile() throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Path path = Paths.get("./snippets");
        Directory index = new MMapDirectory(path);

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        TextFileIndexer textFileIndexer = new TextFileIndexer();
        try (
                FileReader fileReader = new FileReader(("./snippetsJson/python/final/jsonl/train/python_train_0.jsonl"));
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            String currentLine;
            int i = 0;
            while ((currentLine = bufferedReader.readLine()) != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Snippet snippet = mapper.readValue(currentLine, Snippet.class);
                textFileIndexer.addSnippetDoc(w, snippet);
                i = i + 1;
            }
            System.out.println('i' + i);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int numDocs = w.getDocStats().numDocs;
        assertTrue(numDocs > 10000);
        // delete the test added document
        w.deleteAll();
        w.close();
    }

//    @Test
//    public void searchSnippetsFile() throws IOException {
//        StandardAnalyzer analyzer = new StandardAnalyzer();
//        Path path = Paths.get("./snippets");
//        Directory index = new MMapDirectory(path);
//
//        IndexWriterConfig config = new IndexWriterConfig(analyzer);
//
//        IndexWriter w = new IndexWriter(index, config);
//        TextFileIndexer textFileIndexer = new TextFileIndexer();
//        try (
//                FileReader fileReader = new FileReader(("snippetsJson/python/python/final/jsonl/train/python_train_0.jsonl"));
//                BufferedReader bufferedReader = new BufferedReader(fileReader);
//        ) {
//            String currentLine;
//            int i = 0;
//            while ((currentLine = bufferedReader.readLine()) != null) {
//                ObjectMapper mapper = new ObjectMapper();
//                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//                Snippet snippet = mapper.readValue(currentLine, Snippet.class);
//                textFileIndexer.addSnippetDoc(w, snippet);
//                i = i + 1;
//            }
//            System.out.println('i' + i);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        int numDocs = w.getDocStats().numDocs;
//        assertTrue(numDocs > 10000);
//        // delete the test added document
//        w.deleteAll();
//        w.close();
//    }
}


