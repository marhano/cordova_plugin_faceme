package inc.bastion.faceme;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.cyberlink.faceme.PrecisionLevel;
import com.cyberlink.faceme.QueryResult;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.ReturnCode;
import com.cyberlink.faceme.SimilarFaceResult;

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

  private static final String GET_BASE64_IMAGE = "getBase64Image";
  private static final String GET_BOUNDING_BOX = "getBoundingBox";
  private static final String GET_BITMAP_IMAGE = "getBitmapImage";
  private static final String ACTIVATE_LICENSE = "activateLicense";
  private static final String DEACTIVATE_LICENSE = "deactivateLicense";

  //INITIAL METHODS FOR PROTOTYPE
  private static final String TEST_PLUGIN = "testPlugin";
  private static final String INITIALIZE_SDK = "initializeSDK";
  private static final String DETECT_FACE = "detectFace";
  private static final String ENROLL_FACE = "enrollFace";
  private static final String RECOGNIZE_FACE = "recognizeFace";
  private static final String DELETE_FACE = "deleteFace";
  private static final String UPDATE_FACE = "updateFace";
  private static final String SELECT_FACE = "selectFace";

  private CallbackContext execCallback;
  private JSONArray execArgs;

  private FaceHolder _faceHolder;
  private FaceMeDataManager _dataManager = null;
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
      String username = args.getString(0);
      JSONObject faceHolder = args.getJSONObject(1);
      return enrollFace(username, faceHolder, callbackContext);
    }else if(RECOGNIZE_FACE.equals(action)){
      return recognizeFace(callbackContext);
    }else if(DELETE_FACE.equals(action)){
      long faceId = args.getLong(0);
      return deleteFace(faceId, callbackContext);
    }else if(UPDATE_FACE.equals(action)){
      return updateFace(callbackContext);
    }else if(SELECT_FACE.equals(action)){
      return selectFace(callbackContext);
    }
    return false;
  }

  private boolean testPlugin(CallbackContext callbackContext){
    callbackContext.success("PLUGIN TEST ABCD 1234");

    return true;
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

    JSONObject jsonObjectFaceHolder = extractFace(bitmap);

    if(isLicenseActivated == true && jsonObjectFaceHolder != null){
      callbackContext.success(jsonObjectFaceHolder);
    }
    return true;
  }

  private boolean enrollFace(String username, JSONObject faceHolder, CallbackContext callbackContext) throws JSONException{
    FaceMeDataManager dataManager = new FaceMeDataManager();
    int result = dataManager.initializeEx(recognizer.getFeatureScheme());
    if (result < 0) throw new IllegalStateException("Initialize data manager failed: " + result);
    _dataManager = dataManager;

    FaceHolder holder = _faceHolder;
    updateFaceHolder(holder);
    JSONObject jsonObjectFaceHolder = convertToJsonArray(holder);
    //updateFace(holder, username);
    callbackContext.success(jsonObjectFaceHolder);
    return true;
  }

  private boolean recognizeFace(CallbackContext callbackContext){
    FaceMeDataManager dataManager = new FaceMeDataManager();
    int result = dataManager.initializeEx(recognizer.getFeatureScheme());
    if (result < 0) throw new IllegalStateException("Initialize data manager failed: " + result);
    _dataManager = dataManager;

    long collectionId = 0;
    QueryResult queryResult = dataManager.queryFaceCollection(0, 1);
    if(queryResult != null || queryResult.resultIds.isEmpty()){
      collectionId = queryResult.resultIds.get(0);
      QueryResult queryFace = dataManager.queryFace(collectionId, 0, 1);
      long faceId = queryFace.resultIds.get(0);
      FaceFeature faceFeature = dataManager.getFaceFeature(faceId);
      Log.d(TAG, "FaceFeature");
    }
    FaceHolder holder = _faceHolder;
    if(updateFaceHolder(holder)){
      callbackContext.success("true");
    }
    return true;
  }

  private boolean deleteFace(long faceId, CallbackContext callbackContext){
    callbackContext.success("Delete Face");
    
    return true;
  }

  private boolean updateFace(CallbackContext callbackContext){
    callbackContext.success("Update Face");
    
    return true;
  }
  
  private boolean selectFace(CallbackContext callbackContext){
    callbackContext.success("Select Face");
    
    return true;
  }

  private boolean updateFaceHolder(FaceHolder faceHolder){
    if(_dataManager == null) return false;
    float confidenceThreshold = _dataManager.getPrecisionThreshold(PrecisionLevel.LEVEL_1E6);

    List<SimilarFaceResult> searchResult = _dataManager.searchSimilarFace(confidenceThreshold, -1, faceHolder.faceFeature, 1);
    if(searchResult != null && !searchResult.isEmpty()){
      SimilarFaceResult result = searchResult.get(0);
      faceHolder.data.collectionId = result.collectionId;
      faceHolder.data.faceId = result.faceId;
      faceHolder.data.confidence = result.confidence;
    }

    if(faceHolder.data.collectionId > 0){
      faceHolder.data.name = _dataManager.getFaceCollectionName(faceHolder.data.collectionId);
      return true;
    }
    return false;
  }

  private void updateFaces(FaceHolder faceHolder, String name){
    if(_dataManager == null) return;
    FaceMeDataManager dataManager = _dataManager;
    if (dataManager == null) {
      Log.w(TAG, " > addFace: data manager unavailable");
      return;
    }
    long collectionId = 0;
    //QueryResult queryCollectionResult = dataManager.queryFaceCollection(0, 5);
    //collectionId = queryCollectionResult.resultIds.get(0);
    //QueryResult faceId = dataManager.queryFace(collectionId, 0, 1);
    if(faceHolder.data.collectionId >= 0){
      collectionId = faceHolder.data.collectionId;
      boolean success = updateFaceName(collectionId, name);
      if (!success) {
        Log.w(TAG, " > addFace: update existing collectionName: " + faceHolder.data.name + " to " + name);
        return;
      }
    }else {
      QueryResult queryCollectionResult = dataManager.queryFaceCollectionByName(name, 0, 1);
      if (queryCollectionResult == null || queryCollectionResult.resultIds.isEmpty()) {
        collectionId = dataManager.createFaceCollection(name);
        if (collectionId <= 0) {
          if (ReturnCode.DATABASE_COLLECTION_EXCEEDED == collectionId) {
            Log.d(TAG, "DB is already full!");
          } else {
            Log.d(TAG, "Something error when create new collection in DB: " + collectionId);
          }
          return;
        }
        Log.v(TAG, " > addFace: create new collection: " + collectionId);
      } else {
        collectionId = queryCollectionResult.resultIds.get(0);
        Log.v(TAG, " > addFace: collection found: " + collectionId);
      }
    }

    long faceId = dataManager.addFace(collectionId, faceHolder.faceFeature);
    if (faceId <= 0) {
      Log.e(TAG, " > addFace: but failed. Error code: " + faceId);
      return;
    }

    Log.v(TAG, " > addFace: faceId: " + faceId);
    faceHolder.data.collectionId = collectionId;
    faceHolder.data.faceId = faceId;
    faceHolder.data.name = name;
    faceHolder.data.confidence = 1F;
  }

  private boolean updateFaceName(long collectionId, String newName){
    FaceMeDataManager dataManager = _dataManager;
    if(dataManager == null){
      Log.w(TAG, " > addFace: data manager unavailable");
      return false;
    }

    return dataManager.setFaceCollectionName(collectionId, newName);
  }

  private void getAllFaceCollection(FaceHolder faceHolder){

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

  private JSONObject convertToJsonArray(FaceHolder faceHolder) throws JSONException{
    JSONObject faceHolderObj = new JSONObject();
    JSONObject faceAttributeObj = new JSONObject();
    JSONObject poseObj = new JSONObject();
    JSONObject faceInfoObj = new JSONObject();
    JSONObject boundingBoxObj = new JSONObject();
    JSONObject bitmapInfoObj = new JSONObject();

    faceAttributeObj.put("age", faceHolder.faceAttribute.age);
    faceAttributeObj.put("emotion", faceHolder.faceAttribute.emotion);
    faceAttributeObj.put("gender", faceHolder.faceAttribute.gender);
    faceAttributeObj.put("emotion", faceHolder.faceAttribute.emotion);

    poseObj.put("pitch", faceHolder.faceAttribute.pose.pitch);
    poseObj.put("roll", faceHolder.faceAttribute.pose.roll);
    poseObj.put("yaw", faceHolder.faceAttribute.pose.yaw);
    faceAttributeObj.put("pose", poseObj);

    boundingBoxObj.put("left", faceHolder.faceInfo.boundingBox.left);
    boundingBoxObj.put("top", faceHolder.faceInfo.boundingBox.top);
    boundingBoxObj.put("right", faceHolder.faceInfo.boundingBox.right);
    boundingBoxObj.put("bottom", faceHolder.faceInfo.boundingBox.bottom);
    faceInfoObj.put("boundingBox", boundingBoxObj);

    bitmapInfoObj.put("width", maxFrameWidth);
    bitmapInfoObj.put("height", maxFrameHeight);
    faceInfoObj.put("bitmap", bitmapInfoObj);
    faceInfoObj.put("confidence", faceHolder.faceInfo.confidence);

    String croppedFace = bitmapToBase64(faceHolder.faceBitmap);

    faceHolderObj.put("faceAttribute", faceAttributeObj);
    faceHolderObj.put("faceInfo", faceInfoObj);
    faceHolderObj.put("faceBitmap", croppedFace);

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
      //jsonObjectFaceHolder = convertToJsonArray(faces);
    }
    return jsonObjectFaceHolder;
  }

  private JSONObject extractFace(Bitmap bitmap) throws  JSONException{
    int facesCount = recognizer.extractFace(extractConfig, Collections.singletonList(bitmap));
    JSONObject jsonObjectFaceHolder = null;
    if (facesCount > 0) {
      FaceInfo faceInfo = recognizer.getFaceInfo(0, 0);
      FaceLandmark landmark = recognizer.getFaceLandmark(0, 0);
      FaceAttribute attribute = recognizer.getFaceAttribute(0, 0);
      FaceFeature faceFeature = recognizer.getFaceFeature(0, 0);

      Bitmap faceImage = getCropFaceBitmap(bitmap, faceInfo.boundingBox);
      FaceHolder face = new FaceHolder(faceInfo, landmark, attribute, faceFeature, faceImage);
      _faceHolder = face;
      jsonObjectFaceHolder = convertToJsonArray(face);
      return jsonObjectFaceHolder;
    }
    return null;
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

  private String bitmapToBase64(Bitmap bitmapImage) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
    byte[] imageBytes = baos.toByteArray();
    return Base64.encodeToString(imageBytes, Base64.DEFAULT);
  }

}
