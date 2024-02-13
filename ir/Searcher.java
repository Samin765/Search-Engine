package ir;

import java.util.*;


public class Searcher {

    Index index;

    KGramIndex kgIndex;

    public Searcher(Index index, KGramIndex kgIndex) {
        this.index = index;
        this.kgIndex = kgIndex;
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

    public PostingsList search(Query query, QueryType queryType, RankingType rankingType, NormalizationType normtype) {

        if (query.queryterm.size() > 1) { // If a phrase
            PostingsList pl = index.getPostings(query.queryterm.get(0).term); // Get Postings for the first term

            for (int i = 1; i < query.queryterm.size(); i++) { // Go through the whole list of term
                String term = query.queryterm.get(i).term;  // Get the Term

                PostingsList plNext = index.getPostings(term); // Get the postingsList of the next Terms
                pl = queryType == QueryType.INTERSECTION_QUERY // Check if we want InterSection
                ? intersect(pl, plNext) : // Do InterSection
                positionalIntersect(pl, plNext); //Otherwise do positionalIntersection i.e Phrase Query
            }
            return pl; // Returns the PL
        } else {
            return index.getPostings(query.queryterm.get(0).term); //If 1 word just return PL from index
        }
    }
}
