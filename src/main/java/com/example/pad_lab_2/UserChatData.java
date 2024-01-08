package com.example.pad_lab_2;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Setter
@XmlRootElement
@Getter
class UserChatData {
    private String clientName;
    private String ipAddress;
    private int port;
    private List<String> messages = new ArrayList<>();

    public String getLastMessage() {
        if (!messages.isEmpty()) {
            return messages.get(messages.size() - 1);
        } else {
            return "No messages";
        }
    }
}
