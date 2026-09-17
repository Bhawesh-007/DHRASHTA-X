#!/usr/bin/env sh

# Source this file from the repository root: `. scripts/android-env.sh`.
if [ -n "${ZSH_VERSION:-}" ]; then
    _DHRASHTA_ENV_FILE="${(%):-%N}"
else
    _DHRASHTA_ENV_FILE="${BASH_SOURCE[0]:-$0}"
fi

_DHRASHTA_ROOT="$(CDPATH= cd -- "$(dirname -- "$_DHRASHTA_ENV_FILE")/.." && pwd)"
export JAVA_HOME="$_DHRASHTA_ROOT/.tooling/jdk-17"
export ANDROID_HOME="$_DHRASHTA_ROOT/.tooling/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_USER_HOME="$_DHRASHTA_ROOT/.tooling/android-user-home"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

unset _DHRASHTA_ENV_FILE
unset _DHRASHTA_ROOT
