package com.moxi.lyra.Calendar;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.moxi.lyra.Calendar.Event.Event;
import com.moxi.lyra.User.User;

import java.util.List;


@Entity
@Getter
@Setter
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name="user_id" , insertable=true, updatable=true)
    private User user;
    @OneToMany(mappedBy = "calendar" , cascade = CascadeType.ALL , orphanRemoval = true)
    private List<Event> events;

    public Calendar() {

    }
}