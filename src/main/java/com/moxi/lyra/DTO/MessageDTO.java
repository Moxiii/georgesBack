package com.moxi.lyra.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {
    private String sender;
    private String receiver;
    private String content;
}
