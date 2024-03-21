import java.util.*;
import java.io.*;

public class PageRank {

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>(); // 

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS]; //L_q

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0000000001;

       
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	iterate( noOfDocs, 1000 );
    }







	/* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
	void iterate(int numberOfDocs, int maxIterations) {

		double[] r = new double[numberOfDocs]; // Array that holds the pageRanks for all docs
		Arrays.fill(r, 1.0 / numberOfDocs);  // The chance for surfer to land on any doc is 1/number of docs
	
		double[] newR = new double[numberOfDocs];  // we use this to hold the new Ranks
	
		for (int iter = 0; iter < maxIterations; iter++) {  // only iterate until maxIterations

			Arrays.fill(newR, 0.0);   // Placeholder values for the ranks
			double danglingRank = 0.0;    // ranks for a node that has no outlinks
	
			for (int i = 0; i < numberOfDocs; i++) {  // iterate through all docs
				if (out[i] == 0) { 		// Accumulate dangling rank for all sink nodes
					danglingRank += r[i];	
				} else {
					HashMap<Integer, Boolean> outlinks = link.get(i);  // get the outlinks for a doc
					if (outlinks != null) {			// doc is not a sink node
						for (Integer linkedDoc : outlinks.keySet()) {  // get all docs
							newR[linkedDoc] += (r[i] / out[i]);   // new Rank for doc is the rank from the linked page divided by
																	// number of outlinks the page has
						}
					}
				}
			}
	 
			double totalRank = 0.0;  // this is the total rank applying the bored surfer alg
			for (int i = 0; i < numberOfDocs; i++) {  
				newR[i] += danglingRank / numberOfDocs; // Distribute dangling rank
				newR[i] = BORED / numberOfDocs + (1 - BORED) * newR[i]; // bored surfer alg, either we follow the above calc or the surfer gets bored
				totalRank += newR[i];  // new ranks
			}
	
			// Normalization
			for (int i = 0; i < numberOfDocs; i++) {  // normalize due to round-off errors so all pageRanks totals to 1
				newR[i] /= totalRank;
			}
	
			double diff = 0.0;
			for (int i = 0; i < numberOfDocs; i++) {  // checks if we are getting a new probability matrix, 
				diff += Math.abs(newR[i] - r[i]);
			}
			if (diff < EPSILON) {		// if we are not improving the distrubtion by epilson then we can assume it's stationary
				break;
			}
			System.arraycopy(newR, 0, r, 0, numberOfDocs);  // jus
		}
	
		writeTopRanksToFile(r, numberOfDocs);
	}

	void printTopRanks(double[] ranks, int numberOfDocs) {
		Integer[] docIndices = new Integer[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			docIndices[i] = i;
		}
		Arrays.sort(docIndices, (a, b) -> Double.compare(ranks[b], ranks[a]));
	
		System.out.println("Top 30 Pages by PageRank:");
		for (int i = 0; i < Math.min(30, numberOfDocs); i++) {
			int docIdx = docIndices[i];
			System.out.printf("%d. %s \n", i + 1, docName[docIdx], ranks[docIdx]);
		}
	}

	void writeTopRanksToFile(double[] ranks, int numberOfDocs) {
		try {
			PrintWriter writer = new PrintWriter(new File("PageRanks.txt"));
			Integer[] docIndices = new Integer[numberOfDocs];
			for (int i = 0; i < numberOfDocs; i++) {
				docIndices[i] = i;
			}
			Arrays.sort(docIndices, (a, b) -> Double.compare(ranks[b], ranks[a]));
	
			for (int i = 0; i < numberOfDocs; i++) {
				int docIdx = docIndices[i];
				String line = String.format("%s\t%f", docIdx, ranks[docIdx]);
				writer.println(line);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error writing to PageRanks.txt");
			e.printStackTrace();
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


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
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
			link.put(fromdoc, new HashMap<Integer,Boolean>());
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


    
}
