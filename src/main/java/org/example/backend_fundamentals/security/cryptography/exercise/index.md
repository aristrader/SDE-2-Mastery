---
order: 10
search: false
---

# Exercises: ePassport Cryptographic Verification

Practical engineering exercises covering the implementation of Passive Authentication (PA) data verification pipelines and Active Authentication (AA) anti-cloning challenge-response protocols.

---

## Exercise: passive-authentication-verification-pipeline - Design a Resilient Passive Authentication Verification Pipeline

### Context
You are building the core cryptographic verification service for an automated airport e-Gate. When a traveler places their ePassport on the document reader, the terminal reads the chip's `EF.SOD` (Security Object) and the relevant data groups (`DG1` for MRZ text, `DG2` for face biometric, and optionally `DG15` for Active Authentication).

Your service must execute Passive Authentication (PA) as specified in [ICAO Doc 9303 Part 11 Appendix E](https://www.icao.int/publications/documents/9303_p11_cons_en.pdf) against a local trust store populated from the ICAO Public Key Directory (PKD).

### Requirements & Task
Design and implement the verification workflow and method contract for the `PassiveAuthenticationValidator`.

1. **Trust Chain Resolution:**
   * Extract the Document Signer Certificate (DSC) from the `EF.SOD` PKCS#7 container, or retrieve it from the local PKD trust cache if omitted from the chip.
   * Locate the issuing country's Country Signing Certification Authority (CSCA) root certificate in the trusted local key store.
   * Cryptographically verify the DSC signature against the trusted CSCA public key.
   * Verify the DSC validity period against current time and ensure the DSC serial number is not on the active Certificate Revocation List (CRL).
2. **SOD Integrity Verification:**
   * Extract the Document Signer's public key from the validated DSC.
   * Verify the cryptographic signature on the `EF.SOD` hash table using the Document Signer public key.
3. **Data Group Hash Verification:**
   * For each Data Group read from the chip (`DG1`, `DG2`, etc.), compute the cryptographic hash using the algorithm specified in `EF.SOD` (e.g., SHA-256).
   * Compare the computed hash with the corresponding hash stored in the signed `EF.SOD` manifest.
4. **Structured Status Reporting:**
   * Return a deterministic result enum: `SUCCESS`, `UNTRUSTED_ISSUER` (missing/invalid CSCA), `EXPIRED_CERTIFICATE`, `REVOKED_CERTIFICATE`, `INVALID_SOD_SIGNATURE`, or `DATA_GROUP_HASH_MISMATCH`.

### Constraints & Edge Cases
* The reader must not trust the Document Signer Public Key directly from the chip without validating up to a trusted CSCA root.
* If a Data Group is listed in `EF.SOD` but not provided by the chip reader (or vice-versa), flag the mismatch.
* What happens if the system clock drifts into the future past the DSC expiration? Note how your design addresses clock validation.

---

## Exercise: active-authentication-challenge-response - Active Authentication Challenge-Response Protocol & Clone Detection

### Context
Passive Authentication (PA) confirms that the data inside `DG1`, `DG2`, and `EF.SOD` was signed by a legitimate government. However, an attacker can copy all byte streams from a valid passport and flash them onto a generic programmable smart card.

To detect cloned chips, you must implement the Active Authentication (AA) challenge-response verifier defined in [ICAO Doc 9303 Part 11 Section 6.1.6](https://www.icao.int/publications/documents/9303_p11_cons_en.pdf) and [ICAO Doc 9303 Part 10](https://www.icao.int/publications/documents/9303_p11_cons_en.pdf).

### Requirements & Task
Design the protocol execution flow and verification logic for `ActiveAuthenticationVerifier`.

1. **Pre-condition Check & Public Key Extraction:**
   * Inspect if `DG15` was present and successfully verified during Passive Authentication.
   * If `DG15` is missing, mark the verification status as `AA_NOT_SUPPORTED` (since `DG15` is optional under ICAO standards).
   * If `DG15` is present, parse the SubjectPublicKeyInfo to extract the chip's AA Public Key (RSA or ECDSA).
2. **Challenge Generation & APDU Dispatch:**
   * Generate an unpredictable, cryptographically secure 8-byte random nonce ($RND.IFD$).
   * Construct the ISO 7816 `INTERNAL AUTHENTICATE` APDU command containing the 8-byte challenge nonce and transmit it over the contactless smart card channel.
3. **Signature Verification & Clone Detection:**
   * Receive the response APDU containing the signature computed by the chip's isolated secure microcontroller using its non-exportable AA Private Key.
   * Verify the response signature over the original challenge nonce using the AA Public Key from `DG15`.
   * If the signature is valid, return `AUTHENTICATED_ORIGINAL_CHIP`.
   * Return `COMMUNICATION_OR_PROTOCOL_ERROR` for a timeout, transport failure, malformed response, or non-success APDU status. Return `CLONED_CHIP_SUSPECTED` only when a successful response fails cryptographic verification against the fresh nonce.
   * Treat the signature algorithm and response encoding as document-profile inputs; do not hard-code one RSA/ECDSA encoding for every issuing state.

### Discussion Questions
1. Why must the challenge nonce be generated fresh by the inspection reader for every interaction rather than using a fixed timestamp or counter?
2. Why must `DG15` be verified through Passive Authentication before trusting the AA challenge-response result?
