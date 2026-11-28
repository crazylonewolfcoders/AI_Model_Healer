package io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientsInfo {

    private List<ClientBean> clients = new ArrayList<>();
}
