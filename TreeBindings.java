import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Handles all the mouseclicks in the rss reader
 */
public class TreeBindings extends MouseAdapter{
    public void mouseReleased(MouseEvent e){
        FeedTree tree = FeedTree.init();
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if(path == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        Feed feed = (Feed)node.getUserObject();

        switch(e.getButton()){
            case MouseEvent.BUTTON2:
                if(Settings.warnForReadAll()){
                    int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to mark\nall the entries in this feed as read?\n\nThis confirmation can be turned off in settings.", "Mark all as read", JOptionPane.YES_NO_OPTION);
                    if(answer == 0){
                        feed.readAll();
                    }
                }
                else{
                    feed.readAll();
                }
                break;
            case MouseEvent.BUTTON3:
                feed.reload(Settings.getSorting());
                break;
        }
    }
    //TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
}
