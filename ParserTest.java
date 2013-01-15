import org.junit.*;
import org.junit.Test;
import junit.framework.*;
import static org.junit.Assert.assertEquals;

public class ParserTest extends TestCase{
    Tokenizer tokenizer;
    Tag top;
    ParseHelper parser;
    @Before
        public void setUp(){
            tokenizer = new Tokenizer();
            top = new Tag();
            parser = ParseHelper.init(tokenizer);
        }

    @After
        public void tearDown(){
            top = null;
            parser.uninit();
            parser = null;
            tokenizer.clear();
            tokenizer = null;
        }

    @Test
        public void testParseStringSurroundedString(){
            tokenizer.tokenize(" = \"good\" other things");
            StringBuffer result = new StringBuffer();
            int index = parser.parseString(result, 0);
            assertEquals("good", result.toString());
            assertEquals(6, index);
        }

    @Test
        public void testParseStringSingleQuoted(){
            tokenizer.tokenize(" = \'good\' other things");
            StringBuffer result = new StringBuffer();
            int index = parser.parseString(result, 0);
            assertEquals("good", result.toString());
            assertEquals(6, index);
        }

    @Test
        public void testParseStringSpaced(){
            tokenizer.tokenize(" = good other things");
            StringBuffer result = new StringBuffer();
            int index = parser.parseString(result, 0);
            assertEquals("good", result.toString());
            assertEquals(4, index);
        }

    @Test
        public void testParseStringSpacedNumber(){
            tokenizer.tokenize(" = 123 other things");
            StringBuffer result = new StringBuffer();
            int index = parser.parseString(result, 0);
            assertEquals("123", result.toString());
            assertEquals(4, index);
        }

    @Test
        public void testParseStringMultibleWords(){
            tokenizer.tokenize(" = \"good other\" things");
            StringBuffer result = new StringBuffer();
            int index = parser.parseString(result, 0);
            assertEquals("good other", result.toString());
            assertEquals(8, index);
        }

    @Test
        public void testParseArgCorrect(){
            tokenizer.tokenize("arg = 'value' ");
            int result = parser.parseArg(top, 0);
            assertEquals(7, result);
            assertTrue(top.args.containsKey("arg"));
            assertEquals("value", top.args.get("arg"));
        }

    @Test
        public void testParseArgNoSpaces(){
            tokenizer.tokenize("arg='value' ");
            int result = parser.parseArg(top, 0);
            assertEquals(5, result);
            assertTrue(top.args.containsKey("arg"));
            assertEquals("value", top.args.get("arg"));
        }

    @Test
        public void testParseArgEndOfTag(){
            tokenizer.tokenize("<tag arg=value> ");
            int result = parser.parseArg(top, 2);
            assertEquals(6, result);
            assertTrue(top.args.containsKey("arg"));
            assertEquals("value", top.args.get("arg"));
        }

    @Test
        public void testParseArgHtmlString(){
            tokenizer.tokenize("<a href=http://google.com> ");
            int result = parser.parseArg(top, 2);
            assertEquals(12, result);
            assertTrue(top.args.containsKey("href"));
            assertEquals("http://google.com", top.args.get("href"));
        }

    @Test
        public void testParseArgHtmlSlashed(){
            tokenizer.tokenize("<a href=http://google.com/ > ");
            int result = parser.parseArg(top, 2);
            assertEquals(13, result);
            assertTrue(top.args.containsKey("href"));
            assertEquals("http://google.com/", top.args.get("href"));
        }

    @Test
        public void testParseArgNamespaced(){
            tokenizer.tokenize("<a xpl:href=\"http://google.com/\"> ");
            int result = parser.parseArg(top, 2);
            assertEquals(17, result);
            assertTrue(top.args.containsKey("xpl:href"));
            assertEquals("http://google.com/", top.args.get("xpl:href"));
        }

    @Test
        public void testParseArgHtmlMultipleSlashed(){
            tokenizer.tokenize("<a href=http://google.com/ target=_blank/> ");
            int result = parser.parseArg(top, 2);
            assertEquals(13, result);
            assertTrue(top.args.containsKey("href"));
            assertEquals("http://google.com/", top.args.get("href"));

            result = parser.parseArg(top, result);
            assertTrue(top.args.containsKey("href"));
            assertEquals("http://google.com/", top.args.get("href"));

            assertEquals(18, result);
            assertTrue(top.args.containsKey("target"));
            assertEquals("blank", top.args.get("target")); // Can't handle symbols as first character
        }

    @Test
        public void testParseStartTagCorrect(){
            tokenizer.tokenize("<a href=http://google.com/ > ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(15, result);
            assertEquals("a", top.name);

            assertTrue(top.args.containsKey("href"));
            assertEquals("http://google.com/", top.args.get("href"));
        }

    @Test
        public void testParseStartTagNumbered(){
            tokenizer.tokenize("<h1> ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(4, result);
            assertEquals("h1", top.name);
        }

    @Test
        public void testParseStartTagNumberedBackwards(){
            tokenizer.tokenize("<2h> ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(0, result);
            assertEquals(null, top.name);
        }

    @Test
        public void testParseStartTagNamespacedNumbered(){
            tokenizer.tokenize("<xpl:h1> ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(6, result);
            assertEquals("xpl:h1", top.name);
        }

    @Test
        public void testParseStartTagNamespaced(){
            tokenizer.tokenize("<xpl:a href='http://google.com/'> ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(18, result);
            assertEquals("xpl:a", top.name);
        }

    @Test
        public void testParseStartTagSlashed(){
            tokenizer.tokenize("<a href='http://google.com/'/> ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(0, result);
            assertEquals(null, top.name);
        }

    @Test
        public void testParseStartTagShort(){
            tokenizer.tokenize("<a> ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(3, result);
            assertEquals("a", top.name);
        }

    @Test
        public void testParseStartTagEndTag(){
            tokenizer.tokenize("</a> ");
            int result = parser.parseStartTag(top, 0);

            assertEquals(0, result);
            assertEquals(null, top.name);
        }

    @Test
        public void testParseStartTagFake(){
            tokenizer.tokenize("<a och det har varit bra.<p>");
            int result = parser.parseStartTag(top, 0);

            assertEquals(0, result);
            assertEquals(null, top.name);

            assertFalse(top.args.containsKey("och"));
        }

    @Test
        public void testParseStartTagFakePrefaced(){
            tokenizer.tokenize("b<a och det har varit bra.<p>");
            int result = parser.parseStartTag(top, 0);

            assertEquals(0, result);
            assertEquals(null, top.name);

            assertFalse(top.args.containsKey("och"));
        }

    @Test
        public void testParseEndTagShort(){
            tokenizer.tokenize("</a> ");
            int result = parser.parseEndTag("a", 0);

            assertEquals(4, result);
        }

    @Test
        public void testParseEndTagShortNamespacedNumbered(){
            tokenizer.tokenize("</xpl:h1> ");
            int result = parser.parseEndTag("xpl:h1", 0);

            assertEquals(7, result);
        }

    @Test
        public void testParseEndTagWrong(){
            tokenizer.tokenize("</ai> ");
            int result = parser.parseEndTag("a", 0);

            assertEquals(0, result);
        }

    @Test
        public void testParseEndTagWrongButFollowed(){
            tokenizer.tokenize("</ai></a> ");
            int result = parser.parseEndTag("a", 0);

            assertEquals(0, result);
        }

    @Test
        public void testParseCommentTag(){
            tokenizer.tokenize("<!-- </ai> this is a comment --></a> ");
            int result = parser.parseCommentTag(top, 0);

            assertEquals(21, result);
        }

    @Test
        public void testParseCommentTagTricked(){
            tokenizer.tokenize("<!-- </ai> this -- is a comment --></a> ");
            int result = parser.parseCommentTag(top, 0);

            assertEquals(24, result);
        }

    @Test
        public void testParseEmptyTag(){
            tokenizer.tokenize("<br/> ");
            int result = parser.parseEmptyTag(top, 0);

            assertEquals(4, result);
            assertEquals("br", top.name);
        }

    @Test
        public void testParseEmptyTagSpaced(){
            tokenizer.tokenize("<br /> ");
            int result = parser.parseEmptyTag(top, 0);

            assertEquals(5, result);
            assertEquals("br", top.name);
        }

    @Test
        public void testParseEmptyTagSpaced2(){
            tokenizer.tokenize("<br/ > ");
            int result = parser.parseEmptyTag(top, 0);

            assertEquals(5, result);
            assertEquals("br", top.name);
        }

    @Test
        public void testParseEmptyTagArgs(){
            tokenizer.tokenize("<link url=\"http://google.com/\"/> ");
            int result = parser.parseEmptyTag(top, 0);

            assertEquals(17, result);
            assertEquals("link", top.name);
            
            assertTrue(top.args.containsKey("url"));
            assertEquals("http://google.com/", top.args.get("url"));
        }

    @Test
        public void testParseContentTag(){
            tokenizer.tokenize("<p>Good news, everyone!</p> ");
            int result = parser.parseContentTag(top, 0);

            assertEquals(14, result);
            assertEquals("p", top.name);
            assertEquals("Good news, everyone!", top.content.toString());
        }

    @Test
        public void testParseContentTagInlineEmptyTag(){
            tokenizer.tokenize("<p>Good news,<br />everyone!</p> ");
            int result = parser.parseContentTag(top, 0);

            assertEquals(18, result);
            assertEquals("p", top.name);
            assertEquals("Good news,%0 everyone!", top.content.toString());
            assertEquals("br", top.children.get(0).name);
        }

    @Test
        public void testParseContentTagInlineSameContentTag(){
            tokenizer.tokenize("<p>Good news,<p>everyone!</p>We're winning!</p> ");
            int result = parser.parseContentTag(top, 0);

            assertEquals(26, result);
            assertEquals("p", top.name);
            assertEquals("Good news,%0 We're winning!", top.content.toString());
            assertEquals("p", top.children.get(0).name);
            assertEquals("everyone!", top.children.get(0).content.toString());
        }

    @Test
        public void testParseCDataTag(){
            tokenizer.tokenize("<![CDATA[Stuff is <p>Good news,<p>everyone!</p>We're winning!</p>awesome!]]> ");
            int result = parser.parseCDataTag(top, 0);

            assertEquals(40, result);
            assertEquals("CDATA", top.name);
            assertEquals("Stuff is %0 awesome!", top.content.toString());
            assertEquals("p", top.children.get(0).name);
            assertEquals("Good news,%0 We're winning!", top.children.get(0).content.toString());
        }

    @Test
        public void testParseCDataTagFakeTag(){
            tokenizer.tokenize("<![CDATA[a<b <p>Good news,<p>everyone!</p>We're winning!</p>awesome!]]> ");
            int result = parser.parseCDataTag(top, 0);

            assertEquals(40, result);
            assertEquals("CDATA", top.name);
            assertEquals("a<b %0 awesome!", top.content.toString());
            assertEquals("p", top.children.get(0).name);
            assertEquals("Good news,%0 We're winning!", top.children.get(0).content.toString());
        }
}
