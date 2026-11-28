package io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientBean {

    private String id;

    private String clientName;

    private String clientKey;

    private boolean active = true;

    private List<Template>  templates;
}

