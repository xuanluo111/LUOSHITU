#!/bin/zsh
# S1 冒烟闸门：加壳 jar 在 mobile flavor(debug) 下 Init.init → ≥1 个 csp_ 源真调用返回内容。
# 前置：arm64 真机已连接（adb 可见）、Android SDK、JDK 21。x86_64 模拟器结果无效（提示词 3.11-C / A47）。
# 用法：scripts/smoke-jar.sh <jar地址> <api>   例：scripts/smoke-jar.sh https://…/spider.jar csp_WoGGGuard
# jar 地址必须是家属配置实际指向的地址；本脚本与 App 均不内置任何配置（合规红线）。
# 自动化：extras 预填 + auto=1，结果经 logcat SMOKE_RESULT 回读并判定。

set -euo pipefail
cd "$(dirname "$0")/.."

export PATH="/opt/homebrew/share/android-commandlinetools/platform-tools:$PATH"
JAR_URL="${1:?用法: smoke-jar.sh <jar地址> <api>}"
API="${2:?用法: smoke-jar.sh <jar地址> <api>}"

[[ "$(adb devices | awk 'NR==2{print $2}')" == "device" ]] || { echo "没有已授权的 arm64 真机"; exit 1; }
ABI=$(adb shell getprop ro.product.cpu.abi)
[[ "$ABI" == arm64* ]] || { echo "设备 ABI=$ABI 非 arm64，加壳源不可用，结果无效"; exit 1; }

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"
sh ./gradlew :app:assembleMobileDebug --console=plain -q
adb install -r app/build/outputs/apk/mobile/debug/app-mobile-arm64-v8a-debug.apk

adb logcat -c
adb shell am force-stop com.fongmi.android.tv
adb shell am start -n com.fongmi.android.tv/com.luoshitu.video.smoke.SmokeActivity \
  --es jar "$JAR_URL" --es key "$API" --es api "$API" --ez auto true >/dev/null

echo "冒烟运行中（下载+解密+真调用，可能数分钟）…"
RESULT=""
for i in $(seq 1 120); do
  LINE=$(adb logcat -d -s JarSmoke:I 2>/dev/null | grep "SMOKE_RESULT" | tail -1 || true)
  if [[ -n "$LINE" ]]; then RESULT="$LINE"; break; fi
  sleep 3
done

if [[ -z "$RESULT" ]]; then
  echo "结果: 超时未取得 SMOKE_RESULT（10 分钟），请检查网络或手工查看设备冒烟页"
  exit 2
fi

echo "$RESULT" | sed 's/^.*SMOKE_RESULT /SMOKE_RESULT /'
if echo "$RESULT" | grep -q "ok=true"; then
  echo "判定：通过 ✓（S1 冒烟闸门满足）"
else
  echo "判定：失败 ✗（把上面 detail 抄进交付报告）"
  exit 1
fi
