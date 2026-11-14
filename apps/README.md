# Vendored launcher sources

The launchers are built from source in this tree. Nothing under `apps/` may
reference a path outside the AOSP checkout; that keeps the manifest a synced
tree enough to reproduce a build.

## LeanbackLauncher (packaged)

`LeanbackLauncher/` is the `:app` module of the LeanbackLauncher-main Gradle
project (`com.dismal.android.leanbacklauncher`), sources copied from
`src/main` with the Gradle state (`.gradle/`, `build/`, `local.properties`)
left out. `Android.bp` builds it as a privileged, platform-signed system app
and lists the AOSP launchers in its `overrides`, so packaging it removes
Launcher2/Launcher3/Launcher3QuickStep/Go and TvLauncher from the image.
`device.mk` also filters those names out of `PRODUCT_PACKAGES`.

`Launcher3M/` is the Gradle project's `:launcher3-m` library
(`com.marshmallow.launcher`), which supplies the all-apps drawer. It had no
manifest (the namespace lived in `build.gradle`), so a minimal
`AndroidManifest.xml` was added to give its resources a package name. Its
`implementation` dependencies map onto this tree's androidx prebuilts plus
`libprotobuf-java-nano` for `backup/nano`.

## TVLauncher (vendored, not packaged by default)

`TVLauncher/` is Allwinner's reference Android TV launcher
(`com.android.tv.launcher`) from the H618 vendor drop, with its `Android.bp`.
`device.mk` carries the `PRODUCT_PACKAGES += TvLauncher` line commented out;
enable it instead of LeanbackLauncher, not alongside it. It compiles against
AOSP 14 as vendored here.

### Hidden-API bridges

The launcher predates the updatable Bluetooth and Connectivity modules, so it
calls members that only exist on the device at runtime. Module classes reach an
app as the stubs inside `framework.jar`, which shadow the modules' implementation
jars, so the hidden members cannot be linked. `util/BluetoothCompat.java` and
`util/EthernetCompat.java` call them reflectively instead, and each call logs and
degrades rather than throwing:

- `BluetoothA2dp`/`BluetoothHidHost.connect(BluetoothDevice)` and
  `setPriority(device, PRIORITY_AUTO_CONNECT)` for forcing a profile connection
  and marking it for reconnect after a reboot.
- `BluetoothAdapter.cancelBondProcess(BluetoothDevice)`, with a fallback to the
  older `BluetoothDevice.cancelPairing()`.
- `EthernetManager.getConfiguration(String)` / `setConfiguration(String,
  IpConfiguration)` for the Ethernet static-IP screen. When they are
  unavailable, `load()` keeps the DHCP configuration it was constructed with.

Two APIs were removed outright rather than hidden, so the call sites changed:
`BluetoothDevice.convertPinToBytes` is reimplemented (hex string to bytes, same
null-on-invalid contract), and the HDMI-CEC option keys in `InputsActivity` use
their `Settings.Global` string values directly. `IUserSwitchObserver` gained
`onBeforeUserSwitching(int)`, now overridden as a no-op.
