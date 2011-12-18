import java.io.*;
import java.util.*;
import javax.swing.tree.*;

/**
 * A combined version of all feeds in the node all mashed together.
 */
class CompoundFeed extends Feed implements Serializable{
    DefaultMutableTreeNode node;

    public CompoundFeed(String title, DefaultMutableTreeNode node){
        this.title = title;
        this.node = node;
    }

    @Override
        public void init(){
            if(!inited){
                DefaultTreeModel model = (DefaultTreeModel)FeedTree.init().getModel();
                for(Enumeration child = node.children(); child.hasMoreElements();){
                    DefaultMutableTreeNode feed = (DefaultMutableTreeNode)child.nextElement();
                    if(feed.getUserObject() instanceof Feed){
                        ((Feed)feed.getUserObject()).init();
                        //entries = this.append((Feed)feed.getUserObject());
                    }
                }
                //Collections.sort(entries, new SortByPosted());

                inited = true;
            }
        }

    /**
     * Get a list iterator for the entries in this compound feed
     */
    @Override
        public ListIterator<Entry> getEntries(){
            return new CompoundIterator(new SortByPosted());
        }
    /**
     * Get a list iterator for the entries in this compound feed 
     * in a specific sorted order.
     */
    public ListIterator<Entry> getEntries(Comparator<Entry> sorting){
        return new CompoundIterator(sorting);
    }

    /**
     * Uninitializes all children and tells them to release their memory.
     */
    @Override
        public void unInit(){
            unreadCount = 0;
            for(Enumeration child = node.children(); child.hasMoreElements();){
                DefaultMutableTreeNode feed = (DefaultMutableTreeNode)child.nextElement();
                if(feed.getUserObject() instanceof Feed){
                    ((Feed)feed.getUserObject()).unInit();
                }
            }
            inited = false;
        }

    @Override
        public void reload(Comparator<Entry> sorting){
            for(Enumeration child = node.children(); child.hasMoreElements();){
                DefaultMutableTreeNode feed = (DefaultMutableTreeNode)child.nextElement();
                if(feed.getUserObject() instanceof Feed){
                    ((Feed)feed.getUserObject()).reload(new SortByPosted());
                }
            }
        }

    /**
     * Update this tree node, and childrens tree node information.
     * This induces a depthfirst update througout the tree. Used when
     * nodes are moved around in the tree.
     */
    @Override
        public void updateTreeNode(){
            DefaultTreeModel model = (DefaultTreeModel)FeedTree.init().getModel();
            unreadCount = 0;
            for(Enumeration child = node.children(); child.hasMoreElements();){
                DefaultMutableTreeNode feed = (DefaultMutableTreeNode)child.nextElement();
                if(feed.getUserObject() instanceof Feed){
                    ((Feed)feed.getUserObject()).updateTreeNode();
                    unreadCount += ((Feed)feed.getUserObject()).getUnreadCount();
                }
            }
            model.nodeChanged(node);
        }

    /**
     * Iterator to list all the entries from a list of iterators.
     */
    class CompoundIterator implements ListIterator<Entry>{
        Comparator<Entry> sorting;
        ArrayList<ListIterator<Entry> > iters;
        public CompoundIterator(Comparator<Entry> sorting){
            this.sorting = sorting;
            this.iters = new ArrayList<ListIterator<Entry> >();
            Enumeration children = node.children();
            while(children.hasMoreElements()){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.nextElement();
                Feed feed = (Feed)node.getUserObject();
                iters.add(feed.getEntries());
            }
        }

        @Override
            public void add(Entry e){
            }
        @Override
            public void set(Entry e){
            }
        @Override
            public void remove(){
            }
        @Override
            public int previousIndex(){
                return 0;
            }
        @Override
            public int nextIndex(){
                return 0;
            }
        @Override
            public Entry previous(){
                return null;
            }
        @Override
            public boolean hasPrevious(){
                return false;
            }

        /**
         * Goes through all the iterators and gets the next one in the sorting
         * order.
         */
        @Override
            public Entry next(){
                Entry next = null;
                ListIterator<Entry> bestIter = null;
                for(ListIterator<Entry> iter : iters){
                    if(iter.hasNext()){
                        Entry temp = iter.next();
                        if(sorting.compare(next, temp) < 0){
                            if(bestIter != null)
                                // Move the pointer back a step if a earlier entry was found
                                bestIter.previous();
                            next = temp;
                            bestIter = iter;
                        }
                        else{
                            iter.previous();
                        }
                    }
                }
                return next;
            }

        /**
         * Checks if one of the iterators has a next element to list
         */
        @Override
            public boolean hasNext(){
                for(ListIterator<Entry> iter : iters){
                    if(iter.hasNext()){
                        return true;
                    }
                }
                return false;
            }
    }
}
