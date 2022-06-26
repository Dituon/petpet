package xmmt.dituon.plugin.parser.hundun;

import lombok.Data;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;

/**
 * @author hundun
 * Created on 2021/04/27
 */
@Data
public class Token {
    private TokenType type;
    private String textContent;
    private String extraTextContent;
    private Image imageContent;
    private At atContent;

    public void changeToLiteralValue() {
        this.type = TokenType.LITERAL_VALUE;
        // textContent not change
        
        this.extraTextContent = null;
        this.imageContent = null;
        this.atContent = null;
    }
}
