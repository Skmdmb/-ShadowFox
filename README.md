📝 To-Do List App (Java | Android Studio)
📌 Overview

The To-Do List App is a simple, offline Android application developed in Java using Android Studio.
It allows users to add, view, mark complete, delete, and sort tasks by priority — all stored locally using SharedPreferences, so the data persists even after app restarts.

🚀 Features

✅ Add new tasks with a chosen priority (High, Medium, Low).
✅ Mark tasks as completed (with strike-through effect).
✅ Delete tasks instantly.
✅ Sort by priority (High → Low).
✅ Offline storage using SharedPreferences (no internet required).
✅ Simple, clean, and responsive UI using RecyclerView.

🧠 Tech Stack
Component	Description
Language	Java
IDE	Android Studio
UI Components	RecyclerView, AlertDialog, Spinner, Buttons
Storage	SharedPreferences (JSON via Gson)
Library Used	Gson (for serialization & deserialization)
🗂 Project Structure
ToDoListApp/
├── java/com/example/todolist/
│   ├── MainActivity.java      → Main screen (Add, Sort, and Display tasks)
│   ├── Task.java              → Model class for Task object
│   ├── TaskAdapter.java       → RecyclerView Adapter for displaying tasks
└── res/layout/
    ├── activity_main.xml      → Main screen layout (with title, buttons, list)
    ├── item_task.xml          → Layout for individual task item
    └── dialog_add_task.xml    → Layout for "Add Task" popup dialog

⚙️ How It Works

Tap Add Task → Enter task name + choose priority.

The task appears in the list.

Tap the checkbox ✅ to mark as complete.

Tap 🗑️ delete icon to remove a task.

Tap Sort by Priority to reorder tasks (High → Medium → Low).

All tasks are saved locally (no internet needed).

🧩 Key Code Concepts

RecyclerView for dynamic list display.

SharedPreferences + Gson for local persistence.

AlertDialog for adding tasks interactively.

Custom Adapter for list item binding and event handling.

💡 Future Enhancements

🔹 Add Room Database for scalable storage.
🔹 Implement date & time reminders.
🔹 Add swipe-to-delete gestures.
🔹 Include task edit feature.
🔹 Add notification alerts for upcoming tasks.

👨‍💻 Developer
Shaik Mohammed Baharmoos
🎓 B.E. Information Technology – Muffakham Jah College of Engineering & Technology
📍 Hyderabad, India.
