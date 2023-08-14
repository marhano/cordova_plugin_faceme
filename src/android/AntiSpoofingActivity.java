package inc.bastion.faceme;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

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
import com.cyberlink.faceme.QueryResult;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.SimilarFaceResult;
import com.cyberlink.faceme.SpeechFeature;
import com.cyberlink.faceme.widget.AntiSpoofingCallbackV2;
import com.cyberlink.faceme.widget.AntiSpoofingConfig;
import com.cyberlink.faceme.widget.AntiSpoofingFragment;
import com.cyberlink.faceme.widget.LocalizedKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.facemeptt.bastion.R;

public class AntiSpoofingActivity extends AppCompatActivity implements AntiSpoofingCallbackV2{
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_anti_spoofing);

    asFragment = (AntiSpoofingFragment) getSupportFragmentManager().findFragmentById(R.id.fm_antispoofing_fragment);
    asFragment.setAntiSpoofingCallback(this);

    initSdkComponents();
    initDetectSetting();
    initLayoutSetting();

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }

    asFragment.startDetection();
  }

  private void initSdkComponents() {
    if (isFinishing() || isDestroyed()) return;

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
    LicenseManager licenseManager = null;
    try {
      licenseManager = new LicenseManager();
      licenseManager.initializeEx();
      licenseManager.registerLicense();

      Object value = licenseManager.getProperty(LicenseOption.EXTRACTION);
      if (value instanceof Boolean) {
        hasRecognitionFeature = (Boolean) value;
      }
    } finally {
      if (licenseManager != null) licenseManager.release();
    }
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
      _extractConfig.extractFeature = true;
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
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SUBTITLE, getString(R.string.demo_fm_2das_subtitle));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_CENTER_FACE, getString(R.string.demo_fm_2das_center_your_face_look_straight));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_DONT_MOVE, getString(R.string.demo_fm_2das_processing));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_COME_CLOSER, getString(R.string.demo_fm_2das_face_come_closer));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_GO_FARTHER, getString(R.string.demo_fm_2das_face_go_farther));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_PROCESSING, getString(R.string.demo_fm_2das_processing));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_DARK_ENVIRONMENT, getString(R.string.demo_fm_2das_alert_env_dark));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_DARK_ENVIRONMENT_DETAIL, getString(R.string.demo_fm_2das_alert_env_dark_desc));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_LEFT, getString(R.string.demo_fm_2das_turn_left));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_LEFT_DETAIL, getString(R.string.demo_fm_2das_turn_left_desc));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_RIGHT, getString(R.string.demo_fm_2das_turn_right));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_TURN_RIGHT_DETAIL, getString(R.string.demo_fm_2das_turn_right_desc));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SMILE, getString(R.string.demo_fm_2das_smile));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SMILE_DETAIL, getString(R.string.demo_fm_2das_smile_desc));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_NOD_HEAD, getString(R.string.demo_fm_2das_nod));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_NOD_HEAD_DETAIL, getString(R.string.demo_fm_2das_nod_desc));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH, getString(R.string.demo_fm_2das_speech));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH_DETAIL, getString(R.string.demo_fm_2das_speech_desc));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH_LANGUAGE_MANDARIN, getString(R.string.demo_fm_2das_speech_language_mandarin));
    layoutSetting.localizedStrings.put(LocalizedKey.AS_SPEECH_LANGUAGE_ENGLISH, getString(R.string.demo_fm_2das_speech_language_english));

    Typeface exoBold = Typeface.createFromAsset(getAssets(), "www/assets/font/Exo 2/Exo2-Bold.ttf");
    Typeface exoRegular = Typeface.createFromAsset(getAssets(), "www/assets/font/Exo 2/Exo2-Regular.ttf");
    int IDLE_COLOR = Color.WHITE;
    int ACTIVE_COLOR = Color.rgb(255, 80, 25);
    float BORDER_FONT = 5.3f;

    layoutSetting.frameIdleColor = IDLE_COLOR;
    layoutSetting.frameActiveColor = ACTIVE_COLOR;
    layoutSetting.frameBorderWidth = BORDER_FONT;
    layoutSetting.showFrame = true;
    layoutSetting.circleIdleColor = IDLE_COLOR;
    layoutSetting.circleActiveColor = ACTIVE_COLOR;
    layoutSetting.circleBorderWidth = BORDER_FONT;

    layoutSetting.actionHintColor = IDLE_COLOR;
    layoutSetting.actionHintFontSize = 20;
    layoutSetting.actionHintFont = exoRegular;
    layoutSetting.actionDetailHintIdleColor = IDLE_COLOR;
    layoutSetting.actionDetailHintFontSize = 16;
    layoutSetting.actionDetailHintFont = exoRegular;

    layoutSetting.showFPS = true;

    layoutSetting.progressBarForegroundColor = Color.rgb(58, 141, 222);
    layoutSetting.progressBarBackgroundColor = Color.argb(0x55, 255, 80, 25);
    layoutSetting.progressBarWidth = 500;
    layoutSetting.progressBarHeight = 5;

    layoutSetting.userActionHintColor = Color.BLACK;
    layoutSetting.userActionHintFontSize = -1; // Default
    layoutSetting.userActionHintFont = null; // Default
    layoutSetting.showUserActionSteps = true;

    layoutSetting.footerTitleColor = Color.BLACK;
    layoutSetting.footerTitleFontSize = -1; // Default
    layoutSetting.footerTitleFont = null; // Default
    layoutSetting.footerSubtitleColor = Color.BLACK;
    layoutSetting.footerSubtitleFontSize = -1; // Default
    layoutSetting.footerSubtitleFont = null; // Default
    layoutSetting.showFooter = false;

    layoutSetting.alertBackgroundColor = Color.argb(90, 255, 80, 25);
    layoutSetting.alertTitleColor = Color.WHITE;
    layoutSetting.alertTitleFontSize = 20; // Default
    layoutSetting.alertTitleFont = exoBold; // Default
    layoutSetting.alertDescriptionColor = Color.WHITE;
    layoutSetting.alertDescriptionFontSize = 16; // Default
    layoutSetting.alertDescriptionFont = exoRegular; // Default
    layoutSetting.alertDistanceToCircle = -1; // Default

    asFragment.setLayoutSetting(layoutSetting);
  }

  @Override
  public void onPreviewSizeChanged(int i, int i1, float v, float v1) {

  }

  @Override
  public void onAntiSpoofingError(int i) {

  }

  @Override
  public void onAntiSpoofingResult(Bitmap bitmap, FaceInfo face, int i, double v, int i1, SpeechFeature speechFeature) {
    boolean hasSimilarFace = false;
    int facesCount = _recognizer.extractFace(_extractConfig, Collections.singletonList(bitmap));
    if (facesCount > 0) {
      ArrayList<FaceHolder> faces = new ArrayList<>();
      for (int faceIndex = 0; faceIndex < facesCount; faceIndex++) {
        FaceFeature faceFeature = _recognizer.getFaceFeature(0, faceIndex);

        List<SimilarFaceResult> searchResult = _dataManager.searchSimilarFace(confidenceThreshold, -1, faceFeature, 1);
        if (searchResult != null && !searchResult.isEmpty()) {
          hasSimilarFace = true;
        }else{
          hasSimilarFace = false;
        }
      }
      Intent resultIntent = new Intent();
      resultIntent.putExtra("hasSimilarFace", hasSimilarFace);
      setResult(RESULT_OK, resultIntent);
      finish();
    }
  }
}
