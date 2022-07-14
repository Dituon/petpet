package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

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
    private DeformData deformData = null;

    public AvatarModel(AvatarData data, AvatarExtraDataProvider extraData, Type imageType) {
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

    private void setImage(AvatarType type, AvatarExtraDataProvider extraData) {
        switch (type) {
            case FROM:
                image = Objects.requireNonNull(extraData.getFromAvatar()).invoke();
                break;
            case TO:
                image = Objects.requireNonNull(extraData.getToAvatar()).invoke();
                break;
            case GROUP:
                image = Objects.requireNonNull(extraData.getGroupAvatar()).invoke();
                break;
            case BOT:
                image = Objects.requireNonNull(extraData.getBotAvatar()).invoke();
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
                deformData = DeformData.fromPos(posElements);
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

    public boolean isRound() {
        return round;
    }

    public boolean isRotate() {
        return rotate;
    }

    public boolean isOnTop() {
        return onTop;
    }

    public boolean isAntialias() {
        return antialias;
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

    public float getNextAngle() {
        if (!rotate) {
            return angle;
        }
        return ((float) (360 / pos.length) * posIndex) + angle;
    }

    public int[] nextPos() {
        if (posIndex > pos.length) {
            return new int[]{0, 0, 0, 0};
        }
        return pos[posIndex++];
    }

    public PosType getPosType() {
        return posType;
    }

    public DeformData getDeformData() {
        return deformData;
    }

    public static class DeformData {
        static final int POS_SIZE = 4;
        Point2D[] deformPos = new Point2D[POS_SIZE];
        int[] anchor = new int[2];

        public static DeformData fromPos(JsonArray posElements) {
            System.out.println("DeformData fromPos by: " + posElements.toString());
            DeformData deformData = new DeformData();
            for (short i = 0; i < POS_SIZE; i++) {
                deformData.deformPos[i] = new Point2D.Double(
                        Integer.parseInt(((JsonArray) posElements.get(i)).get(0).toString()),
                        Integer.parseInt(((JsonArray) posElements.get(i)).get(1).toString())
                );
            }
            deformData.anchor[0] = Integer.parseInt(((JsonArray) posElements.get(POS_SIZE)).get(0).toString());
            deformData.anchor[1] = Integer.parseInt(((JsonArray) posElements.get(POS_SIZE)).get(1).toString());

            return deformData;
        }

        public Point2D[] getDeformPos() {
            return deformPos;
        }

        public int[] getAnchor() {
            return anchor;
        }
    }

}