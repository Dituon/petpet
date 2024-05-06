package moe.dituon.petpet.share;

import com.jhlabs.image.*;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonPrimitive;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AvatarModel {
    protected AvatarType type;
    protected int[][] pos = {{0, 0, 100, 100}};
    protected FitType fitType;
    protected short angle;
    protected TransformOrigin transformOrigin;
    protected float opacity = 1.0F;
    protected boolean round;
    protected boolean rotate;
    protected boolean onTop;
    protected List<BufferedImage> imageList = null;
    private Type imageType;
    private short posIndex = 0;
    private boolean antialias;
    private boolean resampling;
    private AvatarPosType posType;
    private DeformData deformData = null;
    private CropType cropType;
    private int[] cropPos;
    private List<AvatarStyle> styleList;
    private List<AvatarFilter> filterList;
    private short frameIndex = 0;

    @Deprecated
    public AvatarModel(AvatarData data, AvatarExtraDataProvider extraData, Type imageType) {
        setImage(data.getType(), extraData);
        buildData(data, imageType);
    }

    public AvatarModel(AvatarData data, GifAvatarExtraDataProvider extraData, Type imageType) {
        setImage(data.getType(), extraData);
        buildData(data, imageType);
    }

    private void buildData(AvatarData data, Type imageType) {
        type = data.getType();
        posType = data.getPosType();
        setPos(data.getPos(), this.imageType = imageType);
        cropType = data.getCropType();
        setCrop(data.getCrop());
        fitType = data.getFit();
        styleList = data.getStyle();
        filterList = data.getFilter();
        angle = data.getAngle();
        opacity = data.getOpacity();
        round = data.getRound();
        rotate = data.getRotate();
        transformOrigin = data.getOrigin();
        onTop = data.getAvatarOnTop();
        antialias = Boolean.TRUE.equals(data.getAntialias());
        resampling = Boolean.TRUE.equals(data.getResampling());
        buildImage();
    }

    private void setImage(AvatarType type, AvatarExtraDataProvider extraData) {
        imageList = new ArrayList<>(1);
        switch (type) {
            case FROM:
                imageList.add(Objects.requireNonNull(extraData.getFromAvatar()).invoke());
                break;
            case TO:
                imageList.add(Objects.requireNonNull(extraData.getToAvatar()).invoke());
                break;
            case GROUP:
                imageList.add(Objects.requireNonNull(extraData.getGroupAvatar()).invoke());
                break;
            case BOT:
                imageList.add(Objects.requireNonNull(extraData.getBotAvatar()).invoke());
                break;
            case RANDOM:
                imageList.add(Objects.requireNonNull(extraData.getRandomAvatar()).invoke());
                break;
        }
    }

    private void setImage(AvatarType type, GifAvatarExtraDataProvider extraData) {
        switch (type) {
            case FROM:
                imageList = Objects.requireNonNull(extraData.getFromAvatar()).invoke();
                break;
            case TO:
                imageList = Objects.requireNonNull(extraData.getToAvatar()).invoke();
                break;
            case GROUP:
                imageList = Objects.requireNonNull(extraData.getGroupAvatar()).invoke();
                break;
            case BOT:
                imageList = Objects.requireNonNull(extraData.getBotAvatar()).invoke();
                break;
            case RANDOM:
                imageList = Objects.requireNonNull(extraData.getRandomAvatar()).invoke();
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
                switch (imageType) {
                    case GIF:
                        try {
                            deformData = DeformData.fromGifPos(posElements);
                            break;
                        } catch (Exception ignored) {
                        }
                    case IMG:
                        deformData = DeformData.fromImgPos(posElements);
                        break;
                }
        }
    }

    private void setCrop(int[] crop) {
        if (crop == null || crop.length == 0) return;
        cropPos = crop.length == 2 ? new int[]{0, 0, crop[0], crop[1]} : crop;
    }

    private int[] JsonArrayToIntArray(JsonArray ja) {
        int[] result = new int[ja.size()];
        short i = 0;
        for (JsonElement je : ja) {
            if (je instanceof JsonArray) {
                return JsonArrayToIntArray((JsonArray) je);
            }
            String str = ((JsonPrimitive) je).getContent();
            try {
                result[i] = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                ArithmeticParser parser = new ArithmeticParser(str);
                parser.put("width", this.getImageWidth());
                parser.put("height", this.getImageHeight());
                result[i] = (int) parser.eval();
            }
            i++;
        }
        return result;
    }

    private void buildImage() {
        if (imageType == Type.GIF && !filterList.isEmpty() && filterList.stream().anyMatch(AvatarFilter::hasAnimation)) {
            int maxLength = filterList.stream()
                    .mapToInt(AvatarFilter::getMaxLength)
                    .max().orElse(1);
            if (imageList.size() == 1) {
                imageList = Collections.nCopies(maxLength, imageList.get(0));
            } else {
                imageList = IntStream.range(0, maxLength)
                        .mapToObj(i -> imageList.get(i % imageList.size()))
                        .collect(Collectors.toList());;
            }
        }
        if (imageList.size() == 1) {
            imageList = List.of(buildImage(0, imageList.get(0)));
        } else {
            imageList = ImageSynthesis.execImageList(imageList, this::buildImage);
        }
    }

    public BufferedImage buildImage(int index, BufferedImage image) {
        if (cropType != CropType.NONE) {
            image = ImageSynthesis.cropImage(image, cropType, cropPos);
        }

        if (!styleList.isEmpty()) image = buildStyledImage(image);

        if (!filterList.isEmpty()) image = buildFilteredImage(index, image);

        if (round) {
            image = ImageSynthesis.convertCircular(image, antialias);
        }

        if (resampling && posType == AvatarPosType.ZOOM) {
            int aw = 0, ah = 0, maxSize = 0;
            for (int[] p : pos) {
                if (p[2] > aw) aw = p[2];
                if (p[3] > ah) ah = p[3];
                maxSize = Math.max(aw, ah);
            }

            try {
                image = Thumbnails.of(image).size(maxSize, maxSize).keepAspectRatio(true).asBufferedImage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return image;
    }

    protected BufferedImage buildStyledImage(BufferedImage image) {
        for (AvatarStyle style : styleList) {
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
                    image = ImageSynthesis.binarizeImage(image);
                    break;
            }
        }
        return image;
    }

    protected BufferedImage buildFilteredImage(BufferedImage image) {
        return buildFilteredImage(0, image);
    }

    protected BufferedImage buildFilteredImage(int i, BufferedImage image) {
        for (AvatarFilter filter : filterList) {
            if (filter instanceof AvatarSwirlFilter) {
                AvatarSwirlFilter swirlFilter = (AvatarSwirlFilter) filter;
                TwirlFilter tFilter = new TwirlFilter();
                tFilter.setRadius(getNElement(swirlFilter.getRadius(), i));
                tFilter.setAngle(getNElement(swirlFilter.getAngle(), i) / 2);
                tFilter.setCentreX(getNElement(swirlFilter.getX(), i));
                tFilter.setCentreY(getNElement(swirlFilter.getY(), i));
                image = tFilter.filter(image, null);
            } else if (filter instanceof AvatarBulgeFilter) {
                AvatarBulgeFilter bulgeFilter = (AvatarBulgeFilter) filter;

                int x = Math.round(image.getWidth() * getNElement(bulgeFilter.getX(), i));
                int y = Math.round(image.getHeight() * getNElement(bulgeFilter.getY(), i));

                float radius = getNElement(bulgeFilter.getRadius(), i) != 0
                        ? getNElement(bulgeFilter.getRadius(), i)
                        : Math.min(x, y);

                image = ImageSynthesis.bulgePinchImage(
                        image, x, y, radius, getNElement(bulgeFilter.getStrength(), i)
                );
            } else if (filter instanceof AvatarBlurFilter) {
                AvatarBlurFilter blurFilter = (AvatarBlurFilter) filter;
                BoxBlurFilter bFilter = new BoxBlurFilter();
                bFilter.setRadius(getNElement(blurFilter.getRadius(), i));
                image = bFilter.filter(image, null);
            } else if (filter instanceof AvatarContrastFilter) {
                AvatarContrastFilter contrastFilter = (AvatarContrastFilter) filter;
                ContrastFilter cFilter = new ContrastFilter();
                cFilter.setContrast(getNElement(contrastFilter.getContrast(), i) + 1f);
                cFilter.setBrightness(getNElement(contrastFilter.getBrightness(), i) + 1f);
                image = cFilter.filter(image, null);
            } else if (filter instanceof AvatarHSBFilter) {
                AvatarHSBFilter hsbFilter = (AvatarHSBFilter) filter;
                HSBAdjustFilter hFilter = new HSBAdjustFilter();
                hFilter.setHFactor(getNElement(hsbFilter.getHue(), i));
                hFilter.setSFactor(getNElement(hsbFilter.getSaturation(), i));
                hFilter.setBFactor(getNElement(hsbFilter.getBrightness(), i));
                image = hFilter.filter(image, null);
            } else if (filter instanceof AvatarHalftoneFilter) {
                AvatarHalftoneFilter halftoneFilter = (AvatarHalftoneFilter) filter;
                ColorHalftoneFilter cFilter = new ColorHalftoneFilter();
                cFilter.setdotRadius(getNElement(halftoneFilter.getRadius(), i));
                float angle = getNElement(halftoneFilter.getAngle(), i);
                cFilter.setCyanScreenAngle(cFilter.getCyanScreenAngle() + angle);
                cFilter.setMagentaScreenAngle(cFilter.getMagentaScreenAngle() + angle);
                cFilter.setYellowScreenAngle(cFilter.getYellowScreenAngle() + angle);
                image = cFilter.filter(image, null);
            } else if (filter instanceof AvatarDotScreenFilter) {
                AvatarDotScreenFilter dotScreenFilter = (AvatarDotScreenFilter) filter;
                ColorHalftoneFilter cFilter = new ColorHalftoneFilter();
                cFilter.setdotRadius(getNElement(dotScreenFilter.getRadius(), i));
                float angle = getNElement(dotScreenFilter.getAngle(), i);
                cFilter.setCyanScreenAngle(angle);
                cFilter.setMagentaScreenAngle(angle);
                cFilter.setYellowScreenAngle(angle);
                image = ImageSynthesis.grayImage(cFilter.filter(image, null));
            } else if (filter instanceof AvatarNoiseFilter) {
                AvatarNoiseFilter noiseFilter = (AvatarNoiseFilter) filter;
                NoiseFilter nFilter = new NoiseFilter();
                nFilter.setAmount(Math.round(getNElement(noiseFilter.getAmount(), i) * 100));
                image = nFilter.filter(image, null);
            } else if (filter instanceof AvatarDenoiseFilter) {
                MedianFilter dFilter = new MedianFilter();
                image = dFilter.filter(image, null);
            } else if (filter instanceof AvatarSwimFilter) {
                AvatarSwimFilter swimFilter = (AvatarSwimFilter) filter;
                SwimFilter sFilter = new SwimFilter();
                sFilter.setScale(getNElement(swimFilter.getScale(), i));
                sFilter.setStretch(getNElement(swimFilter.getStretch(), i));
                sFilter.setAmount(getNElement(swimFilter.getAmount(), i));
                sFilter.setAngle(getNElement(swimFilter.getAngle(), i));
                sFilter.setTurbulence(getNElement(swimFilter.getTurbulence(), i));
                sFilter.setTime(getNElement(swimFilter.getTime(), i));
                image = sFilter.filter(image, null);
            } else if (filter instanceof AvatarOilFilter){
                AvatarOilFilter oilFilter = (AvatarOilFilter) filter;
                PetpetOilFilter oFilter = new PetpetOilFilter();
                oFilter.setLevels((int) getNElement(oilFilter.getLevels(), i));
                oFilter.setSkip((int) getNElement(oilFilter.getSkip(), i));
                oFilter.setRange((int) getNElement(oilFilter.getRange(), i));
                image = oFilter.filter(image, null);
            }
        }
        return image;
    }

    private static float getNElement(float[] array, int i){
        return array[i % array.length];
    }

    public FitType getZoomType() {
        return fitType;
    }

    public float getOpacity() {
        return opacity;
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
    public List<BufferedImage> getImageList() {
        return imageList;
    }

    public BufferedImage getFirstImage() {
        return imageList.get(0);
    }

    /**
     * 获取下一个旋转角度
     * <li>不旋转时 返回初始角度</li>
     * <li>GIF 返回下一个旋转角度</li>
     *
     * @deprecated 应当使用index直接获取角度, 之后的版本将不再维护posIndex
     */
    @Deprecated
    public float getNextAngle() {
        if (!rotate) return angle; //不旋转
        return ((float) (360 / pos.length) * posIndex) + angle; //GIF自动旋转
    }

    /**
     * 获取旋转角度
     * <li>不旋转时 返回初始角度</li>
     * <li>GIF 返回自动旋转角度</li>
     */
    public float getAngle(short index) {
        if (!rotate || imageType == Type.IMG) return angle; //不旋转
        return ((float) (360 / pos.length) * index) + angle; //GIF自动旋转
    }

    public TransformOrigin getTransformOrigin() {
        return transformOrigin;
    }

    /**
     * 获取下一个坐标
     *
     * @deprecated 应当使用index直接获取坐标, 之后的版本将不再维护posIndex
     */
    @Deprecated
    public int[] nextPos() {
        return getPos(posIndex++);
    }

    /**
     * 获取坐标(索引越界会返回最后的坐标)
     */
    public int[] getPos(short i) {
        if (i >= pos.length) return pos[pos.length - 1];
        return pos[i];
    }

    public AvatarPosType getPosType() {
        return posType;
    }

    /**
     * 获取坐标数组实际长度
     */
    public short getPosLength() {
        return (short) pos.length;
    }

    public DeformData getDeformData() {
        return deformData;
    }

    public int getImageWidth() {
        return this.getFirstImage().getWidth();
    }

    public int getImageHeight() {
        return this.getFirstImage().getHeight();
    }

    public boolean isGif() {
        return imageList.size() > 1;
    }

    /**
     * 获取头像下一帧, 超过索引长度会重新开始循环 <b>(线程不安全)</b>
     * <b>应当使用index直接获取帧</b>
     */
    public BufferedImage nextFrame() {
        if (frameIndex >= imageList.size()) frameIndex = 0;
        return imageList.get(frameIndex++);
    }

    /**
     * 获取指定帧数, 超过索引长度会从头计数
     * 例如: length: 8 index: 10 return: list[1]
     */
    public BufferedImage getFrame(short i) {
        i = (short) (i % imageList.size());
        return imageList.get(i);
    }

    public static class DeformData {
        static final int POS_SIZE = 4;
        Point2D[][] deformPos;
        int[][] anchor;

        public static DeformData fromGifPos(JsonArray posElements) {
//            BasePetService.LOGGER.info("DeformData fromPos by: " + posElements.toString());

            DeformData deformData = new DeformData();
            deformData.deformPos = new Point2D[posElements.size()][POS_SIZE];
            deformData.anchor = new int[posElements.size()][2];
            short f = 0;
            for (JsonElement frame : posElements) {
                for (short i = 0; i < POS_SIZE; i++) {
                    deformData.deformPos[f][i] = new Point2D.Double(
                            Integer.parseInt(((JsonArray) ((JsonArray) frame).get(i)).get(0).toString()),
                            Integer.parseInt(((JsonArray) ((JsonArray) frame).get(i)).get(1).toString())
                    );
                }
                deformData.anchor[f][0] = Integer.parseInt(((JsonArray) ((JsonArray) frame).get(POS_SIZE)).get(0).toString());
                deformData.anchor[f][1] = Integer.parseInt(((JsonArray) ((JsonArray) frame).get(POS_SIZE)).get(1).toString());
                f++;
            }
            return deformData;
        }

        public static DeformData fromImgPos(JsonArray posElements) {
            DeformData deformData = new DeformData();
            deformData.deformPos = new Point2D[1][POS_SIZE];
            deformData.anchor = new int[1][2];
            for (short i = 0; i < POS_SIZE; i++) {
                deformData.deformPos[0][i] = new Point2D.Double(
                        Integer.parseInt(((JsonArray) posElements.get(i)).get(0).toString()),
                        Integer.parseInt(((JsonArray) posElements.get(i)).get(1).toString())
                );
            }
            deformData.anchor[0][0] = Integer.parseInt(((JsonArray) posElements.get(POS_SIZE)).get(0).toString());
            deformData.anchor[0][1] = Integer.parseInt(((JsonArray) posElements.get(POS_SIZE)).get(1).toString());

            return deformData;
        }

        public short getLength() {
            return (short) deformPos.length;
        }

        public Point2D[] getDeformPos(short i) {
            i = (short) (i % deformPos.length);
            return deformPos[i];
        }

        public int[] getAnchor(short i) {
            i = (short) (i % anchor.length);
            return anchor[i];
        }
    }
}