package com.zds.ydkf_qm;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignUtil {

    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String TERMINATOR = "tc3_request";

    public static String buildAuthorization(
            String secretId,
            String secretKey,
            String service,
            String host,
            String action,
            long timestampSec,
            String payloadJson
    ) {
        try {
            String date = utcDate(timestampSec);

            // Step1 CanonicalRequest
            String httpRequestMethod = "POST";
            String canonicalUri = "/";
            String canonicalQueryString = "";
            String contentType = "application/json; charset=utf-8";

            String canonicalHeaders =
                    "content-type:" + contentType.toLowerCase(Locale.ROOT).trim() + "\n" +
                            "host:" + host.toLowerCase(Locale.ROOT).trim() + "\n" +
                            "x-tc-action:" + action.toLowerCase(Locale.ROOT).trim() + "\n";

            String signedHeaders = "content-type;host;x-tc-action";
            String hashedRequestPayload = sha256Hex(payloadJson);

            String canonicalRequest =
                    httpRequestMethod + "\n" +
                            canonicalUri + "\n" +
                            canonicalQueryString + "\n" +
                            canonicalHeaders + "\n" +
                            signedHeaders + "\n" +
                            hashedRequestPayload;

            String credentialScope = date + "/" + service + "/" + TERMINATOR;
            String hashedCanonicalRequest = sha256Hex(canonicalRequest);

            String stringToSign =
                    ALGORITHM + "\n" +
                            timestampSec + "\n" +
                            credentialScope + "\n" +
                            hashedCanonicalRequest;

            byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, service);
            byte[] secretSigning = hmacSha256(secretService, TERMINATOR);
            String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

            return ALGORITHM + " " +
                    "Credential=" + secretId + "/" + credentialScope + ", " +
                    "SignedHeaders=" + signedHeaders + ", " +
                    "Signature=" + signature;

        } catch (Exception e) {
            throw new RuntimeException("sign failed: " + e.getMessage(), e);
        }
    }

    private static String utcDate(long timestampSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestampSec * 1000L));
    }

    private static byte[] hmacSha256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(key, HMAC_ALGO));
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(d);
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
