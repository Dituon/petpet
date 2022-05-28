package xmmt.dituon;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import xmmt.dituon.share.*;

import java.util.Objects;
import java.util.Random;

import static xmmt.dituon.GifMaker.makeGIF;
import static xmmt.dituon.ImageMaker.makeImage;

public class PetData extends BasePetData {

    public static void sendImage(Group group, Member from, Member to) {
        sendImage(group, from, to, BasePetData.keyList.get(new Random().nextInt(keyList.size())));
    }

    public static void sendImage(Group group, Member from, Member to, boolean random) {
        if (!random) {
            sendImage(group, from, to);
            return;
        }
        int r = new Random().nextInt(randomMax);
        if (r >= keyList.size()) {
            return;
        }
        sendImage(group, from, to, keyList.get(r));
    }

    public static void sendImage(Group group, Member from, Member to, String key) {
        if (!dataMap.containsKey(key)) {
            System.out.println("无效的key: " + key);
            sendImage(group, from, to);
        }
        DataJSON data = dataMap.get(key);
        key = dataRoot.getAbsolutePath() + key + "/";

        try {
            if (data.getType() == Type.GIF) {
                if (data.getAvatar() == Avatar.SINGLE) {
                    int[][] pos = new int[data.getPos().getSize()][4];

                    int i = 0;
                    for (JsonElement je : data.getPos()) {
                        pos[i++] = JsonArrayToIntArray((JsonArray) je);
                    }

                    group.sendMessage(Objects.requireNonNull(makeGIF(to, key, pos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound())));
                    return;
                }
                if (data.getAvatar() == Avatar.DOUBLE) {
                    JsonArray fromJa = (JsonArray) data.getPos().get(0);
                    JsonArray toJa = (JsonArray) data.getPos().get(1);

                    int[][] fromPos = new int[fromJa.getSize()][4];
                    int[][] toPos = new int[toJa.getSize()][4];

                    int i = 0;
                    for (JsonElement fromJe : fromJa) {
                        fromPos[i++] = JsonArrayToIntArray((JsonArray) fromJe);
                    }
                    i = 0;
                    for (JsonElement toJe : toJa) {
                        toPos[i++] = JsonArrayToIntArray((JsonArray) toJe);
                    }

                    group.sendMessage(Objects.requireNonNull(makeGIF(
                            from, to, key, fromPos, toPos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound())));
                    return;
                }
            }

            if (data.getType() == Type.IMG){
                if (data.getAvatar() == Avatar.SINGLE) {
                    int[] pos = JsonArrayToIntArray(data.getPos());

                    group.sendMessage(Objects.requireNonNull(makeImage(to, key, pos,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound())));
                    return;
                }
                if (data.getAvatar() == Avatar.DOUBLE) {
                    int[] pos1 = JsonArrayToIntArray((JsonArray) data.getPos().get(0));
                    int[] pos2 = JsonArrayToIntArray((JsonArray) data.getPos().get(1));

                    group.sendMessage(Objects.requireNonNull(makeImage(from, to, key, pos1, pos2,
                            data.getAvatarOnTop(), data.getRotate(), data.getRound())));
                    return;
                }
            }
        } catch (Exception ex) {
            System.out.println("解析 " + key + "/data.json 出错");
            ex.printStackTrace();
        }
    }

    private static int[] JsonArrayToIntArray(JsonArray ja) {
        return new int[]{
                Integer.parseInt(ja.get(0).toString()),
                Integer.parseInt(ja.get(1).toString()),
                Integer.parseInt(ja.get(2).toString()),
                Integer.parseInt(ja.get(3).toString())
        };
    }
}
