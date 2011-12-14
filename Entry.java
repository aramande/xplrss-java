import java.util.*;
import java.util.regex.*;
import java.text.*;

/**
 * Data representation of an entry in a feed
 */
class Entry{
    public static final int RSS9 = 0;
    public static final int RSS91 = 1;
    public static final int RSS92 = 2;
    public static final int RSS2 = 3;
    public static final int ATOM = 4;

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

    public Entry(Tag current, int version){
        data = initData(current, version);
        view = new EntryPanel(this);
    }

    /**
     * 
     * @param current The recursive tagstructure representing the entry
     * @param version What kind of feed we're parsing
     * @see RSS9
     * @see RSS91
     * @see RSS92
     * @see RSS2
     * @see ATOM
     */
    private Data initData(Tag current, int version){
        ArrayList<Tag> tags = current.children;
        Data result = new Data();
        Tag defTag = new Tag();
        defTag.content = "No text";

        // Setting defaults
        result.title = "Unknown title";
        result.author = "";
        result.summary = defTag;
        result.link = "";
        result.posted = Calendar.getInstance().getTime();
        result.updated = Calendar.getInstance().getTime();
        result.read = false;

        if(version == RSS2){
            for(Tag info : tags){
                if(info.name.equals("title")){
                    result.title = info.content;
                }
                else if(info.name.equals("description")){
                    result.summary = info;
                }
                else if(info.name.equals("pubDate")){
                    result.posted = parseRFC822(info.content);
                }
                else if(info.name.equals("author")){
                    result.author = info.content;
                }
                else if(info.name.equals("link")){
                    result.link = info.content;
                }
            }
        }
        else if(version == ATOM){
            for(Tag info : tags){
                if(info.name.equals("title")){
                    result.title = info.content;
                }
                else if(info.name.equals("summary") || info.name.equals("content")){
                    result.summary = info;
                }
                else if(info.name.equals("updated")){
                    result.posted = parseRFC3339(info.content);
                }
                else if(info.name.equals("author")){
                    handleAuthor(result, info);
                }
                else if(result.link.equals("") && info.name.equals("link")){
                    if(!info.args.containsKey("rel"))
                        result.link = info.args.get("href");
                    else if(info.args.get("rel").equals("alternate")){
                        result.link = info.args.get("href");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Handles the Author-tag in an Atom feed
     */
    private void handleAuthor(Data result, Tag current){
        if(current.name != null){
            if(current.name.equals("name")){
                result.author = current.content;
            }
        }
        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            handleAuthor(result, current.children.get(Integer.parseInt(index)));
        }
    }


    public static Date parseRFC822(String datestring){
        Date d = new Date();
        SimpleDateFormat s = null;
        try{
            s = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
            d = s.parse(datestring);
        }
        catch(ParseException e){
            System.err.println("Warning: Could not parse the date of "+datestring);
        }
        return d;
    }

    /**
     * Taken from http://cokere.com/RFC3339Date.txt
     * Copyright belongs to Chad Okere (ceothrow1 at gmail dotcom).
     *
     * Copyright notice:
     * I was working on an Atom (http://www.w3.org/2005/Atom) parser and
     * discovered that I could not parse dates in the format defined by RFC 3339 using the
     * SimpleDateFormat class. The  reason was the ':' in the time  zone. This code strips out
     * the colon if it's there and tries four different formats on the resulting string
     * depending on if it has a  time zone, or if it has a  fractional second part.  There is a
     * probably a better way  to do this, and a more proper way.  But this is a really
     * small addition to a  codebase  (You don't  need a jar, just throw  this  function in
     * some  static Utility class if you have one).
     *
     * Feel free to use this in your code, but I'd appreciate it if you keep
     * this note  in the code if you distribute it.  Thanks!
     *
     * For  people  who might  be  googling: The date  format  parsed  by  this
     * goes  by: 
     * atomDateConstruct,  xsd:dateTime,  RFC3339  and  is compatable with:
     * ISO.8601.1988, W3C.NOTE-datetime-19980827  and  W3C.REC-xmlschema-2-20041028   (that  I
     * know  of)
     *
     *
     * Copyright 2007, Chad Okere (ceothrow1 at gmail dotcom)
     * OMG NO WARRENTY EXPRESSED OR IMPLIED!!!1
     */
    public static Date parseRFC3339(String datestring){
        Date d = new Date();
        SimpleDateFormat s = null;
        // if there is no time zone, we don't need to do any special
        // parsing.
        if(datestring.endsWith("Z")){
            try{
                s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US); // spec for RFC3339
                d = s.parse(datestring);
            }
            catch(ParseException e){
                // optional decimals SimpleDateFormat
                s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US); // spec for RFC3339 (with fractional seconds) 
                s.setLenient(true);
                try{
                    d = s.parse(datestring);
                }
                catch(ParseException f){
                    System.err.println("Warning: Could not parse the date of " + datestring);
                }
            }
            return d;
        }

        // step one, split off the timezone. 
        String firstpart = datestring.substring(0,datestring.lastIndexOf('-'));
        String secondpart = datestring.substring(datestring.lastIndexOf('-'));

        // step two, remove the colon from the timezone offset
        secondpart = secondpart.substring(0,secondpart.indexOf(':')) + secondpart.substring(secondpart.indexOf(':')+1);
        datestring = firstpart + secondpart;
        s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US); // spec for RFC3339       
        try{ 
            d = s.parse(datestring);          
        }
        catch(ParseException e){
            // Try again with optional decimals
            s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");// spec for RFC3339 (with fractional seconds)
            s.setLenient(true);
            try{
                d = s.parse(datestring);
            }
            catch(ParseException f){
                System.err.println("Warning: Could not parse the date of " + datestring);
            }
        }
        return d;
    }

    public String getTitle(){
        return data.title; 
    }
    public String getAuthor(){
        return data.author; 
    }
    public String getLink(){
        return data.link; 
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
