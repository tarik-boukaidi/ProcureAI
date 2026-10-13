"""Lire le texte d'une facture (image ou PDF) avec PaddleOCR."""
import cv2
import fitz  # PyMuPDF : transforme une page PDF en image
import numpy as np

engine = None


def get_engine():
    # Le modele est lourd : on le charge une seule fois, au premier document
    global engine
    if engine is None:
        from paddleocr import PaddleOCR
        engine = PaddleOCR(lang="fr")
    return engine


def read_text(content: bytes, filename: str) -> list[str]:
    if filename.endswith(".pdf"):
        images = pdf_to_images(content)
    else:
        images = [bytes_to_image(content)]

    lines = []
    for image in images:
        for result in get_engine().predict(image):
            lines += result["rec_texts"]
    return lines


def bytes_to_image(content: bytes):
    image = cv2.imdecode(np.frombuffer(content, np.uint8), cv2.IMREAD_COLOR)
    if image is None:
        raise ValueError("Unreadable image")
    return image


def pdf_to_images(content: bytes):
    images = []
    with fitz.open(stream=content, filetype="pdf") as pdf:
        for page in pdf:
            png = page.get_pixmap(dpi=200).tobytes("png")
            images.append(bytes_to_image(png))
    return images
