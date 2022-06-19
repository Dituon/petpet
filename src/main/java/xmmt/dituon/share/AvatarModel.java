package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class AvatarModel {
    protected AvatarType type;
    protected int[][] pos = {{0, 0, 100, 100}};
    protected int angle;
    protected boolean round;
    protected boolean rotate;
    protected boolean onTop;
    protected BufferedImage image = null;
    private int posIndex = 0;
    private boolean antialias = false;

    public AvatarModel(AvatarData data, AvatarExtraData extraData, Type imageType) {
        type = data.getType();
        setImage(type, extraData);
        setPos(data.getPos(), imageType);
        angle = data.getAngle() != null ? data.getAngle() : 0;
        round = Boolean.TRUE.equals(data.getRound());
        rotate = Boolean.TRUE.equals(data.getRotate());
        onTop = Boolean.TRUE.equals(data.getAvatarOnTop());
        antialias = Boolean.TRUE.equals(data.getAntialias());
    }

    private void setImage(AvatarType type, AvatarExtraData extraData) {
        switch (type) {
            case FROM:
                image = extraData.getFromAvatar();
                break;
            case TO:
                image = extraData.getToAvatar();
                break;
            case GROUP:
                image = extraData.getGroupAvatar();
                break;
            case BOT:
                image = extraData.getBotAvatar();
                break;
        }
    }

    private void setPos(JsonArray posElements, Type imageType) {
        int i = 0;
        switch (imageType) {
            case GIF:
                pos = new int[posElements.size()][4];
                for (JsonElement je : posElements) {
                    pos[i++] = JsonArrayToIntArray((JsonArray) je);
                }
                break;
            case IMG:
                pos[i] = JsonArrayToIntArray(posElements);
                break;
        }
    }

    private int[] JsonArrayToIntArray(JsonArray ja) {
        return new int[]{
                Integer.parseInt(ja.get(0).toString()),
                Integer.parseInt(ja.get(1).toString()),
                Integer.parseInt(ja.get(2).toString()),
                Integer.parseInt(ja.get(3).toString())
        };
    }

    public AvatarType getType() {
        return type;
    }

    public int[][] getPos() {
        return pos;
    }

    public int getAngle() {
        return angle;
    }

    public boolean isRound() {
        return round;
    }

    public boolean isRotate() {
        return rotate;
    }

    public boolean isOnTop() {
        return onTop;
    }

    public BufferedImage getImage() {
        assert image != null;
        if (round) {
            try {
                return ImageSynthesis.convertCircular(image, antialias);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    public int getRotateIndex() {
        if (!rotate) {
            return 0;
        }
        return posIndex;
    }

    public int[] nextPos() {
        if (posIndex > pos.length) {
            return new int[]{0, 0, 0, 0};
        }
        return pos[posIndex++];
    }
}
