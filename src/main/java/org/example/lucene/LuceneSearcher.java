package org.example.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created on 19.04.2020.
 *
 * @author Sergey Radchenko
 */
public class LuceneSearcher {

    public List<Document> search(String searchText, String fileMask) {

        List<Document> relevantDocuments = new ArrayList<>();

        Analyzer analyzer = new StandardAnalyzer();
        QueryBuilder queryBuilder = new QueryBuilder(analyzer);
        BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();

        if(!searchText.isEmpty()) {
            Query q = queryBuilder.createPhraseQuery(LuceneConstants.CONTENTS, searchText);
            booleanBuilder.add(q, BooleanClause.Occur.MUST);
        }

        if(!fileMask.isEmpty()) {
            QueryParser queryParser = new QueryParser(LuceneConstants.FILE_NAME, analyzer);
            queryParser.setAllowLeadingWildcard(true);
            try {
                Query q = queryParser.parse(fileMask);
                booleanBuilder.add(q, BooleanClause.Occur.MUST);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        BooleanQuery totalQuery = booleanBuilder.build();

        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(LuceneConstants.INDEX_DIRECTORY));
            IndexReader indexReader = DirectoryReader.open(indexDirectory);

            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            TopDocs topDocs = indexSearcher.search(totalQuery, Integer.MAX_VALUE);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                int documentId = scoreDoc.doc;
                relevantDocuments.add(indexReader.document(documentId));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return relevantDocuments;
    }

    public int getNumDocs() {
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(LuceneConstants.INDEX_DIRECTORY));
            IndexReader indexReader = DirectoryReader.open(indexDirectory);
            return indexReader.numDocs();
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
