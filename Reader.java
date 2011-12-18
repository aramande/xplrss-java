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

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import javax.swing.event.*;

public class Reader implements TreeSelectionListener{
    // Settings
    private int splitSize = 170;
    private JButton insertButton = new JButton("Insert");
    private JButton deleteButton = new JButton("Delete");


    public Reader(){
        Window window = new Window("Xplrss", 800, 540);
        window.addWindowListener(new FirstTimeRendering());

        FeedTree feedTree = createFeedTree();
        feedTree.addTreeSelectionListener(this);
        FeedList feedList = FeedList.init();

        JScrollPane scrollPane = new JScrollPane(feedTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        final JScrollPane scrollPane2 = new JScrollPane(feedList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane2.addComponentListener(feedList);

        // Prohibit the horizontal scrolling of the scrollbar
        scrollPane2.getHorizontalScrollBar().setMaximum(0);
        // Speed up the mousewheel speed a bit
        scrollPane2.getVerticalScrollBar().setUnitIncrement(16);

        JPanel treePane = new JPanel();
        JPanel buttonPane = new JPanel();

        treePane.setLayout(new BorderLayout());
        treePane.add(scrollPane);
        treePane.add(buttonPane, BorderLayout.SOUTH);

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(insertButton);
        buttonPane.add(deleteButton);

        insertButton.setEnabled(false);
        insertButton.addActionListener(new InsertionListener(feedTree));
        deleteButton.setEnabled(false);
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
                // User cancelled
                return; 

            DefaultTreeModel model = (DefaultTreeModel)feedTree.getModel();

            if(!nodeName.equals("")){
                try {
                    URL url = new URL(nodeName);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString(), "Error: Malformed URL", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString(), "Error: Input/Output error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                newNode = new DefaultMutableTreeNode(null, false);
                newNode.setUserObject(new Feed(nodeName, newNode));
            }
            else{
                newNode = new DefaultMutableTreeNode(null, true);
                newNode.setUserObject(new CompoundFeed("NewCategory", newNode));
            }
            //path = feedTree.getNextMatch("M", 0, Position.Bias.Forward);
            node = (MutableTreeNode)feedTree.getLastSelectedPathComponent();
            if(!node.getAllowsChildren()){
                node = (MutableTreeNode)node.getParent();
            }
            model.insertNodeInto(newNode, node, node.getChildCount());
            ((Feed)newNode.getUserObject()).init();
            ((Feed)FeedTree.init().getRoot().getUserObject()).updateTreeNode();

            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    FeedTree.init().saveToFile(null);
                }
            });
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
                // User clicked yes
                ((Feed)node.getUserObject()).unInit();
                model.removeNodeFromParent(node);
                ((Feed)FeedTree.init().getRoot().getUserObject()).updateTreeNode();
            }

            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    FeedTree.init().saveToFile(null);
                }
            });
        }
    }

    private FeedTree createFeedTree(){
        FeedTree tree = FeedTree.init();
        tree.setMaximumSize(new Dimension(270, 1650));
        return tree;
    }

    public void valueChanged(TreeSelectionEvent e){
        // TODO: Disable button when deselected
        deleteButton.setEnabled(true);
        insertButton.setEnabled(true);
    }

    public static void main(String[] args){
        new Reader();
    }
}

class FirstTimeRendering extends WindowAdapter{
    @Override
        public void windowOpened(WindowEvent e){
            System.out.println("Window opened");
            TreeModel model = FeedTree.init().getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
            CompoundFeed feed = (CompoundFeed)root.getUserObject();
            feed.init();

            AutomaticReload reload = new AutomaticReload();
            reload.start();

        }

    @Override
        public void windowClosing(WindowEvent e){
            System.out.println("Window closed");
            FeedTree.init().saveToFile(null);

            System.exit(0);
        }
}

class AutomaticReload extends Thread{
    @Override
        public void run(){
            while(true){
                try{
                    sleep(1000*60);
                    System.out.println("Reloading!");
                    DefaultMutableTreeNode root = FeedTree.init().getRoot();
                    ((Feed)root.getUserObject()).reload(new SortByPosted());
                    //Not working vvv, implement later?
                    //FeedList.init().renderFeed(true, true);
                }
                catch(InterruptedException e){
                    System.err.println(e);
                }
                catch(NullPointerException e){
                    System.err.println(e);
                }
            }
        }
}
