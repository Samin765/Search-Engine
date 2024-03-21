package ir;

import java.util.*;

import java.io.*;
import java.util.*;
import java.lang.Math;

import javax.lang.model.util.ElementScanner6;


public class Searcher {

    Index index;

    KGramIndex kgIndex;
    HashMap<Integer, Double> pageRanks = new HashMap<Integer, Double>();

    public Searcher(Index index, KGramIndex kgIndex) {
        this.index = index;
        this.kgIndex = kgIndex;
        try {
            File file = new File("/Users/samin/InfoRet/assignment2/pagerank/pageRanks.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 2) {
                    String docID = parts[0].trim();
                    int id = Integer.parseInt((docID));
                    // Replace comma with dot for decimal parsing
                    String pgMaptr = parts[1].replace(',', '.');
                    //System.out.println(docID);
                    //System.out.println(pgMaptr);
                    Double pageRank = Double.parseDouble(pgMaptr.trim());
                    pageRanks.put(id, pageRank);
                }
            }
            System.out.println(pageRanks.get(3));

            System.out.println("READ pageRanks" + pageRanks.size());
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file " + "/Users/samin/InfoRet/assignment2/pagerank/pageRanks.txt");
            e.printStackTrace();
        }
    }

    private PostingsList positionalIntersect(PostingsList pl1, PostingsList pl2) {
        PostingsList answer = new PostingsList();

        Iterator<PostingsEntry> itr1 = pl1.getEntries().iterator(); // goes through all Entries of P1
        Iterator<PostingsEntry> itr2 = pl2.getEntries().iterator(); // goes through all Entries of P2

        if (!itr1.hasNext() || !itr2.hasNext()) { // if no more entries return
            return answer;
        }

        PostingsEntry itr1Value = itr1.next(); // Get next Entry from P1
        PostingsEntry itr2Value = itr2.next(); // Get next Entry from P2

        while (itr1Value != null && itr2Value != null) { // While there are more entries 
            if (itr1Value.docID == itr2Value.docID) {   //if we find a map between docID in entries

                List<Integer> pp1 = itr1Value.offsets;      // get offSet Lists for both
                List<Integer> pp2 = itr2Value.offsets;

                ArrayList<Integer> tempOffsets = new ArrayList<>(); // stores Offsets

                for (Integer pos1 : pp1) {  // goes through offsets from Entry 1
                    for (Integer pos2 : pp2) {   // goes through offsets from Entry 2
                        if (pos2 - pos1 == 1) {  // if the difference in position is only 1
                                    //System.out.println(pos2);

                            tempOffsets.add(pos2);  // Add the offSets to the offSetsList
                        }
                    }
            }

                if (!tempOffsets.isEmpty()) {       // if we have found offsets docID - docID = 1
                    answer.addEntryWithOffsets(itr1Value.docID, tempOffsets); // add entry to answer Posting List
                }

                itr1Value = itr1.hasNext() ? itr1.next() : null; // if hasNext() true = next entry otherwise null
                itr2Value = itr2.hasNext() ? itr2.next() : null;
            } else if (itr1Value.docID < itr2Value.docID) { // if docID for Entry 1 < docID for entry 2
                        //System.out.println("docId < 2docID");

                itr1Value = itr1.hasNext() ? itr1.next() : null; // Go to next entry in PostingsList1
            } else {
                itr2Value = itr2.hasNext() ? itr2.next() : null; // else Go to next entry in PostingsList2
            }
        }

        return answer;
    }



    private PostingsList intersect(PostingsList pl1, PostingsList pl2) {
        PostingsList answer = new PostingsList();

        Iterator<PostingsEntry> itr1 = pl1.getEntries().iterator(); // goes through all Entries of PL1
        Iterator<PostingsEntry> itr2 = pl2.getEntries().iterator(); // goes through all Entries of PL2
        //System.out.println(itr1);
        if (!itr1.hasNext() || !itr2.hasNext()) { // if no nore entries return
            return answer;
                    //System.out.println(answer);

        }

        PostingsEntry itr1Value = itr1.next(); // get next Entry in PL1
        PostingsEntry itr2Value = itr2.next(); // get next Entry in PL2

        while (itr1Value != null && itr2Value != null) { // while PL1 and PL2 not empty
            if (itr1Value.docID == itr2Value.docID) {  // if current Entry in PL1 and PL2 have same docID
                answer.add(new PostingsEntry(itr1Value.docID)); // add the entry to answer PL

                itr1Value = itr1.hasNext() ? itr1.next() : null; //set next entry if exists otherwhise null
                itr2Value = itr2.hasNext() ? itr2.next() : null;

            } else if (itr1Value.docID < itr2Value.docID) { // if docID < docID2 move forward with PL1
                itr1Value = itr1.hasNext() ? itr1.next() : null;

            } else {
                itr2Value = itr2.hasNext() ? itr2.next() : null; // otherwise move forward with PL2
            }
        }
                //System.out.println(answer);

        return answer;
    }

    private HashMap<Integer,Double> docTfIdf(PostingsList pl, String term){
        HashMap<Integer,Double> docs_tf_idf = new HashMap<Integer,Double>();

        for (int i = 0; i < pl.size() ; i ++){
            double doc_tf = pl.get(i).offsets.size();
            double doc_idf = Math.log(index.docLengths.size()/pl.size());
            double doc_tf_idf = (doc_tf * doc_idf) / index.docLengths.get(pl.get(i).docID);

            docs_tf_idf.put(pl.get(i).docID, doc_tf_idf);
        }

        return docs_tf_idf;
    }

    private Double cosSimiliarity(double docs_tf_idf, double query_tf_idf){
        return docs_tf_idf *   query_tf_idf;
    }

    private PostingsList ranked(PostingsList pl , String term){

        // 1. Calculate the tf_idf vector of query term
        double query_tf= 1;
        double query_idf = Math.log(index.docLengths.size()/pl.size());
        double query_tf_idf = query_tf * query_idf;

        // 2. Find Matching Documents 
        // 3. Calculate tf_idf-Vectors for all Matching Documents
        HashMap<Integer,Double> docs_tf_idf = docTfIdf(pl, term);
        for(int i = 0; i < docs_tf_idf.size(); i ++){
            pl.get(i).score = cosSimiliarity(docs_tf_idf.get(pl.get(i).docID), query_tf_idf);
        }
        // 4. Compute Cosin Similiarty between all tf_idc Vectors found and add it the score field in their PostingsEntry
        // 5. Sort the list based on the score
        Collections.sort(pl.getEntries());

        return pl;
    }

    private PostingsList sortScore(PostingsList pl, int N){

        int tf = 0; //number of occurrences of t in d       
        int df_t = 0; //number of documents in the corpus which contain t
        int lengthOfDocd = 0; //number of words in d
        double tf_idf = 0.0;
        double idf = 0.0;

        // for each PE in my PL, calculate the score and find tf_idf for each PE
        for(int i=0; i<pl.size(); i++){
            tf = pl.get(i).offsets.size();
            df_t = pl.size();
            lengthOfDocd = index.docLengths.get(pl.get(i).docID);
            idf = java.lang.Math.log((double)N/(double)df_t);

            tf_idf = (tf * idf)/lengthOfDocd;
            pl.get(i).score = tf_idf;
            
        }

        // sort my PL according to score
        Collections.sort(pl.getEntries());
        return pl;

    }

    public PostingsList search(ir.Query query, QueryType queryType, RankingType rankingType, NormalizationType normtype) {

        if (query.queryterm.size() > 1 && (queryType == QueryType.INTERSECTION_QUERY) || (queryType == QueryType.PHRASE_QUERY))  { // If a phrase
            PostingsList pl = index.getPostings(query.queryterm.get(0).term); // Get Postings for the first term

            for (int i = 1; i < query.queryterm.size(); i++) { // Go through the whole list of term
                String term = query.queryterm.get(i).term;  // Get the Term

                PostingsList plNext = index.getPostings(term); // Get the postingsList of the next Terms

    
                pl = queryType == QueryType.INTERSECTION_QUERY // Check if we want InterSection
                ? intersect(pl, plNext) : // Do InterSection
                positionalIntersect(pl, plNext); //Otherwise do positionalIntersection i.e Phrase Query
                
            }
            return pl; // Returns the PL
        } else if(queryType == QueryType.RANKED_QUERY && (rankingType == RankingType.TF_IDF)) {

            if(query.queryterm.size() == 1){   //if only a word
                String token = query.queryterm.get(0).term; // get term

            
            PostingsList pl = sortScore(index.getPostings(token), index.docLengths.size()); //sort
            return pl;
            }
            else{
                
                ArrayList<String> terms = new ArrayList<String>();  // List that holds all terms

                for (int i=0;i<query.queryterm.size();i++) {
                    terms.add(query.queryterm.get(i).term);  // Gets all terms in order
                }

                PostingsList result = new PostingsList();  // Result PL 

                for (int counter=0;counter<query.queryterm.size();counter++) {  //Process all terms
                    PostingsList wordPostingList = sortScore(index.getPostings(terms.get(counter)),Index.docLengths.size());  // Sort  with score
    
                    for (PostingsEntry pe : wordPostingList.getEntries()) {  // Go through entries
                        PostingsEntry existingEntry = result.search(pe.docID);  // Go through the list
                        if (existingEntry == null) {
                            result.add(new PostingsEntry(pe.docID, pe.score));   // Make new entry with score
                        } else {
                            existingEntry.score += pe.score;   // ADd the score if already exist
                        }
                    }
                }

                Collections.sort(result.getEntries());   // Sort it 
            return result;
        
            }
                
            
        }
        else if(query.queryterm.size() >= 1 && (queryType == QueryType.RANKED_QUERY) && (rankingType == RankingType.PAGERANK))
        {

           ArrayList<String> terms = new ArrayList<String>();   

           for (int i=0;i<query.queryterm.size();i++) {
               terms.add(query.queryterm.get(i).term);  // get Terms 
           }
          
           PostingsList result = new PostingsList();

           for (int i=0;i<query.queryterm.size();i++) {
               PostingsList wordPostingList = index.getPostings(terms.get(i));  // Goes through every terms

               for (PostingsEntry pe : wordPostingList.getEntries()) {  // Get the PostingsEntries for the term
                   PostingsEntry existingEntry = result.search(pe.docID);  // search for postingEntry with docID

                   if (existingEntry == null) {  // if it dosnt exist just create a new entry with the pageRank values
                       
                       result.add(new PostingsEntry( 
                                    pe.docID,pageRanks.get(pe.docID)) );
                   } else {
                       
                       existingEntry.score = pageRanks.get(pe.docID);  //add the pageRank value to existing Entry

                   }
               }
           }

           Collections.sort(result.getEntries());  //sort
           return result;  

            


        }

        else if(query.queryterm.size() >= 1 && (queryType == QueryType.RANKED_QUERY) && (rankingType == RankingType.COMBINATION))
        {
           ArrayList<String> terms = new ArrayList<String>();        

           for (int i=0;i<query.queryterm.size();i++) {
               terms.add(query.queryterm.get(i).term);  // Get all terms
           }
       
           PostingsList result = new PostingsList();

           for (int i=0; i<query.queryterm.size(); i++) { // go through all terms
               PostingsList wordPostingList = sortScore(index.getPostings(terms.get(i)),Index.docLengths.size()); // Get the PL based on score

               for (PostingsEntry pe : wordPostingList.getEntries()) {
                   PostingsEntry existingEntry = result.search(pe.docID);  // Search for entries in the PL 
                   if (existingEntry == null) {
                       result.add(new PostingsEntry(pe.docID, 0.6*pageRanks.get(pe.docID) + 0.4*pe.score)); // Combine pagerank and Score

                   } else {

                       existingEntry.score += 0.6*pe.score+ 0.4*pageRanks.get(pe.docID);
                   }
               }
           }

           Collections.sort(result.getEntries());
           return result;

        }

        else {
            return index.getPostings(query.queryterm.get(0).term); //If 1 word just return PL from index
        }
    }

    HashMap<Integer, Double> docpgMap = new HashMap<>();

    void readpgMap(String filename) {
        try {
            Scanner sc = new Scanner(new File(filename));
            while (sc.hasNextLine()) {
                int docID = sc.nextInt();
                double score = sc.nextDouble();
                docpgMap.put(docID, score);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error reading PageRank file: " + filename);
        }
    }
}
