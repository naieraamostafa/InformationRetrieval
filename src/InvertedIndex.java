import java.io.*;
import java.util.*;

public class InvertedIndex {
    HashMap<String, DictEntry> index;

    public InvertedIndex() {
        index = new HashMap<>();
    }

    public void buildIndex(String[] filenames) throws IOException {
        for (String filename : filenames) {
            int docId = Integer.parseInt(filename.substring(0, filename.lastIndexOf(".")));
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] terms = line.split(" ");
                for (String term : terms) {
                    term = term.toLowerCase().replaceAll("[^a-z0-9 ]", "");
                    if (term.length() == 0)
                        continue;
                    if (!index.containsKey(term))
                        index.put(term, new DictEntry());
                    DictEntry entry = index.get(term);
                    entry.term_freq++;
                    if (entry.pList == null || entry.pList.docId != docId) {
                        entry.doc_freq++;
                        Posting posting = new Posting();
                        posting.docId = docId;
                        entry.pList = addPostingToList(entry.pList, posting);
                    } else {
                        entry.pList.term_freq++;
                    }
                }
            }
            reader.close();
        }
    }

    private Posting addPostingToList(Posting head, Posting posting) {
        if (head == null)
            return posting;
        if (posting.docId < head.docId) {
            posting.next = head;
            return posting;
        }
        head.next = addPostingToList(head.next, posting);
        return head;
    }

    public SearchResult search(String query) {
        query = query.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        if (!index.containsKey(query))
            return null;
        Posting pList = index.get(query).pList;
        SearchResult result = new SearchResult();
        result.term_freq = index.get(query).term_freq;
        result.doc_freq = index.get(query).doc_freq;
        while (pList != null) {
            result.docIds.add(pList.docId);
            pList = pList.next;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        String[] filenames = {"0.txt","1.txt","2.txt","3.txt"};
        InvertedIndex index = new InvertedIndex();
        index.buildIndex(filenames);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a query: ");
        String query = scanner.nextLine();
        SearchResult result = index.search(query);
        if (result != null) {
            System.out.println("Term frequency: " + result.term_freq);
            System.out.println("Document frequency: " + result.doc_freq);
            System.out.println("Document IDs: " + result.docIds);
        } else {
            System.out.println("Not found.");
        }

    }

    public class DictEntry {
        int doc_freq = 0;
        int term_freq = 0;
        Posting pList = null;
    }

    public class Posting {
        public int term_freq;
        int docId;
        int dtf = 1;
        Posting next = null;
    }

    public class SearchResult {
        int term_freq;
        int doc_freq;
        List<Integer> docIds = new ArrayList<>();
    }
}