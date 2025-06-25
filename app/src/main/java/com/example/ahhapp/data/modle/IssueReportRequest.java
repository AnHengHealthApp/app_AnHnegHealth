package com.example.ahhapp.data.modle;

// 回報問題請求物件
public class IssueReportRequest {
    private String issue_description;

    public IssueReportRequest(String issue_description) {
        this.issue_description = issue_description;
    }

    public String getIssue_description() {
        return issue_description;
    }

    public void setIssue_description(String issue_description) {
        this.issue_description = issue_description;
    }
}
