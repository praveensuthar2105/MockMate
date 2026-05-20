with open("mockmate-backend/src/test/java/com/mockmate/service/CodeExecutionServiceTest.java", "r") as f:
    content = f.read()

# Replace expected output from "[0,1]" to "[0, 1]" because Python outputs "[0, 1]" list conversion with spaces.
content = content.replace('testCases = List.of(createTestCase("[2,7,11,15] 9", "[0,1]"));', 'testCases = List.of(createTestCase("[2,7,11,15] 9", "[0, 1]"));')

with open("mockmate-backend/src/test/java/com/mockmate/service/CodeExecutionServiceTest.java", "w") as f:
    f.write(content)
