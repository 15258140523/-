# Homepage Robot Android Shell

这是一个独立的 Android WebView 壳项目，启动后直接打开：

`http://172.19.8.25:5173/home`

这个文件夹可以单独作为 GitHub 仓库提交，不需要提交完整 H5 项目。

## 构建方式

1. 用 Android Studio 打开当前文件夹。
2. 等待 Gradle Sync 完成。
3. 点击 Run 安装到设备，或执行 `Build > Build Bundle(s) / APK(s) > Build APK(s)`。

## GitHub 远程打包

把当前文件夹作为一个新仓库提交到 GitHub 后，会自动运行 Actions。

也可以在 GitHub 仓库页面进入 `Actions > Build Android APK > Run workflow` 手动打包，产物名称为 `homepage-robot-debug-apk`。

APK 文件在 Actions 运行完成后的 `Artifacts` 里下载。

当前壳层已配置：

- 横屏启动
- 全屏沉浸式显示
- HTTP 明文访问
- WebView JavaScript / DOM Storage / 混合内容
- 返回键优先返回 WebView 历史
