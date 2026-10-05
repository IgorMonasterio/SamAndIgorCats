"""Generates lang entries, spawn egg models and the cat-album advancements for every cat."""
import json
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources') + '/'
ASSETS = ROOT + 'assets/samcats/'
DATA = ROOT + 'data/samcats/'

# id, name EN, name ES
CATS = [
    ('naru', 'Naru', 'Naru'),
    ('ivy', 'Ivy', 'Ivy'),
    ('batman', 'Batman', 'Batman'),
    ('bonzo', 'Bonzo', 'Bonzo'),
    ('calcetin', 'Calcetín', 'Calcetín'),
    ('cheeto', 'Cheeto', 'Cheeto'),
    ('dolores', 'Dolores', 'Dolores'),
    ('el_abuelo', 'El Abuelo', 'El Abuelo'),
    ('el_bebe', 'El Bebé', 'El Bebé'),
    ('sin_nombre', 'El gato sin nombre', 'El gato sin nombre'),
    ('el_negrito', 'El Negrito', 'El Negrito'),
    ('itlerina', 'Itlerina pero con Hache', 'Itlerina pero con Hache'),
    ('kalessi', 'Kalessi', 'Kalessi'),
    ('la_trico', 'La Trico', 'La Trico'),
    ('lince', 'Lince', 'Lince'),
    ('mia', 'Mía', 'Mía'),
    ('noah', 'Noah', 'Noah'),
    ('nube', 'Nube', 'Nube'),
    ('oliver', 'Oliver', 'Oliver'),
    ('stripey', 'Stripey', 'Stripey'),
    ('valentino', 'Valentino', 'Valentino'),
]


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write('\n')


def is_cat_key(k):
    return (k.startswith('entity.samcats.') or k.startswith('samcats.cat.') or k.startswith('samcats.owner.')
            or (k.startswith('item.samcats.') and k.endswith('_spawn_egg')))


def lang(code, en):
    path = ASSETS + 'lang/' + code + '.json'
    with open(path, encoding='utf-8') as f:
        data = {k: v for k, v in json.load(f).items() if not is_cat_key(k)}
    for cid, n_en, n_es in CATS:
        name = n_en if en else n_es
        data['entity.samcats.' + cid] = name
        data['item.samcats.' + cid + '_spawn_egg'] = (name + ' Spawn Egg') if en else ('Huevo generador de ' + name)
    data['samcats.msg.ran_away'] = "%s ran away... back tomorrow." if en else "%s se ha escapado... volverá mañana."
    data['advancements.samcats.cats.root.title'] = 'SamAndIgorCats'
    data['advancements.samcats.cats.root.description'] = (
        "The cat album. Tame each cat to add it." if en else "El álbum de gatos. Doma a cada uno para añadirlo.")
    data['advancements.samcats.cats.tame'] = "Tame %s" if en else "Doma a %s"
    data['advancements.samcats.cats.all.title'] = "The Whole Family" if en else "La familia al completo"
    data['advancements.samcats.cats.all.description'] = (
        ("Tame all %d cats in the album" if en else "Doma a los %d gatos del álbum") % len(CATS))
    write_json(path, dict(sorted(data.items())))


def tame(cid):
    return {'trigger': 'minecraft:tame_animal', 'conditions': {'entity': {'type': 'samcats:' + cid}}}


def main():
    lang('en_us', True)
    lang('es_es', False)

    for cid, *_ in CATS:
        write_json(ASSETS + 'models/item/' + cid + '_spawn_egg.json', {'parent': 'item/template_spawn_egg'})

    adv = DATA + 'advancements/cats/'
    write_json(adv + 'root.json', {
        'display': {
            'icon': {'item': 'samcats:naru_spawn_egg'},
            'title': {'translate': 'advancements.samcats.cats.root.title'},
            'description': {'translate': 'advancements.samcats.cats.root.description'},
            'background': 'minecraft:textures/block/orange_terracotta.png',
            'show_toast': False, 'announce_to_chat': False},
        'criteria': {'tick': {'trigger': 'minecraft:tick'}}})
    for cid, *_ in CATS:
        write_json(adv + cid + '.json', {
            'parent': 'samcats:cats/root',
            'display': {
                'icon': {'item': 'samcats:' + cid + '_spawn_egg'},
                'title': {'translate': 'entity.samcats.' + cid},
                'description': {'translate': 'advancements.samcats.cats.tame',
                                'with': [{'translate': 'entity.samcats.' + cid}]},
                'frame': 'task', 'show_toast': True, 'announce_to_chat': True},
            'criteria': {'tamed': tame(cid)}})
    write_json(adv + 'all.json', {
        'parent': 'samcats:cats/root',
        'display': {
            'icon': {'item': 'minecraft:cod'},
            'title': {'translate': 'advancements.samcats.cats.all.title'},
            'description': {'translate': 'advancements.samcats.cats.all.description'},
            'frame': 'challenge', 'show_toast': True, 'announce_to_chat': True},
        'criteria': {cid: tame(cid) for cid, *_ in CATS},
        'requirements': [[cid] for cid, *_ in CATS],
        'rewards': {'experience': 500}})
    print('cats:', len(CATS))


if __name__ == '__main__':
    main()
