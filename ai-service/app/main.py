"""ai-service : lit une facture avec PaddleOCR et renvoie un brouillon.

PaddleOCR lit le texte, Gemini le range dans un JSON structure.
Sans cle Gemini (ou si Gemini echoue), les regex de invoice_parser prennent le relais.
L'utilisateur verifie le brouillon, puis cree la facture avec POST /api/invoices.
"""
import logging
import os

import py_eureka_client.eureka_client as eureka_client
from fastapi import FastAPI, File, Header, HTTPException, UploadFile

from app import llm_parser, ocr
from app.invoice_parser import parse_invoice

log = logging.getLogger("ai-service")

app = FastAPI(title="ai-service")

# S'inscrire dans Eureka : le gateway trouve "ai-service" comme les services Spring
if os.getenv("EUREKA_ENABLED", "true") == "true":
    eureka_client.init(
        eureka_server=os.getenv("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://localhost:8761/eureka"),
        app_name="ai-service",
        instance_port=8091,
    )


@app.post("/api/ai/ocr/invoice")
def read_invoice(file: UploadFile = File(...), x_user_role: str = Header(default=None)):
    # Le comptable saisit les factures fournisseurs
    if x_user_role != "COMPTABLE":
        raise HTTPException(status_code=403, detail="Access denied")

    filename = file.filename.lower()
    if not filename.endswith((".pdf", ".png", ".jpg", ".jpeg")):
        raise HTTPException(status_code=400, detail="Only PDF, PNG or JPG files")

    content = file.file.read()
    if not content:
        raise HTTPException(status_code=400, detail="Empty file")

    try:
        lines = ocr.read_text(content, filename)
    except ValueError:
        raise HTTPException(status_code=400, detail="Unreadable file")

    if llm_parser.is_enabled():
        try:
            return llm_parser.parse_with_gemini(lines)
        except Exception as e:
            log.warning("Gemini failed, using regex parser: %s", e)
    return parse_invoice(lines)
