# Security Policy

This document defines the security policy for the structlog-timber library,
based on the [OWASP Mobile Application Security Verification Standard (MASVS)](https://mas.owasp.org/MASVS/).
Only controls relevant to the library's scope are addressed.

## Compliance

- **Standard:** OWASP MASVS v2
- **Verification Level:** L1 (baseline security)

> structlog-timber is a Timber extension library focused on structured logging.
> Controls outside the library's responsibility (e.g., full-app authentication) are noted as out of scope.

---

## MASVS-STORAGE: Storage

> *"Mobile applications handle a wide variety of sensitive data, such as personally identifiable information (PII), cryptographic material, secrets, and API keys, that often need to be stored locally."*
>
> --- [OWASP MASVS - MASVS-STORAGE](https://mas.owasp.org/MASVS/05-MASVS-STORAGE/)

### MASVS-STORAGE-1: The app securely stores sensitive data.

> *"Apps handle sensitive data coming from many sources such as the user, the backend, system services or other apps on the device and usually need to store it locally."*

**Policy:**

- The library does not persist `StructuredLogEntry` data to the file system or database.
- Log data is held only temporarily in memory (ThreadLocal) and discarded immediately after delegation to Sinks.
- Documentation provides masking guidelines for Sink implementors handling sensitive fields.

### MASVS-STORAGE-2: The app prevents leakage of sensitive data.

> *"There are cases when sensitive data is unintentionally stored or exposed to publicly accessible locations; typically as a side-effect of using certain APIs, system capabilities such as backups or logs."*

**Policy:**

- **Primary control** — preventing sensitive data leakage via logs is the core responsibility of this library.
- Provide a filtering/masking mechanism for values stored in `StructuredLog` context (MDC).
- Allow control of log output levels between debug and release builds for Logcat output.
- Provide filters that automatically exclude sensitive fields (passwords, tokens, etc.) when sending to external services such as Crashlytics.

---

## MASVS-CRYPTO: Cryptography

> *"Cryptography is essential for mobile apps because mobile devices are highly portable and can be easily lost or stolen."*
>
> --- [OWASP MASVS - MASVS-CRYPTO](https://mas.owasp.org/MASVS/06-MASVS-CRYPTO/)

### MASVS-CRYPTO-1 / MASVS-CRYPTO-2

**Policy:**

- The library does not perform any cryptographic operations directly.
- If log data encryption is required, it is the responsibility of the Sink implementation.
- The library does not hold or manage cryptographic keys or secrets.

---

## MASVS-AUTH: Authentication and Authorization

> *"Authentication and authorization are essential components of most mobile apps, especially those that connect to a remote service."*
>
> --- [OWASP MASVS - MASVS-AUTH](https://mas.owasp.org/MASVS/07-MASVS-AUTH/)

### MASVS-AUTH-1 / MASVS-AUTH-2 / MASVS-AUTH-3

**Policy:**

- The library does not perform any authentication or authorization.
- Documentation provides best practices to prevent sensitive credentials (auth tokens, session IDs, etc.) from being included in log fields.

---

## MASVS-NETWORK: Network Communication

> *"Secure networking is a critical aspect of mobile app security, particularly for apps that communicate over the network."*
>
> --- [OWASP MASVS - MASVS-NETWORK](https://mas.owasp.org/MASVS/08-MASVS-NETWORK/)

### MASVS-NETWORK-1: The app secures all network traffic according to the current best practices.

**Policy:**

- The library itself does not perform any network communication.
- Guidelines require TLS for any Sink implementation that transmits logs over the network.

### MASVS-NETWORK-2: The app performs identity pinning for all remote endpoints under the developer's control.

**Policy:**

- For Sinks that delegate to external SDKs (e.g., Crashlytics), network security is governed by the respective SDK's configuration.
- Certificate pinning is recommended for custom Sinks that communicate with remote endpoints.

---

## MASVS-PLATFORM: Platform Interaction

> *"The security of mobile apps heavily depends on their interaction with the mobile platform."*
>
> --- [OWASP MASVS - MASVS-PLATFORM](https://mas.owasp.org/MASVS/09-MASVS-PLATFORM/)

### MASVS-PLATFORM-1 / MASVS-PLATFORM-2 / MASVS-PLATFORM-3

**Policy:**

- The library does not use IPC, WebViews, or UI rendering.
- Log data is not exposed externally via IPC mechanisms.

---

## MASVS-CODE: Code Quality

> *"Mobile apps have many data entry points, including the UI, IPC, network, and file system, which might receive data that has been inadvertently modified by untrusted actors."*
>
> --- [OWASP MASVS - MASVS-CODE](https://mas.owasp.org/MASVS/10-MASVS-CODE/)

### MASVS-CODE-1: The app requires an up-to-date platform version.

**Policy:**

- `minSdk` is set appropriately to target platform versions that receive security patches.

### MASVS-CODE-2: The app has a mechanism for enforcing app updates.

**Policy:**

- Out of scope — this is the responsibility of the consuming application.

### MASVS-CODE-3: The app only uses software components without known vulnerabilities.

**Policy:**

- Dependencies (Timber, Firebase Crashlytics, etc.) are updated regularly.
- GitHub Dependabot or similar tools are used to detect and address known vulnerabilities in dependencies.

### MASVS-CODE-4: The app validates and sanitizes all untrusted inputs.

**Policy:**

- Log messages and field values are sanitized appropriately.
- Sink implementations must guard against log injection attacks (e.g., stripping newline characters).

---

## MASVS-RESILIENCE: Resilience Against Reverse Engineering and Tampering

> *"Defense-in-depth measures such as code obfuscation, anti-debugging, anti-tampering, etc. are important to increase app resilience against reverse engineering and specific client-side attacks."*
>
> --- [OWASP MASVS - MASVS-RESILIENCE](https://mas.owasp.org/MASVS/11-MASVS-RESILIENCE/)

### MASVS-RESILIENCE-1 through MASVS-RESILIENCE-4

**Policy:**

- Out of scope — this is the responsibility of the consuming application.
- The library is compatible with ProGuard / R8 obfuscation and provides the necessary ProGuard rules.

---

## MASVS-PRIVACY: Privacy

> *"The main goal of MASVS-PRIVACY is to provide a baseline for user privacy."*
>
> --- [OWASP MASVS - MASVS-PRIVACY](https://mas.owasp.org/MASVS/12-MASVS-PRIVACY/)

### MASVS-PRIVACY-1: The app minimizes access to sensitive data and resources.

> *"Apps should only request access to the data they absolutely need for their functionality and always with informed consent from the user."*

**Policy:**

- The library does not request any additional Android permissions.
- Log collection occurs only through Sinks explicitly configured by the consumer.

### MASVS-PRIVACY-2: The app prevents identification of the user.

**Policy:**

- The library does not automatically collect user-identifying information (device IDs, etc.).
- Whether to include user-identifying information in the logging context is left to the consumer's discretion.

### MASVS-PRIVACY-3: The app is transparent about data collection and usage.

**Policy:**

- Documentation clearly describes what data is sent to each Sink.
- The library does not perform background data collection.

### MASVS-PRIVACY-4: The app offers user control over their data.

**Policy:**

- `StructuredLog.clearLogContext()` allows consumers to clear all context data.
- Sinks can be dynamically added or removed, giving consumers full control over log destinations.

---

## References

- [OWASP MASVS](https://mas.owasp.org/MASVS/)
- [OWASP MASTG (Mobile Application Security Testing Guide)](https://mas.owasp.org/MASTG/)
- [OWASP MASVS GitHub Repository](https://github.com/OWASP/masvs)
