from PIL import Image, ImageChops
import os

src = r"D:\workspace\LaterGator\Icon.png"
base = r"D:\workspace\LaterGator\app\src\main\res"

sizes = {
    "mipmap-mdpi":    48,
    "mipmap-hdpi":    72,
    "mipmap-xhdpi":   96,
    "mipmap-xxhdpi":  144,
    "mipmap-xxxhdpi": 192,
}

img = Image.open(src).convert("RGBA")
print(f"Source: {img.size}")

# Bounding box via alpha channel
r, g, b, a = img.split()
bbox = a.point(lambda x: 255 if x > 30 else 0).getbbox()
if not bbox:
    bg = Image.new("RGB", img.size, (255, 255, 255))
    diff = ImageChops.difference(img.convert("RGB"), bg)
    bbox = diff.point(lambda x: 255 if x > 15 else 0).convert("L").getbbox()

print(f"Icon bounding box: {bbox}")

w = bbox[2] - bbox[0]
h = bbox[3] - bbox[1]
side = max(w, h)
cx = (bbox[0] + bbox[2]) // 2
cy = (bbox[1] + bbox[3]) // 2
left  = max(0, cx - side // 2)
top   = max(0, cy - side // 2)
right = min(img.width,  left + side)
bot   = min(img.height, top  + side)
icon  = img.crop((left, top, right, bot))
print(f"Cropped icon: {icon.size}")

# Legacy mipmap PNGs — vollständiges Icon, kein extra Padding
for folder, size in sizes.items():
    out_dir = os.path.join(base, folder)
    os.makedirs(out_dir, exist_ok=True)
    r = icon.resize((size, size), Image.LANCZOS)
    r.save(os.path.join(out_dir, "ic_launcher.png"))
    r.save(os.path.join(out_dir, "ic_launcher_round.png"))
    print(f"  {folder}: {size}x{size}")

# Adaptive foreground: 432x432, Icon auf 78% skaliert und zentriert
# → gesamter Inhalt (inkl. Text) bleibt in der Safe Zone aller Launcher-Formen
CANVAS = 432
icon_size = int(CANVAS * 0.78)  # 337px — gut innerhalb der 66dp Safe Zone
canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
resized = icon.resize((icon_size, icon_size), Image.LANCZOS)
offset = (CANVAS - icon_size) // 2
canvas.paste(resized, (offset, offset), resized)
canvas.save(os.path.join(base, "drawable", "ic_launcher_foreground_bitmap.png"))
print(f"  adaptive foreground: {CANVAS}x{CANVAS} (icon at {icon_size}px centered)")
print("Done!")
