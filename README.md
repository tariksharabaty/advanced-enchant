<p align="center"><img src=".github/assets/icon.png" width="96" alt="📖 Advanced Enchant"></p>

<h1 align="center">📖 Advanced Enchant</h1>

<p align="center"><b>Next-level enchanting made effortless, elegant, and crystal clear.</b></p>

<p align="center">
  <img src="https://img.shields.io/badge/version-1.1.2-0A7BBB?style=flat-square&labelColor=555555" alt="version 1.1.2">
  <img src="https://img.shields.io/badge/Minecraft-26.3-62B47A?style=flat-square&labelColor=555555" alt="Minecraft 26.3">
  <img src="https://img.shields.io/badge/loader-Fabric-DBD0B4?style=flat-square&labelColor=38342A" alt="loader Fabric">
  <img src="https://img.shields.io/badge/Java-25-E76F00?style=flat-square&labelColor=555555&logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/license-MIT-F1C40F?style=flat-square&labelColor=555555" alt="license MIT">
  <a href="https://github.com/tariksharabaty/o-lib"><img src="https://img.shields.io/badge/requires-O%20Lib-8957E5?style=flat-square&labelColor=555555" alt="requires O Lib"></a>
  <a href="https://modrinth.com/organization/oidioomstudio"><img src="https://img.shields.io/badge/Modrinth-Oidioom%20Studio-1BD96A?style=flat-square&labelColor=555555&logo=modrinth&logoColor=white" alt="Modrinth Oidioom Studio"></a>
  <a href="https://oidioom.com"><img src="https://img.shields.io/badge/by-Oidioom-0D1117?style=flat-square&labelColor=555555" alt="by Oidioom"></a>
</p>

<p align="center"><a href="#-english">🇬🇧 English</a> · <a href="#-türkçe">🇹🇷 Türkçe</a></p>

---

## 🇬🇧 English

> [!IMPORTANT]
> Requires **[O Lib](https://github.com/tariksharabaty/o-lib)** and **Fabric API**.

**Advanced Enchant** overhauls the traditional Minecraft enchanting experience. Say goodbye to random dice-rolls, frustrating enchantment conflicts, and arbitrary anvil level caps. Introducing **The Enchanting Infuser** and **The Advanced Enchanting Infuser** — empowering you to customize, repair, and tailor your gear with surgical precision.

---

## ✨ Key Features

- 📖 **The Enchanting Infuser:** An elegant, intuitive enchanting station where you directly choose which enchantments to apply or remove using your experience points.
- 🌟 **The Advanced Enchanting Infuser:** Designed for late-game mastery. Allows modifying pre-enchanted items, applying treasure/anvil-exclusive enchantments, repairing damaged gear with raw materials, and unlocking higher power ceilings.
- ⚡ **Universal Incompatibility Removal (Configurable):** Freely combine enchantments that vanilla arbitrarily forbids (such as Fortune + Silk Touch, or different Protection variants) if you choose to enable it.
- 🛡️ **Level V Protection Option:** Optional setting to push Protection up to Level 5.
- 💎 **Integrated with O Lib:** Full in-game configuration through O Lib's responsive **Stone UI** via ModMenu.
- 🌐 **Dedicated Server Ready:** Clean client/server architecture built for flawless multiplayer performance without desync or crashes.

---

## 🛠️ Blocks & Mechanics

### 1. Normal Enchanting Infuser
- **Crafting / Acquisition:** Placed in the *Oidioom Studio* & *Functional Blocks* creative tabs.
- **Function:** Enchant unenchanted tools, armor, and weapons.
- **Bookshelf Power:** Boosted by surrounding bookshelves (0 to 15 default).

### 2. Advanced Enchanting Infuser
- **Function:**
  - Enchant already-enchanted items (add or upgrade existing levels).
  - Bookshelf scaling up to 32 bookshelves for maximum enchanting power.
  - Direct repair using matching ingots, diamonds, or materials.
  - Allows book infusing and anvil-tier enchantments.

---

## ⚙️ In-Game Configuration

Configure everything live without restarting your game! Press the **O Lib Mod Settings Hub** hotkey or open **ModMenu**:
- **Remove Incompatibility:** Toggle enchantment conflict restrictions on/off.
- **Max Protection 5:** Toggle Protection V availability.
- **Normal & Advanced Infuser Shelves:** Adjust required minimum and maximum bookshelves.
- **Cost Scaling:** Customize experience cost multipliers and repair efficiency.

---

## 📥 Installation

1. Install **Minecraft 26.3** with **Fabric Loader**.
2. Install **Fabric API**.
3. Install **O Lib** (required core library).
4. Drop `advanced-enchant-1.1.2-mc26.3.jar` into your `.minecraft/mods` folder.

---

## ⬇️ Download

- **Modrinth:** [Oidioom Studio](https://modrinth.com/organization/oidioomstudio)
- **GitHub Releases:** [`advanced-enchant-1.1.2-mc26.3.jar`](../../releases/latest)

## 🔨 Building from source

Requires **JDK 25**.

```bash
# 1) O Lib is a dependency — publish it to your local Maven first
git clone https://github.com/tariksharabaty/o-lib.git
(cd o-lib && ./gradlew publishToMavenLocal)

# 2) build this mod
./gradlew build
# output: build/libs/advanced-enchant-1.1.2-mc26.3.jar
```

## 📄 License

[MIT](LICENSE) © 2026 Oidioom / Tarek Sharabaty

---

## 🇹🇷 Türkçe

**Büyülemeyi zahmetsiz, şık ve anlaşılır hale getiren yeni nesil büyü sistemi.**

> [!IMPORTANT]
> **[O Lib](https://github.com/tariksharabaty/o-lib)** ve **Fabric API** gerektirir.

### ✨ Özellikler

- Enchanting Infuser: istediğiniz büyüyü doğrudan seçip ekleyin veya kaldırın
- Advanced Infuser: büyülü eşyaları düzenleme, hazine büyüleri ve örs sınırlarını aşma
- Vanilla'nın yasakladığı büyü kombinasyonlarını serbest bırakma (ayarlanabilir)
- İsteğe bağlı Koruma V seviyesi
- O Lib ile oyun içi ayarlar, sunucu uyumlu yapı

### 📥 Kurulum

1. **Minecraft 26.3** için **Fabric Loader** kurun.
2. **Fabric API** ve **O Lib** modlarını `mods` klasörüne ekleyin.
3. `advanced-enchant-1.1.2-mc26.3.jar` dosyasını `mods` klasörüne atın ve oyunu başlatın.

### ⬇️ İndir

- **Modrinth:** [Oidioom Studio](https://modrinth.com/organization/oidioomstudio)
- **GitHub Releases:** [`advanced-enchant-1.1.2-mc26.3.jar`](../../releases/latest)

### 📄 Lisans

[MIT](LICENSE) © 2026 Oidioom / Tarek Sharabaty

---

<p align="center"><a href="https://oidioom.com"><img src=".github/assets/oidioom-logo.svg" width="40" alt="Oidioom"></a></p>
<p align="center"><b>by Oidioom — Tarek Sharabaty</b><br>
<a href="https://oidioom.com">oidioom.com</a> · <a href="https://github.com/tariksharabaty">GitHub</a> · <a href="https://modrinth.com/organization/oidioomstudio">Modrinth</a> · <a href="https://gear.oidioom.com">Gear</a> · <a href="https://www.instagram.com/oidioom">Instagram</a></p>
