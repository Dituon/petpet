package xmmt.dituon.share;

import kotlin.Pair;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

// TODO 支持图片变形
// 应该用3D库进行3D运算...伪3D效率比较低... 欢迎Pr!
// From https://math.stackexchange.com/questions/296794
public class ImageDeformer {

    private static int min(int v1, int v2, int v3, int v4) {
        return Math.min(Math.min(v1, v2), Math.min(v3, v4));
    }

    private static int max(int v1, int v2, int v3, int v4) {
        return Math.max(Math.max(v1, v2), Math.max(v3, v4));
    }

    public static BufferedImage computeImage(BufferedImage image, Point2D[] point) {
        int w = image.getWidth();
        int h = image.getHeight();

        Point2D ip0 = new Point2D.Double(0, 0);
        Point2D ip1 = new Point2D.Double(0, h);
        Point2D ip2 = new Point2D.Double(w, h);
        Point2D ip3 = new Point2D.Double(w, 0);

        Matrix3D originToDeformed = computeProjectionMatrix(
                new Point2D[]{point[0], point[1], point[2], point[3]},
                new Point2D[]{ip0, ip1, ip2, ip3}
        );
        Matrix3D deformedToOrigin = new Matrix3D(originToDeformed);
        deformedToOrigin.invert();

        Point2D deformedip0 = new Point2D.Double(0, 0);
        Point2D deformedip1 = new Point2D.Double(0, h);
        Point2D deformedip2 = new Point2D.Double(w, h);
        Point2D deformedip3 = new Point2D.Double(w, 0);
        originToDeformed.transform(deformedip0);
        originToDeformed.transform(deformedip1);
        originToDeformed.transform(deformedip2);
        originToDeformed.transform(deformedip3);
        Polygon deformedArea = new Polygon();
        deformedArea.addPoint((int)deformedip0.getX(), (int)deformedip0.getY());
        deformedArea.addPoint((int)deformedip1.getX(), (int)deformedip1.getY());
        deformedArea.addPoint((int)deformedip2.getX(), (int)deformedip2.getY());
        deformedArea.addPoint((int)deformedip3.getX(), (int)deformedip3.getY());

        int deformedWidth = deformedArea.getBounds().width;
        int deformedHeight = deformedArea.getBounds().getBounds().height;
        BufferedImage result = new BufferedImage(deformedWidth, deformedHeight, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < deformedHeight; y++) {
            for (int x = 0; x < deformedWidth; x++) {
                if (deformedArea.contains(x, y)) {
                    Point2D originPoint = new Point2D.Double(x, y);
                    deformedToOrigin.transform(originPoint);
                    int originX = Math.min((int) Math.round(originPoint.getX()), w - 1);
                    int originY = Math.min((int) Math.round(originPoint.getY()), h - 1);
                    int rgb = image.getRGB(originX, originY);
                    result.setRGB(x, y, rgb);
                }
            }
        }

        return result;
    }

    private static Matrix3D computeProjectionMatrix(Point2D[] p0, Point2D[] p1) {
        Matrix3D m0 = computeProjectionMatrix(p0);
        Matrix3D m1 = computeProjectionMatrix(p1);
        m1.invert();
        m0.mul(m1);
        return m0;
    }

    private static Matrix3D computeProjectionMatrix(Point2D[] p) {
        Matrix3D m = new Matrix3D(
                p[0].getX(), p[1].getX(), p[2].getX(),
                p[0].getY(), p[1].getY(), p[2].getY(),
                1, 1, 1);
        Point3D p3 = new Point3D(p[3].getX(), p[3].getY(), 1);
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

    private static class Point3D {
        double x;
        double y;
        double z;

        Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Matrix3D {
        double m00;
        double m01;
        double m02;
        double m10;
        double m11;
        double m12;
        double m20;
        double m21;
        double m22;

        Matrix3D(
                double m00, double m01, double m02,
                double m10, double m11, double m12,
                double m20, double m21, double m22) {
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

        Matrix3D(Matrix3D m) {
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

        void invert() {
            double invDet = 1.0 / determinant();
            double nm00 = m22 * m11 - m21 * m12;
            double nm01 = -(m22 * m01 - m21 * m02);
            double nm02 = m12 * m01 - m11 * m02;
            double nm10 = -(m22 * m10 - m20 * m12);
            double nm11 = m22 * m00 - m20 * m02;
            double nm12 = -(m12 * m00 - m10 * m02);
            double nm20 = m21 * m10 - m20 * m11;
            double nm21 = -(m21 * m00 - m20 * m01);
            double nm22 = m11 * m00 - m10 * m01;
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
        double determinant() {
            return
                    m00 * (m11 * m22 - m12 * m21) +
                            m01 * (m12 * m20 - m10 * m22) +
                            m02 * (m10 * m21 - m11 * m20);
        }

        final void mul(double factor) {
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

        void transform(Point3D p) {
            double x = m00 * p.x + m01 * p.y + m02 * p.z;
            double y = m10 * p.x + m11 * p.y + m12 * p.z;
            double z = m20 * p.x + m21 * p.y + m22 * p.z;
            p.x = x;
            p.y = y;
            p.z = z;
        }

        void transform(Point2D pp) {
            Point3D p = new Point3D(pp.getX(), pp.getY(), 1.0);
            transform(p);
            pp.setLocation(p.x / p.z, p.y / p.z);
        }

        void mul(Matrix3D m) {
            double nm00 = m00 * m.m00 + m01 * m.m10 + m02 * m.m20;
            double nm01 = m00 * m.m01 + m01 * m.m11 + m02 * m.m21;
            double nm02 = m00 * m.m02 + m01 * m.m12 + m02 * m.m22;

            double nm10 = m10 * m.m00 + m11 * m.m10 + m12 * m.m20;
            double nm11 = m10 * m.m01 + m11 * m.m11 + m12 * m.m21;
            double nm12 = m10 * m.m02 + m11 * m.m12 + m12 * m.m22;

            double nm20 = m20 * m.m00 + m21 * m.m10 + m22 * m.m20;
            double nm21 = m20 * m.m01 + m21 * m.m11 + m22 * m.m21;
            double nm22 = m20 * m.m02 + m21 * m.m12 + m22 * m.m22;

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
