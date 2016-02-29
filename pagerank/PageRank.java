/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    Random random = new Random();
    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    final static String PAGERANK_FILENAME = "pagerank.txt";

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */

    // Method 1
    double[] endpointRandomStart(int n, int N) {
	double[] pagerank = new double[n];

	for (int i = 0; i < N; ++i) {
	    pagerank[walk(randomPage(n), n)] += 1;
	}

	for (int i = 0; i < n; ++i) {
	    pagerank[i] /= (double) N;
	}

	return pagerank;
	// printTop(pagerank, 50);
    }

    // Method 2
    double[] endpointCyclicStart(int n, int m) {
	int N = n * m;
	double[] pagerank = new double[n];

	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < m; ++j) {
		pagerank[walk(i, n)] += 1;
	    }
	}

	for (int i = 0; i < n; ++i) {
	    pagerank[i] /= N;
	}

	return pagerank;
    }

    // Method 3
    double[] completePath(int n, int m, int T) {
	int N = n * m;
	double[] pagerank = new double[n];

	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < m; ++j) {
		completeWalk(i, n, pagerank, 1, T, false);
	    }
	}

	for (int i = 0; i < n; ++i) {
	    pagerank[i] /= (N * T);
	}

	return pagerank;
    }

    // Method 4
    double[] completePathDangling(int n, int m, int T) {
	int N = n * m;
	double[] pagerank = new double[n];

	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < m; ++j) {
		completeWalk(i, n, pagerank, 1, T, true);
	    }
	}

	double numberOfVisits = 0;
	for (int i = 0; i < n; ++i) {
	    numberOfVisits += pagerank[i];
	}

	for (int i = 0; i < n; ++i) {
	    pagerank[i] /= numberOfVisits;
	}

	return pagerank;
    }

    // Method 5
    double[] completePathRandomStart(int n, int N, int T) {
	double[] pagerank = new double[n];

	for (int i = 0; i < N; ++i) {
	    completeWalk(randomPage(n), n, pagerank, 1, T, true);
	}

	double numberOfVisits = 0;
	for (int i = 0; i < n; ++i) {
	    numberOfVisits += pagerank[i];
	}

	for (int i = 0; i < n; ++i) {
	    pagerank[i] /= numberOfVisits;
	}

	return pagerank;
    }

    int walk(int page, int n) {
	double x = random.nextDouble();
	// Terminate walk.
	if (x <= BORED)
	    return page;

	// No outlinks, terminate.
	if (out[page] == 0) 
	    return randomPage(n);

	return walk(randomPageFrom(page), n);
    }

    void completeWalk(int page, int n, double[] pagerank, int t, int T, boolean dangling) {
	pagerank[page] += 1;

	if (t >= T)
	    return;

	double x = random.nextDouble();

	if (x <= BORED) {
	    completeWalk(randomPage(n), n, pagerank, t+1, T, dangling);
	    return;
	}

	if (out[page] == 0) {
	    if (!dangling)
		completeWalk(randomPage(n), n, pagerank, t+1, T, dangling);
	    return;
	}

	completeWalk(randomPageFrom(page), n, pagerank, t+1, T, dangling);
    }

    int randomPage(int n) {
	return random.nextInt(n);
    }

    int randomPageFrom(int from) {
	int x = random.nextInt(out[from]);
	int i = 0;
	for (Integer to : link.get(from).keySet()) {
	    if (i == x)
		return to;
	    i += 1;
	}
	// Should never be reached.
	return -1;
    }

    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
	try {
	    double[] exactPagerank = readFromDisk(numberOfDocs);
	    int[] sizesOfN = new int[] {100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
	    // int[] sizesOfM = new int[] {1, 2, 3, 4, 5};
	    double lastD = 0;
	    for (int N : sizesOfN) {
	    // for (int m : sizesOfM) {
		// double[] approx = endpointCyclicStart(numberOfDocs, N);
		// double[] approx = endpointRandomStart(numberOfDocs, N);
		// double[] approx = completePath(numberOfDocs, m, 100);
		double[] approx = completePathRandomStart(numberOfDocs, N, 100);
		printTop(approx, 10);
		double d = getTopSquareDiffs(exactPagerank, approx, 50);
		System.out.printf("%10d: %e %e\n", N, d, Math.abs(lastD - d));
		lastD = d;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void exactPagerank(int numberOfDocs) {
	double[] x = new double[numberOfDocs];
	double[] xPrime = new double[numberOfDocs];
	xPrime[0] = 1;

	double[][] g = createG(numberOfDocs);

	double delta = diffNorm(x, xPrime);
	for (int k = 0; k < MAX_NUMBER_OF_ITERATIONS && delta > EPSILON; ++k) {
	    System.out.println("dlet: " + delta);
	    System.out.println("k: " + k);
	    x = xPrime;
	    xPrime = new double[numberOfDocs];
	    for (int i = 0; i < numberOfDocs; ++i) {
		for (int j = 0; j < numberOfDocs; ++j) {
		    xPrime[i] += g[j][i] * x[j];
		}
	    }
	    delta = diffNorm(x, xPrime);
	}

	try {
	    writeToDisk(xPrime, numberOfDocs);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private double[][] createG(int numberOfDocs) {
	double[][] g = new double[numberOfDocs][numberOfDocs];
	for (int i = 0; i < numberOfDocs; ++i) {
	    if (i % 1000 == 0)
		System.out.println("i: " + i);
	    for (int j = 0; j < numberOfDocs; ++j) {
		if (out[i] == 0) {
		    g[i][j] = 1 / (double) numberOfDocs;
		} else {
		    g[i][j] = getP(i, j) / (double) out[i];
		}
		g[i][j] *= (1 - BORED);
		g[i][j] += BORED / numberOfDocs;
	    }
	}
	return g;
    }

    private double diffNorm(double[] x, double[] xPrime) {
	double delta = 0;
	for (int i = 0; i < x.length; ++i) {
	    delta += Math.pow(x[i] - xPrime[i], 2); 
	}
	return Math.sqrt(delta);
    }

    private int getP(int i, int j) {
	// i has no outlinks.
	if (link.get(i) == null)
	    return 0;

	// i has no link to j.
	if (!link.get(i).keySet().contains(j))
	    return 0;

	return 1;
    }

    private void writeToDisk(double[] pagerank, int n) throws FileNotFoundException {
	PrintWriter writer = new PrintWriter(PAGERANK_FILENAME);
	for (int i = 0; i < n; ++i) {
	    writer.println(pagerank[i]);
	}
	writer.close();
    }

    private double[] readFromDisk(int n) throws FileNotFoundException, IOException {
	double[] pagerank = new double[n];
	BufferedReader in = new BufferedReader(new FileReader(PAGERANK_FILENAME));
	for (int i = 0; i < n; ++i) {
	    String line = in.readLine();
	    pagerank[i] = Double.parseDouble(line);
	}
	return pagerank;
    }

    private void printTop(double[] pageRank, int top) {
        List<ValueAndIndex> list = new LinkedList<ValueAndIndex>();
        for (int i = 0; i < pageRank.length; i++) {
            ValueAndIndex d = new ValueAndIndex(pageRank[i], docName[i]);
            list.add(d);
        }

        Collections.sort(list);

        for (int i = 0; i < top && i < pageRank.length; i++) {
            ValueAndIndex d = list.get(i);
            System.out.printf("%d: %s %f\n", i, d.docName, d.score);
        }
    }

    private double getTopSquareDiffs(double[] exact, double[] approx, int top) {
        List<ValueAndIndex> list = new LinkedList<ValueAndIndex>();
        for (int i = 0; i < exact.length; i++) {
            ValueAndIndex d = new ValueAndIndex(exact[i], docName[i]);
            list.add(d);
        }

        Collections.sort(list);

	double sum = 0;
        for (int i = 0; i < top && i < exact.length; i++) {
            ValueAndIndex d = list.get(i);
            sum += Math.pow(d.score - approx[docNumber.get(d.docName)], 2);
        }
	return sum;
    }

    private void printMatrix(double[][] matrix, int n) {
	for (int i = 0; i < n; ++i) {
	    System.out.printf("%d: ", i);
	    for (int j = 0; j < n; ++j) {
		System.out.printf("%e ", matrix[i][j]);
	    }
	    System.out.println("");
	}
    }

    private class ValueAndIndex implements Comparable<ValueAndIndex> {
        public double score;
        public String docName;

        public ValueAndIndex(double score, String docName) {
            this.score = score;
            this.docName = docName;
        }

        @Override
        public int compareTo(ValueAndIndex d) {
            return this.score < d.score ? 1 : this.score > d.score ? -1 : 0;
        }
    }

    /* --------------------------------------------- */

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
