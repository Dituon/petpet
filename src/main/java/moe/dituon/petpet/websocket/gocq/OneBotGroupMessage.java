package moe.dituon.petpet.websocket.gocq;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonPrimitive;
import moe.dituon.petpet.share.BasePetService;
import moe.dituon.petpet.share.TextExtraData;

import java.util.*;
import java.util.stream.Collectors;

public class OneBotGroupMessage {
    GoCQAPIWebSocketClient apiClient = GoCQPetpet.getInstance().apiClient;
    GoCQPetService service = GoCQPetpet.getInstance().service;
    private static Set<String> keyAliaSet = null;

    OneBotGroupMessage(GoCQGroupMessageEventDTO e){
        JsonArray messageChar = e.getMessage();
        StringBuilder messageText = new StringBuilder();

        boolean hasImage = false;
        boolean fuzzyLock = false; //锁住模糊匹配

        GoCQMemberDTO sender = e.getSender();
        String senderName = sender.getName();

        String fromName = null,
                fromUrl = null,
                toName = senderName,
                toUrl = getAvatarUrl(sender.getUser_id()),
                groupName = "你群";

        var imageCachePool = GoCQPetpet.getInstance().imageCachePool;

        long id = e.getGroup_id() + e.getMessage_id();
        for (JsonElement ele : messageChar){
            JsonObject obj = (JsonObject) ele;
            String type = ((JsonPrimitive) Objects.requireNonNull(obj.get("type"))).getContent();
            switch (type){
                case "image":
                    if (hasImage){
                        fromUrl = getDataKey(obj, "url");
                        break;
                    }
                    fuzzyLock = true;
                    hasImage = true;
                    fromName = senderName;
                    fromUrl = getAvatarUrl(sender.getUser_id());
                    toUrl = getDataKey(obj, "url");
                    toName = "这个";

                    //记录图片缓存
                    if (imageCachePool.get(id) == null){
                        imageCachePool.put(id, toUrl);
                    }else{
                        imageCachePool.replace(id, toUrl);
                    }
                    break;
                case "text":
                    messageText.append(getDataKey(obj, "text")).append(' ');
                    break;
                case "at":
                    fuzzyLock = true;
                    long memberId = Long.parseLong(getDataKey(obj, "qq"));
                    var toMember = GoCQPetpet.getInstance().apiClient.requester.getGroupMember(e.getGroup_id(), memberId);
                    fromUrl = getAvatarUrl(sender.getUser_id());
                    fromName = senderName;
//                    PluginPetService.LOGGER.info(toMember.getUser_id() + "");
                    toUrl = getAvatarUrl(toMember.getUser_id());
                    toName = toMember.getName();
                    break;
            }
        }

        for (JsonElement ele : messageChar){
            JsonObject obj = (JsonObject) ele;
            String type = ((JsonPrimitive) Objects.requireNonNull(obj.get("type"))).getContent();
            switch (type){
                case "reply":
                    id = e.getGroup_id() + Integer.parseInt(getDataKey(obj, "id"));
                    if (imageCachePool.get(id) == null) break;
                    fuzzyLock = true;
                    fromUrl = getAvatarUrl(sender.getUser_id());
                    fromName = senderName;
                    toUrl = imageCachePool.get(id);
                    toName = "这个";
                    break;
            }
        }

        String commandData = messageText.toString().trim();

//        if (service.command.equals(commandData)) {
//            // TODO
//            service.sendMessage("Petpet KeyList: \n" + service.getKeyAliasListString());
//            return;
//        }

        ArrayList<String> spanList = new ArrayList<>(Arrays.asList(commandData.split("\\s+")));
        if (spanList.isEmpty()) return;

        String key = null;
        if (service.command.equals(spanList.get(0))) {
            spanList.remove(0); //去掉指令头
            key = service.randomableList.get(BasePetService.random.nextInt(service.randomableList.size())); //随机key
        }
//        PluginPetService.LOGGER.info(key);

        if (!spanList.isEmpty() && !service.strictCommand) { //匹配非标准格式指令
            if (keyAliaSet == null) { //按需初始化
                keyAliaSet = new HashSet<>(service.getDataMap().keySet());
                keyAliaSet.addAll(service.getAliaMap().keySet());
                keyAliaSet = keyAliaSet.stream()
                        .map(str -> str = service.commandHead + str)
                        .collect(Collectors.toSet());
            }
            for (String k : keyAliaSet) {
                if (!spanList.get(0).startsWith(k)) break;
                String span = spanList.set(0, k);
                if (span.length() != k.length()) {
                    spanList.add(1, span.substring(k.length()));
                }
            }
        }

        if (!spanList.isEmpty()) {
            String firstSpan = spanList.get(0);
            if (firstSpan.startsWith(service.commandHead)) {
                spanList.set(0, firstSpan = firstSpan.substring(service.commandHead.length()));
            } else {
                return;
            }

            if (service.getDataMap().containsKey(firstSpan)) { //key
                key = spanList.remove(0);
            } else if (service.getAliaMap().containsKey(firstSpan)) { //别名
                String[] keys = service.getAliaMap().get(spanList.remove(0));
                key = keys[BasePetService.random.nextInt(keys.length)];
            }
        }

        if (key == null) return;

//        if (Cooler.isLocked(e.getSender().getUser_id()) || Cooler.isLocked(e.getGroup_id())) {
////            if (service.inCoolDownMessage == null) return;
////            sendReplyMessage(e, service.inCoolDownMessage);
//            //TODO
//            return;
//        }
//        Cooler.lock(e.getSender().getUser_id(), service.coolDown);
//        Cooler.lock(e.getGroup_id(), service.groupCoolDown);

        if (fromName == null) fromName = "bot";
        if (fromUrl == null) fromUrl = getAvatarUrl(e.getSelf_id());

        service.sendImage(
                e.getGroup_id(),
                key,
                new GoCQPetService.AvatarUrls(fromUrl, toUrl, "", ""),
                new TextExtraData(fromName, toName, groupName, spanList)
        );
    }

    private static String getDataKey(JsonObject obj, String key) {
        JsonObject dataObj = (JsonObject) obj.get("data");
        assert dataObj != null;
        return ((JsonPrimitive) Objects.requireNonNull(dataObj.get(key))).getContent();
    }

    private static String getAvatarUrl(long qq){
        return "http://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640";
    }
}
