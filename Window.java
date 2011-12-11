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

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Notifies the htmlPage to start rendering the 'url'.
    public void render(URL url){
        try{
            htmlPage.setPage(url);
            //adressField.setText(url.toExternalForm());
        }
        catch(IOException e){
            System.out.println("Can't follow link to " + url.toExternalForm());
        }
    }
     */


    /**
     * Listenerclass to handle the clicks on links.
    class LinkListener implements HyperlinkListener{
        public void hyperlinkUpdate(HyperlinkEvent event){
            if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                render(event.getURL());
            }
        }
    }
     */

    /**
     * Listenerclass to handle all the user input alternatives to submit the text in
     * the adressfield.
    class GoAction implements ActionListener, KeyListener{
        public void actionPerformed(ActionEvent event){
        }

        public void keyPressed(KeyEvent event){
            switch(event.getKeyCode()){
                case KeyEvent.VK_ENTER:
                    break;
            }
        }


        public void keyReleased(KeyEvent event){}
        public void keyTyped(KeyEvent event){}
    }
     */
}
