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
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
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
	    // Just get the last added entry and append position.
	    PostingsEntry pe = pl.getLast();
	    pe.positions.add(offset);
	    return;
	}

	PostingsEntry pe = new PostingsEntry(docID);
	pe.positions.add(offset);
	pl.add(pe);
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

	return intersect(query.terms, queryType);
    }

    private PostingsList intersect(LinkedList<String> terms, int queryType) {
	// Don't sort by frequency if we are doing a phrase query because then
	// the order of the terms matter.
	if (queryType != Index.PHRASE_QUERY)
	    sortByIncreasingFrequency(terms);

	PostingsList result = getPostings(terms.getFirst());
	if (result == null)
	    return null;

	terms.remove();

	while (terms.size() > 0 && result != null) {
	    if (queryType == Index.PHRASE_QUERY)
		result = positionalIntersect(result, getPostings(terms.getFirst()));
	    else
		result = intersect(result, getPostings(terms.getFirst()));
	    terms.remove();
	}
	return result;
    }

    private PostingsList intersect(PostingsList l1, PostingsList l2) {
	if (l1 == null || l2 == null) 
	    return null;
	PostingsList answer = new PostingsList();

	for (int i = 0, j = 0; i < l1.size() && j < l2.size(); ++i, ++j) {
	    PostingsEntry p1 = l1.get(i);
	    PostingsEntry p2 = l2.get(j);

	    if (p1.docID == p2.docID) {
		answer.add(p1.docID);
	    } else if (p1.docID < p2.docID) {
		--j;
	    } else {
		--i;
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

    private PostingsList positionalIntersect(PostingsList l1, PostingsList l2) {
	if (l1 == null || l2 == null) 
	    return null;
	PostingsList answer = new PostingsList();

	for (int i = 0, j = 0; i < l1.size() && j < l2.size(); ++i, ++j) {
	    PostingsEntry p1, p2;
	    p1 = l1.get(i);
	    p2 = l2.get(j);
	    if (p1.docID == p2.docID) {
		List<Integer> ppl1 = p1.positions;
		List<Integer> ppl2 = p2.positions;
		int currentPosition1, currentPosition2;
		currentPosition1 = currentPosition2 = 0;
		for (int x = 0; x < ppl1.size(); ++x) {
		    for (int y = 0; y < ppl2.size(); ++y) {
			currentPosition1 = ppl1.get(x);
			currentPosition2 = ppl2.get(y);
			if (currentPosition2 - currentPosition1 == 1) {
			    if (!answer.contains(p1.docID))
				answer.add(p1.docID);
			} else if (currentPosition2 > currentPosition1) {
			    break;
			}
		    }
		}
	    } else if (p1.docID < p2.docID) {
		--j;
	    } else {
		--i;
	    }
	}
	return answer;
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
