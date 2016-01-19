/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.List;
import java.util.HashSet;
import java.io.Serializable;
import java.util.stream.Collectors;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
    private HashSet<Integer> docIDs = new HashSet<Integer>();


    /**  Number of postings in this list  */
    public int size() {
	return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }

    public PostingsEntry getFirst() {
	return get(0);
    }

    public void add(int docID) {
	docIDs.add(docID);

	if (size() < 1) {
	    list.add(new PostingsEntry(docID));
	    return;
	}

	if (docID < list.getFirst().docID) {
	    list.addFirst(new PostingsEntry(docID));
	    return;
	}

	if (docID > list.getLast().docID) {
	    list.addLast(new PostingsEntry(docID));
	    return;
	}

	ListIterator<PostingsEntry> iterator = list.listIterator();
	while (iterator.hasNext()) {
	    PostingsEntry current = iterator.next();
	    if (docID > current.docID) {
		continue;
	    }
	    iterator.add(new PostingsEntry(docID));
	    return;
	}
    }

    public boolean contains(int docID) {
	return docIDs.contains(docID);
    }

    public void printIDs() {
	System.out.println("----Printing IDs----");
	for (PostingsEntry pe : list) {
	    System.out.println(pe.docID);
	}
	System.out.println("----Done     IDs----");
    }

    public List<Integer> getIDs() {
	return list.stream().map(e -> e.docID).collect(Collectors.toList());
    }
}
	

			   
