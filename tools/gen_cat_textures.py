"""Generates the cat coats for SamAndIgorCats from the vanilla 1.20.1 cat textures.
Each cat starts from the vanilla coat closest to the real cat and gets recoloured.
Ivy was made earlier and is not touched here."""
import colorsys, os, random, sys
from PIL import Image

# Folder with the vanilla 1.20.1 cat textures (extract assets/minecraft/textures/entity/cat/ from the client jar).
VANILLA = os.environ.get('VANILLA_CAT_TEXTURES', 'vanilla/cat/')
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets', 'samcats', 'textures', 'entity') + '/'

EYE_OUTER = [(5, 6), (9, 6)]
EYE_INNER = [(6, 6), (8, 6)]
NOSE = [(7, 7)]
FACE = set(EYE_OUTER + EYE_INNER + NOSE)

def load(name):
    return Image.open(VANILLA + name + '.png').convert('RGBA')

def lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]

def clamp(v):
    return max(0, min(255, int(round(v))))

def fur_pixels(im):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            if px[x, y][3] > 0 and (x, y) not in FACE:
                yield x, y

def eyes(im, outer, inner=None):
    px = im.load()
    inner = inner or tuple(clamp(v * 0.75) for v in outer)
    for p in EYE_OUTER: px[p] = outer + (255,)
    for p in EYE_INNER: px[p] = inner + (255,)

def tint(im, fn):
    px = im.load()
    for x, y in fur_pixels(im):
        r, g, b, a = px[x, y]
        px[x, y] = tuple(clamp(v) for v in fn(r, g, b)) + (a,)

def hsv_shift(im, pred, dh=0.0, sm=1.0, vm=1.0, vadd=0.0):
    def f(r, g, b):
        if not pred(r, g, b): return (r, g, b)
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        h = (h + dh) % 1.0; s = min(1, s * sm); v = min(1, v * vm + vadd / 255)
        return tuple(c * 255 for c in colorsys.hsv_to_rgb(h, s, v))
    tint(im, f)

def paint(im, rects, color, jitter=8, seed=1):
    rnd = random.Random(seed); px = im.load()
    for (x0, y0, x1, y1) in rects:
        for y in range(y0, y1):
            for x in range(x0, x1):
                if px[x, y][3] > 0 and (x, y) not in FACE:
                    d = rnd.randint(-jitter, jitter)
                    px[x, y] = tuple(clamp(c + d) for c in color) + (255,)

WHITE = (234, 234, 234)
# Regions of the vanilla cat UV layout (64x32)
PAWS = [(40, 9, 48, 12), (44, 0, 46, 2), (8, 18, 16, 21), (12, 13, 14, 15)]
CHEST = [(27, 6, 30, 9), (26, 0, 30, 2)]
TAIL = [(0, 15, 8, 25)]
EARS = [(0, 10, 12, 13)]
SNOUT_LOW = [(2, 27, 5, 28), (5, 24, 8, 26)]

def make(name):
    rnd = random.Random(name)
    if name == 'naru':
        im = load('tabby')
        def grey(r, g, b):
            if max(r, g, b) and (max(r, g, b) - min(r, g, b)) / max(r, g, b) > 0.6: return (r, g, b)
            L = lum((r, g, b)) * 1.05 + 22
            return (L - 4, L, L + 6)
        tint(im, grey)
    elif name == 'batman':
        im = load('all_black'); eyes(im, (205, 205, 70))
    elif name == 'bonzo':
        im = load('all_black'); tint(im, lambda r, g, b: (r * 1.7 + 10, g * 1.35 + 5, b * 0.85)); eyes(im, (240, 205, 55))
    elif name == 'calcetin':
        im = load('all_black'); paint(im, PAWS, WHITE, seed=3); eyes(im, (215, 190, 70))
    elif name == 'cheeto':
        im = load('jellie')
        tint(im, lambda r, g, b: (r, g, b) if lum((r, g, b)) > 150 else (130 + lum((r, g, b)) * 1.45, 108 + lum((r, g, b)) * 1.05, 52 + lum((r, g, b)) * 0.7))
        eyes(im, (140, 200, 70))
    elif name == 'dolores':
        im = load('all_black'); paint(im, CHEST, WHITE, seed=5); eyes(im, (200, 190, 80))
    elif name == 'el_abuelo':
        im = load('siamese')
    elif name == 'el_bebe':
        im = load('tabby')
        hsv_shift(im, lambda r, g, b: True, sm=0.15, vm=1.05, vadd=30); eyes(im, (190, 205, 90))
    elif name == 'sin_nombre':
        im = load('red'); hsv_shift(im, lambda r, g, b: True, sm=0.7, vadd=18); paint(im, PAWS + CHEST, WHITE, seed=9); eyes(im, (150, 200, 80))
    elif name == 'el_negrito':
        im = load('all_black'); tint(im, lambda r, g, b: (r * 0.7, g * 0.7, b * 0.7)); eyes(im, (245, 215, 50))
    elif name == 'itlerina':
        im = load('white')
        black = (24, 22, 30)
        paint(im, EARS + TAIL + SNOUT_LOW, black, seed=7)
        paint(im, [(5, 0, 8, 3), (0, 5, 3, 9), (36, 8, 40, 12), (37, 16, 40, 21), (30, 14, 32, 19)], black, seed=8)
        eyes(im, (200, 210, 90))
    elif name == 'kalessi':
        im = load('calico')
        hsv_shift(im, lambda r, g, b: r - b > 45 and r > g, sm=0.45, vm=0.95)
        tint(im, lambda r, g, b: (r + 70, g + 68, b + 66) if lum((r, g, b)) < 100 else (r, g, b))
        eyes(im, (170, 190, 120))
    elif name == 'la_trico':
        im = load('calico')
    elif name == 'lince':
        im = load('tabby'); hsv_shift(im, lambda r, g, b: True, sm=0.22, vm=0.85); eyes(im, (200, 190, 90))
    elif name == 'mia':
        # Tortoiseshell: soft ginger/brown blotches over black, not speckles.
        im = load('all_black'); px = im.load()
        blobs = [(rnd.randrange(64), rnd.randrange(32), rnd.choice([1, 1, 2])) for _ in range(70)]
        for x, y in fur_pixels(im):
            for bx, by, r in blobs:
                if abs(x - bx) <= r and abs(y - by) <= r - (1 if abs(x - bx) == r else 0):
                    px[x, y] = (rnd.choice([(128, 74, 36), (104, 60, 30), (150, 92, 48)])) + (255,)
                    break
        eyes(im, (235, 190, 60))
    elif name == 'noah':
        im = load('all_black'); tint(im, lambda r, g, b: (52 + r * 1.3, 51 + g * 1.3, 52 + b * 1.05)); eyes(im, (205, 145, 55))
    elif name == 'nube':
        im = load('ragdoll')
        tint(im, lambda r, g, b: (r * 0.72, g * 0.62, b * 0.55) if lum((r, g, b)) < 180 else (r, g, b))
    elif name == 'oliver':
        im = load('british_shorthair'); tint(im, lambda r, g, b: (lum((r, g, b)) + 78, lum((r, g, b)) + 66, lum((r, g, b)) + 44)); eyes(im, (150, 180, 205))
    elif name == 'stripey':
        im = load('tabby'); eyes(im, (205, 205, 80))
    elif name == 'valentino':
        im = load('british_shorthair'); tint(im, lambda r, g, b: (r * 0.85, g * 0.86, b * 0.95)); eyes(im, (235, 165, 40))
    else:
        raise SystemExit('unknown cat ' + name)
    return im

CATS = ['naru', 'batman', 'bonzo', 'calcetin', 'cheeto', 'dolores', 'el_abuelo', 'el_bebe', 'sin_nombre', 'el_negrito',
        'itlerina', 'kalessi', 'la_trico', 'lince', 'mia', 'noah', 'nube', 'oliver', 'stripey', 'valentino']

if __name__ == '__main__':
    for c in CATS:
        make(c).save(OUT + c + '.png')
    print('generated', len(CATS))
