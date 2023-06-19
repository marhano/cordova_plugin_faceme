package inc.bastion.faceme;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.cyberlink.faceme.DetectionMode;
import com.cyberlink.faceme.DetectionModelSpeedLevel;
import com.cyberlink.faceme.DetectionOutputOrder;
import com.cyberlink.faceme.DetectionSpeedLevel;
import com.cyberlink.faceme.EnginePreference;
import com.cyberlink.faceme.ExtractConfig;
import com.cyberlink.faceme.ExtractionModelSpeedLevel;
import com.cyberlink.faceme.ExtractionOption;
import com.cyberlink.faceme.FaceAttribute;
import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceFeatureScheme;
import com.cyberlink.faceme.FaceInfo;
import com.cyberlink.faceme.FaceLandmark;
import com.cyberlink.faceme.FaceMeDataManager;
import com.cyberlink.faceme.FaceMeRecognizer;
import com.cyberlink.faceme.FaceMeSdk;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.LicenseManager;

import inc.bastion.faceme.FaceHolder;
import inc.bastion.faceme.RectUtil;
import inc.bastion.faceme.LicenseUtils;

import android.graphics.Matrix;
import android.nfc.Tag;
import android.telecom.Call;
import android.util.Base64;
import android.content.Context;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Size;
import android.graphics.BitmapFactory;

public class FaceMe extends CordovaPlugin {
  private static final String TAG = "FaceMe";

  private static final String LICENSE_KEY = "olJ5ziHGlU3FIHWZhhCAq27xJ70q4aMx1lVTK8TI";
  private boolean isLicenseActivated = false;

  private static final String TEST_PLUGIN = "testPlugin";

  private static final String GET_BASE64_IMAGE = "getBase64Image";
  private static final String GET_BOUNDING_BOX = "getBoundingBox";
  private static final String GET_BITMAP_IMAGE = "getBitmapImage";
  private static final String ACTIVATE_LICENSE = "activateLicense";
  private static final String DEACTIVATE_LICENSE = "deactivateLicense";

  //INITIAL METHODS FOR PROTOTYPE
  private static final String INITIALIZE_SDK = "initializeSDK";
  private static final String DETECT_FACE = "detectFace";
  private static final String ENROLL_FACE = "enrollFace";
  private static final String RECOGNIZE_FACE = "recognizeFace";

  private CallbackContext execCallback;
  private JSONArray execArgs;

  private FaceMeRecognizer recognizer = null;
  private ExtractConfig extractConfig = null;

  private int maxFrameHeight = 1280;
  private int maxFrameWidth = 720;

  private static final int MIN_COORDINATE = 0;

  public FaceMe(){
      super();
      Log.d(TAG, "Loading");
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if(TEST_PLUGIN.equals(action)){
      return testPlugin(callbackContext);
    }else if(INITIALIZE_SDK.equals(action)){
      return initializeSDK(callbackContext);
    }else if(GET_BASE64_IMAGE.equals(action)){
      String base64Image = args.getString(0);
      return getBase64Image(base64Image, callbackContext);
    }else if(GET_BITMAP_IMAGE.equals(action)){
      JSONArray pixelDataArray = args.getJSONArray(0);
      return getBitmapImage(pixelDataArray, callbackContext);
    }else if(ACTIVATE_LICENSE.equals(action)){
      return activateLicense(callbackContext);
    }else if(DEACTIVATE_LICENSE.equals(action)){
      return deactivateLicense(callbackContext);
    }else if(DETECT_FACE.equals(action)){
      String base64Image = args.getString(0);
      return detectFace(base64Image, callbackContext);
    }else if(ENROLL_FACE.equals(action)){
      return enrollFace(callbackContext);
    }else if(RECOGNIZE_FACE.equals(action)){
      return recognizeFace(callbackContext);
    }
    return false;
  }

  private boolean initializeSDK(CallbackContext callbackContext){
    Context context = this.cordova.getActivity().getApplicationContext();
    FaceMeSdk.initialize(context, LICENSE_KEY);

    LicenseManager licenseManager = null;
    licenseManager = new LicenseManager();
    int result = licenseManager.initializeEx();
    result = licenseManager.registerLicense();
    licenseManager.release();

    if(result == 0){
      callbackContext.success("SDK initialized successfully");
    }else{
      callbackContext.success("SDK initialized failed: Server Error" + result);
    }

    return true;
  }

  private boolean detectFace(String base64Image, CallbackContext callbackContext) throws JSONException{
    Bitmap bitmap = base64ToBitmap(base64Image);
    if(recognizer == null || bitmap.getHeight() != maxFrameHeight || bitmap.getHeight() != maxFrameWidth){
      maxFrameHeight = bitmap.getHeight();
      maxFrameWidth = bitmap.getWidth();
      releaseRecognizer();
      initializeRecognizer();
    }

    JSONObject jsonObjectFaceHolder = extractBitmap(bitmap);
    if(isLicenseActivated == true){
      callbackContext.success(jsonObjectFaceHolder);
    }
    return true;
  }

  private boolean enrollFace(CallbackContext callbackContext){
    callbackContext.success("Enroll Face");
    return true;
  }

  private boolean recognizeFace(CallbackContext callbackContext){
    callbackContext.success("Recognize Face");
    return true;
  }



  private boolean getBitmapImage(JSONArray pixelDataArray, CallbackContext callbackContext) throws JSONException{
    int width = 400;
    int height = 600;

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    for(int i = 0; i < pixelDataArray.length(); i++){
      int color = pixelDataArray.getInt(i);
      bitmap.setPixel(i % width, i / width, color);
    }

    return true;
  }

  private boolean getBase64Image(String pixelData, CallbackContext callbackContext) throws JSONException{
    Bitmap bitmap = base64ToBitmap(pixelData);


    if(recognizer == null || bitmap.getHeight() != maxFrameHeight || bitmap.getWidth() != maxFrameWidth){
      Log.i(TAG, "Image resolution changed. Reinitialize recognizer.");
      maxFrameHeight = bitmap.getHeight();
      maxFrameWidth = bitmap.getWidth();
      releaseRecognizer();
      initializeRecognizer();
    }

    JSONObject jsonObjectFaceHolder = extractBitmap(configBitmap(bitmap));

    if(jsonObjectFaceHolder == null){
      callbackContext.error("No Face Detected");
    }

    if(isLicenseActivated == true){
      callbackContext.success(jsonObjectFaceHolder);
    }
    callbackContext.success("null");

    return true;
  }

  private boolean testPlugin(CallbackContext callbackContext){
    callbackContext.success("PLUGIN TEST ABCD 1234");

    return true;
  }

  private boolean deactivateLicense(CallbackContext callbackContext){
    Context context = this.cordova.getActivity().getApplicationContext();
    LicenseUtils licenseUtils = new LicenseUtils();
    FaceMeSdk.initialize(context, LICENSE_KEY);
    int isDeactivated = licenseUtils.deactivateLicense();
    if(isDeactivated == 0){
        isLicenseActivated = false;
    }
    callbackContext.success(isDeactivated);
    return true;
  }

  private boolean activateLicense(CallbackContext callbackContext){
    Context context = this.cordova.getActivity().getApplicationContext();
    LicenseUtils licenseUtils = new LicenseUtils();
    FaceMeSdk.initialize(context, LICENSE_KEY);
    int isLicenseValid = licenseUtils.verifyLicense();
    if(isLicenseValid == 0){
      isLicenseActivated = true;
    }
    callbackContext.success(isLicenseValid);
    return true;
  }




  private JSONObject convertToJsonArray(ArrayList<FaceHolder> faceHolders) throws JSONException{
    JSONObject faceHolderObj = new JSONObject();
      for(FaceHolder faceHolder : faceHolders){
        JSONObject faceAttributeObj = new JSONObject();
        faceAttributeObj.put("age", faceHolder.faceAttribute.age);
        faceAttributeObj.put("emotion", faceHolder.faceAttribute.emotion);
        faceAttributeObj.put("gender", faceHolder.faceAttribute.gender);
        faceAttributeObj.put("emotion", faceHolder.faceAttribute.emotion);
        JSONObject poseObj = new JSONObject();
        poseObj.put("pitch", faceHolder.faceAttribute.pose.pitch);
        poseObj.put("roll", faceHolder.faceAttribute.pose.roll);
        poseObj.put("yaw", faceHolder.faceAttribute.pose.yaw);
        faceAttributeObj.put("pose", poseObj);
        JSONObject faceInfoObj = new JSONObject();
        JSONObject boundingBoxObj = new JSONObject();
        boundingBoxObj.put("left", faceHolder.faceInfo.boundingBox.left);
        boundingBoxObj.put("top", faceHolder.faceInfo.boundingBox.top);
        boundingBoxObj.put("right", faceHolder.faceInfo.boundingBox.right);
        boundingBoxObj.put("bottom", faceHolder.faceInfo.boundingBox.bottom);
        faceInfoObj.put("boundingBox", boundingBoxObj);
        JSONObject bitmapInfoObj = new JSONObject();
        bitmapInfoObj.put("width", maxFrameWidth);
        bitmapInfoObj.put("height", maxFrameHeight);
        faceInfoObj.put("bitmap", bitmapInfoObj);
        faceInfoObj.put("confidence", faceHolder.faceInfo.confidence);

        faceHolderObj.put("faceAttribute", faceAttributeObj);
        faceHolderObj.put("faceInfo", faceInfoObj);
      }
    return faceHolderObj;
  }

  private JSONObject extractBitmap(Bitmap bitmap) throws JSONException{
    int facesCount = recognizer.extractFace(extractConfig, Collections.singletonList(bitmap));
    ArrayList<FaceHolder> faces = new ArrayList<>();
    JSONObject jsonObjectFaceHolder = null;
    if (facesCount > 0) {
      for (int faceIndex = 0; faceIndex < facesCount; faceIndex++) {
        FaceInfo faceInfo = recognizer.getFaceInfo(0, faceIndex);
        FaceLandmark faceLandmark = recognizer.getFaceLandmark(0, faceIndex);
        FaceAttribute faceAttr = recognizer.getFaceAttribute(0, faceIndex);
        FaceFeature faceFeature = recognizer.getFaceFeature(0, faceIndex);

        Bitmap faceBitmap = getCropFaceBitmap(bitmap, faceInfo.boundingBox);

        FaceHolder holder = new FaceHolder(faceInfo, faceLandmark, faceAttr, faceFeature, faceBitmap);
        faces.add(holder);
      }
      jsonObjectFaceHolder = convertToJsonArray(faces);
    }
    return jsonObjectFaceHolder;
  }

  // CONFIGURE BITMAP IMAGE DISPLAY
  public Bitmap configBitmap(Bitmap bitmap) {
    Matrix matrix = new Matrix();
    matrix.postRotate(-90);

    // Create a new flipped bitmap
    matrix.setScale(-1, 1);
    Bitmap flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

    return flippedBitmap;
  }

  private Bitmap getCropFaceBitmap(Bitmap source, Rect faceRect) {
    Size maxSize = new Size(source.getWidth(), source.getHeight());
    Rect enlargeRect = RectUtil.enlargeRect(faceRect, maxSize, 0.25f);
    Rect squareRect = RectUtil.squareRect(enlargeRect, maxSize);
    return Bitmap.createBitmap(source, squareRect.left, squareRect.top, squareRect.width(), squareRect.height());
  }

  private void releaseRecognizer(){
    if(recognizer != null){
      recognizer.release();
      recognizer = null;
    }
  }

  private void configure(){
    if(recognizer == null){
      return;
    }

    recognizer.setExtractionOption(
      ExtractionOption.DETECTION_SPEED_LEVEL, DetectionSpeedLevel.PRECISE);
    recognizer.setExtractionOption(
      ExtractionOption.DETECTION_OUTPUT_ORDER,
      DetectionOutputOrder.CONFIDENCE);
    recognizer.setExtractionOption(
      ExtractionOption.DETECTION_MODE, DetectionMode.NORMAL);

    extractConfig = new ExtractConfig();
    extractConfig.extractBoundingBox = true;
    extractConfig.extractFeatureLandmark = true;
    extractConfig.extractFeature = true;
    extractConfig.extractAge = true;
    extractConfig.extractGender = true;
    extractConfig.extractEmotion = true;
    extractConfig.extractPose = true;
    extractConfig.extractOcclusion = true;
  }

  private void initializeRecognizer(){
    int result;
    try {
      releaseRecognizer();

      recognizer = new FaceMeRecognizer();
      RecognizerConfig recognizerConfig = new RecognizerConfig();
      recognizerConfig.preference = EnginePreference.PREFER_NONE;
      recognizerConfig.detectionModelSpeedLevel = DetectionModelSpeedLevel.DEFAULT;
      recognizerConfig.maxDetectionThreads = 2;
      recognizerConfig.extractionModelSpeedLevel = ExtractionModelSpeedLevel.VH6;
      recognizerConfig.maxExtractionThreads = 2;
      recognizerConfig.mode = RecognizerMode.IMAGE;
      recognizerConfig.maxFrameHeight = maxFrameHeight;
      recognizerConfig.maxFrameWidth = maxFrameWidth;
      recognizerConfig.minFaceWidthRatio = 0.05f;
      result = recognizer.initializeEx(recognizerConfig);
      if (result < 0) throw new IllegalStateException("Initialize recognizer failed: " + result);
      configure();
    } catch (Exception e) {
      releaseRecognizer();
      throw e;
    }
  }

  private Bitmap base64ToBitmap(String base64Image) {
    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    return configBitmap(bitmap);
  }

}
