#!/bin/bash

# Android APK 构建脚本
# 此脚本用于在没有完整 Android Studio 环境时构建 APK

echo "=========================================="
echo "  屏幕扫码助手 - APK 构建脚本"
echo "=========================================="
echo ""

# 检查必要工具
check_requirements() {
    echo "[1/5] 检查环境..."
    
    if ! command -v java &> /dev/null; then
        echo "❌ 未找到 Java，请先安装 JDK"
        exit 1
    fi
    
    echo "✓ Java 版本: $(java -version 2>&1 | head -n 1)"
    
    # 检查 Android SDK
    if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
        echo "⚠️  未设置 Android SDK 环境变量"
        echo "   尝试查找默认位置..."
        
        # 常见 SDK 位置
        SDK_PATHS=(
            "$HOME/Android/Sdk"
            "$HOME/android-sdk"
            "/usr/lib/android-sdk"
            "/opt/android-sdk"
        )
        
        FOUND_SDK=""
        for path in "${SDK_PATHS[@]}"; do
            if [ -d "$path" ]; then
                FOUND_SDK="$path"
                break
            fi
        done
        
        if [ -n "$FOUND_SDK" ]; then
            echo "✓ 找到 SDK: $FOUND_SDK"
            export ANDROID_SDK_ROOT="$FOUND_SDK"
        else
            echo "❌ 未找到 Android SDK"
            echo ""
            echo "请安装 Android SDK 或设置 ANDROID_SDK_ROOT 环境变量"
            echo "下载地址: https://developer.android.com/studio#command-tools"
            exit 1
        fi
    fi
    
    echo ""
}

# 下载 Gradle Wrapper
download_gradle() {
    echo "[2/5] 准备 Gradle..."
    
    if [ ! -f "gradlew" ]; then
        echo "  创建 Gradle Wrapper..."
        
        # 创建 gradle wrapper 文件
        mkdir -p gradle/wrapper
        
        cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
        
        # 下载 gradle wrapper jar
        GRADLE_WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
        
        if command -v curl &> /dev/null; then
            curl -L -o gradle/wrapper/gradle-wrapper.jar "$GRADLE_WRAPPER_URL" 2>/dev/null || echo "  ⚠️  下载 wrapper jar 失败，将尝试其他方式"
        elif command -v wget &> /dev/null; then
            wget -q -O gradle/wrapper/gradle-wrapper.jar "$GRADLE_WRAPPER_URL" 2>/dev/null || echo "  ⚠️  下载 wrapper jar 失败，将尝试其他方式"
        fi
        
        # 创建 gradlew 脚本
        cat > gradlew << 'EOF'
#!/bin/sh
exec java -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
EOF
        chmod +x gradlew
        
        echo "✓ Gradle Wrapper 准备完成"
    else
        echo "✓ Gradle Wrapper 已存在"
    fi
    
    echo ""
}

# 编译 APK
build_apk() {
    echo "[3/5] 开始编译 APK..."
    echo "  这可能需要几分钟时间，请耐心等待..."
    echo ""
    
    if [ -f "gradlew" ]; then
        ./gradlew assembleDebug --stacktrace 2>&1 | tee build.log
    else
        echo "❌ 未找到 gradlew 脚本"
        exit 1
    fi
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✓ 编译成功！"
    else
        echo ""
        echo "❌ 编译失败，请查看 build.log 了解详情"
        exit 1
    fi
    
    echo ""
}

# 复制 APK 到输出目录
copy_apk() {
    echo "[4/5] 复制 APK 文件..."
    
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        mkdir -p output
        cp "$APK_PATH" "output/屏幕扫码助手.apk"
        echo "✓ APK 已复制到: output/屏幕扫码助手.apk"
        echo ""
        echo "  文件大小: $(du -h "$APK_PATH" | cut -f1)"
    else
        echo "❌ 未找到生成的 APK 文件"
        echo "  预期路径: $APK_PATH"
        exit 1
    fi
    
    echo ""
}

# 显示完成信息
show_completion() {
    echo "[5/5] 构建完成！"
    echo ""
    echo "=========================================="
    echo "  ✅ APK 构建成功！"
    echo "=========================================="
    echo ""
    echo "输出文件: output/屏幕扫码助手.apk"
    echo ""
    echo "安装方法:"
    echo "  1. 将 APK 文件传输到手机"
    echo "  2. 在手机上点击安装"
    echo "  3. 如提示未知来源，请允许安装"
    echo ""
    echo "注意:"
    echo "  - 首次安装需要允许悬浮窗权限"
    echo "  - 扫码时需要授予屏幕录制权限"
    echo ""
}

# 主函数
main() {
    cd "$(dirname "$0")"
    
    check_requirements
    download_gradle
    build_apk
    copy_apk
    show_completion
}

main "$@"
