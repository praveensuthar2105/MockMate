import sys

def patch_pom(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Need to make sure it is actually included inside <dependencies>
    # It might have been appended at the wrong place if there were multiple <dependencies> blocks.

    # We will just replace it correctly.
    if 'com.h2database' not in content:
        print("H2 not in pom.xml, appending")
    else:
        print("H2 already in pom.xml, verify tests pass")

    # Check if the error was because H2 version was missing? Spring Boot parent manages H2.
    # Let's check `mvn clean test` output again carefully.

if __name__ == '__main__':
    patch_pom('mockmate-backend/pom.xml')
