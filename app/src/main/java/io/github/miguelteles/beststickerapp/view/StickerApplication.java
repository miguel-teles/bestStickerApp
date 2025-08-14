/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.view;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.HashSet;
import java.util.Set;

import io.github.miguelteles.beststickerapp.services.StickerImageConvertionWarmer;

public class StickerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());
    }

    public static class AppLifecycleTracker implements Application.ActivityLifecycleCallbacks {

        private final Set<Activity> currentActivity;

        public AppLifecycleTracker() {
            this.currentActivity = new HashSet<>();
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            warmImageConvertion();
            currentActivity.add(activity);
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            warmImageConvertion();
            currentActivity.add(activity);
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            currentActivity.remove(activity);
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }

        private void warmImageConvertion() {
            if (currentActivity.isEmpty()) {
                StickerImageConvertionWarmer.warm();
            }
        }
    }
}
