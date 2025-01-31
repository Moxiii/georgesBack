package com.moxi.lyra.Calendar.Event;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.moxi.lyra.Calendar.Event.Recurrence.RecurrenceRule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.moxi.lyra.Calendar.Calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.Set;

@Getter
@Setter
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startHours;
    private LocalTime endHours;
    private boolean completed = false;
    @ElementCollection
    private Set<String> tags;
    @Enumerated(EnumType.STRING)
    private RecurrenceRule recurrenceRule;
    private Integer recurrenceInterval;
    private String recurrenceUnit;
    private LocalDate recurrenceEndDate;
    private Period recurrenceDuration;
	private boolean forcePersonalTag;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "calendar_id")
    private Calendar calendar;

}
