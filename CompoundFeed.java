import java.util.*;
/**
 * A combined version of all feeds in the category all mashed together.
 */
class CompoundFeed extends Feed{
    ArrayList<Feed> feeds;
    public CompoundFeed(){
        super("");
        feeds = new ArrayList<Feed>();
    }

    public CompoundFeed(Feed feed){
        super("");
        feeds = new ArrayList<Feed>();
        feeds.add(feed);
    }
}
