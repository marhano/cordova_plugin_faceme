package inc.bastion.faceme;/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */


import android.graphics.Rect;
import android.util.Size;

public class RectUtil {

    public static Rect enlargeRect(Rect src, Size maxSize, float ratio) {
        int h_enlarge_pixel = (int) (src.width() * ratio);
        int v_enlarge_pixel = (int) (src.height() * ratio);
        return enlargeRect(src, maxSize, h_enlarge_pixel, v_enlarge_pixel, h_enlarge_pixel, v_enlarge_pixel);
    }

    static Rect enlargeRect(Rect src, Size maxSize, int left, int top, int right, int bottom) {
        // For horizontal space
        int outputLeft = Math.max(0, src.left - left);
        int outputRight = Math.min(maxSize.getWidth(), src.right + right);

        // For vertical space
        int outputTop = Math.max(0, src.top - top);
        int outputBottom = Math.min(maxSize.getHeight(), src.bottom + bottom);

        return new Rect(outputLeft, outputTop, outputRight, outputBottom);
    }

    public static Rect squareRect(Rect src, Size maxSize) {
        int minEdge = Math.min(src.width(), src.height());
        int maxEdge = Math.max(src.width(), src.height());
        int squareEdge = minEdge;

        int centerX = src.centerX();
        int centerY = src.centerY();
        double halfMaxEdge = Math.ceil(maxEdge / 2.0);

        if (centerX - halfMaxEdge >= 0 &&
                centerX + halfMaxEdge <= maxSize.getWidth() &&
                centerY - halfMaxEdge >= 0 &&
                centerY + halfMaxEdge <= maxSize.getHeight()) {
            squareEdge = maxEdge;
        }

        int halfSquareEdge = (int)Math.ceil(squareEdge / 2.0);
        int outputLeft = Math.max(0, centerX - halfSquareEdge);
        int outputTop = Math.max(0, centerY - halfSquareEdge);
        int outputRight = Math.min(maxSize.getWidth(), centerX + halfSquareEdge);
        int outputBottom = Math.min(maxSize.getHeight(), centerY + halfSquareEdge);

        return new Rect(outputLeft, outputTop, outputRight, outputBottom);
    }
}