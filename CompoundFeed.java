import java.util.*;
import javax.swing.tree.*;
/**
 * A combined version of all feeds in the category all mashed together.
 */
class CompoundFeed extends Feed{
    DefaultMutableTreeNode category;

    public CompoundFeed(String title, DefaultMutableTreeNode category){
        this.title = title;
        this.category = category;
    }

    @Override
    public void init(){
        unreadCount = 0;
        for(Enumeration child = category.children(); child.hasMoreElements();){
            DefaultMutableTreeNode feed = (DefaultMutableTreeNode)child.nextElement();
            if(feed.getUserObject() instanceof Feed){
                ((Feed)feed.getUserObject()).init();
                entries = this.append((Feed)feed.getUserObject());
                unreadCount += ((Feed)feed.getUserObject()).getUnreadCount();
            }
        }
        Collections.sort(entries, new SortByPosted());
        inited = true;
    }
}
