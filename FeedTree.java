import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Graphical representation of the tree of feeds.
 */
class FeedTree extends JTree{
    private ArrayList<TreePath> expanded;
    public FeedTree(){
        super();
        expanded = new ArrayList<TreePath>();
        DefaultMutableTreeNode treeNode = createFeedTree();
        setModel(new DefaultTreeModel(treeNode));
        addTreeSelectionListener(FeedList.init());

        for(TreePath path : expanded){
            expandPath(path);
        }
        System.out.println(tree2opml((DefaultMutableTreeNode)getModel().getRoot(), 0));
    }

    /**
     * For now, this function creates a static tree, this will read from a file
     * later.
     * TODO: Read from file!
     */
    private DefaultMutableTreeNode createFeedTree(){
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Feeds");
        Parser p = new Parser();
        Tag tags = p.parseLocal("xplrss.opml");
        if(tags != null){
            opml2tree(top, tags);
        }
        return top;
    }

    private String tree2opml(DefaultMutableTreeNode tree, int level){
        String result = "";
        if(level == 0){
            result += "<opml>\n\t<head>\n\t</head>\n\t<body>\n";
            result +=tree2opml(tree, level+1);
            result += "\t</body>\n</opml>";
        }
        else{
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
                    System.err.println("Fatal error: Leaf node was not a feed, please consult the creator of this software.");
                    return "";
                }
                result += "<outline text=\"";
                result += feed.getTitle();
                result += "\" title=\"";
                result += feed.getTitle();
                result += "\" xmlUrl=\"";
                result += feed.getXmlUrl();
                result += "\" htmlUrl=\"";
                result += feed.getHtmlUrl();
                result += "\" />\n";
            }
            else{

                result += "<outline text=\"";
                if(tree.getUserObject() instanceof String){
                    result += (String)tree.getUserObject();
                    result += "\" title=\"";
                    result += (String)tree.getUserObject();
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
                    System.out.println(current.args);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(current.args.get("name"));
                    if(!current.args.containsKey("xmlUrl")){
                        System.err.println("Missing xmlUrl argument on leaf outline: "+current.args.get("name"));
                        return;
                    }
                    node.setUserObject(new Feed(current.args.get("name"), current.args.get("xmlUrl")));
                    node.setAllowsChildren(false);
                    tree.add(node);
                }
                else{
                    // Found a Category
                    System.out.println(current.args);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(current.args.get("name"));
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
