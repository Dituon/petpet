package xmmt.dituon.plugin.parser.hundun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;


/**
 * @author hundun
 * Created on 2021/04/27
 */
public class Tokenizer {
    

    //private String KEYWORD_WAKE_UP = "UNSETTED";
    
    private Map<String, TokenType> keywords = new HashMap<>();
    
    /**
     * subCommand aliasIdToStandardId
     */
    private Map<String, String> subCommandMap = new HashMap<>();
    
    
    public Tokenizer() {
    }


    public List<Token> simpleTokenize(Message message) {
        List<Token> result = new ArrayList<>();
        if (message instanceof At) {
            At atMessage = (At)message;
            Token token = new Token();
            token.setType(TokenType.AT);
            token.setAtContent(atMessage);
            result.add(token);
        } else if (message instanceof Image) {
            Image imageMessage = (Image)message;
            Token token = new Token();
            token.setType(TokenType.IMAGE);
            token.setImageContent(imageMessage);
            result.add(token);
        } else if (message instanceof PlainText) {
            PlainText plainTextMessage = (PlainText)message;
            String text = plainTextMessage != null ? plainTextMessage.contentToString() : null;
            if (text != null && text.trim().length() > 0) {
                List<String> subTexts = new ArrayList<>(Arrays.asList(text.split(" ")));
                subTexts = subTexts.stream().map(it -> it.trim()).filter(it -> !it.isEmpty()).collect(Collectors.toList());
//                // special rule: split WAKE_UP from start
//                if (subTexts.get(0).startsWith(KEYWORD_WAKE_UP) && subTexts.get(0).length() > KEYWORD_WAKE_UP.length()) {
//                    String autoSplit = subTexts.get(0).substring(KEYWORD_WAKE_UP.length());
//                    subTexts.set(0, KEYWORD_WAKE_UP);
//                    subTexts.add(1, autoSplit);
//                }
                
                for (String subText : subTexts) {
                    if (keywords.containsKey(subText)) {
                        Token token = new Token();
                        token.setType(keywords.get(subText));
                        token.setTextContent(subText);
                        result.add(token);
                    } else if (subCommandMap.containsKey(subText)) {
                        String standardId = subCommandMap.get(subText);
                        
                        Token token = new Token();
                        token.setType(TokenType.SUB_COMMAND_NAME);
                        token.setTextContent(subText);
                        token.setExtraTextContent(standardId);
                        result.add(token);
                    } else {
                        Token token = new Token();
                        token.setType(TokenType.LITERAL_VALUE);
                        token.setTextContent(subText);
                        result.add(token);
                    }
                }
            }
        }
        return result;
    }

    
    public void registerKeyword(String keyword, TokenType tokenType) throws Exception {
        if (keywords.containsKey(keyword)) {
            throw new Exception("已存在keyword = " + keywords.get(keyword));
        }
        this.keywords.put(keyword, tokenType);
//        if (tokenType == TokenType.MAIN_COMMAND_NAME) {
//            this.KEYWORD_WAKE_UP = keyword;
//        }
    }

    public void registerSubCommand(String subFunction, String customIdentifier) {
        subCommandMap.put(customIdentifier, subFunction);
    }


}
