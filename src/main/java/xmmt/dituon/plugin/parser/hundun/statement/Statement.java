package xmmt.dituon.plugin.parser.hundun.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


import lombok.Data;
import xmmt.dituon.plugin.parser.hundun.Token;

/**
 * @author hundun
 * Created on 2021/04/27
 * @param 
 */
@Data
public abstract class Statement {
    protected List<Token> tokens;
    protected String originMiraiCode;
}
