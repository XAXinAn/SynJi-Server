package org.example.synjiserver.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Long userId;

    @Column(nullable = false)
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "time", nullable = false)
    private LocalTime time;

    @Column(name = "is_all_day", nullable = false)
    @JsonProperty("isAllDay")
    private boolean isAllDay;

    private String location;

    @Column(nullable = false)
    @JsonAlias({"groupName", "belongingName", "category"})
    private String belonging; // 所属分类，如"工作"、"生活"

    @Column(name = "is_important", nullable = false)
    @JsonProperty("important")
    private boolean isImportant;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    @JsonAlias("text")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String ocrText;

    @Column(name = "is_ai_generated", nullable = false)
    @JsonProperty("isAiGenerated")
    private boolean isAiGenerated;

    @Column(name = "is_viewed", nullable = false)
    @JsonProperty("isViewed")
    private boolean isViewed;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public boolean isAllDay() { return isAllDay; }
    public void setAllDay(boolean allDay) { isAllDay = allDay; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBelonging() { return belonging; }
    public void setBelonging(String belonging) { this.belonging = belonging; }

    public boolean isImportant() { return isImportant; }
    public void setImportant(boolean important) { isImportant = important; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public boolean isAiGenerated() { return isAiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { isAiGenerated = aiGenerated; }

    public boolean isViewed() { return isViewed; }
    public void setViewed(boolean viewed) { isViewed = viewed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
