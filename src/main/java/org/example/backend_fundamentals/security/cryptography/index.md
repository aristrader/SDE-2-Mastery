---
order: 30
---

# ePassport Security Architecture: Passive & Active Authentication

An electronic passport (ePassport / Machine Readable Travel Document - MRTD) complies with the international specifications established in [ICAO Doc 9303](https://www.icao.int/publications/documents/9303_p11_cons_en.pdf). From a backend and security engineering perspective, an ePassport combines a Public Key Infrastructure (PKI), signed data manifests, and on-chip hardware cryptography to address two distinct threat models:
1. **Data Tampering & Forgery:** Modifying the biographical or biometric data stored on the document.
2. **Chip Cloning & Replay:** Dumping valid data from a legitimate passport and re-flashing it onto a commodity NFC microchip.

---

## 1. Cryptographic Key Hierarchy & Trust Chain

The ePassport trust architecture separates authority across three independent key layers, spanning offline government infrastructure, document issuance facilities, and individual microchip hardware.

```
+-------------------------------------------------------------+
| CSCA (Country Signing Certification Authority)              |
| - Root of trust per issuing state (Offline Private Key)     |
| - Issues & signs Document Signer Certificates (DSC)         |
+-------------------------------------------------------------+
                              | signs
                              v
+-------------------------------------------------------------+
| Document Signer (DS)                                        |
| - Personalization / Issuance HSM (DS Private Key)           |
| - Signs the EF.SOD (Security Object) manifest once at print |
| - DSC conveyed in EF.SOD or distributed via ICAO PKD        |
+-------------------------------------------------------------+
                              | signs
                              v
+-------------------------------------------------------------+
| EF.SOD (Security Object on Microchip)                       |
| - Contains SHA-256/SHA-512 hashes of present Data Groups    |
| - DG1 (MRZ), DG2 (Face Photo), DG15 (Optional AA PubKey)    |
+-------------------------------------------------------------+
                              | binds (when AA implemented)
                              v
+-------------------------------------------------------------+
| Active Authentication (AA) Key Pair                         |
| - Public Key: Stored in DG15 and hashed into EF.SOD         |
| - Private Key: Isolated inside chip Secure Microcontroller  |
+-------------------------------------------------------------+
```

### Layer 1: Country Signing Certification Authority (CSCA)
* **Entity:** National root certification authority of the issuing state.
* **Storage & Operation:** Held in offline, air-gapped Hardware Security Modules (HSMs). CSCA private keys are never present on passport chips.
* **Role:** Acts as the national trust anchor. Signs short-to-medium term Document Signer Certificates (DSC).
* **Trust Distribution:** CSCA public keys (or self-signed root certs) are exchanged out-of-band between countries through bilateral diplomatic exchanges and consolidated Master Lists published via the [ICAO Public Key Directory (PKD)](https://www.icao.int/icao-pkd/epassport-validation-roadmap-tool-basics).

### Layer 2: Document Signer (DS)
* **Entity:** Secure government passport personalization and personalization factory system.
* **Storage & Operation:** The DS private key resides in high-throughput personalization HSMs.
* **Role:** Digitally signs the citizen's document manifest (`EF.SOD`) at the time of manufacturing.
* **Key Delivery:** The Document Signer Certificate (DSC) containing the DS public key is commonly embedded directly within the passport's `EF.SOD` structure (as a PKCS#7 `SignedData` container), or distributed via the ICAO PKD.
* **Inspection Requirement:** An inspection reader cannot blindly trust a DS public key just because it was read off the chip. The inspection system must validate the DSC's digital signature and validity period against a pre-trusted CSCA root certificate.

### Layer 3: Active Authentication Keys (AA) — Optional / Legacy Anti-Cloning
* **Entity:** Individual passport contactless microcontroller / smart card.
* **Storage & Operation:** A unique asymmetric key pair (RSA or ECDSA) generated per physical passport. The public key is stored in Data Group 15 (`DG15`). The private key is isolated inside the chip's secure hardware element.
* **Role:** Executes an interactive cryptographic challenge-response protocol to prove physical chip authenticity.
* **Profile Note:** Active Authentication is an optional, legacy mechanism defined in [ICAO Doc 9303 Part 10](https://www.icao.int/publications/documents/9303_p11_cons_en.pdf) and Part 11. Modern ePassport deployments often utilize Chip Authentication (CA, part of EAC / PACE-CAM) for anti-cloning and secure channel establishment without changing the core threat distinction.

---

## 2. Chip Logical Data Structure (LDS)

Data on an ICAO Doc 9303 compliant chip is organized into standard Elementary Files (EF) and Data Groups (DG):

| Element | Name / Contents | Security Handling |
| :--- | :--- | :--- |
| **`EF.SOD`** | **Security Object Document** | PKCS#7 signed manifest containing a hash list of all present Data Groups, signed by the Document Signer (DS). |
| **`DG1`** | **Machine Readable Zone (MRZ)** | Passport number, nationality, date of birth, expiration, issuing state. Read via optical MRZ or NFC. |
| **`DG2`** | **Biometric Face Encoded Image** | Primary biometric photo of the passport holder. |
| **`DG15`** | **Active Authentication Public Key Info** | **Optional.** Present only if Active Authentication is implemented on the chip. Contains the public key used to verify AA challenge signatures. |

---

## 3. Threat Model Walkthroughs

### Walkthrough 1: Data Tampering & Forgery

```
Threat: Attacker modifies DG1 (name/expiry) or DG2 (photo) on the chip.
Naive Failure: Reader reads raw bytes from NFC chip; attacker bypasses optical check.
Mechanism: Passive Authentication (PA) verifies DSC -> CSCA, then SOD -> DSC, then DG_i -> SOD hashes.
Tradeoffs: Key distribution latency, CRL revocation lag, clock skew at offline border gates.
```

#### The Threat
An adversary with NFC read/write access intercepts a genuine passport, alters the expiry date or citizen name in `DG1`, or replaces the face photo in `DG2` with an impersonator's image.

#### Why Naive Validation Fails
If an inspection terminal reads data files directly from the contactless chip without cryptographic verification, it treats untrusted storage as authoritative. Even if the chip storage is marked read-only, malicious or modified hardware can emit arbitrary payload bytes over NFC.

#### The Cryptographic Mechanism: Passive Authentication (PA)
According to [ICAO Doc 9303 Part 11 Appendix E](https://www.icao.int/publications/documents/9303_p11_cons_en.pdf) and [Privacy by Design PA Guide](https://privacybydesign.github.io/vcmrtd/info/pa), the inspection terminal executes Passive Authentication:

```
[ Inspection System ]                           [ ePassport Chip ]
        |                                               |
        |--- 1. Read EF.SOD --------------------------->|
        |<-- Returns Signed Data (SOD + DSC) -----------|
        |--- 2. Read Data Groups (DG1, DG2, etc.) ----->|
        |<-- Returns DG Raw Payloads -------------------|
        |
        |=== Verification Phase ===
        | 3. Extract DSC from SOD (or local PKD cache)
        | 4. Validate DSC signature against Trusted CSCA Root
        | 5. Verify SOD signature using validated DS Public Key
        | 6. Compute Hash(DG_n) for each read DG
        | 7. Assert: Calculated Hash(DG_n) == Hash listed in SOD
        |
        v
  [ Pass / Fail ]
```

1. **Read `EF.SOD`:** The reader extracts the signed security object.
2. **Validate DSC against CSCA:** The inspection terminal validates the Document Signer Certificate against its local trust store of validated CSCA public keys (synchronized via the [ICAO PKD](https://www.icao.int/icao-pkd/epassport-validation-roadmap-tool-basics)). If the DSC is expired, untrusted, or revoked, validation halts.
3. **Verify SOD Signature:** The terminal uses the validated DS public key to verify the digital signature on the `EF.SOD` hash table.
4. **Compute and Compare Data Group Hashes:** The terminal reads the raw bytes of each target Data Group (e.g., `DG1`, `DG2`), computes their cryptographic hash (e.g., SHA-256), and compares each digest against the corresponding value in `EF.SOD`.

**Crucial Invariant:** If an attacker modifies even a single bit in `DG1` or `DG2`, the computed hash will mismatch the signed hash in `EF.SOD`. If the attacker re-hashes `EF.SOD`, they cannot produce a valid DS signature without the issuing government's private key.

#### Tradeoffs and Recovery
* **Operational Dependency:** PA requires inspection gates to maintain an up-to-date trust store of foreign CSCA roots and Document Signer revocation lists (CRLs).
* **Failure Modes:** If a border gate loses network access or holds an expired PKD cache, it must either operate in degraded offline mode with cached trust anchors or fall back to manual secondary inspection.
* **Security Boundary:** **Passive Authentication proves data integrity and authenticity, but does NOT prove physical chip originality.** An attacker can copy the intact, unmodified files (`EF.SOD`, `DG1`, `DG2`) byte-for-byte to a duplicate chip.

---

### Walkthrough 2: Chip Cloning and Replay

```
Threat: Attacker reads all valid DGs + EF.SOD and writes them onto a blank NFC chip.
Naive Failure: Passive Authentication passes completely because all hashes and signatures are genuine.
Mechanism: Active Authentication (AA) challenge-response proves possession of an isolated private key.
Tradeoffs: AA is optional/legacy; vulnerable to active MITM without secure messaging; modern systems use Chip Authentication.
```

#### The Threat
An adversary skims a traveler's passport over NFC, capturing `EF.SOD`, `DG1`, `DG2`, and `DG15`. The attacker writes these exact bytes onto a commodity programmable smart card / NFC chip. When presented at an e-Gate, the cloned chip serves genuine, untampered files.

#### Why Naive Validation (PA Alone) Fails
Passive Authentication only validates that the *data* was signed by a legitimate government. Because all data groups in PA are public read, PA cannot distinguish between the original silicon chip and a clone replaying genuine files.

#### The Cryptographic Mechanism: Active Authentication (AA)
As defined in [ICAO Doc 9303 Part 11 Section 6.1.6](https://www.icao.int/publications/documents/9303_p11_cons_en.pdf) and [Privacy by Design AA Guide](https://privacybydesign.github.io/vcmrtd/info/aa), Active Authentication solves the cloning threat through an asymmetric challenge-response protocol:

```
[ Inspection System ]                           [ ePassport Chip (Secure Element) ]
        |                                               |
        |--- 1. Read DG15 (AA Public Key Info) -------->|
        |<-- Returns DG15 ------------------------------|
        |                                               |
        | 2. PA validates DG15 hash inside EF.SOD       |
        |                                               |
        | 3. Generate 8-byte Random Nonce (RND.IFD)     |
        |--- 4. INTERNAL AUTHENTICATE command (Nonce) ->|
        |                                               |--- 5. Secure Microcontroller computes
        |                                               |    Signature = Sign(Nonce, AA_PrivKey)
        |<-- 6. Returns Signature (Response) -----------|
        |                                               |
        | 7. Verify Signature(Nonce) using AA PubKey    |
        |    extracted from validated DG15              |
        v
  [ Authenticated Original / Flagged Clone ]
```

1. **Bind Public Key via PA:** The reader reads `DG15` (if present) and performs Passive Authentication to confirm that the hash of `DG15` matches the signed `EF.SOD`. This cryptographically binds the AA public key to the government's DS signature.
2. **Generate Nonce:** The reader generates an unpredictable 8-byte cryptographic random nonce ($RND.IFD$) to prevent replay attacks.
3. **Execute Challenge (`INTERNAL AUTHENTICATE`):** The reader sends the nonce to the chip using the smart card APDU command `INTERNAL AUTHENTICATE`.
4. **On-Chip Signing:** The chip's internal cryptographic processor computes a digital signature over the nonce using the **AA Private Key**.
5. **Verify Signature:** The reader verifies the signature against the nonce using the **AA Public Key** from `DG15`.

#### Key Isolation & Physical Hardware Boundaries
* **Isolated Hardware Boundary:** The AA private key is stored within a secure smart card microcontroller (Secure Element). The chip operating system provides no command or interface to export, read, or dump the private key.
* **Execution Environment:** Cryptographic signing operations are executed entirely within internal hardware registers.
* **Hardware Resistance:** While high-security smart cards incorporate physical tamper countermeasures (such as passivation layers, sensor grids, and side-channel countermeasures), implementation details vary across silicon vendors. A protocol designer cannot assume universal self-destruction or identical blinding implementations across all models.

#### Tradeoffs, Limits, and Modern Alternatives
* **Replay Resistance vs. Relay Attacks:** AA prevents passive chip cloning, but basic AA without secure channel binding does not prevent real-time relay attacks (e.g., forwarding challenges to an authentic passport nearby over a high-speed link).
* **Optional / Legacy Status:** `DG15` and Active Authentication are optional under ICAO Doc 9303. Modern ePassport standards (such as BSI TR-03110 EAC / PACE) specify **Chip Authentication (CA)**, which replaces the challenge-response signature with an ephemeral Diffie-Hellman key agreement to simultaneously prove chip authenticity and establish an encrypted, tamper-proof communication channel.

---

## 4. Operational Trust & Key-Distribution Limits

In real-world border control and backend verification systems, cryptographic correctness is constrained by operational boundaries:

1. **Trust Store Synchronization:** CSCA root certificates are distributed out-of-band or via ICAO PKD Master Lists. Countries must continually ingest, validate, and distribute these trust lists to thousands of distributed e-Gates and border inspection posts.
2. **Revocation & CRL Latency:** Unlike standard web PKI with online OCSP stapling, border inspection posts often operate under strict sub-second latency constraints or intermittent connectivity. Revocation lists (CRLs) for compromised Document Signer certificates are synced asynchronously.
3. **Clock Drift:** Validating certificate validity intervals ($notBefore \le T_{now} \le notAfter$) requires synchronized Network Time Protocol (NTP) clocks at all inspection endpoints to prevent legitimate passports from being rejected due to local clock skew.

---

## Quick recall

1. **What are the three cryptographic key layers in an ePassport system?**
   CSCA (national offline root anchor), Document Signer (issuance facility HSM key signing `EF.SOD`), and Active Authentication / Chip Authentication (chip-unique asymmetric key pair for anti-cloning).

2. **Does the ePassport chip always store the Document Signer public key directly?**
   No. The Document Signer Certificate (DSC) is typically conveyed inside the `EF.SOD` PKCS#7 container or distributed via the ICAO PKD; the reader must validate this DSC against a trusted CSCA root.

3. **What is the exact purpose of `EF.SOD` in Passive Authentication?**
   `EF.SOD` is a digitally signed manifest containing cryptographic hashes of all present Data Groups (e.g., `DG1`, `DG2`, `DG15`), allowing the reader to detect any data alteration.

4. **Why does Passive Authentication (PA) fail to prevent chip cloning?**
   PA only verifies data integrity and issuing origin. An attacker can clone all publicly readable files (`EF.SOD` and DGs) onto a blank NFC chip without altering any bytes, passing PA completely.

5. **Is `DG15` mandatory on all ePassports?**
   No. `DG15` is optional and is present only when the issuing state implements Active Authentication (AA).

6. **How does Active Authentication prove physical chip authenticity?**
   The reader verifies `DG15` via PA, sends an unpredictable random challenge (nonce) to the chip, and verifies the chip's internal signature generated by its non-exportable hardware private key.
