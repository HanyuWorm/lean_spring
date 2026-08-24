#!/usr/bin/env python3
"""Create a deterministic MkDocs source tree from tracked repository files."""

from __future__ import annotations

import re
import shutil
import subprocess
import posixpath
from collections import defaultdict
from pathlib import Path, PurePosixPath
from urllib.parse import quote


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / ".site-docs"
REPOSITORY_URL = "https://github.com/HanyuWorm/lean_spring"
MAX_EMBED_BYTES = 200_000

CODE_SUFFIXES = {
    ".java": "java",
    ".js": "javascript",
    ".mjs": "javascript",
    ".cjs": "javascript",
    ".ts": "typescript",
    ".tsx": "tsx",
    ".sql": "sql",
    ".yml": "yaml",
    ".yaml": "yaml",
    ".xml": "xml",
    ".json": "json",
    ".properties": "properties",
    ".toml": "toml",
    ".tf": "hcl",
    ".sh": "bash",
    ".ps1": "powershell",
}
ASSET_SUFFIXES = {".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp", ".pdf"}


def tracked_files() -> list[PurePosixPath]:
    raw = subprocess.check_output(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"], cwd=ROOT
    )
    return [PurePosixPath(item.decode("utf-8")) for item in raw.split(b"\0") if item]


def is_vendored_repository(path: PurePosixPath) -> bool:
    parts = path.parts
    return (
        len(parts) >= 3
        and parts[0] == "11-architecture-distributed-case-studies"
        and parts[1] == "repositories"
        and parts[2] != "README.md"
    )


def is_hidden_path(path: PurePosixPath) -> bool:
    return any(part.startswith(".") for part in path.parts)


def copy_file(path: PurePosixPath, destination: Path | None = None) -> None:
    source = ROOT.joinpath(*path.parts)
    target = destination or OUTPUT.joinpath(*path.parts)
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)


def github_url(path: PurePosixPath) -> str:
    encoded = "/".join(quote(part) for part in path.parts)
    kind = "tree" if ROOT.joinpath(*path.parts).is_dir() else "blob"
    return f"{REPOSITORY_URL}/{kind}/main/{encoded}"


def rewrite_vendored_links(content: str, source_path: PurePosixPath) -> str:
    pattern = re.compile(r"(?P<prefix>!?\[[^\]]*\]\()(?P<target>[^)\s]+)(?P<suffix>[^)]*\))")

    def replace(match: re.Match[str]) -> str:
        target = match.group("target")
        if target.startswith(("http://", "https://", "mailto:", "#")):
            return match.group(0)
        path_part, separator, fragment = target.partition("#")
        resolved = PurePosixPath(posixpath.normpath((source_path.parent / path_part).as_posix()))
        if is_vendored_repository(resolved):
            url = github_url(resolved)
            if separator:
                url = f"{url}#{fragment}"
            return f"{match.group('prefix')}{url}{match.group('suffix')}"
        readme = ROOT.joinpath(*resolved.parts, "README.md")
        if ROOT.joinpath(*resolved.parts).is_dir() and readme.is_file():
            target = f"{path_part.rstrip('/')}/README.md"
            if separator:
                target = f"{target}#{fragment}"
            return f"{match.group('prefix')}{target}{match.group('suffix')}"
        return match.group(0)

    return pattern.sub(replace, content)


def write_markdown(path: PurePosixPath, destination: Path | None = None) -> None:
    source = ROOT.joinpath(*path.parts)
    target = destination or OUTPUT.joinpath(*path.parts)
    target.parent.mkdir(parents=True, exist_ok=True)
    content = source.read_text(encoding="utf-8", errors="replace")
    target.write_text(rewrite_vendored_links(content, path), encoding="utf-8")


def safe_fence(content: str) -> str:
    longest = max((len(match.group(0)) for match in re.finditer(r"`+", content)), default=0)
    return "`" * max(4, longest + 1)


def generate_code_pages(paths: list[PurePosixPath]) -> tuple[int, int]:
    grouped: dict[str, list[tuple[PurePosixPath, str | None]]] = defaultdict(list)
    embedded = 0
    linked_only = 0

    for path in paths:
        if is_vendored_repository(path) or is_hidden_path(path) or path.suffix.lower() not in CODE_SUFFIXES:
            continue
        source = ROOT.joinpath(*path.parts)
        module = path.parts[0] if len(path.parts) > 1 else "Root"
        if source.stat().st_size > MAX_EMBED_BYTES:
            grouped[module].append((path, None))
            linked_only += 1
            continue

        content = source.read_text(encoding="utf-8", errors="replace")
        language = CODE_SUFFIXES[path.suffix.lower()]
        output = OUTPUT / "code" / Path(*path.parts).with_suffix(path.suffix + ".md")
        output.parent.mkdir(parents=True, exist_ok=True)
        fence = safe_fence(content)
        output.write_text(
            f"# `{path.name}`\n\n"
            f"**Đường dẫn:** `{path}` · [Mở source trên GitHub]({github_url(path)})\n\n"
            f"{fence}{language}\n{content.rstrip()}\n{fence}\n",
            encoding="utf-8",
        )
        page = PurePosixPath("code", *path.parts).with_suffix(path.suffix + ".md")
        grouped[module].append((path, page.as_posix()))
        embedded += 1

    lines = [
        "# Trình duyệt source code",
        "",
        "Các file do learning workspace sở hữu được render với syntax highlighting và nút copy. "
        "Source của những repository bên thứ ba trong case study không bị nhân đôi vào search index; "
        "hãy mở chúng trực tiếp trên GitHub.",
        "",
        f"- **{embedded}** file được nhúng trực tiếp.",
        f"- **{linked_only}** file lớn chỉ liên kết tới GitHub.",
        "",
    ]
    for module in sorted(grouped, key=str.casefold):
        lines.extend([f"## {module}", ""])
        for path, page in sorted(grouped[module], key=lambda item: item[0].as_posix().casefold()):
            link = f"../{page}" if page else github_url(path)
            lines.append(f"- [`{path}`]({link})")
        lines.append("")

    index = OUTPUT / "code" / "index.md"
    index.parent.mkdir(parents=True, exist_ok=True)
    index.write_text("\n".join(lines), encoding="utf-8")
    return embedded, linked_only


def main() -> None:
    if OUTPUT.exists():
        shutil.rmtree(OUTPUT)
    OUTPUT.mkdir(parents=True)

    tracked = tracked_files()
    markdown_count = 0
    asset_count = 0
    source_asset_count = 0

    for path in tracked:
        suffix = path.suffix.lower()
        if suffix == ".md" and not is_vendored_repository(path):
            destination = OUTPUT / "index.md" if path == PurePosixPath("README.md") else None
            write_markdown(path, destination)
            markdown_count += 1
        elif suffix in CODE_SUFFIXES and not is_vendored_repository(path) and not is_hidden_path(path):
            copy_file(path)
            source_asset_count += 1
        elif suffix in ASSET_SUFFIXES and not is_vendored_repository(path):
            copy_file(path)
            asset_count += 1

    copy_file(PurePosixPath("website/stylesheets/extra.css"), OUTPUT / "assets/stylesheets/extra.css")
    embedded, linked_only = generate_code_pages(tracked)
    print(
        f"Generated {markdown_count} Markdown pages, {embedded} embedded code pages, "
        f"{linked_only} code links, {source_asset_count} source assets and "
        f"{asset_count} media assets in {OUTPUT}"
    )


if __name__ == "__main__":
    main()
