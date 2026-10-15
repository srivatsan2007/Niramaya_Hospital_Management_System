package com.hospital.model;

/**
 * Model representing a Visitor / Patient Contact Desk Inquiry.
 */
public class ContactQuery {
    private String queryId;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String status;
    private String createdAt;

    public ContactQuery() {}

    public ContactQuery(String queryId, String name, String email, String phone, String subject, String message, String status, String createdAt) {
        this.queryId = queryId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
        this.status = status != null ? status : "Open";
        this.createdAt = createdAt;
    }

    public String getQueryId() { return queryId; }
    public void setQueryId(String queryId) { this.queryId = queryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
