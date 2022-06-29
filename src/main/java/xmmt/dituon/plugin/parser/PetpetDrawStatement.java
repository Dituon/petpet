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
public class PetpetDrawStatement extends Statement {

    public static final List<List<TokenType>> DEFAULT_SYNTAX_LIST = new ArrayList<>();
    static {
        // pet @114514
        DEFAULT_SYNTAX_LIST.add(Arrays.asList(
                TokenType.MAIN_COMMAND_NAME,
                TokenType.AT
        ));
        // key @114514
        DEFAULT_SYNTAX_LIST.add(Arrays.asList(
                TokenType.SUB_COMMAND_NAME,
                TokenType.AT
        ));
        // pet @114514 key (可选)additiontext
        DEFAULT_SYNTAX_LIST.add(Arrays.asList(
                TokenType.MAIN_COMMAND_NAME,
                TokenType.AT,
                TokenType.SUB_COMMAND_NAME
        ));
        DEFAULT_SYNTAX_LIST.add(Arrays.asList(
                TokenType.MAIN_COMMAND_NAME,
                TokenType.AT,
                TokenType.SUB_COMMAND_NAME,
                TokenType.LITERAL_VALUE
        ));
        // pet key [image]
        DEFAULT_SYNTAX_LIST.add(Arrays.asList(
                TokenType.MAIN_COMMAND_NAME,
                TokenType.SUB_COMMAND_NAME,
                TokenType.IMAGE
        ));
        // pet [image] key
        DEFAULT_SYNTAX_LIST.add(Arrays.asList(
                TokenType.MAIN_COMMAND_NAME,
                TokenType.IMAGE,
                TokenType.SUB_COMMAND_NAME
        ));
    }

    /**
     * petept模板id
     */
    String templateId;
    @Nullable
    At at;
    @Nullable
    Image image;
    /**
     * 用于替换填充模板的文本
     */
    List<String> additionTexts = new ArrayList<>();

    public PetpetDrawStatement(List<Token> tokens) {

        for (Token token : tokens) {
            switch (token.getType()) {
                case SUB_COMMAND_NAME:
                    this.templateId = Objects.requireNonNull(token.getExtraTextContent());
                    break;
                case LITERAL_VALUE:
                    this.additionTexts.add(Objects.requireNonNull(token.getTextContent()));
                    break;
                case AT:
                    this.at = Objects.requireNonNull(token.getAtContent());
                    break;
                case IMAGE:
                    this.image = Objects.requireNonNull(token.getImageContent());
                    break;
                default:
                    break;
            }
        }
    }

}
