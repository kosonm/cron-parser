# Cron Expression Parser

This application parses a cron string and expands each field to show the times at which it will run.

## Usage

1. **Navigate to the project directory**:
    ```sh
    cd src/main/java
    ```

2. **Compile the Java files**:
    ```sh
    javac pl/CronParser.java
    ```

3. **Run the program**:
    ```sh
    java pl.CronParser "*/15 0 1,15 * 1-5 /usr/bin/find"
    ```