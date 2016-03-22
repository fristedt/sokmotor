/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Comparator;
import java.util.Collections;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class BiwordIndex implements Index {
    private final int N = 17486; // Number of documents in collection.
    private HashMap<String, HashMap<String, PostingsList>> index = new HashMap<String, HashMap<String, PostingsList>>();
    private String previousWord = null;
    private int previousDocID = -1;

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	if (docID != previousDocID) {
	    previousDocID = docID;
	    previousWord = token;
	    return;
	}

	if (!index.containsKey(previousWord)) {
	    index.put(previousWord, new HashMap<String, PostingsList>());
	}

	if (!index.get(previousWord).containsKey(token)) {
	    index.get(previousWord).put(token, new PostingsList());
	}

	PostingsList pl = index.get(previousWord).get(token);
	// Use second word in biword as offset position. Might be dumb.
	pl.add(docID, offset);
	previousWord = token;
    }

    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	System.err.println("getDictionary: Not implemented");
	return null;
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	System.err.println("getPostings: Not implemented");
	return null;
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	if (queryType != Index.RANKED_QUERY) {
	    System.err.println("Only ranked retrevial is supported for biword index.");
	    return null;
	}
	
	if (structureType != Index.BIGRAM) {
	    System.err.println("Only bigram structure is supported for biword index.");
	    return null;
	}
	return rankedRetrieval(query);
    }

    private PostingsList rankedRetrieval(Query query) {
	PostingsList ret = new PostingsList();

	// Number of biwords in query.
	int n = query.terms.size() - 1;

	// Iterate over biwords in query.
	for (int i = 1; i < query.terms.size(); ++i) {
	    HashMap<String, PostingsList> wordMap = index.get(query.terms.get(i-1));
	    if (wordMap == null) {
		continue;
	    }

	    PostingsList pl = wordMap.get(query.terms.get(i));
	    if (pl == null) {
		continue;
	    }

	    double queryTf = 1; // Assume all query biwords are unique.
	    double idf = pl.idf();
	    double queryTfIdf = queryTf * idf / n;

	    // Iterate over documents containing biword.
	    for (int j = 0; j < pl.size(); ++j) {
		PostingsEntry pe = pl.get(j);
		double docTfIdf = pe.tf() * idf / docLengths.get("" + pe.docID);
		pe.score = queryTfIdf * docTfIdf;
		ret.add(pe);
	    }
	}
	ret.sort();
	return ret;
    }



    public void cleanup() {
    }
}
