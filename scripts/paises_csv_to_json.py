#!/usr/bin/env python3
"""Convierte el dataset de paises de CSV a JSON para el seeder.

Fuente del dataset: https://github.com/datasets/country-list

Entrada  (por defecto): src/main/resources/data/paises.csv
Salida   (por defecto): src/main/resources/data/paises.json

El CSV tiene dos columnas:
    - "Name" -> se mapea a "nombre"
    - "code" -> se mapea a "code" (ISO 3166-1 alpha-2)

El JSON de salida es un array de objetos {"nombre": ..., "code": ...},
que es exactamente la forma que consume el record PaisSeed en el backend.

Uso:
    python scripts/paises_csv_to_json.py
    python scripts/paises_csv_to_json.py --input ruta/paises.csv --output ruta/paises.json
"""

import argparse
import csv
import json
import sys
from pathlib import Path

# El script vive en <repo>/scripts/, el dataset en <repo>/src/main/resources/data/
REPO_ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = REPO_ROOT / "src" / "main" / "resources" / "data"
DEFAULT_INPUT = DATA_DIR / "paises.csv"
DEFAULT_OUTPUT = DATA_DIR / "paises.json"


def find_column(fieldnames, wanted):
    """Busca un encabezado de columna sin distinguir mayusculas/espacios."""
    normalized = {name.strip().lower(): name for name in fieldnames}
    return normalized.get(wanted.strip().lower())


def convert(input_path: Path, output_path: Path) -> int:
    if not input_path.exists():
        sys.exit(f"ERROR: no se encontro el CSV de entrada: {input_path}")

    with input_path.open(newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)

        if reader.fieldnames is None:
            sys.exit(f"ERROR: el CSV esta vacio o no tiene encabezados: {input_path}")

        name_col = find_column(reader.fieldnames, "Name")
        code_col = find_column(reader.fieldnames, "code")
        if name_col is None or code_col is None:
            sys.exit(
                "ERROR: el CSV debe tener columnas 'Name' y 'code'. "
                f"Encontradas: {reader.fieldnames}"
            )

        paises = []
        seen_codes = set()
        for line_no, row in enumerate(reader, start=2):  # 1 = encabezado
            nombre = (row.get(name_col) or "").strip()
            code = (row.get(code_col) or "").strip().upper()

            if not nombre or not code:
                print(f"  aviso: fila {line_no} incompleta, se omite: {row}", file=sys.stderr)
                continue

            if code in seen_codes:
                print(f"  aviso: code duplicado '{code}' en fila {line_no}, se omite", file=sys.stderr)
                continue

            seen_codes.add(code)
            paises.append({"nombre": nombre, "code": code})

    # Orden estable por nombre -> diffs limpios cuando se regenera el archivo
    paises.sort(key=lambda p: p["nombre"])

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8") as f:
        json.dump(paises, f, ensure_ascii=False, indent=2)
        f.write("\n")

    return len(paises)


def main():
    parser = argparse.ArgumentParser(description="Convierte paises.csv a paises.json")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="Ruta del CSV de entrada")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT, help="Ruta del JSON de salida")
    args = parser.parse_args()

    total = convert(args.input, args.output)
    print(f"OK: {total} paises escritos en {args.output}")


if __name__ == "__main__":
    main()
