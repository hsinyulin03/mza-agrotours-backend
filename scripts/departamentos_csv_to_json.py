#!/usr/bin/env python3
"""Convierte el dataset de departamentos de CSV a JSON para el seeder.

Fuente del dataset: https://datosabiertos.mendoza.gov.ar/dataset/cabeceras-departamentales-de-mendoza

Entrada  (por defecto): src/main/resources/data/cabeceras-departamentales-georreferenciadas.csv
Salida   (por defecto): src/main/resources/data/cabeceras-departamentales-georreferenciadas.json

El CSV tiene tres columnas:
    - "FID" -> no mapeada
    - "the_geom" -> coordenadas en formato POINT(lon,lat) mapeadas para obtener lon y lat
    - "nombre" -> se mapea a "nombre" del departamento

El JSON de salida es un array de objetos {"nombre": ..., "lon": ..., "lat": ...},
que es exactamente la forma que consume el record DepartamentoSeed en el backend.

Uso:
    python scripts/departamentos_csv_to_json.py
    python scripts/departamentos_csv_to_json.py --input ruta/departamentos.csv --output ruta/departamentos.json
"""

import argparse
import csv
import json
import sys
from pathlib import Path

# El script vive en <repo>/scripts/, el dataset en <repo>/src/main/resources/data/
REPO_ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = REPO_ROOT / "src" / "main" / "resources" / "data"
DEFAULT_INPUT = DATA_DIR / "cabeceras-departamentales-georreferenciadas.csv"
DEFAULT_OUTPUT = DATA_DIR / "cabeceras-departamentales-georreferenciadas.json"


def find_column(fieldnames, wanted):
    """Busca un encabezado de columna sin distinguir mayusculas/espacios."""
    normalized = {name.strip().lower(): name for name in fieldnames}
    return normalized.get(wanted.strip().lower())

def get_lat_lon_from_geom(geom_str):
    return geom_str[7:-1].split(" ")

def convert(input_path: Path, output_path: Path) -> int:
    if not input_path.exists():
        sys.exit(f"ERROR: no se encontro el CSV de entrada: {input_path}")

    with input_path.open(newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)

        if reader.fieldnames is None:
            sys.exit(f"ERROR: el CSV esta vacio o no tiene encabezados: {input_path}")

        cords_col = find_column(reader.fieldnames, "the_geom")
        nombre_col = find_column(reader.fieldnames, "NOMBRE")
        if cords_col is None or nombre_col is None:
            sys.exit(
                "ERROR: el CSV debe tener columnas 'the_geom' y 'NOMBRE'. "
                f"Encontradas: {reader.fieldnames}"
            )

        departamentos = []
        seen_nombres = set()
        for line_no, row in enumerate(reader, start=2):  # 1 = encabezado
            cords = (row.get(cords_col) or "").strip()
            nombre = (row.get(nombre_col) or "").strip()

            if not cords or not nombre:
                print(f"  aviso: fila {line_no} incompleta, se omite: {row}", file=sys.stderr)
                continue

            if nombre in seen_nombres:
                print(f"  aviso: nombre duplicado '{nombre}' en fila {line_no}, se omite", file=sys.stderr)
                continue

            seen_nombres.add(nombre)
            [lon, lat] = get_lat_lon_from_geom(cords)
            departamentos.append({"nombre": nombre, "lon": lon, "lat": lat})

    # Orden estable por nombre -> diffs limpios cuando se regenera el archivo
    departamentos.sort(key=lambda p: p["nombre"])

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8") as f:
        json.dump(departamentos, f, ensure_ascii=False, indent=2)
        f.write("\n")

    return len(departamentos)


def main():
    parser = argparse.ArgumentParser(description="Convierte cabeceras-departamentales-georreferenciadas.csv a cabeceras-departamentales-georreferenciadas.json")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="Ruta del CSV de entrada")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT, help="Ruta del JSON de salida")
    args = parser.parse_args()

    total = convert(args.input, args.output)
    print(f"OK: {total} departamentos escritos en {args.output}")


if __name__ == "__main__":
    main()
