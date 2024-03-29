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
public class HashedIndex implements Index {
    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    private final int N = 17486; // Number of documents in collection.
    private final pagerank.PageRank pagerank = new pagerank.PageRank();

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	if (documentVectors.get(docID) == null)
	    documentVectors.put(docID, new HashMap<String, Integer>());
	if (!documentVectors.get(docID).containsKey(token)) {
	    documentVectors.get(docID).put(token, 1);
	} else {
	    documentVectors.get(docID).put(token, documentVectors.get(docID).get(token) + 1);
	}

        if (!("".equals(token))) {
            PostingsList pl = index.get(token);

            if (pl == null) {
                pl = new PostingsList();
            }
            pl.add(docID, offset);
            index.put(token, pl);

        } else
            System.out.println("Empty token");
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
        if (queryType == Index.RANKED_QUERY)
            return rankedRetrieval(query, rankingType);
        return intersect(query.terms, queryType);
    }

    public PostingsList rankedRetrieval(Query query, int rankingType) {
	PostingsList ranked = null;
	switch (rankingType) {
	    case Index.TF_IDF:
		ranked = cosineScore(query);
		ranked.sort();
		return ranked;
	    case Index.PAGERANK:
		ranked = pageranks(query.terms);
		ranked.sort();
		break;
	    case Index.COMBINATION:
		ranked = combination(query.terms);
		ranked.sort();
		break;
	}
	return ranked;
    }

    public PostingsList cosineScore(Query query) {
	long startTime = System.nanoTime();

        PostingsList ret = new PostingsList();

	int n = query.terms.size();
	for (int i = 0; i < query.terms.size(); ++i) {
	    String t = query.terms.get(i);
            PostingsList termList = index.get(t);
	    // Just continue if word doesn't exist.
	    if (termList == null)
		continue;
            int tf = 1; // assume all query terms are unique.
	    double termWeight = query.weights.get(i);
	    // double tf = query.weights.get(i); // tf 
            double idf = termList.idf(); 
	    System.out.println("IDF: " + idf);
	    if (idf < 4)
		continue;
            double tf_idfQuery = termWeight * tf * idf / n; 
            for (int k = 0; k < termList.size(); k++) {
                PostingsEntry pe = termList.get(k);
                int d = pe.docID;
                // Num occurances in doc * inv num docs that occ / doc len
                double tf_idfDoc = pe.tf() * idf / docLengths.get("" + d);
                pe.score = tf_idfDoc * tf_idfQuery;
                ret.add(pe);
            }
        }
	long endTime = System.nanoTime();
	System.out.printf("Search took %dms\n", (endTime - startTime) / 1000000);
        return ret;
    }

    private PostingsList pageranks(LinkedList<String> terms) {
        PostingsList ret = new PostingsList();

        for (String t : terms) {
            PostingsList termList = index.get(t);
	    // Just continue if word doesn't exist.
	    if (termList == null)
		continue;
            for (int k = 0; k < termList.size(); k++) {
                PostingsEntry pe = termList.get(k);
                pe.score = pagerank(pe.docID);
                ret.add(pe);
            }
        }
        return ret;
    }

    private double pagerank(int docID) {
	String filename = docIDs.get(""+docID);
	return pagerank.getPageRank(filename);
    }

    public PostingsList combination(LinkedList<String> terms) {
        PostingsList ret = new PostingsList();

        for (String t : terms) {
            PostingsList termList = index.get(t);
	    if (termList == null)
		continue;
            int n = terms.size();
            int tf = 1;
            double idf = termList.idf(); 
            double tf_idfQuery = tf * idf / n; 
            for (int k = 0; k < termList.size(); k++) {
                PostingsEntry pe = termList.get(k);
                int d = pe.docID;
                double tf_idfDoc = pe.tf() * termList.idf() / docLengths.get("" + d);
		double cosine = tf_idfDoc * tf_idfQuery;
		double pagerank = pagerank(pe.docID);
		double alpha = 0.00005;
		pe.score = alpha * cosine + (1 - alpha) * pagerank;
                ret.add(pe);
            }
        }
        return ret;
    }

    public PostingsList intersect(PostingsList list1, PostingsList list2) {
        PostingsList result = new PostingsList();

        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            PostingsEntry p1 = list1.get(i);
            PostingsEntry p2 = list2.get(j);

            if (p1.docID == p2.docID) {
                result.add(p1.docID);
                i++;
                j++;
            } else if (p1.docID < p2.docID) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }

    public PostingsList intersect(LinkedList<String> terms, int queryType) {
        if (queryType == Index.INTERSECTION_QUERY)
            frequencySort(terms);	
        PostingsList result = getPostings(terms.getFirst());
        terms.remove();
        while (!terms.isEmpty() && result.size() > 0) {
            if (queryType == Index.INTERSECTION_QUERY)
                result = intersect(result, getPostings(terms.getFirst()));
            else if (queryType == Index.PHRASE_QUERY)
                result = positionalIntersect(result, getPostings(terms.getFirst()));
            terms.remove();
        }
        return result;	
    }

    public void frequencySort(LinkedList<String> terms) {
        Collections.sort(terms, new CompareStrings());
    }

    public class CompareStrings implements Comparator<String> {
        @Override
            public int compare(String s1, String s2) {
                PostingsList pl1 = getPostings(s1);
                PostingsList pl2 = getPostings(s2);

                int size1 = 0;
                int size2 = 0;

                if (pl1 != null) {
                    size1 = pl1.size();	
                }
                if (pl2 != null) {
                    size2 = pl2.size();	
                }

                return Integer.compare(size1, size2);
            }
    }
    // 1.3
    public PostingsList positionalIntersect(PostingsList list1, PostingsList list2) {
        PostingsList answer = new PostingsList();
        int i = 0;
        int j = 0;
        int entry1currentOffset = 0;
        int entry2currentOffset = 0;
        while (i < list1.size() && j < list2.size()) {
            PostingsEntry p1 = list1.get(i);
            PostingsEntry p2 = list2.get(j);

            // If the documents are the same
            if (p1.docID == p2.docID) {
                List<Integer> l = new LinkedList<Integer>();
                
                // Find offsets of the words in the document 
                ArrayList<Integer> entry1offsets = p1.positions;
                ArrayList<Integer> entry2offsets = p2.positions;

                // The second word has to come after the first word
                for (int k = 0; k < entry1offsets.size(); k++) {
                    entry1currentOffset = entry1offsets.get(k);
                    for (int m = 0; m < entry2offsets.size(); m++) {
                        entry2currentOffset = entry2offsets.get(m);

                        // If the second word is right after the first word, we found a match
                        if (entry2currentOffset - entry1currentOffset == 1) {
                            //answer.add(p1.docID);
                            l.add(entry2currentOffset);
                        } else if (entry2currentOffset > entry1currentOffset) {
                            // If we get here, we have either just added an answer
                            // or we havent, but either way the list2 pointer has
                            // gone one step too far, so list1 pointer needs to be incremented.
                            break;
                        }
                        while (!l.isEmpty() && Math.abs(l.get(0) - entry1currentOffset) > 1)
                            l.remove(0);
                        for (int ps : l) {
                            answer.add(p1.docID, ps);
                        }
                    }
                }
                i++;
                j++;
            } else if (p1.docID < p2.docID) {
                i++;
            } else
                j++;
        }
        return answer;
    }

    public void writeFilePaths() {}
    public void getFilePaths() {}
    public void writeIndexToDisk() {}

    /**
     *  no need for cleanup in a hashedindex.
     */
    public void cleanup() {
    }
}
