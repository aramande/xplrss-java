import java.util.*;
import javax.swing.tree.*;
/**
 * A combined version of all feeds in the node all mashed together.
 */
class CompoundFeed extends Feed{
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
                        entries = this.append((Feed)feed.getUserObject());
                    }
                }
                Collections.sort(entries, new SortByPosted());

                inited = true;
            }
        }

    @Override
        public void unInit(){
            DefaultTreeModel model = (DefaultTreeModel)FeedTree.init().getModel();
            for(Enumeration child = node.children(); child.hasMoreElements();){
                DefaultMutableTreeNode feed = (DefaultMutableTreeNode)child.nextElement();
                if(feed.getUserObject() instanceof Feed){
                    ((Feed)feed.getUserObject()).unInit();
                }
            }
            inited = false;
        }

    @Override
        public void update(){
            inited = false;
            init();

            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            if(parent != null){
                ((CompoundFeed)parent.getUserObject()).updateTreeNode();
            }
        }

    @Override
        public void updateTreeNode(){
            DefaultTreeModel model = (DefaultTreeModel)FeedTree.init().getModel();
            unreadCount = 0;
            for(Enumeration child = node.children(); child.hasMoreElements();){
                DefaultMutableTreeNode feed = (DefaultMutableTreeNode)child.nextElement();
                if(feed.getUserObject() instanceof Feed){
                    unreadCount += ((Feed)feed.getUserObject()).getUnreadCount();
                }
            }
            model.nodeChanged(node);

            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            if(parent != null){
                ((CompoundFeed)parent.getUserObject()).updateTreeNode();
            }
        }
}
