import java.util.*;
import java.text.*;

/**
 * Data representation of an entry in a feed
 */
class Entry{
    private LinkedList<Entry> revisions;
    private EntryPanel view;
    private Data data;

    /**
     * Convenient storage structure.
     */
    class Data{
        public String guid;
        public String title;
        public String author;
        public Tag summary;
        public String link;
        public Date posted;
        public Date updated;
        public boolean read;
    }

    public Entry(){
        // TODO: remove this constructor if possible later
        data = new Data();
        data.title = "";
        data.author = "";
        data.summary = new Tag();
        data.link = "";
        data.posted = Calendar.getInstance().getTime();
        data.updated = Calendar.getInstance().getTime();
        data.read = false;
        view = new EntryPanel(this);
    }

    public Entry(String title, String author, Tag summary, 
            String link, Date posted, Date updated, boolean read){
        data = new Data();
        data.title = title;
        data.author = author;
        data.summary = summary;
        data.link = link;
        data.posted = posted;
        data.updated = updated;
        data.read = read;
        view = new EntryPanel(this);
    }

    public Entry(Tag current){
        data = initData(current);
        view = new EntryPanel(this);
    }

    private Data initData(Tag current){
        ArrayList<Tag> tags = current.children;
        Data result = new Data();
        for(Tag info : tags){
            if(info.name.equals("title")){
                result.title = info.content;
            }
            else if(info.name.equals("description")){
                result.summary = info;
            }
            else if(info.name.equals("pubDate")){
                try{
                    DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
                    result.posted = (Date)formatter.parse(info.content);
                }
                catch(ParseException e){
                    System.err.println(e);
                }
            }
        }
        return result;
    }

    public String getTitle(){
        return data.title; 
    }
    public String getAuthor(){
        return data.author; 
    }
    public Date getPosted(){
        return data.posted; 
    }
    public Tag getSummary(){
        return data.summary; 
    }

    public boolean isRead(){
        return data.read;
    }

    public void doRead(){
        data.read = true;
    }

    public void unRead(){
        data.read = false;
    }

    public EntryPanel getView(){
        return view;
    }

    public boolean equals(Object other){
        if(other instanceof Entry){
            Entry temp = (Entry)other;
            if(data.posted.equals(temp.data.posted) && data.author.equals(temp.data.author))
                return true;
        }
        return false;
    }
}

class SortByPosted implements Comparator<Entry>{
    public int compare(Entry self, Entry other){
        return 0;
    }
}

class SortByUpdated implements Comparator<Entry>{
    public int compare(Entry self, Entry other){
        return 0;
    }
}
