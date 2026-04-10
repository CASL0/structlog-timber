# Changelog

## [2.1.1](https://github.com/CASL0/structlog-timber/compare/v2.1.0...v2.1.1) (2026-04-10)


### Bug Fixes

* **deps:** update dependency com.google.firebase:firebase-crashlytics to v20.0.5 ([#24](https://github.com/CASL0/structlog-timber/issues/24)) ([4d2ac02](https://github.com/CASL0/structlog-timber/commit/4d2ac025f46d5466777f590953eba86e32885f70))

## [2.1.0](https://github.com/CASL0/structlog-timber/compare/v2.0.0...v2.1.0) (2026-04-06)


### Features

* **core:** add StructuredTimber.init() convenience method ([001ab9c](https://github.com/CASL0/structlog-timber/commit/001ab9c3eb38c1f594fb7e6ccb8446aaf8180fbd))
* **core:** add StructuredTimber.init() convenience method ([0ca185f](https://github.com/CASL0/structlog-timber/commit/0ca185f4c7b585b6b6498a110b03ab7a3eb705a3))


### Bug Fixes

* **core:** make init() idempotent and add require guard ([e42ce56](https://github.com/CASL0/structlog-timber/commit/e42ce56f063b886252847629a4315f565e0a4045))

## [2.0.0](https://github.com/CASL0/structlog-timber/compare/v1.1.1...v2.0.0) (2026-04-06)


### ⚠ BREAKING CHANGES

* putContext(), removeContext(), clearContext() are renamed to putLogContext(), removeLogContext(), clearLogContext()
* StructuredLog.withFields() is renamed to StructuredLog.withLogContext()

### Code Refactoring

* rename putContext/removeContext/clearContext to putLogContext/removeLogContext/clearLogContext ([f081314](https://github.com/CASL0/structlog-timber/commit/f081314faa0edbbb8512673eb3acbae786cce3ec))
* rename withFields to withLogContext in StructuredLog ([657d291](https://github.com/CASL0/structlog-timber/commit/657d29145a1cb302a29ff0b6459b01cafa29b1d5))

## [1.1.1](https://github.com/CASL0/structlog-timber/compare/v1.1.0...v1.1.1) (2026-04-05)


### Bug Fixes

* **deps:** update dependency com.google.firebase:firebase-crashlytics to v20 ([5c91dd7](https://github.com/CASL0/structlog-timber/commit/5c91dd7d1ee7edfd4f8c1752c854a9995193cd5d))
* **deps:** update dependency com.google.firebase:firebase-crashlytics to v20 ([84dc6f4](https://github.com/CASL0/structlog-timber/commit/84dc6f4b62e9f967ec6d76ea2b484601e56642af))
* remove deprecated SonatypeHost parameter from publishToMavenCentral ([89a7733](https://github.com/CASL0/structlog-timber/commit/89a7733eba2f39d64fa026dc2d027026d14f9a1e))

## [1.1.0](https://github.com/CASL0/structlog-timber/compare/v1.0.2...v1.1.0) (2026-04-03)


### Features

* add StructuredLog.withContext for scoped context management ([18040ce](https://github.com/CASL0/structlog-timber/commit/18040cec42e3d91d1bb2e24f6d247769ed28b511))
* add StructuredLog.withContext for scoped context management ([34d8b44](https://github.com/CASL0/structlog-timber/commit/34d8b44a8db68e06081eafdfdc90ba2952470907))


### Bug Fixes

* snapshot context before mutation in withFields to handle duplicate keys ([3c84c87](https://github.com/CASL0/structlog-timber/commit/3c84c8707c20e270e1ec6ac24a80fa4946823b7b))

## [1.0.2](https://github.com/CASL0/structlog-timber/compare/v1.0.1...v1.0.2) (2026-04-03)


### Bug Fixes

* **ci:** add --repo flag to gh pr merge and explicit labels to config ([18a9dae](https://github.com/CASL0/structlog-timber/commit/18a9daeff90a2989b082864029d6f32a1d5f18cd))
* **ci:** avoid fromJSON error when release PR is not created ([78ba010](https://github.com/CASL0/structlog-timber/commit/78ba01026911fed590e3682bd52cd8c9690dd768))
* **deps:** update dependency androidx.appcompat:appcompat to v1.7.1 ([#4](https://github.com/CASL0/structlog-timber/issues/4)) ([9bf8538](https://github.com/CASL0/structlog-timber/commit/9bf8538e68921306dad5da049ff84f67a6cf3fd9))
* **deps:** update dependency com.google.firebase:firebase-crashlytics-ktx to v19.4.4 ([#5](https://github.com/CASL0/structlog-timber/issues/5)) ([3761e4d](https://github.com/CASL0/structlog-timber/commit/3761e4d9b5ee3bc7fe46b6a42ec4f8d65f2c4bcf))
* prevent failing Sink from blocking subsequent Sinks in StructuredTree ([056f4aa](https://github.com/CASL0/structlog-timber/commit/056f4aaf7fc1bc17e8745c33b7a327818ee7372b))

## [1.0.1](https://github.com/CASL0/structlog-timber/compare/v1.0.0...v1.0.1) (2026-04-02)


### Bug Fixes

* use block markers for release-please version annotation ([34cc50c](https://github.com/CASL0/structlog-timber/commit/34cc50c561dc05da6c49bafb4c91d3d14ded69ec))

## 1.0.0 (2026-04-02)


### Features

* add core structured logging API ([d9b2baa](https://github.com/CASL0/structlog-timber/commit/d9b2baa62880e60dc81dd4e5872226a4bf587a12))
* add CrashlyticsSink for Firebase Crashlytics integration ([430bfb4](https://github.com/CASL0/structlog-timber/commit/430bfb4a8d86856456ea2e436b0ee292e6f8aaa2))
* add Kover for merged code coverage reporting ([b0af939](https://github.com/CASL0/structlog-timber/commit/b0af93997c7cd8d78548b7cf772a4c294463613c))
* add LogcatSink as a separate module ([e9907a1](https://github.com/CASL0/structlog-timber/commit/e9907a1bf7f9aeee6649875261a936c53b3f02e0))
* add minPriority constructor parameter to LogcatSink ([c682e4a](https://github.com/CASL0/structlog-timber/commit/c682e4a589a6b32d857b2d2041c0dbe287e72482))
* add Spotless with ktfmt (Google style) for Kotlin/KTS formatting ([d47a739](https://github.com/CASL0/structlog-timber/commit/d47a7393d013b57631e52ecced3863b593861844))


### Bug Fixes

* **ci:** add chmod +x gradlew to test workflow ([35c322e](https://github.com/CASL0/structlog-timber/commit/35c322eec5496f7725f63d91cb5dbfd9cdd1558c))
* exclude sample module from Kover coverage aggregation ([30557af](https://github.com/CASL0/structlog-timber/commit/30557af8729edce772f7413ba571318e6d3cad1c))
