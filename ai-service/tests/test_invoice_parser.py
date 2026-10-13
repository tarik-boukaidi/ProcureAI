from app.invoice_parser import parse_invoice


def test_values_on_same_line():
    lines = [
        "ICE : 001234567000089",
        "Facture N° : FAC-2026-001",
        "Date : 01/10/2026",
        "Total HT : 859,58",
        "TVA 20% : 171,92",
        "Total TTC : 1 031,50 MAD",
    ]

    assert parse_invoice(lines) == {
        "invoiceNumber": "FAC-2026-001",
        "invoiceDate": "2026-10-01",
        "supplierIce": "001234567000089",
        "totalHT": 859.58,
        "tva": 171.92,
        "totalTTC": 1031.50,
        "lines": [],
    }


def test_values_on_next_line():
    # PaddleOCR lit souvent le libelle et la valeur dans deux cases separees
    lines = ["Facture N°", "FAC-77", "Total TTC", "2 400,00"]

    result = parse_invoice(lines)

    assert result["invoiceNumber"] == "FAC-77"
    assert result["totalTTC"] == 2400.00


def test_missing_values_are_none():
    result = parse_invoice(["Merci pour votre commande"])

    assert result["invoiceNumber"] is None
    assert result["totalTTC"] is None
