# ClassConnect - Smart Attendance & Chat System

**ClassConnect** is a Java-based desktop application designed for classroom management. 
It simplifies attendance tracking, announcement sharing, and provides a simple chatbot for student interaction.

## Features

- **Dual Login System:** Teacher and Student login.
- **Teacher Dashboard:** Add announcements and mark attendance for students.
- **Student Portal:** View attendance percentage, check announcements, and chat with a simple bot.
- **Chatbot:** Responds to attendance and announcement-related queries.
- **File-Based Storage:** Data stored in `.txt` and `.csv` files for simplicity.
- **GUI Implementation:** Built using Java Swing for an intuitive interface.

## Technologies Used

- Java (Core)
- Java Swing (GUI)
- File I/O (TXT/CSV)
- ActionListeners for events

## Project Structure

- `LoginPage.java` - Handles user login for teacher and student roles.
- `StudentPortal.java` - Student dashboard with attendance view and chatbot.
- `StudentChatbot.java` - Chatbot logic for student queries.
- `AttendanceManager.java` - Teacher dashboard for marking attendance and announcements.
- `DataStore.java` - File operations for attendance and announcements.

## Learning Outcomes

This project demonstrates the practical application of the following Java syllabus concepts:

- Classes, Inheritance, Constructors, and Access Modifiers
- Method Overloading & Overriding, Dynamic Method Dispatch
- Interfaces, Exception Handling, and String operations
- File I/O (TXT/CSV), Generics, and basic Multithreading concepts
- Packages, Collections, and Lambda Expressions

## Safety Note

- This project uses **dummy credentials** and does not contain real personal data.
- Only test data should be used when exploring the project.


## Future Scope

- Integration with databases (MySQL/Firebase) for live data.
- Advanced AI chatbot for natural language queries.
- Real-time attendance graphs and analytics.
- Android or web version for wider accessibility.

