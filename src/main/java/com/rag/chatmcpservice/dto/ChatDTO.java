package com.rag.chatmcpservice.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ChatDTO {

    @ToolParam(description = "用户的发出的提问")
    private String msg;

    private Integer loadBook = 0;

    private String conversationId;

    @ToolParam(description = "用户传递的附件对象集合")
    private List<FilePart> files;

}