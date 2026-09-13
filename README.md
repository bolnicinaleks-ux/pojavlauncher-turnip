<h1 align="center">PojavLauncher Turnip Edition</h1>

<p align="center">
  <b>A specialized PojavLauncher fork optimized for Snapdragon Adreno GPUs with custom Turnip driver support and modern Vulkan performance.</b>
</p>

<p align="center">
  <a href="https://github.com/bolnicinaleks-ux/pojavlauncher-turnip/actions"><img src="https://img.shields.io/github/actions/workflow/status/bolnicinaleks-ux/pojavlauncher-turnip/android.yml?branch=main&label=Android%20CI" alt="CI Status"></a>
  <a href="https://github.com/bolnicinaleks-ux/pojavlauncher-turnip/commits/main"><img src="https://img.shields.io/github/commit-activity/m/bolnicinaleks-ux/pojavlauncher-turnip" alt="GitHub commit activity"></a>
  <a href="https://github.com/bolnicinaleks-ux/pojavlauncher-turnip/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-LGPLv3-blue.svg" alt="License"></a>
</p>

---

## 🌟 Key Features of Turnip Edition

- 🚀 **Upgraded Built-in Turnip Driver (Mesa 25.0-devel)**:
  Comes with a fresh build of the open-source Freedreno/Turnip Vulkan driver, unlocking maximum rendering performance and compatibility on Qualcomm Snapdragon Adreno 6xx/7xx series GPUs.
- 📦 **Custom Turnip Driver Installer & Picker**:
  Full in-app Turnip driver management inspired by modern Android emulation software. Install, switch, and delete custom Turnip builds (`.so` or `.zip` driver archives) right from the app settings without root or modifying APK files.
- ⚡ **Zink & VulkanMod Compatibility**:
  Optimized bionic linker namespace isolation and LWJGL Vulkan loader hooks, ensuring seamless execution of Minecraft with Zink (OpenGL over Vulkan) as well as direct Vulkan mods like **VulkanMod**.
- 🔄 **One-Click Fallback**:
  Easily switch between the built-in Mesa 25 Turnip driver, any installed custom driver, or the proprietary Qualcomm System Vulkan driver.

---

## 📑 Table of Contents

- [Introduction](#introduction)
- [How to Use Custom Turnip Drivers](#how-to-use-custom-turnip-drivers)
- [Building from Source](#building-from-source)
- [Compatibility](#compatibility)
- [Credits & Upstream](#credits--upstream)
- [License](#license)

---

## 📖 Introduction

**PojavLauncher Turnip Edition** is a fork of [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher), specifically tuned for Android devices with Qualcomm Snapdragon chipsets (Adreno 600 and 700 series). 

While stock PojavLauncher contains an older Turnip build and limited flexibility for graphics experimentation, this edition allows users to harness the cutting edge of Mesa Turnip developments to achieve higher framerates, better shader compatibility, and reduced graphical artifacts.

---

## 🛠️ How to Use Custom Turnip Drivers

You can easily install third-party Turnip builds (such as builds by Kimocoder, Weab-chan, or Mesa CI):

1. Open **PojavLauncher Turnip Edition**.
2. Go to **Settings** (⚙️) ➔ **Miscellaneous** (*Разное*).
3. Tap on **Turnip graphics drivers** (*Графические драйверы Turnip*).
4. Tap **Install driver** (*Установить драйвер*) and select your driver file via the system file picker:
   - Supported formats:
     - Direct shared library: `libvulkan_freedreno.so` (or any `.so`)
     - Driver packages: `.zip` archives containing the driver library
5. Once imported, select the driver from the list and tap **Set default** (*По умолчанию*) to make it active.
6. Launch Minecraft!

> [!TIP]
> If a custom driver crashes or causes rendering issues on your specific GPU, simply return to the **Turnip Driver Manager** in settings and select **Built-in Turnip** or **System Vulkan driver**.

---

## 🏗️ Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK & NDK (r25c or higher recommended)
- Git

### Build Steps

1. Clone this repository:
   ```bash
   git clone https://github.com/bolnicinaleks-ux/pojavlauncher-turnip.git
   cd pojavlauncher-turnip
   ```

2. Build GLFW stub:
   ```bash
   ./gradlew :jre_lwjgl3glfw:build
   ```

3. Build the launcher debug APK:
   ```bash
   ./gradlew :app_pojavlauncher:assembleDebug
   ```

The built APK will be located at:
`app_pojavlauncher/build/outputs/apk/debug/app_pojavlauncher-debug.apk`

---

## 🎮 Compatibility

- **Target Architectures**: `arm64-v8a` (required for Turnip Vulkan driver).
- **Android Version**: Android 10 (API level 29) or higher is required for Mesa 25 Turnip and namespace loader support.
- **Supported GPUs**: Qualcomm Adreno 6xx and 7xx series (Snapdragon 845, 855, 865, 870, 888, 7+ Gen 2, 8 Gen 1/2/3, etc.).

---

## 🤝 Credits & Upstream

- [PojavLauncherTeam/PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher) — The original PojavLauncher project and team.
- [Mesa 3D Graphics Library / Freedreno & Turnip](https://gitlab.freedesktop.org/mesa/mesa) — Open-source Vulkan driver for Adreno hardware.
- [Boardwalk](https://github.com/zhuowei/Boardwalk) — Original Android Java edition launcher.
- [LWJGL](https://www.lwjgl.org/) — Lightweight Java Game Library.
- Community driver builders (Kimocoder, Weab-chan, and other Mesa contributors).

---

## 📄 License

This project is licensed under the **GNU Lesser General Public License v3.0 (LGPLv3)** — see the [LICENSE](LICENSE) file for details.
