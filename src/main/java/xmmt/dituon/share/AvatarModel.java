package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import java.awt.geom.Point2D;
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
    private PosType posType = PosType.ZOOM;
    private Point2D[] point2D = null;

    public AvatarModel(AvatarData data, AvatarExtraData extraData, Type imageType) {
        type = data.getType();
        setImage(type, extraData);
        posType = data.getPosType() != null ? data.getPosType() : PosType.ZOOM;
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
        switch (posType) {
            case ZOOM:
                switch (imageType) {
                    case GIF:
                        pos = new int[posElements.size()][4];
                        for (JsonElement je : posElements) {
                            JsonArray ja = (JsonArray) je;
                            if (ja.size() != 4) {
                                return;
                            }
                            pos[i++] = JsonArrayToIntArray(ja);
                        }
                        break;
                    case IMG:
                        pos[i] = JsonArrayToIntArray(posElements);
                        break;
                }
                break;
            case DEFORM:
                System.out.println(posElements.toString());
                point2D = JsonArrayToPoint(posElements);
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

    private Point2D[] JsonArrayToPoint(JsonArray ja) {
        System.out.println(ja.get(0).toString());
        Point2D[] point2DList = new Point2D[4];
        for (short i = 0; i < 4; i++) {
            point2DList[i] = new Point2D.Double(
                    Integer.parseInt(((JsonArray) ja.get(i)).get(0).toString()),
                    Integer.parseInt(((JsonArray) ja.get(i)).get(1).toString())
            );
        }
        return point2DList;
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

    public Point2D[] getDeformPos() {
        return point2D;
    }

    public PosType getPosType() {
        return posType;
    }
}
