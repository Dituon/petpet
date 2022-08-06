package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ImageSynthesisCore {

    /**
     * 在Graphics2D画布上 绘制缩放头像
     *
     * @param g2d         Graphics2D 画布
     * @param avatarImage 处理后的头像
     * @param pos         处理后的坐标 (int[4]{x, y, w, h})
     * @param angle       旋转角, 对特殊角度有特殊处理分支
     * @param isRound     裁切为圆形
     */
    protected static void g2dDrawZoomAvatar(Graphics2D g2d, BufferedImage avatarImage, int[] pos,
                                            float angle, boolean isRound) {
        if (avatarImage == null) {
            return;
        }

        int x = pos[0];
        int y = pos[1];
        int w = pos[2];
        int h = pos[3];
        if (angle == 0) {
            g2d.drawImage(avatarImage, x, y, w, h, null);
            return;
        }

        if (isRound || angle % 90 == 0) {
            BufferedImage newAvatarImage = new BufferedImage(avatarImage.getWidth(), avatarImage.getHeight(), avatarImage.getType());
            Graphics2D rotateG2d = newAvatarImage.createGraphics();
            rotateG2d.rotate(Math.toRadians(angle), avatarImage.getWidth() / 2, avatarImage.getHeight() / 2);
            rotateG2d.drawImage(avatarImage, null, 0, 0);
            rotateG2d.dispose();
            g2d.drawImage(newAvatarImage, x, y, w, h, null);
            return;
        }

        g2d.drawImage(rotateImage(avatarImage, angle), x, y, w, h, null);
    }

    /**
     * 在Graphics2D画布上 绘制变形头像
     *
     * @param g2d         Graphics2D 画布
     * @param avatarImage 处理后的头像
     * @param deformPos   头像四角坐标 (Point2D[4]{左上角, 左下角, 右下角, 右上角})
     * @param anchorPos   锚点坐标
     */
    protected static void g2dDrawDeformAvatar(Graphics2D g2d, BufferedImage avatarImage,
                                              Point2D[] deformPos, int[] anchorPos) {
        BufferedImage result = ImageDeformer.computeImage(avatarImage, deformPos);
        g2d.drawImage(result, anchorPos[0], anchorPos[1], null);
    }

    /**
     * 在Graphics2D画布上 绘制文字
     *
     * @param g2d   Graphics2D 画布
     * @param text  文本数据
     * @param pos   坐标 (int[2]{x, y})
     * @param color 颜色
     * @param font  字体
     */
    protected static void g2dDrawText(Graphics2D g2d, String text, int[] pos, Color color, Font font) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.setFont(font);
        if (text.contains("\n")) {
            String[] texts = text.split("\n");
            int y = pos[1];
            short height = (short) TextModel.getFontHeight(font);
            for (String txt : texts) {
                g2d.drawString(txt, pos[0], y);
                y += height;
            }
            return;
        }
        g2d.drawString(text, pos[0], pos[1]);
    }

    /**
     * 将图像裁切为圆形
     *
     * @param input     输入图像
     * @param antialias 抗锯齿
     * @return 裁切后的图像
     */
    public static BufferedImage convertCircular(BufferedImage input, boolean antialias) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, input.getWidth(), input.getHeight());
        Graphics2D g2 = output.createGraphics();
        g2.setClip(shape);

        if (antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g2.drawImage(input, 0, 0, null);
        g2.dispose();
        return output;
    }

    /**
     * 将图像裁切为圆形
     *
     * @param inputList 输入图像数组
     * @param antialias 抗锯齿
     * @return 裁切后的图像
     */
    public static List<BufferedImage> convertCircular(List<BufferedImage> inputList, boolean antialias) {
        return inputList.stream()
                .map(input -> convertCircular(input, antialias))
                .collect(Collectors.toList());
    }

    /**
     * 完整旋转图像 (旋转时缩放以保持图像完整性)
     *
     * @param avatarImage 输入图像
     * @param angle       旋转角度
     * @return 旋转后的图像
     */
    public static BufferedImage rotateImage(BufferedImage avatarImage, float angle) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));
        int w = avatarImage.getWidth();
        int h = avatarImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin),
                newh = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(neww, newh, avatarImage.getType());
        Graphics2D g2d = rotated.createGraphics();
        rotated = g2d.getDeviceConfiguration().createCompatibleImage(
                rotated.getWidth(), rotated.getHeight(), Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = rotated.createGraphics();

        g2d.translate((neww - w) / 2, (newh - h) / 2);
        g2d.rotate(Math.toRadians(angle), w / 2, h / 2);
        g2d.drawRenderedImage(avatarImage, null);
        g2d.dispose();
        return rotated;
    }

    /**
     * 从URL获取网络图像
     *
     * @param imageUrl 图像URL
     */
    public static BufferedImage getWebImage(String imageUrl) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new URL(imageUrl));
        } catch (Exception e) {
            System.out.println("[获取图像失败]  URL: " + imageUrl);
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 从URL获取网络图像 (支持GIF)
     *
     * @param imageUrl 图像URL
     * @return GIF全部帧 或一张静态图像
     */
    public static List<BufferedImage> getWebImageAsList(String imageUrl) {
        ArrayList<BufferedImage> output = new ArrayList<>();
        try {
            URL url = new URL(imageUrl);
            GifDecoder decoder = new GifDecoder();
            BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
            inputStream.mark(0); //循环利用inputStream, 避免重复获取
            decoder.read(inputStream);
            if (decoder.err()) {
                inputStream.reset();
                output.add(ImageIO.read(ImageIO.createImageInputStream(inputStream)));
                inputStream.close();
                return output;
            }
            inputStream.close();
            for (short i = 0; i < decoder.getFrameCount(); i++) {
                output.add(decoder.getFrame(i));
            }
        } catch (Exception ex) {
            System.out.println("[获取/解析 图像失败]  URL: " + imageUrl);
            ex.printStackTrace();
        }
        return output;
    }

//    public static List<BufferedImage> getWebImageAsList(String imageUrl) {
//        InputStream input = null;
//        try {
//            URL url = new URL(imageUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.connect();
//            input = conn.getInputStream();
//            conn.disconnect();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//
//        try {
//            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
//            ImageInputStream ciis = ImageIO.createImageInputStream(input);
//            ArrayList<BufferedImage> output = new ArrayList<>();
//
//            reader.setInput(ciis, false);
//            int noi = reader.getNumImages(true);
//            BufferedImage master = null;
//
//            for (int i = 0; i < noi; i++) {
//                BufferedImage image = reader.read(i);
//                IIOMetadata metadata = reader.getImageMetadata(i);
//                Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
//                NodeList children = tree.getChildNodes();
//
//                for (int j = 0; j < children.getLength(); j++) {
//                    Node nodeItem = children.item(j);
//
//                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
//                        int width;
//                        int height;
//                        int leftPosition;
//                        int topPosition;
//
//                        NamedNodeMap attr = nodeItem.getAttributes();
//                        width = Integer.parseInt(attr.getNamedItem("imageWidth").getNodeValue());
//                        height = Integer.parseInt(attr.getNamedItem("imageHeight").getNodeValue());
//                        leftPosition = Integer.parseInt(attr.getNamedItem("imageLeftPosition").getNodeValue());
//                        topPosition = Integer.parseInt(attr.getNamedItem("imageTopPosition").getNodeValue());
//
//                        if (i == 0) {
//                            master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//                        }
//                        master.getGraphics().drawImage(image, leftPosition, topPosition, null);
//                    }
//                }
//                assert master != null;
//                output.add(master);
//            }
//            return output;
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return null;
//        } catch (Exception ex) {
//            try {
//                List<BufferedImage> output = new ArrayList<>();
//                output.add(ImageIO.read(input));
//                return output;
//            } catch (IOException ioex) {
//                ioex.printStackTrace();
//                return null;
//            }
//        }
//    }

    /**
     * 裁切图像
     *
     * @param image   输入图像
     * @param cropPos 裁切坐标 (int[4]{x1, y1, x2, y2})
     * @return 裁切后的图像
     */
    public static BufferedImage cropImage(BufferedImage image, int[] cropPos) {
        return cropImage(image, cropPos, false);
    }

    /**
     * 裁切图像
     *
     * @param image     输入图像
     * @param cropPos   裁切坐标 (int[4]{x1, y1, x2, y2})
     * @param isPercent 按百分比处理坐标
     * @return 裁切后的图像
     */
    public static BufferedImage cropImage(BufferedImage image, int[] cropPos, boolean isPercent) {
        int width = cropPos[2] - cropPos[0];
        int height = cropPos[3] - cropPos[1];
        if (isPercent) {
            width = (int) ((float) width / 100 * image.getWidth());
            height = (int) ((float) height / 100 * image.getHeight());
        }
        BufferedImage croppedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = croppedImage.createGraphics();
        if (isPercent) { //百分比
            g2d.drawImage(image, 0, 0, width, height,
                    (int) ((float) cropPos[0] / 100 * image.getWidth()),
                    (int) ((float) cropPos[1] / 100 * image.getHeight()),
                    (int) ((float) cropPos[2] / 100 * image.getWidth()),
                    (int) ((float) cropPos[3] / 100 * image.getHeight()), null);
        } else { //像素
            g2d.drawImage(image, 0, 0, width, height
                    , cropPos[0], cropPos[1], cropPos[2], cropPos[3], null);
        }
        g2d.dispose();
        return croppedImage;
    }

    /**
     * 裁切图像
     *
     * @param imageList 输入图像数组
     * @param cropPos   裁切坐标 (int[4]{x1, y1, x2, y2})
     * @param isPercent 按百分比处理坐标
     * @return 裁切后的图像
     */
    public static List<BufferedImage> cropImage(List<BufferedImage> imageList,
                                                int[] cropPos, boolean isPercent) {
        return imageList.stream()
                .map(image -> cropImage(image, cropPos, isPercent))
                .collect(Collectors.toList());
    }

    /**
     * 镜像翻转图像
     */
    public static BufferedImage mirrorImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage mirroredImage;
        Graphics2D g2d;
        (g2d = (mirroredImage = new BufferedImage(width, height, image
                .getColorModel().getTransparency())).createGraphics())
                .drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        g2d.dispose();
        return mirroredImage;
    }

    /**
     * 镜像翻转图像数组
     */
    public static List<BufferedImage> mirrorImage(List<BufferedImage> imageList) {
        return imageList.stream().map(ImageSynthesisCore::mirrorImage).collect(Collectors.toList());
    }

    /**
     * 竖直翻转图像
     */
    public static BufferedImage flipImage(BufferedImage image) {
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(),
                image.getType());
        AffineTransform tran = AffineTransform.getTranslateInstance(0,
                image.getHeight());
        AffineTransform flip = AffineTransform.getScaleInstance(1d, -1d);
        tran.concatenate(flip);
        Graphics2D g2d = flipped.createGraphics();
        g2d.setTransform(tran);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return flipped;
    }

    /**
     * 竖直翻转图像数组
     */
    public static List<BufferedImage> flipImage(List<BufferedImage> imageList) {
        return imageList.stream()
                .map(ImageSynthesisCore::flipImage)
                .collect(Collectors.toList());
    }

    /**
     * 图像灰度化
     */
    public static BufferedImage grayImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
                Color color_end = new Color(gray, gray, gray);
                grayscaleImage.setRGB(x, y, color_end.getRGB());
            }
        }
        return grayscaleImage;
    }

    /**
     * 灰度化图像数组
     */
    public static List<BufferedImage> grayImage(List<BufferedImage> imageList) {
        return imageList.stream().map(ImageSynthesisCore::grayImage).collect(Collectors.toList());
    }

    /**
     * 图像二值化
     */
    public static BufferedImage binarizeImage(BufferedImage image) {
        int h = image.getHeight();
        int w = image.getWidth();
        BufferedImage binarizeImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int val = image.getRGB(i, j);
                int r = (0x00ff0000 & val) >> 16;
                int g = (0x0000ff00 & val) >> 8;
                int b = (0x000000ff & val);
                int m = (r + g + b);
                if (m >= 383) {
                    binarizeImage.setRGB(i, j, Color.WHITE.getRGB());
                } else {
                    binarizeImage.setRGB(i, j, 0);
                }
            }
        }
        return binarizeImage;
    }

    /**
     * 二值化图像数组
     */
    public static List<BufferedImage> binarizeImage(List<BufferedImage> imageList) {
        return imageList.stream().map(ImageSynthesisCore::binarizeImage).collect(Collectors.toList());
    }
}
