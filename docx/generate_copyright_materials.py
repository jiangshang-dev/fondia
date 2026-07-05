#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
软著鉴别材料生成器
- 程序鉴别材料：源程序前连续 30 页 + 后连续 30 页（PDF，每页 50 行）
- 文档鉴别材料：设计文档前连续 30 页 + 后连续 30 页（PDF）
"""

from __future__ import annotations

import os
import textwrap
from pathlib import Path

from docx import Document
from docx.oxml.ns import qn
from fpdf import FPDF

# ========== 配置（与软著申请表保持一致）==========
SOFTWARE_NAME = "Fondia食养智能菜谱平台"
VERSION = "V1.0"
LINES_PER_PAGE = 50
PAGE_COUNT = 30

OUTPUT_DIR = Path(__file__).resolve().parent
DOCX_FILE = OUTPUT_DIR / "Fondia_软件需求分析说明书.docx"

FLUTTER_ROOT = Path("/Users/xiaobai/Deveploer/workspace/itheima/fondia_app/lib")
JAVA_ROOTS = [
    Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot/jeecg-module-user"),
    Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot/jeecg-module-cuisine"),
    Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot/jeecg-module-interaction"),
    Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot/jeecg-module-search"),
    Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot/jeecg-module-support"),
    Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot/jeecg-module-member"),
    Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot/jeecg-module-payment"),
]

SKIP_DIRS = {
    ".git", "node_modules", "build", ".dart_tool", "target", "dist",
    "generated", ".idea", ".gradle",
}

# macOS 中文字体
FONT_PATH = "/System/Library/Fonts/Supplemental/Arial Unicode.ttf"
FONT_BOLD_PATH = "/System/Library/Fonts/Supplemental/Arial Unicode.ttf"


class CopyrightPDF(FPDF):
    def __init__(self, header_title: str):
        super().__init__(orientation="P", unit="mm", format="A4")
        self.header_title = header_title
        self._setup_font()

    def _setup_font(self) -> None:
        if os.path.exists(FONT_PATH):
            self.add_font("cn", "", FONT_PATH)
            self.add_font("cn", "B", FONT_BOLD_PATH)
            self.font_name = "cn"
        else:
            self.font_name = "Helvetica"

    def content_width(self) -> float:
        return self.w - self.l_margin - self.r_margin

    def header(self) -> None:
        self.set_font(self.font_name, "B", 9)
        self.cell(0, 8, f"{self.header_title}  {VERSION}", align="C", new_x="LMARGIN", new_y="NEXT")

    def footer(self) -> None:
        self.set_y(-12)
        self.set_font(self.font_name, "", 9)
        self.cell(0, 8, f"第 {self.page_no()} 页", align="C")


def collect_source_lines() -> list[str]:
    """按 Flutter → Java 顺序收集源码行。"""
    files: list[Path] = []

    def walk(root: Path, exts: tuple[str, ...]) -> None:
        if not root.exists():
            return
        for dp, dirnames, filenames in os.walk(root):
            dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
            for fn in sorted(filenames):
                if fn.endswith(exts):
                    files.append(Path(dp) / fn)

    walk(FLUTTER_ROOT, (".dart",))
    for jr in JAVA_ROOTS:
        walk(jr, (".java",))

    files.sort(key=lambda p: str(p))
    # 入口文件优先
    priority = ["main.dart", "index.dart", "Application.java"]
    files.sort(key=lambda p: (0 if p.name in priority else 1, str(p)))

    lines: list[str] = []
    for fp in files:
        rel = fp
        try:
            rel = fp.relative_to(FLUTTER_ROOT)
        except ValueError:
            pass
        lines.append(f"// ========== {rel} ==========")
        with open(fp, "r", encoding="utf-8", errors="ignore") as f:
            lines.extend(f.read().splitlines())
        lines.append("")
    return lines


def paginate_lines(lines: list[str], lines_per_page: int) -> list[list[str]]:
    pages: list[list[str]] = []
    for i in range(0, len(lines), lines_per_page):
        chunk = lines[i : i + lines_per_page]
        while len(chunk) < lines_per_page:
            chunk.append("")
        pages.append(chunk)
    return pages


def select_front_back_pages(pages: list[list[str]], count: int) -> list[list[str]]:
    if len(pages) <= count * 2:
        return pages
    return pages[:count] + pages[-count:]


def render_code_pdf(pages: list[list[str]], output: Path, title: str) -> None:
    pdf = CopyrightPDF(title)
    pdf.set_auto_page_break(auto=True, margin=15)
    pdf.set_margins(15, 18, 15)

    for page_lines in pages:
        pdf.add_page()
        pdf.set_font(pdf.font_name, "", 8)
        w = pdf.content_width()
        for line in page_lines:
            display = line.replace("\t", "    ")
            if len(display) > 100:
                display = display[:100]
            try:
                pdf.multi_cell(w, 4.2, display)
            except Exception:
                pdf.multi_cell(w, 4.2, display.encode("ascii", "replace").decode())
    pdf.output(str(output))


def extract_docx_pages(docx_path: Path, lines_per_page: int = 42) -> list[list[str]]:
    """从 Word 文档提取分页内容（按分页符 + 行数估算）。"""
    doc = Document(str(docx_path))
    raw_pages: list[list[str]] = [[]]

    def append_line(text: str) -> None:
        if not text:
            return
        wrapped = textwrap.wrap(text, width=46) or [text]
        for w in wrapped:
            raw_pages[-1].append(w)
            if len(raw_pages[-1]) >= lines_per_page:
                raw_pages.append([])

    for para in doc.paragraphs:
        for br in para._element.findall(".//" + qn("w:br")):
            if br.get(qn("w:type")) == "page":
                if raw_pages[-1]:
                    raw_pages.append([])
        style = para.style.name if para.style else ""
        text = para.text.strip()
        if not text:
            continue
        if style.startswith("Heading"):
            if raw_pages[-1]:
                raw_pages.append([])
            append_line(text)
            continue
        append_line(text)

    for table in doc.tables:
        if raw_pages[-1]:
            raw_pages.append([])
        for row in table.rows:
            row_text = " | ".join(c.text.strip() for c in row.cells if c.text.strip())
            if row_text:
                append_line(row_text)

    # 规范化每页行数
    pages: list[list[str]] = []
    buf: list[str] = []
    for pg in raw_pages:
        for ln in pg:
            buf.append(ln)
            if len(buf) >= lines_per_page:
                chunk = buf[:lines_per_page]
                while len(chunk) < lines_per_page:
                    chunk.append("")
                pages.append(chunk)
                buf = buf[lines_per_page:]
    if buf:
        while len(buf) < lines_per_page:
            buf.append("")
        pages.append(buf)
    return pages


def render_doc_pdf(pages: list[list[str]], output: Path, title: str) -> None:
    pdf = CopyrightPDF(title)
    pdf.set_auto_page_break(auto=True, margin=15)
    pdf.set_margins(20, 18, 20)

    for page_lines in pages:
        pdf.add_page()
        pdf.set_font(pdf.font_name, "", 10)
        w = pdf.content_width()
        for line in page_lines:
            pdf.multi_cell(w, 5.5, line)
    pdf.output(str(output))


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    print("1/2 生成程序鉴别材料…")
    src_lines = collect_source_lines()
    all_code_pages = paginate_lines(src_lines, LINES_PER_PAGE)
    code_selected = select_front_back_pages(all_code_pages, PAGE_COUNT)
    code_out = OUTPUT_DIR / f"软著_程序鉴别材料_{SOFTWARE_NAME}_前{PAGE_COUNT}页后{PAGE_COUNT}页.pdf"
    render_code_pdf(code_selected, code_out, SOFTWARE_NAME)
    print(f"   源码总行数: {len(src_lines)}, 总页数: {len(all_code_pages)}")
    print(f"   输出: {code_out} ({len(code_selected)} 页)")

    print("2/2 生成文档鉴别材料…")
    if not DOCX_FILE.exists():
        raise FileNotFoundError(f"未找到 {DOCX_FILE}，请先运行 generate_fondia_docs.py")
    doc_pages = extract_docx_pages(DOCX_FILE)
    doc_selected = select_front_back_pages(doc_pages, PAGE_COUNT)
    doc_out = OUTPUT_DIR / f"软著_文档鉴别材料_需求分析说明书_前{PAGE_COUNT}页后{PAGE_COUNT}页.pdf"
    render_doc_pdf(doc_selected, doc_out, f"{SOFTWARE_NAME} 需求分析说明书")
    print(f"   文档总页数: {len(doc_pages)}")
    print(f"   输出: {doc_out} ({len(doc_selected)} 页)")

    print("\n完成！请在中国版权登记平台上传：")
    print(f"  程序鉴别材料 → {code_out.name}")
    print(f"  文档鉴别材料 → {doc_out.name}")


if __name__ == "__main__":
    main()
