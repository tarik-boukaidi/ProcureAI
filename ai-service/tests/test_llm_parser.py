from types import SimpleNamespace

from fastapi.testclient import TestClient

from app import llm_parser, ocr
from app.llm_parser import InvoiceDraft, InvoiceLine
from app.main import app

client = TestClient(app)

OCR_LINES = ["N° de facture : FA-2026-014", "Montant HT : 7 200,00"]


def fake_gemini(monkeypatch, draft=None, error=None):
    """Remplace le client Gemini : renvoie draft, ou leve error."""
    def generate_content(model, contents, config):
        if error:
            raise error
        return SimpleNamespace(parsed=draft)

    fake_client = SimpleNamespace(models=SimpleNamespace(generate_content=generate_content))
    monkeypatch.setenv("GEMINI_API_KEY", "test-key")
    monkeypatch.setattr(llm_parser, "get_client", lambda: fake_client)
    monkeypatch.setattr(ocr, "read_text", lambda content, filename: OCR_LINES)


def upload():
    return client.post("/api/ai/ocr/invoice",
                       files={"file": ("facture.png", b"image")},
                       headers={"X-User-Role": "COMPTABLE"})


def draft(**changes):
    values = dict(invoiceNumber="FA-2026-014", invoiceDate="2026-10-01", supplierIce="001525874000062",
                  totalHT=7200.0, tva=1440.0, totalTTC=8640.0,
                  lines=[InvoiceLine(productName="Ramette papier A4", quantity=100, unitPrice=72.0)])
    values.update(changes)
    return InvoiceDraft(**values)


def test_gemini_returns_structured_json_with_lines(monkeypatch):
    fake_gemini(monkeypatch, draft=draft())

    response = upload()

    assert response.status_code == 200
    assert response.json() == {
        "invoiceNumber": "FA-2026-014",
        "invoiceDate": "2026-10-01",
        "supplierIce": "001525874000062",
        "totalHT": 7200.0,
        "tva": 1440.0,
        "totalTTC": 8640.0,
        "lines": [{"productName": "Ramette papier A4", "quantity": 100, "unitPrice": 72.0}],
    }


def test_badly_formatted_values_from_gemini_become_null(monkeypatch):
    fake_gemini(monkeypatch, draft=draft(supplierIce="12345", invoiceDate="01/10/2026"))

    body = upload().json()

    assert body["supplierIce"] is None
    assert body["invoiceDate"] is None
    assert body["invoiceNumber"] == "FA-2026-014"


def test_regex_parser_is_used_when_gemini_fails(monkeypatch):
    fake_gemini(monkeypatch, error=RuntimeError("quota exceeded"))
    monkeypatch.setattr(ocr, "read_text", lambda content, filename: ["Facture N° : FAC-1", "Total TTC : 100,00"])

    response = upload()

    assert response.status_code == 200
    assert response.json()["invoiceNumber"] == "FAC-1"
    assert response.json()["lines"] == []
