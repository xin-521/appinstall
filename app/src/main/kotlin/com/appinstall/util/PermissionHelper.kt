package com.appinstall.util

object PermissionHelper {
    
    private val permissionDescriptions = mapOf(
        "android.permission.INTERNET" to "网络访问",
        "android.permission.ACCESS_NETWORK_STATE" to "查看网络状态",
        "android.permission.ACCESS_WIFI_STATE" to "查看WiFi状态",
        "android.permission.CHANGE_WIFI_STATE" to "更改WiFi状态",
        "android.permission.CHANGE_NETWORK_STATE" to "更改网络状态",
        "android.permission.CAMERA" to "相机",
        "android.permission.RECORD_AUDIO" to "录音",
        "android.permission.READ_EXTERNAL_STORAGE" to "读取存储",
        "android.permission.WRITE_EXTERNAL_STORAGE" to "写入存储",
        "android.permission.MANAGE_EXTERNAL_STORAGE" to "管理所有文件",
        "android.permission.READ_MEDIA_IMAGES" to "读取图片",
        "android.permission.READ_MEDIA_VIDEO" to "读取视频",
        "android.permission.READ_MEDIA_AUDIO" to "读取音频",
        "android.permission.ACCESS_FINE_LOCATION" to "精确定位",
        "android.permission.ACCESS_COARSE_LOCATION" to "粗略定位",
        "android.permission.ACCESS_BACKGROUND_LOCATION" to "后台定位",
        "android.permission.READ_CONTACTS" to "读取联系人",
        "android.permission.WRITE_CONTACTS" to "写入联系人",
        "android.permission.GET_ACCOUNTS" to "获取账户",
        "android.permission.READ_PHONE_STATE" to "读取手机状态",
        "android.permission.READ_PHONE_NUMBERS" to "读取手机号码",
        "android.permission.CALL_PHONE" to "拨打电话",
        "android.permission.ANSWER_PHONE_CALLS" to "接听电话",
        "android.permission.READ_CALL_LOG" to "读取通话记录",
        "android.permission.WRITE_CALL_LOG" to "写入通话记录",
        "android.permission.PROCESS_OUTGOING_CALLS" to "处理拨出电话",
        "android.permission.SEND_SMS" to "发送短信",
        "android.permission.RECEIVE_SMS" to "接收短信",
        "android.permission.READ_SMS" to "读取短信",
        "android.permission.RECEIVE_MMS" to "接收彩信",
        "android.permission.RECEIVE_WAP_PUSH" to "接收WAP推送",
        "android.permission.VIBRATE" to "振动",
        "android.permission.WAKE_LOCK" to "唤醒锁",
        "android.permission.RECEIVE_BOOT_COMPLETED" to "开机启动",
        "android.permission.FOREGROUND_SERVICE" to "前台服务",
        "android.permission.FOREGROUND_SERVICE_CAMERA" to "相机前台服务",
        "android.permission.FOREGROUND_SERVICE_LOCATION" to "定位前台服务",
        "android.permission.FOREGROUND_SERVICE_MICROPHONE" to "麦克风前台服务",
        "android.permission.FOREGROUND_SERVICE_DATA_SYNC" to "数据同步前台服务",
        "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" to "媒体播放前台服务",
        "android.permission.REQUEST_INSTALL_PACKAGES" to "安装应用",
        "android.permission.REQUEST_DELETE_PACKAGES" to "卸载应用",
        "android.permission.BLUETOOTH" to "蓝牙",
        "android.permission.BLUETOOTH_ADMIN" to "蓝牙管理",
        "android.permission.BLUETOOTH_CONNECT" to "蓝牙连接",
        "android.permission.BLUETOOTH_SCAN" to "蓝牙扫描",
        "android.permission.BLUETOOTH_ADVERTISE" to "蓝牙广播",
        "android.permission.NFC" to "NFC",
        "android.permission.USE_FINGERPRINT" to "指纹识别",
        "android.permission.USE_BIOMETRIC" to "生物识别",
        "android.permission.USE_FACE_AUTHENTICATION" to "人脸识别",
        "android.permission.READ_CALENDAR" to "读取日历",
        "android.permission.WRITE_CALENDAR" to "写入日历",
        "android.permission.READ_SYNC_SETTINGS" to "读取同步设置",
        "android.permission.WRITE_SYNC_SETTINGS" to "写入同步设置",
        "android.permission.READ_SYNC_STATS" to "读取同步统计",
        "android.permission.AUTHENTICATE_ACCOUNTS" to "验证账户",
        "android.permission.MANAGE_ACCOUNTS" to "管理账户",
        "android.permission.FLASHLIGHT" to "闪光灯",
        "android.permission.SET_WALLPAPER" to "设置壁纸",
        "android.permission.SET_WALLPAPER_HINTS" to "设置壁纸提示",
        "android.permission.EXPAND_STATUS_BAR" to "展开状态栏",
        "android.permission.READ_INPUT_STATE" to "读取输入状态",
        "android.permission.BROADCAST_STICKY" to "发送粘性广播",
        "android.permission.CHANGE_CONFIGURATION" to "更改系统设置",
        "android.permission.MODIFY_AUDIO_SETTINGS" to "修改音频设置",
        "android.permission.MOUNT_UNMOUNT_FILESYSTEMS" to "挂载文件系统",
        "android.permission.SYSTEM_ALERT_WINDOW" to "显示悬浮窗",
        "android.permission.WRITE_SETTINGS" to "修改系统设置",
        "android.permission.DISABLE_KEYGUARD" to "禁用锁屏",
        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" to "请求忽略电池优化",
        "android.permission.ACCESS_NOTIFICATION_POLICY" to "勿扰模式",
        "android.permission.POST_NOTIFICATIONS" to "发送通知",
        "android.permission.SCHEDULE_EXACT_ALARM" to "设置精确闹钟",
        "android.permission.USE_EXACT_ALARM" to "使用精确闹钟",
        "android.permission.HIGH_SAMPLING_RATE_SENSORS" to "高采样率传感器",
        "android.permission.BODY_SENSORS" to "身体传感器",
        "android.permission.BODY_SENSORS_BACKGROUND" to "后台身体传感器",
        "android.permission.ACTIVITY_RECOGNITION" to "活动识别",
        "android.permission.ACCESS_MEDIA_LOCATION" to "访问媒体位置",
        "android.permission.QUERY_ALL_PACKAGES" to "查询所有应用",
        "android.permission.PACKAGE_USAGE_STATS" to "应用使用统计",
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to "无障碍服务",
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" to "通知监听服务",
        "android.permission.BIND_VPN_SERVICE" to "VPN服务",
        "android.permission.BIND_DEVICE_ADMIN" to "设备管理",
        "android.permission.READ_LOGS" to "读取系统日志",
        "android.permission.DUMP" to "系统转储",
        "android.permission.CHANGE_WIFI_MULTICAST_STATE" to "WiFi多播状态",
        "android.permission.NEARBY_WIFI_DEVICES" to "附近WiFi设备",
        "android.permission.UWB_RANGING" to "超宽带测距",
        "android.permission.USB_PERMISSION" to "USB权限",
        "com.android.launcher.permission.INSTALL_SHORTCUT" to "创建快捷方式",
        "com.android.launcher.permission.UNINSTALL_SHORTCUT" to "删除快捷方式",
        "com.android.vending.BILLING" to "应用内购买",
        "com.google.android.c2dm.permission.RECEIVE" to "推送通知",
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION" to "活动识别",
        "com.google.android.providers.gsf.permission.READ_GSERVICES" to "读取Google服务",
        "android.permission.INTERACT_ACROSS_PROFILES" to "跨配置文件交互",
        "android.permission.LAUNCH_MULTI_PANE_SETTINGS_DEEP_LINK" to "启动设置深层链接"
    )
    
    fun getPermissionDescription(permission: String): String {
        return permissionDescriptions[permission] ?: getDefaultDescription(permission)
    }
    
    private fun getDefaultDescription(permission: String): String {
        val shortName = permission.substringAfterLast('.')
        return shortName.replace("_", " ").lowercase().split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
    
    fun formatPermissionList(permissions: List<String>): List<String> {
        return permissions.map { permission ->
            val description = getPermissionDescription(permission)
            if (description != getDefaultDescription(permission)) {
                "$description ($${permission.substringAfterLast('.')})"
            } else {
                description
            }
        }
    }
}
