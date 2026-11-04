# Test files for the file route

The `files/` directory (the one containing this README) holds the test files for `MyFileRouter`. The Camel route "MyFileRouter" and the unit test "MyFileRouterTest" use them as follows:

- At the start of the test, all regular files in `files/` (excluding subfolders) are copied to `files/input/`.
- The Camel flow is responsible for copying the files from `files/input/` to `files/output/`.
- The test verifies that all expected files are processed (including via a Camel mock) and that, in the end, the `files/output/` folder contains the same number of regular files as `files/`.

Notes:
- The folders `files/input/` and `files/output/` are cleared before each test run.
- To include additional test files, place them in `files/`; they will be picked up automatically.

## Manual testing

To test the functionality manually:
- Copy the desired test files from `files/` into `files/input/`. This README is documentation only and can be skipped.
- Start the Spring Boot application.
- For example: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`, or run the `main` method of the application.
- The route will process files from `files/input/` and copy them to `files/output/`. Verify the results in the `files/output/` folder and in the application logs.
