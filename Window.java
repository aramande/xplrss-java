import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class Window extends JFrame{
    private JEditorPane htmlPage;
    private FeedList feedlist;
    private JTree sideBar;

    public Window(String title, int height, int width){
        super(title);
        setSize(height, width);
        setLayout(new BorderLayout());
    }
}
