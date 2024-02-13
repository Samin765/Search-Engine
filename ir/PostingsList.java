

package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class PostingsList {
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();
    private HashMap<Integer,PostingsEntry> map = new HashMap<Integer,PostingsEntry>();



    public ArrayList<PostingsEntry> getEntries()
    {
        return list;
    }

    public HashMap<Integer,PostingsEntry> getMap()
    {
        return map;
    }

    /** Number of postings in this list. */
    public int size() {
    return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
        if(i <= list.size())
            return list.get( i );
        return null;
    }
   
    // 
    //  YOUR CODE HERE
    //



    public void add(PostingsEntry e) {
        list.add(e);
        map.put(e.docID, e);
    }
    

    public PostingsEntry search(int docID) {
        
        if(map.containsKey(docID))
            return map.get(docID);
        else
            return null;
    }

    public void addEntryWithOffset(Integer docID, Integer offSet) { // Adds offsets
        
        PostingsEntry entry = new PostingsEntry(docID);
        entry.addOffset(offSet);
        list.add(entry);
        map.put(docID, entry); 
    }

    public void addEntryWithOffsets(Integer docID, ArrayList<Integer>  offSet) { // Add a list of offsets
        
        PostingsEntry entry = new PostingsEntry(docID);
        entry.addOffsets(offSet);
        list.add(entry);
        map.put(docID, entry); 
    }

    public void addOrdered(PostingsEntry entry) {
        if (list.isEmpty()) {
        list.add(entry);
        } 
        else {
        //using binary search just wnated to test can probably just use Collections.sort 
        int insertionPoint = Collections.binarySearch(list, entry, (e1, e2) -> Integer.compare(e1.docID, e2.docID));
        
        // If the entry is not present in the list(-(insertion point) - 1)..
        if (insertionPoint < 0) {
            // revese the insertion index
            insertionPoint = -(insertionPoint + 1);
        }
        
        
        list.add(insertionPoint, entry);
        }
    }


    
}

