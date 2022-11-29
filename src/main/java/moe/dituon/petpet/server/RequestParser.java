package moe.dituon.petpet.server;

import kotlin.Pair;
import moe.dituon.petpet.share.BaseConfigFactory;
import moe.dituon.petpet.share.TextExtraData;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

abstract class RequestParser {
    Pair<InputStream, String> imagePair;

    public Pair<InputStream, String> getImagePair() {
        return imagePair;
    }

    public void close() {
        imagePair = null;
    }
}

class GETParser extends RequestParser {
    HashMap<String, String> parameterList = new HashMap<>();
    public GETParser(String param) {
        System.out.println("DEBUG[GET URL]: " + param);
        String[] queryList = param.split("&");
        for (String query : queryList) {
            String[] parameter = query.split("=");
            parameterList.put(parameter[0], URLDecoder.decode(parameter[1], StandardCharsets.UTF_8));
        }
        parser();
    }

    private void parser() {
        List<String> textList = get("textList") != null ?
                Arrays.asList(get("textList").split("\\s+")) : new ArrayList<>();

        String randomAvatarListStr = get("randomAvatarList");

        super.imagePair = WebServer.petService.generateImage(
                get("key"),
                BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                        get("fromAvatar"), get("toAvatar"), get("groupAvatar"), get("botAvatar"),
                        get("randomAvatarList") != null ? List.of(randomAvatarListStr.split(",")) : null
                ), new TextExtraData(
                        get("fromName") != null ? get("fromName") : "from",
                        get("toName") != null ? get("toName") : "to",
                        get("groupName") != null ? get("groupName") : "group",
                        textList
                ), null
        );
    }

    private String get(String key) {
        return parameterList.get(key);
    }

    @Override
    public void close(){
        super.imagePair = null;
        parameterList = null;
    }
}

class POSTParser extends RequestParser{
    public POSTParser(String postBody){
        System.out.println("DEBUG[POST BODY]: " + postBody);
        RequestDTO request = RequestDTO.decodeFromString(postBody);
        super.imagePair = WebServer.petService.generateImage(
                request.getKey(),
                BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                        request.getForm().getAvatar(),
                        request.getTo().getAvatar(),
                        request.getGroup().getAvatar(),
                        request.getBot().getAvatar(),
                        request.getRandomAvatarList()
                ), new TextExtraData(
                        request.getForm().getName(),
                        request.getTo().getName(),
                        request.getGroup().getName(),
                        request.getTextList()
                ), null
        );
    }
}
