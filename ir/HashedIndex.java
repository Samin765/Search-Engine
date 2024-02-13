
package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.*;



public class HashedIndex implements Index {


    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


   
    public void insert( String token, int docID, int offset ) {
        
        if (!index.containsKey(token)) {     // Token/term finns inte i Dictiionary                   
            PostingsEntry newEntry = new PostingsEntry(docID);
            newEntry.addOffset(offset);

            PostingsList newPostingList = new PostingsList();
            newPostingList.add(newEntry);

            index.put(token, newPostingList);
        } else {

            PostingsEntry entry = index.get(token).search(docID);

            if (entry == null) { // if no entry with docID create new entry
                PostingsEntry newEntry = new PostingsEntry(docID);
                newEntry.addOffset(offset);
                index.get(token).add(newEntry);

            } else {      // if entry exist just add offset                          
                
                entry.addOffset(offset);
            } 
        }
    }



    public PostingsList getPostings( String token ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        if (!index.containsKey(token)) { 
            return null;
        } else {
            PostingsList postingList = index.get(token);
            
            return postingList;
        }
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
        
    }


    

}
