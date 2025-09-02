# Data Guard - Android Data Usage Monitor

Data Guard is a modern Android application designed to help you monitor your internet data usage effectively. With a clean, user-friendly interface built on the latest Material Design principles, it provides detailed insights into your data consumption and helps you stay within your limits with smart alerts.

## Features

- **Usage Monitoring:** Track your data usage for mobile and Wi-Fi networks daily, weekly, and monthly.
- **Visual Charts:** Interactive charts provide a clear visual representation of your data consumption over time.
- **Smart Alerts:** Set a custom data limit and receive an email alert to a pre-configured address when you exceed it.
- **Secure:** The app is protected by a password, and a separate password is required to modify critical settings, ensuring your configuration is safe.
- **Privacy-Focused:** Data Guard requires minimal permissions to function and clearly explains why each permission is needed.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- **Android Studio:** Ensure you have the latest version of Android Studio installed. You can download it from the [official website](https.developer.android.com/studio).
- **Android SDK:** You will need Android SDK Platform 34 (or newer). This can be installed via the SDK Manager in Android Studio.
- **A Google Account:** For the email alert feature, you'll need a Gmail account. It is **highly recommended** to use an **App Password** for security. See [Google's documentation](https://support.google.com/accounts/answer/185833) on how to create one.

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/dataguard.git
    cd dataguard
    ```

2.  **Open in Android Studio:**
    - Launch Android Studio.
    - Select "Open an Existing Project".
    - Navigate to the `dataguard` directory you just cloned and open it.

3.  **Sync Gradle:**
    - Android Studio will automatically start building the project. This might take a few minutes as it downloads the required dependencies.

4.  **Run the App:**
    - Connect an Android device or start an Android Emulator.
    - Click the "Run" button (green play icon) in Android Studio.

### First-Time Setup

On the first launch, the app will guide you through a mandatory setup process:

1.  **App Password:** You will be prompted to set a password for entering the app.
2.  **Settings Password:** You will set a *different* password that will be required to change settings like the alert email or data limit.
3.  **Configure Alerts:**
    - Enter the Gmail address you want to send alerts *from*.
    - Enter the Google App Password for that account (recommended for security).
    - Enter the destination email address where alerts will be sent.
    - Set your monthly data limit in Gigabytes (GB).

### Required Permissions

The app needs one special permission to monitor data usage: **Usage Access**.

- **Why is it needed?** The `Usage Access` permission allows Data Guard to read network traffic stats collected by the Android system. Without this, the app cannot measure your data consumption.

- **How to grant it?** The app will detect if the permission is missing and will automatically redirect you to the system settings page. On that page, find "Data Guard" in the list and toggle the switch to grant access.

---

## Continuous Integration with GitHub Actions

This project includes a pre-configured GitHub Actions workflow to automatically build and test the application.

### How it Works

The workflow is defined in the `.github/workflows/ci.yml` file. It triggers on every `push` to the `main` branch or any `pull_request`.

Here's a breakdown of the steps:

1.  **Checkout Code:** The workflow starts by checking out the latest version of your code from the repository.
2.  **Set up JDK:** It installs the Java Development Kit (JDK), which is required to build Android apps.
3.  **Set up Android SDK:** It downloads and configures the necessary Android SDK components.
4.  **Grant Gradle Permissions:** It makes the Gradle wrapper (`gradlew`) executable. This is a common requirement on CI/CD platforms.
5.  **Build the App:** It runs the `./gradlew build` command, which compiles the code and runs any unit tests.
6.  **Build APK:** It runs `./gradlew assembleDebug` to generate a debug `.apk` file, which you can install on a device.
7.  **Upload Artifact:** The generated `.apk` is uploaded as a build artifact, which you can download directly from the GitHub Actions summary page.

### How to Get the APK

After the workflow successfully completes, go to the "Actions" tab in your GitHub repository, click on the latest run, and you will find the `app-debug.apk` available for download under the "Artifacts" section.
