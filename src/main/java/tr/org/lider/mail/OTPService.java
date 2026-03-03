package tr.org.lider.mail;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_DURATION = 5 * 60 * 1000; // 5 minutes
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateOTP(String email) {
        String otp = String.format("%06d", new SecureRandom().nextInt(1000000));
        otpStorage.put(email, otp + ":" + System.currentTimeMillis());
        return otp;
    }

    public boolean validateOTP(String email, String inputOtp) {
        String storedOtpData = otpStorage.get(email);
        if (storedOtpData == null) {
            return false;
        }
        String[] parts = storedOtpData.split(":");
        String storedOtp = parts[0];
        long timestamp = Long.parseLong(parts[1]);

        if (System.currentTimeMillis() - timestamp > OTP_EXPIRY_DURATION) {
            otpStorage.remove(email);
            return false; // OTP expired
        }

        boolean isValid = storedOtp.equals(inputOtp);
        if (isValid) {
            otpStorage.remove(email); // OTP is single-use
        }
        return isValid;
    }
}