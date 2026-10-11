#!/usr/bin/env python3
"""
Génère le guide d'utilisation (src/main/resources/com/musigma/guide/guide.pdf) à partir de guide/guide.html.

Usage, depuis n'importe quel dossier :
    python3 guide/generer.py             # PDF à partir des captures existantes
    python3 guide/generer.py --captures  # régénère aussi les captures d'écran (ouvre Musigma hors de l'écran)

Nécessite Google Chrome (ou Chromium) et pdftotext (poppler-utils).
"""
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

RACINE = Path(__file__).resolve().parent.parent
GUIDE = RACINE / 'guide'
TRAVAIL = RACINE / 'target' / 'guide'
SORTIE = RACINE / 'src' / 'main' / 'resources' / 'com' / 'musigma' / 'guide' / 'guide.pdf'


def chrome():
    for nom in ('google-chrome', 'chromium', 'chromium-browser'):
        if shutil.which(nom):
            return nom
    sys.exit('Chrome ou Chromium est nécessaire pour produire le PDF')


def captures():
    """Compile Musigma puis lance CapturesGuide, qui enregistre les captures dans guide/images."""
    mvnw = str(RACINE / ('mvnw.cmd' if sys.platform == 'win32' else 'mvnw'))
    classpath = TRAVAIL / 'classpath.txt'
    TRAVAIL.mkdir(parents=True, exist_ok=True)
    subprocess.run(['sh', mvnw, '-q', 'compile', 'dependency:build-classpath',
                    f'-Dmdep.outputFile={classpath}'], cwd=RACINE, check=True)
    cp = f"{RACINE / 'target' / 'classes'}:{classpath.read_text().strip()}"
    subprocess.run(['javac', '-cp', cp, '-d', str(TRAVAIL), str(GUIDE / 'CapturesGuide.java')], check=True)
    subprocess.run(['java', '-cp', f'{cp}:{TRAVAIL}', 'CapturesGuide', str(GUIDE / 'images')], cwd=RACINE, check=True)


def imprimer(pages, pdf):
    """Produit un PDF de guide.html, avec les numéros de page donnés pour le sommaire."""
    html = (GUIDE / 'guide.html').read_text(encoding='utf-8')
    html = html.replace('<script>', f'<script>window.PAGES = {json.dumps(pages, ensure_ascii=False)};', 1)
    source = GUIDE / '.guide-impression.html'  # dans guide/ pour que les chemins des images restent valides
    source.write_text(html, encoding='utf-8')
    try:
        subprocess.run([chrome(), '--headless=new', '--disable-gpu', '--no-pdf-header-footer',
                        '--run-all-compositor-stages-before-draw', '--virtual-time-budget=5000',
                        f'--print-to-pdf={pdf}', source.as_uri()],
                       check=True, capture_output=True)
    finally:
        source.unlink()


def numeros_de_page(pdf, titres):
    """Retourne la page où apparaît chaque titre, en ignorant la couverture et le sommaire."""
    texte = subprocess.run(['pdftotext', '-layout', str(pdf), '-'], capture_output=True, text=True, check=True).stdout
    pages = texte.split('\f')
    trouves = {}
    for titre in titres:
        motif = re.compile(r'\s+'.join(map(re.escape, titre.split())))
        for numero, page in enumerate(pages[2:], start=3):
            if motif.search(page):
                trouves[titre] = numero
                break
    return trouves


def main():
    if '--captures' in sys.argv:
        captures()
    TRAVAIL.mkdir(parents=True, exist_ok=True)
    html = (GUIDE / 'guide.html').read_text(encoding='utf-8')
    titres = re.findall(r'<h2>(\d+\. [^<]+)</h2>', html) + re.findall(r'<figcaption>([^<]+)</figcaption>', html)
    titres = [t.replace('&rsquo;', '’') for t in titres]

    brouillon = TRAVAIL / 'brouillon.pdf'
    imprimer({}, brouillon)
    pages = numeros_de_page(brouillon, titres)
    manquants = [t for t in titres if t not in pages]
    if manquants:
        print('Titres introuvables dans le PDF :', manquants)
    imprimer(pages, SORTIE)
    print(f'Guide généré : {SORTIE.relative_to(RACINE)}')


if __name__ == '__main__':
    main()
