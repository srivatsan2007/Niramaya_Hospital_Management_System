package com.hospital.model;

public class InventoryRequest {
    private String requestId;
    private String nurseId;
    private String nurseName;
    private String itemName;
    private int quantity;
    private String status; // Pending, Approved, Rejected
    private String requestDate;
    private String remarks;
    private String approvedBy;
    private String createdAt;

    public InventoryRequest() {}

    public InventoryRequest(String requestId, String nurseId, String nurseName, String itemName,
                            int quantity, String status, String requestDate, String remarks) {
        this.requestId = requestId;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.itemName = itemName;
        this.quantity = quantity;
        this.status = status;
        this.requestDate = requestDate;
        this.remarks = remarks;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
