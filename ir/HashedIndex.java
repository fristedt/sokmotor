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
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Comparator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	if ("".equals(token)) {
	    System.err.println("Empty token provided.");
	    return;
	}

	PostingsList pl = index.get(token);
	if (pl == null) {
	    pl = new PostingsList();
	    index.put(token, pl);
	}

	if (pl.contains(docID)) {
	    return;
	}

	pl.add(docID);
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	return index.get(token);
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	if (query.terms.size() < 1)
	    return null;

	if (query.terms.size() == 1)
	    return index.get(query.terms.get(0));

	return intersect(query.terms);
    }

    private PostingsList intersect(LinkedList<String> terms) {
	sortByIncreasingFrequency(terms);

	PostingsList result = getPostings(terms.getFirst());
	System.out.println("ZOMBIE");
	result.getIDs().forEach(i -> printFileAndID(i));
	if (result == null)
	    return null;

	terms.remove();

	while (terms.size() > 0 && result != null) {
	    System.out.println("ZOMBIE");
	    getPostings(terms.getFirst()).getIDs().forEach(i -> printFileAndID(i));
	    result = intersect(result, getPostings(terms.getFirst()));
	    terms.remove();
	}
	return result;
    }

    private PostingsList intersect(PostingsList l1, PostingsList l2) {
	if (l1 == null || l2 == null) 
	    return null;
	PostingsList answer = new PostingsList();

	for (int i = 0, j = 0; i < l1.size() && j < l2.size();) {
	    // TODO: Onödigt att getta varje gång, fixa iterator???
	    PostingsEntry p1 = l1.get(i);
	    PostingsEntry p2 = l2.get(j);

	    if (p1.docID == p2.docID) {
		answer.add(p1.docID);
		i += 1;
		j += 1;
	    } else if (p1.docID < p2.docID) {
		i += 1;
	    } else {
		j += 1;
	    }
	}

	if (answer.size() == 0)
	    return null;

	return answer;
    }

    private void sortByIncreasingFrequency(LinkedList<String> terms) {
	terms.sort(new Comparator<String>() {
	    public int compare(String t1, String t2) {
		PostingsList t1Postings = getPostings(t1);
		PostingsList t2Postings = getPostings(t2);

		int t1Freq = 0;
		int t2Freq = 0;

		if (t1Postings != null)
		    t1Freq = t1Postings.size();
		if (t2Postings != null)
		    t2Freq = t2Postings.size();

		return Integer.compare(t1Freq, t2Freq);
	    }
	});
    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }

    private void printFileAndID(int docID) {
	System.out.printf("ID: %d | %s\n",  docID, docIDs.get("" + docID));
    }
}
