# Nova AI Assistant PRO

[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/UI-Java%20Swing-blue.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Nova AI Assistant PRO** is a portfolio-ready Java Swing desktop assistant built for **CodeAlpha Task 3 (Artificial Intelligence Chatbot)**. It delivers a modern ChatGPT-style experience with multi-session chat, productivity tools, themed UI, and file-based persistence — no external databases or cloud services required.

<p align="center">
  <strong>Chat · Learn · Plan · Practice · Export</strong>
</p>

---

## Highlights

| Area | Capabilities |
|------|----------------|
| **AI Chat** | Intent recognition, conversation memory, typing indicator, multi-session history |
| **Appearance** | Light, Dark, and Midnight themes with live theme switcher |
| **Productivity** | Notes Manager, Study Planner, Resume Assistant, Interview Simulator |
| **Insights** | Statistics dashboard across chats, notes, goals, and interviews |
| **Data** | Export formatted chats and notes; raw chat backup supported |

---

## Screenshots

> Add screenshots to a `screenshots/` folder and link them here.

| Main Chat | Dark Mode | Statistics |
|-----------|-----------|------------|
| *screenshots/chat.png* | *screenshots/dark.png* | *screenshots/stats.png* |

---

## Features

### Core chatbot
- ChatGPT-like layout with sidebar session list
- New / rename / delete / favorite / search conversations
- User & bot bubbles with timestamps
- Rule-based **AIEngine** with synonym-aware **IntentClassifier**
- Categories: Java, Python, C, AI, ML, Data Science, Web, Career, Interview, Resume, and more

### Themes & settings
- **Dark mode** plus **Theme Switcher** (Light · Dark · Midnight)
- **User Settings**: display name, email, theme preference
- Settings persist in `data/user_profile.properties`

### Statistics dashboard
- **StatisticsManager** aggregates usage from all local data files
- Chat sessions, messages, notes, study goals, exams, interview scores
- Refreshable detailed text report

### Export
- **Export Chat (Formatted)** — readable transcript by session
- **Export Chat (Raw Backup)** — full `chat_history.txt` copy
- **Export Notes** — all notes to a single `.txt` file

### Built-in tools
- **Resume Assistant** — build & export resume (TXT / PDF)
- **Interview Simulator** — Java, Python, HR modes with scoring
- **Notes Manager** — add, edit, delete, search notes
- **Study Planner** — daily goals, exam countdown, progress bar

### About
- Dedicated **About** dialog with version and feature overview

---

## Tech stack

| Layer | Technology |
|-------|------------|
| Language | Java 8+ |
| UI | Java Swing |
| Storage | Plain text & `.properties` files |
| Build | `javac` (no Maven/Gradle required) |

---

## Project structure

```text
Main.java
LoginFrame.java
NovaAIFrame.java
ChatPanel.java
MessageBubble.java
AIEngine.java
IntentClassifier.java
ConversationMemory.java
Message.java
ChatSession.java
ChatHistoryData.java
FileStore.java
ThemeManager.java
StatisticsManager.java
UserProfile.java
ResumeAssistant.java
InterviewSimulator.java
NotesManager.java
StudyPlanner.java
RoundedBorder.java
data/
  chat_history.txt
  notes.txt
  study_tasks.txt
  interview_history.txt
  user_profile.properties
  admin.properties
```

---

## Getting started

### Prerequisites
- [JDK 8](https://www.oracle.com/java/technologies/downloads/) or newer
- Terminal / PowerShell / Command Prompt

### Clone & run

```bash
git clone https://github.com/Sathwikadondapati25CodeAlpha-AI-Assistant-PRO.git
cd CodeAlpha_AI_Assistant_PRO
javac *.java
java Main
```

1. **Create admin account** on first launch (login screen).
2. Log in and open **Nova AI Assistant PRO**.
3. Use the menu bar: **File**, **View**, **Tools**, **Settings**, **Help**.

---

## Usage guide

### Menu overview

| Menu | Items |
|------|--------|
| **File** | Export Chat (Formatted / Raw), Export Notes |
| **View** | Statistics Dashboard, Theme Switcher |
| **Tools** | Resume Assistant, Interview Simulator, Notes Manager, Study Planner |
| **Settings** | User Settings |
| **Help** | About |

### Theme switcher
Choose **View → Theme Switcher →** Light, Dark, or Midnight. The selection is saved automatically.

### Keyboard tips
- Press **Enter** in the chat input to send a message.
- Double-click a conversation in the sidebar to rename it.

---

## Data storage

All data is stored locally under `data/`:

| File | Purpose |
|------|---------|
| `chat_history.txt` | Session metadata + messages |
| `notes.txt` | Notes Manager entries |
| `study_tasks.txt` | Daily goals & exams |
| `interview_history.txt` | Interview simulator results |
| `user_profile.properties` | Name, email, theme |
| `admin.properties` | Admin login (local) |

> **Privacy:** Data never leaves your machine unless you export it.

---

## Example conversation

```
You:  I am learning Python
Nova: That's great! Python is a beginner-friendly programming language.
      Are you learning it for AI, web development, or automation?

You:  AI
Nova: Excellent choice. Python is the most popular language for AI and Machine Learning.
```

---

## Architecture

```text
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  NovaAIFrame │────▶│   AIEngine   │────▶│ IntentClassifier │
└──────┬──────┘     └──────────────┘     └─────────────┘
       │
       ├── FileStore (chat, profile)
       ├── ThemeManager (themes)
       ├── StatisticsManager (dashboard)
       └── Tools: Resume / Interview / Notes / Study
```

---

## Roadmap

- [ ] Fuzzy intent matching (Levenshtein)
- [ ] Markdown rendering in chat bubbles
- [ ] Optional OpenAI / LLM API integration
- [ ] Packaged `.jar` / installer for Windows & macOS
- [ ] Unit tests for `AIEngine` and file parsers

---

## Contributing

1. Fork the repository  
2. Create a feature branch: `git checkout -b feature/my-feature`  
3. Commit changes: `git commit -m "Add my feature"`  
4. Push and open a Pull Request  

---

## License

This project is released under the **MIT License**. See [LICENSE](LICENSE) for details.

---

## Author & acknowledgments

Built for **CodeAlpha Internship Task 3** — Artificial Intelligence Chatbot.  
Suitable for GitHub portfolios, LinkedIn project posts, and Java Swing demonstrations.

**Star this repo** if it helped your learning journey.
