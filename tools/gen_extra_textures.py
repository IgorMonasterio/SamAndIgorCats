"""Generates the textures that aren't cat coats: Naru's crown and El Negrito's glowing eyes."""
import os

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets', 'samcats', 'textures', 'entity')

GOLD = (242, 194, 48, 255)
GOLD_LIGHT = (255, 226, 110, 255)
GOLD_DARK = (196, 140, 26, 255)
GEM = (214, 34, 52, 255)

# Same eye pixels as gen_cat_textures.py
EYES = [(5, 6), (6, 6), (8, 6), (9, 6)]
EYE_GLOW = (255, 225, 70, 255)


def crown():
    im = Image.new('RGBA', (16, 16), GOLD)
    px = im.load()
    for x in range(16):
        px[x, 4] = GOLD_DARK            # bottom edge of the band's sides
        px[x, 0] = GOLD_LIGHT           # top of the band
    for x in range(16):
        for y in (6, 7, 9):
            px[x, y] = GOLD_LIGHT       # tips of the points
    px[1, 11] = GEM                     # gem on the front point
    px[1, 10] = GOLD_LIGHT
    im.save(os.path.join(ROOT, 'crown.png'))


def negrito_eyes():
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    px = im.load()
    for p in EYES:
        px[p] = EYE_GLOW
    im.save(os.path.join(ROOT, 'el_negrito_eyes.png'))


if __name__ == '__main__':
    crown()
    negrito_eyes()
    print('ok')
