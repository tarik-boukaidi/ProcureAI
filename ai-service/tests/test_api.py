from fastapi.testclient import TestClient

from app import ocr
from app.main import app

client = TestClient(app)


def upload(filename="facture.png", content=b"image", role="COMPTABLE"):
    return client.post("/api/ai/ocr/invoice",
                       files={"file": (filename, content)},
                       headers={"X-User-Role": role})


def test_comptable_gets_invoice_draft(monkeypatch):
    # On remplace PaddleOCR par un faux resultat : test rapide, sans modele
    monkeypatch.setattr(ocr, "read_text", lambda content, filename: ["Facture N° : FAC-1", "Total TTC : 100,00"])

    response = upload()

    assert response.status_code == 200
    assert response.json()["invoiceNumber"] == "FAC-1"
    assert response.json()["totalTTC"] == 100.00


def test_only_comptable_can_use_ocr():
    assert upload(role="EMPLOYEE").status_code == 403
    assert upload(role="ADMIN").status_code == 403


def test_wrong_file_type_returns_400():
    assert upload(filename="facture.docx").status_code == 400


def test_unreadable_image_returns_400():
    assert upload(content=b"not-an-image").status_code == 400
