import java.util.Random;

public class AIEngine {
    private final IntentClassifier classifier;
    private final ConversationMemory memory;
    private final Random random;

    private static final String[] GREETINGS = {
            "Hello! I am Nova AI Assistant PRO. How can I support you today?",
            "Hi there! Ready to learn and build something amazing?",
            "Hey! Great to see you. What would you like to talk about?",
            "Good to connect with you. How can I help?"
    };

    public AIEngine() {
        this.classifier = new IntentClassifier();
        this.memory = new ConversationMemory();
        this.random = new Random();
    }

    public String respond(String userInput) {
        IntentClassifier.Intent intent = classifier.classify(userInput, memory);
        String normalized = classifier.normalize(userInput);
        String response;

        switch (intent) {
            case GREETING:
                response = pick(GREETINGS);
                memory.setPendingQuestion("");
                memory.rememberUserMessage(userInput, "general");
                break;
            case JAVA:
                response = "Java is a powerful object-oriented language used for enterprise systems, Android apps, desktop software, and backend services. It runs on the JVM, so code is portable across platforms.";
                memory.setPendingQuestion("java_follow_up");
                memory.rememberUserMessage(userInput, "java");
                break;
            case PYTHON:
                response = "That's great! Python is a beginner-friendly programming language. Are you learning it for AI, web development, or automation?";
                memory.setPendingQuestion("python_goal");
                memory.rememberUserMessage(userInput, "python");
                break;
            case C_PROGRAMMING:
                response = "C is a foundational language that helps you understand memory, pointers, and system-level programming. Start with syntax, functions, arrays, pointers, and file handling.";
                memory.setPendingQuestion("c_follow_up");
                memory.rememberUserMessage(userInput, "c programming");
                break;
            case AI:
                response = "Artificial Intelligence helps machines perform tasks that usually need human intelligence, like language understanding, vision, prediction, and decision support.";
                memory.setPendingQuestion("ai_follow_up");
                memory.rememberUserMessage(userInput, "ai");
                break;
            case ML:
                response = "Machine Learning is a branch of AI where models learn patterns from data to make predictions or decisions without explicit rule-by-rule programming.";
                memory.setPendingQuestion("ml_follow_up");
                memory.rememberUserMessage(userInput, "ml");
                break;
            case DATA_SCIENCE:
                response = "Data Science combines statistics, programming, and domain knowledge to extract insights from data. Python, SQL, visualization, and ML are key skills.";
                memory.setPendingQuestion("data_science_follow_up");
                memory.rememberUserMessage(userInput, "data science");
                break;
            case WEB_DEVELOPMENT:
                response = "Web Development includes frontend, backend, and databases. A practical roadmap is HTML/CSS/JavaScript -> React -> APIs -> databases -> deployment.";
                memory.setPendingQuestion("web_follow_up");
                memory.rememberUserMessage(userInput, "web development");
                break;
            case CAREER:
                response = "Build your career with a clear roadmap: strengthen fundamentals, create 3-5 portfolio projects, practice DSA, optimize your resume, and apply consistently.";
                memory.setPendingQuestion("career_follow_up");
                memory.rememberUserMessage(userInput, "career");
                break;
            case INTERVIEW:
                response = "For interviews, prepare core CS concepts, language fundamentals, and project explanations. Practice mock interviews and be ready to explain your trade-offs clearly.";
                memory.setPendingQuestion("interview_follow_up");
                memory.rememberUserMessage(userInput, "interview");
                break;
            case RESUME:
                response = "For a strong resume, keep it one page, highlight impact with numbers, include 2-4 strong projects, and tailor keywords to the role description.";
                memory.setPendingQuestion("resume_follow_up");
                memory.rememberUserMessage(userInput, "resume");
                break;
            case COLLEGE:
                response = "For college success, keep a weekly study plan, revise daily, maintain concise notes, and align semester learning with projects and internships.";
                memory.setPendingQuestion("college_follow_up");
                memory.rememberUserMessage(userInput, "college");
                break;
            case PROJECTS:
                response = "Try portfolio-ready projects like an AI chatbot, expense tracker, smart resume analyzer, or student placement predictor. Want ideas based on your current level?";
                memory.setPendingQuestion("projects_follow_up");
                memory.rememberUserMessage(userInput, "projects");
                break;
            case INTERNSHIP:
                response = "For internships, build real projects, maintain GitHub consistency, tailor your resume, and apply through LinkedIn, company portals, and referrals.";
                memory.setPendingQuestion("internship_follow_up");
                memory.rememberUserMessage(userInput, "internship");
                break;
            case MOTIVATION:
                response = "You are closer than you think. Focus on one small milestone today, then build momentum daily. Consistency beats intensity.";
                memory.setPendingQuestion("");
                memory.rememberUserMessage(userInput, "motivation");
                break;
            case FOLLOW_UP:
                response = handleFollowUp(normalized);
                memory.rememberUserMessage(userInput, memory.getLastTopic());
                break;
            case GENERAL:
            default:
                response = generalResponse(userInput);
                memory.setPendingQuestion("");
                memory.rememberUserMessage(userInput, "general");
                break;
        }
        return response;
    }

    private String handleFollowUp(String normalized) {
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
