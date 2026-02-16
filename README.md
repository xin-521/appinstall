# AppInstall - 应用安装器

一个简洁高效的 Android APK 安装管理工具，支持 APK 文件的解析、安装和卸载功能。

## 功能特性

- **APK 安装**：支持通过文件管理器或浏览器直接打开 APK 文件进行安装
- **APK 解析**：解析并显示 APK 的详细信息（应用名称、包名、版本、大小、权限等）
- **应用卸载**：支持卸载已安装的应用
- **通知提醒**：安装/卸载进度通知提醒
- **现代 UI**：基于 Jetpack Compose 的 Material Design 3 界面

## 技术栈

- **Kotlin** - 主要开发语言
- **Jetpack Compose** - 现代 UI 框架
- **Hilt** - 依赖注入
- **Coroutines** - 异步处理
- **Material Design 3** - UI 设计规范

## 环境要求

- Android 6.0 (API 23) 及以上
- Android Studio Ladybug | 2024.2.1 Patch 2 或更高版本
- JDK 17
- Gradle 8.9

## 构建项目

```bash
# 克隆项目
git clone https://github.com/your-username/AppInstall.git
cd AppInstall

# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease
```

## 项目结构

```
app/
├── src/main/
│   ├── kotlin/com/appinstall/
│   │   ├── di/                    # 依赖注入模块
│   │   ├── installer/             # 安装器实现
│   │   ├── model/                 # 数据模型
│   │   ├── notification/          # 通知管理
│   │   ├── parser/                # APK 解析器
│   │   ├── ui/                    # UI 组件
│   │   ├── util/                  # 工具类
│   │   ├── viewmodel/             # ViewModel
│   │   ├── AppInstallApp.kt       # Application 类
│   │   └── MainActivity.kt        # 主 Activity
│   ├── res/                       # 资源文件
│   └── AndroidManifest.xml        # 清单文件
└── build.gradle.kts               # 模块构建配置
```

## 权限说明

| 权限 | 用途 |
|------|------|
| `REQUEST_INSTALL_PACKAGES` | 请求安装应用 |
| `REQUEST_DELETE_PACKAGES` | 请求卸载应用 |
| `POST_NOTIFICATIONS` | 显示通知 (Android 13+) |
| `FOREGROUND_SERVICE` | 前台服务 |
| `QUERY_ALL_PACKAGES` | 查询已安装应用 |

## 版本历史

### v1.1.0
- UI优化：圆角横幅样式标题栏，移除返回按钮
- 新增APK文件MD5值计算和显示
- 签名和MD5支持点击复制到剪贴板
- 签名采用省略号形式展示
- 添加状态栏安全边界

### v1.0.0
- 初始版本发布
- 支持 APK 安装和卸载
- 显示 APK 详细信息
- 支持中英文界面

## 开源协议

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
