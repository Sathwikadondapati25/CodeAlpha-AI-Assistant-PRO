import java.util.Random;

public class ResponseGenerator {
    private final Random random;

    private enum SubIntent {
        DEFINITION,
        EXAMPLE,
        ADVANTAGE,
        ROOT
    }

    private static final String[] GREETINGS = {
            "Hello! I am Nova AI Assistant PRO. How can I support you today?",
            "Hi there! Ready to learn and build something amazing?",
            "Hey! Great to see you. What would you like to talk about?",
            "Good to connect with you. How can I help?"
    };

    public ResponseGenerator() {
        this.random = new Random();
    }

    private SubIntent detectSubIntent(String normalized, IntentClassifier.Intent intent) {
        if (normalized.contains("example") || normalized.contains("sample") || normalized.contains("code") || normalized.contains("syntax")) {
            return SubIntent.EXAMPLE;
        }
        if (normalized.contains("advantage") || normalized.contains("benefit") || normalized.contains("why use") || normalized.contains("pros") || normalized.contains("feature")) {
            return SubIntent.ADVANTAGE;
        }
        if (normalized.contains("what is") || normalized.contains("wt is") || normalized.contains("explain") || normalized.contains("define") || normalized.contains("meaning")) {
            return SubIntent.DEFINITION;
        }
        
        String rootKeyword = getTopicKeyword(intent);
        if (rootKeyword != null && normalized.equals(rootKeyword)) {
            return SubIntent.ROOT;
        }
        
        return SubIntent.DEFINITION;
    }

    private String getTopicKeyword(IntentClassifier.Intent intent) {
        switch (intent) {
            case HTML: return "html";
            case CSS: return "css";
            case JAVASCRIPT: return "javascript";
            case REACT: return "react";
            case SQL: return "sql";
            case DBMS: return "dbms";
            case OOP: return "oop";
            case DSA: return "dsa";
            case OS: return "os";
            case COMPUTER_NETWORKS: return "computer networks";
            case JAVA: return "java";
            case PYTHON: return "python";
            case C_PROGRAMMING: return "c programming";
            case AI: return "ai";
            case ML: return "ml";
            case DATA_SCIENCE: return "data science";
            case WEB_DEVELOPMENT: return "web development";
            default: return null;
        }
    }

    public String generateResponse(IntentClassifier.Intent intent, String userInput, String normalized, ConversationMemory memory) {
        SubIntent sub = detectSubIntent(normalized, intent);

        switch (intent) {
            case GREETING:
                memory.setPendingQuestion("");
                memory.rememberUserMessage(userInput, "general");
                return pick(GREETINGS);

            case HTML:
                memory.setPendingQuestion("html_follow_up");
                memory.rememberUserMessage(userInput, "html");
                return handleHtml(sub);

            case CSS:
                memory.setPendingQuestion("css_follow_up");
                memory.rememberUserMessage(userInput, "css");
                return handleCss(sub);

            case JAVASCRIPT:
                memory.setPendingQuestion("javascript_follow_up");
                memory.rememberUserMessage(userInput, "javascript");
                return handleJavascript(sub);

            case REACT:
                memory.setPendingQuestion("react_follow_up");
                memory.rememberUserMessage(userInput, "react");
                return handleReact(sub);

            case SQL:
                memory.setPendingQuestion("sql_follow_up");
                memory.rememberUserMessage(userInput, "sql");
                return handleSql(sub);

            case DBMS:
                memory.setPendingQuestion("dbms_follow_up");
                memory.rememberUserMessage(userInput, "dbms");
                return handleDbms(sub);

            case OOP:
                memory.setPendingQuestion("oop_follow_up");
                memory.rememberUserMessage(userInput, "oop");
                return handleOop(sub);

            case DSA:
                memory.setPendingQuestion("dsa_follow_up");
                memory.rememberUserMessage(userInput, "dsa");
                return handleDsa(sub);

            case OS:
                memory.setPendingQuestion("os_follow_up");
                memory.rememberUserMessage(userInput, "os");
                return handleOs(sub);

            case COMPUTER_NETWORKS:
                memory.setPendingQuestion("computer_networks_follow_up");
                memory.rememberUserMessage(userInput, "computer networks");
                return handleComputerNetworks(sub);

            case JAVA:
                memory.setPendingQuestion("java_follow_up");
                memory.rememberUserMessage(userInput, "java");
                return handleJava(sub);

            case PYTHON:
                memory.setPendingQuestion("python_goal");
                memory.rememberUserMessage(userInput, "python");
                return handlePython(sub);

            case C_PROGRAMMING:
                memory.setPendingQuestion("c_follow_up");
                memory.rememberUserMessage(userInput, "c programming");
                return handleC(sub);

            case AI:
                memory.setPendingQuestion("ai_follow_up");
                memory.rememberUserMessage(userInput, "ai");
                return handleAi(sub);

            case ML:
                memory.setPendingQuestion("ml_follow_up");
                memory.rememberUserMessage(userInput, "ml");
                return handleMl(sub);

            case DATA_SCIENCE:
                memory.setPendingQuestion("data_science_follow_up");
                memory.rememberUserMessage(userInput, "data science");
                return handleDataScience(sub);

            case WEB_DEVELOPMENT:
                memory.setPendingQuestion("web_follow_up");
                memory.rememberUserMessage(userInput, "web development");
                return handleWebDev(sub);

            case CAREER:
                memory.setPendingQuestion("career_follow_up");
                memory.rememberUserMessage(userInput, "career");
                return "Build your career with a clear roadmap: strengthen fundamentals, create 3-5 portfolio projects, practice DSA, optimize your resume, and apply consistently.";

            case INTERVIEW:
                memory.setPendingQuestion("interview_follow_up");
                memory.rememberUserMessage(userInput, "interview");
                return "For interviews, prepare core CS concepts, language fundamentals, and project explanations. Practice mock interviews and be ready to explain your trade-offs clearly.";

            case RESUME:
                memory.setPendingQuestion("resume_follow_up");
                memory.rememberUserMessage(userInput, "resume");
                return "For a strong resume, keep it one page, highlight impact with numbers, include 2-4 strong projects, and tailor keywords to the role description.";

            case COLLEGE:
                memory.setPendingQuestion("college_follow_up");
                memory.rememberUserMessage(userInput, "college");
                return "For college success, keep a weekly study plan, revise daily, maintain concise notes, and align semester learning with projects and internships.";

            case PROJECTS:
                memory.setPendingQuestion("projects_follow_up");
                memory.rememberUserMessage(userInput, "projects");
                return "Try portfolio-ready projects like an AI chatbot, expense tracker, smart resume analyzer, or student placement predictor. Want ideas based on your current level?";

            case INTERNSHIP:
                memory.setPendingQuestion("internship_follow_up");
                memory.rememberUserMessage(userInput, "internship");
                return "For internships, build real projects, maintain GitHub consistency, tailor your resume, and apply through LinkedIn, company portals, and referrals.";

            case MOTIVATION:
                memory.setPendingQuestion("");
                memory.rememberUserMessage(userInput, "motivation");
                return "You are closer than you think. Focus on one small milestone today, then build momentum daily. Consistency beats intensity.";

            case FOLLOW_UP:
                String followUpResponse = handleFollowUp(normalized, memory);
                memory.rememberUserMessage(userInput, memory.getLastTopic());
                return followUpResponse;

            case GENERAL:
            default:
                memory.setPendingQuestion("");
                memory.rememberUserMessage(userInput, "general");
                return generalResponse(userInput);
        }
    }

    // --- HTML SUB-INTENTS ---
    private String handleHtml(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "HTML (HyperText Markup Language) is the standard markup language for documents designed to be displayed in a web browser. It defines the structure and layout of a web document by using various tags and attributes.";
            case EXAMPLE:
                return "Here is a basic HTML5 code example:\n\n" +
                        "```html\n" +
                        "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "  <head>\n" +
                        "    <title>My First Page</title>\n" +
                        "  </head>\n" +
                        "  <body>\n" +
                        "    <h1>Hello World!</h1>\n" +
                        "    <p>This is my first HTML page.</p>\n" +
                        "  </body>\n" +
                        "</html>\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of HTML:\n" +
                        "- **Easy to Learn & Use:** Its syntax is highly readable and straightforward.\n" +
                        "- **Browser Compatibility:** Supported by all browsers globally.\n" +
                        "- **Search Engine Friendly:** Semantic HTML tags make it easier for search engines (SEO) to index content.\n" +
                        "- **Integration:** Easily integrates with styling sheets (CSS) and scripting languages (JavaScript).";
            case ROOT:
            default:
                return "**HTML (HyperText Markup Language)** is the standard language used to create and structure web pages.\n\n" +
                        "It defines elements such as:\n" +
                        "* Headings (`<h1>`)\n" +
                        "* Paragraphs (`<p>`)\n" +
                        "* Images (`<img>`)\n" +
                        "* Links (`<a>`)\n" +
                        "* Tables (`<table>`)\n" +
                        "* Forms (`<form>`)\n\n" +
                        "### Simple Example\n\n" +
                        "```html\n" +
                        "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <title>My First Page</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h1>Hello World!</h1>\n" +
                        "    <p>This is my first HTML page.</p>\n" +
                        "</body>\n" +
                        "</html>\n" +
                        "```\n\n" +
                        "### How HTML works with other technologies\n\n" +
                        "```text\n" +
                        "HTML  → Structure\n" +
                        "CSS   → Design & Styling\n" +
                        "JavaScript → Interactivity\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* HTML creates a button.\n" +
                        "* CSS makes the button look attractive.\n" +
                        "* JavaScript makes the button perform actions when clicked.\n\n" +
                        "### Uses of HTML\n\n" +
                        "* Building websites\n" +
                        "* Creating web forms\n" +
                        "* Displaying text, images, and videos\n" +
                        "* Developing web applications\n\n" +
                        "If you're a beginner in web development, the usual learning path is:\n\n" +
                        "```text\n" +
                        "HTML → CSS → JavaScript → React\n" +
                        "```\n\n" +
                        "HTML is the foundation of every website. ";
        }
    }

    // --- CSS SUB-INTENTS ---
    private String handleCss(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "CSS (Cascading Style Sheets) is a stylesheet language used to describe the presentation of a document written in HTML. It controls the colors, fonts, layouts, and animations of web pages.";
            case EXAMPLE:
                return "Here is a CSS code example:\n\n" +
                        "```css\n" +
                        "body {\n" +
                        "  background-color: #f3f4f6;\n" +
                        "  font-family: 'Segoe UI', sans-serif;\n" +
                        "}\n" +
                        "h1 {\n" +
                        "  color: #ff6b6b;\n" +
                        "  text-align: center;\n" +
                        "}\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of CSS:\n" +
                        "- **Separation of Concerns:** Keeps design separate from content (HTML).\n" +
                        "- **Consistency:** Apply the same styles across multiple pages.\n" +
                        "- **Performance:** Pages load faster since style sheets are cached.";
            case ROOT:
            default:
                return "**CSS (Cascading Style Sheets)** is the standard language used to style and layout web pages.\n\n" +
                        "It defines properties such as:\n" +
                        "* Colors (`color`, `background-color`)\n" +
                        "* Fonts (`font-family`, `font-size`)\n" +
                        "* Spacing (`margin`, `padding`)\n" +
                        "* Layouts (`display: flex`, `display: grid`)\n" +
                        "* Borders (`border`, `border-radius`)\n" +
                        "* Transitions (`transition`, `animation`)\n\n" +
                        "### Simple Example\n\n" +
                        "```css\n" +
                        "body {\n" +
                        "    background-color: #f3f4f6;\n" +
                        "    font-family: 'Segoe UI', sans-serif;\n" +
                        "}\n" +
                        "h1 {\n" +
                        "    color: #ff6b6b;\n" +
                        "    text-align: center;\n" +
                        "}\n" +
                        "```\n\n" +
                        "### How CSS works with other technologies\n\n" +
                        "```text\n" +
                        "HTML  → Structure\n" +
                        "CSS   → Design & Styling\n" +
                        "JavaScript → Interactivity\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* HTML creates a button.\n" +
                        "* CSS makes the button look attractive (color, shadow, hover).\n" +
                        "* JavaScript makes the button perform actions when clicked.\n\n" +
                        "### Uses of CSS\n\n" +
                        "* Styling HTML structures\n" +
                        "* Creating responsive layouts (Mobile-first design)\n" +
                        "* Implementing page transitions and animations\n" +
                        "* Adjusting typography and visual hierarchies\n\n" +
                        "If you're a beginner in web development, the usual learning path is:\n\n" +
                        "```text\n" +
                        "HTML → CSS → JavaScript → React\n" +
                        "```\n\n" +
                        "CSS is what brings websites to life visually. ";
        }
    }

    // --- JAVASCRIPT SUB-INTENTS ---
    private String handleJavascript(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "JavaScript (JS) is a lightweight, interpreted programming language with first-class functions. It is best known as the scripting language for web pages, enabling client-side interactivity.";
            case EXAMPLE:
                return "Here is a basic JavaScript code example:\n\n" +
                        "```javascript\n" +
                        "function greetUser(name) {\n" +
                        "    console.log(\"Hello, \" + name + \"! Welcome to Nova.\");\n" +
                        "}\n" +
                        "greetUser(\"Developer\");\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of JavaScript:\n" +
                        "- **Client-Side Execution:** Executes quickly in the browser without server load.\n" +
                        "- **Interactivity:** Allows dynamic UI updates, mouse events, and animations.\n" +
                        "- **Full-Stack Potential:** Can be used on both frontend and backend (Node.js).";
            case ROOT:
            default:
                return "**JavaScript (JS)** is a lightweight, interpreted programming language with first-class functions, famous as the scripting language for Web pages.\n\n" +
                        "It controls actions such as:\n" +
                        "* Variables (`let`, `const`)\n" +
                        "* Functions and Events (`click`, `submit`)\n" +
                        "* DOM Manipulation (`document.querySelector`)\n" +
                        "* Asynchronous Calls (`fetch`, `promises`)\n" +
                        "* Storage (`localStorage`)\n" +
                        "* Conditions and Loops (`if`, `for`)\n\n" +
                        "### Simple Example\n\n" +
                        "```javascript\n" +
                        "function greetUser(name) {\n" +
                        "    console.log(\"Hello, \" + name + \"!\");\n" +
                        "}\n" +
                        "greetUser(\"Developer\");\n" +
                        "```\n\n" +
                        "### How JavaScript works with other technologies\n\n" +
                        "```text\n" +
                        "HTML  → Structure\n" +
                        "CSS   → Design & Styling\n" +
                        "JavaScript → Interactivity\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* HTML creates a button.\n" +
                        "* CSS makes the button look attractive.\n" +
                        "* JavaScript makes the button perform actions when clicked.\n\n" +
                        "### Uses of JavaScript\n\n" +
                        "* Adding interactivity to web pages\n" +
                        "* Fetching data from APIs asynchronously\n" +
                        "* Building backend servers (Node.js)\n" +
                        "* Developing full-stack web applications\n\n" +
                        "If you're a beginner in web development, the usual learning path is:\n\n" +
                        "```text\n" +
                        "HTML → CSS → JavaScript → React\n" +
                        "```\n\n" +
                        "JavaScript makes websites dynamic and responsive. ";
        }
    }

    // --- REACT SUB-INTENTS ---
    private String handleReact(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "React is an open-source, component-based front-end JavaScript library. It is maintained by Meta (Facebook) and is used to build user interfaces, especially single-page applications.";
            case EXAMPLE:
                return "Here is a React component example using Hooks:\n\n" +
                        "```jsx\n" +
                        "import React, { useState } from 'react';\n\n" +
                        "function Counter() {\n" +
                        "  const [count, setCount] = useState(0);\n" +
                        "  return (\n" +
                        "    <button onClick={() => setCount(count + 1)}>\n" +
                        "      Clicked {count} times\n" +
                        "    </button>\n" +
                        "  );\n" +
                        "}\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of React:\n" +
                        "- **Component-Based:** Build modular, self-managing components that are reusable.\n" +
                        "- **Virtual DOM:** React updates only the modified elements, making UI rendering incredibly fast.\n" +
                        "- **Rich Ecosystem:** Offers powerful state management, routing, and developer tools.";
            case ROOT:
            default:
                return "**React** is a popular component-based JavaScript library maintained by Meta for building modern user interfaces.\n\n" +
                        "It defines features such as:\n" +
                        "* Components (Reusable UI blocks)\n" +
                        "* Props (Data passing between components)\n" +
                        "* State (`useState` hook)\n" +
                        "* Effects (`useEffect` hook)\n" +
                        "* JSX (HTML-in-JS syntax)\n" +
                        "* Virtual DOM (High performance updates)\n\n" +
                        "### Simple Example\n\n" +
                        "```jsx\n" +
                        "import React, { useState } from 'react';\n\n" +
                        "function Counter() {\n" +
                        "    const [count, setCount] = useState(0);\n" +
                        "    return <button onClick={() => setCount(count + 1)}>Clicked {count} times</button>;\n" +
                        "}\n" +
                        "```\n\n" +
                        "### How React works with other technologies\n\n" +
                        "```text\n" +
                        "React  → Frontend UI\n" +
                        "NodeJS → Backend Services\n" +
                        "SQL/NoSQL → Data Storage\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* React renders the UI in the browser.\n" +
                        "* NodeJS handles API requests from the frontend.\n" +
                        "* Databases store user and application data.\n\n" +
                        "### Uses of React\n\n" +
                        "* Building Single Page Applications (SPAs)\n" +
                        "* Designing reusable UI component libraries\n" +
                        "* Developing responsive frontend layouts\n" +
                        "* Managing complex state across large apps\n\n" +
                        "If you're a beginner in web development, the usual learning path is:\n\n" +
                        "```text\n" +
                        "HTML → CSS → JavaScript → React\n" +
                        "```\n\n" +
                        "React is the industry standard for modern frontend applications. ";
        }
    }

    // --- SQL SUB-INTENTS ---
    private String handleSql(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "SQL (Structured Query Language) is the standard programming language used to manage relational databases and perform operations on the data within them.";
            case EXAMPLE:
                return "Here is a standard SQL query example:\n\n" +
                        "```sql\n" +
                        "SELECT name, email \n" +
                        "FROM users \n" +
                        "WHERE country = 'India' \n" +
                        "ORDER BY registration_date DESC;\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of SQL:\n" +
                        "- **Highly Standardized:** Declarative language used across MySQL, PostgreSQL, Oracle, SQL Server.\n" +
                        "- **Fast Queries:** Quickly retrieves millions of records using indexes.\n" +
                        "- **Data Integrity:** Enforces relationships and constraints (primary/foreign keys).";
            case ROOT:
            default:
                return "**SQL (Structured Query Language)** is the standard programming language used to manage and query relational database systems.\n\n" +
                        "It defines operations such as:\n" +
                        "* Retrieval (`SELECT`)\n" +
                        "* Insertion (`INSERT INTO`)\n" +
                        "* Modification (`UPDATE`)\n" +
                        "* Deletion (`DELETE`)\n" +
                        "* Database Joins (`INNER JOIN`, `LEFT JOIN`)\n" +
                        "* Table Creation (`CREATE TABLE`)\n\n" +
                        "### Simple Example\n\n" +
                        "```sql\n" +
                        "SELECT name, email \n" +
                        "FROM users \n" +
                        "WHERE status = 'active'\n" +
                        "ORDER BY name ASC;\n" +
                        "```\n\n" +
                        "### How SQL works with other technologies\n\n" +
                        "```text\n" +
                        "SQL       → Database Queries\n" +
                        "DBMS      → Database Management System\n" +
                        "Backend   → Database Connections (JDBC, ORM)\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* SQL specifies what data to retrieve.\n" +
                        "* DBMS (like MySQL) executes the query on disk.\n" +
                        "* Backend code fetches and processes the results.\n\n" +
                        "### Uses of SQL\n\n" +
                        "* Storing and retrieving user accounts\n" +
                        "* Querying transaction histories\n" +
                        "* Running analytical aggregates (GROUP BY)\n" +
                        "* Maintaining data relationships and integrity\n\n" +
                        "If you're studying database systems, the usual learning path is:\n\n" +
                        "```text\n" +
                        "SQL → DBMS → Backend Development → System Design\n" +
                        "```\n\n" +
                        "SQL is the language of data. ";
        }
    }

    // --- DBMS SUB-INTENTS ---
    private String handleDbms(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "A Database Management System (DBMS) is software designed to define, store, retrieve, manage, and security-control data databases.";
            case EXAMPLE:
                return "Here is a layout comparison of DBMS types:\n\n" +
                        "- **RDBMS (Relational):** Data stored in structured tables (e.g. MySQL, PostgreSQL).\n" +
                        "- **NoSQL (Document):** Data stored in flexible JSON documents (e.g. MongoDB).";
            case ADVANTAGE:
                return "Key advantages of a DBMS:\n" +
                        "- **Data Integrity & Security:** Prevents unauthorized access and keeps data clean.\n" +
                        "- **ACID Transactions:** Ensures transactions are Atomic, Consistent, Isolated, and Durable.\n" +
                        "- **Concurrency Control:** Allows multiple users to access data simultaneously without conflict.";
            case ROOT:
            default:
                return "**DBMS (Database Management System)** is system software used to create, manage, retrieve, and secure databases.\n\n" +
                        "It defines concepts such as:\n" +
                        "* ACID properties (Atomicity, Consistency, Isolation, Durability)\n" +
                        "* Database Schema & Normalization (1NF, 2NF, 3NF)\n" +
                        "* Data Integrity & Constraints\n" +
                        "* Indexing & Query Optimization\n" +
                        "* Backup & Crash Recovery\n" +
                        "* Concurrency Control & Locking\n\n" +
                        "### Simple Schema Example\n\n" +
                        "```text\n" +
                        "[Users Table]               [Orders Table]\n" +
                        "- id (Primary Key)  <--->   - user_id (Foreign Key)\n" +
                        "- name                      - order_date\n" +
                        "- email                     - total_amount\n" +
                        "```\n\n" +
                        "### How DBMS works with other technologies\n\n" +
                        "```text\n" +
                        "SQL       → Query Interface\n" +
                        "DBMS      → Storage Engine\n" +
                        "Backend   → Connection Pool\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* SQL specifies the command.\n" +
                        "* DBMS executes it on disk files and manages index locks.\n" +
                        "* Backend consumes it via JDBC or connection pools.\n\n" +
                        "### Uses of DBMS\n\n" +
                        "* Managing large-scale database operations\n" +
                        "* Enforcing transaction safety (ACID)\n" +
                        "* Controlling concurrent user data access\n" +
                        "* Keeping database backups and redundancy\n\n" +
                        "If you're studying databases, the usual learning path is:\n\n" +
                        "```text\n" +
                        "SQL → DBMS → Backend Development → System Design\n" +
                        "```\n\n" +
                        "DBMS is the brain behind data persistence. ";
        }
    }

    // --- OOP SUB-INTENTS ---
    private String handleOop(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Object-Oriented Programming (OOP) is a programming paradigm that structures programs into reusable pieces of code called 'classes' and 'objects'.";
            case EXAMPLE:
                return "Here is an OOP class structure example in Java:\n\n" +
                        "```java\n" +
                        "class Animal {\n" +
                        "  private String name;\n" +
                        "  public Animal(String name) { this.name = name; }\n" +
                        "  public void speak() { System.out.println(\"Animal sound\"); }\n" +
                        "}\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of OOP:\n" +
                        "- **Modularity:** Separation of concerns makes code easy to troubleshoot.\n" +
                        "- **Reusability:** Inheritance allows code sharing across multiple classes.\n" +
                        "- **Flexibility:** Polymorphism allows a single method interface to support different behaviors.";
            case ROOT:
            default:
                return "**OOP (Object-Oriented Programming)** is a software programming paradigm designed around classes and objects containing data and behaviors.\n\n" +
                        "It defines the four core pillars:\n" +
                        "* Encapsulation (Hiding data using private variables)\n" +
                        "* Inheritance (Reusing attributes/methods via parent classes)\n" +
                        "* Polymorphism (Allowing methods to perform different tasks)\n" +
                        "* Abstraction (Hiding complex implementation details)\n\n" +
                        "### Simple Example\n\n" +
                        "```java\n" +
                        "class Animal {\n" +
                        "    private String name;\n" +
                        "    public Animal(String name) { this.name = name; }\n" +
                        "    public void speak() { System.out.println(\"Animal sound\"); }\n" +
                        "}\n" +
                        "```\n\n" +
                        "### How OOP works with other software concepts\n\n" +
                        "```text\n" +
                        "OOP Principles   → Modular Code Structure\n" +
                        "Design Patterns  → Solving Common OOP Problems\n" +
                        "Clean Code       → Code Readability & Maintenance\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* OOP classes define structural objects.\n" +
                        "* Design Patterns (e.g. Factory, Singleton) configure how objects are built.\n" +
                        "* Clean code guidelines make objects readable and maintainable.\n\n" +
                        "### Uses of OOP\n\n" +
                        "* Building modular, reusable software\n" +
                        "* Developing large-scale application frameworks\n" +
                        "* Simulating real-world systems in code\n" +
                        "* Writing maintainable enterprise libraries\n\n" +
                        "If you're studying programming, the usual learning path is:\n\n" +
                        "```text\n" +
                        "Syntax → OOP → Data Structures → System Design\n" +
                        "```\n\n" +
                        "OOP is the foundation of modern software engineering. ";
        }
    }

    // --- DSA SUB-INTENTS ---
    private String handleDsa(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Data Structures and Algorithms (DSA) form the base of computer science problem-solving. A data structure organizes data, while an algorithm processes it.";
            case EXAMPLE:
                return "Here is a stack data structure push operation in Java:\n\n" +
                        "```java\n" +
                        "Stack<Integer> stack = new Stack<>();\n" +
                        "stack.push(10); // Pushes 10 onto the stack\n" +
                        "int val = stack.pop(); // Returns and removes 10\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of studying DSA:\n" +
                        "- **Optimal Performance:** Helps select the best data structure to minimize run-time (Time Complexity).\n" +
                        "- **Efficient Scaling:** Handles large data inputs smoothly without crash or latency.\n" +
                        "- **Interview Foundation:** Key benchmark for coding tests at Google, Meta, etc.";
            case ROOT:
            default:
                return "**DSA (Data Structures and Algorithms)** form the core foundation of problem-solving in computer science and programming.\n\n" +
                        "It defines structures and sorting models such as:\n" +
                        "* Linear Structures (Arrays, Linked Lists, Stacks, Queues)\n" +
                        "* Non-Linear Structures (Trees, Graphs, Hash Maps)\n" +
                        "* Sorting Algorithms (Quick Sort, Merge Sort)\n" +
                        "* Search Algorithms (Binary Search, DFS, BFS)\n" +
                        "* Dynamic Programming & Recursion\n" +
                        "* Big O Notation (Time and Space Complexity)\n\n" +
                        "### Simple Example\n\n" +
                        "```java\n" +
                        "// Using a Stack structure (Last In, First Out)\n" +
                        "Stack<Integer> stack = new Stack<>();\n" +
                        "stack.push(10); // Pushes 10\n" +
                        "int val = stack.pop(); // Returns 10\n" +
                        "```\n\n" +
                        "### How DSA works with code performance\n\n" +
                        "```text\n" +
                        "Optimal DSA Selection  → Lower CPU usage (Time)\n" +
                        "Optimal DSA Selection  → Lower memory footprint (Space)\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* Accessing array elements is O(1) time.\n" +
                        "* Finding items in an unsorted list is O(N) time.\n" +
                        "* Finding items in a Hash Map is O(1) time.\n\n" +
                        "### Uses of DSA\n\n" +
                        "* Optimizing software for memory and speed\n" +
                        "* Designing routing maps and pathfinders (graphs)\n" +
                        "* Managing system processes and queues (stacks/queues)\n" +
                        "* Standardizing code patterns in technical assessments\n\n" +
                        "If you're studying algorithms, the usual learning path is:\n\n" +
                        "```text\n" +
                        "Coding Syntax → Basic DSA → Advanced DSA → Competitive Coding\n" +
                        "```\n\n" +
                        "DSA is the key to writing scalable, efficient code. ";
        }
    }

    // --- OS SUB-INTENTS ---
    private String handleOs(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "An Operating System (OS) is system software that manages computer hardware, software resources, and provides common services for computer programs.";
            case EXAMPLE:
                return "Here is a visual map of OS roles:\n\n" +
                        "Hardware <-> OS Kernel <-> System Calls <-> User Applications";
            case ADVANTAGE:
                return "Key advantages of an OS:\n" +
                        "- **Resource Management:** Schedules processes to maximize CPU utilization.\n" +
                        "- **Abstraction:** Hides hardware complexity from applications via drivers.\n" +
                        "- **Memory Security:** Prevents processes from corrupting each other's memory space.";
            case ROOT:
            default:
                return "**OS (Operating System)** is the system software that sits between a computer's hardware and its applications.\n\n" +
                        "It coordinates processes such as:\n" +
                        "* CPU Scheduling (FCFS, Round Robin, SJF)\n" +
                        "* Memory Management (Virtual Memory, Paging, Segmentation)\n" +
                        "* Deadlock Handling (Banker's Algorithm, Detection)\n" +
                        "* Disk/File Systems (NTFS, FAT32, EXT4)\n" +
                        "* I/O Device Management\n" +
                        "* Inter-process Communication (IPC)\n\n" +
                        "### Process Scheduling Concept\n\n" +
                        "```text\n" +
                        "[Ready Queue]  →  [CPU Scheduler]  →  [Running State]\n" +
                        "                     (Round Robin)\n" +
                        "```\n\n" +
                        "### How OS interacts with software\n\n" +
                        "```text\n" +
                        "Hardware  →  OS Kernel  →  System Calls  →  Applications\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* Hardware stores physical memory addresses.\n" +
                        "* OS Kernel manages page tables to create Virtual Memory.\n" +
                        "* Applications request memory via System Calls (`malloc`).\n\n" +
                        "### Uses of OS\n\n" +
                        "* Managing resource allocation for CPU and RAM\n" +
                        "* Scheduling background and foreground processes\n" +
                        "* Enforcing security and folder access controls\n" +
                        "* Managing files, disks, and network interfaces\n\n" +
                        "If you're studying systems, the usual learning path is:\n\n" +
                        "```text\n" +
                        "Programming Language → Computer Architecture → Operating Systems → Distributed Systems\n" +
                        "```\n\n" +
                        "OS is the engine that keeps your computer running. ";
        }
    }

    // --- COMPUTER NETWORKS SUB-INTENTS ---
    private String handleComputerNetworks(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Computer Networks is the discipline studying how computers connect, communicate, and share files or internet data with each other.";
            case EXAMPLE:
                return "Here is a summary of the 7-layer OSI model layers:\n\n" +
                        "Physical -> Data Link -> Network -> Transport -> Session -> Presentation -> Application";
            case ADVANTAGE:
                return "Key advantages of Computer Networks:\n" +
                        "- **Resource Sharing:** Share files, databases, printers across devices.\n" +
                        "- **High Reliability:** Replicate data across different servers globally.\n" +
                        "- **Connectivity:** Enables global communication services like the internet and VoIP.";
            case ROOT:
            default:
                return "**Computer Networks** is the field of computer science that coordinates connections and data sharing between devices.\n\n" +
                        "It defines protocols and layers such as:\n" +
                        "* OSI 7-layer Model (Physical, Data Link, Network, Transport...)\n" +
                        "* TCP/IP 4-layer Model (Network Access, Internet, Transport, Application)\n" +
                        "* IP Addressing & Subnetting (IPv4, IPv6)\n" +
                        "* Routing & DNS Name Resolution\n" +
                        "* Protocols (TCP for reliability, UDP for speed, HTTP for web)\n" +
                        "* Security Protocols (SSL/TLS, HTTPS)\n\n" +
                        "### Simple OSI Stack Map\n\n" +
                        "```text\n" +
                        "Application (HTTP) → Transport (TCP) → Network (IP) → Link (Ethernet)\n" +
                        "```\n\n" +
                        "### How Networks transfer data\n\n" +
                        "```text\n" +
                        "Client requests URL  →  DNS resolves IP  →  TCP Handshake  →  HTTP Data Transfer\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* Client requests `google.com`.\n" +
                        "* DNS converts name to IP address.\n" +
                        "* Transport layer establishes reliable TCP connection.\n" +
                        "* Application layer sends HTTP Request.\n\n" +
                        "### Uses of Computer Networks\n\n" +
                        "* Accessing resources across the Web\n" +
                        "* Sharing files and databases between servers\n" +
                        "* Standardizing reliable packet transfers (TCP)\n" +
                        "* Securing online communications using encryption (HTTPS)\n\n" +
                        "If you're studying networking, the usual learning path is:\n\n" +
                        "```text\n" +
                        "Basic Hardware → Computer Networks → Cloud Computing → Distributed Security\n" +
                        "```\n\n" +
                        "Computer Networks form the nervous system of the global Internet. ";
        }
    }

    // --- JAVA SUB-INTENTS ---
    private String handleJava(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Java is a class-based, object-oriented language designed to have as few implementation dependencies as possible. It runs on the JVM, so code is portable across platforms.";
            case EXAMPLE:
                return "Here is a standard 'Hello World' program in Java:\n\n" +
                        "```java\n" +
                        "public class HelloWorld {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello, World!\");\n" +
                        "    }\n" +
                        "}\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of Java:\n" +
                        "- **Write Once, Run Anywhere (WORA):** Portable bytecode executed by the JVM.\n" +
                        "- **Automatic Memory Management:** Features a built-in Garbage Collector.\n" +
                        "- **Strong Type Safety:** Prevents illegal memory operations at runtime.";
            case ROOT:
            default:
                return "**Java** is a class-based, object-oriented, concurrent programming language designed to run on the JVM.\n\n" +
                        "It defines operations such as:\n" +
                        "* Object creation (`new` operator)\n" +
                        "* Platform Independence (Bytecode execution)\n" +
                        "* Exception Handling (`try`, `catch`)\n" +
                        "* Garbage Collection (Automatic memory cleaning)\n" +
                        "* Multithreading (`Thread` class, `Runnable`)\n" +
                        "* Rich Standard API (Collections framework)\n\n" +
                        "### Simple Example\n\n" +
                        "```java\n" +
                        "public class Hello {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello, World!\");\n" +
                        "    }\n" +
                        "}\n" +
                        "```\n\n" +
                        "### How Java works with systems\n\n" +
                        "```text\n" +
                        "Java Code (.java)  →  Compiler  →  Bytecode (.class)  →  JVM Execution\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* Compiler compiles source code to bytecode.\n" +
                        "* Java Virtual Machine (JVM) interprets bytecode to machine code.\n" +
                        "* Machine executes bytecode on the local CPU.\n\n" +
                        "### Uses of Java\n\n" +
                        "* Developing enterprise backend architectures\n" +
                        "* Building Android mobile applications\n" +
                        "* Designing big data pipelines (Hadoop, Spark)\n" +
                        "* Writing platform-independent desktop software\n\n" +
                        "If you're studying Java backend, the usual learning path is:\n\n" +
                        "```text\n" +
                        "Java Basics → Advanced OOP → Spring Boot Framework → SQL Databases\n" +
                        "```\n\n" +
                        "Java remains the enterprise standard for scalable backends. ";
        }
    }

    // --- PYTHON SUB-INTENTS ---
    private String handlePython(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Python is an interpreted, high-level, general-purpose programming language. It is famous for its simple, readable syntax.";
            case EXAMPLE:
                return "Here is a Python code example showing a list comprehension:\n\n" +
                        "```python\n" +
                        "squares = [x**2 for x in range(5)]\n" +
                        "print(squares)  # Output: [0, 1, 4, 9, 16]\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of Python:\n" +
                        "- **Simple & Readable:** Clean syntax similar to plain English.\n" +
                        "- **Massive Library Ecosystem:** Outstanding modules for AI/ML (numpy, pandas, tensorflow).\n" +
                        "- **High Productivity:** Write fewer lines of code compared to C++ or Java.";
            case ROOT:
            default:
                return "**Python** is an interpreted, high-level, general-purpose programming language famous for its simple syntax.\n\n" +
                        "It defines features such as:\n" +
                        "* Dynamic Typing (No variable type declaration)\n" +
                        "* Indentation-based scope (No braces `{}`)\n" +
                        "* Advanced Structures (Lists, Tuples, Dictionaries)\n" +
                        "* List Comprehensions\n" +
                        "* Object Oriented & Procedural support\n" +
                        "* Rich third-party packages (via `pip`)\n\n" +
                        "### Simple Example\n\n" +
                        "```python\n" +
                        "def calculate_squares(limit):\n" +
                        "    return [x**2 for x in range(limit)]\n\n" +
                        "print(calculate_squares(5)) # [0, 1, 4, 9, 16]\n" +
                        "```\n\n" +
                        "### How Python works in different fields\n\n" +
                        "```text\n" +
                        "Data Analytics   → Pandas, Numpy\n" +
                        "Machine Learning → Scikit-learn, PyTorch\n" +
                        "Web Apps         → Django, Flask\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* Pandas processes raw analytics metrics.\n" +
                        "* PyTorch/Scikit-learn trains predictive ML models.\n" +
                        "* Django creates server dashboards to show outcomes.\n\n" +
                        "### Uses of Python\n\n" +
                        "* Scripting and workflow automation\n" +
                        "* Developing Machine Learning and Deep Learning models\n" +
                        "* Scraping websites and data mining\n" +
                        "* Writing lightweight web services and REST APIs\n\n" +
                        "If you're studying Python, the usual learning path is:\n\n" +
                        "```text\n" +
                        "Python Syntax → Data Structures → Pandas/Numpy → ML Frameworks\n" +
                        "```\n\n" +
                        "Python is the absolute standard for AI and data science. ";
        }
    }

    // --- C SUB-INTENTS ---
    private String handleC(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "C is a procedural, structured, compiled programming language that offers low-level memory control.";
            case EXAMPLE:
                return "Here is a C code example illustrating pointer usage:\n\n" +
                        "```c\n" +
                        "int num = 10;\n" +
                        "int *ptr = &num; // Pointer stores the memory address of num\n" +
                        "printf(\"Value: %d\\n\", *ptr); // Dereferencing prints 10\n" +
                        "```";
            case ADVANTAGE:
                return "Key advantages of C:\n" +
                        "- **High Performance:** Compiled directly to machine code with minimal runtime overhead.\n" +
                        "- **Low-level Access:** Pointers allow direct address manipulation and hardware controls.\n" +
                        "- **Portability:** C compilers exist for almost every microchip architecture.";
            case ROOT:
            default:
                return "**C** is a procedural, structured, compiled programming language that offers low-level memory control.\n\n" +
                        "It defines processes such as:\n" +
                        "* Procedural Functions\n" +
                        "* Direct Memory Access (Pointers)\n" +
                        "* Manual Memory Allocation (`malloc`, `free`)\n" +
                        "* Compile-time Preprocessing (`#include`, `#define`)\n" +
                        "* Array & String array declarations\n" +
                        "* Header file structures (`.h` files)\n\n" +
                        "### Simple Example\n\n" +
                        "```c\n" +
                        "#include <stdio.h>\n\n" +
                        "int main() {\n" +
                        "    int val = 42;\n" +
                        "    int *ptr = &val; // Pointer holds address of val\n" +
                        "    printf(\"Value is: %d\\n\", *ptr);\n" +
                        "    return 0;\n" +
                        "}\n" +
                        "```\n\n" +
                        "### How C compiles to machine code\n\n" +
                        "```text\n" +
                        "Source Code (.c)  →  Preprocessor  →  Compiler  →  Assembler  →  Machine Code\n" +
                        "```\n\n" +
                        "Example:\n" +
                        "* Preprocessor expands header files and macros.\n" +
                        "* Compiler translates source to assembly code.\n" +
                        "* Assembler converts assembly to executable machine instructions.\n\n" +
                        "### Uses of C\n\n" +
                        "* Writing Operating System Kernels (Linux, Windows)\n" +
                        "* Developing embedded systems and microcontrollers\n" +
                        "* Building compiler utilities and language runtimes\n" +
                        "* High-performance gaming and graphics engines\n\n" +
                        "If you're studying systems programming, the usual learning path is:\n\n" +
                        "```text\n" +
                        "C Basics → Assembly Language → Operating Systems → System Architectures\n" +
                        "```\n\n" +
                        "C is the language closest to physical hardware. ";
        }
    }

    // --- AI SUB-INTENTS ---
    private String handleAi(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Artificial Intelligence (AI) is the simulation of human intelligence processes by machines, especially computer systems.";
            case EXAMPLE:
                return "Examples of AI applications in production include chatbot assistants, predictive search engines, self-driving cars, and computer vision filters.";
            case ADVANTAGE:
                return "Key advantages of AI:\n" +
                        "- **Automation:** Automates repetitive tasks without fatigue.\n" +
                        "- **Data Insights:** Analyzes complex, huge datasets faster than humanly possible.\n" +
                        "- **24/7 Availability:** Provides constant user assistance and customer service.";
            case ROOT:
            default:
                return "Artificial Intelligence simulates human intelligence processes in computing.\n\n" +
                        "###  Key Domains:\n" +
                        "- **NLP:** Language translation, sentiment analysis.\n" +
                        "- **Computer Vision:** Image recognition, optical character reading.\n" +
                        "- **Robotics:** Automated assembly, navigation systems.\n\n" +
                        "###  Core Advantages:\n" +
                        "- **24x7 Coverage:** Continuous operation capability.\n" +
                        "- **Insight generation:** Predicts trends and clusters customers automatically.";
        }
    }

    // --- ML SUB-INTENTS ---
    private String handleMl(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Machine Learning (ML) is a subfield of AI focused on building systems that learn patterns from training data to make decisions without being explicitly programmed.";
            case EXAMPLE:
                return "Here is a conceptual flow of an ML prediction pipeline:\n\n" +
                        "Raw Data -> Data Cleaning -> Model Training -> Parameters Optimization -> Production Inference";
            case ADVANTAGE:
                return "Key advantages of Machine Learning:\n" +
                        "- **Self-Optimization:** Model parameters improve automatically as they consume more data.\n" +
                        "- **Personalization:** Recommends content tailored to individual user behaviors.\n" +
                        "- **Pattern Detection:** Uncovers non-obvious correlations in high-dimensional datasets.";
            case ROOT:
            default:
                return "Machine Learning is an AI subset where algorithms learn rules directly from data.\n\n" +
                        "###  ML Classification:\n" +
                        "- **Supervised:** Labeled data training (regression, classification).\n" +
                        "- **Unsupervised:** Unlabeled clustering (K-Means, association).\n" +
                        "- **Reinforcement:** Trial and error rewards (game bots, path planning).\n\n" +
                        "###  Core Advantages:\n" +
                        "- **Adaptive:** Adjusts classifications automatically over time.\n" +
                        "- **Scalable logic:** Replaces complex nested rule files with a single model file.";
        }
    }

    // --- DATA SCIENCE SUB-INTENTS ---
    private String handleDataScience(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Data Science is the field of study that combines domain expertise, programming skills, and knowledge of mathematics and statistics to extract meaningful insights from data.";
            case EXAMPLE:
                return "A standard data science workflow involves:\n" +
                        "1. Querying datasets using SQL\n" +
                        "2. Cleaning missing values using pandas (Python)\n" +
                        "3. Plotting graphs using matplotlib\n" +
                        "4. Building ML models using scikit-learn";
            case ADVANTAGE:
                return "Key advantages of Data Science:\n" +
                        "- **Evidence-Based Decisions:** Replaces guesses with data-supported business metrics.\n" +
                        "- **Predictive Analytics:** Identifies potential future risks and customer churn rates.\n" +
                        "- **Market Targeting:** Groups target audiences based on behavioral features.";
            case ROOT:
            default:
                return "Data Science extracts structural insight and correlations from raw metrics.\n\n" +
                        "###  Essential Stack:\n" +
                        "- **Python:** Pandas, Numpy, Scikit-learn.\n" +
                        "- **SQL:** Querying large database tables.\n" +
                        "- **BI:** Tableau, PowerBI for interactive reports.\n\n" +
                        "###  Core Advantages:\n" +
                        "- **Risk Mitigation:** Predicts system anomalies and security failures.\n" +
                        "- **Value Optimization:** Identifies and cuts cost leakages in supply lines.";
        }
    }

    // --- WEB DEV SUB-INTENTS ---
    private String handleWebDev(SubIntent sub) {
        switch (sub) {
            case DEFINITION:
                return "Web Development is the work involved in developing a website for the Internet. It spans frontend layouts, backend logic, and database schemas.";
            case EXAMPLE:
                return "Here is a standard web stack structure:\n\n" +
                        "- **Frontend:** HTML, CSS, JavaScript (React)\n" +
                        "- **Backend:** Node.js, Python (Django), Java (Spring Boot)\n" +
                        "- **Database:** SQL (PostgreSQL), NoSQL (MongoDB)";
            case ADVANTAGE:
                return "Key advantages of Web Development:\n" +
                        "- **Global Reach:** Makes your software accessible from any device globally via a browser.\n" +
                        "- **No App Store Barriers:** Updates are deployed instantly to all users without store review processes.\n" +
                        "- **High Integration:** Connects smoothly with external web APIs, OAuth platforms, and payment portals.";
            case ROOT:
            default:
                return "Web Development covers creating and maintaining online web applications.\n\n" +
                        "###  Core Divisions:\n" +
                        "- **Frontend:** Client-side interface rendering (HTML, CSS, React).\n" +
                        "- **Backend:** Server-side logic, controllers, REST APIs.\n" +
                        "- **Database:** Persistent storage (PostgreSQL, MongoDB).\n\n" +
                        "###  Core Advantages:\n" +
                        "- **Instant Updates:** Deploy fixes directly to your servers.\n" +
                        "- **Access flexibility:** Users only need an active browser to run your software.";
        }
    }

    private String handleFollowUp(String normalized, ConversationMemory memory) {
        String pending = memory.getPendingQuestion();
        if ("python_goal".equals(pending)) {
            if (normalized.contains("ai") || normalized.contains("machine learning")) {
                return "Excellent choice. Python is the most popular language for AI and Machine Learning. Start with NumPy, pandas, and scikit-learn, then move to deep learning.";
            }
            if (normalized.contains("web")) {
                return "Great path. For web development, start with Flask or Django and build REST APIs plus a small full-stack app.";
            }
            if (normalized.contains("automation")) {
                return "Nice! Python automation is very practical. Learn scripting, file handling, APIs, and tools like Selenium for workflow automation.";
            }
        }
        if ("resume_follow_up".equals(pending)) {
            return "If you want, I can suggest a resume template section-by-section: headline, skills, projects, education, and achievements.";
        }

        String topic = memory.getLastTopic();
        if ("c programming".equals(topic)) {
            return "To master C quickly, practice pointer problems, dynamic memory, structures, and mini projects like file parser or mini shell.";
        }
        if ("java".equals(topic)) {
            return "If you're diving deeper into Java, next learn OOP thoroughly, collections, exception handling, multithreading, and a framework like Spring Boot.";
        }
        if ("ai".equals(topic) || "ml".equals(topic)) {
            return "A strong AI roadmap is: Python basics -> math foundations -> ML algorithms -> deep learning -> projects with deployment.";
        }
        if ("web development".equals(topic)) {
            return "For web development interviews, be ready with one full-stack project, API design decisions, authentication flow, and deployment steps.";
        }
        if ("career".equals(topic) || "interview".equals(topic)) {
            return "I can help you with a personalized 30-day placement plan if you share your current year and skill level.";
        }

        // Custom follow-ups for the new interview topics
        if ("html".equals(topic)) {
            return "To practice HTML, try building a personal portfolio or a landing page structure. Focus on semantic tags like <header>, <section>, and <footer>.";
        }
        if ("css".equals(topic)) {
            return "For CSS mastery, practice building responsive layouts using Flexbox and Grid. Try converting a Figma design into a real webpage.";
        }
        if ("javascript".equals(topic)) {
            return "To learn JavaScript, practice DOM manipulation, event listeners, promises, and fetching data from public APIs.";
        }
        if ("react".equals(topic)) {
            return "In React, focus on state management (useState, useEffect), passing props, custom hooks, and building reusable UI components.";
        }
        if ("sql".equals(topic)) {
            return "For SQL practice, write queries with JOINs, GROUP BY, subqueries, and aggregates. Try building database schemas on platforms like LeetCode or HackerRank.";
        }
        if ("dbms".equals(topic)) {
            return "Deep dive into ACID properties, normalization (1NF, 2NF, 3NF), indexing for performance, and the differences between SQL and NoSQL.";
        }
        if ("oop".equals(topic)) {
            return "Try implementing OOP principles in Java: create an abstract base class, inherit from it, override methods for polymorphism, and use private fields for encapsulation.";
        }
        if ("dsa".equals(topic)) {
            return "To master DSA, practice coding problems on LeetCode/GeeksforGeeks. Start with Arrays and Strings, then move to Stacks, Queues, Linked Lists, Trees, and Graphs.";
        }
        if ("os".equals(topic)) {
            return "Prepare for OS interviews by studying Banker's algorithm for deadlock avoidance, CPU scheduling policies (Round Robin, FCFS), and page replacement algorithms.";
        }
        if ("computer networks".equals(topic)) {
            return "For networking, understand the functions of each OSI layer, the TCP three-way handshake, subnetting, and the difference between TCP and UDP.";
        }

        return "Good follow-up. Tell me a bit more, and I will tailor my answer to your goal.";
    }

    private String generalResponse(String userInput) {
        if (userInput != null && userInput.toLowerCase().contains("thank")) {
            return "You're welcome! I'm here whenever you need guidance.";
        }
        return "I understand your question. Could you share a little more context so I can give you a more precise answer?";
    }

    private String pick(String[] responses) {
        return responses[random.nextInt(responses.length)];
    }
}
