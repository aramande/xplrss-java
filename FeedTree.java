import javax.swing.*;
import javax.swing.tree.*;

/**
 * Graphical representation of the tree of feeds.
 */
class FeedTree extends JTree{
    public FeedTree(){
        super(createFeedTree());

        addTreeSelectionListener(FeedList.init());
    }

    /**
     * For now, this function creates a static tree, this will read from a file
     * later.
     * TODO: Read from file!
     */
    private static DefaultMutableTreeNode createFeedTree(){
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Feeds");
        DefaultMutableTreeNode youtube = new DefaultMutableTreeNode("YouTube");
        DefaultMutableTreeNode comics = new DefaultMutableTreeNode("Comics");
        top.add(youtube);
        top.add(comics);
        return top;
    }
}
