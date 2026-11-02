package com.smartbook.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "business_holidays",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_business_holiday_owner_date",
                        columnNames = {"business_owner_id", "holiday_date"}
                )
        }
)
public class BusinessHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_owner_id", nullable = false)
    private BusinessOwner businessOwner;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false)
    private String name;

    public BusinessHoliday() {
    }

    public Long getId() {
        return id;
    }

    public BusinessOwner getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(BusinessOwner businessOwner) {
        this.businessOwner = businessOwner;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}