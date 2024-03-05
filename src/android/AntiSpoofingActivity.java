package inc.bastion.faceme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cyberlink.faceme.DetectionMode;
import com.cyberlink.faceme.DetectionModelSpeedLevel;
import com.cyberlink.faceme.DetectionOutputOrder;
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
import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.LicenseOption;
import com.cyberlink.faceme.PrecisionLevel;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.SimilarFaceResult;
import com.cyberlink.faceme.SpeechFeature;
import com.cyberlink.faceme.widget.AntiSpoofingCallbackV2;
import com.cyberlink.faceme.widget.AntiSpoofingConfig;
import com.cyberlink.faceme.widget.AntiSpoofingFragment;
import com.cyberlink.faceme.widget.LocalizedKey;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AntiSpoofingActivity extends Fragment implements AntiSpoofingCallbackV2{
  public interface AntiSpoofingListener{
    void onFaceDetection(int result);
    void onFaceEnroll(FaceHolder enrollFace) throws JSONException;
  }
  private AntiSpoofingListener eventListener;
  private View view;
  private String appResourcePackage;
  private  FaceHolder _faceHolder;

  //private static final String LICENSE_KEY = "gaa6ER882dGwuu2OB4YhGQoh5CU7A89IYzsZC5cS";
  private static final int PERMISSION_REQUEST_CODE = 12345;
  private final String TAG = "AntiSpoofingActivity";
  private AntiSpoofingFragment asFragment;
  private float confidenceThreshold;
  private FaceMeRecognizer _recognizer;
  private ExtractConfig _extractConfig;
  private FaceMeDataManager _dataManager;
  private Boolean hasRecognitionFeature;
  private ExecutorService sdkThread;
  private int frameWidth = 720;
  private int frameHeight = 1280;
  protected final Handler mainHandler = new Handler(Looper.getMainLooper());

  private View viewCenterAnchor;
  private View resultLayoutView;
  private TextView txtResultTitle;
  private TextView txtResultSubtitle;

  public inc.bastion.faceme.AntiSpoofingConfig asConfig;
  public String LICENSE_KEY;

  private static final long RESULT_FROZEN_PERIOD = 3000L;

  public void setEventListener(AntiSpoofingListener listener){
    eventListener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    appResourcePackage = getActivity().getPackageName();

    view = inflater.inflate(getResources().getIdentifier("activity_anti_spoofing", "layout", appResourcePackage), container, false);

    initSdkComponents();
    int asFragmentId = getActivity().getResources().getIdentifier("fm_antispoofing_fragment", "id", appResourcePackage);
    asFragment = (AntiSpoofingFragment) getChildFragmentManager().findFragmentById(asFragmentId);
    if (asFragment != null) {
      asFragment.setAntiSpoofingCallback(this);
    } else {
      // Handle the case where the fragment was not found or not yet added
    }

    initDetectSetting();
    initLayoutSetting();
    initResultUi();

    int permissionStatus = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA);
    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
      asFragment.startDetection();
    } else {
      requestPermissions((new String[]{android.Manifest.permission.CAMERA}), PERMISSION_REQUEST_CODE);
    }

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    if(asFragment != null){
      getChildFragmentManager().beginTransaction().remove(asFragment);
      asFragment = null;
    }
  }


  private void initSdkComponents() {
    if (getActivity().isFinishing() || getActivity().isDestroyed()) return;

    if (sdkThread == null) {
      sdkThread = Executors.newSingleThreadExecutor();
    }
    sdkThread.submit(this::initSdkEngine);
  }

  @WorkerThread
  private void initSdkEngine() {
    releaseSdkEngine();

    try {
      checkLicense();
      initRecognizer();
      initDataManager();
    } catch (Exception e) {
      Log.d(TAG,"Initialize SDK failed:\n" + e.getMessage());
    }
  }

  private void checkLicense() {
    Context context = getActivity().getApplicationContext();
    FaceMeSdk.initialize(context, LICENSE_KEY);

    LicenseManager licenseManager = new LicenseManager();
    int result = licenseManager.initializeEx();
    result = licenseManager.registerLicense();
    licenseManager.release();
  }

  private void initRecognizer(){
    FaceMeRecognizer recognizer = null;
    int result;
    try {
      recognizer = new FaceMeRecognizer();
      RecognizerConfig recognizerConfig = new RecognizerConfig();
      recognizerConfig.preference = EnginePreference.PREFER_NONE;
      recognizerConfig.detectionModelSpeedLevel = DetectionModelSpeedLevel.DEFAULT;
      recognizerConfig.maxDetectionThreads = 2;
      recognizerConfig.extractionModelSpeedLevel = ExtractionModelSpeedLevel.VH6;
      recognizerConfig.maxExtractionThreads = 2;
      recognizerConfig.mode = RecognizerMode.IMAGE;
      recognizerConfig.maxFrameHeight = frameHeight;
      recognizerConfig.maxFrameWidth = frameWidth;
      recognizerConfig.minFaceWidthRatio = 0.05f;
      result = recognizer.initializeEx(recognizerConfig);
      if (result < 0) throw new IllegalStateException("Initialize recognizer failed: " + result);

      recognizer.setExtractionOption(ExtractionOption.DETECTION_OUTPUT_ORDER, DetectionOutputOrder.CONFIDENCE);
      recognizer.setExtractionOption(ExtractionOption.DETECTION_MODE, DetectionMode.NORMAL);

      _recognizer = recognizer;
      confidenceThreshold = recognizer.getFeatureScheme().threshold_1_1e6;

      _extractConfig = new ExtractConfig();
      _extractConfig.extractBoundingBox = true;
      _extractConfig.extractFeatureLandmark = true;
      _extractConfig.extractFeature = true;
      _extractConfig.extractAge = true;
      _extractConfig.extractGender = true;
      _extractConfig.extractEmotion = true;
      _extractConfig.extractPose = true;
      _extractConfig.extractOcclusion = true;
    } catch (Exception e) {
      if (recognizer != null) recognizer.release();
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
  @WorkerThread
  private void releaseSdkEngine() {
    hasRecognitionFeature = false;

    if (_recognizer != null) {
      _recognizer.release();
      _recognizer = null;
    }
    if (_dataManager != null) {
      _dataManager.release();
      _dataManager = null;
    }
  }

  private void initDetectSetting(){
    AntiSpoofingConfig.DetectSetting detectSetting = new AntiSpoofingConfig.DetectSetting();

    detectSetting.userInteractionEnabled = AntiSpoofingConfig.INTERACTION_OFF;
    detectSetting.raNodEnable = false;
    detectSetting.raSmileEnable = false;
    detectSetting.speechEnable = false;
    detectSetting.enableVibrateAfterActionComplete = true;

    asFragment.setDetectSetting(detectSetting);
  }

  private void initLayoutSetting(){
    AntiSpoofingConfig.LayoutSetting layoutSetting = new AntiSpoofingConfig.LayoutSetting();

    // XXX: Customize your own configurations to meet your application scenario.
    //layoutSetting.logo = getDrawable(R.drawable.fm_ic_app_logo);

    // XXX: Localize your own string.
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SUBTITLE, getString(getActivity().getResources().getIdentifier("demo_fm_2das_subtitle", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_CENTER_FACE, getString(getActivity().getResources().getIdentifier("demo_fm_2das_center_your_face_look_straight", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_DONT_MOVE, getString(getActivity().getResources().getIdentifier("demo_fm_2das_processing", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_COME_CLOSER, getString(getActivity().getResources().getIdentifier("demo_fm_2das_face_come_closer", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_GO_FARTHER, getString(getActivity().getResources().getIdentifier("demo_fm_2das_face_go_farther", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_PROCESSING, getString(getActivity().getResources().getIdentifier("demo_fm_2das_processing", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_DARK_ENVIRONMENT, getString(getActivity().getResources().getIdentifier("demo_fm_2das_alert_env_dark", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_DARK_ENVIRONMENT_DETAIL, getString(getActivity().getResources().getIdentifier("demo_fm_2das_alert_env_dark_desc", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_LEFT, getString(getActivity().getResources().getIdentifier("demo_fm_2das_turn_left", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_LEFT_DETAIL, getString(getActivity().getResources().getIdentifier("demo_fm_2das_turn_left_desc", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_RIGHT, getString(getActivity().getResources().getIdentifier("demo_fm_2das_turn_right", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_RIGHT_DETAIL, getString(getActivity().getResources().getIdentifier("demo_fm_2das_turn_right_desc", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SMILE, getString(getActivity().getResources().getIdentifier("demo_fm_2das_smile", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SMILE_DETAIL, getString(getActivity().getResources().getIdentifier("demo_fm_2das_smile_desc", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_NOD_HEAD, getString(getActivity().getResources().getIdentifier("demo_fm_2das_nod", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_NOD_HEAD_DETAIL, getString(getActivity().getResources().getIdentifier("demo_fm_2das_nod_desc", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH, getString(getActivity().getResources().getIdentifier("demo_fm_2das_speech", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH_DETAIL, getString(getActivity().getResources().getIdentifier("demo_fm_2das_speech_desc", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH_LANGUAGE_MANDARIN, getString(getActivity().getResources().getIdentifier("demo_fm_2das_speech_language_mandarin", "string", appResourcePackage)));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH_LANGUAGE_ENGLISH, getString(getActivity().getResources().getIdentifier("demo_fm_2das_speech_language_english", "string", appResourcePackage)));

//    Typeface exoBold = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/font/Exo 2/Exo2-Bold.ttf");
//    Typeface exoRegular = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/font/Exo 2/Exo2-Regular.ttf");

    Typeface tfActionHintFont = null;
    Typeface tfActionDetailHintFont = null;
    Typeface tfAlertDescriptionFont = null;
    Typeface tfAlertTitleFont = null;
    Typeface tfFooterTitleFont = null;
    Typeface tfFooterSubtitleFont = null;
    Typeface tfUserActionHintFont = null;

    if(asConfig.actionHintFont != "null"){
      tfActionHintFont = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/" + asConfig.actionHintFont);
    }
    if(asConfig.actionDetailHintFont != "null"){
      tfActionDetailHintFont = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/" + asConfig.actionDetailHintFont);
    }
    if(asConfig.alertDescriptionFont != "null"){
      tfAlertDescriptionFont = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/" + asConfig.alertDescriptionFont);
    }
    if(asConfig.alertTitleFont != "null"){
      tfAlertTitleFont = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/" + asConfig.alertTitleFont);
    }
    if(asConfig.footerTitleFont != "null"){
      tfFooterTitleFont = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/" + asConfig.footerTitleFont);
    }
    if(asConfig.footerSubtitleFont != "null"){
      tfFooterSubtitleFont = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/" + asConfig.footerSubtitleFont);
    }
    if(asConfig.userActionHintFont != "null"){
      tfUserActionHintFont = Typeface.createFromAsset(getActivity().getAssets(), "www/assets/" + asConfig.userActionHintFont);
    }

    int IDLE_COLOR = Color.WHITE;
    int ACTIVE_COLOR = Color.rgb(255, 80, 25);
    float BORDER_FONT = 5.3f;

    layoutSetting.showFPS = asConfig.showFPS;

    layoutSetting.frameIdleColor = asConfig.frameIdleColor;
    layoutSetting.frameActiveColor = asConfig.frameActiveColor;
    layoutSetting.frameBorderWidth = asConfig.frameBorderWidth;
    layoutSetting.showFrame = asConfig.showFrame;

    layoutSetting.circleIdleColor = asConfig.circleIdleColor;
    layoutSetting.circleActiveColor = asConfig.circleActiveColor;
    layoutSetting.circleBorderWidth = asConfig.circleBorderWidth;

    layoutSetting.actionDetailHintActiveColor = asConfig.actionDetailHintActiveColor;
    layoutSetting.actionDetailHintIdleColor = asConfig.actionDetailHintIdleColor;
    layoutSetting.actionDetailHintFont = tfActionDetailHintFont;
    layoutSetting.actionDetailHintFontSize = asConfig.actionDetailHintFontSize;

    layoutSetting.actionHintColor = asConfig.actionHintColor;
    layoutSetting.actionHintFont = tfActionHintFont;
    layoutSetting.actionHintFontSize = asConfig.actionHintFontSize;

    layoutSetting.progressBarForegroundColor = asConfig.progressBarForegroundColor;
    layoutSetting.progressBarBackgroundColor = asConfig.progressBarBackgroundColor;
    layoutSetting.progressBarWidth = asConfig.progressBarWidth;
    layoutSetting.progressBarHeight = asConfig.progressBarHeight;

    layoutSetting.footerTitleColor = asConfig.footerTitleColor;
    layoutSetting.footerTitleFontSize = asConfig.footerTitleFontSize;
    layoutSetting.footerTitleFont = tfFooterTitleFont;
    layoutSetting.footerSubtitleColor = asConfig.footerSubtitleColor;
    layoutSetting.footerSubtitleFontSize = asConfig.footerSubtitleFontSize;
    layoutSetting.footerSubtitleFont = tfFooterSubtitleFont;
    layoutSetting.showFooter = asConfig.showFooter;

    layoutSetting.userActionHintColor = asConfig.userActionHintColor;
    layoutSetting.userActionHintFontSize = asConfig.userActionHintFontSize;
    layoutSetting.userActionHintFont = tfUserActionHintFont;
    layoutSetting.showUserActionSteps = asConfig.showUserActionSteps;

    layoutSetting.alertDistanceToCircle = asConfig.alertDistanceToCircle;

    layoutSetting.alertBackgroundColor = asConfig.alertBackgroundColor;

    layoutSetting.alertTitleColor = asConfig.alertTitleColor;
    layoutSetting.alertTitleFont = tfAlertTitleFont;
    layoutSetting.alertTitleFontSize = asConfig.alertTitleFontSize;

    layoutSetting.alertDescriptionColor = asConfig.alertDescriptionColor;
    layoutSetting.alertDescriptionFont = tfAlertDescriptionFont;
    layoutSetting.alertDescriptionFontSize = asConfig.alertDescriptionFontSize;

    asFragment.setLayoutSetting(layoutSetting);
  }

  private void initResultUi(){
    int viewCenterAnchor_id = getActivity().getResources().getIdentifier("viewCenterAnchor", "id", appResourcePackage);
    viewCenterAnchor = view.findViewById(viewCenterAnchor_id);
    int resultLayoutView_id = getActivity().getResources().getIdentifier("resultLayoutView", "id", appResourcePackage);
    resultLayoutView = view.findViewById(resultLayoutView_id);
    int resultTitle_id = getActivity().getResources().getIdentifier("txtResultTitle", "id", appResourcePackage);
    txtResultTitle = view.findViewById(resultTitle_id);
    int resultSubtitle_id = getActivity().getResources().getIdentifier("txtResultSubtitle", "id", appResourcePackage);
    txtResultSubtitle = view.findViewById(resultSubtitle_id);
  }

  private final Runnable finishResultPage = this::restartDetection;

  @SuppressLint("MissingPermission")
  private void restartDetection(){
    mainHandler.removeCallbacks(finishResultPage);
    resultLayoutView.setVisibility(View.GONE);
    if(asFragment != null){
      asFragment.startDetection();
    }
  }

  @Override
  public void onPreviewSizeChanged(int i, int i1, float v, float v1) {

  }

  @Override
  public void onAntiSpoofingError(int i) {

  }

  @Override
  public void onAntiSpoofingResult(Bitmap bitmap, FaceInfo face, int result, double v, int i1, SpeechFeature speechFeature) {
    txtResultTitle.setText("");
    resultLayoutView.setVisibility(View.VISIBLE);
    txtResultSubtitle.setVisibility(View.GONE);
    try {
      extractFaceFromImage(bitmap);
      FaceHolder holder = _faceHolder;
      if(checkSimilarFace(holder)){
        if(asConfig.faceEnroll){
          eventListener.onFaceEnroll(null);
        }else if(asConfig.faceDetection){
          eventListener.onFaceDetection(1);
        }
      }else{
        if(result == AntiSpoofingCallbackV2.UI_RESULT_SHAKEN){
          txtResultTitle.setText(getString(getActivity().getResources().getIdentifier("demo_fm_2das_result_shaken", "string", appResourcePackage)));
        }else if(result == AntiSpoofingCallbackV2.UI_RESULT_SPOOFING){
          txtResultTitle.setText(getString(getActivity().getResources().getIdentifier("demo_fm_2das_result_spoofing", "string", appResourcePackage)));
        }else{
          if(asConfig.faceEnroll){
            eventListener.onFaceEnroll(holder);
          }else if(asConfig.faceDetection){
            eventListener.onFaceDetection(0);
          }
        }
      }
      mainHandler.removeCallbacks(finishResultPage);
      mainHandler.postDelayed(finishResultPage, RESULT_FROZEN_PERIOD);
      asFragment.enableTestDump(false);
    }catch (JSONException e){
      e.printStackTrace();
    }
  }
  private Bitmap getCropFaceBitmap(Bitmap source, Rect faceRect) {
    Size maxSize = new Size(source.getWidth(), source.getHeight());
    Rect enlargeRect = RectUtil.enlargeRect(faceRect, maxSize, 0.25f);
    Rect squareRect = RectUtil.squareRect(enlargeRect, maxSize);
    return Bitmap.createBitmap(source, squareRect.left, squareRect.top, squareRect.width(), squareRect.height());
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

  private void extractFaceFromImage(Bitmap bitmap){
    int facesCount = _recognizer.extractFace(_extractConfig, Collections.singletonList(bitmap));
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
}
