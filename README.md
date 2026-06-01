# 屏幕扫码助手 (Screen QR Scanner)

一款 Android 屏幕二维码识别工具，无需切换应用即可快速识别屏幕上的二维码。

## 功能特点

- 📱 **悬浮窗扫码** - 在任意界面显示悬浮按钮，一键扫码
- 🔍 **屏幕截图识别** - 自动捕获屏幕并识别二维码
- 🔔 **快捷设置入口** - 支持从通知栏快捷设置启动
- 📋 **智能结果处理** - 支持复制、分享、打开链接等操作
- 🎯 **多种码类型** - 支持网址、WiFi、电话、邮箱、文本等

## 使用方式

### 方式一：悬浮窗
1. 打开应用，点击「开启悬浮窗」
2. 屏幕上会出现紫色圆形悬浮按钮
3. 看到二维码时，点击悬浮按钮即可识别
4. 可拖动悬浮按钮调整位置

### 方式二：快捷设置
1. 下拉通知栏，编辑快捷设置
2. 添加「屏幕扫码」快捷开关
3. 点击即可快速启动扫码

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **扫码**: Google ML Kit Barcode Scanning
- **屏幕捕获**: MediaProjection API
- **最低版本**: Android 8.0 (API 26)

## 项目结构

```
app/src/main/java/com/example/screenqrscanner/
├── MainActivity.kt              # 主界面
├── ScanResultActivity.kt        # 扫码结果展示
├── ScreenCaptureActivity.kt     # 屏幕捕获处理
├── ScannerApp.kt                # Application 类
├── service/
│   ├── FloatingWindowService.kt # 悬浮窗服务
│   └── QrScannerTileService.kt  # 快捷设置 Tile
└── ui/theme/
    ├── Theme.kt                 # 主题配置
    └── Type.kt                  # 字体配置
```

## 权限说明

| 权限 | 用途 |
|------|------|
| `FOREGROUND_SERVICE` | 保持悬浮窗服务运行 |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | 屏幕录制捕获 |
| `SYSTEM_ALERT_WINDOW` | 显示悬浮窗 |
| `POST_NOTIFICATIONS` | 显示前台服务通知 |

## 构建说明

```bash
# 克隆项目后使用 Android Studio 打开
# 或使用命令行构建
./gradlew assembleDebug
```

## 注意事项

1. 首次使用需要授予「悬浮窗」和「屏幕录制」权限
2. 屏幕录制权限每次都需要用户确认（系统限制）
3. 建议在 Android 8.0 及以上系统使用

## 许可证

MIT License
