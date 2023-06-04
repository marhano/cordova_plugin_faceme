package com.example.faceme;

import android.graphics.Bitmap;

import com.cyberlink.faceme.FaceAttribute;
import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceInfo;
import com.cyberlink.faceme.FaceLandmark; 

import com.example.faceme.FaceData;

public class FaceHolder{
    public final FaceInfo faceInfo;
    public final FaceLandmark faceLandmark;
    public final FaceAttribute faceAttribute;
    public final FaceFeature faceFeature;
    public final Bitmap faceBitmap;

    public final FaceData data = new FaceData();

    public FaceHolder(FaceInfo faceInfo, FaceLandmark faceLandmark, FaceAttribute faceAttribute, FaceFeature faceFeature, Bitmap faceBitmap){
        this.faceInfo = faceInfo;
        this.faceLandmark = faceLandmark;
        this.faceAttribute = faceAttribute;
        this.faceFeature = faceFeature;
        this.faceBitmap = faceBitmap;
    }
}
