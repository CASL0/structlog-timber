# Git Workflow

This document defines the Git workflow and conventions for the
structlog-timber project.

---

## Branching Strategy: GitHub Flow

This project follows
[GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow)
--- a simple branch-based workflow.

### Rules

1. `main` is always deployable.
2. Create a feature branch from `main` for every change.
3. Open a Pull Request to merge back into `main`.
4. After review and CI pass, squash-merge the PR.
5. Delete the feature branch after merge.

### Branch Naming

Use a descriptive kebab-case name with an optional type prefix:

```
feature/add-datadog-sink
fix/thread-safety-in-structured-log
chore/update-dependencies
```

---

## Commit Messages: Conventional Commits

All commits must follow the
[Conventional Commits](https://www.conventionalcommits.org/) specification.
This is required for Release Please to automatically determine version bumps
and generate changelogs.

### Format

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types

| Type | Purpose | Version Bump |
|------|---------|-------------|
| `feat` | New feature | minor |
| `fix` | Bug fix | patch |
| `docs` | Documentation only | none |
| `style` | Formatting, no code change | none |
| `refactor` | Code change that neither fixes a bug nor adds a feature | none |
| `test` | Adding or correcting tests | none |
| `chore` | Maintenance tasks | none |
| `ci` | CI/CD configuration changes | none |
| `build` | Build system or dependency changes | none |

### Breaking Changes

Append `!` after the type/scope to indicate a breaking change. This triggers
a **major** version bump.

```
feat!: remove deprecated LogcatSink constructor

BREAKING CHANGE: LogcatSink now requires minPriority parameter.
```

### Scope (optional)

Use a scope to clarify which area of the codebase is affected:

```
fix(ci): add --repo flag to gh pr merge
build(deps): update Timber to 5.1.0
feat(crashlytics): add custom key filtering
```

### Examples

```
feat: add minPriority constructor parameter to LogcatSink
fix: exclude sample module from Kover coverage aggregation
docs: add Kotlin coding guidelines based on Android style guide
ci: use GitHub App token in Release Please workflow
build: configure Maven Central publishing for all library modules
refactor: extract magic string to DEFAULT_TAG constant in LogcatSink
test: add unit tests for CrashlyticsSink with MockK
chore: add GitHub issue templates for bug reports and feature requests
```

---

## Pull Requests

### Creating a PR

Use the `gh` CLI to create Pull Requests:

```bash
# Push your branch and create a PR
gh pr create --title "feat: add DatadogSink" --body "$(cat <<'EOF'
## Summary
- Add DatadogSink implementation
- Configure Datadog API client with constructor injection

## Test plan
- [ ] Unit tests for DatadogSink.emit()
- [ ] Unit tests for DatadogSink.isLoggable()
- [ ] Integration test with StructuredTree
EOF
)"
```

### PR Title

The PR title must follow the Conventional Commits format, as squash-merging
uses the PR title as the commit message.

```
feat: add DatadogSink implementation
fix: prevent NPE when tag is null in LogcatSink
```

### PR Description

Include a concise summary of changes and a test plan:

```markdown
## Summary
- <bullet points describing what changed and why>

## Test plan
- [ ] <checklist of testing steps>
```

### Merge Strategy

All PRs are **squash-merged** into `main`. This keeps the commit history
clean and ensures each merge corresponds to a single Conventional Commit.

### Review Checklist

Before requesting review, verify:

- [ ] Code follows [CODING_GUIDELINES.md](CODING_GUIDELINES.md)
- [ ] All public APIs have KDoc
- [ ] Tests pass locally (`./gradlew testDebugUnitTest`)
- [ ] Build succeeds (`./gradlew assembleDebug`)
- [ ] Formatting is correct (`./gradlew spotlessCheck`)
- [ ] PR title follows Conventional Commits format

---

## Release Process

Releases are fully automated via
[Release Please](https://github.com/googleapis/release-please).

### How It Works

1. Push commits to `main` following Conventional Commits.
2. Release Please automatically creates/updates a release PR with:
   - Version bump in `gradle.properties`
   - Generated `CHANGELOG.md` entries
3. When the release PR is merged, Release Please creates a GitHub Release
   with a git tag (e.g., `v1.0.2`).
4. The publish workflow triggers and deploys artifacts to Maven Central.

### Version Bumps

Release Please determines the version bump from commit types:

| Commit Type | Example | Bump |
|---|---|---|
| `fix:` | `fix: prevent NPE` | `1.0.0` -> `1.0.1` |
| `feat:` | `feat: add DatadogSink` | `1.0.0` -> `1.1.0` |
| `feat!:` or `BREAKING CHANGE:` | `feat!: redesign Sink API` | `1.0.0` -> `2.0.0` |

### No Manual Steps Required

- Do **not** manually edit `VERSION_NAME` in `gradle.properties`.
- Do **not** manually create GitHub Releases or tags.
- Do **not** manually update `CHANGELOG.md`.

Release Please handles all of this automatically.

---

## Dependency Updates

[Renovate](https://docs.renovatebot.com/) is configured to automatically
create PRs for dependency updates.

- **Minor and patch** updates are auto-merged.
- **Major** updates require manual review.
- All dependency PRs are labeled with `dependencies`.

---

## References

- [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Release Please](https://github.com/googleapis/release-please)
- [Renovate](https://docs.renovatebot.com/)
