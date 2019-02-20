package lab5;

import opennlp.tools.parser.Cons;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Searcher {

    public static void main(String args[]) throws ParseException, IOException {
        // Load the previously generated index (DONE)
        IndexReader reader = getIndexReader();
        assert reader != null;

        // Construct index searcher (DONE)
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        // Standard analyzer - might be helpful
        Analyzer analyzer = new StandardAnalyzer();


        // TERM QUERY
        // A Query that matches documents containing a term.
        // This may be combined with other terms with a BooleanQuery.
        // as you may notice, this word is not normalized (but is should be normalized
        // in the same way as all documents were normalized when constructing the index.
        // For that reason you can use analyzer object (utf8TOString()!).
        // Then, build a Term object (seek in content - Constants.content) and TermQuery.
        // Lastly, invoke printResultsForQuery.
        String queryMammal = "MaMMal";
        TermQuery tq1;
        {
            // --------------------------------------
            // COMPLETE THE CODE HERE
            System.out.println("1) term query: mammal (CONTENT)");
            String mammal = analyzer.normalize("mammal", queryMammal).utf8ToString();
            Term mammalTerm = new Term(Constants.content, mammal);
            tq1 = new TermQuery(mammalTerm);
            printResultsForQuery(indexSearcher, tq1);

            // --------------------------------------
        }

        // Compare the results for "mammal" and "bird".
        String queryBird = "bird";
        TermQuery tq2;
        {
            // --------------------------------------
            System.out.println("2) term query bird (CONTENT)");
            String bird = analyzer.normalize("mammal", queryBird).utf8ToString();
            Term termBird = new Term(Constants.content, bird);
            tq2 = new TermQuery(termBird);
            printResultsForQuery(indexSearcher, tq2);

            // --------------------------------------
        }

        // Construct two clauses: BooleanClause (use BooleanClause.Occur to set a proper flag).
        // The first concerns tq1 ("mammal") and the second concerns ("bird").
        // To construct BooleanQuery, Use static methods of BooleanQuery.Builder().
        // Additionally, use setMinimumNumberShouldMatch() with a proper parameter
        // to generate "mammal" or "bird" rule.

        // Boolean query
        {
            // --------------------------------------
            System.out.println("3) boolean query (CONTENT): mammal or bird");
            BooleanClause clauseForMammal = new BooleanClause(tq1, BooleanClause.Occur.SHOULD);
            BooleanClause clauseForBird = new BooleanClause(tq2, BooleanClause.Occur.SHOULD);
            BooleanQuery fullQuery = new BooleanQuery.Builder()
                    .add(clauseForMammal)
                    .add(clauseForBird)
                    .setMinimumNumberShouldMatch(1)
                    .build();
            printResultsForQuery(indexSearcher, fullQuery);

            // --------------------------------------
        }

        // For this reason, construct Range query.
        // Use IntPoint.newRangeQuery.
        {
            // --------------------------------------
            System.out.println("4) range query: file size in [0b, 1000b]");
            Query queryFileSize = IntPoint.newRangeQuery(Constants.filesize, 0, 1000);
            printResultsForQuery(indexSearcher, queryFileSize);

            // --------------------------------------
        }

        // For this reason, construct PrefixQuery.
        {
            // --------------------------------------
            System.out.println("5) Prefix query (FILENAME): ant");
            Term antTerm = new Term(Constants.filename, "ant");
            PrefixQuery antQuery = new PrefixQuery(antTerm);
            printResultsForQuery(indexSearcher, antQuery);

            // --------------------------------------
        }

        // Construct a WildcardQuery object. Look for documents
        // which contain a term "eat?" "?" stand for any letter (* for a sequence of letters).
        {
            // --------------------------------------
            System.out.println("6) Wildcard query (CONTENT): eat?");
            Term eatTerm = new Term(Constants.content, "eat?");
            WildcardQuery eatQuery = new WildcardQuery(eatTerm);
            printResultsForQuery(indexSearcher, eatQuery);

            // --------------------------------------
        }

        // Find all documents that contain words which are similar to "mamml".
        // Which documents have been found?
        {
            // --------------------------------------
            System.out.println("7) Fuzzy querry (CONTENT): mamml?");
            Term mammlTerm = new Term(Constants.content, "mamml?");
            FuzzyQuery mammlQuery = new FuzzyQuery(mammlTerm);
            printResultsForQuery(indexSearcher, mammlQuery);

            // --------------------------------------
        }

        // and generate query object.
        // - use AND, OR , NOT, (, ), + (must), and - (must not) to construct boolean queries
        // - use * and ? to contstruct wildcard queries
        // - use ~ to construct fuzzy (one word, similarity) or proximity (at least two words) queries
        // - use - to construct proximity queries
        // - use \ as an escape character for: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
        // Consider following 5 examples of queries:
        String queryP1 = "MaMMal AND bat";
        String queryP2 = "ante*";
        String queryP3 = "brd~ ";
        String queryP4 = "(\"nocturnal life\"~10) OR bat";
        String queryP5 = "(\"nocturnal life\"~10) OR (\"are nocturnal\"~10)";
        // Select some query:
        String selectedQuery = queryP1;
        // Complete the code here, i.e., build query parser object, parse selected query
        // to query object, and find relevant documents. Analyze the outcomes.
        {
            // --------------------------------------
            System.out.println("8) query parser = " + selectedQuery);
            QueryParser queryParser = new QueryParser(Constants.content, analyzer);
            Query parseP1 = queryParser.parse(selectedQuery);
            printResultsForQuery(indexSearcher, parseP1);

            // --------------------------------------
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResultsForQuery(IndexSearcher indexSearcher, Query q) throws IOException {
        // - use indexSearcher to search for documents that
        // are relevant according to the query q
        // - Get TopDocs object (number of derived documents = Constant.top_docs)
        // - Iterate over ScoreDocs (in in TopDocs) and print for each document (in separate lines):
        // a) score
        // b) filename
        // c) id
        // d) file size
        // You may use indexSearcher to get a Document object for some docId (ScoreDoc)
        // and use document.get(name of the field) to get the value of id, filename, etc.

        // --------------------------------
        TopDocs topDocs = indexSearcher.search(q, Constants.top_docs);
        List<ScoreDoc> found = Stream.of(topDocs.scoreDocs).collect(Collectors.toList());
        System.out.println("Found " + found.size() + " query hits in documents");
        found.forEach(result -> printResult(indexSearcher, result));
        // --------------------------------
    }

    private static void printResult(IndexSearcher indexSearcher, ScoreDoc result) {
        try {
            Document document = indexSearcher.doc(result.doc);
            System.out.println("Score: " + result.score + " in File: " + document.get(Constants.filename) +
                    " id: " + document.get(Constants.id) + " file size: " + document.get(Constants.filesize));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static IndexReader getIndexReader() {
        try {
            Directory dir = FSDirectory.open(Paths.get(Constants.index_dir));
            return DirectoryReader.open(dir);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
