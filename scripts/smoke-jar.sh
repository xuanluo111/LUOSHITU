#!/bin/zsh
# S1 冒烟闸门：加壳 jar 在 mobile flavor(debug) 下 Init.init → ≥1 个 csp_ 源真调用返回内容。
# 前置：arm64 真机已连接（adb 可见）、Android SDK、JDK 21。x86_64 模拟器结果无效（提示词 3.11-C / A47）。
# 用法：scripts/smoke-jar.sh <jar地址> <api>   例：scripts/smoke-jar.sh https://…/spider.jar csp_Bili
# jar 地址必须是家属配置实际指向的地址；本脚本与 App 均不内置任何配置（合规红线）。

set -euo pipefail
cd "$(dirname "$0")/.."

JAR_URL="${1:?用法: smoke-jar.sh <jar地址> <api>}"
API="${2:?用法: smoke-jar.sh <jar地址> <api>}"

[[ "$(adb devices | awk 'NR==2{print $2}')" == "device" ]] || { echo "没有已授权的 arm64 真机"; exit 1; }
ABI=$(adb shell getprop ro.product.cpu.abi)
[[ "$ABI" == arm64* ]] || { echo "设备 ABI=$ABI 非 arm64，加壳源不可用，结果无效"; exit 1; }

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"
sh ./gradlew :app:assembleMobileDebug --console=plain -q
adb install -r app/build/outputs/apk/mobile/debug/app-mobile-arm64-v8a-debug.apk

adb shell am start -n com.fongmi.android.tv/com.luoshitu.video.smoke.SmokeActivity
echo "已在真机打开冒烟页。填入以下内容后点『运行冒烟』："
echo "  jar 地址: $JAR_URL"
echo "  api:     $API"
echo "判定：『结果: 通过 ✓』= S1 闸门满足；失败时把页面文本抄进交付报告。"
