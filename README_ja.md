[English version](README.md)

# structlog-timber

[![Maven Central](https://img.shields.io/maven-central/v/io.github.casl0/structlog-timber-core)](https://central.sonatype.com/search?q=g:io.github.casl0+structlog-timber)
[![codecov](https://codecov.io/github/CASL0/structlog-timber/graph/badge.svg?token=13QUMCR321)](https://codecov.io/github/CASL0/structlog-timber)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Android 向け構造化ロギングライブラリ。[Timber](https://github.com/JakeWharton/timber) をベースに、ログ呼び出しごとにキー・バリューのフィールドを付与し、Logcat や Crashlytics、任意の送信先へルーティングします。

```kotlin
StructuredTimber.i(
    "Purchase completed",
    "item_id" to "SKU-123",
    "price" to 1980,
    "currency" to "JPY",
)
// Logcat 出力:
// I/StructuredLog: Purchase completed {item_id=SKU-123, price=1980, currency=JPY}
```

## 主な機能

- **ログごとのキー・バリューフィールド** -- `Pair<String, Any?>` の可変長引数でメッセージと一緒に渡せます。
- **スレッドローカルコンテキスト (MDC)** -- `StructuredLog.putContext()` で一度セットすれば、同一スレッド上の全ログに自動付与されます。
- **グローバルフィールド** -- `StructuredTree` で `app_version` や `build_type` など、全エントリに共通の値を設定できます。
- **プラグイン可能な Sink アーキテクチャ** -- Logcat と Firebase Crashlytics 用の Sink を同梱。`Sink` インターフェースを実装すれば独自の送信先を追加できます。
- **優先度フィルタリング** -- Sink ごとに `isLoggable()` で最低ログレベルを制御できます。
- **未使用時のオーバーヘッドゼロ** -- フィールドは `ThreadLocal` に保持され、`StructuredTree` が plant されている場合のみ消費されます。
- **純 Kotlin のコアモジュール** -- core モジュールは Timber のみに依存し、Android フレームワーク非依存です。

## セットアップ

### Gradle

必要なモジュールを `build.gradle.kts` に追加します。

```kotlin
dependencies {
    // 必須 -- コア API
    implementation("io.github.casl0:structlog-timber-core:<version>")

    // 任意 -- Logcat 用 Sink
    implementation("io.github.casl0:structlog-timber-logcat:<version>")

    // 任意 -- Firebase Crashlytics 用 Sink
    implementation("io.github.casl0:structlog-timber-crashlytics:<version>")
}
```

### モジュール一覧

| モジュール | アーティファクト | 概要 |
|------------|------------------|------|
| `structlog-timber-core` | `io.github.casl0:structlog-timber-core` | コア API: `StructuredTimber`, `StructuredTree`, `StructuredLog`, `Sink` |
| `structlog-timber-logcat` | `io.github.casl0:structlog-timber-logcat` | `LogcatSink` -- 構造化ログを Android Logcat に出力 |
| `structlog-timber-crashlytics` | `io.github.casl0:structlog-timber-crashlytics` | `CrashlyticsSink` -- フィールドを Crashlytics のカスタムキーとして送信 |

### 動作要件

- Android API 26 以上
- Java 17 以上

## クイックスタート

### 1. Tree を plant する

`Application.onCreate()` で `StructuredTree` を設定します。

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(
            StructuredTree(
                sinks = listOf(LogcatSink(minPriority = Log.DEBUG)),
                globalFields = mapOf("app_version" to BuildConfig.VERSION_NAME),
            )
        )
    }
}
```

### 2. フィールド付きでログを出力する

キー・バリューのペアを可変長引数として渡します。

```kotlin
StructuredTimber.d(
    "Activity created",
    "lifecycle" to "onCreate",
    "has_saved_state" to (savedInstanceState != null),
)
```

### 3. スレッドローカルコンテキストを設定する

コンテキスト値を設定すると、同一スレッド上の全ログに自動的に付与されます。

```kotlin
StructuredLog.putContext("screen", "main")
StructuredLog.putContext("user_id", userId)

// 以下のログには screen と user_id が自動で含まれる
StructuredTimber.i("Item viewed", "item_id" to "SKU-456")
StructuredTimber.i("Item added to cart", "item_id" to "SKU-456")
```

不要になったコンテキストは削除します。

```kotlin
StructuredLog.removeContext("user_id")  // 単一キーを削除
StructuredLog.clearContext()            // 全キーを削除
```

### 4. タグを指定する

特定のログにタグを付けられます。

```kotlin
StructuredTimber.tag("Checkout")
    .w("Slow payment response", "latency_ms" to 3200, "gateway" to "stripe")
```

### 5. 例外をログに記録する

`e()` の第 1 引数に `Throwable` を渡します。

```kotlin
StructuredTimber.e(
    RuntimeException("Something went wrong"),
    "Unexpected error",
    "error_code" to "E001",
)
```

### 6. カスタム Sink を実装する

`Sink` インターフェースを実装して、任意の送信先にログを転送できます。

```kotlin
class DatadogSink : Sink {
    override fun isLoggable(priority: Int): Boolean = priority >= Log.INFO

    override fun emit(entry: StructuredLogEntry) {
        // entry.message と entry.fields をバックエンドに送信
    }
}
```

Tree を plant する際に登録します。

```kotlin
Timber.plant(
    StructuredTree(
        sinks = listOf(LogcatSink(), DatadogSink()),
    )
)
```

## フィールドのマージ順序

`StructuredTree` は 3 つのレイヤーからフィールドをマージします。キーが重複した場合、優先度の高いレイヤーの値が使われます。

| 優先度 | ソース | 設定方法 |
|--------|--------|----------|
| 1 (最低) | グローバルフィールド | `StructuredTree(globalFields = ...)` |
| 2 | スレッドローカルコンテキスト | `StructuredLog.putContext()` |
| 3 (最高) | ログごとのフィールド | `StructuredTimber.d("msg", "key" to value)` |

## ドキュメント

- [アーキテクチャ](docs/ARCHITECTURE.md) -- モジュール構成、データフロー、設計原則
- [コーディングガイドライン](docs/CODING_GUIDELINES.md) -- Kotlin スタイル規約
- [セキュリティポリシー](docs/SECURITY.md) -- OWASP MASVS に基づくセキュリティ指針
- [Git ワークフロー](docs/GIT_WORKFLOW.md) -- ブランチ戦略、コミット規約、リリースプロセス
- [KDoc API リファレンス](https://casl0.github.io/structlog-timber/) -- 自動生成 API ドキュメント

## コントリビューション

コントリビューションを歓迎します。開発環境の構築方法やコーディング規約、プルリクエストの手順については [CONTRIBUTING.md](CONTRIBUTING.md) を参照してください。

## ライセンス

```
Copyright 2026 CASL0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
