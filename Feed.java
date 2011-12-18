import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.tree.*;

/**
 * This is the data of a single feed that can be printed in the right area of the reader.
 */
class Feed implements Serializable{
    protected LinkedList<Entry> entries;
    protected ArrayList<Integer> readEntries;
    protected String xmlUrl;
    protected String htmlUrl;
    protected String guid;
    protected String title;
    protected String description;
    protected DefaultMutableTreeNode node;
    protected int unreadCount;
    protected int hash;
    protected boolean inited;

    public Feed(String xmlUrl, DefaultMutableTreeNode node){
        this.hash = 0;
        this.guid = "";
        this.title = "";
        this.xmlUrl = xmlUrl;
        this.htmlUrl = "";
        this.description = "";
        this.entries = new LinkedList<Entry>();
        this.readEntries = new ArrayList<Integer>();
        this.node = node;
        this.inited = false;
    }
    public Feed(String title, String xmlUrl, ArrayList<Integer> readEntries, DefaultMutableTreeNode node, int hash){
        this.hash = hash;
        this.guid = "";
        this.title = title;
        this.xmlUrl = xmlUrl;
        this.htmlUrl = "";
        this.description = "";
        this.entries = new LinkedList<Entry>();
        this.readEntries = readEntries;
        this.node = node;
        this.inited = false;
    }
    public Feed(){
        this.hash = 0;
        this.guid = "";
        this.title = "";
        this.xmlUrl = "";
        this.htmlUrl = "";
        this.description = "";
        this.entries = new LinkedList<Entry>();
        this.readEntries = new ArrayList<Integer>();
        this.inited = false;
    }

    /**
     * Loads the xmlfile for the first time
     */
    public void init(){
        System.gc();
        if(!inited){
            System.out.println("Inits "+title);
            if(hash != 0){
                try{
                    loadFile(new SortByPosted());
                } 
                catch(FileNotFoundException e){
                    reload(new SortByPosted());
                }
            }
            else
                reload(new SortByPosted());
            inited = true;
        }
    }

    /**
     * Loads the xml-file that was cached on the computer.
     */
    public void loadFile(Comparator<Entry> sorting) throws FileNotFoundException{
        Parser parser = new Parser();
        String filename = Integer.toString(hashCode())+".xml";
        Tag top = parser.parseLocal(filename);
        LinkedList<Entry> tempEntries = entries;
        entries = new LinkedList<Entry>();
        init(top, sorting, tempEntries);
        entries.addAll(tempEntries);
        top = null;

        ((Feed)FeedTree.init().getRoot().getUserObject()).updateTreeNode();
    }

    /**
     * Updates the feed using the xmlUrl
     */
    public void reload(Comparator<Entry> sorting){
        Parser parser = new Parser();
        Tag top = parser.parse(xmlUrl);
        LinkedList<Entry> tempEntries = entries;
        entries = new LinkedList<Entry>();
        init(top, sorting, tempEntries);
        entries.addAll(tempEntries);
        top = null;

        saveToFile();
    }

    /**
     * Recursive function to go through the tag structure parsed by 
     * the parser and create the feed and its entries.
     */
    private void init(Tag current, Comparator<Entry> sorting, LinkedList<Entry> tempEntries){
        if(current.name != null){
            if(current.name.equals("rss")){
                String version = current.args.get("version");
                if(version.equals("2.0")){
                    initRSS2(current, sorting, tempEntries);
                }
                else{
                    System.err.println("Warning: This RSS version ("+ current.args.get("version") +") is not yet fully supported, attempting to parse as RSS 3.0");
                    initRSS2(current, sorting, tempEntries);
                }
                return;
            }
            if(current.name.equals("feed")){
                initAtom(current, sorting, tempEntries);
                return;
            }

        }
        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            init(current.children.get(Integer.parseInt(index)), sorting, tempEntries);
        }
    }
    private void initRSS2(Tag current, Comparator<Entry> sorting, LinkedList<Entry> tempEntries){
        if(current.name != null){
            if(current.name.equals("item")){
                Entry newEntry = new Entry(current, Entry.RSS2, this);
                insertInto(newEntry, tempEntries, sorting);
                return;
            }
            else if(title.equals("") && current.name.equals("title")){
                title = Parser.getTagStructure(current);
            }
            else if(current.name.equals("guid")){
                guid = Parser.getTagStructure(current);
            }
            else if(current.name.equals("description")){
                description = Parser.getTagStructure(current);
            }
        }

        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            initRSS2(current.children.get(Integer.parseInt(index)), sorting, tempEntries);
        }

    }
    private void initAtom(Tag current, Comparator<Entry> sorting, LinkedList<Entry> tempEntries){
        if(current.name != null){
            if(current.name.equals("entry")){
                Entry newEntry = new Entry(current, Entry.ATOM, this);
                insertInto(newEntry, tempEntries, sorting);
                return;
            }
            else if(title.equals("") && current.name.equals("title")){
                title = Parser.getTagStructure(current);
            }
            else if(current.name.equals("id")){
                guid = Parser.getTagStructure(current);
            }
            else if(current.name.equals("summary")){
                description = Parser.getTagStructure(current);
            }
        }

        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            initAtom(current.children.get(Integer.parseInt(index)), sorting, tempEntries);
        }
    }

    /**
     * Automatically inserts newEntry into entries.
     */
    public void insertInto(Entry newEntry, LinkedList<Entry> tempEntries, Comparator<Entry> sorting){
        while(true){
            if(tempEntries.peek() == null){
                entries.addLast(newEntry);
                break;
            }
            Entry oldEntry = tempEntries.pop();
            int sort = sorting.compare(oldEntry, newEntry);
            if(sort < 0){
                // Inserting new entry
                entries.addLast(newEntry);
                tempEntries.push(oldEntry);
                break;
            }
            else if(sort > 0){
                // Inserting old entry
                entries.addLast(oldEntry);
            }
            else{
                // Updating old entry
                entries.addLast(newEntry);
                if(oldEntry.isRead()){
                    newEntry.doRead();
                }
                break;
            }
        }
    }


    /**
     * Unloads the entries, use this to save heap space
     */
    public void unInit(){
        System.out.println("Uninits "+title);
        inited = false;
        entries = null;
        System.gc();
        entries = new LinkedList<Entry>();
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

    public DefaultMutableTreeNode getNode(){
        return node;
    }

    public void readEntry(Integer entryHash){
        if(readEntries.contains(entryHash)){
            return;
        }
        readEntries.add(entryHash);
        ((Feed)(FeedTree.init().getRoot()).getUserObject()).updateTreeNode();
    }

    public void unreadEntry(Integer entryHash){
        if(readEntries.contains(entryHash)){
            readEntries.remove(entryHash);
        }
        ((Feed)(FeedTree.init().getRoot()).getUserObject()).updateTreeNode();
    }

    public int getUnreadCount(){
        return unreadCount;
    }

    public boolean isRead(Integer entryHash){
        return readEntries.contains(entryHash);
    }

    public void updateTreeNode(){
        FeedTreeModel model = (FeedTreeModel)FeedTree.init().getModel();
        unreadCount = entries.size() - readEntries.size();
        model.nodeChanged(node);
    }

    public void saveToFile(){
        String file = Integer.toString(hashCode());
        file += ".xml";
        try{
            FileWriter writer = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(writer);

            out.write(feed2rss());

            out.close();
        }
        catch(IOException e){
            System.err.println("Couldn't write to file: "+file);
        }
    }

    private String feed2rss(){
        SimpleDateFormat s = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        String result = "";
        result += "<rss version=\"2.0\">\n\t<channel>\n";
        result += "\t\t<title>" + title + "</title>\n";
        result += "\t\t<guid>" + guid + "</guid>\n";
        result += "\t\t<description>" + description + "</description>\n";
        for(Entry entry : entries){
            result += "\t\t<item>\n";
            //result += "\t\t\t<id>" + entry.hashCode() + "</id>\n";
            result += "\t\t\t<title>" + entry.getTitle() + "</title>\n";
            result += "\t\t\t<link>" + entry.getLink() + "</link>\n";
            result += "\t\t\t<author>" + entry.getAuthor() + "</author>\n";
            if(entry.getSummary().children.size() != 0){
                result += "\t\t\t<description>" + tag2string(entry.getSummary()) + "</description>\n";
            }
            else{
                result += "\t\t\t<description>" + entry.getSummary() + "</description>\n";
            }
            result += "\t\t\t<pubDate>" + s.format(entry.getPosted()) + "</pubDate>\n";
            result += "\t\t</item>\n";
        }
        result += "\t</channel>\n</rss>";
        return result;
    }

    private String tag2string(Tag tag){
        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(tag.content);
        StringBuffer sb = new StringBuffer(tag.content.length());

        if(tag.children.size() > 0 || !tag.content.equals("")){
            if(!tag.name.equals("content")
                    && !tag.name.equals("description")
                    && !tag.name.equals("summary")){

                while(match.find()){
                    String index = match.group(1);
                    String text = "";
                    Tag child = tag.children.get(Integer.parseInt(index));
                    text += "<" + child.name + ">";
                    text += tag2string(child);
                    text += "</" + child.name + ">";
                    match.appendReplacement(sb, Matcher.quoteReplacement(text));
                }
                match.appendTail(sb);

            }
            else{
                while(match.find()){
                    String index = match.group(1);
                    String text = tag2string(tag.children.get(Integer.parseInt(index)));
                    match.appendReplacement(sb, Matcher.quoteReplacement(text));
                }
            }
        }
        else{
            sb.append("<");
            sb.append(tag.name);
            sb.append("/>");
        }

        return sb.toString();
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
    public List<Integer> getReadEntries(){
        return readEntries;
    }

    /**
     * If the feed is no longer needed, clear out all the entries.
     */
    public void unloadAll(){
        entries.clear();
    }

    public String toString(){
        if(getUnreadCount() <= 0)
            return title;
        return title + " (" + getUnreadCount() + ")";
    }

    public int hashCode(){
        if(hash != 0) return hash;
        if(!guid.equals("")) return guid.hashCode();
        if(!description.equals("")) return description.hashCode();
        else return title.hashCode();
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
