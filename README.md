
-----

## Project Overview ✒️

The Lekhak AI is a Chrome extension with a Spring Boot backend designed to simplify your email writing process. The extension uses AI (Artificial Intelligence) to generate email content in a professional, concise, or desired tone based on your text or instructions. The goal of this project is to make email replies and compositions quick and efficient.

-----


## Tech Stack ⚒️

### Backend (Spring Boot)
- **Language:** **Java 21 ☕**
- **Framework:** **Spring Boot** - To build the REST API backend.
- **AI Integration:** **Google Gemini API** - For generating email content.
- **HTTP Client:** **WebClient** - A non-blocking client for communicating with the Gemini API.
- **Utilities:** **Lombok** - For reducing boilerplate code (getters, setters, constructors).

---

### Frontend (Chrome Extension)
- **Languages:** **HTML, CSS, JavaScript** - To build the UI and logic for the Chrome extension.
- **Web APIs:** `fetch` API - For sending and receiving data from the backend.
- **Browser APIs:** **MutationObserver** - For detecting changes in Gmail's UI to inject the button.

## Key Features

* **AI-Powered Email Generation:** Creates email content based on user instructions.
* **Dynamic Prompting:** Instead of fixed translations, the app follows user instructions to perform various tasks like summarizing or changing the tone.
* **Backend-Driven:** All heavy processing occurs on the backend server, keeping the extension light and fast.
* **Seamless Integration:** Injects a button and an input box directly into the Gmail compose window.
* **CORS Configuration:** Uses Cross-Origin Resource Sharing (CORS) to ensure secure communication between the backend and the frontend.

-----

## Installation & Setup

### 1\. Backend Setup

1.  **Clone the Repository:**

    ```bash
    git clone https://github.com/Priyadarshan-Garg/email-writer-backend.git
    cd email-writer-backend
    ```

2.  **Install Dependencies:**
    Open the project in your IDE (like IntelliJ IDEA). Maven will automatically download the required dependencies.

3.  **Set Environment Variables:**
    Create a `.env` file in your project's root folder. In this file, add your Gemini API keys.

    ```env
    gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent
    gemini.api.key=YOUR_GEMINI_API_KEY
    ```

    Alternatively, you can set these variables directly in your IDE's **Run/Debug Configurations**.

4.  **Run the Backend:**

    ```bash
    ./mvnw spring-boot:run
    ```

    Your backend will now be running at `http://localhost:8080`.

### 2\. Frontend Setup (Chrome Extension)

1.  **Clone the Repository:**

    ```bash
    git clone https://github.com/Priyadarshan-Garg/email-writer-frontend
    ```

2.  **Load the Extension:**

    * Open Chrome and go to `chrome://extensions`.
    * Turn on **Developer mode**.
    * Click the **"Load unpacked"** button and select the `email-writer-extension` folder.

-----

## Usage

1.  Open Gmail and either compose a new email or reply to an existing one.
2.  An **"AI Reply"** button and an instruction box will appear.
3.  **For a New Email:** Write your instructions in the input box, for example, "Write a professional email."
4.  **For a Reply:** The extension will use the content of the email you are replying to. You can also add your own instructions.
5.  Click the **"AI Reply"** button. The AI-generated content will appear in the compose box.

-----

## Contributing

If you would like to contribute to this project, please open issues or submit pull requests.

-----

## License

This project is licensed under the MIT License.