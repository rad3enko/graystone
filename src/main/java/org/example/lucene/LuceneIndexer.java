package org.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created on 19.04.2020.
 *
 * @author Sergey Radchenko
 */
public class LuceneIndexer {

    public LuceneIndexer() {
    }

    public void createIndex(String dataDir) {
        synchronized (LuceneIndexer.class) {
            System.out.println("Indexing started.");
            try {
                Directory directory = FSDirectory.open(Paths.get(LuceneConstants.INDEX_DIRECTORY));
                Analyzer analyzer = new StandardAnalyzer();
                IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

                indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

                IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
                indexDocs(indexWriter, Paths.get(dataDir));
                indexWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Indexing ended.");
        }
    }

    private void indexDocs(final IndexWriter indexWriter, Path dataDir) throws IOException {
        if (Files.isDirectory(dataDir)) {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(indexWriter, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ex) {
                        // dont index files that cant be read
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(indexWriter, dataDir, Files.getLastModifiedTime(dataDir).toMillis());
        }
    }

    private void indexDoc(IndexWriter indexWriter, Path file, long lastModified) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            Document document = new Document();

            StringField fileNameField = new StringField(LuceneConstants.FILE_NAME,
                                                        file.getFileName().toString(),
                                                        Field.Store.YES);
            StringField filePathField = new StringField(LuceneConstants.FILE_PATH,
                                                        file.toString(),
                                                        Field.Store.YES);
            TextField contentField = new TextField(LuceneConstants.CONTENTS,
                                                   new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));

            document.add(fileNameField);
            document.add(filePathField);
            document.add(contentField);

            indexWriter.addDocument(document);
        }
    }
}