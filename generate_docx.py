import zipfile
import html
import os

content_types_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""

rels_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

styles_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
        <w:color w:val="1A1A1A"/>
      </w:rPr>
    </w:rPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
    <w:pPr>
      <w:spacing w:after="160" w:line="260" w:lineRule="auto"/>
    </w:pPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Title">
    <w:name w:val="Title"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="240" w:after="240"/>
      <w:jc w:val="center"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Segoe UI" w:hAnsi="Segoe UI"/>
      <w:b/>
      <w:sz w:val="52"/>
      <w:color w:val="0052CC"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Subtitle">
    <w:name w:val="Subtitle"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="0" w:after="360"/>
      <w:jc w:val="center"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Segoe UI" w:hAnsi="Segoe UI"/>
      <w:i/>
      <w:sz w:val="26"/>
      <w:color w:val="5E6C84"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="Heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="360" w:after="160"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Segoe UI Semibold" w:hAnsi="Segoe UI Semibold"/>
      <w:b/>
      <w:sz w:val="36"/>
      <w:color w:val="0052CC"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="Heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="240" w:after="120"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Segoe UI Semibold" w:hAnsi="Segoe UI Semibold"/>
      <w:b/>
      <w:sz w:val="28"/>
      <w:color w:val="172B4D"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading3">
    <w:name w:val="Heading 3"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="180" w:after="80"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Segoe UI Semibold" w:hAnsi="Segoe UI Semibold"/>
      <w:b/>
      <w:sz w:val="24"/>
      <w:color w:val="008DA6"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="CodeBlock">
    <w:name w:val="Code Block"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="40" w:after="40" w:line="220" w:lineRule="auto"/>
      <w:shd w:val="clear" w:color="auto" w:fill="F4F5F7"/>
      <w:ind w:left="360" w:right="360"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/>
      <w:sz w:val="18"/>
      <w:color w:val="091E42"/>
    </w:rPr>
  </w:style>
</w:styles>"""

def escape(text):
    return html.escape(text).encode('ascii', 'xmlcharrefreplace').decode('ascii')

class DocxBuilder:
    def __init__(self):
        self.paragraphs = []

    def add_title(self, text):
        self.paragraphs.append(f"""<w:p><w:pPr><w:pStyle w:val="Title"/></w:pPr><w:r><w:t>{escape(text)}</w:t></w:r></w:p>""")

    def add_subtitle(self, text):
        self.paragraphs.append(f"""<w:p><w:pPr><w:pStyle w:val="Subtitle"/></w:pPr><w:r><w:t>{escape(text)}</w:t></w:r></w:p>""")

    def add_h1(self, text):
        self.paragraphs.append(f"""<w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t>{escape(text)}</w:t></w:r></w:p>""")

    def add_h2(self, text):
        self.paragraphs.append(f"""<w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t>{escape(text)}</w:t></w:r></w:p>""")

    def add_h3(self, text):
        self.paragraphs.append(f"""<w:p><w:pPr><w:pStyle w:val="Heading3"/></w:pPr><w:r><w:t>{escape(text)}</w:t></w:r></w:p>""")

    def add_p(self, text, bold=False, italic=False):
        b_tag = "<w:b/>" if bold else ""
        i_tag = "<w:i/>" if italic else ""
        self.paragraphs.append(f"""<w:p><w:r><w:rPr>{b_tag}{i_tag}</w:rPr><w:t xml:space="preserve">{escape(text)}</w:t></w:r></w:p>""")

    def add_bullet(self, title, desc=""):
        desc_xml = f"""<w:r><w:t xml:space="preserve">: {escape(desc)}</w:t></w:r>""" if desc else ""
        self.paragraphs.append(f"""<w:p><w:pPr><w:ind w:left="480"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="0052CC"/></w:rPr><w:t xml:space="preserve">&#x25CF; {escape(title)}</w:t></w:r>{desc_xml}</w:p>""")

    def add_code_block(self, code_str, filename=""):
        if filename:
            self.paragraphs.append(f"""<w:p><w:pPr><w:spacing w:before="240" w:after="60"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="008DA6"/><w:sz w:val="20"/></w:rPr><w:t xml:space="preserve">&#x1F4C4; File: {escape(filename)}</w:t></w:r></w:p>""")
        
        for line in code_str.split("\n"):
            disp_line = line if line else " "
            self.paragraphs.append(f"""<w:p><w:pPr><w:pStyle w:val="CodeBlock"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/><w:sz w:val="18"/><w:color w:val="091E42"/></w:rPr><w:t xml:space="preserve">{escape(disp_line)}</w:t></w:r></w:p>""")

    def build_xml(self):
        body = "".join(self.paragraphs)
        return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    {body}
    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"/>
    </w:sectPr>
  </w:body>
</w:document>"""

def read_file(path):
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            return f.read()
    return ""

def main():
    doc = DocxBuilder()

    doc.add_title("AIopter Assistant")
    doc.add_subtitle("Complete Technical Specifications & Full Android Codebase Scripts")

    doc.add_h1("1. AIopter Overview & System Architecture")
    doc.add_p("AIopter is an ambient, user-controlled AI companion for Android designed with absolute transparency, strict security boundaries, and an iconic rotating propeller interface.")
    
    doc.add_h2("Core Architecture Principles")
    doc.add_bullet("The Propeller Logo IS the Floating Object", "No generic circular bubble or opaque chat button. The official blue/cyan aerodynamic 3-blade rotor floats directly on top of apps with a transparent background and ambient drop shadow/glow.")
    doc.add_bullet("Automatic 4-Corner Magnetic Self-Docking", "When dragged and released anywhere on screen, Euclidean distance squared calculates the nearest corner (Top-Left, Top-Right, Bottom-Left, Bottom-Right) and animates smoothly using a 280ms DecelerateInterpolator.")
    doc.add_bullet("Touch vs. Drag Discrimination", "Distinguishes between a quick stationary tap (<400ms, within touch-slop) to expand the assistant panel, versus a dragging motion across the screen.")
    doc.add_bullet("Rotational Dynamics & Spin-Down", "Rotates smoothly clockwise at 1.35s per full 360-degree turn during voice processing, screen analysis, or response streaming. Decelerates smoothly back to the 0-degree resting position when idle.")
    doc.add_bullet("Safe Action Paradigm (Suggest -> Explain -> Approve -> Execute)", "No external action or mutation executes without clear risk classification and explicit user approval.")
    doc.add_bullet("Hardware Emergency STOP / KILL Switch", "Instantly halts screen projection, microphone feeds, in-flight AI queries, clears buffers, and removes the overlay.")

    doc.add_h1("2. Floating Propeller & Overlay Implementation Scripts")
    doc.add_p("Below are the complete source scripts powering the floating propeller overlay window, magnetic corner docking, and rotational animation.")

    overlay_service_code = read_file("app/src/main/java/com/example/service/AIopterOverlayService.kt")
    doc.add_code_block(overlay_service_code, "app/src/main/java/com/example/service/AIopterOverlayService.kt")

    rotor_view_code = read_file("app/src/main/java/com/example/ui/components/AIopterRotorView.kt")
    doc.add_code_block(rotor_view_code, "app/src/main/java/com/example/ui/components/AIopterRotorView.kt")

    bubble_content_code = read_file("app/src/main/java/com/example/ui/overlay/OverlayBubbleContent.kt")
    doc.add_code_block(bubble_content_code, "app/src/main/java/com/example/ui/overlay/OverlayBubbleContent.kt")

    doc.add_h1("3. Session State Machine & Preferences")
    doc.add_p("Strict state machine preventing 'looks on but is actually off' synchronization bugs.")

    session_state_code = read_file("app/src/main/java/com/example/model/SessionState.kt")
    doc.add_code_block(session_state_code, "app/src/main/java/com/example/model/SessionState.kt")

    prefs_code = read_file("app/src/main/java/com/example/data/preferences/AIopterPreferences.kt")
    doc.add_code_block(prefs_code, "app/src/main/java/com/example/data/preferences/AIopterPreferences.kt")

    doc.add_h1("4. Safe Action Engine & Local Room Database")
    doc.add_p("Local persistence and action execution engine for secure permissions and audit trails.")

    action_models_code = read_file("app/src/main/java/com/example/model/ActionModels.kt")
    doc.add_code_block(action_models_code, "app/src/main/java/com/example/model/ActionModels.kt")

    action_executor_code = read_file("app/src/main/java/com/example/actions/ActionExecutor.kt")
    doc.add_code_block(action_executor_code, "app/src/main/java/com/example/actions/ActionExecutor.kt")

    db_entities_code = read_file("app/src/main/java/com/example/data/db/Entities.kt")
    doc.add_code_block(db_entities_code, "app/src/main/java/com/example/data/db/Entities.kt")

    db_main_code = read_file("app/src/main/java/com/example/data/db/AIopterDatabase.kt")
    doc.add_code_block(db_main_code, "app/src/main/java/com/example/data/db/AIopterDatabase.kt")

    doc.add_h1("5. Android Manifest & Main Navigation Entry Point")
    doc.add_p("Application declaration and Jetpack Compose 7-destination navigation host.")

    manifest_code = read_file("app/src/main/AndroidManifest.xml")
    doc.add_code_block(manifest_code, "app/src/main/AndroidManifest.xml")

    main_activity_code = read_file("app/src/main/java/com/example/MainActivity.kt")
    doc.add_code_block(main_activity_code, "app/src/main/java/com/example/MainActivity.kt")

    doc_xml = doc.build_xml()

    docx_path = "AIopter_Coding_Specifications.docx"
    with zipfile.ZipFile(docx_path, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", content_types_xml)
        zf.writestr("_rels/.rels", rels_xml)
        zf.writestr("word/styles.xml", styles_xml)
        zf.writestr("word/document.xml", doc_xml)

    print(f"Successfully generated {docx_path} ({os.path.getsize(docx_path)} bytes)")

if __name__ == "__main__":
    main()
