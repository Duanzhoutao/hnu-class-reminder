# 海大智能签到提醒助手

这是一个面向海南大学学生的 Android 课表提醒应用。导入教务系统导出的 Excel 课表后，App 会解析课程、计算周次，并在课前、上课时、课后发送本地提醒。

## 下载

- 最新发布页：
  - [GitHub Releases](https://github.com/Duanzhoutao/hnu-class-reminder/releases/latest)
- 一键下载 APK：
  - [下载最新 APK](https://github.com/Duanzhoutao/hnu-class-reminder/releases/latest/download/hnu-class-reminder-latest-debug.apk)

## 当前功能

- 导入海大 `.xls` / `.xlsx` 课表
- 解析课程名、教师、地点、周次、节次
- 支持同一单元格内多门课拆分
- 从 Excel 第一列读取节次时间
- 设置第一周周一并自动计算当前周次
- 展示今日课程和本周课表
- 支持单节课提醒开关
- 支持今日免打扰
- 支持课前、上课、课后 3 类提醒
- 支持普通 / 标准 / 强提醒 3 档提醒方式
- 支持节假日跳过
- 支持调试页查看提醒、通知日志和解析异常

## 技术栈

- Kotlin
- Jetpack Compose
- MVVM + Repository
- Room
- DataStore
- Hilt
- AlarmManager + BroadcastReceiver
- WorkManager
- Apache POI

## 项目结构

```text
app/
  src/main/java/com/hainanu/signinassistant/
    alarm/
    data/
    di/
    domain/
    notification/
    receiver/
    ui/
  src/main/assets/holidays/
```

## 本地运行

1. 安装 Android Studio 和 Android SDK。
2. 在 `local.properties` 中配置本机 SDK 路径。
3. 运行：

```powershell
.\gradlew.bat assembleDebug
```

4. 安装到 Android 8.0 及以上设备。

## 导入教程

1. 登录 [https://jxgl.hainanu.edu.cn/](https://jxgl.hainanu.edu.cn/)
2. 右边选择栏进入 `培养管理 -> 学期课表`
3. 点击导出
4. 回到 App，选择导出的 Excel 文件导入

## 第一周设置

在设置页选择学期第一周的周一日期。设置后会立即影响：

- 首页当前周次
- 本周课表
- 今日课程判断
- 本地提醒调度

当前内置已知默认值：

- `2025-2026-2` 第一周周一：`2026-03-02`

当前内置节假日：

- `2026-04-05`
- `2026-05-01`
- `2026-06-19`

如果要加入补课日，可以扩展对应学期 JSON：

```json
{
  "date": "2026-04-11",
  "type": "MAKEUP_WORKDAY",
  "makeupWeekday": 5,
  "title": "周六补周五的课"
}
```

## 提醒机制

- 优先使用 `AlarmManager.setExactAndAllowWhileIdle()`
- 没有精确闹钟权限时会自动降级，App 不会崩溃
- 导入课表、修改设置、重启手机、修改系统时间后会自动重建提醒
- 使用 WorkManager 维护未来 14 天提醒窗口

## 隐私说明

- 个人课表文件不会上传，数据完全本地，不会泄露。
- 节假日数据与用户课表数据分开存储

## 已知限制

- 当前只适配海南大学课表格式
- 底部备注类课程只展示，不参与提醒
- 铃声目前使用系统通知音开关
- 强提醒暂不包含全屏弹窗
- 如果设备里已经存了错误的第一周周一，需要在设置页手动修正或重新导入
