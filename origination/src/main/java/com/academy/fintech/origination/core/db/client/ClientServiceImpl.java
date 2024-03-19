package com.academy.fintech.origination.core.db.client;

import com.academy.fintech.application.ClientData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository repository;

    @Override
    public ClientEntity getOrCreate(ClientData clientData) throws IllegalArgumentException {
        ClientEntity clientEntity = repository.findByEmail(clientData.getEmail())
                .orElseGet(() -> repository.save(ClientEntity.from(clientData)));
        if (!validate(clientEntity, clientData)) {
            throw new IllegalArgumentException("Wrong data of existing client passed");
        }
        return clientEntity;
    }

    private boolean validate(ClientEntity entity, ClientData data) {
        return entity.getFirstName().equals(data.getFirstName())
                && entity.getLastName().equals(data.getLastName())
                && entity.getEmail().equals(data.getEmail())
                && entity.getSalary() == data.getSalary();
    }
}
