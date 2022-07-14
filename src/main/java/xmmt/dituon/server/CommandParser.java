package xmmt.dituon.server;

import kotlin.Pair;
import xmmt.dituon.share.BaseConfigFactory;
import xmmt.dituon.share.TextExtraData;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandParser {
    private HashMap<String, String> parameterList = new HashMap<>();
    private Pair<InputStream, String> imagePair;

    public CommandParser(String command) {
        String[] queryList = command.split("&");
        for (String query : queryList) {
            String[] parameter = query.split("=");
            parameterList.put(parameter[0], parameter[1]);
        }
        parser();
    }

    private void parser() {
        List<String> textList = get("textList") != null ?
                Arrays.asList(get("textList").split("\\s+")) : new ArrayList<>();

        imagePair = WebServer.petService.generateImage(
                get("key"),
                BaseConfigFactory.getAvatarExtraDataFromUrls(
                        get("fromAvatar"), get("toAvatar"), get("groupAvatar"), get("botAvatar")
                ), new TextExtraData(
                        get("fromName") != null ? get("fromName") : "from",
                        get("toName") != null ? get("toName") : "to",
                        get("groupName") != null ? get("groupName") : "group",
                        textList
                ), null
        );
    }

    public String get(String key) {
        System.out.println("DEBUG: input: " + key + '=' + parameterList.get(key));
        return parameterList.get(key);
    }

    public Pair<InputStream, String> getImagePair() {
        return imagePair;
    }

    public void close() {
        parameterList = null;
        imagePair = null;
    }
}
