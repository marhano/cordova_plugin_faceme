package inc.bastion.faceme;

import org.apache.cordova.CordovaPlugin;

import java.util.ArrayList;

import javax.security.auth.callback.Callback;

import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.cyberlink.faceme.QueryResult;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.LicenseManager;

import inc.bastion.faceme.FaceHolder;

import android.content.Context;
import android.util.Log;
/**
 * This class echoes a string called from JavaScript.
 */
public class FaceMe extends CordovaPlugin {
    private static final String TAG = "FaceMe";

    private static final String TEST_PLUGIN = "testPlugin";
    private static final String INITIALIZE_SDK = "initializeSDK";

    private CallbackContext testPluginCallbackContext;
    private CallbackContext initializeSDKCallbackContext;

    private CallbackContext execCallback;
    private JSONArray execArgs;
    
    private FaceMeRecognizer recognizer = null;
    private ExtractConfig extractConfig = null;

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
        }
         return false;
    }

    private boolean testPlugin(CallbackContext callbackContext){
        testPluginCallbackContext = callbackContext;

        callbackContext.success("PLUGIN TEST ABCD 1234");

        return true;
    }

    private boolean initializeSDK(CallbackContext callbackContext){

        String LICENSE_KEY = "Ae7XoEbv9HPwTdMh8IeZJxmtIkg4FAvzW8v8WdJc";
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

/* 
    private ArrayList<FaceHolder> extractBitmap(Bitmap bitmap){
        if(recognizer == null || bitmap.getHeight() != maxFrameHeight || bitmap.getWidth() != maxFrameWidth){
            maxFrameHeight = bitmap.getHeight();
            maxFrameWidth = bitmap.getWidth();
            releaseRecognizer();
            initializeRecognizer();
        }

        int facesCount = recognizer.extractFace(extractConfig, Collections.singletonList(bitmap));
        ArrayList<FaceHolder> faces = new ArrayList<>();
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
        }
        return faces;
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
            
            recognizer = new FaceRecognizer();
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

        } catch (Exception e) {
            releaseRecognizer();
            throw e;
        }
    }
    private JSONArray convertToJsonArray(ArrayList<FaceHolder> faceHolders) throws JSONException{
        JSONArray jsonArray = new JSONArray();

        for(FaceHolder faceHolder : faceHolders){
            JSONObject jsonObject = new JSONObject();

            for (FaceInfo faceInfo : faceHolder.faceInfo) {
                
            }
            for (FaceLandmark faceLandmark : faceHolder.faceLandmark) {
                
            }
            for (FaceAttribute faceAttribute : faceHolder.faceAttribute) {
                
            }
            for (FaceFeature faceFeature : faceHolder.faceFeature) {
                
            }
            
        }

        return jsonArray;
    }
*/
}
