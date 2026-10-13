"""Trouver les champs d'une facture dans les lignes lues par l'OCR."""
import re

AMOUNT = r"\d[\d ]*,\d{2}"  # format marocain : 1 031,50


def parse_invoice(lines: list[str]) -> dict:
    return {
        "invoiceNumber": find_value(lines, r"facture\s*n[°o]", r"[A-Z0-9][A-Z0-9/-]*\d"),
        "invoiceDate": to_iso_date(find_value(lines, r"date", r"\d{2}/\d{2}/\d{4}")),
        "supplierIce": find_value(lines, r"ICE", r"\d{15}"),
        "totalHT": to_number(find_value(lines, r"total\s*HT", AMOUNT)),
        "tva": to_number(find_value(lines, r"TVA", AMOUNT)),
        "totalTTC": to_number(find_value(lines, r"total\s*TTC", AMOUNT)),
        # Les regex ne savent pas lire un tableau : seul Gemini remplit les lignes
        "lines": [],
    }


def find_value(lines: list[str], label: str, value: str) -> str | None:
    """Cherche le libelle, puis la valeur sur la meme ligne ou sur la ligne suivante."""
    for i, line in enumerate(lines):
        match = re.search(label, line, re.IGNORECASE)
        if not match:
            continue
        next_line = lines[i + 1] if i + 1 < len(lines) else ""
        for text in (line[match.end():], next_line):
            found = re.search(value, text)
            if found:
                return found.group()
    return None


def to_iso_date(text: str | None) -> str | None:
    if text is None:
        return None
    day, month, year = text.split("/")
    return f"{year}-{month}-{day}"


def to_number(text: str | None) -> float | None:
    if text is None:
        return None
    return float(text.replace(" ", "").replace(",", "."))
