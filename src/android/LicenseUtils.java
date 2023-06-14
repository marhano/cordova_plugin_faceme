package inc.bastion.faceme;

import com.cyberlink.faceme.LicenseManager;

public class LicenseUtils {
    private LicenseManager licenseManager;

    public int verifyLicense(){
        licenseManager = new LicenseManager();
        int result = licenseManager.initializeEx();
        result = licenseManager.registerLicense();
        licenseManager.release();

        return result;
    }

    public int deactivateLicense(){
        licenseManager = new LicenseManager();
        int result = licenseManager.initializeEx();
        result = licenseManager.deactivateLicense();
        licenseManager.release();

        return result;
    }
}
