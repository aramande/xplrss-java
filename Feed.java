import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.tree.*;

/**
 * This is the data of a single feed that can be printed in the right area of the reader.
 */
class Feed{
    private LinkedList<Entry> entries;
    private ArrayList<Long> readEntries;
    private String xmlUrl;
    private String htmlUrl;
    private String title;
    private String description;
    private boolean inited;

    public Feed(String xmlUrl){
        this.title = "";
        this.xmlUrl = xmlUrl;
        this.htmlUrl = "";
        this.description = "";
        this.entries = new LinkedList<Entry>();
        this.readEntries = new ArrayList<Long>();
        this.inited = false;
    }
    public Feed(String title, String xmlUrl, ArrayList<Long> readEntries){
        this.title = title;
        this.xmlUrl = xmlUrl;
        this.htmlUrl = "";
        this.description = "";
        this.entries = new LinkedList<Entry>();
        this.readEntries = readEntries;
        this.inited = false;
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
            System.out.println(feed2rss());
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
        return entries.size()-readEntries.size();
    }

    private String feed2rss(){
        SimpleDateFormat s = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        String result = "";
        result += "<rss>\n\t<channel>\n";
        result += "\t\t<title>" + title + "</title>\n";
        result += "\t\t<description>" + description + "</description>\n";
        for(Entry entry : entries){
            result += "\t\t<item>\n";
            result += "\t\t\t<title>" + entry.getTitle() + "</title>\n";
            result += "\t\t\t<author>" + entry.getAuthor() + "</author>\n";
            result += "\t\t\t<description>" + entry.getSummary() + "</description>\n";
            result += "\t\t\t<pubDate>" + s.format(entry.getPosted()) + "</pubDate>\n";
            result += "\t\t</item>\n";
        }
        result += "\t</channel>\n</rss>";
        return result;
    }

    /**
     * @return The first element as a listiterator, to be able to iterate over
     * all the successive elements.
     */
    public ListIterator<Entry> getEntries(){
        init();
        return getEntries(0);
    }

    /**
     * @return An element with offset 'index' as a listiterator.
     */
    public ListIterator<Entry> getEntries(int index){
        init();
        return entries.listIterator(index);
    }

    /**
     * Get a list of the hashcodes that define the read entries.
     */
    public List<Long> getReadEntries(){
        return readEntries;
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
