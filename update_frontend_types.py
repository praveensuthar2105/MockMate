import sys

def update_file(file_path):
    with open(file_path, "r") as f:
        content = f.read()

    # In sessionMapper.ts:
    if "sessionMapper.ts" in file_path:
        # Add reportJson and dsaProblemJson to BackendInterviewResponse
        if "reportJson?: string;" not in content:
            content = content.replace(
                "totalScore: number | null;",
                "totalScore: number | null;\n    reportJson?: string;\n    dsaProblemJson?: string;"
            )

        # Map them into InterviewSession
        if "reportJson: response.reportJson," not in content:
            content = content.replace(
                "endedAt: null,",
                "endedAt: null,\n        reportJson: response.reportJson,\n        dsaProblemJson: response.dsaProblemJson,"
            )

    with open(file_path, "w") as f:
        f.write(content)

update_file("mockmate-frontend/src/services/sessionMapper.ts")
