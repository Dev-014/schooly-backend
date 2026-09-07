package com.school.erp.entity.hr;

import com.school.erp.entity.Staff;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff_bank_accounts")
public class StaffBankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false, unique = true)
    private Staff staff;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "branch_name")
    private String branchName;
}
