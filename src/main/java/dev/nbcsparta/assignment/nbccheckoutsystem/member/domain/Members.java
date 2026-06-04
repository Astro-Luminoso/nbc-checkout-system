package dev.nbcsparta.assignment.nbccheckoutsystem.member.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.InvalidPointUseException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Members {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedDate;

    private int pointBalance;


    public Members (String email, String password, String name, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void deductPointBalance(int usedPoint) {
        if (usedPoint < 0 || usedPoint > pointBalance) {
            throw new InvalidPointUseException();
        }

        this.pointBalance -= usedPoint;
    }

    public void addPointBalance(int paidAmount) {
        int earnPoint = (int) (paidAmount * 0.01);

        if (earnPoint < 0) {
            throw new InvalidPointUseException();
        }

        this.pointBalance += earnPoint;
    }
}
