import java.util.*;
/**
 * Helper functions to assist in the parsing of xml.
 */
public class ParseHelper{
    private static ParseHelper self = null;
    private Tokenizer tokenizer;
    private ParseHelper(Tokenizer tokenizer){
        this.tokenizer = tokenizer;
    }

    public static ParseHelper init(Tokenizer tokenizer){
        if(self != null) return self;
        self = new ParseHelper(tokenizer);
        return self;
    }

    public static void uninit(){
        self = null;
    }

    /** 
     * Ignores a tag in the format of <!--.*-->.
     */ 
    public int parseCommentTag(Tag current, int tokenIndex){
        tokenizer.moveTo(tokenIndex);
        if(!tokenizer.isToken("<"))
            tokenizer.nextTokenOf("<");
        if(!tokenizer.isNextToken("!"))
            return tokenIndex;
        if(!tokenizer.isNextToken("-"))
            return tokenIndex;
        if(!tokenizer.isNextToken("-"))
            return tokenIndex;

        while(tokenizer.hasNextToken()){
            tokenizer.nextTokenOf("-");
            if(!tokenizer.isNextToken("-"))
                continue;
            if(!tokenizer.isNextToken(">"))
                continue;
            else{
                tokenizer.nextToken();
                return tokenizer.getIndex();
            }
        }
        return tokenIndex;
    }

    /**
     * Parses a tag in the format of "<![CDATA[.*]]>". 
     * Leaves content as-is and continue parsing tags recursively.
     */
    public int parseCDataTag(Tag current, int tokenIndex){
        tokenizer.moveTo(tokenIndex);
        if(!tokenizer.isToken("<"))
            tokenizer.nextTokenOf("<");
        if(!tokenizer.isNextToken("!"))
            return tokenIndex;
        if(!tokenizer.isNextToken("["))
            return tokenIndex;
        if(!tokenizer.isNextToken("CDATA"))
            return tokenIndex;
        if(!tokenizer.isNextToken("["))
            return tokenIndex;

        tokenizer.nextToken();

        while(tokenizer.hasNextToken()){
            if(!tokenizer.isToken("]")){
                for(Token token : tokenizer.listTokensTo("]")){
                    System.out.print(token);
                    if(token.identifier.equals("<")){
                        tokenizer.nextTokenOf("<");
                        break;
                    }
                    current.content.append(token.identifier);
                }
            }

            if(tokenizer.isToken("<")){
                Tag child = new Tag();
                int newIndex = 0;
                int currentIndex = tokenizer.getIndex();
                if((newIndex = parseEmptyTag(child, currentIndex)) != currentIndex){
                    current.content.append("%");
                    current.content.append(current.children.size());
                    current.content.append(" ");
                    current.children.add(child);

                    tokenizer.moveTo(newIndex);
                    continue;
                }
                else if((newIndex = parseContentTag(child, currentIndex)) != currentIndex){
                    current.content.append("%");
                    current.content.append(current.children.size());
                    current.content.append(" ");
                    current.children.add(child);

                    tokenizer.moveTo(newIndex);
                    continue;
                }
                else{
                    current.content.append("<");
                    continue;
                }
            }
            tokenizer.nextTokenOf("]");
            if(!tokenizer.isNextToken("]")){
                current.content.append("]");
                continue;
            }
            if(!tokenizer.isNextToken(">")){
                current.content.append("]]");
                continue;
            }
            else{
                current.name = "CDATA";
                tokenizer.nextToken();
                return tokenizer.getIndex();
            }
        }
        return tokenIndex;
    }

    /** 
     * Parses a tag in the format of "parseStartTag() .* parseEndTag()".
     * Decoding content as html and continue parsing tags recursively.
     */ 
    public int parseContentTag(Tag current, int tokenIndex){
        int newIndex = 0;
        int currentIndex = parseStartTag(current, tokenIndex);
        if(currentIndex == tokenIndex)
            return tokenIndex;

        String content = "";
        while(tokenizer.hasNextToken()){
            if(!tokenizer.isToken("<")){
                for(Token token : tokenizer.listTokensTo("<")){
                    current.content.append(token.identifier);
                }
                tokenizer.nextTokenOf("<");
            }
            Tag child = new Tag();
            currentIndex = tokenizer.getIndex();
            if((newIndex = parseEmptyTag(child, currentIndex)) != currentIndex){
                current.content.append("%");
                current.content.append(current.children.size());
                current.content.append(" ");
                current.children.add(child);
                
                currentIndex = newIndex;
            }
            else if((newIndex = parseContentTag(child, currentIndex)) != currentIndex){
                current.content.append("%");
                current.content.append(current.children.size());
                current.content.append(" ");
                current.children.add(child);

                currentIndex = newIndex;
            }
            else if((newIndex = parseEndTag(current.name, currentIndex)) != currentIndex){
                return newIndex;
            }
        }
        return tokenIndex;
    }

    /** 
     * Parses a tag in the format "<tag [parseArg() [...]]/>".
     */ 
    public int parseEmptyTag(Tag current, int tokenIndex){
        tokenizer.moveTo(tokenIndex);
        if(!tokenizer.isToken("<"))
            tokenizer.nextTokenOf("<");
        // Parse tagname
        if(!tokenizer.isNextToken(Type.ALPHA)){
            // Didn't find tagname
            if(!tokenizer.isNextToken(" ")){
                // Nor a space, so we fail here
                return tokenIndex;
            }
            if(!tokenizer.isNextToken(Type.ALPHA)){
                // We found a space but it wasn't followed by a word
                // definite failure here
                return tokenIndex;
            }
        }
        String tagName = tokenizer.getToken().identifier;
        if(tokenizer.isNextToken(":")){
            // Found a xml namespace, continue parsing
            tagName += ":" + tokenizer.nextToken().identifier;
            tokenizer.nextToken();
        }

        boolean foundEnding = false;
        int index = tokenizer.getIndex();
        int temp = parseArg(current, index);
        // Parse arguments
        while(index != temp){
            index = temp;
            temp = parseArg(current, index);
        }
        tokenizer.moveTo(temp);

        // Parse ending
        if(tokenizer.isToken(" ")){
            if(tokenizer.isNextToken("/")){
                if(tokenizer.isNextToken(">")){
                    foundEnding = true;
                }
                else if(tokenizer.isToken(" ")){
                    if(tokenizer.isNextToken(">")){
                        foundEnding = true;
                    }
                }
            }
            else if(tokenizer.isToken(">")){
                foundEnding = true;
            }
        }
        else if(tokenizer.isToken("/")){
            if(tokenizer.isNextToken(">")){
                foundEnding = true;
            }
            else if(tokenizer.isToken(" ")){
                if(tokenizer.isNextToken(">")){
                    foundEnding = true;
                }
            }
        }
        if(foundEnding){
            current.name = tagName;
            tokenizer.nextToken();
            return tokenizer.getIndex();
        }
        return tokenIndex;
    }


    /**
     * Parses a tag in the format "<tag [parseArg() [...]]>", saves all
     * information to 'current'.
     *
     * @return index of token before > if tag was found and index of < if it was not. 
     */
    public int parseStartTag(Tag current, int tokenIndex){
        tokenizer.moveTo(tokenIndex);
        if(!tokenizer.isToken("<"))
            tokenizer.nextTokenOf("<");
        if(!tokenizer.isNextToken(Type.ALPHA)){
            // Didn't find tagname
            if(!tokenizer.isNextToken(" ")){
                // Nor a space, so we fail here
                return tokenIndex;
            }
            if(!tokenizer.isNextToken(Type.ALPHA)){
                // We found a space but it wasn't followed by a word
                // definite failure here
                return tokenIndex;
            }
        }
        String tagName = tokenizer.getToken().identifier;
        while(tokenizer.hasNextToken()){
            tokenizer.nextToken();
            if(tokenizer.isToken(Type.ALPHA))
                tagName += tokenizer.getToken().identifier;
            else if(tokenizer.isToken(Type.NUM))
                tagName += tokenizer.getToken().identifier;
            else if(tokenizer.isToken(":")){
                // Found a xml namespace, continue parsing
                tagName += ":" + tokenizer.nextToken().identifier;
            }
            else{
                break;
            }
        }

        boolean foundEnding = false;
        int index = tokenizer.getIndex();
        int temp = parseArg(current, index);
        while(index != temp){
            index = temp;
            temp = parseArg(current, index);
        }
        tokenizer.moveTo(temp);

        if(tokenizer.isToken(" ")){
            if(tokenizer.isNextToken(">")){
                foundEnding = true;
            }
        }
        else if(tokenizer.isToken(">")){
            foundEnding = true;
        }

        if(!foundEnding){
            return tokenIndex;
        }

        current.name = tagName;
        tokenizer.nextToken();
        return tokenizer.getIndex();
    }

    /**
     * Parses a tag in the format "</tag>" where tag equals to tagName.
     */
    public int parseEndTag(String tagName, int tokenIndex){
        tokenizer.moveTo(tokenIndex);
        if(!tokenizer.isToken("<"))
            tokenizer.nextTokenOf("<");
        if(!tokenizer.isNextToken("/")){
            return tokenIndex;
        }
        if(!tokenizer.isNextToken(Type.ALPHA)){
            // Didn't find tagname
            if(!tokenizer.isNextToken(" ")){
                // Nor a space, so we fail here
                System.out.println(tokenizer.getToken());
                return tokenIndex;
            }
            if(!tokenizer.isNextToken(Type.ALPHA)){
                // We found a space but it wasn't followed by a word
                // definite failure here
                return tokenIndex;
            }
        }
        String temp = tokenizer.getToken().identifier;
        while(true){
            tokenizer.nextToken();
            if(tokenizer.isToken(Type.ALPHA))
                temp += tokenizer.getToken().identifier;
            else if(tokenizer.isToken(Type.NUM))
                temp += tokenizer.getToken().identifier;
            else if(tokenizer.isToken(":")){
                // Found a xml namespace, continue parsing
                temp += ":" + tokenizer.nextToken().identifier;
            }
            else{
                break;
            }
        }
        if(!tagName.equals(temp)){
            return tokenIndex;
        }
        tokenizer.nextToken();
        return tokenizer.getIndex();
    }


    /**
     * Parses an argument in the format "key=value", inserts into
     * current.args.
     */
    public int parseArg(Tag current, int tokenIndex){
        tokenizer.moveTo(tokenIndex);
        if(!tokenizer.isToken(Type.ALPHA))
            tokenizer.nextTokenOf(Type.ALPHA);
        String key = tokenizer.getToken().identifier;
        if(tokenizer.isNextToken(":")){
            // Found a xml namespace, continue parsing
            key += ":" + tokenizer.nextToken().identifier;
            tokenizer.nextToken();
        }
        if(!tokenizer.isToken("=")){
            if(!tokenizer.isToken(" ")){
                // Nor a space, so we fail here
                return tokenIndex;
            }
            if(!tokenizer.isNextToken("=")){
                // Space found but no equal
                return tokenIndex;
            }
        }
        StringBuffer value = new StringBuffer();
        int index = parseString(value, tokenizer.getIndex());
        current.args.put(key, value.toString());
        // This mathematic index is inaccurate and doesn't necessarily represent
        // the end of the value, but it's before that point.
        return index;
    }

    /**
     * Gets a string either surrounded by quotes or singlequotes 
     * if no such character is found, a single word until next space
     * or greater-than is found.
     *
     * @return token index of the token right after the string
     */
    public int parseString(StringBuffer value, int tokenIndex){
        Token token;
        boolean prevWasSlash = false;
        ArrayList<Token> delimiters = new ArrayList<Token>();
        delimiters.add(new Token("\"", null));
        delimiters.add(new Token("\'", null));
        delimiters.add(new Token(null, Type.ALPHA));
        delimiters.add(new Token(null, Type.NUM));

        tokenizer.moveTo(tokenIndex);
        token = tokenizer.nextTokenOf(delimiters); // Search for value

        if(token.identifier.equals("\"")){
            tokenizer.nextToken();
            for(Token values : tokenizer.listTokensTo("\"")){
                value.append(values.identifier);
            }
            tokenizer.nextTokenOf("\"");
            tokenizer.nextToken();
        }
        else if(token.identifier.equals("\'")){
            tokenizer.nextToken();
            for(Token values : tokenizer.listTokensTo("\'")){
                value.append(values.identifier);
            }
            tokenizer.nextTokenOf("\'");
            tokenizer.nextToken();
        }
        else{
            // Found string or int value without quotes, parse until space
            for(Token values : tokenizer.listTokensTo(" ")){
                if(values.identifier.equals(">")){
                    if(prevWasSlash){
                        tokenizer.moveTo(values);
                        return tokenizer.getIndex()-1;
                    }
                    else{
                        tokenizer.moveTo(values);
                        return tokenizer.getIndex();
                    }
                }
                if(prevWasSlash){
                    value.append("/");
                    prevWasSlash = false;
                }
                if(values.identifier.equals("/")){
                    prevWasSlash = true;
                    continue;
                }
                value.append(values.identifier);
            }
            tokenizer.nextTokenOf(" ");
        }
        if(prevWasSlash){
            value.append("/");
            prevWasSlash = false;
        }

        return tokenizer.getIndex();
    }

    private String cleanHtml(String content){
        int index = 0;
        final int size = 4;
        StringBuffer result = new StringBuffer(content);

        while(index < result.length()){
            int rightEnd = result.indexOf("&gt;", index);
            if(rightEnd == -1){
                return result.toString();
            }
            StringBuffer temp = new StringBuffer(result.substring(index, rightEnd));
            int leftEnd = temp.lastIndexOf("&lt;");
            if(leftEnd != -1){
                int other = temp.lastIndexOf(">");
                if(other > leftEnd){
                    index = leftEnd+index+size;
                    continue;
                }
                other = temp.lastIndexOf("<");
                if(other > leftEnd){
                    index = leftEnd+index+size;
                    continue;
                }
                result.replace(rightEnd, rightEnd + size, ">");
                result.replace(leftEnd + index, leftEnd + index + size, "<");
                index = leftEnd;
            }
            else{
                index = rightEnd+1;
            }
        }
        return result.toString();
    }

}
