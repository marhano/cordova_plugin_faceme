package inc.bastion.faceme;

import org.apache.cordova.CordovaPlugin;

import java.util.ArrayList;

import javax.security.auth.callback.Callback;

import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cyberlink.faceme.FaceMeSdk;
import com.cyberlink.faceme.LicenseManager;

import android.content.Context;
/**
 * This class echoes a string called from JavaScript.
 */
public class faceme extends CordovaPlugin {
    private static final String TAG = "FaceMe";

    private static final String TEST_PLUGIN = "testPlugin";
    private static final String INITIALIZE_SDK = "initializeSDK";

    private CallbackContext testPluginCallbackContext;
    private CallbackContext initializeSDKCallbackContext;

    private CallbackContext execCallback;
    private JSONArray execArgs;
    
    private FaceMeRecognizer recognizer = null;
    private ExtractConfig extractConfig = null;

    public faceme(){
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



        // if (action.equals("coolMethod")) {
        //     String message = args.getString(0);
        //     this.coolMethod(message, callbackContext);
        //     return true;
        // }else if(action.equals("initializeFaceme")){
        //     this.initializeFaceme(callbackContext);
        //     return true;
        // }
        // return false;
    }


    private void testPlugin(CallbackContext callbackContext){
        testPluginCallbackContext = callbackContext;

        callbackContext.success("PLUGIN TEST ABCD 1234");
    }

    private void initializeSDK(CallbackContext callbackContext){

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
        
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
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

}
