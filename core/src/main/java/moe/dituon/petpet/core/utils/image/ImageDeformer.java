package moe.dituon.petpet.core.utils.image;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

// From https://math.stackexchange.com/questions/296794
public class ImageDeformer {
    protected final Matrix3D deformedMatrix;

    public ImageDeformer(Point2D[] point) {
        this.deformedMatrix = computeProjectionMatrix(point);
    }

    public BufferedImage computeImage(BufferedImage image) {
        return computeImage(image, this.deformedMatrix);
    }

    public static BufferedImage computeImage(BufferedImage image, Point2D[] deformedCoords) {
        return computeImage(image, computeProjectionMatrix(deformedCoords));
    }

    public static BufferedImage computeImage(final BufferedImage image, final Matrix3D deformedMatrix) {
        return computeImage(image, new DeformMapping(image, deformedMatrix));
    }

    public static BufferedImage computeImage(BufferedImage image, DeformMapping deformMapping) {
        return computeImage(image, deformMapping.deformedArea, deformMapping.deformedMatrix);
    }

    public DeformMapping createDeformMapping(BufferedImage image) {
        return new DeformMapping(image, this.deformedMatrix);
    }

    public static class DeformMapping {
        public final Polygon deformedArea;
        public final Matrix3D deformedMatrix;

        public DeformMapping(final BufferedImage image, final Matrix3D deformedMatrix) {
            float w = image.getWidth();
            float h = image.getHeight();

            Matrix3D originToDeformed = computeMatrix(
                    new Matrix3D(deformedMatrix),
                    computeRectMatrix(w, h)
            );
            this.deformedMatrix = new Matrix3D(originToDeformed);
            this.deformedMatrix.invert();
            float[] deformedip0 = originToDeformed.transform(new float[2], 0, 0);
            float[] deformedip1 = originToDeformed.transform(new float[2], 0, h);
            float[] deformedip2 = originToDeformed.transform(new float[2], w, h);
            float[] deformedip3 = originToDeformed.transform(new float[2], w, 0);

            int[] xPoints = new int[]{
                    (int) deformedip0[0], (int) deformedip1[0],
                    (int) deformedip2[0], (int) deformedip3[0]
            };
            int[] yPoints = new int[]{
                    (int) deformedip0[1], (int) deformedip1[1],
                    (int) deformedip2[1], (int) deformedip3[1]
            };
            deformedArea = new Polygon(xPoints, yPoints, 4);
        }

        public int getHeight() {
            return deformedArea.getBounds().height;
        }

        public int getWidth() {
            return deformedArea.getBounds().width;
        }
    }

    public static BufferedImage computeImage(
            final BufferedImage image,
            final Polygon deformedArea,
            final Matrix3D deformedToOrigin
    ) {
        Rectangle bounds = deformedArea.getBounds();
        int originWidth = image.getWidth();
        int originHeight = image.getHeight();
        int deformedWidth = bounds.width;
        int deformedHeight = bounds.height;

        BufferedImage result = new BufferedImage(
                Math.max(deformedWidth, 1), Math.max(deformedHeight, 1), BufferedImage.TYPE_INT_ARGB
        );

        float[] p = new float[2];
        for (int y = 0; y < deformedHeight; y++) {
            for (int x = 0; x < deformedWidth; x++) {
                if (deformedArea.contains(x, y)) {
                    deformedToOrigin.transform(p, x, y);
                    int originX = Math.min((int) p[0], originWidth - 1);
                    int originY = Math.min((int) p[1], originHeight - 1);
                    int rgb = image.getRGB(Math.max(originX, 0), Math.max(originY, 0));
                    result.setRGB(x, y, rgb);
                }
            }
        }

        return result;
    }

    protected static Matrix3D computeProjectionMatrix(Point2D[] p0, Point2D[] p1) {
        Matrix3D m0 = computeProjectionMatrix(p0);
        Matrix3D m1 = computeProjectionMatrix(p1);
        return computeMatrix(m0, m1);
    }

    protected static Matrix3D computeMatrix(Matrix3D m0, Matrix3D m1) {
        m1.invert();
        m0.mul(m1);
        return m0;
    }

    public static void toAbsoluteCoords(Point2D[] points) {
        float ltx = Float.MAX_VALUE;
        float lty = Float.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            float x = (float) points[i].getX();
            float y = (float) points[i].getY();
            ltx = Math.min(ltx, x);
            lty = Math.min(lty, y);
        }
        for (int i = 0; i < 4; i++) {
            float x = (float) points[i].getX();
            float y = (float) points[i].getY();
            points[i] = new Point2D.Float(x - ltx, y - lty);
        }
        float x = (float) points[4].getX();
        float y = (float) points[4].getY();
        points[4] = new Point2D.Float(x + ltx, y + lty);
    }

    protected static Matrix3D computeProjectionMatrix(Point2D[] p) {
        Matrix3D m = new Matrix3D(
                (float) p[0].getX(), (float) p[1].getX(), (float) p[2].getX(),
                (float) p[0].getY(), (float) p[1].getY(), (float) p[2].getY(),
                1f, 1f, 1f);
        Point3D p3 = new Point3D((float) p[3].getX(), (float) p[3].getY(), 1f);
        Matrix3D mInv = new Matrix3D(m);
        mInv.invert();
        mInv.transform(p3);
        m.m00 *= p3.x;
        m.m01 *= p3.y;
        m.m02 *= p3.z;
        m.m10 *= p3.x;
        m.m11 *= p3.y;
        m.m12 *= p3.z;
        m.m20 *= p3.x;
        m.m21 *= p3.y;
        m.m22 *= p3.z;
        return m;
    }

    protected static Matrix3D computeRectMatrix(float w, float h) {
        Matrix3D m = new Matrix3D(
                0, 0, w,
                0, h, h,
                1, 1, 1);
        Point3D p3 = new Point3D(w, 0, 1);
        Matrix3D mInv = new Matrix3D(m);
        mInv.invert();
        mInv.transform(p3);
        m.m00 *= p3.x;
        m.m01 *= p3.y;
        m.m02 *= p3.z;
        m.m10 *= p3.x;
        m.m11 *= p3.y;
        m.m12 *= p3.z;
        m.m20 *= p3.x;
        m.m21 *= p3.y;
        m.m22 *= p3.z;
        return m;
    }

    protected static class Point3D {
        float x;
        float y;
        float z;

        public Point3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Matrix3D {
        float m00;
        float m01;
        float m02;
        float m10;
        float m11;
        float m12;
        float m20;
        float m21;
        float m22;

        public Matrix3D(
                float m00, float m01, float m02,
                float m10, float m11, float m12,
                float m20, float m21, float m22) {
            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
        }

        public Matrix3D(Matrix3D m) {
            this.m00 = m.m00;
            this.m01 = m.m01;
            this.m02 = m.m02;
            this.m10 = m.m10;
            this.m11 = m.m11;
            this.m12 = m.m12;
            this.m20 = m.m20;
            this.m21 = m.m21;
            this.m22 = m.m22;
        }

        public void invert() {
            float invDet = 1f / determinant();
            float nm00 = m22 * m11 - m21 * m12;
            float nm01 = -(m22 * m01 - m21 * m02);
            float nm02 = m12 * m01 - m11 * m02;
            float nm10 = -(m22 * m10 - m20 * m12);
            float nm11 = m22 * m00 - m20 * m02;
            float nm12 = -(m12 * m00 - m10 * m02);
            float nm20 = m21 * m10 - m20 * m11;
            float nm21 = -(m21 * m00 - m20 * m01);
            float nm22 = m11 * m00 - m10 * m01;
            m00 = nm00 * invDet;
            m01 = nm01 * invDet;
            m02 = nm02 * invDet;
            m10 = nm10 * invDet;
            m11 = nm11 * invDet;
            m12 = nm12 * invDet;
            m20 = nm20 * invDet;
            m21 = nm21 * invDet;
            m22 = nm22 * invDet;
        }

        // From http://www.dr-lex.be/random/matrix_inv.html
        public float determinant() {
            return
                    m00 * (m11 * m22 - m12 * m21) +
                            m01 * (m12 * m20 - m10 * m22) +
                            m02 * (m10 * m21 - m11 * m20);
        }

        public void mul(float factor) {
            m00 *= factor;
            m01 *= factor;
            m02 *= factor;

            m10 *= factor;
            m11 *= factor;
            m12 *= factor;

            m20 *= factor;
            m21 *= factor;
            m22 *= factor;
        }

        protected void transform(Point3D p) {
            float x = m00 * p.x + m01 * p.y + m02 * p.z;
            float y = m10 * p.x + m11 * p.y + m12 * p.z;
            float z = m20 * p.x + m21 * p.y + m22 * p.z;
            p.x = x;
            p.y = y;
            p.z = z;
        }

        public void transform(Point2D pp) {
            Point3D p = new Point3D((float) pp.getX(), (float) pp.getY(), 1);
            transform(p);
            pp.setLocation(p.x / p.z, p.y / p.z);
        }

        public float[] transform(float[] result, float x, float y) {
            float z = m20 * x + m21 * y + m22;
            result[0] = (m00 * x + m01 * y + m02) / z;
            result[1] = (m10 * x + m11 * y + m12) / z;
            return result;
        }

        public void mul(Matrix3D m) {
            float nm00 = m00 * m.m00 + m01 * m.m10 + m02 * m.m20;
            float nm01 = m00 * m.m01 + m01 * m.m11 + m02 * m.m21;
            float nm02 = m00 * m.m02 + m01 * m.m12 + m02 * m.m22;

            float nm10 = m10 * m.m00 + m11 * m.m10 + m12 * m.m20;
            float nm11 = m10 * m.m01 + m11 * m.m11 + m12 * m.m21;
            float nm12 = m10 * m.m02 + m11 * m.m12 + m12 * m.m22;

            float nm20 = m20 * m.m00 + m21 * m.m10 + m22 * m.m20;
            float nm21 = m20 * m.m01 + m21 * m.m11 + m22 * m.m21;
            float nm22 = m20 * m.m02 + m21 * m.m12 + m22 * m.m22;

            m00 = nm00;
            m01 = nm01;
            m02 = nm02;
            m10 = nm10;
            m11 = nm11;
            m12 = nm12;
            m20 = nm20;
            m21 = nm21;
            m22 = nm22;
        }
    }
}
