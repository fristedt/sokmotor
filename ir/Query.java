/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Query {
    private static final double alpha = 1;
    private static final double beta = 0.8;
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
	
	// Both query vector and document vector use term frequency as weight.

	// Query vector
	HashMap<String, Double> queryVector = new HashMap<String, Double>();
	double weightsSum = 0;
	for (int i = 0; i < terms.size(); ++i) {
	    weightsSum += weights.get(i);
	    if (queryVector.containsKey(terms.get(i))) {
		// Probably rare that someone searches for the same term twice.
		queryVector.put(terms.get(i), queryVector.get(terms.get(i)) + weights.get(i));
	    } else {
		queryVector.put(terms.get(i), weights.get(i));
	    }
	}

	// Normalize
	for (String term : queryVector.keySet()) {
	    queryVector.put(term, queryVector.get(term) / weightsSum);
	}

	// Document vector
	HashMap<String, Double> documentVectorSum = new HashMap<String, Double>();
	int numberOfRelevantDocs = 0;
	for (int i = 0; i < docIsRelevant.length; ++i) {
	    // Skip irrelevant documents.
	    if (!docIsRelevant[i])
		continue;

	    numberOfRelevantDocs += 1;
	    HashMap<String, Integer> documentVector = indexer.index.documentVectors.get(results.get(i).docID);

	    int tfSum = 0;
	    for (Integer tf : documentVector.values()) {
		tfSum += tf;
	    }

	    for (Map.Entry<String, Integer> entry : documentVector.entrySet()) {
		String term = entry.getKey();
		int tf = entry.getValue();
		// Normalize 
		double tfNorm = tf / (double) tfSum;

		if (documentVectorSum.containsKey(term)) {
		    // If term already in document vector sum, add the
		    // normalized term frequency.
		    documentVectorSum.put(term, documentVectorSum.get(term) + tfNorm);
		} else {
		    // Otherwise, just add the normalized term frequency.
		    documentVectorSum.put(term, tfNorm);
		}
	    }
	}

	// Normalize and put into average document vector.
	HashMap<String, Double> averageDocumentVector = new HashMap<String, Double>();
	for (Map.Entry<String, Double> entry : documentVectorSum.entrySet()) {
	    String term = entry.getKey();
	    double weight = entry.getValue();
	    averageDocumentVector.put(term, weight / numberOfRelevantDocs);
	}
	
	// Merge document vector and query vector, and sum their weights.
	HashMap<String, Double> mergedVector = new HashMap<String, Double>();
	for (Map.Entry<String, Double> entry : queryVector.entrySet()) {
	    mergedVector.put(entry.getKey(), entry.getValue() * alpha);
	}

	for (Map.Entry<String, Double> entry : averageDocumentVector.entrySet()) {
	    String term = entry.getKey();
	    double weight = entry.getValue() * beta;
	    if (mergedVector.containsKey(term)) {
		mergedVector.put(term, mergedVector.get(term) + weight);
	    } else {
		mergedVector.put(term, weight);
	    }
	}

	terms = new LinkedList<String>(mergedVector.keySet());
	weights = new LinkedList<Double>(mergedVector.values());
    }
}

    
