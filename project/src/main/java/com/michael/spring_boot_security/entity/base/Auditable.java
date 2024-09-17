package com.michael.spring_boot_security.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.michael.spring_boot_security.exception.payload.ApiException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.AlternativeJdkIdGenerator;

import java.time.LocalDateTime;


@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updateAt"}, allowGetters = true)
public abstract class Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq", allocationSize =1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id;
    private String referenceId = new AlternativeJdkIdGenerator().generateId().toString();

    @NotNull
    private Long createdBy;
    @NotNull
    private Long updatedBy;
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @CreatedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

//TODO: fix
    @PrePersist
    public void beforePersist() {
        var userId =  0L;//RequestContext.getUserId();
//        if (userId == null) {
//            throw new ApiException("Cannot persist entity without user ID in RequestContext for this thread");
//        }
        setCreatedAt(LocalDateTime.now());
        setCreatedBy(userId);
        setUpdatedBy(userId);
        setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    public void beforeUpdate() {
        var userId = 0L;// RequestContext.getUserId();
//        if (userId == null) {
//            throw new ApiException("Cannot update entity without user ID in RequestContext for this thread");
//        }

        setUpdatedBy(userId);
        setUpdatedAt(LocalDateTime.now());
    }


}
