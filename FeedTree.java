import java.io.*;
import java.awt.*;
import java.awt.dnd.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Graphical representation of the tree of feeds.
 */
public class FeedTree extends JTree{
    private ArrayList<TreePath> expanded;
    private String opmlFile;

    private static FeedTree self;

    private FeedTree(){
        super();
        opmlFile = "xplrss.opml";
        setEditable(true);
        setDropMode(DropMode.ON_OR_INSERT);

        expanded = new ArrayList<TreePath>();
        DefaultMutableTreeNode treeNode = createFeedTree();
        setModel(new FeedTreeModel(treeNode));
        addTreeSelectionListener(FeedList.init());

        DragAndDrop drag = new DragAndDrop(this, this, DnDConstants.ACTION_MOVE);

        for(TreePath path : expanded){
            expandPath(path);
        }
    }

    public static FeedTree init(){
        if(self == null){
            self = new FeedTree();
        }
        return self;
    }

    /**
     * For now, this function creates a static tree, this will read from a file
     * later.
     */
    private DefaultMutableTreeNode createFeedTree(){
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Feeds");
        top.setUserObject(new CompoundFeed("Feeds", top));
        Parser p = new Parser();
        Tag tags = p.parseLocal(opmlFile);
        if(tags != null){
            opml2tree(top, tags);
        }
        return top;
    }

    /**
     * A shortcut to the root of the tree.
     */
    public DefaultMutableTreeNode getRoot(){
        return (DefaultMutableTreeNode)getModel().getRoot();
    }


    /**
     * Saves the entire tree structure to file, overwriting the previous file.
     * @param newOpmlFile This is the 'Save as'-file, use null if you want to
     * use the previously loaded file. 
     */
    public void saveToFile(String newOpmlFile){
        String file = (newOpmlFile == null) ? opmlFile : newOpmlFile;
        try{
            FileWriter writer = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(writer);

            out.write(tree2opml(getRoot(), 0));

            out.close();
        }
        catch(IOException e){
            System.out.println("Couldn't write to file: "+file);
        }
    }

    /**
     * Loads the file into the tree structure, overwriting the current tree.
     * @param newOpmlFile File to load from, use null if you want to update the
     * tree using the same file.
     */
    public void loadFromFile(String newOpmlFile){
        String file = (newOpmlFile == null) ? opmlFile : newOpmlFile;

    }

    private String tree2opml(DefaultMutableTreeNode tree, int level){
        String result = "";
        if(level == 0){
            result += "<opml>\n\t<head>\n\t</head>\n\t<body>\n";
            result += tree2opml(tree, level+1);
            result += "\t</body>\n</opml>";
        }
        else{
            if(tree.isRoot()){
                // Don't insert the root node, or we'll get a recursive loop of
                // "Feeds" categories inside eachother every time we save and
                // load again.
                DefaultMutableTreeNode temp;
                for(Enumeration iter = tree.children(); iter.hasMoreElements();){
                    temp = (DefaultMutableTreeNode)iter.nextElement();
                    result += tree2opml(temp, level);
                }
                return result;
            }
            result += "\t";
            for(int i=0; i<level; ++i){
                result += "\t";
            }
            if(tree.isLeaf()){
                Feed feed = null;
                if(tree.getUserObject() instanceof Feed){
                    feed = (Feed)tree.getUserObject();
                }
                else{
                    System.err.println("Fatal error: Leaf node was not a feed, deleting node.");
                    return "";
                }
                result += "<outline text=\"";
                result += feed.getTitle();
                result += "\" title=\"";
                result += feed.getTitle();
                result += "\" id=\"";
                result += feed.hashCode();
                result += "\" xmlUrl=\"";
                result += feed.getXmlUrl();
                result += "\" htmlUrl=\"";
                result += feed.getHtmlUrl();
                result += "\" xpl:readEntries=\"";
                boolean first = true;
                for(Integer read : feed.getReadEntries()){
                    if(!first){
                        result += ",";
                    }
                    first = false;
                    result += Integer.toString(read, 16);
                }
                result += "\" />\n";
            }
            else{

                result += "<outline text=\"";
                if(tree.getUserObject() instanceof CompoundFeed){
                    result += ((CompoundFeed)tree.getUserObject()).getTitle();
                    result += "\" title=\"";
                    result += ((CompoundFeed)tree.getUserObject()).getTitle();
                }    
                else{
                    System.err.println("Fatal error: Non-leaf node was not a category, please consult the creator of this software.");
                    return "";
                }

                result += "\" xpl:expanded=\"";

                LinkedList<TreeNode> list = new LinkedList<TreeNode>();
                TreeNode temp2 = tree;
                while (temp2 != null) {
                    list.addFirst(temp2);
                    temp2 = temp2.getParent();
                }
                if(isExpanded(new TreePath(list.toArray()))){
                    result += "true";
                }
                else{
                    result += "false";
                }
                result += "\">\n";
                DefaultMutableTreeNode temp;
                for(Enumeration iter = tree.children(); iter.hasMoreElements();){
                    temp = (DefaultMutableTreeNode)iter.nextElement();
                    result += tree2opml(temp, level+1);
                }
                result += "\t";
                for(int i=0; i<level; ++i){
                    result += "\t";
                }
                result += "</outline>\n";
            }
        }
        return result;
    }


    private void opml2tree(DefaultMutableTreeNode tree, Tag current){
        if(current.name != null){
            if(current.name.equals("outline")){
                if(current.children.size() == 0){
                    // Found a Feed
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                    if(!current.args.containsKey("xmlUrl")){
                        System.err.println("Missing xmlUrl argument on leaf outline: "+current.args.get("text"));
                        return;
                    }

                    ArrayList<Integer> readEntries = new ArrayList<Integer>();
                    if(current.args.containsKey("xpl:readEntries") && !current.args.get("xpl:readEntries").equals("")){
                        String[] hashList = current.args.get("xpl:readEntries").split(",");
                        for(String hash : hashList){
                            readEntries.add(Integer.parseInt(hash, 16));
                        }
                    }

                    node.setUserObject(new Feed(current.args.get("text"), current.args.get("xmlUrl"), readEntries, node, Integer.parseInt(current.args.get("id"))));
                    node.setAllowsChildren(false);
                    tree.add(node);
                }
                else{
                    // Found a Category
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                    node.setAllowsChildren(true);
                    tree.add(node);

                    if(current.args.containsKey("xpl:expanded") && current.args.get("xpl:expanded").equals("true")){
                        LinkedList<TreeNode> list = new LinkedList<TreeNode>();
                        TreeNode temp = node;
                        while (temp != null) {
                            list.addFirst(temp);
                            temp = temp.getParent();
                        }
                        expanded.add(new TreePath(list.toArray()));
                    }

                    Pattern pattern = Pattern.compile("%([0-9]+) ");
                    Matcher match = pattern.matcher(current.content);

                    while(match.find()){
                        String index = match.group(1);
                        opml2tree(node, current.children.get(Integer.parseInt(index)));
                    }
                    node.setUserObject(new CompoundFeed(current.args.get("text"), node));
                    return;
                }
            }
        }
        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);

        while(match.find()){
            String index = match.group(1);
            opml2tree(tree, current.children.get(Integer.parseInt(index)));
        }
        return;
    }
}

class FeedTreeModel extends DefaultTreeModel{
    public FeedTreeModel(TreeNode root){
        super(root);
    }

    /**
     * Handling the renaming of labels in the feedtree so that the unread count
     * remains intact.
     * @param path The path to the node which is being changed
     * @param newValue Expecting a String to have as a title for the treelabel
     */
    @Override
        public void valueForPathChanged(TreePath path, Object newValue){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if(node.getUserObject() instanceof Feed){
                Feed temp = (Feed)node.getUserObject();
                temp.setTitle(newValue.toString());
                super.valueForPathChanged(path, temp);
            }
            else{
                super.valueForPathChanged(path, newValue);
            }
        }
}

/*@Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors){
  return true;
  }

//@SuppressWarnings("unchecked")
//@Override
public boolean importData(JComponent comp, Transferable t){
System.out.println("Importing data");
if(!canImport(comp, t.getTransferDataFlavors())){
return false;
}
JTree tree = (JTree) comp;
List<DefaultMutableTreeNode> data = null;
TransferSupport support = new TransferSupport(comp, t);

Point dropPoint = support.getDropLocation().getDropPoint();
TreePath path = tree.getPathForLocation(dropPoint.x, dropPoint.y);
DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
System.out.println("Heeere's Johnny!");

            try {
                data = (List) t.getTransferData(NodesTransferable.getDataFlavor());
                Iterator i = data.iterator();
                while (i.hasNext()) {
                    File f = (File) i.next();
                    parent.add(new DefaultMutableTreeNode(f.getName()));
                }
            }
            catch(UnsupportedFlavorException e){
                System.err.println(e);
            } 
            catch(IOException e){
                System.err.println(e);
            }

            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.reload();
            return true;
        }

    //@Override
        public int getSourceActions(JComponent comp) {
            return MOVE;
        }

    /**
     * This piece of code was taken from
     * http://stackoverflow.com/questions/4595998/subclass-of-treenode-dnd-issue
     * All rights belong to Denis Tulskiy.
     *
    //@Override
        public Transferable createTransferable(JComponent comp) {
            if(comp instanceof FeedTree){
                FeedTree tree = (FeedTree)comp;
                TreePath[] paths = tree.getSelectionPaths();
                ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
                for (TreePath path : tree.getSelectionPaths()) {
                    DefaultMutableTreeNode component = (DefaultMutableTreeNode) path.getLastPathComponent();
                    nodes.add(component);
                }
                return new NodesTransferable(nodes);
            }
            return null;
        }*/
