/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Collections;
import java.util.Comparator;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {

    /** The postings list as a linked list. */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();
    private HashSet<Integer> docIDs = new HashSet<Integer>();
    private final int N = 17486;

    public PostingsList() {
    }

    public double idf() {
        return Math.log(N/list.size());
    }

    public void addAll(PostingsList temp) {
        list.addAll(temp.list);
    }

    /**  Number of postings in this list  */
    public int size() {
        return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
        return list.get( i );
    }

    public void add(PostingsEntry pe) {
        if (!contains(pe.docID)) {
            docIDs.add(pe.docID);
            list.add(pe);
        } else {
	    // Just add the score.
	    for (int i = 0; i < size(); ++i) {
		if (get(i).docID == pe.docID)
		    get(i).score += pe.score;
	    }
	}
    }

    public void sort() {
        Collections.sort(list, new CompareEntries());
    }

    private class CompareEntries implements Comparator<PostingsEntry> {
        @Override
        public int compare(PostingsEntry pe1, PostingsEntry pe2) {
            double s1 = pe1.score;
            double s2 = pe2.score;
            return Double.compare(s2, s1);
        }
    }
    
    public void add(int docID) {
        if (!contains(docID)) {
            docIDs.add(docID);
            list.add(new PostingsEntry(docID));
        }
    }

    public void add(int docID, int offset) {
        if (!contains(docID)) {
            docIDs.add(docID);
            list.add(new PostingsEntry(docID, offset));
        } else {
            PostingsEntry pe = list.get(list.size()-1);
            pe.positions.add(offset);
        }
    }

    public boolean contains(int docID) {
        return docIDs.contains(docID);
    }

    public void printList() {
        System.out.println("BUUURG");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).docID);
        }
    }
}



