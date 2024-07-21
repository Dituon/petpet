package moe.dituon.petpet.share.element.avatar;

import com.jhlabs.image.*;
import lombok.Getter;
import moe.dituon.petpet.share.*;
import moe.dituon.petpet.share.element.TemplateElement;
import moe.dituon.petpet.share.filter.PetpetOilFilter;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public abstract class AvatarModel implements TemplateElement {
    protected FitType fitType;
    protected short angle;
    protected TransformOrigin transformOrigin;
    protected float opacity = 1.0F;
    protected boolean round;
    protected boolean rotate;
    protected boolean onTop;
    protected List<BufferedImage> imageList;
    protected final boolean antialias;
    protected final boolean resampling;
    protected final AvatarPosType posType;
    protected CropType cropType;
    protected int[] cropPos;
    protected final List<AvatarStyle> styleList;
    protected final List<AvatarFilter> filterList;

    public AvatarModel(AvatarTemplate data, Supplier<List<BufferedImage>> extraData) {
        this(data, extraData, true);
    }

    public AvatarModel(AvatarTemplate data, Supplier<List<BufferedImage>> extraData, boolean initFlag) {
        imageList = extraData.get();
        posType = data.getPosType();
        cropType = data.getCropType();
        cropPos = data.getCrop();
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
        if (initFlag) buildImage();
    }

    @Override
    public TemplateElement.Type getElementType() {
        return TemplateElement.Type.AVATAR;
    }

    protected void buildImage() {
        if (getPosLength() > 1 && !filterList.isEmpty()
                && filterList.stream().anyMatch(AvatarFilter::hasAnimation)
        ) {
            int maxLength = filterList.stream()
                    .mapToInt(AvatarFilter::getMaxLength)
                    .max().orElse(1);
            if (imageList.size() == 1) {
                imageList = Collections.nCopies(maxLength, imageList.get(0));
            } else {
                imageList = IntStream.range(0, maxLength)
                        .mapToObj(i -> imageList.get(i % imageList.size()))
                        .collect(Collectors.toList());
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
                        ? getNElement(bulgeFilter.getRadius(), i) : Math.min(x, y);

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
            } else if (filter instanceof AvatarOilFilter) {
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

    private static float getNElement(float[] array, int i) {
        return array[i % array.length];
    }

    public FitType getZoomType() {
        return fitType;
    }

    public BufferedImage getFirstImage() {
        return imageList.get(0);
    }

    @Override
    public int getWidth() {
        return this.getFirstImage().getWidth();
    }

    @Override
    public int getHeight() {
        return this.getFirstImage().getHeight();
    }

    public abstract int getPosLength();

    public boolean isGif() {
        return imageList.size() > 1;
    }

    /**
     * 获取指定帧数, 超过索引长度会从头计数
     * 例: length: 8 index: 10 return: list[1]
     */
    public BufferedImage getFrame(int i) {
        return imageList.get(i % imageList.size());
    }
}