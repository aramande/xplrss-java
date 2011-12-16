import java.util.regex.*;
import java.util.*;
import java.net.*;
import java.io.*;
public class Parser{
    public Tag parseLocal(String filename){
        StringBuffer text = null;
        BufferedReader reader = null;
        try{
            text = new StringBuffer(1024);
            reader = new BufferedReader(new FileReader(filename));
            try{
                char[] buf = new char[512];
                int numRead=0;
                while((numRead=reader.read(buf)) != -1){
                    String read = String.valueOf(buf, 0, numRead);
                    text.append(read);
                    buf = new char[512];
                }
            }
            catch(IOException e){
                System.err.println("Error: Could not read file, "+ filename);
                return null;
            }
            finally{
                try{
                    reader.close();
                }
                catch(IOException e){
                    // Ignore
                }
            }
        }
        catch(FileNotFoundException e){
            System.err.println("Warning: No such file, "+ filename);
            return null;
        }

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(text.toString());
        text.setLength(0);
        tagNames = new Stack<String>();
        Tag parent = parseTokens(tokenizer);
        tokenizer = null;

        return parent;
    }
    public Tag parse(String urlString){
        StringBuffer buffer = new StringBuffer(1024); 
        String line = "";
        try{
            URL url = new URL(urlString);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            while((line = br.readLine()) != null) {
                buffer.append(line + "\n");
            }
        }
        catch(MalformedURLException e){
            System.err.println("Error: The URL is malformed, please try again");
        }
        catch(IOException e){
            System.err.println("Error: Something went wrong when trying to read the page");
        }
        if(buffer.length() == 0){
            return null;
        }
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(buffer.toString());
        buffer.setLength(0);
        tagNames = new Stack<String>();
        Tag parent = parseTokens(tokenizer);
        return parent;
    }

    /**
     * Example tag parsing function that just returns all the text from the
     * webpage. 
     */
    private String getTagStructure(Tag current){
        String site = "";
        Pattern pattern = Pattern.compile("%([0-9]+) ");
        Matcher match = pattern.matcher(current.content);
        StringBuffer sb = new StringBuffer(current.content.length());
        while(match.find()){
            String index = match.group(1);
            String text = getTagStructure(current.children.get(Integer.parseInt(index)));
            match.appendReplacement(sb, Matcher.quoteReplacement(text));
        }
        match.appendTail(sb);
        return sb.toString();
    }


    private Stack<String> tagNames;
    /**
     * Parse all tokens into a recursively linked Tag structure.
     * Can handle pages with multiple root nodes.
     *
     * @param tokenizer The tokenizer containing all the tokens to be parsed.
     *
     * @return First Tag in the structure.
     */
    private Tag parseTokens(Tokenizer tokenizer){
        Token token;
        Tag parent, currentTag;
        parent = new Tag();
        currentTag = parent;
        boolean wasSpace = true;
        while(true){
            if((token = tokenizer.nextToken()) == null){
                break;
            }
            if(token.identifier.equals("<")){
                token = tokenizer.nextToken();
                if(token.identifier.equals("/")){
                    // Found opening of end tag
                    String name = "";
                    for(Token tempToken : tokenizer.listTokensTo(">")){
                        token = tokenizer.nextToken();
                        name += tempToken.identifier;
                    }
                    if(name.equals(tagNames.peek())){
                        if((token = tokenizer.nextToken()) != null && !token.identifier.equals(">")){
                            System.err.println("Failed to close end tag for " + tagNames.peek() + ", expected > found " + token );
                            System.exit(1);
                        }
                        tagNames.pop();
                        currentTag = currentTag.parent;
                    }
                }
                else if(token.identifier.equals("!")){
                    // Found opening of comment tag, ignore comments
                    token = tokenizer.nextTokenOf(">");
                }
                else if(token.type == Type.ALPHA){
                    // Found opening of start tag
                    String name = token.identifier;
                    for(Token tempToken : tokenizer.listTokensTo(" ")){
                        if(tempToken.identifier.equals("/") || tempToken.identifier.equals(">")){
                            token = tempToken;
                            break;
                        }
                        token = tokenizer.nextToken();
                        name += tempToken.identifier;
                    }
                    tagNames.push(name);

                    Tag tempTag = new Tag();
                    tempTag.name = name;
                    tempTag.parent = currentTag;

                    currentTag.content += "%" + currentTag.children.size() + " ";
                    currentTag.children.add(tempTag);
                    currentTag = tempTag;                    
                    if(!token.identifier.equals(">")){
                        // Tag didn't end, parse all arguments
                        while((token = tokenizer.nextToken()) != null){
                            if(token.type == Type.ALPHA){
                                // Expect equals sign
                                ArrayList<Token> fullArgName = tokenizer.listTokensTo("="); 
                                String key = token.identifier;
                                for(Token tempToken : fullArgName){
                                    if(tempToken.identifier.equals("/") || tempToken.identifier.equals(">")){
                                        token = tempToken;
                                        break;
                                    }
                                    token = tokenizer.nextToken();
                                    key += tempToken.identifier;
                                }

                                String value = "";
                                ArrayList<Token> delimiters = new ArrayList<Token>();
                                delimiters.add(new Token("\"", null));
                                delimiters.add(new Token("\'", null));
                                delimiters.add(new Token(null, Type.ALPHA));
                                delimiters.add(new Token(null, Type.NUM));

                                token = tokenizer.nextTokenOf(delimiters); // Search for value

                                if(token.identifier.equals("\"")){
                                    for(Token values : tokenizer.listTokensTo("\"")){
                                        tokenizer.nextToken();
                                        value += values.identifier;
                                    }
                                }
                                else if(token.identifier.equals("\'")){
                                    for(Token values : tokenizer.listTokensTo("\'")){
                                        tokenizer.nextToken();
                                        value += values.identifier;
                                    }
                                }
                                else{
                                    // Found string or int value without quotes, parse until space
                                    for(Token values : tokenizer.listTokensTo(" ")){
                                        tokenizer.nextToken();
                                        value += values.identifier;
                                    }
                                }

                                currentTag.args.put(key, value);
                            }
                            else if(token.identifier.equals("/")){
                                // Found oneline tag
                                // NOTE: This takes into account any forward
                                // slash in the tag that does not belong to a
                                // value.
                                if(tagNames.peek().equals(name)){
                                    //System.out.println("Oneline popping tag " + tagNames.peek());
                                    tagNames.pop();
                                    currentTag = currentTag.parent;
                                }
                            }
                            else if(token.identifier.equals(">")){
                                break;
                            }
                        }
                    }
                    else{
                        tokenizer.moveTo(token);
                    }
                }
                else{
                    System.err.println("Syntax error: Found " + token.identifier + " with type " + token.type.toString());
                }
            }
            else{
                // Clear out some extra spaces that was created by empty tags
                if(!wasSpace && token.identifier.equals(" ")){
                    currentTag.content += token.identifier;
                    wasSpace = true;
                }
                else if(!token.identifier.equals(" ")){
                    currentTag.content += token.identifier;
                    wasSpace = false;
                }
            }
        }
        return parent;
    }
}

class Tag{
    public String name;
    public Tag parent;
    public ArrayList<Tag> children;
    public HashMap<String, String> args;
    public String content;

    public Tag(){
        children = new ArrayList<Tag>();
        args = new HashMap<String, String>();
        content = "";
    }

    public String toString(){
        return content;
    }
}
