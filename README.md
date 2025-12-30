# statusbar-sdk

Android 状态栏 SDK。提供自定义状态栏视图（网络/蓝牙/时间/电池/标题），支持动态与 XML 两种模式。

## 使用方式

- 直接引入 AAR
  - 下载 GitHub Release 的 `sdk-release.aar` 并放入目标项目 `app/libs/`
  - 在目标项目 `app/build.gradle` 添加：
    ```
    dependencies {
      implementation files('libs/sdk-release.aar')
    }
    ```
- JitPack（推荐）
  - 给仓库打 Tag（例如 `v0.1.0`）
  - 在目标项目 `settings.gradle` 添加：
    ```
    repositories {
      maven { url "https://jitpack.io" }
    }
    ```
  - 在目标项目 `dependencies` 添加：
    ```
    implementation("com.github.lfeilu520:statusbar-sdk:v0.1.0")
    ```
- GitHub Packages（自动发布）
  - 仓库已配置 Actions，推送 Tag `v*` 将自动：
    - 构建 `sdk-release.aar` 并创建 Release 附件
    - 运行 `:sdk:publish` 发布到 GitHub Packages
  - 使用时在 `settings.gradle` 添加：
    ```
    repositories {
      maven {
        url = uri("https://maven.pkg.github.com/lfeilu520/statusbar-sdk")
        credentials {
          username = System.getenv("GITHUB_ACTOR")
          password = System.getenv("GITHUB_TOKEN")
        }
      }
    }
    ```
    然后在 `dependencies`：
    ```
    implementation("com.github.lfeilu520:statusbar-sdk:v0.1.0")
    ```

## 接口

- 初始化：
  ```
  StatusBarController.prepare(activity)
  StatusBarController.install(activity, config)
  ```
- 配置：
  ```
  StatusBarConfig.white(context)
  StatusBarConfig.cameraMode(context)
  ```
- 运行时：
  ```
  StatusBarController.setTitleVisible(activity, true)
  StatusBarController.setTitleText(activity, "标题")
  StatusBarController.setLightMode(activity, true)
  ```

