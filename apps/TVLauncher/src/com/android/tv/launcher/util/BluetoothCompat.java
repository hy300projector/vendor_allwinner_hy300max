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

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidHost;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

/**
 * Reflective bridges to the hidden Bluetooth profile API.
 *
 * <p>The launcher forces A2DP and HID connections and marks them for
 * auto-connect, which only the hidden {@code connect(BluetoothDevice)} and
 * {@code setPriority(BluetoothDevice, int)} calls on {@link BluetoothA2dp} and
 * {@link BluetoothHidHost} do. Those classes reach an app as the public stubs
 * that ship in framework.jar, so the members are not on the compile classpath,
 * but they exist on the device at runtime. The same is true for
 * {@code BluetoothDevice.cancelPairing()} and {@code BluetoothDevice.EXTRA_REASON}.
 *
 * <p>Every call degrades to a log line instead of throwing, so a platform that
 * drops one of these methods loses only the corresponding auto-connect hint.
 */
public final class BluetoothCompat {
    private static final String TAG = "BluetoothCompat";

    /**
     * {@code BluetoothProfile.PRIORITY_AUTO_CONNECT}, the value the hidden
     * {@code setPriority} maps onto {@code CONNECTION_POLICY_ALLOWED}. Written
     * out because the constant is not part of the app's stub API.
     */
    private static final int PRIORITY_AUTO_CONNECT = 1000;

    /**
     * {@code BluetoothDevice.EXTRA_REASON}: the unbond reason extra, same
     * value as {@code EXTRA_UNBOND_REASON}.
     */
    public static final String EXTRA_REASON = "android.bluetooth.device.extra.REASON";

    private BluetoothCompat() {
    }

    /** Connects a profile to {@code device}; false when the platform says no. */
    public static boolean connect(BluetoothProfile profile, BluetoothDevice device) {
        if (profile == null || device == null) {
            return false;
        }
        try {
            Object result = profile.getClass()
                    .getMethod("connect", BluetoothDevice.class)
                    .invoke(profile, device);
            return result instanceof Boolean && (Boolean) result;
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "connect() unavailable on " + profile.getClass().getSimpleName(), e);
            return false;
        }
    }

    /**
     * Marks {@code device} as auto-connect for the profile, which is what makes
     * the device reconnect after a reboot. The priority is not sticky across a
     * reboot; callers re-apply it whenever they connect.
     */
    public static boolean setAutoConnectPriority(BluetoothProfile profile, BluetoothDevice device) {
        if (profile == null || device == null) {
            return false;
        }
        try {
            Object result = profile.getClass()
                    .getMethod("setPriority", BluetoothDevice.class, int.class)
                    .invoke(profile, device, PRIORITY_AUTO_CONNECT);
            return result instanceof Boolean && (Boolean) result;
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "setPriority() unavailable on " + profile.getClass().getSimpleName(), e);
            return false;
        }
    }

    /**
     * Cancels an in-flight pairing. Both spellings are hidden: current
     * platforms put it on {@link BluetoothAdapter#cancelBondProcess} (whose
     * binding is not on the app's classpath) and older ones on
     * {@code BluetoothDevice.cancelPairing()}, so try the current one and fall
     * back to the old one.
     */
    public static boolean cancelPairing(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            try {
                Object result = BluetoothAdapter.class
                        .getMethod("cancelBondProcess", BluetoothDevice.class)
                        .invoke(adapter, device);
                return result instanceof Boolean && (Boolean) result;
            } catch (ReflectiveOperationException e) {
                Log.w(TAG, "BluetoothAdapter.cancelBondProcess() unavailable", e);
            }
        }
        try {
            device.getClass().getMethod("cancelPairing").invoke(device);
            return true;
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "BluetoothDevice.cancelPairing() unavailable", e);
            return false;
        }
    }

    /**
     * Converts a pairing PIN or passkey string to the bytes
     * {@code BluetoothDevice.setPin(byte[])} expects: the string is hex, and an
     * empty, odd-length, over-long or non-hex value is rejected with null, the
     * same contract the removed {@code BluetoothDevice.convertPinToBytes} had.
     */
    public static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        int length = pin.length();
        if (length == 0 || length > 16 || (length % 2) != 0) {
            return null;
        }
        byte[] bytes = new byte[length / 2];
        try {
            for (int i = 0; i < length; i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(pin.substring(i, i + 2), 16);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return bytes;
    }
}
