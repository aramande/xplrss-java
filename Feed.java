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
    private String title;
    private String description;
    private boolean inited;

    public Feed(String xmlUrl){
        this.title = "";
        this.xmlUrl = xmlUrl;
        this.htmlUrl = "";
        this.inited = false;
        this.entries = new LinkedList<Entry>();
        this.readEntriesContent = new ArrayList<Long>();
        this.readEntriesID = new ArrayList<Long>();
        init();
    }
    public Feed(String title, String xmlUrl){
        this.title = title;
        this.xmlUrl = xmlUrl;
        this.htmlUrl = "";
        this.inited = false;
        this.entries = new LinkedList<Entry>();
        this.readEntriesContent = new ArrayList<Long>();
        this.readEntriesID = new ArrayList<Long>();
        init();
    }
    public Feed(){
        title = "";
        xmlUrl = "";
        htmlUrl = "";
        entries = new LinkedList<Entry>();
    }

    /**
     * Loads the xmlfile for the first time
     */
    public void init(){
        if(!inited){
            Parser parser = new Parser();
            Tag top = parser.parse(xmlUrl);
            init(top);
            inited = true;
        }
    }

    /**
     * Recursive function to go through the tag structure parsed by 
     * the parser and create the feed and its entries.
     */
    private void init(Tag current){
        if(current.name != null){
            if(current.name.equals("rss")){
                String version = current.args.get("version");
                if(version.equals("2.0")){
                    initRSS2(current);
                }
                else{
                    System.err.println("Warning: This RSS version ("+ current.args.get("version") +") is not yet fully supported, attempting to parse as RSS 3.0");
                    initRSS2(current);
                }
                return;
            }
            if(current.name.equals("feed")){
                initAtom(current);
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
    private void initRSS2(Tag current){
        if(current.name != null){
            if(current.name.equals("item")){
                entries.addLast(new Entry(current, Entry.RSS2));
                return;
            }
            else if(title.equals("") && current.name.equals("title")){
                title = current.content;
            }
            else if(current.name.equals("description")){
                description = current.content;
            }
        }

        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            initRSS2(current.children.get(Integer.parseInt(index)));
        }

    }
    private void initAtom(Tag current){
        if(current.name != null){
            if(current.name.equals("entry")){
                entries.addLast(new Entry(current, Entry.ATOM));
                return;
            }
            else if(title.equals("") && current.name.equals("title")){
                title = current.content;
            }
            else if(current.name.equals("summary")){
                description = current.content;
            }
        }


        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            initAtom(current.children.get(Integer.parseInt(index)));
        }
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String value){
        if(value == null)
            return;
        if(value.equals(""))
            return;
        this.title = value;
    }

    public String getXmlUrl(){
        return xmlUrl;
    }

    public String getHtmlUrl(){
        return htmlUrl;
    }

    public int getUnreadCount(){
        return entries.size()-readEntriesID.size();
    }

    /**
     * @return The first element as a listiterator, to be able to iterate over
     * all the successive elements.
     */
    public ListIterator<Entry> getEntries(){
        return getEntries(0);
    }

    /**
     * @return An element with offset 'index' as a listiterator.
     */
    public ListIterator<Entry> getEntries(int index){
        return entries.listIterator(index);
    }

    /**
     * If the feed is no longer needed, clear out all the entries.
     */
    public void unloadAll(){
        entries.clear();
    }

    public String toString(){
        return title + " (" + getUnreadCount() + ")";
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
    public LinkedList<Entry> append(LinkedList<Entry> other){
        LinkedList<Entry> result = new LinkedList<Entry>(this.entries);
        result.addAll(other);
        return result;
    }
}
