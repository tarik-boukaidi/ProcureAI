"""Structurer le texte lu par PaddleOCR avec Gemini (sortie JSON imposee par un schema)."""
import os
import re
from datetime import date

from pydantic import BaseModel


class InvoiceLine(BaseModel):
    productName: str
    quantity: int
    unitPrice: float


class InvoiceDraft(BaseModel):
    invoiceNumber: str | None
    invoiceDate: str | None
    supplierIce: str | None
    totalHT: float | None
    tva: float | None
    totalTTC: float | None
    lines: list[InvoiceLine]


PROMPT = """Tu lis le texte OCR d'une facture fournisseur marocaine.
Extrais les champs demandes :
- invoiceNumber : le numero de la facture
- invoiceDate : la date de la facture, au format AAAA-MM-JJ
- supplierIce : l'ICE du fournisseur (15 chiffres), pas celui du client
- totalHT, tva, totalTTC : les montants, au format 1234.50
- lines : les lignes de produits (designation, quantite, prix unitaire HT)
Si une valeur n'apparait pas dans le texte, mets null : n'invente rien.

Texte OCR :
"""

client = None


def is_enabled() -> bool:
    return bool(os.getenv("GEMINI_API_KEY"))


def get_client():
    # Cree une seule fois, au premier document
    global client
    if client is None:
        from google import genai
        from google.genai import types
        client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"),
                              http_options=types.HttpOptions(timeout=30_000))
    return client


def parse_with_gemini(lines: list[str]) -> dict:
    response = get_client().models.generate_content(
        model=os.getenv("GEMINI_MODEL", "gemini-2.5-flash"),
        contents=PROMPT + "\n".join(lines),
        config={
            "response_mime_type": "application/json",
            "response_schema": InvoiceDraft,
            "temperature": 0,
        },
    )
    if response.parsed is None:
        raise ValueError("Gemini did not return the expected JSON")
    return clean(response.parsed.model_dump())


# Un LLM peut inventer une valeur au mauvais format : on la remplace par null
def clean(draft: dict) -> dict:
    if draft["supplierIce"] and not re.fullmatch(r"\d{15}", draft["supplierIce"]):
        draft["supplierIce"] = None
    if draft["invoiceDate"] and not is_iso_date(draft["invoiceDate"]):
        draft["invoiceDate"] = None
    return draft


def is_iso_date(text: str) -> bool:
    try:
        date.fromisoformat(text)
        return True
    except ValueError:
        return False
