package io.github.crazy.lonewolf.coders.omnij.datasource.service;

import io.github.crazy.lonewolf.coders.omnij.datasource.annotation.OmniJTransactional;
import io.github.crazy.lonewolf.coders.omnij.datasource.repository.ClientRepository;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.ClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.entities.OmnijClientEntity;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.ClientBean;
import io.github.crazy.lonewolf.coders.omnij.plugin.util.ApiKeyUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@OmniJTransactional // Automatically uses omnijTransactionManager for all methods in this class!
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<OmnijClientEntity> saveOrUpdateClientList(List<ClientBean> clients) {

            List<OmnijClientEntity> clientEntities = new ArrayList<>();
        clientEntities =  clients.stream().map(i ->{
                    OmnijClientEntity clientEntity = new OmnijClientEntity();
                if(i.getId() != null){
                    clientEntity.setId(UUID.fromString(i.getId()));
                }

                clientEntity.setClientKey(i.getClientKey());
                clientEntity.setClientName(i.getClientName());

                return clientEntity;
            }
            ).toList();


        return clientRepository.saveAll(clientEntities);
    }


    public OmnijClientEntity getClientByClientKey(String clientKey, String clientId){

            clientKey = ApiKeyUtils.hashApiKey(clientKey);
          return  clientRepository.findByClientidAndClientKeyAndActiveTrue(clientId,clientKey).orElse(new OmnijClientEntity());

    }

}
