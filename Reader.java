/**
 * Welcome to the sourcecode of Xplrss.
 *
 * The name Xplrss was derived from the words Explorer, due to the interface I
 * had in mind when planning the reader, and RSS from the functionality.
 *
 * It was of course contracted into an unrecognizable name as to avoid confusion
 * and legal issues. I made a google search for this name and nothing came up,
 * so it's safe to say that no one has used this name before.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;

public class Reader{
    // Settings
    private int splitSize = 170;

    public Reader(){
        Window window = new Window("Xplrss", 800, 540);
        window.addWindowListener(new FirstTimeRendering());

        FeedTree feedTree = createFeedTree();
        FeedList feedList = FeedList.init();

        JScrollPane scrollPane = new JScrollPane(feedTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        final JScrollPane scrollPane2 = new JScrollPane(feedList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane2.addComponentListener(feedList);

        // Prohibit the horizontal scrolling of the scrollbar
        scrollPane2.getHorizontalScrollBar().setMaximum(0);
        scrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane2.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
            public void adjustmentValueChanged(AdjustmentEvent e){
                //System.out.println("Scrollbar changed to " + e.getValue() + " by " + e.getAdjustmentType());
                //e.getAdjustable().setValue(0);
            }
        });

        JPanel treePane = new JPanel();
        JPanel buttonPane = new JPanel();
        JButton insertButton = new JButton("Insert");
        JButton deleteButton = new JButton("Delete");

        treePane.setLayout(new BorderLayout());
        treePane.add(scrollPane);
        treePane.add(buttonPane, BorderLayout.SOUTH);

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(insertButton);
        buttonPane.add(deleteButton);

        insertButton.addActionListener(new InsertionListener(feedTree));
        deleteButton.addActionListener(new DeletionListener(feedTree));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, scrollPane2);

        splitPane.setPreferredSize(new Dimension(800, 540));
        splitPane.setDividerLocation(splitSize);
        window.add(splitPane, BorderLayout.CENTER);

        window.pack();
        window.setVisible(true);
    }

    class InsertionListener implements ActionListener{
        FeedTree feedTree;
        public InsertionListener(FeedTree feedTree){
            this.feedTree = feedTree;
        }

        public void actionPerformed(ActionEvent e){
            //TreePath path;
            JTree tree;
            DefaultMutableTreeNode newNode;
            MutableTreeNode node;
            
            String nodeName = JOptionPane.showInputDialog(null, "Enter the feed url, leave empty if creating a category:");
            if(nodeName == null)
                return; // User cancelled

            DefaultTreeModel model = (DefaultTreeModel)feedTree.getModel();

            if(!nodeName.equals("")){
                newNode = new DefaultMutableTreeNode(new Feed(nodeName), false);
            }
            else{
                newNode = new DefaultMutableTreeNode("NewCategory", true);
            }
            //path = feedTree.getNextMatch("M", 0, Position.Bias.Forward);
            node = (MutableTreeNode)feedTree.getLastSelectedPathComponent();
            if(!node.getAllowsChildren()){
                node = (MutableTreeNode)node.getParent();
            }
            model.insertNodeInto(newNode, node, node.getChildCount());
        }
    }

    class DeletionListener implements ActionListener{
        FeedTree feedTree;
        public DeletionListener(FeedTree feedTree){
            this.feedTree = feedTree;
        }

        public void actionPerformed(ActionEvent e){
            DefaultMutableTreeNode node = null;
            DefaultTreeModel model = (DefaultTreeModel)feedTree.getModel();
            node = (DefaultMutableTreeNode)feedTree.getLastSelectedPathComponent();
            int answer = -1;
            if(!node.getAllowsChildren())
                answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this feed?", "Deleting "+node.toString(), JOptionPane.YES_NO_OPTION);
            else if(node.getChildCount() == 0)
                answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this category?", "Deleting "+node.toString(), JOptionPane.YES_NO_OPTION);
            else
                answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this category?\nAll of its feeds will be deleted too.", "Deleting "+node.toString(), JOptionPane.YES_NO_OPTION);

            if(answer == 0){
                model.removeNodeFromParent(node);
            }
        }
    }

    private FeedTree createFeedTree(){
        FeedTree tree = new FeedTree();
        tree.setMaximumSize(new Dimension(270, 1650));
        return tree;
    }

    public static void main(String[] args){
        new Reader();
    }
}

class FirstTimeRendering extends WindowAdapter{
    public void windowOpened(WindowEvent e){
        System.out.println("Window opened");
        //FeedList.init().setFeed(new Feed("http://notch.tumblr.com/rss"));
    }
}
