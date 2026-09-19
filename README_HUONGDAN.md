# 🐉 Dragon Boy - Hướng Dẫn Build APK

## 📁 Cấu trúc Project
```
DragonBoy/
├── .github/workflows/build-apk.yml   ← GitHub Actions tự build APK
├── app/src/main/
│   ├── java/com/teammobi/dragonboy/  ← Toàn bộ source code Java
│   │   ├── engine/   (GameEngine, AssetManager, InputHandler, SoundManager)
│   │   ├── entities/ (Hero, Enemy, Platform, Item, Projectile)
│   │   ├── screens/  (MenuScreen, GameScreen)
│   │   ├── utils/    (HUD, LevelBuilder)
│   │   ├── GameThread.java
│   │   ├── GameView.java
│   │   └── MainActivity.java
│   ├── assets/sprites/               ← Đặt ảnh thật vào đây (tùy chọn)
│   │   ├── hero_idle.png             (spritesheet 4 frames ngang)
│   │   ├── hero_run.png              (6 frames)
│   │   ├── hero_attack.png           (4 frames)
│   │   ├── hero_skill.png            (5 frames)
│   │   ├── slime.png                 (4 frames)
│   │   ├── skeleton.png              (4 frames)
│   │   ├── boss.png                  (6 frames)
│   │   ├── tiles.png                 (tileset)
│   │   └── coin.png                  (8 frames)
│   ├── res/values/strings.xml
│   └── AndroidManifest.xml
├── build.gradle
├── settings.gradle
└── gradlew
```

---

## 🖼️ Cách Thêm Ảnh Thật (Sprite Sheets Miễn Phí)

Game **tự chạy được không cần ảnh** (dùng đồ họa canvas tự vẽ).
Khi muốn thêm ảnh thật:

### Nguồn ảnh miễn phí CC0 (dùng thoải mái, không cần credit):
- **Kenney.nl**: https://kenney.nl/assets (tìm "platformer", "characters")
- **OpenGameArt**: https://opengameart.org (filter CC0)
- **itch.io free**: https://itch.io/game-assets/free/tag-cc0

### Cách đặt ảnh:
1. Tải sprite sheet về
2. Đặt vào thư mục `app/src/main/assets/sprites/`
3. Đặt tên file theo danh sách trên
4. Mỗi file là **sprite sheet nằm ngang** (các frame xếp từ trái sang phải)

---

## 🚀 BUILD APK BẰNG GITHUB ACTIONS (Khuyên dùng)

### Bước 1: Tạo Repository GitHub
```bash
# Trên máy tính hoặc termux:
git init
git add .
git commit -m "Dragon Boy v1.0"
git branch -M main
git remote add origin https://github.com/TEN_BAN/dragonboy.git
git push -u origin main
```

### Bước 2: Chờ GitHub tự build
- Vào **github.com/TEN_BAN/dragonboy** → tab **Actions**
- Thấy workflow "Build Dragon Boy APK" đang chạy
- Chờ ~5-8 phút

### Bước 3: Tải APK về
- Click vào workflow run vừa chạy xong
- Kéo xuống phần **Artifacts**
- Click **DragonBoy-debug** → tải về ZIP → giải nén lấy `.apk`
- Cài trực tiếp lên Android!

### Build lại bất kỳ lúc nào:
- Tab Actions → "Build Dragon Boy APK" → **Run workflow** (nút bên phải)

---

## 📱 BUILD & CÀI APK BẰNG TERMUX (Không cần máy tính)

### Bước 1: Cài đặt môi trường Termux
```bash
# Mở Termux, chạy từng lệnh:
pkg update -y
pkg upgrade -y
pkg install -y openjdk-17 git wget unzip

# Cài Android SDK (cách nhẹ nhất)
pkg install -y aapt apksigner dx
```

### Bước 2: Cài Gradle trong Termux
```bash
cd ~
wget https://services.gradle.org/distributions/gradle-8.4-bin.zip
unzip gradle-8.4-bin.zip -d ~/gradle
export PATH=$PATH:~/gradle/gradle-8.4/bin
echo 'export PATH=$PATH:~/gradle/gradle-8.4/bin' >> ~/.bashrc
```

### Bước 3: Clone và Build
```bash
cd ~
git clone https://github.com/TEN_BAN/dragonboy.git
cd dragonboy
chmod +x gradlew
./gradlew assembleDebug
```

### Bước 4: Tìm và cài APK
```bash
# APK nằm ở:
ls app/build/outputs/apk/debug/app-debug.apk

# Copy ra Downloads để dễ tìm:
cp app/build/outputs/apk/debug/app-debug.apk ~/storage/downloads/DragonBoy.apk

# Cài APK (cần storage permission trước):
termux-setup-storage
# Sau đó mở file manager → Downloads → DragonBoy.apk → Install
```

### Lỗi thường gặp trong Termux:
```bash
# Lỗi "SDK not found":
export ANDROID_HOME=$PREFIX/share/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME

# Lỗi Java heap:
export GRADLE_OPTS="-Xmx1024m"

# Lỗi gradlew permission:
chmod +x gradlew
```

---

## 🎮 CÁCH KHỞI ĐỘNG SERVER + CHƠI GAME

Dragon Boy là **game offline hoàn toàn** - KHÔNG cần server.

### Chỉ cần:
1. Cài file `DragonBoy.apk` lên Android
2. Mở app lên → chơi ngay

### Nếu bạn muốn thêm server (multiplayer/leaderboard) sau này:
```bash
# Dùng Python Flask đơn giản:
pip install flask
python3 server.py
# Sửa BASE_URL trong GameConfig.java thành IP máy chủ
```

---

## 🎯 Gameplay

| Nút | Chức năng |
|-----|-----------|
| Joystick trái | Di chuyển trái/phải |
| **B** (xanh) | Nhảy |
| **A** (đỏ) | Tấn công cận chiến |
| **S** (tím) | Phóng lửa rồng (cần MP, cooldown 3s) |

### 3 Level:
- **Level 1**: Đồng bằng - Slime + Skeleton
- **Level 2**: Rừng - Nhiều kẻ địch hơn, bẫy gai nhiều hơn
- **Level 3**: Boss Dragon - Hãy sử dụng kỹ năng!

### Items:
- 💰 **Coin**: Cộng điểm
- ❤️ **HP Orb**: Hồi máu
- 💎 **Gem**: Bonus điểm cao

---

## 🔧 Thêm/Sửa Level

Mở file `utils/LevelBuilder.java`:
```java
private static LevelData buildLevel1(int sw, int sh, AssetManager assets) {
    // Thêm nền đất:
    d.platforms.add(new Platform(x, y, width, height, Platform.Type.GROUND, assets));
    // Thêm quái:
    addEnemy(d, x, y, Enemy.Type.SLIME, assets);    // Slime nhỏ
    addEnemy(d, x, y, Enemy.Type.SKELETON, assets); // Skeleton mạnh hơn
    addEnemy(d, x, y, Enemy.Type.BOSS, assets);     // Boss!
}
```

---

*Dragon Boy v1.0 - TeamMobi*
