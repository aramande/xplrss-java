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

public class Reader{
    // Settings
    private int splitSize = 150;

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, scrollPane2);

        splitPane.setPreferredSize(new Dimension(800, 540));
        splitPane.setDividerLocation(splitSize);
        window.add(splitPane, BorderLayout.CENTER);

        window.pack();
        window.setVisible(true);
    }


    private FeedTree createFeedTree(){
        FeedTree tree = new FeedTree();
        tree.setMaximumSize(new Dimension(250, 1650));
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
