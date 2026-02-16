package com.appinstall.di

import android.content.Context
import com.appinstall.installer.InstallManager
import com.appinstall.notification.InstallNotificationManager
import com.appinstall.parser.ApkParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideApkParser(@ApplicationContext context: Context): ApkParser {
        return ApkParser(context)
    }
    
    @Provides
    @Singleton
    fun provideInstallNotificationManager(@ApplicationContext context: Context): InstallNotificationManager {
        return InstallNotificationManager(context)
    }
    
    @Provides
    @Singleton
    fun provideInstallManager(
        @ApplicationContext context: Context,
        apkParser: ApkParser,
        notificationManager: InstallNotificationManager
    ): InstallManager {
        return InstallManager(context, apkParser, notificationManager)
    }
}
