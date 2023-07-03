package inc.bastion.faceme;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.util.Base64;
import android.content.Context;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Size;
import android.graphics.BitmapFactory;

import androidx.annotation.WorkerThread;

public class FaceMe extends CordovaPlugin {
  private static final String TAG = "FaceMe";

  private static final String LICENSE_KEY = "olJ5ziHGlU3FIHWZhhCAq27xJ70q4aMx1lVTK8TI";
  private boolean isLicenseActivated = false;
  private ExecutorService sdkThread;

  //INITIAL METHODS FOR PROTOTYPE
  private static final String TEST_PLUGIN = "testPlugin";
  private static final String INITIALIZE_SDK = "initializeSDK";
  private static final String ACTIVATE_LICENSE = "activateLicense";
  private static final String DEACTIVATE_LICENSE = "deactivateLicense";
  private static final String DETECT_FACE = "detectFace";
  private static final String ENROLL_FACE = "enrollFace";
  private static final String RECOGNIZE_FACE = "recognizeFace";
  private static final String DELETE_FACE = "deleteFace";
  private static final String UPDATE_FACE = "updateFace";
  private static final String SELECT_FACE = "selectFace";
  private static final String ADD_FACE = "addFace";
  private static final String START_ANTI_SPOOFING = "startAntiSpoofing";

  private CallbackContext startAntiSpoofingCallbackContext;
  private static final int ANTI_SPOOFING_REQUEST_CODE = 123;

  private FaceHolder _tempHolder;
  private FaceHolder _faceHolder;
  private FaceMeDataManager _dataManager = null;
  private FaceMeRecognizer _recognizer = null;
  private ExtractConfig extractConfig = null;

  private int maxFrameHeight = 1280;
  private int maxFrameWidth = 720;

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
    }else if(ACTIVATE_LICENSE.equals(action)){
      cordova.getThreadPool().execute(() -> activateLicense(callbackContext));
      return true;
    }else if(DEACTIVATE_LICENSE.equals(action)){
      cordova.getThreadPool().execute(() -> deactivateLicense(callbackContext));
      return true;
    }else if(DETECT_FACE.equals(action)){
      String base64Image = args.getString(0);
      detectFace(base64Image, callbackContext);
      return true;
    }else if(ENROLL_FACE.equals(action)){
      String base64Image = args.getString(0);
      cordova.getThreadPool().execute(() -> {
        try {
          enrollFace(base64Image, callbackContext);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      });
      return true;
    }else if(RECOGNIZE_FACE.equals(action)){
      return recognizeFace(callbackContext);
    }else if(DELETE_FACE.equals(action)){
      long faceId = args.getLong(0);
      return deleteFace(faceId, callbackContext);
    }else if(UPDATE_FACE.equals(action)){
      return updateFace(callbackContext);
    }else if(SELECT_FACE.equals(action)){
      return selectFace(callbackContext);
    }else if(ADD_FACE.equals(action)){
      String username = args.getString(0);
      return addFace(username, callbackContext);
    }else if(START_ANTI_SPOOFING.equals(action)){
      return startAntiSpoofing(callbackContext);
    }
    return false;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == ANTI_SPOOFING_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        boolean hasSimilarFace = data.getBooleanExtra("hasSimilarFace", false);
        startAntiSpoofingCallbackContext.success(hasSimilarFace ? 1 : 0); // Return 1 for true, 0 for false
      } else {
        startAntiSpoofingCallbackContext.error("Anti-spoofing activity result canceled or failed");
      }
    }
  }

  private boolean testPlugin(CallbackContext callbackContext){
    startAntiSpoofingCallbackContext = callbackContext;

    cordova.getActivity().runOnUiThread(() -> {
      Intent intent = new Intent(cordova.getActivity(), AntiSpoofingActivity.class);
      cordova.startActivityForResult(this, intent, ANTI_SPOOFING_REQUEST_CODE);
    });

    return true;
  }

  private boolean startAntiSpoofing(CallbackContext callbackContext){
    Intent intent = new Intent(cordova.getActivity(), AntiSpoofingActivity.class);
    cordova.getActivity().startActivity(intent);

    return true;
  }

  private boolean initializeSDK(CallbackContext callbackContext){
    Context context = this.cordova.getActivity().getApplicationContext();
    FaceMeSdk.initialize(context, LICENSE_KEY);

    LicenseManager licenseManager = new LicenseManager();
    int result = licenseManager.initializeEx();
    result = licenseManager.registerLicense();
    licenseManager.release();

    if(result == 0){
      initSdkComponents();
    }
    callbackContext.success(result);
    return true;
  }

  private void detectFace(String base64Image, CallbackContext callbackContext) throws JSONException{
    Bitmap bitmap = base64ToBitmap(base64Image);
    if(_recognizer == null || bitmap.getHeight() != maxFrameHeight || bitmap.getHeight() != maxFrameWidth){
      maxFrameHeight = bitmap.getHeight();
      maxFrameWidth = bitmap.getWidth();
      releaseRecognizer();
      initRecognizer();
    }
    JSONObject jsonObjectFaceHolder = extractFace(bitmap);

    if(isLicenseActivated && jsonObjectFaceHolder != null){
      callbackContext.success(jsonObjectFaceHolder);
    }
  }

  private void enrollFace(String base64Image, CallbackContext callbackContext) throws JSONException{
    Bitmap bitmap = base64ToBitmap(base64Image);
    if(_recognizer == null || bitmap.getHeight() != maxFrameHeight || bitmap.getHeight() != maxFrameWidth){
      maxFrameHeight = bitmap.getHeight();
      maxFrameWidth = bitmap.getWidth();
    }

    extractFaceFromImage(bitmap);
    FaceHolder holder = _faceHolder;

    if(holder == null){
      callbackContext.success(1);
      return;
    }

    if(checkSimilarFace(holder)){
      callbackContext.success(0);
    }else{
      _tempHolder = holder;
      JSONObject jsonObject = convertToJsonArray(holder);
      callbackContext.success(jsonObject);
    }
  }

  private boolean recognizeFace(CallbackContext callbackContext){
    long collectionId = 0;
    QueryResult queryResult = _dataManager.queryFaceCollection(0, 1);
    if(queryResult != null || queryResult.resultIds.isEmpty()){
      collectionId = queryResult.resultIds.get(0);
      QueryResult queryFace = _dataManager.queryFace(collectionId, 0, 1);
      long faceId = queryFace.resultIds.get(0);
      FaceFeature faceFeature = _dataManager.getFaceFeature(faceId);
      Log.d(TAG, "FaceFeature");
    }
    FaceHolder holder = _faceHolder;
    if(checkSimilarFace(holder)){
      callbackContext.success("true");
    }else{
      callbackContext.success("false");
    }
    return true;
  }

  private boolean deleteFace(long faceId, CallbackContext callbackContext){
    boolean success = _dataManager.deleteFaceCollection(faceId);
    if(success){
      callbackContext.success(1);
    }else{
      callbackContext.success(0);
    }

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

  private boolean addFace(String username, CallbackContext callbackContext) throws JSONException{
    FaceHolder holder = _tempHolder;
    updateFaces(holder, username);
    JSONObject faceHolderObject = convertToJsonArray(holder);
    _tempHolder = null;
    callbackContext.success(faceHolderObject);

    return true;
  }

  private boolean checkSimilarFace(FaceHolder faceHolder){
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

  private void deactivateLicense(CallbackContext callbackContext){
    Context context = this.cordova.getActivity().getApplicationContext();
    LicenseUtils licenseUtils = new LicenseUtils();
    FaceMeSdk.initialize(context, LICENSE_KEY);
    int isDeactivated = licenseUtils.deactivateLicense();
    if(isDeactivated == 0){
        isLicenseActivated = false;
    }
    callbackContext.success(isDeactivated);
  }

  private void activateLicense(CallbackContext callbackContext){
    Context context = this.cordova.getActivity().getApplicationContext();
    LicenseUtils licenseUtils = new LicenseUtils();
    FaceMeSdk.initialize(context, LICENSE_KEY);
    int isLicenseValid = licenseUtils.verifyLicense();
    if(isLicenseValid == 0){
      isLicenseActivated = true;
    }
    callbackContext.success(isLicenseValid);
  }

  private JSONObject convertToJsonArray(FaceHolder faceHolder) throws JSONException{
    JSONObject faceHolderObj = new JSONObject();
    JSONObject faceAttributeObj = new JSONObject();
    JSONObject poseObj = new JSONObject();
    JSONObject faceInfoObj = new JSONObject();
    JSONObject boundingBoxObj = new JSONObject();
    JSONObject bitmapInfoObj = new JSONObject();
    JSONObject faceDataObj = new JSONObject();

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

    faceDataObj.put("collectionId", faceHolder.data.collectionId);
    faceDataObj.put("confidence", faceHolder.data.confidence);
    faceDataObj.put("faceId", faceHolder.data.faceId);
    faceDataObj.put("name", faceHolder.data.name);
    faceDataObj.put("similarity", faceHolder.data.similarity);

    faceHolderObj.put("faceAttribute", faceAttributeObj);
    faceHolderObj.put("faceInfo", faceInfoObj);
    faceHolderObj.put("faceBitmap", croppedFace);
    faceHolderObj.put("faceData", faceDataObj);

    return faceHolderObj;
  }

  //Return JsonObject with "ALL" data
  private JSONObject extractFace(Bitmap bitmap) throws  JSONException{
    int facesCount = _recognizer.extractFace(extractConfig, Collections.singletonList(bitmap));
    JSONObject jsonObjectFaceHolder = null;
    if (facesCount > 0) {
      FaceInfo faceInfo = _recognizer.getFaceInfo(0, 0);
      FaceLandmark landmark = _recognizer.getFaceLandmark(0, 0);
      FaceAttribute attribute = _recognizer.getFaceAttribute(0, 0);
      FaceFeature faceFeature = _recognizer.getFaceFeature(0, 0);

      Bitmap faceImage = getCropFaceBitmap(bitmap, faceInfo.boundingBox);
      FaceHolder face = new FaceHolder(faceInfo, landmark, attribute, faceFeature, faceImage);
      _faceHolder = face;
      jsonObjectFaceHolder = convertToJsonArray(face);
      return jsonObjectFaceHolder;
    }
    return null;
  }

  private void extractFaceFromImage(Bitmap bitmap) throws  JSONException{
    int facesCount = _recognizer.extractFace(extractConfig, Collections.singletonList(bitmap));
    if (facesCount > 0) {
      FaceInfo faceInfo = _recognizer.getFaceInfo(0, 0);
      FaceLandmark landmark = _recognizer.getFaceLandmark(0, 0);
      FaceAttribute attribute = _recognizer.getFaceAttribute(0, 0);
      FaceFeature faceFeature = _recognizer.getFaceFeature(0, 0);

      Bitmap faceImage = getCropFaceBitmap(bitmap, faceInfo.boundingBox);
      FaceHolder face = new FaceHolder(faceInfo, landmark, attribute, faceFeature, faceImage);
      _faceHolder = face;
    }else{
      _faceHolder = null;
    }
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
    if(_recognizer != null){
      _recognizer.release();
      _recognizer = null;
    }
  }

  private void initSdkComponents(){
    if(sdkThread == null){
      sdkThread = Executors.newSingleThreadExecutor();
    }
    sdkThread.submit(this::initSdkEngine);
  }
  @WorkerThread
  private void initSdkEngine(){
    releaseSdkEngine();

    try{
      initRecognizer();
      initDataManager();
    }catch (Exception e){
      Log.d(TAG,"Initialize SDK failed:\n" + e.getMessage());
    }
  }

  @WorkerThread
  private void releaseSdkEngine(){
    if(_recognizer != null){
      _recognizer.release();
      _recognizer = null;
    }

    if(_dataManager != null){
      _dataManager.release();
      _dataManager = null;
    }
  }

  private void configExtraction(){
    if(_recognizer == null){
      return;
    }

    _recognizer.setExtractionOption(
      ExtractionOption.DETECTION_SPEED_LEVEL, DetectionSpeedLevel.PRECISE);
    _recognizer.setExtractionOption(
      ExtractionOption.DETECTION_OUTPUT_ORDER,
      DetectionOutputOrder.CONFIDENCE);
    _recognizer.setExtractionOption(
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

  private void initRecognizer(){
    int result;
    try {
      releaseRecognizer();

      _recognizer = new FaceMeRecognizer();
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
      result = _recognizer.initializeEx(recognizerConfig);
      if (result < 0) throw new IllegalStateException("Initialize recognizer failed: " + result);
      configExtraction();
    } catch (Exception e) {
      releaseRecognizer();
      throw e;
    }
  }

  private void initDataManager() {
    FaceMeDataManager dataManager = null;
    int result;
    try {
      dataManager = new FaceMeDataManager();
      result = dataManager.initializeEx(_recognizer.getFeatureScheme());
      if (result < 0) throw new IllegalStateException("Initialize data manager failed: " + result);

      _dataManager = dataManager;
    } catch (Exception e) {
      if (dataManager != null) dataManager.release();
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
