package xmmt.dituon.plugin.parser;

import lombok.Getter;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import org.jetbrains.annotations.Nullable;
import xmmt.dituon.plugin.parser.hundun.Token;
import xmmt.dituon.plugin.parser.hundun.TokenType;
import xmmt.dituon.plugin.parser.hundun.statement.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class PetpetSpecialStatement extends Statement {


    public static final List<List<TokenType>> SPECIAL_SYNTAX_LIST = new ArrayList<>();

    static {
        // pet
        SPECIAL_SYNTAX_LIST.add(Arrays.asList(
                TokenType.MAIN_COMMAND_NAME
        ));
        // pet on 或者 pet fooText
        SPECIAL_SYNTAX_LIST.add(Arrays.asList(
                TokenType.MAIN_COMMAND_NAME,
                TokenType.LITERAL_VALUE
        ));
    }

    public enum SubType {
        ON,
        OFF,
        LIST_KEY,
        NONE
    }

    SubType subType = SubType.NONE;


    public PetpetSpecialStatement(List<Token> tokens) {

        if (tokens.size() == 1) {
            this.subType = SubType.LIST_KEY;
        } else if (tokens.size() == 2) {
            String text = tokens.get(0).getTextContent();
            if (text.equals("on")) {
                this.subType = SubType.ON;
            } else if (text.equals("off")) {
                this.subType = SubType.OFF;
            }
        }
    }

}
