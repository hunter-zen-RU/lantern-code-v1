package com.example.server

object WebCodeStudioHtml {
  fun getHtml(projectName: String, branch: String, pairingCode: String, hostIp: String, port: Int): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$projectName - LANtern Web Code Studio</title>
  <style>
    :root {
      --bg-dark: #0b0f17;
      --card-bg: #131b2a;
      --border: #1e293b;
      --neon-green: #00E676;
      --cyan: #38BDF8;
      --text-primary: #f8fafc;
      --text-muted: #94a3b8;
      --accent: #22c55e;
      --danger: #ef4444;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "JetBrains Mono", monospace;
      background-color: var(--bg-dark);
      color: var(--text-primary);
      display: flex;
      flex-direction: column;
      height: 100vh;
      overflow: hidden;
    }
    /* Header */
    header {
      background: var(--card-bg);
      border-bottom: 1px solid var(--border);
      padding: 10px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 700;
      font-size: 16px;
      letter-spacing: 0.5px;
    }
    .brand-icon {
      color: var(--neon-green);
      font-size: 20px;
    }
    .project-pill {
      background: rgba(56, 189, 248, 0.15);
      color: var(--cyan);
      border: 1px solid rgba(56, 189, 248, 0.3);
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
    }
    .pairing-code-badge {
      background: rgba(0, 230, 118, 0.12);
      border: 1px solid rgba(0, 230, 118, 0.4);
      color: var(--neon-green);
      padding: 4px 12px;
      border-radius: 6px;
      font-family: monospace;
      font-weight: bold;
      font-size: 13px;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .header-actions {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .btn {
      background: var(--card-bg);
      color: var(--text-primary);
      border: 1px solid var(--border);
      padding: 6px 14px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s;
    }
    .btn:hover { background: #1e293b; }
    .btn-save {
      background: var(--neon-green);
      color: #000;
      border: none;
      font-weight: 600;
    }
    .btn-save:hover { background: #00c853; }
    /* Main Layout */
    .studio-container {
      display: flex;
      flex: 1;
      height: calc(100vh - 80px);
      overflow: hidden;
    }
    /* Sidebar */
    .sidebar {
      width: 250px;
      background: #0f172a;
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
    }
    .sidebar-header {
      padding: 12px 14px;
      border-bottom: 1px solid var(--border);
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--text-muted);
    }
    .file-tree {
      flex: 1;
      overflow-y: auto;
      padding: 8px 0;
    }
    .file-item {
      display: flex;
      align-items: center;
      padding: 6px 16px;
      cursor: pointer;
      font-size: 13px;
      gap: 8px;
      color: var(--text-muted);
      transition: background 0.15s;
    }
    .file-item:hover {
      background: rgba(255, 255, 255, 0.05);
      color: var(--text-primary);
    }
    .file-item.active {
      background: rgba(0, 230, 118, 0.15);
      color: var(--neon-green);
      font-weight: 600;
      border-left: 3px solid var(--neon-green);
    }
    /* Editor Zone */
    .editor-zone {
      flex: 1;
      display: flex;
      flex-direction: column;
      background: var(--bg-dark);
      overflow: hidden;
    }
    .tab-bar {
      display: flex;
      background: #090d14;
      border-bottom: 1px solid var(--border);
      overflow-x: auto;
    }
    .tab {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      border-right: 1px solid var(--border);
      cursor: pointer;
      font-size: 12px;
      color: var(--text-muted);
    }
    .tab.active {
      background: var(--card-bg);
      color: var(--cyan);
      border-bottom: 2px solid var(--cyan);
      font-weight: 600;
    }
    .editor-wrapper {
      display: flex;
      flex: 1;
      overflow: hidden;
      position: relative;
    }
    .gutter {
      width: 48px;
      background: #090d14;
      border-right: 1px solid var(--border);
      color: #64748b;
      font-family: "JetBrains Mono", monospace;
      font-size: 13px;
      line-height: 22px;
      padding: 12px 6px;
      text-align: right;
      user-select: none;
      overflow: hidden;
    }
    .gutter-line.highlight {
      color: var(--neon-green);
      font-weight: bold;
      background: rgba(0, 230, 118, 0.2);
    }
    .code-textarea {
      flex: 1;
      background: var(--bg-dark);
      color: #e2e8f0;
      font-family: "JetBrains Mono", Consolas, monospace;
      font-size: 13px;
      line-height: 22px;
      padding: 12px;
      border: none;
      outline: none;
      resize: none;
      white-space: pre;
      overflow: auto;
      tab-size: 2;
    }
    /* Chat & Reference Drawer */
    .chat-drawer {
      width: 320px;
      background: #0f172a;
      border-left: 1px solid var(--border);
      display: flex;
      flex-direction: column;
    }
    .chat-header {
      padding: 12px 14px;
      border-bottom: 1px solid var(--border);
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      font-weight: 600;
    }
    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .chat-bubble {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 10px;
      font-size: 13px;
    }
    .chat-meta {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;
      font-size: 11px;
      color: var(--text-muted);
    }
    .sender-name {
      font-weight: 600;
      color: var(--cyan);
    }
    .code-ref-card {
      margin-top: 8px;
      background: #090d14;
      border: 1px solid rgba(0, 230, 118, 0.4);
      border-left: 3px solid var(--neon-green);
      border-radius: 6px;
      padding: 8px 10px;
      cursor: pointer;
      transition: all 0.2s;
    }
    .code-ref-card:hover {
      background: #131d2e;
      border-color: var(--neon-green);
      transform: translateY(-1px);
    }
    .ref-header {
      display: flex;
      justify-content: space-between;
      font-size: 11px;
      color: var(--neon-green);
      font-family: monospace;
      font-weight: bold;
      margin-bottom: 4px;
    }
    .ref-snippet {
      font-family: "JetBrains Mono", monospace;
      font-size: 11px;
      color: #e2e8f0;
      background: #000;
      padding: 4px 6px;
      border-radius: 4px;
      white-space: pre-wrap;
      word-break: break-all;
    }
    .chat-input-bar {
      padding: 10px;
      border-top: 1px solid var(--border);
      background: var(--card-bg);
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .chat-input-row {
      display: flex;
      gap: 6px;
    }
    .chat-input {
      flex: 1;
      background: #090d14;
      border: 1px solid var(--border);
      border-radius: 6px;
      color: #fff;
      padding: 8px 10px;
      font-size: 12px;
      outline: none;
    }
    .ref-pill-btn {
      font-size: 11px;
      padding: 3px 8px;
      background: rgba(0, 230, 118, 0.12);
      color: var(--neon-green);
      border: 1px solid rgba(0, 230, 118, 0.3);
      border-radius: 4px;
      cursor: pointer;
      align-self: flex-start;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    /* Status Footer */
    footer {
      height: 28px;
      background: #090d14;
      border-top: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 16px;
      font-size: 11px;
      color: var(--text-muted);
    }
    .toast {
      position: fixed;
      bottom: 40px;
      right: 20px;
      background: var(--neon-green);
      color: #000;
      font-weight: 600;
      padding: 8px 16px;
      border-radius: 6px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.5);
      display: none;
      z-index: 999;
    }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <span class="brand-icon">⚡</span>
      <span>LANtern Code Studio</span>
      <span class="project-pill">$projectName</span>
      <span style="font-size:12px; color:#64748b;">($branch)</span>
    </div>
    <div class="pairing-code-badge" title="Team Global Pairing Code">
      <span>🔗 PAIR:</span>
      <span id="pairing-code-val">$pairingCode</span>
    </div>
    <div class="header-actions">
      <span style="font-size:12px; color:var(--neon-green); display:flex; align-items:center; gap:5px;">
        <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:var(--neon-green);"></span>
        LAN Live ($hostIp:$port)
      </span>
      <button class="btn btn-save" id="save-btn" onclick="saveActiveFile()">💾 Save (Ctrl+S)</button>
    </div>
  </header>

  <div class="studio-container">
    <!-- Sidebar -->
    <div class="sidebar">
      <div class="sidebar-header">
        <span>Files Explorer</span>
        <button class="btn" style="padding:2px 6px; font-size:11px;" onclick="promptNewFile()">+ File</button>
      </div>
      <div class="file-tree" id="file-list">
        <!-- Rendered by JS -->
      </div>
    </div>

    <!-- Code Editor -->
    <div class="editor-zone">
      <div class="tab-bar" id="tab-bar">
        <div class="tab active" id="active-tab-title">Loading...</div>
      </div>
      <div class="editor-wrapper">
        <div class="gutter" id="gutter"></div>
        <textarea class="code-textarea" id="code-editor" spellcheck="false" placeholder="Select a file to edit..."></textarea>
      </div>
    </div>

    <!-- Chat & Code Reference Drawer -->
    <div class="chat-drawer">
      <div class="chat-header">
        <span>💬 Team Collaboration</span>
        <span style="font-size:11px; color:var(--cyan);" id="peer-count">2 online</span>
      </div>
      <div class="chat-messages" id="chat-messages">
        <!-- Chat bubbles rendered here -->
      </div>
      <div class="chat-input-bar">
        <button class="ref-pill-btn" onclick="attachCurrentLineReference()">
          <span>📌 Reference line:</span>
          <span id="current-ref-label">Line 1</span>
        </button>
        <div class="chat-input-row">
          <input type="text" class="chat-input" id="chat-text" placeholder="Type team message..." onkeydown="if(event.key==='Enter') sendChatMessage()">
          <button class="btn" onclick="sendChatMessage()">Send</button>
        </div>
      </div>
    </div>
  </div>

  <footer>
    <div style="display:flex; gap:16px;">
      <span id="cursor-pos">Ln 1, Col 1</span>
      <span>UTF-8</span>
      <span>Spaces: 2</span>
    </div>
    <div>
      <span>LANtern Mobile P2P Sync Active</span>
    </div>
  </footer>

  <div class="toast" id="toast">Saved successfully!</div>

  <script>
    let files = [];
    let activeFile = null;
    let currentLine = 1;
    let pendingRef = null;

    async function loadProject() {
      try {
        const res = await fetch('/api/files');
        files = await res.json();
        renderFileList();
        if (files.length > 0) {
          openFile(files[0].path);
        }
      } catch (e) {
        console.error('Failed to load files:', e);
      }
      loadChat();
    }

    function renderFileList() {
      const container = document.getElementById('file-list');
      container.innerHTML = '';
      files.forEach(f => {
        const div = document.createElement('div');
        div.className = 'file-item' + (activeFile && activeFile.path === f.path ? ' active' : '');
        div.innerHTML = '📄 ' + f.name;
        div.onclick = () => openFile(f.path);
        container.appendChild(div);
      });
    }

    async function openFile(path, jumpToLine = null) {
      try {
        const res = await fetch('/api/file?path=' + encodeURIComponent(path));
        const fileData = await res.json();
        activeFile = fileData;
        document.getElementById('active-tab-title').textContent = fileData.name;
        const editor = document.getElementById('code-editor');
        editor.value = fileData.content || '';
        renderFileList();
        updateGutter();

        if (jumpToLine) {
          goToLine(jumpToLine);
        }
      } catch (e) {
        console.error('Failed to open file:', e);
      }
    }

    function updateGutter() {
      const editor = document.getElementById('code-editor');
      const lines = editor.value.split('\n').length;
      const gutter = document.getElementById('gutter');
      let html = '';
      for (let i = 1; i <= lines; i++) {
        html += '<div class="gutter-line' + (i === currentLine ? ' highlight' : '') + '">' + i + '</div>';
      }
      gutter.innerHTML = html;
    }

    function goToLine(lineNum) {
      const editor = document.getElementById('code-editor');
      const lines = editor.value.split('\n');
      if (lineNum < 1 || lineNum > lines.length) return;
      
      let charPos = 0;
      for (let i = 0; i < lineNum - 1; i++) {
        charPos += lines[i].length + 1;
      }
      editor.focus();
      editor.setSelectionRange(charPos, charPos + lines[lineNum - 1].length);
      currentLine = lineNum;
      updateGutter();

      // Scroll editor
      const lineHeight = 22;
      editor.scrollTop = (lineNum - 5) * lineHeight;
      document.getElementById('cursor-pos').textContent = 'Ln ' + lineNum + ', Col 1';
      document.getElementById('current-ref-label').textContent = (activeFile ? activeFile.name : '') + ' : Ln ' + lineNum;
    }

    async function saveActiveFile() {
      if (!activeFile) return;
      const content = document.getElementById('code-editor').value;
      try {
        await fetch('/api/save', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            fileId: activeFile.id,
            path: activeFile.path,
            content: content
          })
        });
        showToast('Saved to mobile & /home/LANtern/projects/!');
      } catch (e) {
        alert('Failed to save file: ' + e);
      }
    }

    function showToast(msg) {
      const t = document.getElementById('toast');
      t.textContent = msg;
      t.style.display = 'block';
      setTimeout(() => t.style.display = 'none', 2500);
    }

    async function promptNewFile() {
      const name = prompt('Enter new file path (e.g. src/Service.kt):');
      if (!name) return;
      await fetch('/api/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ path: name, name: name.split('/').pop(), content: '' })
      });
      loadProject();
    }

    // Chat System with Code Referencing
    async function loadChat() {
      try {
        const res = await fetch('/api/chat');
        const msgs = await res.json();
        renderChat(msgs);
      } catch (e) {}
    }

    function renderChat(msgs) {
      const box = document.getElementById('chat-messages');
      box.innerHTML = '';
      msgs.forEach(m => {
        const div = document.createElement('div');
        div.className = 'chat-bubble';
        let refHtml = '';
        if (m.hasCodeReference) {
          refHtml = `
            <div class="code-ref-card" onclick="openFile('${'$'}{m.referencedFilePath}', ${'$'}{m.referencedLineNumber})">
              <div class="ref-header">
                <span>📍 ${'$'}{m.referencedFilePath} : Line ${'$'}{m.referencedLineNumber}</span>
                <span>Jump &rarr;</span>
              </div>
              <div class="ref-snippet">${'$'}{m.referencedCodeSnippet || '(Code reference)'}</div>
            </div>
          `;
        }
        div.innerHTML = `
          <div class="chat-meta">
            <span class="sender-name">${'$'}{m.senderName}</span>
            <span>${'$'}{new Date(m.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
          </div>
          <div>${'$'}{m.text}</div>
          ${'$'}{refHtml}
        `;
        box.appendChild(div);
      });
      box.scrollTop = box.scrollHeight;
    }

    function attachCurrentLineReference() {
      if (!activeFile) return;
      const editor = document.getElementById('code-editor');
      const lines = editor.value.split('\n');
      const snippet = lines[currentLine - 1] || '';
      pendingRef = {
        filePath: activeFile.path,
        lineNumber: currentLine,
        snippet: snippet.trim()
      };
      document.getElementById('current-ref-label').textContent = activeFile.name + ':' + currentLine + ' (Attached)';
    }

    async function sendChatMessage() {
      const input = document.getElementById('chat-text');
      const text = input.value.trim();
      if (!text) return;

      const payload = {
        senderName: 'Web Browser Dev',
        text: text,
        hasCodeReference: !!pendingRef,
        referencedFilePath: pendingRef ? pendingRef.filePath : '',
        referencedLineNumber: pendingRef ? pendingRef.lineNumber : 0,
        referencedCodeSnippet: pendingRef ? pendingRef.snippet : ''
      };

      await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      input.value = '';
      pendingRef = null;
      document.getElementById('current-ref-label').textContent = 'Line ' + currentLine;
      loadChat();
    }

    // Event listeners
    document.getElementById('code-editor').addEventListener('input', () => {
      updateGutter();
    });

    document.getElementById('code-editor').addEventListener('keyup', (e) => {
      const editor = e.target;
      const textBefore = editor.value.substr(0, editor.selectionStart);
      const lineNum = textBefore.split('\n').length;
      const colNum = textBefore.length - textBefore.lastIndexOf('\n');
      currentLine = lineNum;
      document.getElementById('cursor-pos').textContent = 'Ln ' + lineNum + ', Col ' + colNum;
      document.getElementById('current-ref-label').textContent = (activeFile ? activeFile.name : '') + ' : Ln ' + lineNum;
      updateGutter();
    });

    document.getElementById('code-editor').addEventListener('scroll', (e) => {
      document.getElementById('gutter').scrollTop = e.target.scrollTop;
    });

    document.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        saveActiveFile();
      }
    });

    // Auto poll chat every 3s
    setInterval(loadChat, 3000);

    // Initial load
    loadProject();
  </script>
</body>
</html>
    """.trimIndent()
  }
}
