package inc.bastion.faceme;

public class AntiSpoofingConfig {
  //GLOBAL VARIABLES
  public boolean showFPS;

  //Frame
  public int frameActiveColor;
  public int frameIdleColor;
  public float frameBorderWidth;
  public boolean showFrame;

  //Circle
  public int circleActiveColor;
  public int circleIdleColor;
  public float circleBorderWidth;

  //Action Detail Hint
  public int actionDetailHintActiveColor;
  public int actionDetailHintIdleColor;
  public String actionDetailHintFont;
  public float actionDetailHintFontSize;

  //Action Hint
  public int actionHintColor;
  public String actionHintFont;
  public float actionHintFontSize;

  //Progress Bar
  public int progressBarForegroundColor;
  public int progressBarBackgroundColor;
  public int progressBarWidth;
  public int progressBarHeight;

  //Footer
  public int footerTitleColor;
  public String footerTitleFont;
  public float footerTitleFontSize;
  public int footerSubtitleColor;
  public String footerSubtitleFont;
  public float footerSubtitleFontSize;
  public boolean showFooter;

  //User Action Hint
  public int userActionHintColor;
  public String userActionHintFont;
  public float userActionHintFontSize;
  public boolean showUserActionSteps;

  //Speech Number
  public int speechNumberActiveColor;
  public int speechNumberIdleColor;
  public String speechNumberFont;
  public float speechNumberFontSize;

  //Speech Language
  public int speechLanguageColor;
  public String speechLanguageFont;
  public int speechLanguageBackgroundColor;
  public boolean showSpeechLanguage;

  //Alert Position
  public int alertDistanceToCircle;

  //Alert Background
  public int alertBackgroundColor;

  //Alert Title
  public int alertTitleColor;
  public String alertTitleFont;
  public float alertTitleFontSize;

  //Alert Description
  public int alertDescriptionColor;
  public String alertDescriptionFont;
  public float alertDescriptionFontSize;

  public AntiSpoofingConfig(boolean showFPS,
                               int frameActiveColor, int frameIdleColor,
                                float frameBorderWidth, boolean showFrame,
                               int circleActiveColor, int circleIdleColor,
                               float circleBorderWidth,
                               int actionDetailHintActiveColor, int actionDetailHintIdleColor, String actionDetailHintFont, float actionDetailHintFontSize,
                               int actionHintColor, String actionHintFont, float actionHintFontSize,
                               int progressBarForegroundColor, int progressBarBackgroundColor, int progressBarWidth, int progressBarHeight,
                               int footerTitleColor, String footerTitleFont, float footerTitleFontSize,
                               int footerSubtitleColor, String footerSubtitleFont, float footerSubtitleFontSize, boolean showFooter,
                               int userActionHintColor, String userActionHintFont, float userActionHintFontSize, boolean showUserActionSteps,
                               int speechNumberActiveColor, int speechNumberIdleColor, String speechNumberFont, float speechNumberFontSize,
                               int speechLanguageColor, String speechLanguageFont, int speechLanguageBackgroundColor, boolean showSpeechLanguage,
                               int alertDistanceToCircle, int alertBackgroundColor,
                               int alertTitleColor, String alertTitleFont, float alertTitleFontSize,
                               int alertDescriptionColor, String alertDescriptionFont, float alertDescriptionFontSize) {
    // Initialize all the variables here
    this.showFPS = showFPS;
    this.frameActiveColor = frameActiveColor;
    this.frameIdleColor = frameIdleColor;
    this.frameBorderWidth = frameBorderWidth;
    this.showFrame = showFrame;
    this.circleActiveColor = circleActiveColor;
    this.circleIdleColor = circleIdleColor;
    this.circleBorderWidth = circleBorderWidth;
    this.actionDetailHintActiveColor = actionDetailHintActiveColor;
    this.actionDetailHintIdleColor = actionDetailHintIdleColor;
    this.actionDetailHintFont = actionDetailHintFont;
    this.actionDetailHintFontSize = actionDetailHintFontSize;
    this.actionHintColor = actionHintColor;
    this.actionHintFont = actionHintFont;
    this.actionHintFontSize = actionHintFontSize;
    this.progressBarForegroundColor = progressBarForegroundColor;
    this.progressBarBackgroundColor = progressBarBackgroundColor;
    this.progressBarWidth = progressBarWidth;
    this.progressBarHeight = progressBarHeight;
    this.footerTitleColor = footerTitleColor;
    this.footerTitleFont = footerTitleFont;
    this.footerTitleFontSize = footerTitleFontSize;
    this.footerSubtitleColor = footerSubtitleColor;
    this.footerSubtitleFont = footerSubtitleFont;
    this.footerSubtitleFontSize = footerSubtitleFontSize;
    this.showFooter = showFooter;
    this.userActionHintColor = userActionHintColor;
    this.userActionHintFont = userActionHintFont;
    this.userActionHintFontSize = userActionHintFontSize;
    this.showUserActionSteps = showUserActionSteps;
    this.speechNumberActiveColor = speechNumberActiveColor;
    this.speechNumberIdleColor = speechNumberIdleColor;
    this.speechNumberFont = speechNumberFont;
    this.speechNumberFontSize = speechNumberFontSize;
    this.speechLanguageColor = speechLanguageColor;
    this.speechLanguageFont = speechLanguageFont;
    this.speechLanguageBackgroundColor = speechLanguageBackgroundColor;
    this.showSpeechLanguage = showSpeechLanguage;
    this.alertDistanceToCircle = alertDistanceToCircle;
    this.alertBackgroundColor = alertBackgroundColor;
    this.alertTitleColor = alertTitleColor;
    this.alertTitleFont = alertTitleFont;
    this.alertTitleFontSize = alertTitleFontSize;
    this.alertDescriptionColor = alertDescriptionColor;
    this.alertDescriptionFont = alertDescriptionFont;
    this.alertDescriptionFontSize = alertDescriptionFontSize;
  }
}
