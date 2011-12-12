import java.util.*;
public class Tokenizer{
    ArrayList<Token> tokens = new ArrayList<Token>();
    int currentToken;

    public Tokenizer(){
        tokens = new ArrayList<Token>();
        currentToken = -1;
    }

    /**
     * Retrieve the next token from the list and increment the currentToken
     * counter.
     */
    public Token nextToken(){
        if(currentToken+1 >= tokens.size())
            return null; //throw new ArrayIndexOutOfBoundsException();
        //System.out.println(currentToken+1);
        return tokens.get(++currentToken);
    }

    /**
     * Search through the list of tokens until the next of provided token identifier. 
     * Looks for a token ahead of the currentToken. 
     * 
     * @return The token of the identifier
     */
    public Token nextTokenOf(String identifier){
        Token token;
        
        while((token = nextToken()) != null){
            if(token.identifier.equals(identifier)){
                break;
            }
        }
        return token;
    }

    public Token nextTokenOf(List<Token> delimiters){
        Token token;
        
        while((token = nextToken()) != null){
            for(Token comp : delimiters){
                if(comp.equals(token)){
                    return token;
                }
            }
        }
        return null;
    }

    /**
     * Search through the list of tokens until the next of provided type. Looks
     * for a token ahead of the currentToken. 
     * @return The token of the type
     */
    public Token nextTokenOf(Type type){
        Token token;
        
        while((token = nextToken()) != null){
            if(token.type == type){
                break;
            }
        }
        return token;
    }

    /**
     * Search through tokens until the next 'identifier'. Does not increment
     * the currentToken pointer.

     *
     * @return Index of the token
     */
    public int nextIndexOf(String identifier){
        Token token;
        int tempCurrentToken = currentToken, result;
        
        nextTokenOf(identifier);
        result = currentToken;
        currentToken = tempCurrentToken;
        return result;

    }
    
    /**
     * Search through tokens until the next token similar to one of the
     * delimiters. Does not increment
     * the currentToken pointer.

     *
     * @return Index of the token
     */
    public int nextIndexOf(List<Token> delimiters){
        Token token;
        int tempCurrentToken = currentToken, result;

        nextTokenOf(delimiters);
        result = currentToken;
        currentToken = tempCurrentToken;
        return result;
    }

    /**
     * Search through tokens until the next token of 'type'. Does not increment
     * the currentToken pointer.
     *
     * @return Index of the token
     */
    public int nextIndexOf(Type type){
        Token token;
        int tempCurrentToken = currentToken, result;
        
        nextTokenOf(type);
        result = currentToken;
        currentToken = tempCurrentToken;
        return result;
    }

    /**
     * Lists all tokens from (but not including) currentToken up to (but not including) an identifier. Does not
     * increment the currentToken pointer.
     */
    public ArrayList<Token> listTokensTo(String identifier){
        ArrayList<Token> result = new ArrayList<Token>();
        Token token;
        int tempCurrentToken = currentToken;
        while((token = nextToken()) != null){
            if(token.identifier.equals(identifier)){
                break;
            }
            result.add(token);
        }
        currentToken = tempCurrentToken;
        return result;

    }

    /**
     * Increments the currentToken pointer to a specific token. Following
     * nextToken call will retrieve the token following the token you provided.
     */
    public void moveTo(Token token){
        int index = currentToken;
        Token current;
        while((current = nextToken()) != null){
            if(current.identifier.equals(token.identifier)){
                currentToken = index+1;
                return;
            }
            ++index;
        }
    }

    public void moveTo(int index){
        if(index < -1) throw new ArrayIndexOutOfBoundsException();
        currentToken = index;
    }

    /**
     * Get token by id.
     */
    public Token getToken(int index){
        return tokens.get(index);
    }

    public void tokenize(String input){
        char[] buffer = scan(input);
        int tmpIndex;
        for(int i=0; i<buffer.length; ++i){
            if(buffer[i] == '\0')
                break;

            Token tmpToken = new Token();

            tokens.add(tmpToken);
            tmpIndex = parseToken(tmpToken, buffer, i, alphas);
            if(tmpIndex != i) {
                i = tmpIndex-1;
                continue;
            }
            else i = tmpIndex;

            tmpIndex = parseToken(tmpToken, buffer, i, numbers);
            if(tmpIndex != i) {
                i = tmpIndex-1;
                continue;
            }
            else i = tmpIndex;

            tmpToken.type = Type.SYMBOL;
            tmpToken.identifier = Character.toString(buffer[i]);
        }
    }

    private int parseToken(Token token, char[] buffer, int i, int type){
        for(int x=0; x < characters[type].length; ++x){
            if(buffer[i] == characters[type][x]){
                token.identifier = "";
                switch(type){
                    case 0:
                        token.type = Type.ALPHA;
                        break;
                    case 1:
                        token.type = Type.NUM;
                        break;
                }
                i = parseWord(token, buffer, i, type);
                return i;
            }
        }
        return i;
    }

    /**
     * Parse a word of characters, belonging to 'type'.
     * 
     * @param token The token to write to
     * @param buffer List of characters the function should work over
     * @param offset Where in the list to start looking 
     * @param type Type of characters to find a word of: alphas, numbers
     * @return Index of the letter after the parsed word
     */
    private int parseWord(Token token, char[] buffer, int offset, int type){
fromStart:
        for(int i=offset; i<buffer.length; ++i){
            for(int x=0; x < characters[type].length; ++x){
                if(buffer[i] == characters[type][x]){
                    token.identifier += buffer[i];
                    continue fromStart;
                }
            }
            // If it gets here, the character currently parsed isn't of the same type 
            return i;
        }
        System.out.println("Fatal Error: Something horrible has happened!\nExiting with token: "+token+" and was looking for type: "+type);
        System.exit(1);
        return 0;
    }

    private char[] scan(String input){
        char[] text = input.toCharArray();
        char[] buffer = new char[text.length];
        boolean scannedSpace = false;;
        // Scanning the site and removing all newlines, and reducing all spaces
        // to one.
        for(int i=0, x=0; i<text.length; ++i){
            switch(text[i]){
                case '\n':
                case '\r':
                case '\t':
                case ' ':
                    if(!scannedSpace){
                        scannedSpace = true;
                        buffer[x] = ' ';
                        ++x;
                    }
                    break;
                default:
                    scannedSpace = false;
                    buffer[x] = text[i];
                    ++x;
                    break;
            }
        }
        return buffer;
    }

    private static final int alphas=0, numbers=1; 
    private static final char[][] characters =  { 
        {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
            'U', 'V', 'W', 'X', 'Y', 'Z'},
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}
    };
}

enum Type{
    ALPHA, NUM, SYMBOL
}

class Token{
    public String identifier;
    public Type type;

    public Token(){}
    public Token(String identifier, Type type){
        this.identifier = identifier;
        this.type = type;
    }

    public boolean equals(Object other){
        if(other instanceof Token){
            Token token = (Token)other;

            if(this.identifier == null || token.identifier == null){
                if(this.type == null || token.type == null)
                    return false; // Incomparable, no common elements
                if(this.type == token.type)
                    return true; // Types are the same, only valid if either of them has no identifier
            }
            else
                if(this.identifier.equals(token.identifier))
                    return true;

        }
        return false;
    }

    public String toString(){
        return identifier;
    }
}
