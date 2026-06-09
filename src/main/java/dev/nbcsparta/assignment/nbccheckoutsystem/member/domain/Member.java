package dev.nbcsparta.assignment.nbccheckoutsystem.member.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.BaseEntity;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.InvalidPointUseException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Member extends BaseEntity {

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

    private int pointBalance;


    public Member(String email, String password, String name, String phoneNumber) {
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

    public void addPointBalance(int usedPoint) {
        if (usedPoint < 0) {
            throw new InvalidPointUseException();
        }

        this.pointBalance += usedPoint;
    }

    // 환불 시 사용 포인트 복구
    public void restoreUsedPoint(int usedPoint) {
        if (usedPoint < 0) {
            throw new InvalidPointUseException();
        }

        this.pointBalance += usedPoint;
    }

    // 환불 시 결제 적립 포인트 회수
    public void cancelEarnedPoint(int earnedPoint) {
        if (earnedPoint < 0 || earnedPoint > pointBalance) {
            throw new InvalidPointUseException();
        }

        this.pointBalance -= earnedPoint;
    }
}
