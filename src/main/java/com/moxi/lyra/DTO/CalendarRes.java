package com.moxi.lyra.DTO;

import lombok.Getter;
import lombok.Setter;
import com.moxi.lyra.Calendar.Event.Event;

import java.util.List;

@Getter
@Setter
public class CalendarRes {
    private Long id;
    private String username;
    private List<Event> eventsList;
}