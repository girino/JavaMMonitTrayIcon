# JavaMMonitTrayIcon

Tray icon app that polls a **JSON HTTP API**, evaluates **[jq](https://stedolan.github.io/jq/tutorial/)-style** rules, and shows a **colored status dot** in the system tray. It was built for **[M/Monit](https://mmonit.com/)**, but you can point it at any API and define your own rules and colors.

You need a **desktop environment with a system tray** (Windows, macOS, or most Linux desktop sessions). **Java 17 or newer** is required to run the application.

---

## Table of contents

- [Download and run (GitHub Releases)](#download-and-run-github-releases)
- [Using the application](#using-the-application)
- [Configuration](#configuration)
- [Configuration file location](#configuration-file-location)
- [Building from source (developers)](#building-from-source-developers)
- [Tests and continuous integration](#tests-and-continuous-integration)
- [Troubleshooting](#troubleshooting)
- [Licensing](#licensing)
- [Disclaimer](#disclaimer)

---

## Download and run (GitHub Releases)

### 1. Download the JAR

1. Open the project’s **Releases** page:  
   **[https://github.com/girino/JavaMMonitTrayIcon/releases](https://github.com/girino/JavaMMonitTrayIcon/releases)**
2. Choose a release:
   - **Latest** stable release is recommended for normal use.
   - Releases tagged with suffixes such as **`-rc1`**, **`-beta`**, or **`-SNAPSHOT`** are marked as **pre-releases** on GitHub; use them if you want to test upcoming builds.
3. Under **Assets**, download the fat JAR whose name looks like:

   `mmonit-tray-<version>-jar-with-dependencies.jar`

   (There may be other files; you only need this **`-jar-with-dependencies`** artifact to run the app.)

4. Save it anywhere you like (for example `Downloads` or a folder of your choice).

### 2. Install Java 17+

The JAR does not bundle a JVM. Install a **Java 17 or newer** runtime (JRE or JDK), for example:

- **[Eclipse Temurin](https://adoptium.net/)** (Windows, macOS, Linux)
- **macOS**: `brew install openjdk@17` (or a newer LTS)
- **Ubuntu / Debian**: `sudo apt install openjdk-17-jre` (or `openjdk-17-jdk`)

Check in a terminal:

```bash
java -version
```

You should see version **17** or higher.

### 3. Run the application

**From a terminal** (replace the file name with the one you downloaded):

```bash
cd /path/to/folder/containing/the/jar
java -jar mmonit-tray-1.1.0-jar-with-dependencies.jar
```

**From a file manager**: on many systems you can **double-click** the JAR if `.jar` files are associated with Java (this varies by OS).

On **first run**, if no configuration file exists yet, the **settings** window opens automatically so you can enter your server URL, authentication, and rules (see [Configuration](#configuration)).

---

## Using the application

### Tray icon

- The icon is a **filled circle**. Its **color** comes from your [rules](#rules): the **first rule** (top to bottom) whose jq expression evaluates to an **integer greater than zero** wins; that rule’s color is shown.
- If **no rule** is positive, the icon stays **blue** (default “OK / nothing matched”).
- If a **poll fails** (network error, bad response, etc.), the icon turns **black**.

### Tray menu

Right-click the tray icon (or the platform equivalent):

| Item      | Action |
| --------- | ------ |
| **About** | License / copyright notice |
| **Settings** | Open the configuration window |
| **Exit**  | Quit the app (attempts logout for form-based sessions) |

### Default click action

**Left-click** (or primary click) the tray icon: if your OS supports it, the app tries to open your configured **server URL** in the default web browser.

---

## Configuration

When you run the app for the first time (or open **Settings**), configure at least:

1. **Server URL** (base URL of the service).
2. **Authentication** and any **paths** required for your setup.
3. **Rules** (color + jq expression).

### Authentication

- **Basic**: put credentials in the URL:

  `https://username:password@host.example.com:8443`

- **Form**: inspect the HTML login form for field names (username, password, CSRF tokens, etc.). You can add as many **form fields** as needed. The sample config is oriented toward M/Monit defaults.

### Pages (form authentication only)

If **Auth. type** is **Form**, these paths must be set (defaults in the sample config target M/Monit):

| Page   | Purpose |
| ------ | ------- |
| **Init** | Page that serves the login form (cookies/session often start here). |
| **Login** | URL the form **posts** to (HTML `form` **action**). |
| **Logout** | Logout URL (used when you exit the app). |
| **API** | URL that returns the **JSON** your rules evaluate. |

For **Basic** auth, only the **API** path is required in practice for polling.

### Rules

Rules are the heart of the app. Each rule has:

- A **color** (named AWT color or hex, e.g. `RED`, `#FF0000`).
- A **[jq 1.6](https://stedolan.github.io/jq/tutorial/)** expression that must evaluate to a **number**. The expression is applied to the JSON from the API.

Rules run **in order**. The **first** expression whose result is **`> 0`** sets the tray color. If the API returns booleans or strings, convert them to integers in jq (see example below).

**Example JSON:**

```json
{
  "red": "enabled",
  "green": "enabled"
}
```

**Example rules:**

| Color | Rule (jq) |
| ----- | --------- |
| RED   | `.red == "enabled" \| if . then 1 else 0 end` |
| GREEN | `.green == "enabled" \| if . then 1 else 0 end` |

The icon is **RED**, because the RED rule is listed first and both match.

---

## Configuration file location

After you save settings, the app stores a **`config.properties`** file under the OS-specific user config directory:

| OS      | Typical path |
| ------- | -------------- |
| **Windows** | `%LOCALAPPDATA%\JavaMMonitTrayIcon\` |
| **macOS**   | `~/Library/Application Support/JavaMMonitTrayIcon/` |
| **Linux**   | `~/.config/JavaMMonitTrayIcon/` |

You may edit this file by hand; note that Java’s `Properties` API does not preserve key order when saving from the GUI, so diffs can look noisy.

---

## Building from source (developers)

### Prerequisites

- **JDK 17** or newer (not only JRE).
- **[Apache Maven](https://maven.apache.org/)** 3.6+.
- **`JAVA_HOME`** must point at that JDK so **`mvn -version`** reports Java **17+** (not only `java -version` in the shell).

### Clone and build

```bash
git clone https://github.com/girino/JavaMMonitTrayIcon.git
cd JavaMMonitTrayIcon
mvn package
```

Outputs:

- **`target/mmonit-tray-<version>.jar`** — normal JAR (dependencies not included).
- **`target/mmonit-tray-<version>-jar-with-dependencies.jar`** — **fat JAR**; same artifact as Releases, runnable with `java -jar ...`.

### Useful commands

| Command        | Purpose |
| -------------- | ------- |
| `mvn test`     | Run unit tests |
| `mvn verify`   | Tests + package (used in release automation) |
| `mvn clean package` | Clean `target/` then build |

### WSL and Windows-mounted drives

If the repo lives under **`/mnt/c/...`**, **`/mnt/f/...`**, or similar (Windows filesystem from WSL), `mvn clean` can fail when a **JAR is locked** (e.g. the app still running, or another process holding the file). Stop the app, or delete `target/` from Windows after closing handles, or run **`mvn package`** without **`clean`**. The POM configures a recent **`maven-clean-plugin`** with **`retryOnError`** to reduce flaky deletes.

---

## Tests and continuous integration

- **Unit / integration tests** live under `src/test/java`. Run them with **`mvn test`**.
- **GitHub Actions** (if enabled on the repo):
  - **CI** runs tests on pushes to branches and on pull requests.
  - **Release** workflows build and attach the fat JAR when a **version tag** is pushed (e.g. `v1.1.0` or pre-release tags like `v1.1.0-rc1`).

Maintainers: see `.github/workflows/` for exact triggers and JDK version.

---

## Troubleshooting

| Symptom | What to try |
| ------- | ----------- |
| **`release version 17 not supported`** (build) | Maven is using an old JDK. Run **`mvn -version`**, install **JDK 17+**, set **`JAVA_HOME`**, put **`$JAVA_HOME/bin`** early on **`PATH`**. |
| **Enforcer rule failed: requireJavaVersion** | Same as above: point Maven at JDK 17+. |
| **System tray not supported** | Run on a desktop session with a tray; many minimal/headless environments have no tray. |
| **Cannot delete JAR during `clean`** | Another process has the file open; quit the tray app or remove the lock (see [WSL and Windows-mounted drives](#wsl-and-windows-mounted-drives) above). |
| **Tray slow / frozen while site is down** | Recent versions poll off the UI thread and use HTTP timeouts; upgrade to the latest release. |

---

## Licensing

This software is licensed under **Girino’s Anarchist License**. Full text: **[https://girino.org/license/](https://girino.org/license/)**. Keep copyright notices; use and modify as you like. The author is not liable for damages arising from use of this software.

---

## Disclaimer

This software is shared **as is** for personal and community use. If you find bugs, reports and well-written patches are welcome.
