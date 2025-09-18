# Test files for the file route

This folder contains the test files. The Camel route "MyFileRouter" and the unit test "MyFileRouterTest" use these files as follows:

- At the start of the test, all regular files from this folder (excluding subfolders) are copied to `files/input/`.
- The Camel flow is responsible for copying the files from `files/input/` to `files/output/`.
- The test verifies that all expected files are processed (including via a Camel mock) and that, in the end, there are 8 files in the `files/output/` folder.

Notes:
- The folders `files/input/` and `files/output/` are cleared before each test run.
- To include additional test files, simply place them in this folder; they will be picked up automatically.

## Manual testing

To test the functionality manually:
- Copy the desired files from this folder into `files/input/`.
- Start the Spring Boot application.
    - For example: `mvn spring-boot:run`, or run the `main` method of the application.
- The route will process files from `files/input/` and copy them to `files/output/`. Verify the results in the `files/output/` folder and in the application logs.
