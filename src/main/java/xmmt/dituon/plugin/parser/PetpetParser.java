package xmmt.dituon.plugin.parser;

import lombok.Getter;
import xmmt.dituon.plugin.parser.hundun.Parser;

import java.util.List;
import java.util.Map;

public class PetpetParser extends Parser {

    @Getter
    PetpetParserConfig config;

    public PetpetParser(PetpetParserConfig config, Map<String, List<String>> namesMap) {
        this.config = config;

        registerMainCommand(config.getMainCommand());

        namesMap.forEach((standardName, names) -> {
            registerSubCommand(standardName, names);
        });

        registerSyntaxs(
                tokens -> new PetpetDrawStatement(tokens),
                PetpetDrawStatement.DEFAULT_SYNTAX_LIST,
                PetpetDrawStatement.class
        );

        registerSyntaxs(
                tokens -> new PetpetSpecialStatement(tokens),
                PetpetSpecialStatement.SPECIAL_SYNTAX_LIST,
                PetpetSpecialStatement.class
        );
    }
}
