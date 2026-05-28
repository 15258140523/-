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
- 个推 Android SDK，AppID 为 `Q8yvSODiJv9xTIrZbMK7A9`
- H5 可通过 `window.AndroidBridge.getGetuiClientId()` 获取 CID，也可监听 `getui-cid` 事件
- 个推透传消息会由原生 Service 直接弹系统通知，App 页面不在前台时也不会只停留在站内消息
- 推送通知使用高优先级渠道和全屏 Intent，离线收到消息时会尽量直接弹出提醒页

## 杀死 App 后仍要收到通知

测试后台发送时请使用“通知栏消息”或“透传消息”，并保持“允许离线下发”开启。当前壳已经做了透传消息的原生通知兜底：只要个推 SDK 的 Service 被唤醒收到消息，就会直接发系统通知。

Android 14 及以上系统可能需要单独打开“全屏通知”权限。App 首次启动时会自动跳转一次对应设置页，如果仍只显示通知栏，请在系统设置里允许“机器人节点监控”的全屏通知/弹出提醒权限。

如果用户在系统设置里强行停止应用，或部分国产系统清后台后限制自启动，普通个推通道可能不会被唤醒。生产环境要进一步在个推控制台配置厂商通道（华为/荣耀/小米/OPPO/vivo/魅族等），并在应用市场/厂商后台创建对应推送应用后把参数填入个推后台。

注意：`Master Secret` 只应放在服务端，例如 `getui-push-service`，不要写进 Android APK。
