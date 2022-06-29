package xmmt.dituon.plugin.parser

import kotlinx.serialization.Serializable
import xmmt.dituon.plugin.parser.hundun.TokenType

@Serializable
data class PetpetParserConfig (
    val mainCommand: String = "pet",
    val drawSyntaxList: List<List<TokenType>> = PetpetDrawStatement.DEFAULT_SYNTAX_LIST,
)