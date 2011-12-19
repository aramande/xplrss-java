import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.text.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

/**
 * Graphical reptresentation of an entry to be listed by the FeedList.
 */
public class EntryPanel extends JPanel implements MouseListener{
    private boolean maximized;
    private Entry entry;
    private JTextPane titlePane, datePane, contentPane;
    private StyledDocument title, date, content;
    private SimpleDateFormat dateFormat;
    private boolean selected;

    private static boolean fontsInited = false;
    private static StyleContext sc = new StyleContext();

    public EntryPanel(Entry entry){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        //addActionListener(this);
        dateFormat = new SimpleDateFormat("HH:mm EEE, d MMM yyyy");

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.LINE_AXIS));

        title = new DefaultStyledDocument(sc);
        titlePane = new JTextPane(title);
        titlePane.addMouseListener(this);
        titlePane.setSize(1650, 9999);

        date = new DefaultStyledDocument(sc);
        datePane = new JTextPane(date);
        datePane.addMouseListener(this);
        Style alignRight = sc.addStyle(null, null);
        alignRight.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_RIGHT);
        datePane.setParagraphAttributes(alignRight, true);

        content = new DefaultStyledDocument(sc);
        contentPane = new JTextPane(content);
        contentPane.setSize(1650, 9999);
        contentPane.addMouseListener(this);

        header.add(titlePane);
        header.add(datePane);

        body.add(contentPane);

        add(header);
        add(body);

        this.entry = entry;
        selected = false;
        maximized = !entry.isRead();
        setAlignmentX(0.0f);

        titlePane.setEditable(false);
        datePane.setEditable(false);
        contentPane.setEditable(false);

        initFonts();
    }

    public void select(){
        if(!selected){
            selected = true;
            render();
        }
        else{
            // TODO: Open url in browser!!
            Desktop desktop = Desktop.getDesktop();

            if(!desktop.isSupported(Desktop.Action.BROWSE)){
                System.err.println("Error: Desktop doesn't support the browse action");
                return;
            }

            try {
                java.net.URI uri = new URI(entry.getLink());
                System.out.println("[Open website in browser here]");
                desktop.browse(uri);
            }
            catch(URISyntaxException e){
                System.err.println("Error: Couldn't understand the link from entry: "+ entry.getTitle());
            }
            catch(IOException e){
                System.err.println("Error: Couldn't open the browser: "+ e);
            }
        }
    }

    public void deselect(){
        if(selected){
            selected = false;
            minimize();
        }
    }

    public void minimize(){
        if(maximized){
            maximized = false;
            render();
            resize();
        }
    }

    public void maximize(){
        if(!maximized){
            maximized = true;
            render();
            resize();
        }
    }

    public void toggle(){
        maximized = !maximized;
        render();
        resize();
    }


    /**
     * Ugly fix for scrolling the viewport up to the top entry. 
     */
    public void scrollTo(){
        titlePane.setCaretPosition(0);
    }

    /**
     * Resizes the entrypanel's textpanes so they wordwrap properly.
     * If width is 0, preferred width of the pane is used.
     */
    public void resize(){
        // Get width of the viewport
        int width = FeedList.init().getParent().getWidth();

        titlePane.setSize(width-150, 9999);
        datePane.setSize(150, 9999);
        contentPane.setSize(width, 9999);
        int titleHeight = (int)titlePane.getPreferredSize().getHeight();
        titlePane.setMaximumSize(new Dimension(width-150, titleHeight));
        datePane.setMaximumSize(new Dimension(150, titleHeight));
        datePane.setMinimumSize(new Dimension(150, titleHeight));

        if(content.getLength() == 0){
            contentPane.setMaximumSize(new Dimension(width, 0));
        }
        else{
            int contentHeight = (int)contentPane.getPreferredSize().getHeight();
            contentPane.setMaximumSize(new Dimension(width, contentHeight));
        }
    }

    /**
     * Draws out the text from the entry onto the textpane.
     */
    public void render(){
        if(selected){
            Color selectedColor = new Color(0xdd, 0xee, 0xff);
            titlePane.setBackground(selectedColor);
            datePane.setBackground(selectedColor);
            contentPane.setBackground(selectedColor);
        }
        else{
            Color normalColor = new Color(0xff, 0xff, 0xff);
            titlePane.setBackground(normalColor);
            datePane.setBackground(normalColor);
            contentPane.setBackground(normalColor);
        }

        if(maximized){
            try{
                title.remove(0, title.getLength());
                date.remove(0, date.getLength());
                content.remove(0, content.getLength());
                if(!entry.isRead())
                    title.insertString(title.getLength(), entry.getTitle(), sc.getStyle("titleFont"));
                else
                    title.insertString(title.getLength(), entry.getTitle(), sc.getStyle("readTitleFont"));

                if(!entry.getAuthor().equals("")){
                    title.insertString(title.getLength(), " (", sc.getStyle("textFont"));
                    title.insertString(title.getLength(), entry.getAuthor(), sc.getStyle("textFont"));
                    title.insertString(title.getLength(), ")", sc.getStyle("textFont"));
                }

                date.insertString(date.getLength(), dateFormat.format(entry.getPosted())+" ", sc.getStyle("dateFont"));

                String temp = getTagStructure(entry.getSummary());
                content.insertString(content.getLength(), temp, sc.getStyle("textFont"));
            }
            catch(BadLocationException e){
                System.err.println(e);
            }
        }
        else{
            try{
                title.remove(0, title.getLength());
                date.remove(0, date.getLength());
                content.remove(0, content.getLength());

                title.insertString(title.getLength(), entry.getTitle(), sc.getStyle("readTitleFont"));
                if(!entry.getAuthor().equals("")){
                    title.insertString(title.getLength(), " (", sc.getStyle("textFont"));
                    title.insertString(title.getLength(), entry.getAuthor(), sc.getStyle("textFont"));
                    title.insertString(title.getLength(), ")", sc.getStyle("textFont"));
                }

                date.insertString(date.getLength(), dateFormat.format(entry.getPosted())+" ", sc.getStyle("dateFont"));
            }
            catch(BadLocationException e){
                System.err.println(e);
            }
        }
    }

    private String getTagStructure(Tag current){
        String site = "";
        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content+" ");
        StringBuffer sb = new StringBuffer(current.content.length());
        while(match.find()){
            String index = match.group(1);
            String text = getTagStructure(current.children.get(Integer.parseInt(index)));
            match.appendReplacement(sb, Matcher.quoteReplacement(text));
        }
        match.appendTail(sb);
        return sb.toString();
    }

    /**
     * Create all the fonts used to render the entry.
     * This could be made into some kind of settings file later if I want to go
     * into details.
     */
    public static void initFonts(){
        if(!fontsInited){
            Style titleFont = sc.addStyle("titleFont", null);
            titleFont.addAttribute(StyleConstants.FontSize, new Integer(12));
            titleFont.addAttribute(StyleConstants.FontFamily, "sans");
            titleFont.addAttribute(StyleConstants.Bold, new Boolean(true));

            Style readTitleFont = sc.addStyle("readTitleFont", titleFont);
            readTitleFont.addAttribute(StyleConstants.Bold, new Boolean(false));

            Style dateFont = sc.addStyle("dateFont", null);
            dateFont.addAttribute(StyleConstants.FontSize, new Integer(10));
            dateFont.addAttribute(StyleConstants.FontFamily, "sans");

            Style textFont = sc.addStyle("textFont", null);
            textFont.addAttribute(StyleConstants.FontSize, new Integer(11));
            textFont.addAttribute(StyleConstants.FontFamily, "sans");

            Style boldFont = sc.addStyle("boldFont", textFont);
            boldFont.addAttribute(StyleConstants.Bold, new Boolean(true));

            Style italicFont = sc.addStyle("italicFont", textFont);
            italicFont.addAttribute(StyleConstants.Italic, new Boolean(true));

            fontsInited = true;
        }
    }

    public int hashCode(){
        // TODO: Implement hashcode function
        return 1;
    }

    public void mouseExited(MouseEvent e){
    }
    public void mouseEntered(MouseEvent e){
    }
    public void mousePressed(MouseEvent e){
    }
    public void mouseReleased(MouseEvent e){
        System.out.println("Mouse is poked!");
        maximize();
        entry.doRead();
        FeedList.init().selectEntry(this);
    }
    public void mouseClicked(MouseEvent e){
    }
}

