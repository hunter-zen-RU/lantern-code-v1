# LANtern Code 💡 - Collaborative Android IDE & Workspace

LANtern Code is an Android IDE and collaborative development studio engineered for mobile-first developers, hackathon teams, and pair-programming squads. It combines desktop-grade project management, local SQLite/Room workspace persistence, GitHub version control, WebRTC peer-to-peer live collaboration with invite links, custom API & local AI vibecoding agents, comprehensive project audits, and automated test runners.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed via interactive consultation and form the core specification:
> 
> - **Collaboration & Sharing Mode**: WebRTC peer-to-peer room architecture with shareable invite links (`lantern://join?room=...`), peer session negotiation, and direct data-channel synchronization for low-latency multi-cursor editing and file transfers.
> - **AI Vibecoding & Audit Engine**: Dual-mode engine supporting user-configured custom API endpoints (compatible with local Ollama, LM Studio, vLLM, OpenAI-compatible APIs) alongside a deterministic on-device agent runner for offline vibecoding, syntax fixes, security scans, and code audits.
> - **Workspace Focus**: Comprehensive IDE suite integrating an interactive multi-tab code editor, Git/GitHub manager (clone, branch, commit, push, pull, diffs), Room SQLite database explorer and query console, automated test runner, and cross-platform deployment export.

---

### 1. Overview & Core Concept

LANtern Code bridges localized offline development with modern cloud and peer workflows. Mobile developers can manage full codebases on Android tablets and phones without compromise.

- **Target Persona**: Mobile software engineers, hackathon participants, students, and pair programmers needing a rapid, zero-setup collaborative coding environment on Android.
- **Core Value Proposition**:
  1. Full Git/GitHub integration for cloning repositories, committing, branching, and pushing code.
  2. Built-in SQLite database engine (Room) for local project metadata, file trees, commit histories, team sessions, and an interactive SQL query playground.
  3. Real-time WebRTC collaborative rooms with instant invite links, QR codes, and live peer activity status.
  4. AI vibecoding assistance supporting custom API endpoints (e.g., self-hosted Ollama/vLLM) and a local offline agent for code generation, inline refactoring, security audits, and complexity scoring.
  5. Built-in test runner, dependency audit scanner, and cross-platform build/export preview.

---

### 2. User Experience & Visual Design

#### Visual Identity & Aesthetic Direction
- **Vibe**: Cyber-Industrial Neon Terminal / Obsidian Green. Inspired by the neon green Wi-Fi lantern emblem (`Neon_Wi_Fi_Git_Lantern_Emblem_cb1b22ba.png`).
- **Color Palette (Material 3 Tokens)**:
  - `primary`: `#00E676` (Electric Lantern Neon Green)
  - `onPrimary`: `#003915`
  - `primaryContainer`: `#005322`
  - `onPrimaryContainer`: `#69FFA1`
  - `surface` & `background`: `#0D1117` (Deep Obsidian Black/Slate)
  - `surfaceVariant`: `#161B22` (Subtle dark panel border)
  - `outline`: `#30363D`
  - `secondary`: `#38BDF8` (Cyan Blue for Git branches and network links)
  - `tertiary`: `#F59E0B` (Amber for warnings, audits, and test alerts)
  - `error`: `#F85149` (Vivid Crimson for syntax errors and test failures)
- **Typography & Layout**:
  - Monospace font styling for code editors, line numbers, diff views, and SQL results.
  - Clean M3 typography for navigation rails, bottom sheets, and audit dashboards.
  - Fluid adaptive layouts: responsive NavigationRail on tablets/foldables and NavigationBar on compact phones.
  - Safe-area insets handled edge-to-edge (`enableEdgeToEdge`, `navigationBarsPadding`, `statusBarsPadding`).

#### Key User Flows & Screen Architecture
1. **Workspace Hub (Projects & Repositories)**:
   - Dashboard displaying active projects, Git branch indicators, dirty file badges, and recent audits.
   - Quick Actions: Create Project, Clone from GitHub (with Personal Access Token input), or Initialize Local SQLite Project.
2. **Code Studio (Editor & Navigation)**:
   - Expandable hierarchical file tree with file creation, renaming, and deletion.
   - Tabbed code editor with line numbering, syntax highlighting, search/replace, cursor tracking, and indentation helpers.
   - Live AI Vibecoding drawer: floating agent bar to "Vibecode Feature", "Audit File", "Explain Code", or "Refactor Selection".
3. **P2P Collaboration Hub (WebRTC Rooms & Invite Links)**:
   - Create Room: Generates unique room ID, encrypted peer token, and shareable invite link (`lantern://room/{roomId}` and Web-compatible link).
   - QR code display and one-tap clipboard copy for instant team joining.
   - Connected peers list showing peer badges, permissions (Read-Only / Full-Write), and active files.
4. **SQLite & Data Management Console**:
   - Visual schema browser for project tables, columns, and relations.
   - Interactive SQL query runner with tabular results, query execution metrics, and sample data generator.
5. **Audit, Testing & Deployment Center**:
   - Automated code audit: security vulnerability checks (hardcoded keys, SQL injections, insecure permissions), cyclomatic complexity score, and performance suggestions.
   - Test runner: simulated unit test suite with pass/fail telemetry, coverage indicators, and execution timings.
   - Deployment & Export: multi-target packaging (ZIP archive, Git push, Dockerfile generator, and Android build config preview).

---

### 3. Key Product Decisions & Trade-Offs

- **WebRTC Peer-to-Peer with Invite Links**:
  - *Chosen Approach*: WebRTC room signaling representation with invite link generation, QR codes, and local session management. Peers exchange state updates and cursor telemetry over low-latency peer channels.
  - *Why*: Allows zero-server local network and WAN collaboration without locking developers into costly cloud relays.
- **Custom AI API Endpoint & Local Agent Runner**:
  - *Chosen Approach*: Configurable endpoint settings (Base URL, Model Name, API Key) supporting local Ollama servers (e.g., `http://10.0.2.2:11434/v1` or LAN IP) and cloud providers, combined with an embedded offline rule-based agent for audits and syntax checks when offline.
  - *Why*: Ensures developers can vibecode completely privately on air-gapped local networks using self-hosted models or connect to preferred cloud endpoints.
- **Room Database Architecture for Workspaces**:
  - *Chosen Approach*: Robust Room schema managing `ProjectEntity`, `FileEntity`, `GitCommitEntity`, `AuditReportEntity`, and `CollaboratorEntity`.
  - *Why*: High-speed reactive SQLite queries (`Flow<List<T>>`), atomic transactions, and zero external database overhead.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        LANtern Code Studio UI                          │
│   ┌───────────────┬───────────────────┬───────────────┬────────────┐   │
│   │ Project Tree  │ Code Editor Tabs  │ AI Vibecoding │ SQL Studio │   │
│   └───────┬───────┴─────────┬─────────┴───────┬───────┴──────┬─────┘   │
└───────────┼─────────────────┼─────────────────┼──────────────┼─────────┘
            ▼                 ▼                 ▼              ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          Core ViewModels                               │
│      WorkspaceViewModel │ EditorViewModel │ CollaborationViewModel     │
│      AuditViewModel     │ DatabaseViewModel │ SettingsViewModel        │
└───────────┬─────────────────┬─────────────────┬──────────────┬─────────┘
            ▼                 ▼                 ▼              ▼
┌──────────────────┐  ┌──────────────────┐  ┌────────────────────────────┐
│   Room Database  │  │   WebRTC P2P     │  │     Custom AI Engine       │
│ - Projects       │  │ - Room Signaling │  │ - Custom Endpoint Client   │
│ - Files & Diffs  │  │ - Invite Links   │  │   (Ollama / OpenAI spec)   │
│ - Git Commits    │  │ - P2P Sync Hub   │  │ - Local Offline Agent      │
│ - Audit Records  │  │ - Data Channels  │  │ - Vulnerability Scanner    │
└──────────────────┘  └──────────────────┘  └────────────────────────────┘
            │                                              │
            ▼                                              ▼
┌───────────────────────────────────┐  ┌─────────────────────────────────┐
│       Git & GitHub Service        │  │   Test Runner & Build Deploy    │
│ - Remote Clone & PAT Auth         │  │ - Automated Test Executor       │
│ - Branching, Commits, History     │  │ - Cross-platform Build Packager │
└───────────────────────────────────┘  └─────────────────────────────────┘
```

#### Room Entities & SQLite Schema
1. `ProjectEntity`: `id`, `name`, `description`, `gitRemoteUrl`, `currentBranch`, `createdAt`, `updatedAt`
2. `FileEntity`: `id`, `projectId`, `path`, `filename`, `content`, `language`, `isModified`, `updatedAt`
3. `GitCommitEntity`: `id`, `projectId`, `commitHash`, `message`, `author`, `timestamp`, `branch`
4. `AuditReportEntity`: `id`, `projectId`, `score`, `issuesCount`, `vulnerabilitiesJson`, `timestamp`
5. `PeerSessionEntity`: `id`, `roomId`, `peerName`, `role`, `status`, `ipAddress`, `lastActive`

#### Adaptive App Launcher Icon Integration
- Integration of the custom neon Wi-Fi Git lantern icon asset (`/public/assets/aistudio/Neon_Wi_Fi_Git_Lantern_Emblem_cb1b22ba.png`) configured with Material You adaptive icon standards and density mipmap PNG fallbacks.
