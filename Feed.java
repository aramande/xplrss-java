import java.util.*;
import java.util.regex.*;

/**
 * This is the data of a single feed that can be printed in the right area of the reader.
 */
class Feed{
    private LinkedList<Entry> entries;
    private ArrayList<Long> readEntriesContent;
    private ArrayList<Long> readEntriesID;
    private String xmlUrl;
    private String htmlUrl;

    public Feed(String xmlUrl){
        this.xmlUrl = xmlUrl;
        this.htmlUrl = "";
        entries = new LinkedList<Entry>();
        Parser parser = new Parser();
        Tag top = parser.parse(xmlUrl);
        init(top);
    }
    public Feed(){
        xmlUrl = "";
        htmlUrl = "";
        entries = new LinkedList<Entry>();
        /*
        Calendar temp = Calendar.getInstance();
        temp.set(2010, 3, 13, 21, 32, 21);
        entries.push(new Entry("My life in a nutshell", "Aramande", 
                    "This is not my blog entry from a long time ago, " +
                    "it's just a test of the wordwrap and generally the " +
                    "unread feature of the reader.", "http://google.com/", 
                    temp.getTime(),
                    Calendar.getInstance().getTime(),
                    false));
        temp.set(2011, 11, 4, 14, 56, 8);
        entries.push(new Entry("Unread, maximized entry", "Aramande", 
                    "Hello RSS World!", "http://google.com/", 
                    temp.getTime(),
                    Calendar.getInstance().getTime(),
                    false));
                    entries.push(new Entry("Read, minimized entry", "Aramande", 
                    "Hello RSS World!", "http://google.com/", 
                    Calendar.getInstance().getTime(),
                    Calendar.getInstance().getTime(),
                    true));
                    entries.push(new Entry("Read, minimized entry 2", "Aramande", 
                    "Hello RSS World!", "http://google.com/", 
                    Calendar.getInstance().getTime(),
                    Calendar.getInstance().getTime(),
                    true));
                    */
    }

    /**
     * Recursive function to go through the tag structure parsed by 
     * the parser and create the feed and its entries.
     */
    private void init(Tag current){
        if(current.name != null){
            if(current.name.equals("item")){
                entries.addLast(new Entry(current));
                return;
            }
        }

        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            init(current.children.get(Integer.parseInt(index)));
        }

    }

    public ListIterator<Entry> getEntries(){
        return getEntries(0);
    }

    public ListIterator<Entry> getEntries(int index){
        return entries.listIterator(index);
    }

    /**
     * Loads a section of a feed from the file into the list of entries.
     */
    public void load(int start, int length){

    }

    /**
     * Clears out everything after the index 'newEnd'.
     * Useful for releasing memory when switching pages.
     */
    public void unloadTail(int newEnd){
        //TODO: Implement this
    }

    /**
     * If the feed is no longer needed, clear out all the entries.
     */
    public void unloadAll(){
        entries.clear();
    }

    /**
     * Appends two list of entries, using the object and 'other', into one.
     * Make sure the feeds are loaded before running this function, or you will
     * get nothing in the result. Does not modify the object.
     */
    public LinkedList<Entry> append(Feed other){
        LinkedList<Entry> result = new LinkedList<Entry>(this.entries);
        result.addAll(other.entries);
        return result;
    }
}
