#!/usr/bin/env python3
"""
One-time content downloader.

Scans DatabaseSeeder.kt for every https://ik.imagekit.io/mhmdrzsaemi/... URL and downloads
each file into the local `content/` directory at the SAME relative path (query string dropped,
percent-encoding decoded) so the Ktor static server can serve it.

After running this, DatabaseSeeder uses `content("<relative path>")` URLs pointing at our own
server instead of imagekit.

Usage (from the server/ directory):
    python3 scripts/fetch_content.py
"""

import os
import re
import sys
import time
import urllib.parse
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
SERVER_DIR = os.path.dirname(HERE)
SEEDER = os.path.join(
    SERVER_DIR, "src", "main", "kotlin", "mohaamadreza", "saemipour", "no", "vazheh",
    "database", "DatabaseSeeder.kt",
)
CONTENT_DIR = os.path.join(SERVER_DIR, "content")
BASE = "https://ik.imagekit.io/mhmdrzsaemi/"

URL_RE = re.compile(r'"(https?://ik\.imagekit\.io/mhmdrzsaemi/[^"]*)"')


def relative_path_for(url: str) -> str:
    """The local path (relative to content/) a URL maps to: path after the base,
    without the query string, percent-decoded so it matches what Ktor looks up."""
    after = url.split(BASE, 1)[1]
    after = after.split("?", 1)[0]          # drop ?updatedAt=...
    return urllib.parse.unquote(after)      # %D8%B3.. -> سـ.., %20 -> space


def download(url: str, dest: str, retries: int = 3) -> None:
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    # imagekit needs the original (still-encoded) URL; requote safely.
    safe_url = urllib.parse.quote(url, safe=":/?&=%")
    last_err = None
    for attempt in range(1, retries + 1):
        try:
            req = urllib.request.Request(safe_url, headers={"User-Agent": "novazheh-fetch/1.0"})
            with urllib.request.urlopen(req, timeout=30) as resp:
                data = resp.read()
            if not data:
                raise IOError("empty response")
            with open(dest, "wb") as f:
                f.write(data)
            return
        except Exception as e:  # noqa: BLE001
            last_err = e
            time.sleep(attempt)
    raise RuntimeError(f"failed after {retries} tries: {last_err}")


def main() -> int:
    with open(SEEDER, encoding="utf-8") as f:
        text = f.read()

    urls = sorted(set(URL_RE.findall(text)))
    if not urls:
        print("No imagekit URLs found in the seeder — nothing to do.")
        return 0

    print(f"Found {len(urls)} unique URLs. Downloading into {CONTENT_DIR}\n")
    ok = skipped = failed = 0
    failures = []

    for url in urls:
        rel = relative_path_for(url)
        dest = os.path.join(CONTENT_DIR, rel)
        if os.path.isfile(dest) and os.path.getsize(dest) > 0:
            skipped += 1
            continue
        try:
            download(url, dest)
            ok += 1
            print(f"  ✓ {rel}")
        except Exception as e:  # noqa: BLE001
            failed += 1
            failures.append((rel, str(e)))
            print(f"  ✗ {rel}  ({e})")

    print(f"\nDone. downloaded={ok} skipped(existing)={skipped} failed={failed}")
    if failures:
        print("\nFailures:")
        for rel, err in failures:
            print(f"  - {rel}: {err}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
