package com.example.pad_lab_2;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@Setter
@XmlRootElement
class UserChatDataCollection {
    private List<UserChatData> userChatDataList;
}