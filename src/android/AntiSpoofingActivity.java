package inc.bastion.faceme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import com.cyberlink.faceme.FaceInfo;
import com.cyberlink.faceme.SpeechFeature;
import com.cyberlink.faceme.widget.AntiSpoofingCallbackV2;
import com.cyberlink.faceme.widget.AntiSpoofingConfig;
import com.cyberlink.faceme.widget.AntiSpoofingFragment;
import com.cyberlink.faceme.widget.LocalizedKey;

import io.ionic.starter.R;

public class AntiSpoofingActivity extends AppCompatActivity implements AntiSpoofingCallbackV2{
  private AntiSpoofingFragment asFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_anti_spoofing);

    asFragment = (AntiSpoofingFragment) getSupportFragmentManager().findFragmentById(R.id.fm_antispoofing_fragment);
    asFragment.setAntiSpoofingCallback(this);

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
  public void onAntiSpoofingResult(Bitmap bitmap, FaceInfo faceInfo, int i, double v, int i1, SpeechFeature speechFeature) {

  }
}
