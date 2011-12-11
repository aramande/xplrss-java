import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Graphical representation of an entry-list from a feed contained within 
 * a page of the reader. 
 * Only one of these may exist at any one time of the instance of the program.
 */
class FeedList extends JPanel implements TreeSelectionListener, ComponentListener{
    private Feed currentFeed;
    private int currentPage;
    private static FeedList self = new FeedList();
    private EntryPanel selected;

    private FeedList(){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(new Color(0x23, 0x23, 0x62));
        
        currentFeed = null;
        currentPage = 0;
        selected = null;
    }

    public static FeedList init(){
        return self;
    }

    public void setFeed(Feed feed){
        currentFeed = feed;
        currentPage = 0;
        // Reset the feedlist
        removeAll();

        ListIterator<Entry> temp = currentFeed.getEntries();
        while(temp.hasNext()){
            Entry entry=temp.next();
            add(Box.createRigidArea(new Dimension(0,2)));
            add(entry.getView());
        }
        renderFeed(true, true);
        //((JScrollPane)getParent().getParent()).getVerticalScrollBar().setValue(0);
    }

    public void selectEntry(EntryPanel entry){
        if(entry.equals(selected)){
            selected.select();
        }
        else{
            if(selected != null)
                selected.deselect();
        
            selected = entry;

            if(selected != null)
                selected.select();
        }
    }

    /**
    public void nextPage(){
        if(currentFeed.hasNextPage()){
            ++currentPage;
            renderFeed();
        }
    }
    */

    /**
    public void previousPage(){
        if(currentPage != 0){
            --currentPage;
            renderFeed();
        }
    }
    */

    /**
     * Renders all entries in the feed if 'render' is true.
     * If width is set to 0, no resizing will be done, otherwise it will resize
     * all the entries and calculate the new height due to linewrap. 
     *
     * The term render here is used to describe the act of reprinting all the
     * text in the entry.
     */
    public void renderFeed(boolean render, boolean resize){
        Component[] entries = getComponents();
        boolean first = true;
        EntryPanel saved = null;
        for(int i=0; i<entries.length; ++i){
            if(entries[i] instanceof EntryPanel){
                if(first){
                    saved = ((EntryPanel)entries[i]);
                    first = false;
                }

                if(render)
                    ((EntryPanel)entries[i]).render();

                if(resize)
                    ((EntryPanel)entries[i]).resize();
            }
        }
        if(saved != null)
            saved.scrollTo();
    }

    public void valueChanged(TreeSelectionEvent e){
        System.out.println("Changed selection in tree, update FeedList");
    }
    public void componentHidden(ComponentEvent e) {
        System.out.println("Now?");
    }
    public void componentMoved(ComponentEvent e) {
    }
    public void componentResized(ComponentEvent e) {
        System.out.println("Resizing all entries");
        setSize(new Dimension(
                  ((JScrollPane)e.getComponent()).getViewport().getWidth(), 
                  getHeight()));
        renderFeed(false, true);
    }
    public void componentShown(ComponentEvent e) {
    }
}
