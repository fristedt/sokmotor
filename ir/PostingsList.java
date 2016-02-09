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
import java.util.ListIterator;
import java.util.List;
import java.util.HashSet;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();

    /**  Number of postings in this list  */
    public int size() {
	return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }

    public PostingsEntry getFirst() {
	return list.get(0);
    }

    public PostingsEntry getLast() {
	return list.get(list.size() - 1);
    }

    public void add(int docID) {
	list.add(new PostingsEntry(docID));
    }

    public void add(int docID, int offset) {
	if (!contains(docID)) {
	    PostingsEntry pe = new PostingsEntry(docID);
	    pe.positions.add(offset);
	    list.add(pe);
	} else {
	    PostingsEntry pe = list.get(list.size()-1);
	    pe.positions.add(offset);
	}
    }

    public void add(PostingsEntry pe) {
	list.add(pe);
    }

    public boolean contains(int docID) {
	for (PostingsEntry pe : list) {
	    if (pe.docID == docID)
		return true;
	}
	return false;
    }

    public void printIDs() {
	System.out.println("----Printing IDs----");
	for (PostingsEntry pe : list) {
	    System.out.println(pe.docID);
	}
	System.out.println("----Done     IDs----");
    }

    public List<Integer> getIDs() {
	List<Integer> result = new ArrayList<Integer>();
	for (PostingsEntry pe : list) {
	    result.add(pe.docID);
	}
	return result;
    }

    public void addAll(PostingsList other) {
	list.addAll(other.list);
    }
}
	

			   
