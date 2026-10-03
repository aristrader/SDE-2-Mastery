---
order: 20
search: false
---

# Solutions: ePassport Cryptographic Verification

Worked solutions and reference architectures for the Passive Authentication (PA) pipeline and Active Authentication (AA) challenge-response protocol.

---

## Solution: passive-authentication-verification-pipeline - Design a Resilient Passive Authentication Verification Pipeline

### 1. Architectural Pipeline & Sequence

```
1. Receive Input (EF.SOD bytes, Map<DataGroupId, byte[]> rawDataGroups)
   │
   ▼
2. Parse EF.SOD (ASN.1 / PKCS#7 SignedData)
   ├── Extract Document Signer Certificate (DSC) [or fetch from local PKD cache]
   ├── Extract SignerInfo & SignedAttributes (contains Hash Algorithm & Digest)
   └── Extract LDS Security Object Content (Map<DataGroupId, byte[] expectedHashes>)
   │
   ▼
3. Validate Trust Anchor (CSCA -> DSC)
   ├── Lookup Issuing Country CSCA Root Cert in Trusted TrustStore
   │     └── [Missing CSCA] ──> Return UNTRUSTED_ISSUER
   ├── Verify DSC Signature using CSCA Public Key
   │     └── [Invalid Sig]  ──> Return UNTRUSTED_ISSUER
   ├── Validate DSC Validity Interval (notBefore <= T_now <= notAfter)
   │     └── [Expired]      ──> Return EXPIRED_CERTIFICATE
   └── Check Certificate Revocation List (CRL)
         └── [Revoked]      ──> Return REVOKED_CERTIFICATE
   │
   ▼
4. Verify EF.SOD Digital Signature
   ├── Extract DS Public Key from Validated DSC
   └── Verify SignedData signature over SOD content
         └── [Invalid Sig]  ──> Return INVALID_SOD_SIGNATURE
   │
   ▼
5. Verify Data Group Hashes
   ├── For each (dgId, expectedHash) in SOD manifest:
   │     ├── Retrieve raw bytes for dgId from reader input
   │     ├── Compute actualHash = Digest(rawDgBytes, algorithmFromSOD)
   │     └── Assert: constantTimeEquals(actualHash, expectedHash)
   │           └── [Mismatch] ──> Return DATA_GROUP_HASH_MISMATCH (Altered Data)
   │
   ▼
6. Return SUCCESS (Data Integrity & Issuing Origin Cryptographically Proven)
```

### 2. Java Reference Implementation

```java
package org.example.backend_fundamentals.security.cryptography;

import java.security.*;
import java.security.cert.X509Certificate;
import java.security.cert.X509CRL;
import java.util.*;

public class PassiveAuthenticationValidator {

    public enum ValidationStatus {
        SUCCESS,
        UNTRUSTED_ISSUER,
        EXPIRED_CERTIFICATE,
        REVOKED_CERTIFICATE,
        INVALID_SOD_SIGNATURE,
        DATA_GROUP_HASH_MISMATCH,
        MISSING_MANDATORY_DATA_GROUP
    }

    public record ParsedSOD(
        X509Certificate embeddedDSC,
        String issuingCountry,
        String hashAlgorithm,
        byte[] sodSignature,
        byte[] signedContent,
        Map<Integer, byte[]> dataGroupHashes
    ) {}

    public record PAResult(ValidationStatus status, String detail) {}

    private final Map<String, X509Certificate> cscaTrustStore;
    private final Map<String, X509CRL> crlStore;

    public PassiveAuthenticationValidator(
            Map<String, X509Certificate> cscaTrustStore,
            Map<String, X509CRL> crlStore) {
        this.cscaTrustStore = cscaTrustStore;
        this.crlStore = crlStore;
    }

    public PAResult validate(ParsedSOD sod, Map<Integer, byte[]> readDataGroups, Date validationTime) {
        // Step 1: Validate Trust Anchor (CSCA -> DSC)
        X509Certificate cscaCert = cscaTrustStore.get(sod.issuingCountry());
        if (cscaCert == null) {
            return new PAResult(ValidationStatus.UNTRUSTED_ISSUER,
                "No trusted CSCA root certificate found for country: " + sod.issuingCountry());
        }

        X509Certificate dscCert = sod.embeddedDSC();
        try {
            // Verify DSC signature against CSCA root
            dscCert.verify(cscaCert.getPublicKey());

            // Validate validity window
            dscCert.checkValidity(validationTime);

            // Check CRL revocation
            X509CRL crl = crlStore.get(sod.issuingCountry());
            if (crl != null && crl.isRevoked(dscCert)) {
                return new PAResult(ValidationStatus.REVOKED_CERTIFICATE, "Document Signer Certificate is revoked");
            }
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            return new PAResult(ValidationStatus.EXPIRED_CERTIFICATE, "DSC certificate validity window expired");
        } catch (Exception e) {
            return new PAResult(ValidationStatus.UNTRUSTED_ISSUER, "DSC signature verification against CSCA failed");
        }

        // Step 2: Verify EF.SOD signature using validated DS public key
        try {
            Signature sig = Signature.getInstance(dscCert.getSigAlgName());
            sig.initVerify(dscCert.getPublicKey());
            sig.update(sod.signedContent());
            if (!sig.verify(sod.sodSignature())) {
                return new PAResult(ValidationStatus.INVALID_SOD_SIGNATURE, "EF.SOD digital signature is invalid");
            }
        } catch (Exception e) {
            return new PAResult(ValidationStatus.INVALID_SOD_SIGNATURE, "Error executing SOD signature verification: " + e.getMessage());
        }

        // Step 3: Validate individual Data Group hashes
        try {
            MessageDigest md = MessageDigest.getInstance(sod.hashAlgorithm());
            for (Map.Entry<Integer, byte[]> entry : sod.dataGroupHashes().entrySet()) {
                int dgId = entry.getKey();
                byte[] expectedHash = entry.getValue();

                byte[] actualDgBytes = readDataGroups.get(dgId);
                if (actualDgBytes == null) {
                    // Mandatory DGs like DG1 (MRZ) and DG2 (Photo) must be present
                    if (dgId == 1 || dgId == 2) {
                        return new PAResult(ValidationStatus.MISSING_MANDATORY_DATA_GROUP, "Mandatory DG" + dgId + " missing from read");
                    }
                    continue;
                }

                byte[] calculatedHash = md.digest(actualDgBytes);
                if (!MessageDigest.isEqual(calculatedHash, expectedHash)) {
                    return new PAResult(ValidationStatus.DATA_GROUP_HASH_MISMATCH, "Tamper detected in Data Group " + dgId);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            return new PAResult(ValidationStatus.INVALID_SOD_SIGNATURE, "Unsupported digest algorithm: " + sod.hashAlgorithm());
        }

        return new PAResult(ValidationStatus.SUCCESS, "Passive Authentication passed successfully");
    }
}
```

### 3. Key Design Decisions & Failure Mitigations
* **Never Trust Raw DSC Directly:** The DSC is untrusted input until its signature is validated against the pre-installed CSCA root certificate.
* **Constant-Time Comparison:** Use `MessageDigest.isEqual()` to prevent timing side-channels when comparing hash values.
* **Clock Skew Handling:** System time should be synchronized using NTP. In edge cases where an e-Gate is operating offline during network outages, cached time anchors with bounded tolerance prevent false rejections.

---

## Solution: active-authentication-challenge-response - Active Authentication Challenge-Response Protocol & Clone Detection

### 1. Protocol Execution Flow & State Machine

```
[ Terminal / Reader ]                                [ ePassport Microcontroller ]
         │                                                        │
         ├── 1. Check if DG15 present in validated PA DGs        │
         │      ├── No ──> Return AA_NOT_SUPPORTED                │
         │      └── Yes ─> Parse AA Public Key from DG15          │
         │                                                        │
         ├── 2. Generate 8-byte Cryptographic Nonce (RND.IFD)     │
         │                                                        │
         ├── 3. Send APDU: INTERNAL AUTHENTICATE (RND.IFD) ──────>│
         │                                                        ├── 4. Hardware Crypto Engine
         │                                                        │      Computes Signature =
         │                                                        │      Sign(RND.IFD, AA_PrivKey)
         │                                                        │      (Private key never leaves
         │                                                        │       secure microcontroller)
         |<── 5. Receive APDU Response (Signature / Status Word) ──┤
         │                                                        │
         ├── 6. Verify Signature(RND.IFD) using DG15 AA PubKey    │
         │      ├── Valid ───> Return AUTHENTICATED_ORIGINAL_CHIP │
         │      └── Invalid ─> Return CLONED_CHIP_SUSPECTED       │
```

### 2. Java Reference Implementation

```java
package org.example.backend_fundamentals.security.cryptography;

import java.security.*;
import java.util.Arrays;

public class ActiveAuthenticationVerifier {

    public enum AAResultStatus {
        AUTHENTICATED_ORIGINAL_CHIP,
        CLONED_CHIP_SUSPECTED,
        AA_NOT_SUPPORTED,
        COMMUNICATION_ERROR
    }

    public interface SmartCardTransceiver {
        // Sends APDU command bytes and returns raw response bytes
        byte[] transmit(byte[] commandApdu) throws Exception;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public AAResultStatus verifyActiveAuthentication(
            PublicKey aaPublicKeyFromDG15,
            boolean isDG15VerifiedByPA,
            SmartCardTransceiver transceiver,
            String signatureAlgorithm) {

        // Step 1: Pre-condition check
        if (aaPublicKeyFromDG15 == null || !isDG15VerifiedByPA) {
            // DG15 is optional under ICAO Doc 9303. If absent, AA cannot be performed.
            return AAResultStatus.AA_NOT_SUPPORTED;
        }

        // Step 2: Generate unpredictable 8-byte nonce (RND.IFD)
        byte[] nonce = new byte[8];
        secureRandom.nextBytes(nonce);

        // Step 3: Construct ISO 7816 INTERNAL AUTHENTICATE APDU
        // CLA=0x00, INS=0x88 (INTERNAL AUTHENTICATE), P1=0x00, P2=0x00, Lc=0x08, Data=nonce, Le=0x00
        byte[] apdu = new byte[5 + nonce.length + 1];
        apdu[0] = 0x00;        // CLA
        apdu[1] = (byte) 0x88; // INS: INTERNAL AUTHENTICATE
        apdu[2] = 0x00;        // P1
        apdu[3] = 0x00;        // P2
        apdu[4] = (byte) nonce.length; // Lc
        System.arraycopy(nonce, 0, apdu, 5, nonce.length);
        apdu[apdu.length - 1] = 0x00; // Le (expects response)

        byte[] responseApdu;
        try {
            responseApdu = transceiver.transmit(apdu);
        } catch (Exception e) {
            return AAResultStatus.COMMUNICATION_ERROR;
        }

        if (responseApdu == null || responseApdu.length < 2) {
            return AAResultStatus.COMMUNICATION_ERROR;
        }

        // Check Smart Card Status Word (SW1 SW2 == 0x90 0x00 for SUCCESS)
        int sw1 = responseApdu[responseApdu.length - 2] & 0xFF;
        int sw2 = responseApdu[responseApdu.length - 1] & 0xFF;
        if (sw1 != 0x90 || sw2 != 0x00) {
            return AAResultStatus.COMMUNICATION_ERROR;
        }

        // Extract signature payload (excluding 2-byte status word)
        byte[] signature = Arrays.copyOf(responseApdu, responseApdu.length - 2);

        // Step 4: Verify digital signature using DG15 Public Key
        try {
            // This value comes from the passport's AA profile; it cannot be inferred from key type alone.
            Signature sigVerifier = Signature.getInstance(signatureAlgorithm);
            sigVerifier.initVerify(aaPublicKeyFromDG15);
            sigVerifier.update(nonce);

            if (sigVerifier.verify(signature)) {
                return AAResultStatus.AUTHENTICATED_ORIGINAL_CHIP;
            } else {
                return AAResultStatus.CLONED_CHIP_SUSPECTED;
            }
        } catch (Exception e) {
            return AAResultStatus.COMMUNICATION_ERROR;
        }
    }
}
```

### 3. Answers to Discussion Questions

1. **Why must the challenge nonce be generated fresh by the inspection reader?**
   If the challenge were predictable (e.g., fixed value, predictable counter, or timestamp with coarse granularity), an adversary could pre-compute or capture a signature from an authentic passport once, and replay that signature from an emulator chip. A freshly generated 8-byte cryptographic random nonce guarantees that the challenge is single-use and unique per interaction, making replay attacks statistically impossible.

2. **Why must `DG15` be verified through Passive Authentication first?**
   If the inspection reader does not perform PA on `DG15`, an attacker cloning a passport could generate their own arbitrary RSA/ECDSA key pair, place the private key on a blank programmable chip, and write the corresponding fake public key into `DG15`. The cloned chip would easily produce valid signatures matching its own fake public key. Passive Authentication ensures that the public key in `DG15` is cryptographically hashed inside `EF.SOD` and signed by the issuing government's Document Signer, preventing substitution of the public key.

**Result interpretation:** a timeout, malformed APDU, unsupported profile, or non-`0x9000` status is an operational/protocol failure to investigate, not proof of a clone. Only a well-formed successful AA response that fails verification using the PA-validated `DG15` public key is clone suspicion. The exact RSA/ECDSA algorithm and signature encoding are document-profile details.
