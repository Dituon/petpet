package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AvatarModel {
    private final Type imageType;
    protected AvatarType type;
    protected int[][] pos = {{0, 0, 100, 100}};
    protected int angle;
    protected boolean round;
    protected boolean rotate;
    protected boolean onTop;
    protected BufferedImage image = null;
    private int posIndex = 0;
    private final boolean antialias;
    private final PosType posType;
    private DeformData deformData = null;
    private final CropType cropType;
    private int[] cropPos;
    private final List<Style> styleList;

    public AvatarModel(AvatarData data, AvatarExtraDataProvider extraData, Type imageType) {
        type = data.getType();
        setImage(type, extraData);
        posType = data.getPosType() != null ? data.getPosType() : PosType.ZOOM;
        setPos(data.getPos(), this.imageType = imageType);
        cropType = data.getCropType();
        setCrop(data.getCrop());
        styleList = data.getStyle();
        angle = data.getAngle() != null ? data.getAngle() : 0;
        round = Boolean.TRUE.equals(data.getRound());
        rotate = Boolean.TRUE.equals(data.getRotate());
        onTop = Boolean.TRUE.equals(data.getAvatarOnTop());
        antialias = Boolean.TRUE.equals(data.getAntialias());
        buildImage();
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

    private void setCrop(JsonArray crop) {
        if (crop == null || crop.isEmpty()) return;
        int[] result = JsonArrayToIntArray(crop);
        cropPos = result.length == 2 ? new int[]{0, 0, result[0], result[1]} : result;
    }

    private int[] JsonArrayToIntArray(JsonArray ja) {
        int[] result = new int[ja.size()];
        short i = 0;
        for (JsonElement je : ja) {
            String str = je.toString().replace("\"", "");
            try {
                result[i] = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                ArithmeticParser parser = new ArithmeticParser(str);
                parser.put("width", image.getWidth());
                parser.put("height", image.getHeight());
                result[i] = (int) parser.eval();
            }
            i++;
        }
        return result;
    }

    private void buildImage() {
        if (cropType != CropType.NONE) image = ImageSynthesis.cropImage(image, cropType, cropPos);

        for (Style style : styleList) {
            switch (style) {
                case FLIP:
                    image = ImageSynthesis.flipImage(image);
                    break;
                case MIRROR:
                    image = ImageSynthesis.mirrorImage(image);
                    break;
                case GRAY:
                    image = ImageSynthesis.grayImage(image);
                    break;
                case BINARIZATION:
                    image = ImageSynthesis.BinarizeImage(image);
                    break;
            }
        }

        if (round) {
            image = ImageSynthesis.convertCircular(image, antialias);
        }
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

    /**
     * 获取已经构建好的图像, 请放心食用
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * 获取旋转角度
     * <li>不旋转时 返回初始角度</li>
     * <li>IMG格式 返回随机角度</li>
     * <li>GIF 返回下一个旋转角度</li>
     */
    public float getNextAngle() {
        if (!rotate) return angle; //不旋转
        if (imageType == Type.IMG) return new Random().nextInt(angle != 0 ? angle : 360); //IMG随机旋转
        return ((float) (360 / pos.length) * posIndex) + angle; //GIF自动旋转
    }

    /**
     * 获取下一个坐标
     */
    public int[] nextPos() {
        if (posIndex >= pos.length) {
            return new int[]{0, 0, 0, 0};
        }
        return pos[posIndex++];
    }

    public PosType getPosType() {
        return posType;
    }

    /**
     * 获取坐标数组实际长度
     */
    public short getPosLength(){
        return (short) pos.length;
    }

    public DeformData getDeformData() {
        return deformData;
    }

    public static class DeformData {
        static final int POS_SIZE = 4;
        Point2D[] deformPos = new Point2D[POS_SIZE];
        int[] anchor = new int[2];

        public static DeformData fromPos(JsonArray posElements) {
//            System.out.println("DeformData fromPos by: " + posElements.toString());
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

    public int getImageWidth() {
        return image.getWidth();
    }

    public int getImageHeight() {
        return image.getHeight();
    }
}