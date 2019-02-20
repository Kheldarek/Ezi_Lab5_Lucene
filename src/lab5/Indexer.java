package lab5;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Indexer
{
    public static void main(String args[])
    {
        Indexer indexer = new Indexer();
        indexer.indexDocuments();
    }

    private void indexDocuments()
    {
        // REMOVE PREVIOUSLY GENERATED INDEX (DONE)
        try
        {
            FileUtils.deleteDirectory(new File(Constants.index_dir));
        } catch (IOException ignored)
        {
        }

        ArrayList<Document> documents = getHTMLDocuments();

        try {
            constructIndex(documents);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void constructIndex(ArrayList<Document> documents) throws IOException{
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter((FSDirectory.open(Paths.get(Constants.index_dir))), indexWriterConfig);
        indexWriter.addDocuments(documents);
        indexWriter.commit();
        indexWriter.close();
    }

    private ArrayList<Document> getHTMLDocuments()    {
        // This method is finished. Find getHTMLDocument
        File dir = new File("./resources/pages");
        File[] files = dir.listFiles();
        if (files != null)
        {
            ArrayList<Document> htmls = new ArrayList<>(files.length);
            for (int id = 0; id < files.length; id++)
            {
                System.out.println("Loading "+ files[id].getName());
                htmls.add(getHTMLDocument("./resources/pages/" + files[id].getName(), id));
            }
            return htmls;
        }
        return null;
    }

    private Document getHTMLDocument(String path, int id)
    {
        File file = new File(path);
        Document document = new Document();


        String content = getTextFromHTMLFile(file);
        if(content != null){
            TextField text = new TextField(Constants.content, content,Field.Store.NO);
            document.add(text);
        }

        document.add(new StoredField(Constants.id,id));
        document.add(new TextField(Constants.filename,file.getName(),Field.Store.YES));
        document.add(new IntPoint(Constants.filesize_int, (int) file.length()));
        document.add(new StoredField(Constants.filesize, file.length()));

        return document;

    }

    // (DONE)
    private String getTextFromHTMLFile(File file)
    {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputStream;
        try
        {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }

        ParseContext pContext = new ParseContext();

        //Html parser
        HtmlParser htmlparser = new HtmlParser();
        try
        {
            htmlparser.parse(inputStream, handler, metadata, pContext);
        } catch (IOException | SAXException | TikaException e)
        {
            e.printStackTrace();
        }

        return handler.toString();
    }

}