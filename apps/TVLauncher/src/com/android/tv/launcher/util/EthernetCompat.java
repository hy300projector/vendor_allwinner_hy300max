/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.launcher.util;

import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.util.Log;

/**
 * Reflective bridges to EthernetManager's hidden per-interface configuration
 * API.
 *
 * <p>{@code getConfiguration(String)} and {@code setConfiguration(String,
 * IpConfiguration)} are hidden and therefore absent from the stub
 * {@link EthernetManager} an app compiles against, although the device's
 * Connectivity module still implements them. EthernetManager's visible surface
 * only covers interface availability, so the static/dynamic IP screen keeps
 * using the hidden pair through reflection.
 */
public final class EthernetCompat {
    private static final String TAG = "EthernetCompat";

    private EthernetCompat() {
    }

    /** The interface's current configuration, or null when unavailable. */
    public static IpConfiguration getConfiguration(EthernetManager manager, String iface) {
        if (manager == null || iface == null) {
            return null;
        }
        try {
            Object result = EthernetManager.class
                    .getMethod("getConfiguration", String.class)
                    .invoke(manager, iface);
            return result instanceof IpConfiguration ? (IpConfiguration) result : null;
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "getConfiguration() unavailable for " + iface, e);
            return null;
        }
    }

    /** Applies {@code config} to the interface; false when the call is unavailable. */
    public static boolean setConfiguration(EthernetManager manager, String iface,
            IpConfiguration config) {
        if (manager == null || iface == null || config == null) {
            return false;
        }
        try {
            EthernetManager.class
                    .getMethod("setConfiguration", String.class, IpConfiguration.class)
                    .invoke(manager, iface, config);
            return true;
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "setConfiguration() unavailable for " + iface, e);
            return false;
        }
    }
}
