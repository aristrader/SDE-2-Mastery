---
order: 20
---

# ePassport Security Architecture: Passive & Active Authentication Documentation

This document serves as a technical reference outlining the cryptographic security mechanisms embedded within electronic passports (ePassports) compliant with ICAO Doc 9303. It details how data integrity is maintained and how physical chip cloning is prevented.

---

## 1. Cryptographic Key Layers
An ePassport system utilizes three separate layers of cryptographic keys. Each layer operates independently and serves a distinct security purpose.

### Layer 1: Master Keys (CSCA Keys)
* **Entity:** Country Signing Certification Authority (CSCA).
* **Location:** Kept in highly secure, offline government environments. They are **never** placed inside a passport.
* **Purpose:** Acts as the ultimate trust anchor for a nation. The CSCA private key is used to sign the **Document Signer (DS)** certificates.
* **Distribution:** CSCA public keys are exchanged securely between nations via bilateral agreements or the ICAO Public Key Directory (PKD).

### Layer 2: Document Signer Keys (DS Keys)
* **Entity:** The Secure Government Personalization/Printing Facility.
* **Location:** The DS Public Key is written directly onto the passport chip; the DS Private Key stays inside the print facility's Hardware Security Modules (HSMs).
* **Purpose:** Digitally signs the individual citizen's biographical data, biometrics, and security configurations during passport manufacturing.

### Layer 3: Active Authentication Keys (AA Keys)
* **Entity:** The individual passport microchip hardware.
* **Location:** Unique pair generated for every single individual passport (see Section 3 for storage locations).
* **Purpose:** Executes a live challenge-response protocol to prove that the physical chip is authentic and has not been cloned.

---

## 2. Passive Authentication (PA): Data Integrity

Passive Authentication proves that the biographical and biometric data on the chip is genuine and has not been altered since it was printed by the government facility.

### The Security Object (SOD)
All data on the passport is organized into Data Groups (e.g., DG1 for text data, DG2 for the face photo, DG15 for Active Authentication). The printing facility creates a master file called the **Security Object (SOD)**, which functions as a digital manifest:

1. The facility generates a cryptographic hash (fingerprint) for every individual Data Group.
2. The facility places all these hashes into the SOD manifest.
3. The facility signs the entire SOD manifest **once** using its **Document Signer (DS) Private Key**.

### Verification Flow at Border Control
1. The reader retrieves the **DS Public Key** from the passport chip.
2. The reader verifies the **DS Public Key** against the trusted **CSCA Master Key** (obtained via ICAO PKD).
3. The reader uses the verified **DS Public Key** to validate the signature on the SOD manifest.
4. The reader hashes the physical files on the chip (DG1, DG2, DG15) and ensures they match the hashes listed inside the verified SOD.

> **Key Takeaway:** The Document Signer (DS) Public Key simultaneously verifies two things: that the citizen's personal data is unchanged, and that the Active Authentication Public Key (DG15) is legitimate.

---

## 3. Active Authentication (AA): Anti-Cloning

While Passive Authentication proves the data is authentic, it cannot prevent a bad actor from copying all files (including the signed SOD) and writing them onto a blank, duplicate NFC chip. Active Authentication solves this by proving the physical chip itself is original.

### Key Storage Split on the Chip
To make cloning impossible, the Active Authentication key pair is split across two completely different, isolated memory environments inside the passport chip:

* **The AA Public Key (Stored in DG15):** 
  * Sits in standard flash memory (EEPROM).
  * Openly readable via NFC by any standard passport reader.
  * Protected from alteration because its hash is locked inside the signed SOD manifest (via Passive Authentication).
* **The AA Private Key (Stored in the Secure Enclave):**
  * Sits inside an isolated, tamper-resistant cryptographic co-processor (Secure Element / Smart Card Hardware).
  * **It can never be read, viewed, or exported.** The chip's operating system lacks any command to output this key. It can only be utilized internally by the hardware processor to sign incoming data.

### Why the Private Key Cannot Be Extracted
The hardware chip acts as a tiny computer with active defense mechanisms:
1. **Crypto-Isolation:** The private key never travels across the chip's internal communication buses. Calculations happen entirely within the registers of the secure enclave.
2. **Physical Hardware Shields:** The transistors storing the private key are covered by a microscopic, active electronic mesh layer. If a fraudster attempts to physically cut, drill, or probe the chip under a microscope, the mesh circuit breaks, triggering an instant self-destruct that erases the private key.
3. **Side-Channel Protection:** To prevent hackers from guessing the key by analyzing power consumption or electromagnetic emissions during math operations, the chip uses "blinding" algorithms that inject random noise and power fluctuations.

### Active Authentication Verification Flow
Once Passive Authentication confirms that the AA Public Key in DG15 is legitimate, the challenge-response protocol executes as follows:

1. **The Challenge:** The airport border control reader generates a completely random, single-use number (nonce) and transmits it to the passport chip over NFC.
2. **The Black Box Calculation:** The passport's internal processor receives the random number, sends it into the isolated Secure Enclave, encrypts/signs it using the hidden **AA Private Key**, and sends the resulting digital signature back to the reader.
3. **The Proof:** The reader decrypts and verifies the signature using the trusted **AA Public Key** extracted from DG15. 

If the signature matches the random challenge, the chip is proven authentic. If a fraudster clones the passport files onto a standard blank chip, the cloned chip will lack the unreadable hardware private key. When challenged, it will fail to produce a valid signature, causing the system to flag the passport as a clone.


<ExerciseNav />
